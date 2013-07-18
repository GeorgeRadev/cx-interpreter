package cx;

import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.runtime.ContextFrame;

public class TestContext extends TestCase {

	public void testSudoku() {
		{
			Context cx = new Context();
			cx.set("SUDOKU", new Integer(0xCAFE));
			List<Node> block = (new Parser(new File("sudoku.cx"))).parse();
			// cx.evaluate(block);
			// assertTrue(cx.get("SUDOKU") instanceof List);
		}
	}

	public void testSwitch() {
		{
			Context cx = new Context();
			cx.evaluate((new Parser(
					"function test(v){var r=0; switch(v){case 1:return 1; case 2:break; case 3: r++; case '4': r++; break; default: r=10;} return r;};")).parse());
			List<Node> block = (new Parser("f=test(f);")).parse();

			cx.set("f", new Integer(1));
			cx.evaluate(block);
			assertEquals(1, ((Number) cx.get("f")).intValue());

			cx.set("f", new Integer(2));
			cx.evaluate(block);
			assertEquals(0, ((Number) cx.get("f")).intValue());

			cx.set("f", new Integer(3));
			cx.evaluate(block);
			assertEquals(2, ((Number) cx.get("f")).intValue());

			cx.set("f", new Integer(4));
			cx.evaluate(block);
			assertEquals(1, ((Number) cx.get("f")).intValue());

			for (int i = 5; i < 20; i++) {
				cx.set("f", new Integer(i));
				cx.evaluate(block);
				assertEquals(10, ((Number) cx.get("f")).intValue());
			}
		}
	}

	public void testBlock() {
		{
			Context cx = new Context();
			cx.set("i", new Integer(0xCAFE));
			cx.evaluate((new Parser("i = 2;{j = 3;}")).parse());
			assertEquals(2, ((Number) cx.get("i")));
			assertNull(cx.get("j"));
		}
		{
			Context cx = new Context();
			cx.set("i", new Integer(0xCAFE));
			cx.evaluate((new Parser("i = 2;{var j = 3;}")).parse());
			assertEquals(2, ((Number) cx.get("i")));
			assertNull(cx.get("j"));
		}
		{
			Context cx = new Context();
			cx.set("i", new Integer(0xCAFE));
			cx.evaluate((new Parser("i = 2;{var i = 3;}")).parse());
			assertEquals(2, ((Number) cx.get("i")));
		}
	}

	public void testTryCatch() {
		{// with return
			Context cx = new Context();
			Parser parser = new Parser("var r=0; function x(){try{return 1;}finally{return 2;}}; r =x();");
			parser.supportTryCatchThrow = true;
			List<Node> block = parser.parse();

			cx.evaluate(block);
			assertEquals("2", cx.get("r").toString());
		}
		{// with finally
			Context cx = new Context();
			Parser parser = new Parser(
					"var f = 0; try{ if(arg==1)throw 1; if(arg==2)throw 'str';}catch(Integer e){arg = 'integer';}catch(String e){arg = 'string';}finally{f = 42;}");
			parser.supportTryCatchThrow = true;
			List<Node> block = parser.parse();

			cx.set("arg", 0);
			cx.evaluate(block);
			assertEquals("0", cx.get("arg").toString());
			assertEquals("42", cx.get("f").toString());

			cx.set("arg", 1);
			cx.evaluate(block);
			assertEquals("integer", cx.get("arg").toString());
			assertEquals("42", cx.get("f").toString());

			cx.set("arg", 2);
			cx.evaluate(block);
			assertEquals("string", cx.get("arg").toString());
			assertEquals("42", cx.get("f").toString());

			cx.set("arg", 3);
			cx.evaluate(block);
			assertEquals("3", cx.get("arg").toString());
			assertEquals("42", cx.get("f").toString());
		}
		{// just catch
			Context cx = new Context();
			Parser parser = new Parser(
					"try{ if(arg==1)throw 1; if(arg==2)throw 'str';}catch(Integer e){arg = 'integer';}catch(String e){arg = 'string';}");
			parser.supportTryCatchThrow = true;
			List<Node> block = parser.parse();

			cx.set("arg", 0);
			cx.evaluate(block);
			assertEquals("0", cx.get("arg").toString());

			cx.set("arg", 1);
			cx.evaluate(block);
			assertEquals("integer", cx.get("arg").toString());

			cx.set("arg", 2);
			cx.evaluate(block);
			assertEquals("string", cx.get("arg").toString());

			cx.set("arg", 3);
			cx.evaluate(block);
			assertEquals("3", cx.get("arg").toString());
		}
	}

	public void testFunction() {
		{// Factorial calculating
			Context cx = new Context();
			cx.evaluate((new Parser("function fact(num){ return  (num == 0) ? 1 : num * fact( num - 1 );};")).parse());
			List<Node> block = (new Parser("f=fact(f);")).parse();

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
			List<Node> block = (new Parser("obj = {n:42, inc:function(){this.n+=42;}}; obj.inc(); i=obj.n;")).parse();
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
			List<Node> block = (new Parser("arr = [[1],[2]]; arr[1][0]++; --arr[0][0];")).parse();
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
			List<Node> block = (new Parser("arr = [1,2,3]; arr[0]++;")).parse();
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
		List<Node> block;
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
		List<Node> block;
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
			assertEquals(3, block.size());
			Context cx = new Context();
			cx.set("a", new Integer(3));
			cx.evaluate(block);
			assertEquals(3, ((Number) cx.get("a")).intValue());
			assertEquals(8, ((Number) cx.get("b")).intValue());
		}
		{
			parser = new Parser("a+=1;a=a+4;");
			block = parser.parse();
			assertEquals(2, block.size());
			Context cx = new Context();
			cx.set("a", new Integer(3));
			cx.evaluate(block);
			assertEquals(8, ((Number) cx.get("a")).intValue());
		}
	}
}
