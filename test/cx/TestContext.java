package cx;

import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import cx.ast.NodeBlock;

public class TestContext extends TestCase {
	@SuppressWarnings("rawtypes")
	public void testObject() {
		{
			Context cx = new Context();
			cx.setVariable("obj", new Integer(0xCAFE));
			cx.evaluate((new Parser("obj = {a:1, b: 'string'};")).parse());
			Map obj = (Map) cx.getVariable("obj");
			assertEquals(2, obj.size());
			assertEquals("1", obj.get("a").toString());
			assertEquals("string", obj.get("b").toString());
		}
	}

	@SuppressWarnings("rawtypes")
	public void testArray() {
		{
			Context cx = new Context();
			cx.setVariable("arr", new Integer(0xCAFE));
			cx.evaluate((new Parser("arr = [1,2,3]; arr[0]++;")).parse());
			List arr = (List) cx.getVariable("arr");
			assertEquals(3, arr.size());
		}
		{
			Context cx = new Context();
			cx.setVariable("arr", new Integer(0xCAFE));
			cx.evaluate((new Parser("arr = [1,2,3,4,5]; arr+=6;")).parse());
			List arr = (List) cx.getVariable("arr");
			assertEquals(6, arr.size());
		}
	}

	public void testLogic() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("a^=a;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("a", new Integer(0xCAFE));
			cx.evaluate(block);
			assertEquals(0, ((Number) cx.getVariable("a")).intValue());
		}
		{
			parser = new Parser("b = true || false;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("b", Boolean.FALSE);
			cx.evaluate(block);
			assertTrue((Boolean) cx.getVariable("b"));
		}
		{
			parser = new Parser("b = true && false;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("b", Boolean.TRUE);
			cx.evaluate(block);
			assertFalse((Boolean) cx.getVariable("b"));
		}
		{
			parser = new Parser("a|=0xFF;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("a", new Integer(0xFF00));
			cx.evaluate(block);
			assertEquals(0xFFFF, ((Number) cx.getVariable("a")).intValue());
		}
		{
			parser = new Parser("a&=0xFF;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("a", new Integer(0xFFFF));
			cx.evaluate(block);
			assertEquals(0xFF, ((Number) cx.getVariable("a")).intValue());
		}
		{
			parser = new Parser("a<<=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("a", new Integer(2));
			cx.evaluate(block);
			assertEquals(8, ((Number) cx.getVariable("a")).intValue());
		}
		{
			parser = new Parser("a>>=1;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("a", new Integer(8));
			cx.evaluate(block);
			assertEquals(4, ((Number) cx.getVariable("a")).intValue());
		}
	}

	public void testArithmetic() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("a/=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("a", new Integer(2));
			cx.evaluate(block);
			assertEquals(1d, ((Number) cx.getVariable("a")).doubleValue());
		}
		{
			parser = new Parser("a*=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.setVariable("a", new Integer(2));
			cx.evaluate(block);
			assertEquals(4, ((Number) cx.getVariable("a")).intValue());
		}
		{
			parser = new Parser("a+=1;var b=a+4;a=b-5;");
			block = parser.parse();
			assertEquals(3, block.statements.size());
			Context cx = new Context();
			cx.setVariable("a", new Integer(3));
			cx.evaluate(block);
			assertEquals(3, ((Number) cx.getVariable("a")).intValue());
			assertNull(cx.getVariable("b"));
		}
		{
			parser = new Parser("a+=1;a=a+4;");
			block = parser.parse();
			assertEquals(2, block.statements.size());
			Context cx = new Context();
			cx.debugMode = true;
			cx.setVariable("a", new Integer(3));
			cx.evaluate(block);
			assertEquals(8, ((Number) cx.getVariable("a")).intValue());
		}
	}
}
