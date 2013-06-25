package cx.runtime;


class Symbol {
	String name = null;
	Object value = null;
	Symbol next = null;
	private boolean isScopeEnd = false;
	private boolean isFrameEnd = false;

	Symbol(String paramString, Object paramScriptObject) {
		name = paramString;
		value = paramScriptObject;
	}

	void setScopeEnd(boolean paramBoolean) {
		isScopeEnd = paramBoolean;
	}

	boolean getScopeEnd() {
		return isScopeEnd;
	}

	void setFrameEnd(boolean paramBoolean) {
		isFrameEnd = paramBoolean;
	}

	boolean getFrameEnd() {
		return isFrameEnd;
	}
}