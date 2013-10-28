package cx.ast;

import java.util.List;

public class NodeTry extends Node {
	public List<Node> tryBody;
	public final String[] exceptionTypes;
	public final String[] exceptionNames;
	public final Node[] exceptionBodies;
	public List<Node> finallyBody;

	public NodeTry(SourcePosition position, List<Node> tryBody, String[] exceptionTypes, String[] exceptionNames,
			Node[] exceptionBodies, List<Node> finallyBody) {
		super(position);
		this.tryBody = tryBody;
		this.exceptionTypes = exceptionTypes;
		this.exceptionNames = exceptionNames;
		this.exceptionBodies = exceptionBodies;
		this.finallyBody = finallyBody;
	}

	public void accept(Visitor visitor) {
		visitor.visitTry(this);
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("try {\n").append(explode(tryBody, ';')).append(";\n}");
		for (int i = 0, l = exceptionTypes.length; i < l; i++) {
			buf.append("catch(").append(exceptionTypes[i]).append(' ');
			buf.append(exceptionNames[i]).append(')');
			Node body = exceptionBodies[i];
			if (body == null) {
				buf.append("{}");
			} else if (body instanceof NodeBlock) {
				buf.append(body);
			} else {
				buf.append("{").append(body).append(";}");
			}
		}
		if (finallyBody != null) {
			buf.append("finally{").append(finallyBody).append('}');
		}
		return buf.toString();

	}
}
