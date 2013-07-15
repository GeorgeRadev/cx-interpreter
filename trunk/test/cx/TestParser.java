package cx;

import java.io.File;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.ast.NodeAccess;
import cx.ast.NodeArray;
import cx.ast.NodeAssign;
import cx.ast.NodeBlock;
import cx.ast.NodeBreak;
import cx.ast.NodeCall;
import cx.ast.NodeCaseList;
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

public class TestParser extends TestCase {

	public void testTryCatchFinally() {
		Parser parser;
		NodeBlock block;
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
			assertEquals(1, block.statements.size());
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
		NodeBlock block;
		{
			parser = new Parser(new File("sudoku.cx"));
			block = parser.parse();
			assertEquals(14, block.statements.size());
		}
	}


	public void testMultipleExpressions() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("obj = {n:42, inc:function(){n+=42;}}; obj.inc(); i=obj.n;");
			block = parser.parse();
			assertEquals(3, block.statements.size());
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
			assertEquals(14, block.statements.size());
		}
	}

	public void testSwitch() {
		Parser parser;
		NodeBlock block;
		try {
			parser = new Parser("switch(v){case a+3:}");
			parser.parse();
			fail();
		} catch (Throwable e) {
			// OK, expression after , is empty
		}
		{
			parser = new Parser("switch(v){default:break; case 2:case 3:return;}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.statements.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals(0, nodeSwitch.defaultIndex);
			assertEquals("v", ((NodeVariable) nodeSwitch.value).name);

			NodeCaseList caselist = nodeSwitch.cases;
			assertEquals(3, caselist.cases.size());
			assertNull(caselist.cases.get(0).caseValue);
			assertEquals("2", caselist.cases.get(1).caseValue.value);
			assertEquals("3", caselist.cases.get(2).caseValue.value);
		}
		{
			parser = new Parser("switch(v){case '1': r+=1;b+=3;   case 2: {return;}  }");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.statements.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals(-1, nodeSwitch.defaultIndex);
			assertEquals("v", ((NodeVariable) nodeSwitch.value).name);

			NodeCaseList caselist = nodeSwitch.cases;
			assertEquals(2, caselist.cases.size());
			assertEquals("1", caselist.cases.get(0).caseValue.value);
			assertEquals("2", caselist.cases.get(1).caseValue.value);
		}
		{
			parser = new Parser("switch(a){default:}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.statements.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals("a", ((NodeVariable) nodeSwitch.value).name);
			assertEquals(1, nodeSwitch.cases.cases.size());
		}
		{
			parser = new Parser("switch(a){case 'check':}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.statements.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals("a", ((NodeVariable) nodeSwitch.value).name);
			assertEquals(1, nodeSwitch.cases.cases.size());
		}
		{
			parser = new Parser("switch(1){}");
			block = parser.parse();
			NodeSwitch nodeSwitch = (NodeSwitch) block.statements.get(0);
			assertNotNull(nodeSwitch.value);
			assertEquals("1", ((NodeNumber) nodeSwitch.value).value);
			assertEquals(0, nodeSwitch.cases.cases.size());
		}
	}

	public void testFunction() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("function fact(num){ return  (num == 0) ? 1 : num * fact( num - 1 );};");
			block = parser.parse();
			NodeFunction function = (NodeFunction) block.statements.get(0);
			assertNotNull(function.name);
			assertEquals(1, function.arguments.elements.size());
			assertEquals(1, function.body.statements.size());
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
			NodeFunction function = (NodeFunction) block.statements.get(0);
			assertNull(function.name);
			assertEquals(1, function.arguments.elements.size());
			assertEquals(0, function.body.statements.size());
		}
		{
			parser = new Parser("function(a,b,c){a++;return a+b+c;};");
			block = parser.parse();
			NodeFunction function = (NodeFunction) block.statements.get(0);
			assertNull(function.name);
			assertEquals(3, function.arguments.elements.size());
			assertEquals(2, function.body.statements.size());
		}
		{
			parser = new Parser("function func(){};");
			block = parser.parse();
			NodeFunction function = (NodeFunction) block.statements.get(0);
			assertEquals("func", function.name);
			assertEquals(0, function.arguments.elements.size());
			assertEquals(0, function.body.statements.size());
		}
	}

	public void testVar() {
		Parser parser;
		NodeBlock block;
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
			NodeVar var = (NodeVar) block.statements.get(0);
			assertEquals(3, var.vars.size());
			assertEquals("i", ((NodeVariable) var.vars.get(0).left).name);
			assertEquals("j", ((NodeVariable) var.vars.get(1).left).name);
			assertEquals("k", ((NodeVariable) var.vars.get(2).left).name);
		}
		{
			parser = new Parser("var i;");
			block = parser.parse();
			NodeVar var = (NodeVar) block.statements.get(0);
			assertEquals(1, var.vars.size());
			NodeAssign assign = var.vars.get(0);
			assertEquals("i", ((NodeVariable) assign.left).name);
			assertNull(assign.right);
		}
		{
			parser = new Parser("var i = 5;");
			block = parser.parse();
			NodeVar var = (NodeVar) block.statements.get(0);
			assertEquals(1, var.vars.size());
			NodeAssign assign = var.vars.get(0);
			assertEquals("i", ((NodeVariable) assign.left).name);
			assertNotNull(assign.right);
		}

		{
			parser = new Parser("i += 5;");
			block = parser.parse();
			NodeAssign assign = (NodeAssign) block.statements.get(0);
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
		NodeBlock block;
		{
			parser = new Parser("return bool?true:false;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.statements.get(0));
			assertTrue(ret.expression instanceof NodeTernary);
		}
		{
			parser = new Parser("return a?b:c;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.statements.get(0));
			assertTrue(ret.expression instanceof NodeTernary);
		}
		{
			parser = new Parser("return true;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.statements.get(0));
			assertNotNull(ret.expression);
		}
		{
			parser = new Parser("return null;");
			block = parser.parse();
			NodeReturn ret = (NodeReturn) (block.statements.get(0));
			assertNull(ret.expression);
		}
		{
			parser = new Parser("return;");
			block = parser.parse();
			assertTrue((block.statements.get(0)) instanceof NodeReturn);
		}
		{
			parser = new Parser("break;");
			block = parser.parse();
			assertTrue((block.statements.get(0)) instanceof NodeBreak);
		}
		{
			parser = new Parser("continue;");
			block = parser.parse();
			assertTrue((block.statements.get(0)) instanceof NodeContinue);
		}
	}

	public void testWhile() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("do i++; while(i<4);");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.statements.get(0);
			assertNotNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertTrue(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("do{i++;}while();");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.statements.get(0);
			assertNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertTrue(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("do;while(true);");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.statements.get(0);
			assertNotNull(nodeWhile.condition);
			assertNull(nodeWhile.body);
			assertTrue(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while();");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.statements.get(0);
			assertNull(nodeWhile.condition);
			assertNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while(){}");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.statements.get(0);
			assertNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while(true){a;}");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.statements.get(0);
			assertNotNull(nodeWhile.condition);
			assertNotNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
		{
			parser = new Parser("while();");
			block = parser.parse();
			NodeWhile nodeWhile = (NodeWhile) block.statements.get(0);
			assertNull(nodeWhile.condition);
			assertNull(nodeWhile.body);
			assertFalse(nodeWhile.isDoWhile);
		}
	}

	public void testFor() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("for(i=0;i<0;i++);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNotNull(nodeFor.initialization);
			assertNotNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(var a=0;a<0;a++);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNotNull(nodeFor.initialization);
			assertNotNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(var a:array);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNotNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(a:array);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNotNull(nodeFor.elements);
			assertNull(nodeFor.body);
		}
		{
			parser = new Parser("for(;;);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(a;;);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNotNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(;true;)i++;");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNull(nodeFor.initialization);
			assertNotNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNotNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(;;i++);");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNotNull(nodeFor.iterator);
			assertNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
		{
			parser = new Parser("for(;;){}");
			block = parser.parse();
			NodeFor nodeFor = (NodeFor) block.statements.get(0);
			assertNull(nodeFor.initialization);
			assertNull(nodeFor.condition);
			assertNull(nodeFor.iterator);
			assertNotNull(nodeFor.body);
			assertNull(nodeFor.elements);
		}
	}

	public void testIf() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("if(a){}");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.statements.get(0);
			assertEquals("a", ((NodeVariable) nodeIf.condition).name);
			assertTrue(nodeIf.body instanceof NodeBlock);
			assertNull(nodeIf.elseBody);
		}
		{
			parser = new Parser("if(a);");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.statements.get(0);
			assertEquals("a", ((NodeVariable) nodeIf.condition).name);
			assertNull(nodeIf.body);
			assertNull(nodeIf.elseBody);
		}
		{
			parser = new Parser("if(true);else {}");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.statements.get(0);
			assertTrue(nodeIf.condition instanceof NodeTrue);
			assertTrue(nodeIf.body == null);
			assertTrue(nodeIf.elseBody instanceof NodeBlock);
		}
		{
			parser = new Parser("if(true){a;}else{b;}");
			block = parser.parse();
			NodeIf nodeIf = (NodeIf) block.statements.get(0);
			assertTrue(nodeIf.condition instanceof NodeTrue);
			assertTrue(nodeIf.body instanceof NodeBlock);
			assertTrue(nodeIf.elseBody instanceof NodeBlock);
		}
	}

	public void testCalling() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("call();");
			block = parser.parse();
			NodeCall call = (NodeCall) block.statements.get(0);
			NodeVariable method = (NodeVariable) call.function;
			NodeArray params = (NodeArray) call.arguments;
			assertEquals("call", method.name);
			assertEquals(0, params.elements.size());
		}
		{
			parser = new Parser("call(1,a,b=c);");
			block = parser.parse();
			NodeCall call = (NodeCall) block.statements.get(0);
			NodeVariable method = (NodeVariable) call.function;
			NodeArray params = (NodeArray) call.arguments;
			assertEquals("call", method.name);
			assertEquals(3, params.elements.size());
		}
		{
			parser = new Parser("obj.call(1,a,);");
			block = parser.parse();
			NodeCall call = (NodeCall) block.statements.get(0);
			NodeAccess access = (NodeAccess) call.function;
			NodeVariable object = (NodeVariable) access.object;
			NodeVariable element = (NodeVariable) access.element;
			assertEquals("obj", object.name);
			assertEquals("call", element.name);
			NodeArray params = (NodeArray) call.arguments;
			assertEquals(3, params.elements.size());
			List<Node> args = params.elements;
			assertNull(args.get(2));
		}
		{
			parser = new Parser("obj.call(,);");
			block = parser.parse();
			NodeCall call = (NodeCall) block.statements.get(0);
			NodeAccess access = (NodeAccess) call.function;
			NodeVariable object = (NodeVariable) access.object;
			NodeVariable element = (NodeVariable) access.element;
			assertEquals("obj", object.name);
			assertEquals("call", element.name);
			NodeArray params = (NodeArray) call.arguments;
			assertEquals(2, params.elements.size());
			List<Node> args = params.elements;
			assertNull(args.get(0));
			assertNull(args.get(1));
		}
	}

	public void testIndexing() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("array.subarray[i][j]++;");
			block = parser.parse();
			NodeUnary inc = (NodeUnary) block.statements.get(0);
			assertEquals(Operator.INC_POST, inc.operator);
			NodeAccess chain1 = (NodeAccess) inc.expresion;
			assertEquals("j", ((NodeVariable) chain1.element).name);
			NodeAccess chain2 = (NodeAccess) chain1.object;
			assertEquals("i", ((NodeVariable) chain2.element).name);
			NodeAccess chain3 = (NodeAccess) chain2.object;
			assertEquals("subarray", ((NodeVariable) chain3.element).name);
			assertEquals("array", ((NodeVariable) chain3.object).name);
		}
		{
			parser = new Parser("object[5];");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.statements.get(0);
			NodeVariable object = (NodeVariable) access.object;
			NodeNumber element = (NodeNumber) access.element;
			assertEquals("object", object.name);
			assertEquals("5", element.value);
		}
		{
			parser = new Parser("object['test'];");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.statements.get(0);
			NodeVariable object = (NodeVariable) access.object;
			NodeString element = (NodeString) access.element;
			assertEquals("object", object.name);
			assertEquals("test", element.value);
		}
	}

	public void testChaining() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("object.'5';");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.statements.get(0);
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
			NodeAccess access = (NodeAccess) block.statements.get(0);
			NodeVariable object = (NodeVariable) access.object;
			NodeVariable element = (NodeVariable) access.element;
			assertEquals("object", object.name);
			assertEquals("element", element.name);
		}

		{
			parser = new Parser("object.element.subelement;");
			block = parser.parse();
			NodeAccess access = (NodeAccess) block.statements.get(0);
			NodeAccess accessParent = (NodeAccess) access.object;
			NodeVariable object = (NodeVariable) accessParent.object;
			NodeVariable element = (NodeVariable) accessParent.element;
			NodeVariable subelement = (NodeVariable) access.element;
			assertEquals("object", object.name);
			assertEquals("element", element.name);
			assertEquals("subelement", subelement.name);
		}

	}

	public void testNewObjects() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("new parent { ,  1:'value', };");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 1);
			assertTrue(objMap.get("1") instanceof NodeString);
			assertEquals(((NodeString) objMap.get("1")).value, "value");
		}
		{// clone
			parser = new Parser("new parent;");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {a:1,  1:'value', 'a b':5};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
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
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {,,,,,,,,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertEquals("parent", obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("new parent {arr:[1,2,3]};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
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
		NodeBlock block;
		{
			parser = new Parser("{a:1,  1:'value', 'a b':5};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
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
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 1);
			assertTrue(objMap.get("1") instanceof NodeString);
			assertEquals(((NodeString) objMap.get("1")).value, "value");
		}
		{
			parser = new Parser("{};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("{,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
		{
			parser = new Parser("{,,,,,,,,};");
			block = parser.parse();
			NodeObject obj = (NodeObject) block.statements.get(0);
			assertNull(obj.parent);
			Map<String, Node> objMap = obj.object;
			assertEquals(objMap.size(), 0);
		}
	}

	public void testArrays() {
		Parser parser;
		NodeBlock block;
		{
			parser = new Parser("[1,'2', \"3\"];");
			block = parser.parse();
			NodeArray arr = (NodeArray) block.statements.get(0);
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
			NodeArray arr = (NodeArray) block.statements.get(0);
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
			NodeArray arr = (NodeArray) block.statements.get(0);
			List<Node> list = arr.elements;
			assertEquals(list.size(), 0);
		}
	}
}
