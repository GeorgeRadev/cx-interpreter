package cx.ast;

import cx.util.SourcePosition;

public class NodeAssign extends Node {
	public final Node left;
	public final Node right;

	public NodeAssign(SourcePosition position, Node left, Node right) {
		super(position);
		this.left = left;
		this.right = right;
	}

	public void accept(Visitor visitor) {
		visitor.visitAssign(this);
	}

	public String toString() {
		return left + " = " + right;
	}
}
