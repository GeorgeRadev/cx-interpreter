package cx.ast;

public class NodeBreak extends Node {
	public NodeBreak(SourcePosition position) {
		super(position);
	}

	public void accept(Visitor visitor) {
		visitor.visitBreak(this);
	}

	public String toString() {
		return "break";
	}
}
