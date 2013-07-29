package cx;

import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import cx.ast.Node;
import cx.runtime.ObjectHandler;

public class TestSudoku extends TestCase {

	public static void main(String[] args) throws Exception {
		// for profiling...
		System.out.println("press ENTER:");
		System.in.read();
		System.in.read();
		TestSudoku t = new TestSudoku();
		t.testSudoku();
		System.out.println("press ENTER:");
		System.in.read();
		System.in.read();
	}

	public void testSudoku() {
		{
			Context cx = new Context();
			int solution[] = new int[] { 2, 1, 7, 9, 3, 5, 4, 6, 8, 5, 8, 3, 4, 6, 1, 7, 2, 9, 9, 6, 4, 8, 7, 2, 5, 3,
					1, 3, 7, 2, 6, 5, 9, 8, 1, 4, 8, 4, 1, 3, 2, 7, 9, 5, 6, 6, 9, 5, 1, 4, 8, 2, 7, 3, 1, 2, 8, 7, 9,
					6, 3, 4, 5, 4, 5, 6, 2, 8, 3, 1, 9, 7, 7, 3, 9, 5, 1, 4, 6, 8, 2 };
			PrintHandler printHandler = new PrintHandler();
			cx.addHandler(printHandler);
			List<Node> block = (new Parser(new File("sudoku.cx"))).parse();
			long time = System.currentTimeMillis();
			cx.evaluate(block);
			System.out.println("time: " + (System.currentTimeMillis() - time));
			List<?> sugestion = (List<?>) cx.get("SUDOKU_ZERO");
			for (int i = 0; i < solution.length; i++) {
				assertEquals(solution[i], ((Number) sugestion.get(i)).intValue());
			}
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
	}
}
