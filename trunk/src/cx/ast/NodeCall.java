package cx.ast;


public class NodeCall extends Node {
	public final Node function;
	public final NodeArray arguments;

	public NodeCall(SourcePosition position, Node object, NodeArray paramListNode) {
		super(position);
		this.function = object;
		arguments = paramListNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitCall(this);
	}

	public String toString() {
		return function + "" + arguments;
	}
}
