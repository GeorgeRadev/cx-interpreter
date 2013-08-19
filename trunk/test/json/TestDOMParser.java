package json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

public class TestDOMParser extends TestCase {

	public void testFile() throws Exception {
		File file = new File("./ui.json");
		JSONDOMParser parser = new JSONDOMParser();
		Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 1026 * 32);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) parser.parse(fileReader);
		if (map == null) {
			fail();
		}
	}

	public void testBorders() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "";
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testMap1() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{\"stock\": {\"warehouse\": 300, \"retail\": 20 } }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("stock") instanceof Map);
			assertEquals("300", ((Map<String, Object>) (map.get("stock"))).get("warehouse").toString());
			assertEquals("20", ((Map<String, Object>) (map.get("stock"))).get("retail").toString());
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testMap2() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{'stock': {'warehouse': 300, 'retail': 20 } }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("stock") instanceof Map);
			assertEquals("300", ((Map<String, Object>) (map.get("stock"))).get("warehouse").toString());
			assertEquals("20", ((Map<String, Object>) (map.get("stock"))).get("retail").toString());
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testList1() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{\"tags\": [ \"Bar\", \"Eek\"] }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("tags") instanceof List);
			assertEquals("Bar", ((List<Object>) (map.get("tags"))).get(0).toString());
			assertEquals("Eek", ((List<Object>) (map.get("tags"))).get(1).toString());
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testList2() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{'tags': [ 'Bar', 'Eek'] }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("tags") instanceof List);
			assertEquals("Bar", ((List<Object>) (map.get("tags"))).get(0).toString());
			assertEquals("Eek", ((List<Object>) (map.get("tags"))).get(1).toString());
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testString() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{\"name\": \"\\u0434\\u0430\\n \\u0433\\u043E \\u0435\\u0431\\u0430\" }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("name") instanceof String);
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testLong() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{\"price\": 1234}";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("price") instanceof Long);
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testDouble() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{\"id\": 143.04528 }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("id") instanceof Double);
		} else {
			fail();
		}

		json = "{\"id\": 143e-8 }";
		map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("id") instanceof Double);
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testBoolean() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{\"true\": true, \"false\": false }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("true") instanceof Boolean);
			assertTrue(map.get("false") instanceof Boolean);
		} else {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	public void testNull() {
		JSONDOMParser parser = new JSONDOMParser();
		String json = "{\"null\": null }";
		Map<String, Object> map = (Map<String, Object>) parser.parse(json);
		if (map != null) {
			assertTrue(map.get("null") == null);
		} else {
			fail();
		}
	}
}
