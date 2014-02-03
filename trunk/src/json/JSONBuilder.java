package json;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONBuilder {
	final StringBuilder builder;

	public static final String NULL = "null";
	private static final Map<String, String> reservedJSWords = new HashMap<String, String>(64);
	static {
		reservedJSWords.put("break", NULL);
		reservedJSWords.put("case", NULL);
		reservedJSWords.put("catch", NULL);
		reservedJSWords.put("class", NULL);
		reservedJSWords.put("continue", NULL);
		reservedJSWords.put("debugger", NULL);
		reservedJSWords.put("default", NULL);
		reservedJSWords.put("delete", NULL);
		reservedJSWords.put("do", NULL);
		reservedJSWords.put("else", NULL);
		reservedJSWords.put("enum", NULL);
		reservedJSWords.put("export", NULL);
		reservedJSWords.put("extends", NULL);
		reservedJSWords.put("false", NULL);
		reservedJSWords.put("finally", NULL);
		reservedJSWords.put("for", NULL);
		reservedJSWords.put("function", NULL);
		reservedJSWords.put("if", NULL);
		reservedJSWords.put("implements", NULL);
		reservedJSWords.put("import", NULL);
		reservedJSWords.put("in", NULL);
		reservedJSWords.put("instanceof", NULL);
		reservedJSWords.put("interface", NULL);
		reservedJSWords.put("let", NULL);
		reservedJSWords.put("new", NULL);
		reservedJSWords.put("null", NULL);
		reservedJSWords.put("package", NULL);
		reservedJSWords.put("private", NULL);
		reservedJSWords.put("protected", NULL);
		reservedJSWords.put("public", NULL);
		reservedJSWords.put("return", NULL);
		reservedJSWords.put("static", NULL);
		reservedJSWords.put("super", NULL);
		reservedJSWords.put("switch", NULL);
		reservedJSWords.put("this", NULL);
		reservedJSWords.put("throw", NULL);
		reservedJSWords.put("true", NULL);
		reservedJSWords.put("try", NULL);
		reservedJSWords.put("typeof", NULL);
		reservedJSWords.put("var", NULL);
		reservedJSWords.put("void", NULL);
		reservedJSWords.put("while", NULL);
		reservedJSWords.put("with", NULL);
		reservedJSWords.put("yield", NULL);
	}

	public JSONBuilder() {
		builder = new StringBuilder();
	}

	public JSONBuilder(int size) {
		builder = new StringBuilder(size);
	}

	public final JSONBuilder reset() {
		builder.setLength(0);
		return this;
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
			return NULL;
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
		if (obj == null) {
			return NULL;
		} else {
			JSONBuilder builder = new JSONBuilder();
			objectToJSON(builder, obj);
			return builder.toString();
		}
	}

	@SuppressWarnings("unchecked")
	public static final void objectToJSON(JSONBuilder builder, Object obj) {
		if (obj == null) {
			builder.addValue(NULL);
		} else if (obj instanceof Map) {
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
