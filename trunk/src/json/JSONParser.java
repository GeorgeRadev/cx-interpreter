package json;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONParser {

	private static final Object OBJECT_END = Character.valueOf(']');
	private static final Object ARRAY_END = Character.valueOf('}');
	private static final Object COLON = Character.valueOf(':');
	private static final Object COMMA = Character.valueOf(',');
	private static final Object PARSE_END = Character.valueOf(StringCharacterIterator.DONE);
	private static Map<Character, Character> escapes = new HashMap<Character, Character>();

	static {
		escapes.put(Character.valueOf('"'), Character.valueOf('"'));
		escapes.put(Character.valueOf('\\'), Character.valueOf('\\'));
		escapes.put(Character.valueOf('/'), Character.valueOf('/'));
		escapes.put(Character.valueOf('b'), Character.valueOf('\b'));
		escapes.put(Character.valueOf('f'), Character.valueOf('\f'));
		escapes.put(Character.valueOf('n'), Character.valueOf('\n'));
		escapes.put(Character.valueOf('r'), Character.valueOf('\r'));
		escapes.put(Character.valueOf('t'), Character.valueOf('\t'));
	}

	private CharacterIterator it;
	private char c;
	private Object token;
	private StringBuilder buf = new StringBuilder();

	public JSONParser() {
	}

	public Map<Object, Object> parseJSONString(char[] json) throws Exception {
		return parseJSONString(new String(json));
	}

	@SuppressWarnings("unchecked")
	public Map<Object, Object> parseJSONString(String json) throws Exception {
		if (json == null || json.length() <= 0) {
			throw new Exception("expecting non-null string!");
		}
		Object obj = read(json);
		if (obj instanceof Map) {
			return (Map<Object, Object>) obj;
		} else {
			return null;
		}
	}

	private char next() {
		c = it.next();
		return c;
	}

	private void skipWhiteSpace() {
		while (c <= ' ') {
			next();
		}
	}

	private Object read(String string) throws Exception {
		if (string == null) {
			throw new Exception("null cannot be parsed - please, pass a JSON string!");
		}
		it = new StringCharacterIterator(string);
		c = it.first();
		return read();
	}

	private Object read() throws Exception {
		Object ret = null;
		skipWhiteSpace();

		if (c == '"') {
			next();
			ret = string('"');
		} else if (c == '\'') {
			next();
			ret = string('\'');
		} else if (c == '[') {
			next();
			ret = array();
		} else if (c == ']') {
			ret = ARRAY_END;
			next();
		} else if (c == ',') {
			ret = COMMA;
			next();
		} else if (c == '{') {
			next();
			ret = object();
		} else if (c == '}') {
			ret = OBJECT_END;
			next();
		} else if (c == ':') {
			ret = COLON;
			next();
		} else if (Character.isDigit(c) || (c == '-')) {
			ret = number();
		} else if (Character.isLetter(c) || c == '_') {
			ret = identifier();
			if ("true".equals(ret)) {
				ret = Boolean.TRUE;
			} else if ("false".equals(ret)) {
				ret = Boolean.FALSE;
			} else if ("null".equals(ret)) {
				ret = null;
			}
			// next
		} else if (c == StringCharacterIterator.DONE) {
			ret = PARSE_END;
		} else {
			throw new Exception("Input string is not well formed JSON (invalid char " + c + " at index "
					+ it.getIndex() + ")");
		}

		token = ret;

		return ret;
	}

	private Map<String, Object> object() throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>(32);
		Object next = read();
		if (next != OBJECT_END) {
			String key = "";
			if (next instanceof String) {
				key = (String) next;
			} else {
				throw new Exception("Input string is not well formed JSON (missing key at index " + it.getIndex() + ")");
			}

			while (token != OBJECT_END) {
				read();
				if (token != COLON) {
					throw new Exception("Input string is not well formed JSON (missing colon at index " + it.getIndex()
							+ ")");
				}
				if (token != OBJECT_END) {
					ret.put(key, read());

					if (c == StringCharacterIterator.DONE) {
						throw new Exception(
								"Input string is not well formed JSON (missing closing bracket for object at index "
										+ it.getIndex() + ")");
					}

					if (read() == COMMA) {
						Object name = read();

						if (name instanceof String) {
							key = (String) name;
						} else {
							throw new Exception("Input string is not well formed JSON (missing key at index "
									+ it.getIndex() + ")");
						}
					}
				}
			}
		}

		return ret;
	}

	private List<Object> array() throws Exception {
		List<Object> ret = new ArrayList<Object>(32);
		Object value = read();

		while (token != ARRAY_END) {
			ret.add(value);

			if (c == StringCharacterIterator.DONE) {
				throw new Exception("Input string is not well formed JSON (missing closing bracket for array at index "
						+ it.getIndex() + ")");
			}

			if (read() == COMMA) {
				value = read();
			}
		}

		return ret;
	}

	private Object number() {
		buf.setLength(0);

		if (c == '-') {
			add();
		}

		addDigits();

		boolean decimal = false;
		if (c == '.') {
			add();
			addDigits();
			decimal = true;
		}

		if ((c == 'e') || (c == 'E')) {
			decimal = true;
			add();

			if ((c == '+') || (c == '-')) {
				add();
			}

			addDigits();
		}

		return (decimal) ? (Object) Double.parseDouble(buf.toString()) : (Object) Long.parseLong(buf.toString());
	}

	/** letter digi t_ . */
	private Object identifier() {
		buf.setLength(0);
		add(c);
		while (c == '_' || c == '.' || Character.isLetter(c) || Character.isDigit(c)) {
			add();
		}
		return buf.toString();
	}

	private Object string(char quote) throws Exception {
		buf.setLength(0);

		while (c != quote && c != StringCharacterIterator.DONE) {
			if (c == '\\') {
				next();

				if (c == 'u') {
					add(unicode());
				} else {
					Object value = escapes.get(Character.valueOf(c));

					if (value != null) {
						add(((Character) value).charValue());
					}
				}
			} else {
				add();
			}
		}

		if (c == StringCharacterIterator.DONE) {
			throw new Exception("Input string is not well formed JSON (missing quotes at index " + it.getIndex() + ")");
		}
		next();

		return buf.toString();
	}

	private void add(char cc) {
		buf.append(cc);
		next();
	}

	private void add() {
		add(c);
	}

	private void addDigits() {
		while (Character.isDigit(c)) {
			add();
		}
	}

	private char unicode() {
		int value = 0;

		for (int i = 0; i < 4; ++i) {
			int v = toHex(next());
			if (v == -1) {
				throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
			}
			value = (value << 4) + v;
		}

		return (char) value;
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

	/*
	 * utility methods for reading maps
	 */

	public static Object getObject(Object jsonObj, Object... path) throws Exception {
		Object trace = jsonObj;
		for (Object token : path) {
			if (trace instanceof Map) {
				trace = ((Map<?, ?>) trace).get(token);

			} else if (trace instanceof List && token instanceof Number) {
				int ix = ((Number) token).intValue();
				List<?> list = ((List<?>) trace);

				if (list.size() <= ix) {
					throw new Exception("json Path " + Arrays.asList(path).toString() + " list [" + token
							+ "]  exceeds size!");
				}
				if (list.size() <= ix) {
					throw new Exception("json Path " + Arrays.asList(path).toString() + " list [" + token
							+ "] doesnot contains such index!");
				}
				trace = ((List<?>) trace).get(ix);

			} else {
				throw new Exception("json Path " + Arrays.asList(path).toString() + " element [" + token
						+ "] not reachable!");
			}

			if (trace == null) {
				throw new Exception("json Path " + Arrays.asList(path).toString() + " element [" + token
						+ "] not reachable!");
			}
		}
		return trace;
	}

	public static List<?> getList(Object jsonObj, Object... path) throws Exception {
		Object result = getObject(jsonObj, path);
		if (result instanceof List || result == null) {
			return (List<?>) result;
		} else {
			throw new Exception("json Path " + Arrays.asList(path).toString() + " doesnot pints to a list!");
		}
	}

	public static Map<?, ?> getMap(Object jsonObj, Object... path) throws Exception {
		Object result = getObject(jsonObj, path);
		if (result instanceof Map) {
			return (Map<?, ?>) result;
		} else {
			throw new Exception("json Path " + Arrays.asList(path).toString() + " doesnot points to a map!");
		}
	}

	public static String getString(Object jsonObj, Object... path) throws Exception {
		Object result = getObject(jsonObj, path);
		return String.valueOf(result);
	}

	public static boolean getBoolean(Object jsonObj, Object... path) throws Exception {
		Object result = getObject(jsonObj, path);
		if (result instanceof Boolean) {
			return Boolean.TRUE.equals(result);
		} else {
			throw new Exception("json Path " + Arrays.asList(path).toString() + " doesnot points to a boolean!");
		}
	}

	public static Number getNumber(Object jsonObj, Object... path) throws Exception {
		Object result = getObject(jsonObj, path);
		if (result instanceof Number || result == null) {
			return (Number) result;
		} else {
			throw new Exception("json Path " + Arrays.asList(path).toString() + " doesnot points to a number!");
		}
	}

	public static int getInteger(Object jsonObj, Object... path) throws Exception {
		Number result = getNumber(jsonObj, path);
		return result.intValue();
	}

	public static long getLong(Object jsonObj, Object... path) throws Exception {
		Number result = getNumber(jsonObj, path);
		return result.longValue();
	}

	public static double getDouble(Object jsonObj, Object... path) throws Exception {
		Number result = getNumber(jsonObj, path);
		return result.doubleValue();
	}

}
