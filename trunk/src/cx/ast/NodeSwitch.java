package cx.ast;


public class NodeSwitch extends Node {
	public final Node value;
	public final NodeCaseList cases;
	public final int defaultIndex;

	public NodeSwitch(SourcePosition position, Node value, NodeCaseList caseList, int defaultIndex) {
		super(position);
		this.value = value;
		cases = caseList;
		this.defaultIndex = defaultIndex;
	}

	public void accept(Visitor visitor) {
		visitor.visitSwitch(this);
	}

	public String toString() {
		return "switch(" + value + "){" + cases + "}";
	}
}