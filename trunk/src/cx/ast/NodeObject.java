package cx.ast;

import java.util.HashMap;
import java.util.Map;

public class NodeObject extends Node {
	public final String parent;
	public final Map<String, Node> object = new HashMap<String, Node>();

	public NodeObject(SourcePosition position, String parent) {
		super(position);
		this.parent = parent;
	}

	public void put(String name, Node value) {
		object.put(name, value);
	}

	public Map<String, Node> getObject() {
		return object;
	}

	public void accept(Visitor visitor) {
		visitor.visitObject(this);
	}

	public String toString() {
		if (parent != null) {
			return "new parent " + object;
		} else {
			return "" + object;
		}
	}
}
