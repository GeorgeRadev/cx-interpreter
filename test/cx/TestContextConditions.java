package cx;

import junit.framework.TestCase;

public class TestContextConditions extends TestCase {
	public void testMixingConditions() {
		{// null = false = 0 = 0.0 = ""
			Context cx = new Context();
			cx.evaluate((new Parser("b = null == false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null == '';")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 == '';")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' == null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 == null;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// >
			Context cx = new Context();
			cx.evaluate((new Parser("b = true > null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false > null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null > false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null > null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 > null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null > 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 > 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 > '';")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' > 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// >=
			Context cx = new Context();
			cx.evaluate((new Parser("b = true >= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 >= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null >= 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 < 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 < 5;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 < 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 < -10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 < 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 <= 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 <= 5;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 <= 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10 <= 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 <= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// !=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 != 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 != 5;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10 != 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 != 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// ==
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 == 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 == -10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 == 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
	}

	public void testBottomElements() {
		{// null = false = 0 = 0.0 = ""
			Context cx = new Context();
			cx.evaluate((new Parser("b = null == null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null == false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null == '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = false == null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false == false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false == '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0 == null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 == false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 == '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0.0 == null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 == false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 == '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = '' == null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' == false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' == 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' == 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' == '';")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// >
			Context cx = new Context();
			cx.evaluate((new Parser("b = null > null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null > false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null > 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null > 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null > '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = false > null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false > false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false > 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false > 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false > '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0 > null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 > false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 > 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 > 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 > '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0.0 > null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 > false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 > 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 > 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 > '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = '' > null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' > false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' > 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' > 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' > '';")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// >=
			Context cx = new Context();
			cx.evaluate((new Parser("b = null >= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null >= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null >= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null >= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null >= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = false >= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false >= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false >= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false >= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false >= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0 >= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 >= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 >= 0 ;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 >= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 >= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0.0 >= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 >= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 >= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 >= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 >= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = '' >= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' >= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' >= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' >= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' >= '';")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// <
			Context cx = new Context();
			cx.evaluate((new Parser("b = null < null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null < false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null < 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null < 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null < '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = false < null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false < false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false < 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false < 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false < '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0 < null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 < false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 < 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 < 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 < '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0.0 < null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 < false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 < 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 < 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 < '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = '' < null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' < false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' < 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' < 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' < '';")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <=
			Context cx = new Context();
			cx.evaluate((new Parser("b = null <= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null <= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null <= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null <= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null <= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = false <= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false <= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false <= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false <= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false <= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0 <= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 <= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 <= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 <= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 <= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0.0 <= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 <= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 <= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 <= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 <= '';")).parse());
			assertTrue((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = '' <= null;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' <= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' <= 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' <= 0.0;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' <= \"\";")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// !=
			Context cx = new Context();
			cx.evaluate((new Parser("b = null != null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null != false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null != 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null != 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = null != '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = false != null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false != false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false != 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false != 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false != '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0 != null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 != false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 != 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 != 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0 != '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = 0.0 != null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 != false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 != 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 != 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 0.0 != '';")).parse());
			assertFalse((Boolean) cx.get("b"));

			cx.evaluate((new Parser("b = '' != null;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' != false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' != 0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' != 0.0;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = '' != '';")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
	}

	public void testStringConditions() {
		{// >
			Context cx = new Context();
			cx.evaluate((new Parser("b = -10.2 > 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 > 5.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 > -10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 > 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// >=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 >= 5.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 >= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 >= 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10.2 >= 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 < 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 < 5.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 < 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 < -10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 <= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 <= 5.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 <= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10.2 <= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// ==
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 == 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 == -10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 == 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}

		{// !=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 != 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 != 5.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10.2 != 0.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
	}

	public void testDoubleConditions() {
		{// >
			Context cx = new Context();
			cx.evaluate((new Parser("b = -10.2 > 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 > 5.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 > -10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 > 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// >=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 >= 5.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 >= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 >= 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10.2 >= 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 < 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 < 5.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 < 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 < -10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 <= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 <= 5.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 <= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10.2 <= 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// ==
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 == 10.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 == -10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5.2 == 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}

		{// !=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10.2 != 10.2;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10.2 != 5.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10.2 != 0.2;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
	}

	public void testIntegerConditions() {
		{// >
			Context cx = new Context();
			cx.evaluate((new Parser("b = -10 > 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 > 5;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 > -10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 > 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// >=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 >= 5;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 >= 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 >= 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10 >= 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 < 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 < 5;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 < 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 < -10;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 <= 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 <= 5;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 <= 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10 <= 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// ==
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 == 10;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 == -10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 5 == 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}

		{// !=
			Context cx = new Context();
			cx.evaluate((new Parser("b = 10 != 10;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = 10 != 5;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = -10 != 0;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
	}

	public void testBoolConditions() {
		{// >
			Context cx = new Context();
			cx.evaluate((new Parser("b = true > true;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = true > false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false > true;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false > false;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// >=
			Context cx = new Context();
			cx.evaluate((new Parser("b = true >= true;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = true >= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false >= true;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false >= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// <
			Context cx = new Context();
			cx.evaluate((new Parser("b = false < true;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = true < true;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = true < false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false < false;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
		{// <=
			Context cx = new Context();
			cx.evaluate((new Parser("b = true <= true;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = true <= false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false <= true;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false <= false;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}
		{// ==
			Context cx = new Context();
			cx.evaluate((new Parser("b = true == true;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = true == false;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false == true;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false == false;")).parse());
			assertTrue((Boolean) cx.get("b"));
		}

		{// !=
			Context cx = new Context();
			cx.evaluate((new Parser("b = true != true;")).parse());
			assertFalse((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = true != false;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false != true;")).parse());
			assertTrue((Boolean) cx.get("b"));
			cx.evaluate((new Parser("b = false != false;")).parse());
			assertFalse((Boolean) cx.get("b"));
		}
	}
}
