package cx.ast;


public class NodeIf extends Node {
	public Node condition;
	public Node body;
	public Node elseBody;

	public NodeIf(SourcePosition position, Node condition, Node trueNode, Node elseNode) {
		super(position);
		this.condition = condition;
		this.body = trueNode;
		this.elseBody = elseNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitIf(this);
	}

	public String toString() {
		return "if(" + condition + ")" + body + ((elseBody == null) ? "" : (" else " + elseBody));
	}
}
