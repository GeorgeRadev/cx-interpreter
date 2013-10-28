package cx.ast;

import java.util.ArrayList;
import java.util.List;

public class NodeBlock extends Node {
	public NodeBlock(SourcePosition position) {
		super(position);
	}

	public final List<Node> statements = new ArrayList<Node>();

	public void add(Node paramNode) {
		statements.add(paramNode);
	}

	public List<Node> getElements() {
		return statements;
	}

	public void accept(Visitor visitor) {
		visitor.visitBlock(this);
	}

	public String toString() {
		return "{" + explode(statements, ';') + ";}";
	}
}
