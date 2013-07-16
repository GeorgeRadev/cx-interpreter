package cx;

import java.util.List;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.ast.NodeBinary;
import cx.ast.NodeNumber;
import cx.ast.NodeString;
import cx.ast.NodeTernary;
import cx.ast.NodeUnary;
import cx.ast.NodeVariable;

public class TestParserExpressions extends TestCase {

	public void testTernaryExpressions() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("a?5:'str';");
			block = parser.parse();
			NodeTernary ternary = (NodeTernary) block.get(0);
			assertEquals("a", ((NodeVariable) ternary.condition).name);
			assertEquals(((NodeNumber) ternary.trueValue).value, "5");
			assertEquals(((NodeString) ternary.falseValue).value, "str");
		}
		{
			parser = new Parser("(a+b);");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.ADD);
			assertEquals("a", ((NodeVariable) binary.left).name);
			assertEquals("b", ((NodeVariable) binary.right).name);
		}
	}

	public void testBinaryExpressions() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("a^b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.BIT_XOR);
		}
		{
			parser = new Parser("a&b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.BIT_AND);
		}
		{
			parser = new Parser("a+b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.ADD);
			assertEquals("a", ((NodeVariable) binary.left).name);
			assertEquals("b", ((NodeVariable) binary.right).name);
		}
		{
			parser = new Parser("a-q;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.SUB);
			assertEquals("a", ((NodeVariable) binary.left).name);
			assertEquals("q", ((NodeVariable) binary.right).name);
		}
		{
			parser = new Parser("a*b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.MUL);
		}
		{
			parser = new Parser("a/b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.DIV);
		}
		{
			parser = new Parser("a%b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.MOD);
		}
		{
			parser = new Parser("a||b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.OR);
		}
		{
			parser = new Parser("a|b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.BIT_OR);
		}
		{
			parser = new Parser("a&&b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.AND);
		}
		{
			parser = new Parser("a<<b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.BIT_LEFT);
		}
		{
			parser = new Parser("a>>b;");
			block = parser.parse();
			NodeBinary binary = (NodeBinary) block.get(0);
			assertEquals(binary.operator, Operator.BIT_RIGHT);
		}
	}

	public void testUnaryExpressions() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("+p;");
			block = parser.parse();
			NodeVariable var = (NodeVariable) block.get(0);
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("+ + +p;");
			block = parser.parse();
			NodeVariable var = (NodeVariable) block.get(0);
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("-p;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.NEGATE);
			NodeVariable var = (NodeVariable) preInc.expresion;
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("!p;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.NOT);
			NodeVariable var = (NodeVariable) preInc.expresion;
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("++p;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.INC_PRE);
			NodeVariable var = (NodeVariable) preInc.expresion;
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("--p;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.DEC_PRE);
			NodeVariable var = (NodeVariable) preInc.expresion;
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("p++;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.INC_POST);
			NodeVariable var = (NodeVariable) preInc.expresion;
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("p--;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.DEC_POST);
			NodeVariable var = (NodeVariable) preInc.expresion;
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("++++p;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.INC_PRE);
			NodeUnary preInc2 = (NodeUnary) preInc.expresion;
			assertEquals(preInc2.operator, Operator.INC_PRE);
			NodeVariable var = (NodeVariable) preInc2.expresion;
			assertEquals(var.name, "p");
		}
		{
			parser = new Parser("++p++;");
			block = parser.parse();
			NodeUnary preInc = (NodeUnary) block.get(0);
			assertEquals(preInc.operator, Operator.INC_PRE);
			NodeUnary preInc2 = (NodeUnary) preInc.expresion;
			assertEquals(preInc2.operator, Operator.INC_POST);
			NodeVariable var = (NodeVariable) preInc2.expresion;
			assertEquals(var.name, "p");
		}
	}

	public void testCardinalExpressions() {
		Parser parser;
		List<Node> block;
		{
			parser = new Parser("0x123;");
			block = parser.parse();
			NodeNumber var = (NodeNumber) block.get(0);
			assertEquals(var.value, "0x123");
		}
		{
			parser = new Parser("42;");
			block = parser.parse();
			NodeNumber var = (NodeNumber) block.get(0);
			assertEquals(var.value, "42");
		}
		{
			parser = new Parser("'string';");
			block = parser.parse();
			NodeString var = (NodeString) block.get(0);
			assertEquals(var.value, "string");
		}
		{
			parser = new Parser("\"string\";");
			block = parser.parse();
			NodeString var = (NodeString) block.get(0);
			assertEquals(var.value, "string");
		}
		{
			parser = new Parser(";;;;;;;");
			block = parser.parse();
			assertEquals(0, block.size());
		}
	}

}
