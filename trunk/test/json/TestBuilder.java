package json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

public class TestBuilder extends TestCase {
	public void testBuilderObject() {
		{
			Map<String, Object> map = new HashMap<String, Object>();
			List<String> list = new ArrayList<String>();
			list.add("foo");
			list.add("bar");
			map.put("list", list);
			String json = JSONBuilder.objectToJSON(map);
			assertEquals("{list:[\"foo\",\"bar\"]}", json);
		}
	}

	public void testBuilder() {
		{
			JSONBuilder builder = new JSONBuilder();
			assertEquals("null", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startObject().endObject();
			assertEquals("{}", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startArray().endArray();
			assertEquals("[]", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startObject().addKeyValue("key", "value").endObject();
			assertEquals("{key:\"value\"}", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startObject().addKeyValue("key", "value").addKeyValue("for", "while").addKeyValue("int", 5).endObject();
			assertEquals("{key:\"value\",\"for\":\"while\",int:5}", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startArray().addValue("for").addValue("value").addValue(10).endArray();
			assertEquals("[\"for\",\"value\",10]", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startObject();
			builder.addKeyValue("key", "value").addKeyValue("for", "while").addKey("array");
			builder.startArray().addValue("for").addValue("value").addValue(10).endArray();
			builder.endObject();
			assertEquals("{key:\"value\",\"for\":\"while\",array:[\"for\",\"value\",10]}", builder.toString());
		}
	}
}
