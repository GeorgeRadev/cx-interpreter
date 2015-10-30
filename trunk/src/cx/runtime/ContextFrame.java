package cx.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import json.JSONBuilder;

public class ContextFrame implements Map<String, Object> {
	public Object result = null;
	public final ContextFrame parent;
	private final Map<String, Object> frame = new HashMap<String, Object>(32);
	private Set<String> keySet = null;

	public ContextFrame() {
		parent = null;
	}

	public ContextFrame(ContextFrame parent) {
		this.parent = parent;
	}

	/**
	 * shallow get - retrieve element only from the current context frame
	 */
	public Object _get(String varName) {
		return frame.get(varName);
	}

	/**
	 * shallow get - retrieve element only from the current context frame
	 */
	public Object _get(Object varName) {
		return frame.get(varName);
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

	@Override
	public Object get(Object key) {
		if (key == null) {
			return null;
		} else {
			return get(String.valueOf(key));
		}
	}

	/**
	 * shallow put - set a key value only in the current context frame, does not
	 * go deep to fine the source and to replace it if it exists.
	 */
	public ContextFrame _put(String varName, Object value) {
		frame.put(varName, value);
		return this;
	}

	public ContextFrame put(String varName, Object value) {
		ContextFrame ccx = this;
		do {
			if (ccx.frame.containsKey(varName)) {
				ccx.frame.put(varName, value);
				return this;
			}
			ccx = ccx.parent;
		} while (ccx != null);
		frame.put(varName, value);
		// invalidate cash
		keySet = null;
		return this;
	}

	public static final void flattenAintoB(ContextFrame A, ContextFrame B) {
		Stack<ContextFrame> stack = new Stack<ContextFrame>();
		ContextFrame ccx = A;
		while (ccx != null) {
			stack.push(ccx);
			ccx = ccx.parent;
		}
		while (!stack.isEmpty()) {
			ccx = stack.pop();
			B.frame.putAll(ccx.frame);
		}
	}

	public String toString() {
		final List<String> _keys = new ArrayList<String>(frame.size());
		for (String key : frame.keySet()) {
			_keys.add(key);
		}
		Collections.sort(_keys);

		StringBuilder result = new StringBuilder("{\n");
		for (int i = 0; i < _keys.size(); i++) {
			Object key = _keys.get(i);
			result.append(key).append(" : ");
			Object value = frame.get(key);
			if (value == this) {
				JSONBuilder.escapeAsString(result, "this_self_reference");
				result.append(",\n");
			} else if (value instanceof String) {
				// escape strings
				JSONBuilder.escapeAsString(result, (String) value);
				result.append(",\n");
			} else {
				result.append(value).append(",\n");
			}
		}
		if (_keys.size() > 0) {
			result.setLength(result.length() - 2);
		}
		result.append("\n}");
		return result.toString();
	}

	public void printContext() {
		System.out.println("Context frame variables:");
		System.out.println(toString());
	}

	private Set<String> generateKeySetIfNeeded() {
		Set<String> keySet = this.keySet;
		if (keySet == null) {
			keySet = new HashSet<String>();
			Stack<ContextFrame> stack = new Stack<ContextFrame>();
			ContextFrame ccx = this;
			while (ccx != null) {
				stack.push(ccx);
				ccx = ccx.parent;
			}
			while (!stack.isEmpty()) {
				ccx = stack.pop();
				keySet.addAll(ccx.frame.keySet());
			}
			this.keySet = keySet;
		}
		return keySet;
	}

	// so far used for shallow checks
	@Override
	public int size() {
		return frame.size();
	}

	// so far used for shallow checks
	@Override
	public boolean isEmpty() {
		return frame.size() <= 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public Object remove(Object key) {
		return put(String.valueOf(key), null);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		frame.putAll(m);
		keySet = null;
	}

	@Override
	public void clear() {
		frame.clear();
	}

	@Override
	public Set<String> keySet() {
		return generateKeySetIfNeeded();
	}

	// so far used for shallow enumeration
	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return frame.entrySet();
	}

	@Override
	public boolean containsValue(Object value) {
		throw new IllegalStateException("not implemented yet!");
	}

	@Override
	public Collection<Object> values() {
		throw new IllegalStateException("not implemented yet!");
	}
}
