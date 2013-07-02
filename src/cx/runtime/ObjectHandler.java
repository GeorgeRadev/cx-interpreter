package cx.runtime;

public interface ObjectHandler {

	boolean accept(Object object);

	Object get(Object thiz, String method);

	void set(Object thiz, String method, Object value);

	Object call(Object thiz, Object method, Object[] args);
}
