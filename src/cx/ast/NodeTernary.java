package cx.ast;

import cx.util.SourcePosition;

public class NodeTernary extends Node {
	public final Node condition;
	public final Node trueValue;
	public final Node falseValue;

	public NodeTernary(SourcePosition position, Node condition, Node trueValue, Node falseValue) {
		super(position);
		this.condition = condition;
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}

	public void accept(Visitor visitor) {
		visitor.visitTernary(this);
	}

	public String toString() {
		return "(" + condition + "?" + trueValue + ":" + falseValue;
	}
}
