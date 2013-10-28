package cx.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Node {
	public SourcePosition position;

	public Node() {
		position = null;
	}

	public Node(SourcePosition position) {
		this.position = position;
	}

	public abstract void accept(Visitor visitor);

	public static final <T> String arrayToString(T[] array) {
		if (array == null) {
			return "";
		}
		if (array.length <= 0) {
			return "[]";
		}
		StringBuilder result = new StringBuilder(256);
		result.append('[');
		for (int i = 0, l = array.length; i < l; i++) {
			result.append(array[i].toString());
			result.append(',');
		}
		result.setCharAt(result.length() - 1, ']');
		return result.toString();
	}

	public static final <T> String explode(T[] array, char separator) {
		if (array == null || array.length <= 0) {
			return "";
		}
		StringBuilder result = new StringBuilder(256);
		for (int i = 0, l = array.length; i < l; i++) {
			result.append(array[i].toString());
			result.append(separator);
		}
		result.setLength(result.length() - 1);
		return result.toString();
	}

	public static final <T> String explode(List<T> list, char separator) {
		if (list == null || list.size() <= 0) {
			return "";
		}
		StringBuilder result = new StringBuilder(256);
		for (T e : list) {
			if (e != null) {
				result.append(e.toString());
			}
			result.append(separator);
		}
		result.setLength(result.length() - 1);
		return result.toString();
	}

	/* this one should be the same as in the Scanner class */
	private static Map<Character, Character> escapes = new HashMap<Character, Character>(16);
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

	public static final <T> String escapeString(String string) {
		int len;
		if (string == null || (len = string.length()) <= 0) {
			return "";
		}

		StringBuilder result = new StringBuilder(len + 256);
		for (int i = 0; i < len; i++) {
			final char c = string.charAt(i);
			if (escapes.containsKey(c)) {
				result.append('\\');
				result.append(c);

			} else if (c > 0x7f) {
				result.append('\\');
				result.append('u');
				String hex = Integer.toHexString((int) c);
				for (int j = hex.length(); j < 4; j++) {
					result.append('0');
				}
				result.append(hex);

			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
}