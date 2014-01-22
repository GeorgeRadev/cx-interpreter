package cx.exception;

public class JumpReturn extends ScriptException {
	private static final long serialVersionUID = 0xC0DE51DECA5E0011L;

	public final Object value;

	public JumpReturn() {
		value = null;
	}

	public JumpReturn(Object paramScriptObject) {
		value = paramScriptObject;
	}

}