package cx.ast;


public class NodeWhile extends Node {
	public Node condition;
	public Node body;
	public boolean isDoWhile;

	public NodeWhile(SourcePosition position, Node condition, Node body, boolean isDoWhile) {
		super(position);
		this.condition = condition;
		this.body = body;
		this.isDoWhile = isDoWhile;
	}

	public void accept(Visitor visitor) {
		visitor.visitWhile(this);
	}

	public String toString() {
		if (isDoWhile) {
			return "do{" + body + "}while(" + condition + ")";
		} else {
			return "while(" + condition + "){" + body + "}";
		}
	}
}
