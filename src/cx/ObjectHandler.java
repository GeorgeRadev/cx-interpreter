package cx;

interface ObjectHandler {
	boolean accept(Object object);

	Object access(Object thiz, String method);

	Object call(Object thiz, String method, Object[] args);
}
