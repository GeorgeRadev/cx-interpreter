package cx.ast;

import cx.Operator;

public class NodeUnary extends Node {
	public final Operator operator;
	public final Node expresion;

	public NodeUnary(SourcePosition position, Operator op, Node paramNode) {
		super(position);
		this.operator = op;
		expresion = paramNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitUnary(this);
	}

	public String toString() {
		return "" + operator + " " + expresion;
	}
}
