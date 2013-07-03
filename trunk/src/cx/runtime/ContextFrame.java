package cx.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ContextFrame {
	public Object result = null;
	public final ContextFrame parent;
	public final Map<String, Object> frame = new HashMap<String, Object>(32);

	public ContextFrame() {
		parent = null;
	}

	public ContextFrame(ContextFrame parent) {
		this.parent = parent;
	}

	public Object get(String varName) {
		ContextFrame ccx = this;
		Object result;
		do {
			result = ccx.frame.get(varName);
			if (result != null) {
				return result;
			}
			ccx = ccx.parent;
		} while (result == null && ccx != null);
		return null;
	}

	public ContextFrame set(String varName, Object value) {
		ContextFrame ccx = this;
		Object result;
		do {
			result = ccx.frame.get(varName);
			if (result != null) {
				ccx.frame.put(varName, value);
				return this;
			}
			ccx = ccx.parent;
		} while (result == null && ccx != null);
		frame.put(varName, value);
		return this;
	}

	public String toString() {
		final List<String> _keys = new ArrayList<String>(frame.size());// of
																		// String
		for (String key : frame.keySet()) {
			_keys.add(key);
		}
		Collections.sort(_keys);

		StringBuilder result = new StringBuilder("{\n");
		for (int i = 0; i < _keys.size(); i++) {
			Object key = _keys.get(i);
			result.append(key).append(" = ");
			result.append(frame.get(key)).append("\n");
		}
		result.append("}");
		return result.toString();
	}

	public void dumpContext() {
		System.out.println("Dump Variables:");
		System.out.println(toString());
	}

	public static final void mergeContext(ContextFrame from, ContextFrame to) {
		List<ContextFrame> frameStack = new LinkedList<ContextFrame>();

		ContextFrame ccx = from;
		while (ccx != null) {
			frameStack.add(ccx);
			ccx = ccx.parent;
		}
		for (ContextFrame c : frameStack) {
			to.frame.putAll(c.frame);
		}
	}
}
