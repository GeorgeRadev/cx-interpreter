package cx.exception;


public class JumpReturn extends ScriptException {
	private Object value = null;

	public JumpReturn() {}

	public JumpReturn(Object paramScriptObject) {
		value = paramScriptObject;
	}

	public JumpReturn(String paramString) {
		super(paramString);
	}

	public Object getValue() {
		return value;
	}
}