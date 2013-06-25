package cx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cx.ast.Node;
import cx.runtime.EvaluateVisitor;

public class Context {
	public final Context parent;
	private final Map<String, Object> cx = new HashMap<String, Object>(32);
	public Object result = null;
	public boolean debugMode = false;

	public Context() {
		parent = null;
	}

	public Context(Context parent) {
		this.parent = parent;
	}

	public Context evaluate(Node node) {
		EvaluateVisitor localEvaluateVisitor = new EvaluateVisitor(this, debugMode);
		node.accept(localEvaluateVisitor);
		return this;
	}

	public Map<String, Object> getVariables() {
		return cx;
	}

	public Object getVariable(String paramString) {
		Context ccx = this;
		Object result;
		do {
			result = ccx.cx.get(paramString);
			if (result != null) {
				return result;
			}
			ccx = ccx.parent;
		} while (result == null && ccx != null);
		return null;
	}

	public Context setVariable(String paramString, Object paramScriptObject) {
		Context ccx = this;
		Object result;
		do {
			result = ccx.cx.get(paramString);
			if (result != null) {
				ccx.cx.put(paramString, paramScriptObject);
				return this;
			}
			ccx = ccx.parent;
		} while (result == null && ccx != null);
		cx.put(paramString, paramScriptObject);
		return this;
	}

	public Context registerObjectHandler() {
		return this;
	}

	public String toString() {
		final List<String> _keys = new ArrayList<String>(cx.size());// of String
		for (String key : cx.keySet()) {
			_keys.add(key);
		}
		Collections.sort(_keys);

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < _keys.size(); i++) {
			Object key = _keys.get(i);
			result.append(key).append(" = ");
			result.append(cx.get(key)).append("\n");
		}
		return result.toString();
	}

	public void dumpContext() {
		System.out.println("Dump Variables:");
		System.out.println(toString());
	}
}