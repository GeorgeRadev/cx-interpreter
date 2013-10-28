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
		StringBuilder result = new StringBuilder(1024);
		result.append("switch (").append(value).append(") {\n");
		int caseIndex = 0;
		for (int i = 0, l = caseStatements.length; i < l; i++) {
			if (caseIndex < caseValueIndexes.length && i == caseValueIndexes[caseIndex]) {
				result.append("case ").append(caseValues[caseIndex]).append(" : ");
				caseIndex++;
			} else if (defaultIndex == i) {
				result.append("default : ");
			}
			result.append(caseStatements[i]).append(";\n");
		}
		result.append("\n}");
		return result.toString();
	}
}