package cx.ast;

import cx.util.SourcePosition;

public class NodeContinue extends Node {
	public NodeContinue(SourcePosition position) {
		super(position);
	}

	public void accept(Visitor visitor) {
		visitor.visitContinue(this);
	}

	public String toString() {
		return "continue";
	}
}
