package json;

public interface JSONSAXListener {

	void startObject();

	void endObject();

	void startArray();

	void endArray();

	/** called for each key of an object */
	void key(String text);

	/** called for each value of object or Array */
	void value(String value);

	void value(double value);

	void value(long value);

	void value(boolean value);

}
