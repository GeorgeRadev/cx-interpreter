package json;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

public class TestParser extends TestCase {

	public void testFile() throws Exception {
		File file = new File("." + File.separator + "test" + File.separator + "json" + File.separator + "ui.json");
		long length = file.length();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 1026 * 32);
		char[] content = new char[(int) length];
		in.read(content);
		in.close();

		JSONParser parser = new JSONParser();
		Map<Object, Object> map = parser.parseJSONString(content);
		if (map == null) {
			fail();
		}
	}

	public void testBorders() {
		JSONParser parser = new JSONParser();
		Map<Object, Object> map = null;
		try {
			map = parser.parseJSONString((String) null);
			fail();
		} catch (Exception e) {
		}
		if (map != null) {
			fail();
		}

		try {
			map = parser.parseJSONString((char[]) null);
			fail();
		} catch (Exception e) {
		}
		if (map != null) {
			fail();
		}

		try {
			map = parser.parseJSONString("");
			fail();
		} catch (Exception e) {
		}
		if (map != null) {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testMap1() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{\"stock\": {\"warehouse\": 300, \"retail\": 20 } }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("stock") instanceof Map);
			assertEquals("300", ((Map<String, Object>) (((Map<?, ?>) jsonObject).get("stock"))).get("warehouse")
					.toString());
			assertEquals("20", ((Map<String, Object>) (((Map<?, ?>) jsonObject).get("stock"))).get("retail").toString());

			assertTrue(JSONParser.getMap(jsonObject, "stock") instanceof Map);
			assertEquals(JSONParser.getString(jsonObject, "stock", "warehouse"), "300");
			assertEquals(JSONParser.getString(jsonObject, "stock", "retail"), "20");
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testMap2() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{'stock': {'warehouse': 300, 'retail': 20 } }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("stock") instanceof Map);
			assertEquals("300", ((Map<String, Object>) (((Map<?, ?>) jsonObject).get("stock"))).get("warehouse")
					.toString());
			assertEquals("20", ((Map<String, Object>) (((Map<?, ?>) jsonObject).get("stock"))).get("retail").toString());

			assertTrue(JSONParser.getMap(jsonObject, "stock") instanceof Map);
			assertEquals(JSONParser.getString(jsonObject, "stock", "warehouse"), "300");
			assertEquals(JSONParser.getString(jsonObject, "stock", "retail"), "20");
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testMap3() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{ stock : { warehouse : 300,  retail   : 20 } }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("stock") instanceof Map);
			assertEquals("300", ((Map<String, Object>) (((Map<?, ?>) jsonObject).get("stock"))).get("warehouse")
					.toString());
			assertEquals("20", ((Map<String, Object>) (((Map<?, ?>) jsonObject).get("stock"))).get("retail").toString());

			assertTrue(JSONParser.getMap(jsonObject, "stock") instanceof Map);
			assertEquals(JSONParser.getString(jsonObject, "stock", "warehouse"), "300");
			assertEquals(JSONParser.getString(jsonObject, "stock", "retail"), "20");
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testList1() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{\"tags\": [ \"Bar\", \"Eek\"] }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("tags") instanceof List);
			assertEquals("Bar", ((List<Object>) (((Map<?, ?>) jsonObject).get("tags"))).get(0).toString());
			assertEquals("Eek", ((List<Object>) (((Map<?, ?>) jsonObject).get("tags"))).get(1).toString());

			assertTrue(JSONParser.getList(jsonObject, "tags") instanceof List);
			assertEquals(JSONParser.getString(jsonObject, "tags", 0), "Bar");
			assertEquals(JSONParser.getString(jsonObject, "tags", 1), "Eek");
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testList2() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{'tags': [ 'Bar', 'Eek'] }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("tags") instanceof List);
			assertEquals("Bar", ((List<Object>) (((Map<?, ?>) jsonObject).get("tags"))).get(0).toString());
			assertEquals("Eek", ((List<Object>) (((Map<?, ?>) jsonObject).get("tags"))).get(1).toString());

			assertTrue(JSONParser.getList(jsonObject, "tags") instanceof List);
			assertEquals(JSONParser.getString(jsonObject, "tags", 0), "Bar");
			assertEquals(JSONParser.getString(jsonObject, "tags", 1), "Eek");
		} else {
			fail();
		}
	}

	public void testString() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{\"name\": \"\\u0434\\u0430\\n \\u0433\\u043E \\u0435\\u0431\\u0430\" }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("name") instanceof String);
		} else {
			fail();
		}

		json = "{\"name\": \"\\/\" }";
		jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertEquals("/", ((Map<?, ?>) jsonObject).get("name"));
		} else {
			fail();
		}
	}

	public void testLong() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{\"price\": 1234}";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("price") instanceof Long);

			assertEquals(JSONParser.getInteger(jsonObject, "price"), 1234);
			assertEquals(JSONParser.getLong(jsonObject, "price"), 1234L);
		} else {
			fail();
		}
	}

	public void testDouble() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{\"id\": 143.04528 }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("id") instanceof Double);

			assertEquals(JSONParser.getDouble(jsonObject, "id"), 143.04528d);
		} else {
			fail();
		}

		json = "{\"id\": 143e-8 }";
		jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("id") instanceof Double);
			JSONParser.getDouble(jsonObject, "id");
		} else {
			fail();
		}
	}

	public void testBoolean() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{\"true\": true, \"false\": false }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("true") instanceof Boolean);
			assertTrue(((Map<?, ?>) jsonObject).get("false") instanceof Boolean);

			assertTrue(JSONParser.getBoolean(jsonObject, "true"));
			assertFalse(JSONParser.getBoolean(jsonObject, "false"));
		} else {
			fail();
		}
	}

	public void testNull() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{\"null\": null }";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertTrue(((Map<?, ?>) jsonObject).get("null") == null);
			try {

				JSONParser.getObject(jsonObject, "null");
				fail();
			} catch (Exception e) {
				// ok null is actualy null
			}
		} else {
			fail();
		}
	}

	public void testPath1() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{a:{b:{c:{d:'abcd'}}}}";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertEquals(JSONParser.getString(jsonObject, "a", "b", "c", "d"), "abcd");
		} else {
			fail();
		}
	}

	public void testPath2() throws Exception {
		JSONParser parser = new JSONParser();
		String json = "{a:{b:[{c:{d:'abcd'}},{e:{f:'abef'}}]}}";
		Object jsonObject = parser.parseJSONString(json);
		if (jsonObject != null) {
			assertEquals(JSONParser.getString(jsonObject, "a", "b", 0, "c", "d"), "abcd");
			assertEquals(JSONParser.getString(jsonObject, "a", "b", 1, "e", "f"), "abef");
		} else {
			fail();
		}
	}
}
