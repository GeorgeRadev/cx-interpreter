package cx.ast;

import java.util.List;

public class NodeFunction extends Node {
	public final String name;
	public final String[] argumentNames;
	public final List<Node> body;

	public NodeFunction(SourcePosition position, String functionName, NodeArray paramListNode, NodeBlock paramBlockNode) {
		super(position);
		name = functionName;
		List<Node> args = paramListNode.elements;
		int l = args.size();
		argumentNames = new String[l];
		for (int i = 0; i < l; i++) {
			argumentNames[i] = ((NodeVariable) args.get(i)).name;
		}
		body = paramBlockNode.statements;
	}

	public void accept(Visitor visitor) {
		visitor.visitFunction(this);
	}

	public String toString() {
		if (name == null) {
			return "function(" + explode(argumentNames, ',') + "){" + explode(body, ';') + ";}";
		} else {
			return "function " + name + "(" + explode(argumentNames, ',') + "){" + explode(body, ';') + ";}";
		}
	}
}
