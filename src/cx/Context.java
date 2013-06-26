package cx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.sun.beans.ObjectHandler;
import cx.ast.Node;
import cx.runtime.EvaluateVisitor;

public class Context {
	public Object result = null;
	public final Context parent;
	public final Map<String, Object> cx = new HashMap<String, Object>(32);
	private EvaluateVisitor localEvaluateVisitor;

	public Context() {
		parent = null;
		localEvaluateVisitor = new EvaluateVisitor(this);
	}

	public Context(Context parent) {
		this.parent = parent;
		localEvaluateVisitor = parent.localEvaluateVisitor;
	}

	void addHandler(ObjectHandler handler) {
		// add handler to the parent interpreter: localEvaluateVisitor
		if (handler == null) return;
		Context ccx = this;
		while (ccx.parent != null) {
			ccx = ccx.parent;
		}
		ccx.addHandler(handler);
	}

	public static final void mergeContext(Context from, Context to) {
		List<Context> cxs = new LinkedList<Context>();
		Context ccx = from;
		while (ccx != null) {
			cxs.add(ccx);
			ccx = ccx.parent;
		}
		for (Context c : cxs) {
			to.addContext(c);
		}
	}

	public void addContext(Context context) {
		cx.putAll(context.cx);
	}

	public Context evaluate(Node node) {
		node.accept(localEvaluateVisitor);
		return this;
	}

	public Object get(String paramString) {
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

	public Context set(String paramString, Object paramScriptObject) {
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

		StringBuilder result = new StringBuilder("{\n");
		for (int i = 0; i < _keys.size(); i++) {
			Object key = _keys.get(i);
			result.append(key).append(" = ");
			result.append(cx.get(key)).append("\n");
		}
		result.append("}");
		return result.toString();
	}

	public void dumpContext() {
		System.out.println("Dump Variables:");
		System.out.println(toString());
	}
}