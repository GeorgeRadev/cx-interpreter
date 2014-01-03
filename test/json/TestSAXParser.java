package json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class TestSAXParser extends TestCase {

	SAXToDOMListener listener = new SAXToDOMListener();

	public void testFile() throws Exception {
		File file = new File("./ui.json");
		long length = file.length();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 1026 * 32);
		char[] content = new char[(int) length];
		in.read(content);
		in.close();

		listener.reset();
		JSONSAXParser.parse("", listener);
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			fail();
		}
	}

	public void testBorders() throws IOException, Exception {
		listener.reset();
		JSONSAXParser.parse("", listener);
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			fail();
		}
	}

	public void testCardinalTypes() throws IOException, Exception {

		listener.reset();
		JSONSAXParser.parse("null", listener);
		Object obj = listener.getObject();
		assertNull(obj);

		listener.reset();
		JSONSAXParser.parse("4", listener);
		obj = listener.getObject();
		assertEquals(4L, ((Number) obj).longValue());

		listener.reset();
		JSONSAXParser.parse("3.14", listener);
		obj = listener.getObject();
		assertEquals(3.14f, ((Number) obj).floatValue());

		listener.reset();
		JSONSAXParser.parse("'string'", listener);
		obj = listener.getObject();
		assertEquals("string", obj);

		listener.reset();
		JSONSAXParser.parse("'null'", listener);
		obj = listener.getObject();
		assertEquals("null", obj);

		listener.reset();
		JSONSAXParser.parse("null", listener);
		obj = listener.getObject();
		assertNull(obj);
	}

	@SuppressWarnings("unchecked")
	public void testMap1() throws IOException, Exception {
		String json = "{\"stock\": {\"warehouse\": 300, \"retail\": 20 } }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("stock") instanceof Map);
			assertEquals("300", ((Map<String, Object>) (map.get("stock"))).get("warehouse").toString());
			assertEquals("20", ((Map<String, Object>) (map.get("stock"))).get("retail").toString());
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testMap2() throws IOException, Exception {
		String json = "{'stock': {'warehouse': 300, 'retail': 20 } }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("stock") instanceof Map);
			assertEquals("300", ((Map<String, Object>) (map.get("stock"))).get("warehouse").toString());
			assertEquals("20", ((Map<String, Object>) (map.get("stock"))).get("retail").toString());
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testList1() throws IOException, Exception {
		String json = "{\"tags\": [ \"Bar\", \"Eek\"] }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("tags") instanceof List);
			assertEquals("Bar", ((List<Object>) (map.get("tags"))).get(0).toString());
			assertEquals("Eek", ((List<Object>) (map.get("tags"))).get(1).toString());
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testList2() throws IOException, Exception {
		String json = "{'tags': [ 'Bar', 'Eek'] }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("tags") instanceof List);
			assertEquals("Bar", ((List<Object>) (map.get("tags"))).get(0).toString());
			assertEquals("Eek", ((List<Object>) (map.get("tags"))).get(1).toString());
		} else {
			fail();
		}
	}

	public void testList3() throws IOException, Exception {
		String json = "['tags',  'Bar', 'Eek' ]";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		List<Object> list = listener.getList();
		if (list != null) {
			assertEquals("tags", list.get(0));
			assertEquals("Bar", list.get(1));
			assertEquals("Eek", list.get(2));
		} else {
			fail();
		}
	}

	public void testString() throws IOException, Exception {
		String json = "{\"name\": \"\\u0434\\u0430\\n \\u0433\\u043E \\u0435\\u0431\\u0430\" }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		assertNull(error);
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("name") instanceof String);
			assertEquals("\u0434\u0430\n \u0433\u043E \u0435\u0431\u0430", map.get("name").toString());
		} else {
			fail();
		}
	}

	public void testLong() throws IOException, Exception {
		String json = "{\"price\": 1234}";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("price") instanceof Long);
		} else {
			fail();
		}
	}

	public void testDouble() throws IOException, Exception {
		String json = "{\"id\": 143.04528 }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("id") instanceof Double);
		} else {
			fail();
		}

		json = "{\"id\": 143e-8 }";
		listener.reset();
		error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("id") instanceof Double);
		} else {
			fail();
		}
	}

	public void testBoolean() throws IOException, Exception {
		String json = "{\"true\": true, \"false\": false }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("true") instanceof Boolean);
			assertTrue(map.get("false") instanceof Boolean);
		} else {
			fail();
		}
	}

	public void testNull() throws IOException, Exception {
		String json = "{\"null\": null }";
		listener.reset();
		String error = JSONSAXParser.parse(json, listener);
		if (error != null) {
			fail(error);
		}
		Map<String, Object> map = listener.getMap();
		if (map != null) {
			assertTrue(map.get("null") == null);
		} else {
			fail();
		}
	}
}
