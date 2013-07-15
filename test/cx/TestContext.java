package cx;

import java.util.List;
import junit.framework.TestCase;
import cx.ast.NodeBlock;
import cx.runtime.ContextFrame;

public class TestContext extends TestCase {

	public void testSudoku() {
		{
			// Context cx = new Context();
			// cx.set("SUDOKU", new Integer(0xCAFE));
			// NodeBlock block = (new Parser(new File("sudoku.cx"))).parse();
			// cx.evaluate(block);
			// assertTrue(cx.get("SUDOKU") instanceof List);
		}
	}

	public void testFunction() {
		{// Factorial calculating
			Context cx = new Context();
			cx.evaluate((new Parser("function fact(num){ return  (num == 0) ? 1 : num * fact( num - 1 );};")).parse());
			NodeBlock block = (new Parser("f=fact(f);")).parse();

			long f = 1;
			for (int i = 1; i < 14; i++) {
				cx.set("f", new Integer(i));
				cx.evaluate(block);
				f *= i;
				assertEquals(f, ((Number) cx.get("f")).longValue());
			}
		}
		{
			Context cx = new Context();
			cx.set("i", new Integer(0xCAFE));
			NodeBlock block = (new Parser("obj = {n:42, inc:function(){this.n+=42;}}; obj.inc(); i=obj.n;")).parse();
			cx.evaluate(block);
			assertEquals(84, ((Number) cx.get("i")));
		}
		{
			Context cx = new Context();
			cx.set("i", new Integer(0xCAFE));
			cx.evaluate((new Parser("function inc(a){return a+5;}; i=inc(5);")).parse());
			assertEquals(10, ((Number) cx.get("i")));
		}
		{
			Context cx = new Context();
			cx.set("i", new Integer(0xCAFE));
			cx.evaluate((new Parser("i=0; function inc(){i++;}; inc();")).parse());
			assertEquals(1, ((Number) cx.get("i")));
		}
	}

	public void testObject() {
		{
			Context cx = new Context();
			cx.set("obj", new Integer(0xCAFE));
			cx.evaluate((new Parser("obj = {a:1, b: 'string'}; obj.a++; obj.b += ' more';")).parse());
			ContextFrame obj = (ContextFrame) cx.get("obj");
			assertEquals(2, obj.frame.size());
			assertEquals(2, obj.get("a"));
			assertEquals("string more", obj.get("b"));
		}
		{
			Context cx = new Context();
			cx.set("obj", new Integer(0xCAFE));
			cx.evaluate((new Parser("obj = {a:1, b: 'string'};")).parse());
			ContextFrame obj = (ContextFrame) cx.get("obj");
			assertEquals(2, obj.frame.size());
			assertEquals(1, obj.get("a"));
			assertEquals("string", obj.get("b"));
		}
	}

	@SuppressWarnings("rawtypes")
	public void testArray() {
		{
			Context cx = new Context();
			cx.set("arr", new Integer(0xCAFE));
			NodeBlock block = (new Parser("arr = [[1],[2]]; arr[1][0]++; --arr[0][0];")).parse();
			cx.evaluate(block);
			List arr = (List) cx.get("arr");
			assertEquals(2, arr.size());
			List arr1 = (List) arr.get(0);
			List arr2 = (List) arr.get(1);
			assertEquals(0, arr1.get(0));
			assertEquals(3, arr2.get(0));
		}
		{
			Context cx = new Context();
			cx.set("arr", new Integer(0xCAFE));
			NodeBlock block = (new Parser("arr = [1,2,3]; arr[0]++;")).parse();
			cx.evaluate(block);
			List arr = (List) cx.get("arr");
			assertEquals(3, arr.size());
			assertEquals(2, arr.get(0));
			assertEquals(2, arr.get(1));
			assertEquals(3, arr.get(2));
		}
		{
			Context cx = new Context();
			cx.set("arr", new Integer(0xCAFE));
			cx.evaluate((new Parser("arr = [1,2,3,4,5]; arr+=6;")).parse());
			List arr = (List) cx.get("arr");
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
			cx.set("a", new Integer(0xCAFE));
			cx.evaluate(block);
			assertEquals(0, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("b = true || false;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("b", Boolean.FALSE);
			cx.evaluate(block);
			assertTrue((Boolean) cx.get("b"));
		}
		{
			parser = new Parser("b = true && false;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("b", Boolean.TRUE);
			cx.evaluate(block);
			assertFalse((Boolean) cx.get("b"));
		}
		{
			parser = new Parser("a|=0xFF;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", new Integer(0xFF00));
			cx.evaluate(block);
			assertEquals(0xFFFF, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a&=0xFF;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", new Integer(0xFFFF));
			cx.evaluate(block);
			assertEquals(0xFF, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a<<=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", new Integer(2));
			cx.evaluate(block);
			assertEquals(8, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a>>=1;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", new Integer(8));
			cx.evaluate(block);
			assertEquals(4, ((Number) cx.get("a")).intValue());
		}
	}

	public void testArithmetic() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("a/=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", new Integer(2));
			cx.evaluate(block);
			assertEquals(1d, ((Number) cx.get("a")).doubleValue());
		}
		{
			parser = new Parser("a*=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", new Integer(2));
			cx.evaluate(block);
			assertEquals(4, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a+=1;var b=a+4;a=b-5;");
			block = parser.parse();
			assertEquals(3, block.statements.size());
			Context cx = new Context();
			cx.set("a", new Integer(3));
			cx.evaluate(block);
			assertEquals(3, ((Number) cx.get("a")).intValue());
			assertEquals(8, ((Number) cx.get("b")).intValue());
		}
		{
			parser = new Parser("a+=1;a=a+4;");
			block = parser.parse();
			assertEquals(2, block.statements.size());
			Context cx = new Context();
			cx.set("a", new Integer(3));
			cx.evaluate(block);
			assertEquals(8, ((Number) cx.get("a")).intValue());
		}
	}
}
