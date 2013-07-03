package cx.ast;

public class SourcePosition {
	public final int idxStart;
	public final int idxFinish;
	public final int lineNo;
	public final int offset;

	public SourcePosition(int idxStart, int idxFinish, int lineNo, int offset) {
		this.idxStart = idxStart;
		this.idxFinish = idxFinish;
		this.lineNo = lineNo;
		this.offset = offset;
	}

	public String toString() {
		return "line: " + lineNo + " offset: " + offset;
	}
}