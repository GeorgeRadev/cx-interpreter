package cx.ast;

import java.util.List;

public class NodeCall extends Node {
	public final Node function;
	public final List<Node> arguments;

	public NodeCall(SourcePosition position, Node object, List<Node> paramListNode) {
		super(position);
		this.function = object;
		arguments = paramListNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitCall(this);
	}

	public String toString() {
		return function + "(" + explode(arguments, ',') + ")";
	}
}
