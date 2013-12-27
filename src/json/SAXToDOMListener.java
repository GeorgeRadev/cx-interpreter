package json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SAXToDOMListener implements JSONSAXListener {
	Stack<Object> objectStack;
	Map<String, Object> currentMap;
	List<Object> currentList;
	String key;
	Object currentValue;

	public SAXToDOMListener() {
		reset();
	}

	public Object getObject() {
		if (currentMap != null) {
			return currentMap;
		}
		if (currentList != null) {
			return currentList;
		}
		return currentValue;
	}

	public Map<String, Object> getMap() {
		return currentMap;
	}

	public List<Object> getList() {
		return currentList;
	}

	public void reset() {
		objectStack = new Stack<Object>();
		currentMap = null;
		currentList = null;
		key = null;
	}

	public void startObject() {
		Map<String, Object> newMap = new HashMap<String, Object>(32);
		if (objectStack.size() > 0) {
			if (key != null) {
				currentMap.put(key, newMap);
				key = null;
			} else {
				currentList.add(newMap);
			}
		}
		objectStack.push(currentMap = newMap);
	}

	@SuppressWarnings("unchecked")
	public void endObject() {
		currentMap = (Map<String, Object>) objectStack.pop();
		currentList = null;
	}

	public void startArray() {
		List<Object> newArray = new ArrayList<Object>(32);
		if (objectStack.size() > 0) {
			if (key != null) {
				currentMap.put(key, newArray);
				key = null;
			} else {
				currentList.add(newArray);
			}
		}
		objectStack.push(currentList = newArray);
	}

	@SuppressWarnings("unchecked")
	public void endArray() {
		currentList = (List<Object>) objectStack.pop();
		currentMap = null;
	}

	public void key(String text) {
		key = text;
	}

	public void value(String value) {
		if (key != null) {
			currentMap.put(key, value);
			key = null;
		} else if (currentList != null) {
			currentList.add(value);
		} else {
			currentValue = value;
		}
	}

	public void value(double value) {
		if (key != null) {
			currentMap.put(key, value);
			key = null;
		} else if (currentList != null) {
			currentList.add(value);
		} else {
			currentValue = value;
		}
	}

	public void value(long value) {
		if (key != null) {
			currentMap.put(key, value);
			key = null;
		} else if (currentList != null) {
			currentList.add(value);
		} else {
			currentValue = value;
		}
	}

	public void value(boolean value) {
		if (key != null) {
			currentMap.put(key, value);
			key = null;
		} else if (currentList != null) {
			currentList.add(value);
		} else {
			currentValue = value;
		}
	}
}
