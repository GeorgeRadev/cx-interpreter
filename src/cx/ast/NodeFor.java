package cx.ast;

import java.util.List;


public class NodeFor extends Node {
	public NodeVar initialization;
	public Node condition;
	public List<Node> iterator;
	public NodeVariable element;
	public Node elements;
	public Node body;

	public NodeFor(SourcePosition position, NodeVar initialization, Node condition, List<Node> iterator,
			NodeVariable element, Node elements, Node body) {
		super(position);
		this.initialization = initialization;
		this.condition = condition;
		this.iterator = iterator;
		this.element = element;
		this.elements = elements;
		this.body = body;
	}

	public void accept(Visitor visitor) {
		visitor.visitFor(this);
	}

	public String toString() {
		if (elements != null) {
			return "for(" + element + ":" + elements + ")" + body;
		} else {
			return "for(" + initialization + ";" + condition + ";" + iterator + ")" + body;
		}
	}
}
