package cx.ast;

import cx.util.SourcePosition;

public class NodeTrue extends Node {
	public NodeTrue(SourcePosition position) {
		super(position);
	}

	public void accept(Visitor visitor) {
		visitor.visitTrue(this);
	}

	public String toString() {
		return "true";
	}
}
