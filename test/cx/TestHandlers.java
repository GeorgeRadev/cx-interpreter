package cx;

import junit.framework.TestCase;
import cx.runtime.DateHandler;

public class TestHandlers extends TestCase {

	public void testDateHandler() {
		{
			Context cx = new Context();
			cx.addHandler(new DateHandler());
			cx.evaluate((new Parser(
					"var date = newDate('2013-07-08','yyyy-MM-dd'); var datestr = formatDate(date,'dd/MM/yyyy');")).parse());
			assertEquals("08/07/2013", cx.get("datestr"));
		}
	}

}
