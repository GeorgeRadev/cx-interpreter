package cx;

import junit.framework.TestCase;
import cx.runtime.DateHandler;
import cx.runtime.MathHandler;
import cx.runtime.StringHandler;

public class TestHandlers extends TestCase {
	public void testStringHandler() {
		{
			Context cx = new Context();
			cx.addHandler(new StringHandler());
			cx.evaluate((new Parser("str = toString(5);")).parse());
			assertEquals("5", cx.get("str").toString());

			cx.evaluate((new Parser("str = ' trim '; str = str.trim();")).parse());
			assertEquals("trim", cx.get("str").toString());
			cx.evaluate((new Parser("str = ('te' + 'st ').trim();")).parse());
			assertEquals("test", cx.get("str"));

			cx.evaluate((new Parser("str = 'test'.length();")).parse());
			assertEquals("test".length(), cx.get("str"));
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
}
