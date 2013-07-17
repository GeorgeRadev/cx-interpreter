package cx.ast;

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
}