package cx.ast;

public class NodeNumber extends NodeString {
	public final String value;
	public final Number number;

	public NodeNumber(SourcePosition position, String paramString, Number number) {
		super(position, paramString);
		value = paramString;
		this.number = number;
	}

	public void accept(Visitor visitor) {
		visitor.visitNumber(this);
	}

	public String toString() {
		return value;
	}
}