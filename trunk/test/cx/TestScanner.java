package cx;

import junit.framework.TestCase;

public class TestScanner extends TestCase {

	public void testScanner() {
		Scanner scanner;

		scanner = new Scanner("0xAC;");
		scanner.setDebugMode(true);
		assertEquals(Token.NUMBER, scanner.getToken());
		assertEquals("0xAC", scanner.getString());
		assertEquals(Token.SEMICOLON, scanner.getToken());
		assertEquals(Token.EOF, scanner.getToken());

		scanner = new Scanner("++p;");
		scanner.setDebugMode(true);
		assertEquals(Token.INCREMENT, scanner.getToken());
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.SEMICOLON, scanner.getToken());
		assertEquals(Token.EOF, scanner.getToken());

		scanner = new Scanner("/* comment */++p;");
		scanner.setDebugMode(true);
		assertEquals(Token.INCREMENT, scanner.getToken());
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.SEMICOLON, scanner.getToken());
		assertEquals(Token.EOF, scanner.getToken());

		scanner = new Scanner(" // comment \n ++p;");
		scanner.setDebugMode(true);
		assertEquals(Token.INCREMENT, scanner.peekToken());
		assertEquals(Token.INCREMENT, scanner.getToken());
		assertEquals(Token.NAME, scanner.peekToken());
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.SEMICOLON, scanner.peekToken());
		assertEquals(Token.SEMICOLON, scanner.getToken());
		assertEquals(Token.EOF, scanner.getToken());

		scanner = new Scanner(" // comment \n ++p;");
		scanner.setDebugMode(true);
		assertEquals(Token.INCREMENT, scanner.peekToken());
		assertEquals(Token.INCREMENT, scanner.peekToken());
		assertEquals(Token.INCREMENT, scanner.peekToken());
		assertEquals(Token.INCREMENT, scanner.getToken());
		assertEquals(Token.NAME, scanner.peekToken());
		assertEquals(Token.NAME, scanner.peekToken());
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.SEMICOLON, scanner.peekToken());
		assertEquals(Token.SEMICOLON, scanner.peekToken());
		assertEquals(Token.SEMICOLON, scanner.getToken());
		assertEquals(Token.EOF, scanner.getToken());

		scanner = new Scanner(" // comment \n ++p;");
		scanner.setDebugMode(true);
		assertEquals(Token.INCREMENT, scanner.peekToken(1));
		assertEquals(Token.NAME, scanner.peekToken(2));
		assertEquals(Token.SEMICOLON, scanner.peekToken(3));
		assertEquals(Token.EOF, scanner.peekToken(4));
		assertEquals(Token.EOF, scanner.peekToken(5));
		assertEquals(Token.EOF, scanner.peekToken(5));

		scanner = new Scanner("obj.5?^??;");
		scanner.setDebugMode(true);
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.NUMBER, scanner.getToken());
		assertEquals(Token.QUESTION, scanner.getToken());
		assertEquals(Token.BIT_XOR, scanner.getToken());
		assertEquals(Token.NULL_VALUE, scanner.getToken());

		scanner = new Scanner("0.0;");
		scanner.setDebugMode(true);
		assertEquals(Token.NUMBER, scanner.getToken());

		scanner = new Scanner("sql := select * from 'table';");
		scanner.setDebugMode(true);
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.SQL_STRING_ESCAPE, scanner.getToken());
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.MUL, scanner.getToken());
		assertEquals(Token.NAME, scanner.getToken());
		assertEquals(Token.STRING, scanner.getToken());
		assertEquals(Token.SEMICOLON, scanner.getToken());

		scanner = new Scanner(
				"if do for new \t var \r case else null \n true  break    while false return switch delete default continue function try catch \r finally throw");
		scanner.setDebugMode(true);
		assertEquals(Token.IF, scanner.getToken());
		assertEquals(Token.DO, scanner.getToken());
		assertEquals(Token.FOR, scanner.getToken());
		assertEquals(Token.NEW, scanner.getToken());
		assertEquals(Token.VAR, scanner.getToken());
		assertEquals(Token.CASE, scanner.getToken());
		assertEquals(Token.ELSE, scanner.getToken());
		assertEquals(Token.NULL, scanner.getToken());
		assertEquals(Token.TRUE, scanner.getToken());
		assertEquals(Token.BREAK, scanner.getToken());
		assertEquals(Token.WHILE, scanner.getToken());
		assertEquals(Token.FALSE, scanner.getToken());
		assertEquals(Token.RETURN, scanner.getToken());
		assertEquals(Token.SWITCH, scanner.getToken());
		assertEquals(Token.DELETE, scanner.getToken());
		assertEquals(Token.DEFAULT, scanner.getToken());
		assertEquals(Token.CONTINUE, scanner.getToken());
		assertEquals(Token.FUNCTION, scanner.getToken());
		assertEquals(Token.TRY, scanner.getToken());
		assertEquals(Token.CATCH, scanner.getToken());
		assertEquals(Token.FINALLY, scanner.getToken());
		assertEquals(Token.THROW, scanner.getToken());
	}

	public void testTokenCompare() {
		assertTrue(Token.compare(new StringBuilder(""), ""));
		assertTrue(Token.compare(new StringBuilder("test"), "test"));
		assertFalse(Token.compare(new StringBuilder("test"), "pest"));
		assertFalse(Token.compare(new StringBuilder("_test"), "_tesm"));
	}
}
