package cx.ast;


public abstract class Node {
	public SourcePosition position;

	public Node() {
		position = null;
	}

	public Node(SourcePosition position) {
		this.position = position;
	}

	public abstract void accept(Visitor visitor);
}