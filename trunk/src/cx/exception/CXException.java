package cx.exception;

public class CXException extends RuntimeException {
	private static final long serialVersionUID = 0xC0DE51DECA5E0000L;

	public final String name;
	public final Object value;

	public CXException(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getMessage() {
		return String.valueOf(value);
	}
}
