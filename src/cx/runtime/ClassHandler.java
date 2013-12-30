package cx.runtime;

import cx.ast.Visitor;

public interface ClassHandler {
	// init() is called when a handler was added to a visitor

	void init(Visitor cx);

	// for attribute access and call implementation

	boolean accept(Object object);

	void set(Object object, String variable, Object value);

	Object get(Object object, String variable);

	Object call(Object object, Object[] args);

	// for static calls implementation

	boolean acceptStaticCall(String method, Object[] args);

	Object staticCall(String method, Object[] args);
}
