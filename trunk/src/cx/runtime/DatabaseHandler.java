package cx.runtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import cx.ast.Node;
import cx.ast.NodeCall;
import cx.ast.NodeNumber;
import cx.ast.NodeString;
import cx.ast.SourcePosition;
import cx.ast.Visitor;

public class DatabaseHandler implements Handler {
	protected Visitor visitor;

	public void init(Visitor visitor) {
		// defines global object for Database operations
		this.visitor = visitor;
		visitor.set("Database", this);
	}

	public static enum DatabaseMethod {
		open, connect, execute, commit, rollback, close, setProperty, setConnectionString;

		public static DatabaseMethod parse(final String str) {
			String guess = null;
			final int length = str.length();
			DatabaseMethod method = null;

			switch (length) {
				case 4:
					if (str.charAt(0) == 'o') {
						guess = "open";
						method = open;
					}
					break;
				case 5:
					if (str.charAt(0) == 'c') {
						guess = "close";
						method = close;
					}
					break;
				case 6:
					if (str.charAt(0) == 'c') {
						guess = "commit";
						method = commit;
					}
					break;
				case 7:
					if (str.charAt(0) == 'e') {
						guess = "execute";
						method = execute;
					} else if (str.charAt(3) == 'n') {
						guess = "connect";
						method = connect;
					}
					break;
				case 8:
					if (str.charAt(0) == 'r') {
						guess = "rollback";
						method = rollback;
					}
					break;
				case 11:
					if (str.charAt(0) == 's') {
						guess = "setProperty";
						method = setProperty;
					}
					break;
				case 19:
					if (str.charAt(0) == 's') {
						guess = "setConnectionString";
						method = setConnectionString;
					}
					break;
			}
			if ((guess != null) && !guess.equals(str)) {
				return null;
			}
			return method;
		}
	}

	private static class DatabaseObject {
		String connectionString;
		Properties connectionProperties;
		Connection connection;
		String error;

		DatabaseObject(String connectionString) {
			connectionProperties = new Properties();
			this.connectionString = connectionString;
			this.connection = null;
			error = "";
		}

		public DatabaseObject setProperty(String property, String value) {
			if (property != null) {
				if (value == null) {
					connectionProperties.remove(property);
				} else {
					connectionProperties.setProperty(property, value);
				}
			}
			return this;
		}

		public boolean open(String connectionString) {
			Connection conn;
			try {
				conn = DriverManager.getConnection(connectionString, connectionProperties);
				if (conn != null) {
					try {
						conn.setAutoCommit(false);
					} catch (SQLException e) {
						conn = null;
						error = "Cannot set up commit state connection!";
					}
				}
			} catch (Exception e) {
				conn = null;
				error = "Cannot create a DB Connection!";
			}
			if (connection != null) {
				close();
			}
			this.connectionString = connectionString;
			connection = conn;
			return conn != null;
		}

		public boolean execute(String sql, Visitor visitor, Object functionObject) {
			if (connection == null || sql == null) {
				return false;
			}
			ResultSet resultSet = null;
			Statement statement = null;
			boolean update = true;
			{// check if SQL start with "SELECT" otherwise do update;
				int p = 0;
				for (; p < sql.length() && sql.charAt(p) <= ' '; p++)
					;
				if (p + 6 <= sql.length()) {
					if ("SELECT".equals(sql.substring(p, p + 6))) {
						update = false;
					}
				}

			}
			try {
				statement = connection.createStatement();

				if (update) {
					statement.executeUpdate(sql);
				} else {
					resultSet = statement.executeQuery(sql);
				}

			} catch (SQLException e) {
				error = e.getMessage();
				close(statement);
				close(resultSet);
				return false;
			}

			error = "";
			if (resultSet != null) {
				if (visitor != null && functionObject instanceof Function) {
					try {
						// prepare function arguments
						Function function = (Function) functionObject;
						final int argsLen;
						ResultSetMetaData meta = resultSet.getMetaData();
						{
							int l1 = function.body.argumentNames.length;
							int l2 = meta.getColumnCount();
							argsLen = Math.min(l1, l2);
						}
						List<Node> arguments = new ArrayList<Node>(argsLen);
						int[] argumentsType = new int[argsLen];
						for (int i = 0; i < argsLen; i++) {
							argumentsType[i] = meta.getColumnType(i + 1);
							arguments.add(null);
						}
						SourcePosition position = function.body.position;

						while (next(resultSet)) {
							// fill up the arguments
							for (int i = 0; i < argsLen; i++) {
								switch (argumentsType[i]) {
									case Types.DOUBLE:
									case Types.FLOAT:
									case Types.DECIMAL:
										arguments.set(i, new NodeNumber(position, resultSet.getString(i + 1),
												new Double(resultSet.getDouble(i + 1))));
										break;

									case Types.INTEGER:
									case Types.BIGINT:
									case Types.SMALLINT:
									case Types.TINYINT:
										arguments.set(i, new NodeNumber(position, resultSet.getString(i + 1), new Long(
												resultSet.getLong(i + 1))));
										break;

									case Types.DATE:
									case Types.TIME:
									case Types.TIMESTAMP:
										arguments.set(i, new NodeNumber(position, resultSet.getString(i + 1), new Long(
												resultSet.getDate(i + 1).getTime())));
										break;

									default:
										arguments.set(i, new NodeString(position, resultSet.getString(i + 1)));
								}

							}
							// do call to listener
							NodeCall call = new NodeCall(position, function.body, arguments);
							visitor.visitCall(call);
						}

					} catch (SQLException e) {
						error = e.getMessage();
						close(statement);
						close(resultSet);
						return false;
					}
				}
			}

			close(statement);
			close(resultSet);
			return true;
		}

		public boolean next(ResultSet rs) {
			if (rs != null) {
				try {
					return rs.next();
				} catch (SQLException e) {
					error = "Cannot position at next element! " + e.getMessage();
				}
			}
			return false;
		}

		public final void close(ResultSet rs) {
			if (rs == null) {
				return;
			}
			try {
				rs.close();
			} catch (Exception e) {
				// not interested
			}
		}

		public final void close(Statement statement) {
			if (statement == null) {
				return;
			}
			try {
				statement.close();
			} catch (Exception e) {
				// not interested
			}
		}

		public boolean commit() {
			boolean result = false;
			if (connection != null) {
				try {
					connection.commit();
					result = true;
				} catch (SQLException e) {
					error = "Cannot commit DB Connection! " + e.getMessage();
					try {
						connection.rollback();
					} catch (SQLException x) {}
				}
			}
			return result;
		}

		public boolean rollback() {
			boolean result = false;
			if (connection != null) {
				try {
					connection.rollback();
					result = true;
				} catch (SQLException e) {
					error = "Cannot rollback DB Connection! " + e.getMessage();
				}
			}
			return result;
		}

		public boolean close() {
			if (connection == null) {
				return true;
			}
			error = "";
			try {
				connection.close();
				return true;
			} catch (SQLException e) {
				error = "Cannot close DB Connection: " + e.getMessage();
			}
			return false;
		}
	}

	private static class DatabaseCall {
		private final DatabaseObject db;
		private final DatabaseMethod method;

		DatabaseCall(DatabaseObject db, DatabaseMethod method) {
			this.db = db;
			this.method = method;
		}
	}

	public boolean accept(Object object) {
		return object instanceof DatabaseObject || object instanceof DatabaseCall || object instanceof DatabaseHandler;
	}

	public void set(Object object, String variable, Object value) {
		if (object instanceof DatabaseObject) {
			if ("connectionString".equals(variable)) {
				((DatabaseObject) object).connectionString = (value == null) ? "" : value.toString();
			}
		}
	}

	public Object get(Object thiz, String variable) {
		if (thiz instanceof DatabaseObject) {
			if ("connectionString".equals(variable)) {
				return ((DatabaseObject) thiz).connectionString;
			}
			if ("error".equals(variable)) {
				return ((DatabaseObject) thiz).error;
			}
			DatabaseMethod method = DatabaseMethod.parse(variable);
			if (method != null) {
				return new DatabaseCall((DatabaseObject) thiz, method);
			}
		} else if (thiz instanceof DatabaseHandler) {
			if ("create".equals(variable)) {
				DatabaseObject db = new DatabaseObject("");
				DatabaseCall call = new DatabaseCall(db, DatabaseMethod.setConnectionString);
				return call;
			}
		}
		return null;
	}

	public Object call(Object object, Object[] args) {
		if (!(object instanceof DatabaseCall)) {
			return null;
		}

		DatabaseCall databaseCall = (DatabaseCall) object;

		Object result = null;
		switch (databaseCall.method) {
			case open:
			case connect:
				if (args.length >= 1) {
					String connectionStr = null;
					if (args[0] != null) {
						connectionStr = String.valueOf(args[0]);
					}
					result = databaseCall.db.open(connectionStr);
				} else {
					result = databaseCall.db.open(databaseCall.db.connectionString);
				}
				break;
			case execute:
				if (args.length == 1) {
					String sql = null;
					if (args[0] != null) {
						sql = String.valueOf(args[0]);
					}
					result = databaseCall.db.execute(sql, visitor, null);
				} else if (args.length >= 2) {
					String sql = null;
					if (args[0] != null) {
						sql = String.valueOf(args[0]);
					}
					result = databaseCall.db.execute(sql, visitor, args[1]);
				}
				break;
			case commit:
				result = databaseCall.db.commit();
				break;
			case rollback:
				result = databaseCall.db.rollback();
				break;
			case close:
				result = databaseCall.db.close();
				break;
			case setProperty:
				if (args.length == 1) {
					String property = null;
					if (args[0] != null) {
						property = String.valueOf(args[0]);
					}
					result = databaseCall.db.setProperty(property, null);

				} else if (args.length >= 2) {
					String property = null;
					if (args[0] != null) {
						property = String.valueOf(args[0]);
					}
					String value = null;
					if (args[1] != null) {
						value = String.valueOf(args[1]);
					}
					result = databaseCall.db.setProperty(property, value);
				}
				break;
			case setConnectionString:
				if (args.length >= 1) {
					String connectionStr = null;
					if (args[0] != null) {
						connectionStr = String.valueOf(args[0]);
					}
					databaseCall.db.connectionString = connectionStr;
				} else {
					databaseCall.db.connectionString = databaseCall.db.connectionString;
				}
				result = databaseCall.db;
				break;
		}
		return result;
	}

	public boolean acceptStaticCall(String method, Object[] args) {
		return ((args.length == 1) && ("connect".equals(method) || "open".equals(method)));
	}

	public Object staticCall(String method, Object[] args) {
		if (args.length == 1) {
			if ("connect".equals(method) || "open".equals(method)) {
				DatabaseObject db = new DatabaseObject(String.valueOf(args[0]));
				return db;
			}
		}
		return null;
	}
}