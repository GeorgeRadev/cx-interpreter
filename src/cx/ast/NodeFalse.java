package cx.ast;

import cx.util.SourcePosition;

public class NodeFalse extends Node {
	public NodeFalse(SourcePosition position) {
		super(position);
	}

	public void accept(Visitor visitor) {
		visitor.visitFalse(this);
	}

	public String toString() {
		return "false";
	}
}
