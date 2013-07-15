package cx.ast;


public class NodeReturn extends Node {
	public final Node expression;

	public NodeReturn(SourcePosition position, Node expression) {
		super(position);
		this.expression = expression;
	}

	public void accept(Visitor visitor) {
		visitor.visitReturn(this);
	}

	public String toString() {
		return expression == null ? "return" : ("return " + expression);
	}
}