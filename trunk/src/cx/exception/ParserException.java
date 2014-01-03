package cx.exception;

import cx.ast.SourcePosition;

public class ParserException extends ScriptException {
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
