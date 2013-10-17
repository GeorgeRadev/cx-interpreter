package cx.ast;

import java.util.List;

public class NodeIf extends Node {
	public Node condition;
	public List<Node> body;
	public List<Node> elseBody;

	public NodeIf(SourcePosition position, Node condition, List<Node> trueNode, List<Node> elseNode) {
		super(position);
		this.condition = condition;
		this.body = trueNode;
		this.elseBody = elseNode;
	}

	public void accept(Visitor visitor) {
		visitor.visitIf(this);
	}

	public String toString() {
		if (elseBody == null) {
			return "if(" + condition + "){" + explode(body, ';') + ";}";
		} else {
			return "if(" + condition + "){" + explode(body, ';') + ";} else {" + explode(elseBody, ';') + ";";
		}
	}
}
