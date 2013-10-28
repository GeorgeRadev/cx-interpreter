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
		if (operator == Operator.DEC_POST || operator == Operator.INC_POST) {
			return expresion + " " + operator;
		} else {
			return operator + " " + expresion;
		}
	}
}
