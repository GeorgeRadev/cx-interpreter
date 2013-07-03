package cx.ast;


public class NodeNumber extends NodeString {
	public final String value;

	public NodeNumber(SourcePosition position, String paramString) {
		super(position, paramString);
		value = paramString;
	}

	public void accept(Visitor visitor) {
		visitor.visitNumber(this);
	}

	public String toString() {
		return value;
	}
}