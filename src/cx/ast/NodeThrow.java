package cx.ast;


public class NodeThrow extends Node {
	public final Node expresion;

	public NodeThrow(SourcePosition position, Node paramNode) {
		super(position);
		expresion = paramNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitThrow(this);
	}

	public String toString() {
		return "throw " + expresion;
	}
}
