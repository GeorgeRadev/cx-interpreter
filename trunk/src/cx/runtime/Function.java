package cx.runtime;

import cx.Context;
import cx.ast.NodeFunction;

public class Function {
	public final NodeFunction function;
	public final Context thiz;

	public Function(Context thiz, NodeFunction function) {
		this.function = function;
		this.thiz = thiz;
	}

	public String toString() {
		return function.toString();
	}
}
