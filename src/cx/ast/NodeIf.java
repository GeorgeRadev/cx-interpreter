package cx.ast;


public class NodeIf extends Node {
	public Node condition;
	public Node[] body;
	public Node[] elseBody;

	public NodeIf(SourcePosition position, Node condition, Node[] trueNode, Node[] elseNode) {
		super(position);
		this.condition = condition;
		this.body = trueNode;
		this.elseBody = elseNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitIf(this);
	}

	public String toString() {
		if (body == null) {
			return "(" + condition + ") ?? (" + explode(elseBody, ';') + ")";
		} else if (elseBody == null) {
			return "if(" + condition + "){" + explode(body, ';') + ";}";
		} else {
			return "if(" + condition + "){" + explode(body, ';') + ";} else {" + explode(elseBody, ';') + ";}";
		}
	}
}
