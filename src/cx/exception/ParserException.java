package cx.exception;

import cx.ast.SourcePosition;

public class ParserException extends ScriptException {
	private static final long serialVersionUID = 0xC0DE51DECA5E0020L;

	SourcePosition position;

	public ParserException() {
	}

	public ParserException(String paramString) {
		super(paramString);
	}

	public ParserException(String paramString, Throwable e) {
		super(paramString, e);
	}

	public ParserException(String message, SourcePosition position) {
		super(message + " at line " + position.lineNo);
		this.position = position;
	}

	public SourcePosition getSourcePosition() {
		return position;
	}
}
