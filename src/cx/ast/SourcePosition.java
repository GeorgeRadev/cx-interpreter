package cx.ast;

public class SourcePosition {
	public final int idx;
	public final int lineNo;
	public final int offset;

	public SourcePosition(int idx, int lineNo, int offset) {
		this.idx = idx;
		this.lineNo = lineNo;
		this.offset = offset;
	}

	public String toString() {
		return "line: " + lineNo + " offset: " + offset;
	}
}