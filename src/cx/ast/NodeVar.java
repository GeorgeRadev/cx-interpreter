package cx.ast;

import java.util.ArrayList;
import java.util.List;
import cx.util.SourcePosition;

public class NodeVar extends Node {
	public final List<NodeAssign> vars = new ArrayList<NodeAssign>(4);

	public NodeVar(SourcePosition position) {
		super(position);
	}

	public void addVar(NodeAssign assignment) {
		vars.add(assignment);
	}

	public void accept(Visitor visitor) {
		visitor.visitVar(this);
	}

	public String toString() {
		return "var " + vars.toString();
	}
}
