package cx;

import java.io.File;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.ast.NodeAccess;
import cx.ast.NodeArray;
import cx.ast.NodeAssign;
import cx.ast.NodeBreak;
import cx.ast.NodeCall;
import cx.ast.NodeContinue;
import cx.ast.NodeFor;
import cx.ast.NodeFunction;
import cx.ast.NodeIf;
import cx.ast.NodeNumber;
import cx.ast.NodeObject;
import cx.ast.NodeReturn;
import cx.ast.NodeString;
import cx.ast.NodeSwitch;
import cx.ast.NodeTernary;
import cx.ast.NodeTrue;
import cx.ast.NodeUnary;
import cx.ast.NodeVar;
import cx.ast.NodeVariable;
import cx.ast.NodeWhile;
import cx.exception.ParserException;

public class TestParser extends TestCase {

	public void testTryCatchFinally() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("throw '2'+5;");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			parser = new Parser("throw;");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			parser = new Parser("try ; catch(Exception e);");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			assertEquals(1, block.size());
			parser = new Parser("try{}catch(Exception e){}catch(Exception2 e){}");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			parser = new Parser("try{}catch(Exception1 a){}catch(Exception2 b);catch(Exception3 c){}finally;");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			parser = new Parser("try{}catch(Exception e);finally{}");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			parser = new Parser("try{}finally{}");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
		}
		try {
			parser = new Parser("try ; catch(Exception e);");
			block = parser.parse();
			fail();
		} catch (Throwable e) {
			// OK
		}
		try {
			parser = new Parser("try ; catch(Exception e);");
			block = parser.parse();
			fail();
		} catch (Throwable e) {
			// OK
		}
		try {
			parser = new Parser("try{}");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			fail();
		} catch (Throwable e) {
			// OK
		}
		try {
			parser = new Parser("throw");
			parser.supportTryCatchThrow = true;
			block = parser.parse();
			fail();
		} catch (Throwable e) {
			// OK
		}
	}

	public void testFileParsing() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser(new File("sudoku.cx"));
			block = parser.parse();
			assertEquals(14, block.size());
		}
	}


	public void testMultipleExpressions() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("obj = {n:42, inc:function(){n+=42;}}; obj.inc(); i=obj.n;");
			block = parser.parse();
			assertEquals(3, block.size());
		}
		{
			parser = new Parser("'string'.length(); system.out.print(m,b[5], 'm+p=', m+p);"
					+ "array.subarray[i][j]++; /*comment*/ function empty(){};"
					+ "function a(b, arg, c){ var b = arg % 4, c= 'string'.length(); return b?true:false;};"
					+ "function empty(){}  //  another comment   \n ;"
					+ "var m = 12*(5-6); n = -12* 5-6 ; p = a(m, null); for(i=0, l=5;i<l;i++)m*=i;"
					+ "for(;i==0;--i); for(i:array) {array[i]++;} system.out.print(m,b[5], m+p);"
					+ "for(;i==0;--i) array.subarray[i][j]++;");
			block = parser.parse();
			assertEquals(14, block.size());
		}
	}

	public void testSwitch() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("switch(v){default:break; case 2:case 3:return;}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals(0, nodeSwitch.defaultIndex);
			assertEquals("v", ((NodeVariable) nodeSwitch.value).name);
			assertEquals(2, nodeSwitch.caseValues.length);
			assertEquals(2, nodeSwitch.caseValueIndexes.length);
			assertEquals(2, nodeSwitch.caseStatements.length);
		}
		{
			parser = new Parser("switch(v){case '1': r+=1;b+=3;   case 2: {return;}  }");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals(-1, nodeSwitch.defaultIndex);
			assertEquals("v", ((NodeVariable) nodeSwitch.value).name);
			assertEquals(2, nodeSwitch.caseValues.length);
			assertEquals(2, nodeSwitch.caseValueIndexes.length);
			assertEquals(3, nodeSwitch.caseStatements.length);
		}
		{
			parser = new Parser("switch(a){default:}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals(0, nodeSwitch.defaultIndex);
			assertEquals("a", ((NodeVariable) nodeSwitch.value).name);
			assertEquals(0, nodeSwitch.caseValues.length);
			assertEquals(0, nodeSwitch.caseValueIndexes.length);
			assertEquals(0, nodeSwitch.caseStatements.length);
		}
		{
			parser = new Parser("switch(a){case 'check':}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals(-1, nodeSwitch.defaultIndex);
			assertEquals("a", ((NodeVariable) nodeSwitch.value).name);
			assertEquals(1, nodeSwitch.caseValues.length);
			assertEquals(1, nodeSwitch.caseValueIndexes.length);
			assertEquals(0, nodeSwitch.caseStatements.length);
		}
		{
			parser = new Parser("switch(1){}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals(-1, nodeSwitch.defaultIndex);
			assertEquals("1", ((NodeNumber) nodeSwitch.value).value);
			assertEquals(0, nodeSwitch.caseValues.length);
			assertEquals(0, nodeSwitch.caseValueIndexes.length);
			assertEquals(0, nodeSwitch.caseStatements.length);
		}
		try {
			parser = new Parser("switch(v){case a+3:}");
			parser.parse();
			fail();
		} catch (Throwable e) {
			// OK, expression after , is empty
		}
	}

	public void testFunction() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("function fact(num){ return  (num == 0) ? 1 : num * fact( num - 1 );};");
			block = parser.parse();
			NodeFunction function = (NodeFunction) block.get(0);
			assertNotNull(function.name);
			assertEquals(1, function.argumentNames.length);
			assertEquals(1, function.body.size());
		}
		try {
			parser = new Parser("function(a,b,c,){};");
			parser.parse();
			fail();
		} catch (Throwable e) {
			// OK, expression after , is empty
		}
		{
			parser = new Parser("function(a){};");
			block = parser.parse();
			NodeFunction function = (NodeFunction) block.get(0);
			assertNull(function.name);
			assertEquals(1, function.argumentNames.length);
			assertEquals(0, function.body.size());
		}
		{
			parser = new Parser("function(a,b,c){a++;return a+b+c;};");
			block = parser.parse();
			NodeFunction function = (NodeFunction) block.get(0);
			assertNull(function.name);
			assertEquals(3, function.argumentNames.length);
			assertEquals(2, function.body.size());
		}
		{
			parser = new Parser("function func(){};");
			block = parser.parse();
			NodeFunction function = (NodeFunction) block.get(0);
			assertEquals("func", function.name);
			assertEquals(0, function.argumentNames.length);
			assertEquals(0, function.body.size());
		}
	}

	public void testVar() {
		Parser parser;
		List<Node> block;
		try {
			parser = new Parser("var i,j,;");
			parser.parse();
			fail();
		} catch (Throwable e) {
			// OK, expression after , is empty
		}
		{
			parser = new Parser("var i,j=7,k;");
			block = parser.parse();
			NodeVar var = (NodeVar) block.get(0);
			assertEquals(3, var.vars.size());
			assertTrue(var.defineLocaly);
			assertEquals("i", ((NodeVariable) ((NodeAssign) var.vars.get(0)).left).name);
			assertEquals("j", ((NodeVariable) ((NodeAssign) var.vars.get(1)).left).name);
			assertEquals("k", ((NodeVariable) ((NodeAssign) var.vars.get(2)).left).name);
		}
		{
			parser = new Parser("var i;");
			block = parser.parse();
			NodeVar var = (NodeVar) block.get(0);
			assertEquals(1, var.vars.size());
			NodeAssign assign = (NodeAssign) var.vars.get(0);
			assertEquals("i", ((NodeVariable) assign.left).name);
			assertNull(assign.right);
		}
		{
			parser = new Parser("var i = 5;");
			block = parser.parse();
			NodeVar var = (NodeVar) block.get(0);
			assertEquals(1, var.vars.size());
			NodeAssign assign = (NodeAssign) var.vars.get(0);
			assertEquals("i", ((NodeVariable) assign.left).name);
			assertNotNull(assign.right);
		}

		{
			parser = new Parser("i += 5;");
			block = parser.parse();
			NodeAssign assign = (NodeAssign) block.get(0);
			assertEquals("i", ((NodeVariable) assign.left).name);
			assertNotNull(assign.right);
		}
		try {
			parser = new Parser("var i += 5;");
			parser.parse();
			fail();
		} catch (Throwable e) {
			// variable accepts only initialization
		}
	}

	public void testGoto() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("return bool?true:false;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.get(0));
			assertTrue(ret.expression instanceof NodeTernary);
		}
		{
			parser = new Parser("return a?b:c;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.get(0));
			assertTrue(ret.expression instanceof NodeTernary);
		}
		{
			parser = new Parser("return true;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.get(0));
			assertNotNull(ret.expression);
		}
		{
			parser = new Parser("return null;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.get(0));
			assertNull(ret.expression);
		}
		{
			parser = new Parser("return;");
			block = parser.parse();
			assertTrue((block.get(0)) instanceof NodeReturn);
		}
		{
			parser = new Parser("break;");
			block = parser.parse();
			assertTrue((block.get(0)) instanceof NodeBreak);
		}
		{
			parser = new Parser("continue;");
			block = parser.parse();
			assertTrue((block.get(0)) instanceof NodeContinue);
		}
	}

	public void testWhile() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("do i++; while(i<4);");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.get(0);
			assertNotNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertTrue(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("do{i++;}while();");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.get(0);
			assertNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertTrue(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("do;while(true);");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.get(0);
			assertNotNull(nodeWhile.condition);
			assertNull(nodeWhile.body);
			assertTrue(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while();");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.get(0);
			assertNull(nodeWhile.condition);
			assertNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while(){}");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.get(0);
			assertNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while(true){a;}");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.get(0);
			assertNotNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while();");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.get(0);
			assertNull(nodeWhile.condition);
			assertNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
	}

	public void testFor() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("for(i=0;i<0;i++);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNotNull(nodeFor.initialization);
			assertNotNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(var a=0;a<0;a++);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNotNull(nodeFor.initialization);
			assertNotNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(var a:array);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNotNull(nodeFor.element);
			assertNotNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(a:array);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNotNull(nodeFor.element);
			assertNotNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(;;);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNull(nodeFor.element);
			assertNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(a;;);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNotNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(;true;)i++;");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNull(nodeFor.initialization);
			assertNotNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNotNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(;;i++);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(;;){}");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNotNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
	}

	public void testIf() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("if(a){}");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.get(0);
			assertEquals("a", ((NodeVariable) nodeIf.condition).name);
			assertNotNull(nodeIf.body);
			assertNull(nodeIf.elseBody);
		}
		{
			parser = new Parser("if(a);");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.get(0);
			assertEquals("a", ((NodeVariable) nodeIf.condition).name);
			assertNull(nodeIf.body);
			assertNull(nodeIf.elseBody);
		}
		{
			parser = new Parser("if(true);else {}");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.get(0);
			assertTrue(nodeIf.condition instanceof NodeTrue);
			assertNull(nodeIf.body);
			assertNotNull(nodeIf.elseBody);
		}
		{
			parser = new Parser("if(true){a;}else{b;}");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.get(0);
			assertTrue(nodeIf.condition instanceof NodeTrue);
			assertNotNull(nodeIf.body);
			assertNotNull(nodeIf.elseBody);
		}
	}

	public void testCalling() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("call();");
			block = parser.parse();
			NodeCall call = (NodeCall) block.get(0);
			NodeVariable method = (NodeVariable) call.function;
			assertEquals("call", method.name);
			assertEquals(0, call.arguments.size());
		}
		{
			parser = new Parser("call(1,a,b=c);");
			block = parser.parse();
			NodeCall call = (NodeCall) block.get(0);
			NodeVariable method = (NodeVariable) call.function;
			assertEquals("call", method.name);
			assertEquals(3, call.arguments.size());
		}
		{
			parser = new Parser("obj.call(1,a,);");
			block = parser.parse();
			NodeCall call = (NodeCall) block.get(0);
			NodeAccess access = (NodeAccess) call.function;
			NodeVariable object = (NodeVariable) access.object;
			NodeString element = (NodeString) access.element;
			assertEquals("obj", object.name);
			assertEquals("call", element.value);
			assertEquals(3, call.arguments.size());
			List<Node> args = call.arguments;
			assertNull(args.get(2));
		}
		{
			parser = new Parser("obj.call(,);");
			block = parser.parse();
			NodeCall call = (NodeCall) block.get(0);
			NodeAccess access = (NodeAccess) call.function;
			NodeVariable object = (NodeVariable) access.object;
			NodeString element = (NodeString) access.element;
			assertEquals("obj", object.name);
			assertEquals("call", element.value);
			assertEquals(2, call.arguments.size());
			List<Node> args = call.arguments;
			assertNull(args.get(0));
			assertNull(args.get(1));
		}
	}

	public void testIndexing() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("array.subarray[i][j]++;");
			block = parser.parse();
			NodeUnary inc = (NodeUnary) block.get(0);
			assertEquals(Operator.INC_POST, inc.operator);
			NodeAccess chain1 = (NodeAccess) inc.expresion;
			assertEquals("j", ((NodeVariable) chain1.element).name);
			NodeAccess chain2 = (NodeAccess) chain1.object;
			assertEquals("i", ((NodeVariable) chain2.element).name);
			NodeAccess chain3 = (NodeAccess) chain2.object;
			assertEquals("subarray", ((NodeString) chain3.element).value);
			assertEquals("array", ((NodeVariable) chain3.object).name);
		}
		{
			parser = new Parser("object[5];");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.get(0);
			NodeVariable object = (NodeVariable) access.object;
			NodeNumber element = (NodeNumber) access.element;
			assertEquals("object", object.name);
			assertEquals("5", element.value);
		}
		{
			parser = new Parser("object['test'];");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.get(0);
			NodeVariable object = (NodeVariable) access.object;
			NodeString element = (NodeString) access.element;
			assertEquals("object", object.name);
			assertEquals("test", element.value);
		}
	}

	public void testChaining() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("object.'5';");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.get(0);
			NodeVariable object = (NodeVariable) access.object;
			NodeString element = (NodeString) access.element;
			assertEquals("object", object.name);
			assertEquals("5", element.value);
		}
		try {
			parser = new Parser("object.5;");
			parser.parse();
			fail();
		} catch (Throwable e) {
			// ok .5 is a number
		}
		{
			parser = new Parser("object.element;");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.get(0);
			NodeVariable object = (NodeVariable) access.object;
			NodeString element = (NodeString) access.element;
			assertEquals("object", object.name);
			assertEquals("element", element.value);
		}

		{
			parser = new Parser("object.element.subelement;");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.get(0);
			NodeAccess accessParent = (NodeAccess) access.object;
			NodeVariable object = (NodeVariable) accessParent.object;
			NodeString element = (NodeString) accessParent.element;
			NodeString subelement = (NodeString) access.element;
			assertEquals("object", object.name);
			assertEquals("element", element.value);
			assertEquals("subelement", subelement.value);
		}

	}

	public void testNewObjects() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("new parent { ,  1:'value', };");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 1);
			assertTrue(objMap.get("1") instanceof NodeString);
			assertEquals(((NodeString) objMap.get("1")).value, "value");
		}
		{// clone
			parser = new Parser("new parent;");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {a:1,  1:'value', 'a b':5};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 3);
			assertTrue(objMap.get("a") instanceof NodeNumber);
			assertEquals(((NodeNumber) objMap.get("a")).value, "1");
			assertTrue(objMap.get("1") instanceof NodeString);
			assertEquals(((NodeString) objMap.get("1")).value, "value");
			assertTrue(objMap.get("a b") instanceof NodeNumber);
			assertEquals(((NodeNumber) objMap.get("a b")).value, "5");
		}
		{
			parser = new Parser("new parent {};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {,,,,,,,,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {arr:[1,2,3]};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 1);
			assertTrue(objMap.get("arr") instanceof NodeArray);
			NodeArray arr = (NodeArray) objMap.get("arr");
			assertEquals(3, arr.elements.size());
			assertEquals(((NodeNumber) arr.elements.get(0)).value, "1");
		}
	}

	public void testObjects() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("{a:1,  1:'value', 'a b':5};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertEquals(null, obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 3);
			assertTrue(objMap.get("a") instanceof NodeNumber);
			assertEquals(((NodeNumber) objMap.get("a")).value, "1");
			assertTrue(objMap.get("1") instanceof NodeString);
			assertEquals(((NodeString) objMap.get("1")).value, "value");
			assertTrue(objMap.get("a b") instanceof NodeNumber);
			assertEquals(((NodeNumber) objMap.get("a b")).value, "5");
		}
		{
			parser = new Parser("{ ,  1:'value', };");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 1);
			assertTrue(objMap.get("1") instanceof NodeString);
			assertEquals(((NodeString) objMap.get("1")).value, "value");
		}
		{
			parser = new Parser("{};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("{,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("{,,,,,,,,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
	}

	public void testArrays() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("[1,'2', \"3\"];");
			block = parser.parse();
			NodeArray arr = (NodeArray) block.get(0);
			List<Node> list = arr.elements;
			assertEquals(list.size(), 3);
			assertTrue(list.get(0) instanceof NodeNumber);
			assertEquals(((NodeNumber) list.get(0)).value, "1");
			assertTrue(list.get(1) instanceof NodeString);
			assertEquals(((NodeString) list.get(1)).value, "2");
			assertTrue(list.get(2) instanceof NodeString);
			assertEquals(((NodeString) list.get(2)).value, "3");
		}
		{
			parser = new Parser("[,'2',];");
			block = parser.parse();
			NodeArray arr = (NodeArray) block.get(0);
			List<Node> list = arr.elements;
			assertEquals(list.size(), 3);
			assertTrue(list.get(0) == null);
			assertTrue(list.get(1) instanceof NodeString);
			assertEquals(((NodeString) list.get(1)).value, "2");
			assertTrue(list.get(2) == null);
		}
		{
			parser = new Parser("[];");
			block = parser.parse();
			NodeArray arr = (NodeArray) block.get(0);
			List<Node> list = arr.elements;
			assertEquals(list.size(), 0);
		}
	}

	public void testCardinals() {
		{
			List<Node> block = new Parser("'test';").parse();
			NodeString str = (NodeString) block.get(0);
			assertEquals(str.value, "test");
		}
		{
			List<Node> block = new Parser("0.0;").parse();
			NodeNumber n = (NodeNumber) block.get(0);
			assertEquals(n.value, "0.0");
			assertEquals(n.number.doubleValue(), 0.0D);
		}
		{
			List<Node> block = new Parser("0e5;").parse();
			NodeNumber n = (NodeNumber) block.get(0);
			assertEquals(n.value, "0e5");
			assertEquals(n.number.doubleValue(), 0D);
		}
		try {
			new Parser("0e;").parse();
			fail();
		} catch (ParserException e) {
			// ok
		}
		{
			List<Node> block = new Parser("0x5;").parse();
			NodeNumber n = (NodeNumber) block.get(0);
			assertEquals(n.value, "0x5");
			assertEquals(n.number.intValue(), 5);
		}
		try {
			new Parser("0x;").parse();
			fail();
		} catch (ParserException e) {
			// ok
		}
	}
}
