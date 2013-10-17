package cx.ast;

import java.util.List;

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
}