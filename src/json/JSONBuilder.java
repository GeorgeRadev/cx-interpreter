package json;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONBuilder {
	final StringBuilder builder;

	private static String EMPTY = "";
	private static Map<String, String> reservedJSWords = new HashMap<String, String>(64);
	static {
		reservedJSWords.put("break", EMPTY);
		reservedJSWords.put("case", EMPTY);
		reservedJSWords.put("catch", EMPTY);
		reservedJSWords.put("class", EMPTY);
		reservedJSWords.put("continue", EMPTY);
		reservedJSWords.put("debugger", EMPTY);
		reservedJSWords.put("default", EMPTY);
		reservedJSWords.put("delete", EMPTY);
		reservedJSWords.put("do", EMPTY);
		reservedJSWords.put("else", EMPTY);
		reservedJSWords.put("enum", EMPTY);
		reservedJSWords.put("export", EMPTY);
		reservedJSWords.put("extends", EMPTY);
		reservedJSWords.put("false", EMPTY);
		reservedJSWords.put("finally", EMPTY);
		reservedJSWords.put("for", EMPTY);
		reservedJSWords.put("function", EMPTY);
		reservedJSWords.put("if", EMPTY);
		reservedJSWords.put("implements", EMPTY);
		reservedJSWords.put("import", EMPTY);
		reservedJSWords.put("in", EMPTY);
		reservedJSWords.put("instanceof", EMPTY);
		reservedJSWords.put("interface", EMPTY);
		reservedJSWords.put("let", EMPTY);
		reservedJSWords.put("new", EMPTY);
		reservedJSWords.put("null", EMPTY);
		reservedJSWords.put("package", EMPTY);
		reservedJSWords.put("private", EMPTY);
		reservedJSWords.put("protected", EMPTY);
		reservedJSWords.put("public", EMPTY);
		reservedJSWords.put("return", EMPTY);
		reservedJSWords.put("static", EMPTY);
		reservedJSWords.put("super", EMPTY);
		reservedJSWords.put("switch", EMPTY);
		reservedJSWords.put("this", EMPTY);
		reservedJSWords.put("throw", EMPTY);
		reservedJSWords.put("true", EMPTY);
		reservedJSWords.put("try", EMPTY);
		reservedJSWords.put("typeof", EMPTY);
		reservedJSWords.put("var", EMPTY);
		reservedJSWords.put("void", EMPTY);
		reservedJSWords.put("while", EMPTY);
		reservedJSWords.put("with", EMPTY);
		reservedJSWords.put("yield", EMPTY);
	}

	public JSONBuilder() {
		builder = new StringBuilder();
	}

	public JSONBuilder(int size) {
		builder = new StringBuilder(size);
	}

	public final JSONBuilder startObject() {
		builder.append('{');
		return this;
	}

	public final JSONBuilder put(String name, String value) {
		escapeName(builder, name);
		builder.append(':');
		escapeAsString(builder, value);
		builder.append(',');
		return this;
	}

	public final JSONBuilder put(String name, Object value) {
		escapeName(builder, name);
		builder.append(':');
		escapeName(builder, value.toString());
		builder.append(',');
		return this;
	}

	public final JSONBuilder addKeyValue(String name, String value) {
		escapeName(builder, name);
		builder.append(':');
		escapeAsString(builder, value);
		builder.append(',');
		return this;
	}

	public final JSONBuilder addKeyValue(String name, Object value) {
		escapeName(builder, name);
		builder.append(':');
		escapeName(builder, value.toString());
		builder.append(',');
		return this;
	}

	public final JSONBuilder addKey(String name) {
		escapeName(builder, name);
		builder.append(':');
		return this;
	}

	public final JSONBuilder addValue(String str) {
		escapeAsString(builder, str);
		builder.append(',');
		return this;
	}

	public final JSONBuilder addValue(long value) {
		builder.append(value);
		builder.append(',');
		return this;
	}

	public final JSONBuilder addValue(int value) {
		builder.append(value);
		builder.append(',');
		return this;
	}

	public final JSONBuilder addValue(Object value) {
		escapeName(builder, value.toString());
		builder.append(',');
		return this;
	}

	public final JSONBuilder endObject() {
		removeComma();
		builder.append('}');
		builder.append(',');
		return this;
	}

	public final JSONBuilder startArray() {
		builder.append('[');
		return this;
	}

	public final JSONBuilder endArray() {
		removeComma();
		builder.append(']');
		builder.append(',');
		return this;
	}

	private void removeComma() {
		final int len = builder.length() - 1;
		if (builder.charAt(len) == ',') {
			builder.setLength(len);
		}
	}

	public String toString() {
		if (builder.length() <= 0) {
			return EMPTY;
		}
		final int len = builder.length() - 1;
		if (builder.charAt(len) == ',') {
			return builder.substring(0, len);
		} else {
			return builder.toString();
		}
	}

	void escapeName(StringBuilder builder, String name) {
		if (reservedJSWords.get(name) != null) {
			escapeAsString(builder, name);
			return;
		}
		for (int i = 0, l = name.length(); i < l; i++) {
			final char c = name.charAt(i);
			if (c < '0' || (c > 'z') || (c > '9' && c < 'A') || (c != '_' && (c > 'Z' && c < 'a'))) {
				escapeAsString(builder, name);
				return;
			}
		}
		builder.append(name);
	}

	static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	void escapeAsString(StringBuilder builder, String str) {
		builder.append('"');
		for (int i = 0, l = str.length(); i < l; i++) {
			final char c = str.charAt(i);
			switch (c) {
				case '"':
					builder.append("\\\"");
					break;
				case '\\':
					builder.append("\\\\");
					break;
				case '/':
					builder.append("\\/");
					break;
				case '\b':
					builder.append("\\b");
					break;
				case '\f':
					builder.append("\\f");
					break;
				case '\n':
					builder.append("\\n");
					break;
				case '\r':
					builder.append("\\r");
					break;
				case '\t':
					builder.append("\\t");
					break;
				default:
					if ((c < 0x0020) || (c > 0x007e)) {
						builder.append("\\u");
						builder.append(hexDigit[((c >> 12) & 0xF)]);
						builder.append(hexDigit[((c >> 8) & 0xF)]);
						builder.append(hexDigit[((c >> 4) & 0xF)]);
						builder.append(hexDigit[(c & 0xF)]);
					} else {
						builder.append(c);
					}
					break;
			}
		}
		builder.append('"');
	}

	public static final String objectToJSON(Object obj) {
		JSONBuilder builder = new JSONBuilder();
		objectToJSON(builder, obj);
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	private static final void objectToJSON(JSONBuilder builder, Object obj) {
		if (obj instanceof Map) {
			builder.startObject();
			for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) obj).entrySet()) {
				builder.addKey(entry.getKey().toString());
				objectToJSON(builder, entry.getValue());
			}
			builder.endObject();
		} else if (obj instanceof List) {
			builder.startArray();
			for (Object entry : ((List<Object>) obj)) {
				objectToJSON(builder, entry);
			}
			builder.endArray();
		} else if (obj instanceof Date) {
			builder.addValue(((Date) obj).getTime());
		} else if (obj instanceof Calendar) {
			builder.addValue(((Calendar) obj).getTimeInMillis());
		} else {
			builder.addValue(obj.toString());
		}

	}
}
