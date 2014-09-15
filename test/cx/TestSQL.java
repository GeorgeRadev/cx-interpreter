package cx;

import java.util.List;
import junit.framework.TestCase;

public class TestSQL extends TestCase {
	public void testToString() {
		{// array
			Context cx = new Context();
			cx.evaluate((new Parser("a=['te\\'st',1,2.3]; a+=42; b=eval(''+a+';');")).parse());
			assertTrue(cx.get("b") instanceof List);

		}
	}
}
