package cx.exception;

import cx.ast.SourcePosition;

public class EvaluatorException extends ParserException {
	private static final long serialVersionUID = 0xC0DE51DECA5E0030L;

	public EvaluatorException() {
	}

	public EvaluatorException(String paramString) {
		super(paramString);
	}

	public EvaluatorException(String message, SourcePosition position, String paramString) {
		super(message + " < " + paramString + " >" + " at line " + position.lineNo);
		this.position = position;
	}

	public EvaluatorException(String message, String paramString) {
		super(message + " < " + paramString + " >");
	}
}
