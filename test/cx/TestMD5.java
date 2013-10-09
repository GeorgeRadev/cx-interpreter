package cx;

import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.runtime.BreakPoint;
import cx.runtime.ContextFrame;
import cx.runtime.StringHandler;

public class TestMD5 extends TestCase {
	public void testMD5() {
		BreakPoint myBreakPoint = new BreakPoint() {
			public void run(int line, ContextFrame cx) {
				return;
			}
		};
		Context cx = new Context();
		cx.addHandler(new StringHandler());
		cx.setBreakpoints(new int[] { 140, 141, 142, 143, 144 }, myBreakPoint);
		List<Node> block = (new Parser(new File("md5.cx"))).parse();
		cx.evaluate(block);
		cx.evaluate((new Parser("digest = md5('hello');")).parse());
		// assertEquals("5d41402abc4b2a76b9719d911017c592", cx.get("digest"));
	}
}
