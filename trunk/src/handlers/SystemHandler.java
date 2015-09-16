package handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import json.JSONBuilder;
import cx.ast.Node;
import cx.ast.NodeCall;
import cx.ast.NodeString;
import cx.ast.Visitor;
import cx.runtime.Function;
import cx.runtime.Handler;

// provides static methods for system specifics functions
// void debug(...) - system out of all parameters

public class SystemHandler implements Handler {

	protected Visitor visitor;
	protected final String referenceName;

	public SystemHandler(String refName) {
		referenceName = refName;
	}

	public void init(Visitor visitor) {
		this.visitor = visitor;
		visitor.set(referenceName, this);
	}

	public static enum SystemMethod {
		print, split, tokenize, execute, isFile, isDirectory, fileMove, fileCopy, fileDelete, directoryCreate, directoryDelete, directoryList;

		public static SystemMethod parse(final String str) {
			String guess = null;
			final int length = str.length();
			SystemMethod method = null;
			char c;
			switch (length) {
				case 5:
					c = str.charAt(0);
					if (c == 'p') {
						guess = "print";
						method = print;
					} else if (c == 's') {
						guess = "split";
						method = split;
					}
					break;
				case 6:
					c = str.charAt(0);
					if (c == 'i') {
						guess = "isFile";
						method = isFile;
					}
					break;
				case 7:
					c = str.charAt(0);
					if (c == 'e') {
						guess = "execute";
						method = execute;
					}
					break;
				case 8:
					c = str.charAt(4);
					if (c == 'n') {
						guess = "tokenize";
						method = tokenize;
					} else if (c == 'M') {
						guess = "fileMove";
						method = fileMove;
					} else if (c == 'C') {
						guess = "fileCopy";
						method = fileCopy;
					}
					break;
				case 10:
					c = str.charAt(0);
					if (c == 'f') {
						guess = "fileDelete";
						method = fileDelete;
					}
					break;
				case 11:
					c = str.charAt(0);
					if (c == 'i') {
						guess = "isDirectory";
						method = isDirectory;
					}
					break;
				case 13:
					c = str.charAt(0);
					if (c == 'd') {
						guess = "directoryList";
						method = directoryList;
					}
					break;
				case 15:
					c = str.charAt(9);
					if (c == 'C') {
						guess = "directoryCreate";
						method = directoryCreate;
					} else if (c == 'D') {
						guess = "directoryDelete";
						method = directoryDelete;
					}
					break;
			}
			if ((guess != null) && !guess.equals(str)) {
				return null;
			}
			return method;
		}
	}

	public Class<?>[] supportedClasses() {
		return new Class<?>[] { SystemMethod.class, SystemHandler.class };
	}

	public void set(Object object, String variable, Object value) {}

	public Object get(Object thiz, String variable) {
		if (thiz instanceof SystemHandler) {
			// dynamic object calls are here
			SystemMethod method = SystemMethod.parse(variable);
			if (method != null) {
				return method;
			}
		}
		return null;
	}

	public Object call(Object object, Object[] args) {
		if (!(object instanceof SystemMethod)) {
			return null;
		}
		// dynamic object calls are here
		switch ((SystemMethod) object) {
			case print:
				// print(...)
				if (args != null && args.length > 0) {
					JSONBuilder builder = new JSONBuilder();
					for (Object arg : args) {
						if (arg instanceof String || arg instanceof Number) {
							System.out.print(arg.toString());
						} else {
							builder.reset();
							builder.addValue(arg);
							System.out.print(builder.toString());
						}
					}
					System.out.println();
				}
				return null;

			case split:
				// split(separatorChar, ...)
				if (args != null && args.length >= 2) {
					char separator;
					{
						Object _separator = args[0];
						if (_separator == null) {
							return null;
						}
						String str = _separator.toString();
						if (str.length() <= 0) {
							return null;
						}
						separator = str.charAt(0);
					}
					List<String> result = new ArrayList<String>();
					for (int i = 1; i < args.length; i++) {
						Object arg = args[i];
						if (arg == null) {
							continue;
						}
						String str = arg.toString();
						split(result, str, separator);
					}
					return result;
				}
				return null;

			case tokenize:
				// tokenize(separatorChar, ...)
				if (args != null && args.length >= 2) {
					char separator;
					{
						Object _separator = args[0];
						if (_separator == null) {
							return null;
						}
						String str = _separator.toString();
						if (str.length() <= 0) {
							return null;
						}
						separator = str.charAt(0);
					}
					List<String> result = new ArrayList<String>();
					for (int i = 1; i < args.length; i++) {
						Object arg = args[i];
						if (arg == null) {
							continue;
						}
						String str = arg.toString();
						tokenize(result, str, separator);
					}
					return result;
				}
				return null;
			case isFile:
				// isFile(fullFilewithPath)
				if (args != null && args.length > 0) {
					File file = new File(String.valueOf(args[0]));
					return file.exists() && file.isFile();
				}
				return null;
			case isDirectory:
				// isDirectory(fullDirectorywithPath)
				if (args != null && args.length > 0) {
					File file = new File(String.valueOf(args[0]));
					return file.exists() && file.isDirectory();
				}
				return null;
			case fileMove:
				// fileMove(sourceFullFilewithPath, destinationFullFilewithPath)
				if (args != null && args.length == 2) {
					File source = new File(String.valueOf(args[0]));
					File destination = new File(String.valueOf(args[1]));
					File destinationParent = destination.getParentFile();
					if (source.exists() && (source.isFile() || source.isDirectory()) && destinationParent != null
							&& destinationParent.exists() && destinationParent.isDirectory()) {
						if (!source.renameTo(destination)) {
							throw new IllegalStateException("cannot move " + source + " to " + destination);
						}
					}
				}
				return null;
			case fileCopy:
				// fileCopy(sourceFullFilewithPath, destinationFullFilewithPath)
				if (args != null && args.length == 2) {
					File source = new File(String.valueOf(args[0]));
					File destination = new File(String.valueOf(args[1]));
					File destinationParent = destination.getParentFile();
					if (source.exists() && (source.isFile() || source.isDirectory()) && destinationParent != null
							&& destinationParent.exists() && destinationParent.isDirectory()) {
						InputStream input = null;
						OutputStream output = null;
						try {
							input = new FileInputStream(source);
							output = new FileOutputStream(destination);
							byte[] buf = new byte[4096];
							int bytesRead;
							while ((bytesRead = input.read(buf)) > 0) {
								output.write(buf, 0, bytesRead);
							}
						} catch (Exception e) {
							throw new IllegalStateException(e.getMessage(), e);
						} finally {
							if (input != null) {
								try {
									input.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if (output != null) {
								try {
									output.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				return null;
			case fileDelete:
				// fileDelete(files,...)
				if (args != null && args.length > 0) {
					for (Object arg : args) {
						if (arg == null) continue;
						File file = new File(arg.toString());
						if (file.exists() && file.isFile()) {
							if (!file.delete()) {
								throw new IllegalStateException("Cannot delete file " + file);
							}
						}
					}
				}
				return null;
			case directoryCreate:
				// directoryCreate(directories,...)
				if (args != null && args.length > 0) {
					for (Object arg : args) {
						if (arg == null) continue;
						File directory = new File(arg.toString());
						if (!directory.exists()) {
							if (!directory.mkdirs()) {
								throw new IllegalStateException("Cannot create directory " + directory);
							}
						}
					}
				}
				return null;
			case directoryDelete:
				// directoryDelete(directories,...)
				if (args != null && args.length > 0) {
					for (Object arg : args) {
						if (arg == null) continue;
						File directory = new File(arg.toString());
						if (directory.exists() && directory.isDirectory()) {
							if (!deleteFolderContent(directory)) {
								throw new IllegalStateException("Cannot delete directory " + directory);
							}
						}
					}
				}
				return null;
			case directoryList:
				// directoryDelete(directories,... listener(entryName{}))
				if (args != null && args.length >= 2) {
					final int argLen = args.length - 1;
					final Function function;
					{
						Object _function = args[argLen];
						if (_function == null || !(_function instanceof Function)) {
							return null;
						}
						function = (Function) _function;
					}
					final List<Node> arguments = new ArrayList<Node>(1);
					arguments.add(null);
					for (int i = 0; i < argLen; i++) {
						Object arg = args[i];
						if (arg == null) continue;

						final File directory = new File(arg.toString());
						if (directory.exists() && directory.isDirectory()) {
							for (File dirEntry : directory.listFiles()) {
								// build arguments
								arguments.set(0, new NodeString(function.body.position, dirEntry.getAbsolutePath()));
								// do call to listener
								NodeCall call = new NodeCall(function.body.position, function.body, arguments);
								visitor.visitCall(call);
							}
						}
					}
				}
				return null;
			case execute:
				// execute(application, parameters...) returns the stdout
				if (args != null && args.length >= 1) {
					List<String> arguments = new ArrayList<String>(args.length);
					for (Object arg : args) {
						if (arg != null) {
							arguments.add(arg.toString());
						}
					}
					if (arguments.size() > 0) {
						return execute(arguments);
					}
				}
				return null;
			default:
				return null;
		}
	}

	public String[] supportedStaticCalls() {
		return new String[] { "print" };
	}

	public Object staticCall(String method, Object[] args) {
		SystemMethod dbStaticMethod = SystemMethod.parse(method);
		if (dbStaticMethod != null) {
			switch (dbStaticMethod) {
				case print:
					if (args != null && args.length > 0) {
						JSONBuilder builder = new JSONBuilder();
						for (Object arg : args) {
							if (arg instanceof String || arg instanceof Number) {
								System.out.print(arg.toString());
							} else {
								builder.reset();
								builder.addValue(arg);
								System.out.print(builder.toString());
							}
						}
						System.out.println();
					} else {
						return null;
					}
				default:
					break;
			}
		}
		return null;
	}

	/** return occurrences of separator in string */
	public static int count(final String string, final char separator) {
		int count = 0;
		for (int i = 0, len = string.length(); i < len; i++) {
			final char c = string.charAt(i);
			if (c == separator) count++;
		}
		return count;
	}

	/** split string by separator without empties */
	public static void split(List<String> tokens, final String string, final char separator) {
		if (string == null || string.length() <= 0) {
			return;
		}
		int count = count(string, separator);
		if (count <= 0) {
			tokens.add(string);
			return;
		}

		int i = 0, len = string.length(), s = 0;
		for (; i < len; i++) {
			final char c = string.charAt(i);
			if (c == separator) {
				if (i - s > 0) {
					final String addstr = string.substring(s, i).trim();
					if (addstr.length() > 0) tokens.add(addstr);
				}
				s = i + 1;
			}
		}
		// if there is remaining string - add it
		if (len - s > 0) {
			final String addstr = string.substring(s, i).trim();
			if (addstr.length() > 0) tokens.add(addstr);
		}
	}

	/** split string by separator with empties */
	public static void tokenize(List<String> tokens, final String string, final char separator) {
		if (string == null || string.length() <= 0) {
			return;
		}
		int count = count(string, separator);
		if (count <= 0) {
			tokens.add(string);
			return;
		}

		int i = 0, len = string.length(), s = 0;
		for (; i < len; i++) {
			final char c = string.charAt(i);
			if (c == separator) {
				final String addstr = string.substring(s, i).trim();
				tokens.add(addstr);
				s = i + 1;
			}
		}
		final String addstr = string.substring(s, i).trim();
		tokens.add(addstr);
	}

	public static boolean deleteFolderContent(File directory) {
		if (directory.isDirectory()) {
			for (File folderEntry : directory.listFiles()) {

				if (folderEntry.isDirectory()) {
					// delete folder content
					deleteFolderContent(folderEntry);
				}
				folderEntry.delete();
			}
			return directory.delete();
		}
		return false;
	}

	public static String execute(List<String> arguments) {
		if (arguments.size() <= 0) return null;
		ProcessBuilder pb = new ProcessBuilder(arguments);

		Process p;
		try {
			p = pb.start();
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		SystemHandlerReadThread stdout = new SystemHandlerReadThread("stdin " + arguments.get(0), p.getInputStream());
		SystemHandlerReadThread stderr = new SystemHandlerReadThread("stderr " + arguments.get(0), p.getErrorStream());
		stdout.start();
		stderr.start();

		while (isAlive(p)) {
			Thread.yield();
		}
		p.destroy();

		StringBuilder result = new StringBuilder(stdout.buffer.length() + stderr.buffer.length());
		if (stdout.buffer.length() > 0) {
			result.append(stdout.buffer);
		} else {
			result.append(stderr.buffer);
		}

		return result.toString();
	}

	private static boolean isAlive(Process p) {
		try {
			p.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}
}