package cx.ast;

public class NodeFor extends Node {
	public Node initialization;
	public Node condition;
	public Node iterator;
	public Node elements;
	public Node body;

	public NodeFor(SourcePosition position, Node initialization, Node condition, Node iterator, Node elements, Node body) {
		super(position);
		this.initialization = initialization;
		this.condition = condition;
		this.iterator = iterator;
		this.elements = elements;
		this.body = body;
	}

	public void accept(Visitor visitor) {
		visitor.visitFor(this);
	}

	public String toString() {
		if (elements != null) {
			return "for(" + iterator + ":" + elements + ")" + body;
		} else {
			return "for(" + initialization + ";" + condition + ";" + iterator + ")" + body;
		}
	}
}
