package cx.ast;

import cx.util.SourcePosition;

public class NodeVariable extends Node {
	public final String name;

	public NodeVariable(SourcePosition position, String name) {
		super(position);
		this.name = name;
	}

	public void accept(Visitor visitor) {
		visitor.visitVariable(this);
	}

	public String toString() {
		return name;
	}
}
