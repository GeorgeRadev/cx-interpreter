package cx.handlers;

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
import cx.runtime.Function;
import cx.runtime.Handler;

//Database Handler provides a wrapper around JDBC implementations. 
// it registers the following object:
//Database {
//	static Database open(connectionString);
//	static Database connect(connectionString);
//	static void classForName(className);
//	
//	Database create();
//	
//	Database open();
//	Database connect();
//	boolean execute(sql);
//	boolean execute(sql, callback(parameters, that, match, the, select, columns) {...});
//	commit();
//	rollback();
//	close();
//	setProperty();
//	String setConnectionString // variable 	
//}

// provided methods are used like:
// if the registered handler is with name Database then
// Database.classForName("org.sqlite.JDBC); // create JDBC instance
// var db = Database.create(); // create JDBC instance
// var db = Database.create(connectionString); // create JDBC instance
//
// db.setProperty(key,value); // may be used before open/connect if JDBC driver requires properties
// db.setConnectionString = "new connection string"; // may be used 
//
// // the following four methods are initializing the connection
// // and return true if everything is ok
// // otherwize the error is in db.error
// db.open();
// db.open(connectionstring);
// db.connect(); 
// db.connect(connectionString);
// 
// // when db connection is ok the following methods can be used
// db.execute(sql);
// db.execute(sql, callback(parameters, that, match, the, select, columns) {...});
// db.commit(); 
//  db.rollback(); 
//
//  // to close the connection:
//  db.close();
//}

//Handler can be added to the Context like:
//Context cx = new Context();
//cx.addHandler(new DatabaseHandler("Database")); // register database handler under name "Database"

public class DatabaseHandler implements Handler {
	protected Visitor visitor;
	protected final String referenceName;

	public DatabaseHandler() {
		referenceName = "Database";
	}

	public DatabaseHandler(String refName) {
		referenceName = refName;
	}

	public void init(Visitor visitor) {
		// defines global object for Database operations
		this.visitor = visitor;
		visitor.set(referenceName, this);
	}

	public static enum DatabaseMethod {
		open, connect, execute, commit, rollback, close, setProperty, setConnectionString, classForName, create;

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
					if (str.charAt(1) == 'o') {
						guess = "commit";
						method = commit;
					} else if (str.charAt(1) == 'r') {
						guess = "create";
						method = create;
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
				case 12:
					if (str.charAt(0) == 'c') {
						guess = "classForName";
						method = classForName;
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
						error = "Cannot set up commit state connection! " + e.getMessage();
					}
				}
			} catch (Exception e) {
				conn = null;
				error = "Cannot create a DB Connection! " + e.getMessage();
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

	public Object[] supportedClasses() {
		return new Object[] { DatabaseObject.class, DatabaseCall.class, DatabaseHandler.class };
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
			DatabaseMethod method = DatabaseMethod.parse(variable);
			if (method != null) {
				switch (method) {
					case create: {
						DatabaseObject db = new DatabaseObject("");
						DatabaseCall call = new DatabaseCall(db, DatabaseMethod.setConnectionString);
						return call;
					}
					case classForName: {
						DatabaseCall call = new DatabaseCall(null, DatabaseMethod.classForName);
						return call;
					}
					default:
						return null;
				}
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
			case classForName:
				for (Object arg : args) {
					classForName(String.valueOf(arg));
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
			default:
				break;
		}
		return result;
	}

	public String[] supportedStaticCalls() {
		return new String[] { "connect", "open" };
	}

	public Object staticCall(String method, Object[] args) {
		DatabaseMethod dbStaticMethod = DatabaseMethod.parse(method);
		if (dbStaticMethod != null) {
			switch (dbStaticMethod) {
				case connect:
				case open:
					if (args.length > 0) {
						DatabaseObject db = new DatabaseObject(String.valueOf(args[0]));
						return db;
					} else {
						return null;
					}
				default:
					break;
			}
		}
		return null;
	}

	public void classForName(String className) {
		if (className != null && className.length() > 0) {
			try {
				Class.forName(className);
			} catch (ClassNotFoundException e) {}
		}
	}
}