package json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class JSONSAXParser {
	private Reader reader;
	private int parserLineNr = 0;
	private int parserLinePos = 0;
	private int parserLineOffset = 0;
	private final JSONSAXListener listener;
	private final StringBuilder stringbuffer = new StringBuilder(4096);

	public static String parse(String json, JSONSAXListener listener) {
		try {
			new JSONSAXParser(new StringReader(json), listener);
			return null;
		} catch (Exception exception) {
			return exception.getMessage();
		}
	}

	public static String parse(Reader reader, JSONSAXListener listener) {
		try {
			new JSONSAXParser(reader, listener);
			return null;
		} catch (Exception exception) {
			return exception.getMessage();
		}
	}

	public JSONSAXParser(String json, JSONSAXListener listener) throws IOException, Exception {
		this.listener = listener;
		parseFromReader(new StringReader(json));
	}

	public JSONSAXParser(Reader reader, JSONSAXListener listener) throws IOException, Exception {
		this.listener = listener;
		parseFromReader(reader);
	}

	private void parseFromReader(Reader reader) throws IOException, Exception {
		this.reader = reader;
		scanElement();
	}

	protected final int readChar() throws IOException, Exception {
		final int i = reader.read();
		parserLinePos++;
		parserLineOffset++;
		if (i == '\n') {
			parserLineNr++;
			parserLinePos = 0;
		}
		return i;
	}

	protected final int scanWhitespace() throws IOException, Exception {
		do {
			int c = readChar();
			if (c == -1 || c > ' ') {
				return c;
			}
		} while (true);
	}

	protected String scanString(char terminationChar) throws IOException, Exception {
		stringbuffer.setLength(0);
		do {
			int i = readChar();
			if (i == -1) {
				throw expectedInput(terminationChar + " to end the string.");
			}
			char c = (char) i;
			if (c == terminationChar) {
				return stringbuffer.toString();
			} else if (c == '\\') {
				// escaping
				i = readChar();
				if (i == -1) {
					throw syntaxError("Not finished escaping");
				}
				c = (char) i;

				if (c == '\'') {
					stringbuffer.append(c);
				} else if (c == '\"') {
					stringbuffer.append(c);
				} else if (c == '\\') {
					stringbuffer.append(c);
				} else if (c == 'n') {
					stringbuffer.append('\n');
				} else if (c == 'r') {
					stringbuffer.append('\r');
				} else if (c == 't') {
					stringbuffer.append('\t');
				} else if (c == 'f') {
					stringbuffer.append('\f');
				} else if (c == 'b') {
					stringbuffer.append('\b');
				} else if (c == '0') {
					stringbuffer.append('\0');
				} else if (c == 'u') {
					// read next 4 hex values for UNICODE string
					int value = 0;
					for (int x = 0; x < 4; x++) {
						i = readChar();
						if (i == -1) {
							throw syntaxError("Not finished escaping");
						}
						int v = toHex((char) i);
						if (v == -1) {
							throw syntaxError("Malformed \\uxxxx encoding.");
						}
						value = (value << 4) + v;
					}

					stringbuffer.append((char) value);
				} else {
					throw syntaxError("Unrecognized \\escaping.");
				}
			} else {
				stringbuffer.append(c);
			}
		} while (true);

	}

	protected void scanObject() throws IOException, Exception {
		listener.startObject();

		do {
			int i = scanWhitespace();
			if (i == -1) {
				throw expectedInput("}");
			}
			if (i == '\"') {
				scanString((char) i);
			} else if (i == '\'') {
				scanString((char) i);
			} else {
				stringbuffer.setLength(0);
				stringbuffer.append((char) i);
				do {
					i = readChar();
					if (i == -1) {
						throw syntaxError("Unexpected end of stream.");
					} else if (i < ' ' || i == ':') {
						break;
					} else {
						stringbuffer.append((char) i);
					}
				} while (true);
			}

			String key = stringbuffer.toString();
			listener.key(key);

			// check if : was already found, or skip to it
			if (i != ':') {
				i = scanWhitespace();
				if (i != ':') {
					throw syntaxError("Expecting ':'");
				}
			}

			i = scanElement();
			while (i != -1 && i <= ' ') {
				i = scanWhitespace();
			}
			if (i == '}') {
				break;
			} else if (i == ',') {
				continue;
			} else {
				throw syntaxError("Expecting '}' or ','");
			}
		} while (true);
		listener.endObject();
	}

	protected int scanElement() throws IOException, Exception {
		// recognizes boolean, long, double, string, array, object
		int i = scanWhitespace();

		if (i == -1) {
			return i;

		} else if (i == '{') {
			// new object
			scanObject();
			i = readChar();

		} else if (i == '[') {
			// new array
			listener.startArray();
			do {
				i = scanElement();
				while (i != -1 && i <= ' ') {
					i = scanWhitespace();
				}
				if (i == ']') {
					break;
				} else if (i == ',') {
					continue;
				} else {
					throw syntaxError("Expecting ']' or ','");
				}
			} while (true);
			listener.endArray();
			i = readChar();

		} else if (i == '+' || i == '-' || (i >= '0' && i <= '9')) {
			// long or double
			stringbuffer.setLength(0);
			stringbuffer.append((char) i);

			i = addDigits();

			boolean decimal = false;
			if (i == '.') {
				decimal = true;
				stringbuffer.append((char) i);
				i = addDigits();
			}

			if ((i == 'e') || (i == 'E')) {
				decimal = true;
				stringbuffer.append((char) i);
				i = readChar();
				if (i == '+' || i == '-') {
					stringbuffer.append((char) i);
				}

				i = addDigits();
			}

			if (decimal) {
				listener.value(toDouble(stringbuffer.toString()));
			} else {
				listener.value(toLong(stringbuffer.toString()));
			}

		} else if (i == '\'') {
			// string
			scanString((char) i);
			listener.value(stringbuffer.toString());
			i = readChar();

		} else if (i == '\"') {
			// string
			scanString((char) i);
			listener.value(stringbuffer.toString());
			i = readChar();

		} else {
			stringbuffer.setLength(0);
			stringbuffer.append((char) i);
			do {
				i = readChar();
				if (i == -1) {
					break;
				} else if (!Character.isLetter((char) i)) {
					break;
				} else {
					stringbuffer.append((char) i);
				}
			} while (true);

			String constantString = stringbuffer.toString();
			if ("true".equals(constantString)) {
				listener.value(true);
			} else if ("false".equals(constantString)) {
				listener.value(false);
			} else if ("null".equals(constantString)) {
				listener.value(null);
			} else {
				listener.value(stringbuffer.toString());
			}
		}
		return i;
	}

	private final int addDigits() throws IOException, Exception {
		do {
			int i = readChar();
			if (i >= '0' && i <= '9') {
				stringbuffer.append((char) i);
			} else {
				return i;
			}
		} while (true);
	}

	public static long toLong(String s) {
		return toLong(s, 0);
	}

	public static long toLong(String str, int i) {
		if (str == null) {
			return i;
		}
		try {
			return Long.parseLong(str, 10);
		} catch (NumberFormatException numberformatexception) {
			return i;
		}
	}

	public static double toDouble(String s) {
		return toDouble(s, 0.0D);
	}

	public static double toDouble(String s, double d) {
		if (s == null) {
			return d;
		}
		try {
			return Double.valueOf(s).doubleValue();
		} catch (NumberFormatException numberformatexception) {
			return d;
		}
	}

	public static boolean toBoolean(String s) {
		if (s == null) {
			return false;
		}
		return Boolean.valueOf(s).booleanValue();
	}

	protected Exception syntaxError(String s) {
		return new Exception("line:" + parserLineNr + " pos:" + parserLinePos + " (offset:" + parserLineOffset + ") "
				+ s);
	}

	protected Exception expectedInput(String s) {
		return new Exception("line:" + parserLineNr + " pos:" + parserLinePos + " (offset:" + parserLineOffset + ") "
				+ s);
	}

	public final static int toHex(char c) {
		switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return c - '0';

			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				return 0x0A + (c - 'a');

			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				return 0x0A + (c - 'A');
			case 58: // ':'
			case 59: // ';'
			case 60: // '<'
			case 61: // '='
			case 62: // '>'
			case 63: // '?'
			case 64: // '@'
			case 71: // 'G'
			case 72: // 'H'
			case 73: // 'I'
			case 74: // 'J'
			case 75: // 'K'
			case 76: // 'L'
			case 77: // 'M'
			case 78: // 'N'
			case 79: // 'O'
			case 80: // 'P'
			case 81: // 'Q'
			case 82: // 'R'
			case 83: // 'S'
			case 84: // 'T'
			case 85: // 'U'
			case 86: // 'V'
			case 87: // 'W'
			case 88: // 'X'
			case 89: // 'Y'
			case 90: // 'Z'
			case 91: // '['
			case 92: // '\\'
			case 93: // ']'
			case 94: // '^'
			case 95: // '_'
			case 96: // '`'
			default:
				return -1;
		}
	}
}
