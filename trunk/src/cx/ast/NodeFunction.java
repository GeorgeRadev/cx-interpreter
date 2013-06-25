package cx.ast;

import cx.util.SourcePosition;

public class NodeFunction extends Node {
	public final String name;
	public final NodeArray arguments;
	public final NodeBlock body;

	public NodeFunction(SourcePosition position, String paramString, NodeArray paramListNode,
			NodeBlock paramBlockNode) {
		super(position);
		name = paramString;
		arguments = paramListNode;
		body = paramBlockNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitFunction(this);
	}

	public String toString() {
		return "fuction " + name + "(" + arguments + ")" + body;
	}
}
