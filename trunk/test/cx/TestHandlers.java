package cx;

import java.io.File;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.handlers.DatabaseHandler;
import cx.handlers.DateHandler;
import cx.handlers.MathHandler;
import cx.handlers.ObjectHandler;
import cx.handlers.StringHandler;
import cx.runtime.ContextFrame;

public class TestHandlers extends TestCase {

	public static class TestClass {
		String method1;

		public String method1(String value) {
			method1 = value;
			return value;
		}

		long method2;

		public long method2(long value) {
			method2 = value;
			return value;
		}

		public long method3() {
			return 42L;
		}

		public Object methodList(List<?> list, int i) {
			try {
				return list.get(i);
			} catch (Exception e) {
				return null;
			}
		}

		public Object methodMap(Map<?, ?> map, Object key) {
			try {
				return map.get(key);
			} catch (Exception e) {
				return null;
			}
		}
	}

	public void testClassHandler() {
		Context cx = new Context();
		TestClass instance = new TestClass();
		cx.addHandler(new ObjectHandler(instance, "obj"));

		cx.evaluate((new Parser("str = obj.method1(5);")).parse());
		assertEquals("5", cx.get("str").toString());
		assertEquals("5", instance.method1);

		cx.evaluate((new Parser("str = obj.method1('test');")).parse());
		assertEquals("test", cx.get("str").toString());
		assertEquals("test", instance.method1);

		cx.evaluate((new Parser("l = obj.method2(5);")).parse());
		assertEquals(5L, cx.get("l"));
		assertEquals(5L, instance.method2);

		cx.evaluate((new Parser("l = obj.method3();")).parse());
		assertEquals(42L, cx.get("l"));

		cx.evaluate((new Parser("l = obj.methodList([1,2,3],1);")).parse());
		assertEquals(2L, cx.get("l"));

		cx.evaluate((new Parser("l = obj.methodMap({a:1, b:2,c:3},'b');")).parse());
		assertEquals(2L, cx.get("l"));
	}

	public void testStringHandler() {
		{
			Context cx = new Context();
			cx.addHandler(new StringHandler());
			cx.evaluate((new Parser("str = toString(5);")).parse());
			assertEquals("5", cx.get("str").toString());
			cx.evaluate((new Parser("str = chr(0x41);")).parse());
			assertEquals("A", cx.get("str"));

			cx.evaluate((new Parser("str = ' trim '; str = str.trim();")).parse());
			assertEquals("trim", cx.get("str").toString());
			cx.evaluate((new Parser("str = ('te' + 'st ').trim();")).parse());
			assertEquals("test", cx.get("str"));

			cx.evaluate((new Parser("str = 'test'.length();")).parse());
			assertEquals(4L, cx.get("str"));
			cx.evaluate((new Parser("str = 'smallCammelCase'.toLowerCase();")).parse());
			assertEquals("smallCammelCase".toLowerCase(), cx.get("str"));
			cx.evaluate((new Parser("str = 'smallCammelCase'.toUpperCase();")).parse());
			assertEquals("smallCammelCase".toUpperCase(), cx.get("str"));

			cx.evaluate((new Parser("str = 'ccaabb'.replace('aa','cc');")).parse());
			assertEquals("ccaabb".replace("aa", "cc"), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.replace('a','c');")).parse());
			assertEquals("ccaabb".replace('a', 'c'), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.substring(1,4);")).parse());
			assertEquals("ccaabb".substring(1, 4), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.substring(4);")).parse());
			assertEquals("ccaabb".substring(4), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.indexOf('aa',2);")).parse());
			assertEquals("ccaabb".indexOf("aa", 2), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.indexOf('a',2);")).parse());
			assertEquals("ccaabb".indexOf('a', 2), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.indexOf('aa');")).parse());
			assertEquals("ccaabb".indexOf("aa"), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.indexOf('a');")).parse());
			assertEquals("ccaabb".indexOf('a'), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.endsWith('aa');")).parse());
			assertEquals("ccaabb".endsWith("aa"), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.endsWith('bb');")).parse());
			assertEquals("ccaabb".endsWith("bb"), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.startsWith('aa');")).parse());
			assertEquals("ccaabb".startsWith("aa"), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.startsWith('cc');")).parse());
			assertEquals("ccaabb".startsWith("cc"), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.lastIndexOf('aa');")).parse());
			assertEquals("ccaabb".lastIndexOf("aa"), cx.get("str"));
			cx.evaluate((new Parser("str = 'ccaabb'.lastIndexOf('a');")).parse());
			assertEquals("ccaabb".lastIndexOf('a'), cx.get("str"));
		}
	}

	public void testMathHandler() {
		{
			Context cx = new Context();
			cx.addHandler(new MathHandler());
			assertNotNull(cx.get("Math"));
			cx.evaluate((new Parser("d = 1+Math.random(); d/=d;")).parse());
			assertEquals("1.0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.abs(-0.23);")).parse());
			assertEquals("0.23", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.ceil(0.23);")).parse());
			assertEquals("1.0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.floor(0.23);")).parse());
			assertEquals("0.0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.round(0.23);")).parse());
			assertEquals("0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.round(0.53);")).parse());
			assertEquals("1", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.sqrt(4);")).parse());
			assertEquals("2.0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.max(2,4);")).parse());
			assertEquals("4.0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.max(-1,0,1,2,4);")).parse());
			assertEquals("4.0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.min(2,4,6,8,9);")).parse());
			assertEquals("2.0", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.parseDouble('2.3');")).parse());
			assertEquals("2.3", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.parseDouble(2.3);")).parse());
			assertEquals("2.3", cx.get("d").toString());
			cx.evaluate((new Parser("d = Math.parseInteger('2.3');")).parse());
			assertEquals("2", cx.get("d").toString());
		}
	}

	public void testDateHandler() {
		{
			Context cx = new Context();
			cx.addHandler(new DateHandler());
			cx.evaluate((new Parser(
					"var date = newDate(); date.year=2013; date.month=07;date.day=08 ;var datestr = formatDate(date,'yyyy-MM-dd');")).parse());
			assertEquals("2013-07-08", cx.get("datestr"));
		}
		{
			Context cx = new Context();
			cx.addHandler(new DateHandler());
			cx.evaluate((new Parser(
					"var date = newDate('08/07/2013','dd/MM/yyyy'); var datestr = formatDate(date,'yyyy-MM-dd');")).parse());
			assertEquals("2013-07-08", cx.get("datestr"));
		}
	}

	public void testDatabaseHandler() throws ClassNotFoundException {
		Context cx = new Context();
		cx.addHandler(new DatabaseHandler("Database"));
		Parser parser = new Parser(new File("database_sqlite.cx"));
		parser.supportTryCatchThrow = true;
		parser.supportSQLEscaping = true;
		List<Node> block = parser.parse();
		long time = System.currentTimeMillis();
		try {
			cx.evaluate(block);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("time: " + (System.currentTimeMillis() - time));

		ContextFrame frame = (ContextFrame) cx.get("map");
		assertEquals("just a string", frame.frame.get("42"));
		assertEquals("ok", cx.get("error"));
	}
}
