package cx;

import java.util.List;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.ast.Visitor;
import cx.runtime.ContextFrame;
import cx.runtime.ObjectHandler;

public class TestContext extends TestCase {

	@SuppressWarnings("rawtypes")
	public void testToString() {
		{// array
			Context cx = new Context();
			PrintHandler printHandler = new PrintHandler();
			cx.addHandler(printHandler);
			cx.evaluate((new Parser("a=['te\\'st',1,2.3]; a+=42; b=eval(''+a+';');")).parse());
			assertTrue(cx.get("b") instanceof List);
			List l = (List) cx.get("b");
			assertEquals("te'st", l.get(0).toString());
			assertEquals("1", l.get(1).toString());
			assertEquals("2.3", l.get(2).toString());
			assertEquals("42", l.get(3).toString());
		}
	}

	private static class PrintHandler implements ObjectHandler {
		public String value = "";

		public boolean accept(Object object) {
			return false;
		}

		public void set(Object thiz, String method, Object value) {}

		public Object get(Object thiz, String method) {
			return null;
		}

		public Object call(Object object, Object[] args) {
			return null;
		}

		public boolean acceptStaticCall(String method, Object[] args) {
			switch (args.length) {
				case 0:
					return "breakpoint".equals(method);
				case 1:
					return "print".equals(method) || "log".equals(method);
			}
			return false;
		}

		static int lines = 0;

		public Object staticCall(String method, Object[] args) {
			switch (args.length) {
				case 0:
					if ("breakpoint".equals(method)) {
						// break point
						return false;
					}
					break;
				case 1:
					if ("print".equals(method) || "log".equals(method)) {
						String str = args[0].toString();
						System.out.println(str);
						value = value + str;
						lines++;
						if (lines > 50000) System.exit(0);
						return value;
					}
					break;
			}
			return null;
		}

		public void init(Visitor cx) {}
	}

	public void testHandler() {
		{
			Context cx = new Context();
			PrintHandler printHandler = new PrintHandler();
			cx.addHandler(printHandler);
			cx.evaluate((new Parser("print('hi '+'world');")).parse());
			assertEquals(printHandler.value, "hi world");
		}
	}

	public void testEval() {
		{
			Context cx = new Context();
			PrintHandler printHandler = new PrintHandler();
			cx.addHandler(printHandler);
			cx.evaluate((new Parser("a = 5; eval('a++;');")).parse());
			assertEquals(6L, cx.get("a"));
		}
		{
			Context cx = new Context();
			PrintHandler printHandler = new PrintHandler();
			cx.addHandler(printHandler);
			cx.evaluate((new Parser("a = 5; inc = eval('function (x){return ++x;};'); a = inc(a);")).parse());
			assertEquals(6L, cx.get("a"));
		}
	}

	public void testSwitch() {
		{
			Context cx = new Context();
			cx.evaluate((new Parser(
					"function test(v){var r=0; switch(v){case 1:return 1; case 2:break; case 3: r++; case '4': r++; break; default: r=10;} return r;};")).parse());
			List<Node> block = (new Parser("f=test(f);")).parse();

			cx.set("f", Integer.valueOf(1));
			cx.evaluate(block);
			assertEquals(1L, cx.get("f"));

			cx.set("f", Integer.valueOf(2));
			cx.evaluate(block);
			assertEquals(0L, cx.get("f"));

			cx.set("f", Integer.valueOf(3));
			cx.evaluate(block);
			assertEquals(2L, cx.get("f"));

			cx.set("f", Integer.valueOf(4));
			cx.evaluate(block);
			assertEquals(1L, cx.get("f"));

			for (int i = 5; i < 20; i++) {
				cx.set("f", Integer.valueOf(i));
				cx.evaluate(block);
				assertEquals(10L, cx.get("f"));
			}
		}
	}

	public void testBlock() {
		{
			Context cx = new Context();
			cx.set("i", Integer.valueOf(0xCAFE));
			cx.evaluate((new Parser("i = 2;{j = 3;}")).parse());
			assertEquals(2L, cx.get("i"));
			assertNull(cx.get("j"));
		}
		{
			Context cx = new Context();
			cx.set("i", Integer.valueOf(0xCAFE));
			cx.evaluate((new Parser("i = 2;{var j = 3;}")).parse());
			assertEquals(2L, cx.get("i"));
			assertNull(cx.get("j"));
		}
		{
			Context cx = new Context();
			cx.set("i", Integer.valueOf(0xCAFE));
			cx.evaluate((new Parser("i = 2;{var i = 3;}")).parse());
			assertEquals(2L, cx.get("i"));
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
					"var f = 0; try{ if(arg==1)throw 1; if(arg==2)throw 'str';}catch(Long e){arg = 'integer';}catch(String e){arg = 'string';}finally{f = 42;}");
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
					"try{ if(arg==1)throw 1; if(arg==2)throw 'str';}catch(Long e){arg = 'integer';}catch(String e){arg = 'string';}");
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

	@SuppressWarnings("rawtypes")
	public void testFunction() {
		{// arguments
			Context cx = new Context();
			cx.evaluate((new Parser("function f(num){ return  arguments;};")).parse());
			cx.evaluate((new Parser("result=f();")).parse());
			assertEquals(0, ((List) cx.get("result")).size());

			cx.evaluate((new Parser("result=f(1,2,3,4,5);")).parse());
			List args = (List) cx.get("result");
			assertEquals(5, args.size());
			assertEquals(1L, args.get(0));
			assertEquals(5L, args.get(4));
		}
		{// Factorial calculating
			Context cx = new Context();
			cx.evaluate((new Parser("function fact(num){ return  (num == 0) ? 1 : num * fact( num - 1 );};")).parse());
			List<Node> block = (new Parser("f=fact(f);")).parse();

			long f = 1;
			for (int i = 1; i < 14; i++) {
				cx.set("f", Integer.valueOf(i));
				cx.evaluate(block);
				f *= i;
				assertEquals(f, ((Number) cx.get("f")).longValue());
			}
		}
		{
			Context cx = new Context();
			List<Node> block = (new Parser("obj = {n:42, inc:function(){n+=42;}}; obj.inc(); i=obj.n;")).parse();
			cx.evaluate(block);
			assertEquals(84L, cx.get("i"));
		}
		{
			Context cx = new Context();
			List<Node> block = (new Parser("obj = {n:42, inc:function(){n+=42;}}; obj.inc(); i=obj['n'];")).parse();
			cx.evaluate(block);
			assertEquals(84L, cx.get("i"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("function inc(a){return a+5;}; i=inc(5);")).parse());
			assertEquals(10L, cx.get("i"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("i=0; function inc(){i++;}; inc();")).parse());
			assertEquals(1L, cx.get("i"));
		}
		{// call nonexistent function
			Context cx = new Context();
			cx.evaluate((new Parser("a = nonexistent('test');")).parse());
			assertNull(cx.get("a"));

			cx.evaluate((new Parser("a = none.nonexistent('test');")).parse());
			assertNull(cx.get("a"));
		}
	}

	public void testFor() {
		{
			Context cx = new Context();
			cx.evaluate((new Parser("array = [0,1,2,3]; for(i=0;i<array.length;i++){array[i]++;}")).parse());
			@SuppressWarnings("rawtypes")
			List arr = (List) cx.get("array");
			assertEquals(4, arr.size());
			assertEquals(1L, arr.get(0));
			assertEquals(2L, arr.get(1));
			assertEquals(3L, arr.get(2));
			assertEquals(4L, arr.get(3));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("s=0; array = [0,1,2,3]; for(e:array){s+=e;}")).parse());
			assertEquals(6L, cx.get("s"));
		}
		{
			Context cx = new Context();
			cx.set("i", Integer.valueOf(0xCAFE));
			cx.evaluate((new Parser("s=0; obj = {a:0,b:1,c:2,d:3}; for(e:obj){s+=obj[e];}")).parse());
			assertEquals(6L, cx.get("s"));
			cx.evaluate((new Parser("s+=obj['d'];")).parse());
			assertEquals(9L, cx.get("s"));
		}
	}

	public void testObject() {
		{
			Context cx = new Context();
			cx.evaluate((new Parser("obj = {a:1, b: 'string'}; obj.a++; obj.b += ' more';")).parse());
			ContextFrame obj = (ContextFrame) cx.get("obj");
			assertEquals(2, obj.frame.size());
			assertEquals(2L, obj.get("a"));
			assertEquals("string more", obj.get("b"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("obj = {a:1, b: 'string'};")).parse());
			ContextFrame obj = (ContextFrame) cx.get("obj");
			assertEquals(2, obj.frame.size());
			assertEquals(1L, obj.get("a"));
			assertEquals("string", obj.get("b"));
		}
	}

	@SuppressWarnings("rawtypes")
	public void testArray() {
		{
			Context cx = new Context();
			List<Node> block = (new Parser("arr = [[1],[2]]; arr[1][0]++; --arr[0][0];")).parse();
			cx.evaluate(block);
			List arr = (List) cx.get("arr");
			assertEquals(2, arr.size());
			List arr1 = (List) arr.get(0);
			List arr2 = (List) arr.get(1);
			assertEquals(0L, arr1.get(0));
			assertEquals(3L, arr2.get(0));
		}
		{
			Context cx = new Context();
			List<Node> block = (new Parser("arr = [1,2,3]; arr[0]++;")).parse();
			cx.evaluate(block);
			List arr = (List) cx.get("arr");
			assertEquals(3, arr.size());
			assertEquals(2L, arr.get(0));
			assertEquals(2L, arr.get(1));
			assertEquals(3L, arr.get(2));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("arr = [1,2,3,4,5]; arr+=6;")).parse());
			List arr = (List) cx.get("arr");
			assertEquals(6, arr.size());
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("arr = 'abcdef'; arr=arr[3];")).parse());
			assertEquals((int) 'd', ((Number) cx.get("arr")).intValue());
		}
	}

	public void testLogic() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("a^=a;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", Integer.valueOf(0xCAFE));
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
			cx.set("a", Integer.valueOf(0xFF00));
			cx.evaluate(block);
			assertEquals(0xFFFF, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a&=0xFF;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", Integer.valueOf(0xFFFF));
			cx.evaluate(block);
			assertEquals(0xFF, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a<<=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", Integer.valueOf(2));
			cx.evaluate(block);
			assertEquals(8, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a>>=1;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", Integer.valueOf(8));
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
			cx.set("a", Integer.valueOf(2));
			cx.evaluate(block);
			assertEquals(1d, ((Number) cx.get("a")).doubleValue());
		}
		{
			parser = new Parser("a*=2;");
			block = parser.parse();
			Context cx = new Context();
			cx.set("a", Integer.valueOf(2));
			cx.evaluate(block);
			assertEquals(4, ((Number) cx.get("a")).intValue());
		}
		{
			parser = new Parser("a+=1;var b=a+4;a=b-5;");
			block = parser.parse();
			assertEquals(3, block.size());
			Context cx = new Context();
			cx.set("a", Integer.valueOf(3));
			cx.evaluate(block);
			assertEquals(3, ((Number) cx.get("a")).intValue());
			assertEquals(8, ((Number) cx.get("b")).intValue());
		}
		{
			parser = new Parser("a+=1;a=a+4;");
			block = parser.parse();
			assertEquals(2, block.size());
			Context cx = new Context();
			cx.set("a", Integer.valueOf(3));
			cx.evaluate(block);
			assertEquals(8, ((Number) cx.get("a")).intValue());
		}
		{
			Context cx = new Context();
			cx.evaluate(new Parser("i=0;str='test'+i;").parse());
			assertEquals("test0", cx.get("str").toString());
		}
	}
}
