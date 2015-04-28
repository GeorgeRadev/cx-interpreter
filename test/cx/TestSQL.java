package cx;

import java.util.Calendar;
import java.util.TimeZone;
import junit.framework.TestCase;

public class TestSQL extends TestCase {

	public void testSQLStringEscape() {
		Parser parser = new Parser();
		parser.supportSQLEscaping = true;
		{
			Context cx = new Context();
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.set(Calendar.ZONE_OFFSET, 0);
			cal.setTimeInMillis(0);
			cx.set("datestr", cal);
			parser.supportSQLEscaping = true;
			cx.evaluate(parser.parse("sql := select 'id' where 'updated' = datestr;"));
			String sql = cx.get("sql").toString();
			assertEquals("select id where updated = '1970-01-01 02:00:00' ", sql);
		}
		{
			Context cx = new Context();
			cx.evaluate(parser.parse("a = 4; sql := update 'schema'.'table' set 'id' = 'id' + a;"));
			String sql = cx.get("sql").toString();
			assertEquals("update schema . table set id = id + 4 ", sql);
		}
		{
			Context cx = new Context();
			cx.evaluate(parser.parse("date = 'test'; sql := update 'table' set date = 0;"));
			String sql = cx.get("sql").toString();
			// date is reserved word
			assertEquals("update table set date = 0 ", sql);
		}
		{
			Context cx = new Context();
			cx.evaluate(parser.parse("s = 'value'; sql := select 'id' where 'name' = s and 'city' = \"'NY'\";"));
			String sql = cx.get("sql").toString();
			assertEquals("select id where name = 'value' and city = 'NY' ", sql);
		}
	}
}
