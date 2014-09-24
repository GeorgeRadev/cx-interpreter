package cx.ast;

public class NodeThrow extends Node {
	public final String name;
	public final Node expression;

	public NodeThrow(SourcePosition position, String name, Node expression) {
		super(position);
		this.name = name;
		this.expression = expression;
	}

	public void accept(Visitor visitor) {
		visitor.visitThrow(this);
	}

	public String toString() {// expression
		if (name == null) {
			return "throw";
		}
		if (expression == null) {
			return "throw " + name;
		}
		return "throw " + name + "(" + expression + ")";
	}
}
