package cx;

/**
 * All supported tokens are enumerated here. This class is also used to
 * recognize reserved words in a semi-optimal way.
 */
public enum Token {
	ERROR, EOF, EOL, NAME, NUMBER, STRING, L_PAREN, R_PAREN, COMMA, DOT, RETURN, EQ, NE, LT, LE, GT, GE, ADD, SUB, MUL, DIV, MOD, NOT, NEW, SEMICOLON, L_BRACKET, R_BRACKET, L_CURLY, R_CURLY, ASSIGN, ASSIGNOP, COLON, OR, AND, INCREMENT, DECREMENT, NULL, FALSE, TRUE, REGEXP, FUNCTION, IF, ELSE, SWITCH, CASE, DEFAULT, WHILE, DO, FOR, BREAK, CONTINUE, VAR, BIT_OR, BIT_AND, DELETE, QUESTION, BIT_XOR, BIT_LEFT, BIT_RIGHT, TRY, CATCH, FINALLY, THROW, COMPLEMENT, BIT_RIGHTU, NULL_VALUE, SQL_STRING_ESCAPE;

	static Token toKeyword(final StringBuilder strToken) {
		String keywordGuess = null;
		final int length = strToken.length();
		Token token = ERROR;
		char c;

		switch (length) {
			case 2:
				c = strToken.charAt(0);
				if ((c == 'i') && (strToken.charAt(1) == 'f')) {
					token = IF;
				} else if ((c == 'd') && (strToken.charAt(1) == 'o')) {
					token = DO;
				}
				return token;
			case 3:
				switch (c = strToken.charAt(0)) {
					case 'f':
						keywordGuess = "for";
						token = FOR;
						break;
					case 'n':
						keywordGuess = "new";
						token = NEW;
						break;
					case 't':
						keywordGuess = "try";
						token = TRY;
						break;
					case 'v':
						keywordGuess = "var";
						token = VAR;
				}
				break;
			case 4:
				switch (c = strToken.charAt(1)) {
					case 'a':
						keywordGuess = "case";
						token = CASE;
						break;
					case 'l':
						keywordGuess = "else";
						token = ELSE;
						break;
					case 'u':
						keywordGuess = "null";
						token = NULL;
						break;
					case 'r':
						keywordGuess = "true";
						token = TRUE;
						break;
				}
				break;
			case 5:
				switch (c = strToken.charAt(0)) {
					case 'b':
						keywordGuess = "break";
						token = BREAK;
						break;
					case 'c':
						keywordGuess = "catch";
						token = CATCH;
						break;
					case 'w':
						keywordGuess = "while";
						token = WHILE;
						break;
					case 'f':
						keywordGuess = "false";
						token = FALSE;
						break;
					case 't':
						keywordGuess = "throw";
						token = THROW;
				}
				break;
			case 6:
				switch (c = strToken.charAt(0)) {
					case 'r':
						keywordGuess = "return";
						token = RETURN;
						break;
					case 's':
						keywordGuess = "switch";
						token = SWITCH;
						break;
					case 'd':
						keywordGuess = "delete";
						token = DELETE;
				}
				break;
			case 7:
				switch (c = strToken.charAt(0)) {
					case 'd':
						keywordGuess = "default";
						token = DEFAULT;
						break;
					case 'f':
						keywordGuess = "finally";
						token = FINALLY;
				}
				break;
			case 8:
				switch (c = strToken.charAt(0)) {
					case 'c':
						keywordGuess = "continue";
						token = CONTINUE;
						break;
					case 'f':
						keywordGuess = "function";
						token = FUNCTION;
				}
				break;
		}
		if ((keywordGuess != null) && !compare(strToken, keywordGuess)) {
			token = ERROR;
		}
		return token;
	}

	static boolean compare(StringBuilder buffer, String str) {
		int i = str.length();
		if (i != buffer.length()) {
			return false;
		}
		for (i--; i >= 0; i--) {
			if (buffer.charAt(i) != str.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}