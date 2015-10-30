package cx;

import java.util.List;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.ast.Visitor;
import cx.exception.JumpReturn;
import cx.runtime.ContextFrame;
import cx.runtime.Handler;

public class TestContext extends TestCase {
	public void testIf() {
		{
			Context cx = new Context();
			cx.evaluate((new Parser("function test(v){ if(v.a) return v.a; else if(v.b(3)){return '3';} };")).parse());
			List<Node> block = (new Parser("f=test({a:0, b:function(c){return c;}});")).parse();

			cx.evaluate(block);
			assertEquals("3", cx.get("f"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser(
					"function test(v){ if(v==1) return 'a'; else if(v==2){return 'b';} else if(v==3) return 'c'; };")).parse());
			List<Node> block = (new Parser("f=test(f);")).parse();

			cx.set("f", Integer.valueOf(1));
			cx.evaluate(block);
			assertEquals("a", cx.get("f"));

			cx.set("f", Integer.valueOf(2));
			cx.evaluate(block);
			assertEquals("b", cx.get("f"));

			cx.set("f", Integer.valueOf(3));
			cx.evaluate(block);
			assertEquals("c", cx.get("f"));
		}
	}

	public void testChainCalls() {
		Parser parser = new Parser();
		{
			Context cx = new Context();
			List<Node> block = parser.parse("obj = {r:5, inc:function(){r++;}, dec:function(){r--;}}; obj.inc().inc().inc().dec().inc(); r = obj.r;");
			cx.evaluate(block);
			assertEquals(8L, cx.get("r"));
		}
		{
			Context cx = new Context();
			List<Node> block = parser.parse("obj = {r:5, getInstance:function(){}}; r = obj.r; obj1 = obj.getInstance(); obj1.r=8; r1 = obj.r; r2 = obj1.r;");
			cx.evaluate(block);
			assertEquals(8L, cx.get("r1"));
			assertEquals(8L, cx.get("r2"));
		}
		{
			Context cx = new Context();
			List<Node> block = parser.parse("r = 8; function getInstance(){}; obj = getInstance(); obj.r = 9; r1 = obj.r;");
			cx.evaluate(block);
			assertEquals(9L, cx.get("r"));
			assertEquals(9L, cx.get("r1"));
		}
	}

	public void testIndexing() {
		Parser parser = new Parser();
		List<Node> block;
		{
			Context cx = new Context();
			block = parser.parse("arr=[1,2,3,4,5]; var1 = arr['0']; var2 = arr['2']; var3 = arr['10']; var4 = arr['-2']; var5 = arr['-10'];");
			cx.evaluate(block);
			assertEquals(1L, cx.get("var1"));
			assertEquals(3L, cx.get("var2"));
			assertNull(cx.get("var3"));
			assertEquals(4L, (char) ((Long) cx.get("var4")).intValue());
			assertNull(cx.get("var5"));
		}
		{
			Context cx = new Context();
			block = parser.parse("arr=[1,2,3,4,5]; var1 = arr[0]; var2 = arr[2]; var3 = arr[10]; var4 = arr[-2]; var5 = arr[-10];");
			cx.evaluate(block);
			assertEquals(1L, cx.get("var1"));
			assertEquals(3L, cx.get("var2"));
			assertNull(cx.get("var3"));
			assertEquals(4L, (char) ((Long) cx.get("var4")).intValue());
			assertNull(cx.get("var5"));
		}
		{
			Context cx = new Context();
			block = parser.parse("str='12345'; var1 = str['0']; var2 = str['2']; var3 = str['10']; var4 = str['-2']; var5 = str['-10'];");
			cx.evaluate(block);
			assertEquals('1', (char) ((Long) cx.get("var1")).intValue());
			assertEquals('3', (char) ((Long) cx.get("var2")).intValue());
			assertNull(cx.get("var3"));
			assertEquals('4', (char) ((Long) cx.get("var4")).intValue());
			assertNull(cx.get("var5"));
		}
		{
			Context cx = new Context();
			block = parser.parse("str='12345'; var1 = str[0]; var2 = str[2]; var3 = str[10]; var4 = str[-2]; var5 = str[-10];");
			cx.evaluate(block);
			assertEquals('1', (char) ((Long) cx.get("var1")).intValue());
			assertEquals('3', (char) ((Long) cx.get("var2")).intValue());
			assertNull(cx.get("var3"));
			assertEquals('4', (char) ((Long) cx.get("var4")).intValue());
			assertNull(cx.get("var5"));
		}
	}

	public void testBinaryArithmetic() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser(
					"a = true; b = false; valAdd = a + b; valSub = a - b; valMul = a * b; valDiv = a / b; valMod = a % b;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(Boolean.TRUE, cx.get("valAdd"));
			assertEquals(Boolean.TRUE, cx.get("valSub"));
			assertEquals(Boolean.FALSE, cx.get("valMul"));
			assertEquals(Boolean.TRUE, cx.get("valDiv"));
			assertEquals(null, cx.get("valMod"));
		}
		{
			parser = new Parser(
					"a = 24; b = 2; valAdd = a + b; valSub = a - b; valMul = a * b; valDiv = a / b; valMod = a % b;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(26L, cx.get("valAdd"));
			assertEquals(22L, cx.get("valSub"));
			assertEquals(48L, cx.get("valMul"));
			assertEquals(12d, cx.get("valDiv"));
			assertEquals(0L, cx.get("valMod"));
		}
		{
			parser = new Parser(
					"a = 'str'; b = 2; valAdd = a + b; valSub = a - b; valMul = a * b; valDiv = a / b; valMod = a % b;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals("str2", cx.get("valAdd"));
			assertEquals(null, cx.get("valSub"));
			assertEquals(null, cx.get("valMul"));
			assertEquals(null, cx.get("valDiv"));
			assertEquals(null, cx.get("valMod"));
		}
		{
			parser = new Parser(
					"a = []; b = 2; valAdd = a + b; valSub = a - b; valMul = a * b; valDiv = a / b; valMod = a % b;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertTrue(cx.get("valAdd") instanceof List);
			List<?> l = (List<?>) cx.get("valAdd");
			assertEquals(1, l.size());
			assertEquals(2L, l.get(0));
			assertEquals(null, cx.get("valSub"));
			assertEquals(null, cx.get("valMul"));
			assertEquals(null, cx.get("valDiv"));
			assertEquals(null, cx.get("valMod"));
		}
		{
			parser = new Parser(
					"a = {}; b = 2; valAdd = a + b; valSub = a - b; valMul = a * b; valDiv = a / b; valMod = a % b;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(null, cx.get("valAdd"));
			assertEquals(null, cx.get("valSub"));
			assertEquals(null, cx.get("valMul"));
			assertEquals(null, cx.get("valDiv"));
			assertEquals(null, cx.get("valMod"));
		}
	}

	public void testUnaryArithmetic() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("str = '-42'; n = -12.34; val1 = +str; val2 = +n; val3 = --'13'; val4 = '13'++;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(42L, cx.get("val1"));
			assertEquals(12.34d, cx.get("val2"));
			assertEquals(12L, cx.get("val3"));
			assertEquals("13", cx.get("val4"));
		}
		{
			parser = new Parser("str = '-42.42'; val1 = +str; val2 = ++str; val3 = ++val1;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(43.42d, cx.get("val1"));
			assertEquals(-41.42d, cx.get("val2"));
			assertEquals(43.42d, cx.get("val3"));
		}
		{
			parser = new Parser("val1 = ~~'-5';val2 = ~~'3.14';");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(-5L, cx.get("val1"));
			assertEquals(3L, cx.get("val2"));
		}
	}

	public void testAccess() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("obj = {};\n obj[6] = 'six'; r1 = obj.'6'; r2 = obj[6]; r3 = obj['6'];");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals("six", cx.get("r1"));
			assertEquals("six", cx.get("r2"));
			assertEquals("six", cx.get("r3"));
		}
		{
			parser = new Parser("obj = {}; obj.a = 5; r1 = obj.a; r2 = obj.'a'; r3 = obj[a]; r4 = obj['a'];");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(5L, cx.get("r1"));
			assertEquals(5L, cx.get("r2"));
			assertNull(cx.get("r3"));
			assertEquals(5L, cx.get("r4"));
		}
		{
			parser = new Parser("a = 'a'; obj = {}; obj.a = 5; r1 = obj.a; r2 = obj.'a'; r3 = obj[a]; r4 = obj['a'];");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(5L, cx.get("r1"));
			assertEquals(5L, cx.get("r2"));
			assertEquals(5L, cx.get("r3"));
			assertEquals(5L, cx.get("r4"));
		}
		{
			parser = new Parser(
					"obj = {b:7}; a = 'b'; obj.a = 5; r1 = obj.a; r2 = obj.'a'; r3 = obj[a]; r4 = obj['a'];");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(5L, cx.get("r1"));
			assertEquals(5L, cx.get("r2"));
			assertEquals(7L, cx.get("r3"));
			assertEquals(5L, cx.get("r4"));
		}
	}

	public void testLength() {
		{
			Context cx = new Context();
			cx.evaluate((new Parser("a = 'test'; len = a.length;")).parse());
			assertEquals(4L, cx.get("len"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("a = [1,2,3]; len = a.length;")).parse());
			assertEquals(3L, cx.get("len"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("a = {a:4, b:'string'}; len = a.length;")).parse());
			assertEquals(2L, cx.get("len"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("a = 5; len = a.length;")).parse());
			assertNull(cx.get("len"));
		}
	}

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
		{// object
			Context cx = new Context();
			PrintHandler printHandler = new PrintHandler();
			cx.addHandler(printHandler);
			cx.evaluate((new Parser(
					"o={number:5, str:'string', arr:[0,1], obj: new {}, 'simpleObj':{} }; b=eval(''+o+';');")).parse());
			assertTrue(cx.get("b") instanceof ContextFrame);
			ContextFrame frame = (ContextFrame) cx.get("b");
			assertEquals(5L, frame.get("number"));
			assertEquals("string", frame.get("str"));
			assertTrue(frame.get("arr") instanceof List);
			assertTrue(frame.get("obj") instanceof ContextFrame);
			assertTrue(frame.get("simpleObj") instanceof ContextFrame);
		}
	}

	private static class PrintHandler implements Handler {
		public String value = "";

		public Object[] supportedClasses() {
			return null;
		}

		public void set(Object thiz, String method, Object value) {}

		public Object get(Object thiz, String method) {
			return null;
		}

		public Object call(Object object, Object[] args) {
			return null;
		}

		public String[] supportedStaticCalls() {
			return new String[] { "breakpoint", "print", "log" };
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
			cx.evaluate((new Parser("a = 5; inc = eval('function _(x){return ++x;};'); a = inc(a);")).parse());
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
		{
			Context cx = new Context();
			Parser parser = new Parser();
			parser.supportTryCatchThrow = true;
			cx.evaluate(parser.parse("result = false; try{ throw AnyException; } catch(e){result = true;} "));
			assertEquals(Boolean.TRUE, cx.get("result"));
			cx.evaluate(parser.parse("result = false; try{ throw MyException; } catch(Error e){ } catch(MyException e){result = true;} "));
			assertEquals(Boolean.TRUE, cx.get("result"));
			cx.evaluate(parser.parse("result = true; try{ throw Error(false); } catch(Error e){result = e; } catch(MyException e){result = true;} "));
			assertEquals(Boolean.FALSE, cx.get("result"));
		}
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
					"var f = 0; try{ if(arg==1)throw Long(1); if(arg==2)throw String('str');}catch(Long e){arg = 'long';}catch(String e){arg = 'string';}finally{f = 42;}");
			parser.supportTryCatchThrow = true;
			List<Node> block = parser.parse();

			cx.set("arg", 0);
			cx.evaluate(block);
			assertEquals("0", cx.get("arg").toString());
			assertEquals("42", cx.get("f").toString());

			cx.set("arg", 1);
			cx.evaluate(block);
			assertEquals("long", cx.get("arg").toString());
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
					"try{ if(arg==1)throw Long(1); if(arg==2)throw String('str');}catch(Long e){arg = 'integer';}catch(String e){arg = 'string';}");
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
			cx.evaluate((new Parser(
					"function gcd(a, b) { var diff = a-b; if (diff == 0)return a; return diff > 0 ? gcd(b, diff) : gcd(a, -diff); }  ")).parse());
			cx.evaluate((new Parser("result=gcd(60, 40);")).parse());
			assertEquals(20L, cx.get("result"));

			cx.evaluate((new Parser("var mygcd = gcd; result = mygcd(7, 40);")).parse());
			assertEquals(1L, cx.get("result"));
		}
		{// return function
			Context cx = new Context();
			cx.evaluate((new Parser("function g(n){ return function(){return n+42;};} var f = g(42);result = f(42);")).parse());
			assertEquals(84L, cx.get("result"));
		}
		{// return function
			Context cx = new Context();
			cx.evaluate((new Parser("function g(n){ return function(){return n+42;};} result = g(42)(42);")).parse());
			assertEquals(84L, cx.get("result"));
		}
		{// arguments
			Context cx = new Context();
			cx.evaluate((new Parser("function f(num){ return arguments;}")).parse());
			cx.evaluate((new Parser("result=f();")).parse());
			assertEquals(0, ((List) cx.get("result")).size());

			cx.evaluate((new Parser("result=f(1,2,3,4,5);")).parse());
			List args = (List) cx.get("result");
			assertEquals(5, args.size());
			assertEquals(1L, args.get(0));
			assertEquals(5L, args.get(4));
		}
		{// arguments as last executed statement will be returned
			Context cx = new Context();
			cx.evaluate((new Parser("function f(num){return arguments;}")).parse());
			cx.evaluate((new Parser("result=f();")).parse());
			assertEquals(0, ((List) cx.get("result")).size());

			cx.evaluate((new Parser("result=f(1,2,3);")).parse());
			List args = (List) cx.get("result");
			assertEquals(3, args.size());
			assertEquals(1L, args.get(0));
			assertEquals(3L, args.get(2));
		}
		{// arguments.length
			Context cx = new Context();
			cx.evaluate((new Parser("function f(num){return arguments.length;}")).parse());
			cx.evaluate((new Parser("result=f(1,2,3,4);")).parse());
			assertEquals(4L, cx.get("result"));
			cx.evaluate((new Parser("result=f();")).parse());
			assertEquals(0L, cx.get("result"));
		}
		{// Factorial calculating
			Context cx = new Context();
			cx.evaluate((new Parser("function fact(num){ return  (num == 0) ? 1 : num * fact( num - 1 );}")).parse());
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
		{// return without function
			Context cx = new Context();
			List<Node> block = (new Parser()).parse("return 10;");
			try {
				cx.evaluate(block);
				fail();
			} catch (JumpReturn e) {}
		}
	}

	public void testObject() {
		{// test inheritance with flatten and function this update
			Context cx = new Context();
			cx.evaluate((new Parser("obj = {key:'value', setValue:function(value){key = value;}};")).parse());
			cx.evaluate((new Parser("obj.setValue('oldValue'); newObj = new obj{}; newObj.setValue('newValue');")).parse());
			cx.evaluate((new Parser("key1 = obj.key; key2 = newObj.key;")).parse());
			assertEquals("oldValue", cx.get("key1"));
			assertEquals("newValue", cx.get("key2"));
			cx.evaluate((new Parser("newObj.key = null; key3 = newObj.key;")).parse());
			assertEquals("oldValue", cx.get("key3"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("var obj1 = new {};")).parse());
			ContextFrame obj = (ContextFrame) cx.get("obj1");
			assertEquals(0, obj.size());

			cx.evaluate((new Parser("var obj2 = {};")).parse());
			obj = (ContextFrame) cx.get("obj2");
			assertEquals(0, obj.size());

			cx.evaluate((new Parser("obj2['message'] = 'string';")).parse());
			obj = (ContextFrame) cx.get("obj2");
			assertEquals(1, obj.size());
			assertEquals("string", obj._get("message"));

			cx.evaluate((new Parser("var obj3 = new obj2 {getMessage: function(){ return message;} };")).parse());
			cx.evaluate((new Parser(
					"var msg1 = obj3.getMessage(); var msg2 = obj3['message']; var msg3 = obj3.message;")).parse());
			obj = (ContextFrame) cx.get("obj3");
			// new object with parent inherit all context elements
			assertEquals(2, obj.size());

			assertEquals("string", cx.get("msg1"));
			assertEquals("string", cx.get("msg2"));
			assertEquals("string", cx.get("msg3"));

			cx.evaluate((new Parser(
					"var obj3 = new obj2 {}; var obj4 = new obj2 {}; obj3.message = 'str1'; obj4.message = 'str2'; var msg1 = obj3.message; var msg2 = obj4.message;")).parse());

			assertEquals("str1", cx.get("msg1"));
			assertEquals("str2", cx.get("msg2"));

			// undefine object value to access the parent context
			cx.evaluate((new Parser(" obj3.message = null; var msg1 = obj3.message; ")).parse());
			assertEquals("string", cx.get("msg1"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("obj = {a:1, b: 'string'}; obj.a++; obj.b += ' more';")).parse());
			ContextFrame obj = (ContextFrame) cx.get("obj");
			assertEquals(2, obj.size());
			assertEquals(2L, obj.get("a"));
			assertEquals("string more", obj.get("b"));
		}
		{
			Context cx = new Context();
			cx.evaluate((new Parser("obj = {a:1, b: 'string'};")).parse());
			ContextFrame obj = (ContextFrame) cx.get("obj");
			assertEquals(2, obj.size());
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
		{
			parser = new Parser("a = true ? 1 : 2;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(1L, ((Number) cx.get("a")).longValue());
		}
		{
			parser = new Parser("a = 3 ?? 5;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(3L, ((Number) cx.get("a")).longValue());
		}
		{
			parser = new Parser("a = 'a' ?? 5;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals("a", cx.get("a"));
		}
		{
			parser = new Parser("a = null ?? 5;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(5L, ((Number) cx.get("a")).longValue());
		}
		{
			parser = new Parser("b = null; a = b ?? 5;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(5L, ((Number) cx.get("a")).longValue());
		}
	}

	public void testArithmetic() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("i = 1; i = +i;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(1L, cx.get("i"));
		}
		{
			parser = new Parser("var i = -1; i = +i;");
			block = parser.parse();
			Context cx = new Context();
			cx.evaluate(block);
			assertEquals(1L, cx.get("i"));
		}
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
