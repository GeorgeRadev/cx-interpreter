package cx.exception;

public class CXException extends RuntimeException {
	private static final long serialVersionUID = 0xC0DE51DECA5E0000L;

	public final Object object;

	public CXException(Object object) {
		this.object = object;
	}
}
