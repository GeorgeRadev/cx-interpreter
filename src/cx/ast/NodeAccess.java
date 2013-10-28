package cx.ast;

public class NodeAccess extends Node {
	public final Node object;
	public final Node element;

	public NodeAccess(SourcePosition position, Node left, Node right) {
		super(position);
		object = left;
		element = right;
	}

	public void accept(Visitor visitor) {
		visitor.visitAccess(this);
	}

	public String toString() {
		return object + "[" + element + "]";
	}
}
