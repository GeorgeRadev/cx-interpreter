package cx.exception;

public abstract class ScriptException extends RuntimeException {
	public ScriptException() {
	}

	public ScriptException(String paramString) {
		super(paramString);
	}

	public ScriptException(String paramString, Throwable e) {
		super(paramString, e);
	}

	public Throwable fillInStackTrace() {
		return this;
	}

	protected Throwable originalFillInStackTrace() {
		return super.fillInStackTrace();
	}
}