package cx.exception;

import cx.util.SourcePosition;

public class EvaluatorException extends ParserException {
	public EvaluatorException() {}

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
