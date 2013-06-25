package cx.ast;

import cx.util.SourcePosition;

public class NodeCase extends Node {
	public final NodeString caseValue;
	public final NodeBlock body;

	public NodeCase(SourcePosition position, NodeString value, NodeBlock block) {
		super(position);
		this.caseValue = value;
		this.body = block;
	}

	public void accept(Visitor visitor) {
		visitor.visitCase(this);
	}

	public String toString() {
		return ((caseValue == null) ? "default" : ("case " + caseValue)) + " : " + body;
	}
}
