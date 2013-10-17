package cx.ast;

import java.util.ArrayList;
import java.util.List;

public class NodeArray extends Node {
	public NodeArray(SourcePosition position) {
		super(position);
	}

	public final List<Node> elements = new ArrayList<Node>();

	public void add(Node paramNode) {
		elements.add(paramNode);
	}

	public List<Node> getElements() {
		return elements;
	}

	public void accept(Visitor visitor) {
		visitor.visitArray(this);
	}

	public String toString() {
		return "[" + explode(elements, ',') + "]";
	}
}