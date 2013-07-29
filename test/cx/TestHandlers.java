package cx;

import junit.framework.TestCase;
import cx.runtime.DateHandler;

public class TestHandlers extends TestCase {

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
