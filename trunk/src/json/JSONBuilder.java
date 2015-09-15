package json;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import cx.runtime.ContextFrame;

public class JSONBuilder {
	final StringBuilder builder;

	public static final String NULL = "null";

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
		addKeyValue(name, value);
		return this;
	}

	public final JSONBuilder put(String name, Object value) {
		addKeyValue(name, value);
		return this;
	}

	public final JSONBuilder addKeyValue(String name, String value) {
		addKey(name);
		addValue(value);
		return this;
	}

	public final JSONBuilder addKeyValue(String name, Object value) {
		addKey(name);
		addValue(value);
		return this;
	}

	public final JSONBuilder addKey(String name) {
		escapeAsString(builder, name);
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

		if (value == null) {
			builder.append(NULL);
		} else if (value instanceof Number || value instanceof Boolean) {
			builder.append(value.toString());
		} else if (value instanceof List) {
			List<?> theList = (List<?>) value;
			startArray();
			for (Object element : theList) {
				addValue(element);
			}
			endArray();
			removeComma();

		} else if (value instanceof Map) {
			Map<?, ?> theMap = (Map<?, ?>) value;
			startObject();
			for (Entry<?, ?> element : theMap.entrySet()) {
				Object key = element.getKey();
				if (key == null) {
					continue;
				}
				addKey(String.valueOf(element.getKey()));
				addValue(element.getValue());
			}
			endObject();
			removeComma();

		} else if (value instanceof ContextFrame) {
			ContextFrame frame = (ContextFrame) value;
			startObject();
			for (Entry<?, ?> element : frame.frame.entrySet()) {
				Object key = element.getKey();
				if (key == null) {
					continue;
				}
				addKey(String.valueOf(element.getKey()));
				addValue(element.getValue());
			}
			endObject();
			removeComma();

		} else if (value instanceof Date) {
			addValue(((Date) value).getTime());
		} else if (value instanceof Calendar) {
			addValue(((Calendar) value).getTimeInMillis());
		} else {
			escapeAsString(builder, String.valueOf(value));
		}
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

	static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static void escapeAsString(StringBuilder builder, String str) {
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

	public static final void objectToJSON(JSONBuilder builder, Object obj) {
		builder.addValue(obj);
	}
}
