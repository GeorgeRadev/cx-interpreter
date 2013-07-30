package cx;

import junit.framework.TestCase;
import cx.runtime.DateHandler;
import cx.runtime.MathHandler;

public class TestHandlers extends TestCase {

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
