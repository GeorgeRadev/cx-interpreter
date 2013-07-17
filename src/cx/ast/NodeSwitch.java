package cx.ast;

import java.util.List;


public class NodeSwitch extends Node {
	public final Node value;
	public final int defaultIndex;
	public final Object[] caseValues;
	public final Integer[] caseValueIndexes;
	public final Node[] caseStatements;

	public NodeSwitch(SourcePosition position, Node value, int defaultIndex, List<Object> caseValues,
			List<Integer> caseValueIndexes, List<Node> caseStatements) {
		super(position);
		this.value = value;
		this.defaultIndex = defaultIndex;
		this.caseValues = caseValues.toArray(new Object[caseValues.size()]);
		this.caseValueIndexes = caseValueIndexes.toArray(new Integer[caseValueIndexes.size()]);
		this.caseStatements = caseStatements.toArray(new Node[caseStatements.size()]);
	}

	public void accept(Visitor visitor) {
		visitor.visitSwitch(this);
	}

	public String toString() {
		return "switch(" + value + "){" + arrayToString(caseValues) + arrayToString(caseValueIndexes)
				+ arrayToString(caseStatements) + "}";
	}
}