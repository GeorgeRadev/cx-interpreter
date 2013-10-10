package cx.ast;

import java.util.ArrayList;
import java.util.List;

public class NodeVar extends Node {
	public final boolean defineLocaly;
	public final List<NodeAssign> vars = new ArrayList<NodeAssign>(4);

	public NodeVar(SourcePosition position, boolean defineLocaly) {
		super(position);
		this.defineLocaly = defineLocaly;
	}

	public void addVar(NodeAssign assignment) {
		vars.add(assignment);
	}

	public void accept(Visitor visitor) {
		visitor.visitVar(this);
	}

	public String toString() {
		return (defineLocaly ? "var " : "") + vars.toString();
	}
}
