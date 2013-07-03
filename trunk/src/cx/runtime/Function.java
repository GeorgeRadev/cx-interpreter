package cx.runtime;

import cx.ast.NodeFunction;

public class Function {
	public final NodeFunction function;
	public final ContextFrame thiz;

	public Function(ContextFrame thiz, NodeFunction function) {
		this.function = function;
		this.thiz = thiz;
	}

	public String toString() {
		return function.toString();
	}
}
