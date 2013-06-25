package cx.ast;

import cx.util.SourcePosition;

public class NodeString extends Node {
	public final String value;

	public NodeString(SourcePosition position, String paramString) {
		super(position);
		value = paramString;
	}

	public void accept(Visitor visitor) {
		visitor.visitString(this);
	}

	public String toString() {
		return "'" + value + "'";
	}
}
