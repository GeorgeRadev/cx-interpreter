package cx.runtime;

public interface ObjectHandler {

	// for attribute access and call implementation

	boolean accept(Object object);

	void set(Object object, String variable, Object value);

	Object get(Object object, String variable);

	Object call(Object object, Object[] args);

	// for static calls implementation

	boolean acceptStaticCall(String method, Object[] args);

	Object staticCall(String method, Object[] args);
}
