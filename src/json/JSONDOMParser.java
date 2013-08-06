package json;

import java.io.Reader;

public class JSONDOMParser {

	final SAXToDOMListener listener;
	public String error;

	public JSONDOMParser() {
		listener = new SAXToDOMListener();
	}

	public Object parse(String json) {
		try {
			listener.reset();
			error = JSONSAXParser.parse(json, listener);
			return error == null ? listener.getObject() : null;
		} catch (Exception e) {
			error = e.getMessage();
			return null;
		}
	}

	public Object parse(Reader json) {
		try {
			listener.reset();
			error = JSONSAXParser.parse(json, listener);
			return error == null ? listener.getObject() : null;
		} catch (Exception e) {
			error = e.getMessage();
			return null;
		}
	}
}
