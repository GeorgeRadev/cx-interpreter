package cx.exception;

public class CXException extends RuntimeException {
	public final Object object;

	public CXException(Object object) {
		this.object = object;
	}
}
