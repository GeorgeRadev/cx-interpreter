package cx.ast;

import java.util.ArrayList;
import java.util.List;
import cx.util.SourcePosition;

public class NodeCaseList extends Node {
	public NodeCaseList(SourcePosition position) {
		super(position);
	}

	public final List<NodeCase> cases = new ArrayList<NodeCase>();

	public void add(NodeCase paramNode) {
		cases.add(paramNode);
	}

	public List<NodeCase> getElements() {
		return cases;
	}

	public void accept(Visitor visitor) {
		visitor.visitCaseList(this);
	}

	public String toString() {
		return cases.toString();
	}
}
