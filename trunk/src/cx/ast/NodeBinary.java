package cx.ast;

import cx.Operator;
import cx.util.SourcePosition;

public class NodeBinary extends Node {
	public final Node left;
	public final Node right;
	public final Operator operator;

	public NodeBinary(SourcePosition position, Node left, Operator op, Node right) {
		super(position);
		this.left = left;
		this.right = right;
		operator = op;
	}

	public void accept(Visitor visitor) {
		visitor.visitBinary(this);
	}

	public String toString() {
		return "" + left + " " + operator + " " + right;
	}
}
