package cx.runtime;

import cx.ast.NodeFunction;

public class Function {
	public final NodeFunction body;
	public final ContextFrame thiz;

	public Function(ContextFrame thiz, NodeFunction function) {
		this.body = function;
		this.thiz = thiz;
	}

	public String toString() {
		return body.toString();
	}
}
