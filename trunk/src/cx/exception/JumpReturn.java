package cx.exception;


public class JumpReturn extends ScriptException {
	public final Object value;

	public JumpReturn() {
		value = null;
	}

	public JumpReturn(Object paramScriptObject) {
		value = paramScriptObject;
	}

}