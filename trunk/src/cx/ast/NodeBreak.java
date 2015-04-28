package cx.ast;

public class NodeBreak extends Node {
	public Node condition;

	public NodeBreak(SourcePosition position, Node condition) {
		super(position);
		this.condition = condition;
	}

	public void accept(Visitor visitor) {
		visitor.visitBreak(this);
	}

	public String toString() {
		if (condition == null) {
			return "break";
		} else {
			return "break " + condition;
		}
	}
}
