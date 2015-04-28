package cx.ast;

public class NodeContinue extends Node {
	public Node condition;

	public NodeContinue(SourcePosition position, Node condition) {
		super(position);
		this.condition = condition;
	}

	public void accept(Visitor visitor) {
		visitor.visitContinue(this);
	}

	public String toString() {
		if (condition == null) {
			return "continue";
		} else {
			return "continue " + condition;
		}
	}
}
