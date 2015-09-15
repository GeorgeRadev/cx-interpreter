package json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
			assertEquals("{\"list\":[\"foo\",\"bar\"]}", json);
		}
	}

	public void testBuilder() {
		assertEquals(JSONBuilder.NULL, JSONBuilder.objectToJSON(null));
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
			assertEquals("{\"key\":\"value\"}", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startObject().addKeyValue("key", "value").addKeyValue("for", "while").addKeyValue("int", 5)
					.endObject();
			assertEquals("{\"key\":\"value\",\"for\":\"while\",\"int\":5}", builder.toString());
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
			assertEquals("{\"key\":\"value\",\"for\":\"while\",\"array\":[\"for\",\"value\",10]}", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startObject().addKeyValue("key", "N/A").endObject();
			assertEquals("{\"key\":\"N\\/A\"}", builder.toString());
		}
		{
			JSONBuilder builder = new JSONBuilder();
			builder.startObject().addKeyValue("key", "value").addKeyValue("for", "while").addKeyValue("int", 5).endObject();
			assertEquals("{\"key\":\"value\",\"for\":\"while\",\"int\":5}", builder.toString());
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
			assertEquals("{\"key\":\"value\",\"for\":\"while\",\"array\":[\"for\",\"value\",10]}", builder.toString());
		}

		{
			JSONBuilder builder = new JSONBuilder();
			Map<String, Object> aMap = new TreeMap<String, Object>();
			aMap.put("string", "string");
			aMap.put("list", Arrays.asList(new Integer[] { 1, 2, 3 }));
			Map<String, Object> submap = new HashMap<String, Object>();
			submap.put("a", true);
			submap.put("b", false);
			aMap.put("map", submap);
			builder.startObject();
			builder.addKeyValue("map", aMap);
			builder.endObject();
			assertEquals("{\"map\":{\"list\":[1,2,3],\"map\":{\"b\":false,\"a\":true},\"string\":\"string\"}}",
					builder.toString());
		}
		{// serialize a single map as content
			JSONBuilder builder = new JSONBuilder();
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("a", true);
			map.put("b", false);
			map.put(null, false);

			builder.addValue(map);
			assertEquals("{\"b\":false,\"a\":true}", builder.toString());
		}
		{// serialize a single list as content
			JSONBuilder builder = new JSONBuilder();
			List<Integer> list = new ArrayList<Integer>();
			list.add(1);
			list.add(2);
			list.add(3);
			builder.addValue(list);
			assertEquals("[1,2,3]", builder.toString());
		}

		{// serialize a single map and null
			JSONBuilder builder = new JSONBuilder();
			Map<Object, Object> map = new TreeMap<Object, Object>();
			map.put("a", true);
			map.put("b", false);
			map.put("c", null);

			builder.addValue(map);
			assertEquals("{\"a\":true,\"b\":false,\"c\":null}", builder.toString());
		}
		{// serialize a single list as content
			JSONBuilder builder = new JSONBuilder();
			List<Integer> list = new ArrayList<Integer>();
			list.add(1);
			list.add(2);
			list.add(3);
			list.add(null);
			builder.addValue(list);
			assertEquals("[1,2,3,null]", builder.toString());
		}
	}
}
