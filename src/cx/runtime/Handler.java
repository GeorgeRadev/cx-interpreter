package cx.runtime;

import cx.ast.Visitor;

public interface Handler {
	// init() is called when a handler was added to a visitor
	// use it to register variables into Visitor context
	void init(Visitor cx);

	// return classes for all supported attribute access, static and dynamic
	// call implementations
	Object[] supportedClasses();

	// return names of all supported static functions calls
	String[] supportedStaticCalls();

	// for variable access
	void set(Object object, String variable, Object value);

	Object get(Object object, String variable);

	// for dynamic calls
	Object call(Object object, Object[] args);

	// for static calls implementation
	Object staticCall(String method, Object[] args);
}
