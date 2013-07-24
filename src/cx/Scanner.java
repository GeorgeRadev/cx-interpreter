package cx;

import java.util.LinkedList;
import java.util.List;
import cx.ast.SourcePosition;
import cx.exception.ParserException;

class Scanner {
	private boolean isDebug = false;
	private static final char ZERO = (char) 0;
	private static int READ_AHEAD_TOKENS = 10;
	private final char[] src;
	private final int srcLength;
	private int lineno;
	private int offset;
	private int lastlineidx;
	private int srcIdx;
	private String error;
	private StringBuilder strToken;
	private SourcePosition currentSourcePosition;
	private Token op;
	private List<Token> tokenStack = new LinkedList<Token>();
	private List<String> stringStack = new LinkedList<String>();
	private List<SourcePosition> positionStack = new LinkedList<SourcePosition>();

	Scanner(char[] paramArrayOfChar) {
		src = paramArrayOfChar;
		srcLength = src.length;
		srcIdx = 0;
		strToken = new StringBuilder(256);
		lineno = 1;
		error = null;
		currentSourcePosition = getSrcPosInternal();
	}

	Scanner(String paramArrayOfChar) {
		this(paramArrayOfChar.toCharArray());
	}

	Token getToken() {
		if (tokenStack.isEmpty()) {
			Token token = getTokenInternal();
			currentSourcePosition = getSrcPosInternal();
			if (isDebug) System.out.println("Token: " + token);
			return token;
		} else {
			strToken.setLength(0);
			strToken.append(stringStack.remove(0));
			currentSourcePosition = positionStack.remove(0);
			return tokenStack.remove(0);
		}
	}

	final Token peekToken() {
		return peekToken(1);
	}

	final Token peekToken(int n) {
		if (n > tokenStack.size()) {
			while (n + READ_AHEAD_TOKENS > tokenStack.size()) {
				Token token = getTokenInternal();
				if (isDebug) System.out.println("Token: " + token);
				tokenStack.add(token);
				stringStack.add(strToken.toString());
				positionStack.add(getSrcPosInternal());
				if (token == Token.EOF) break;
			}
		}
		return tokenStack.get(n - 1);
	}

	SourcePosition getSrcPos() {
		return currentSourcePosition;
	}

	protected SourcePosition getSrcPosInternal() {
		offset = (srcIdx - lastlineidx);
		SourcePosition localSourcePosition = new SourcePosition(srcIdx, lineno, offset);
		return localSourcePosition;
	}

	boolean matchToken(Token paramInt) {
		Token token = peekToken(1);
		if (token != paramInt) {
			return false;
		}
		getToken();
		return true;
	}

	protected Token getTokenInternal() {
		strToken.setLength(0);
		if (srcIdx >= srcLength) return Token.EOF;
		while (true) {
			char c = getChar();
			while (true) {
				if (srcIdx > srcLength) {
					strToken.append(ZERO);
					return Token.EOF;
				}
				if (isLineEnd(c)) {
					c = nextLine();
				}
				if (!isSpace(c)) {
					break;
				}
				c = getChar();
			}
			if (c == ZERO) {
				return Token.EOF;
			}
			boolean isIdentifier = Character.isJavaIdentifierStart(c);
			if (isIdentifier) {
				strToken.append(c);
				while (true) {
					c = getChar();
					if ((c == ZERO) || (!Character.isJavaIdentifierPart(c))) {
						break;
					}
					strToken.append(c);
				}
				srcIdx--;
				Token i = Token.toKeyword(strToken.toString());
				if (i != Token.EOF) {
					return (i != Token.ERROR) ? i : Token.NAME;
				}
			}
			char pc = peekChar();
			if (c == '0' && (pc == 'x' || pc == 'X')) {
				strToken.append(c);
				strToken.append(getChar());
				for (c = getChar(); (('0' <= c) && (c <= '9')) || (('a' <= c) && (c <= 'f'))
						|| (('A' <= c) && (c <= 'F')); c = getChar()) {
					strToken.append(c); 
				}
				srcIdx--;
				String value = strToken.toString();
				try {
					Long.parseLong(value.substring(2), 16);
				} catch (NumberFormatException localNumberFormatException) {
					error = "Invalid hex number: " + value;
					return Token.ERROR;
				}
				return Token.NUMBER;
			}
			if ((isDigit(c)) || ((c == '.') && (isDigit(peekChar())))) {
				strToken.append(c);
				for (c = getChar(); ('0' <= c) && (c <= '9'); c = getChar())
					strToken.append(c);
				if (c == '.' || c == 'e' || c == 'E') {
					int len = strToken.length() + 1;
					do {
						strToken.append(c);
						c = getChar();
					} while (isDigit(c));
					if (len >= strToken.length()) {
						error = "Invalid decimal format in number: " + strToken.toString();
						return Token.ERROR;
					}
				}
				srcIdx--;
				String value = strToken.toString();
				try {
					Double.parseDouble(value);
				} catch (NumberFormatException localNumberFormatException) {
					error = "Invalid number: " + value;
				}
				return Token.NUMBER;
			}
			if ((c == '"') || (c == '\'')) {
				char stringOpening = c;
				for (c = getChar(); c != stringOpening; c = getChar()) {
					if ((isLineEnd(c)) || (c == 0)) {
						srcIdx--;
						error = "Missing quote";
						return Token.ERROR;
					}
					if (c == '\\') {
						c = getChar();
						switch (c) {
							case 'n':
								c = 10;
								break;
							case 'r':
								c = 13;
								break;
							case 't':
								c = 13;
								break;
							default:
								srcIdx--;
						}
					}
					strToken.append(c);
				}
				return Token.STRING;
			}
			switch (c) {
				case ';':
					return Token.SEMICOLON;
				case '[':
					return Token.L_BRACKET;
				case ']':
					return Token.R_BRACKET;
				case '{':
					return Token.L_CURLY;
				case '}':
					return Token.R_CURLY;
				case '(':
					return Token.L_PAREN;
				case ')':
					return Token.R_PAREN;
				case ',':
					return Token.COMMA;
				case '.':
					return Token.DOT;
				case ':':
					return Token.COLON;
				case '?':
					return Token.QUESTION;
				case '^':
					if (matchChar('=')) {
						op = Token.BIT_XOR;
						return Token.ASSIGNOP;
					}
					return Token.BIT_XOR;
				case '|':
					if (matchChar('=')) {
						op = Token.BIT_OR;
						return Token.ASSIGNOP;
					}
					if (matchChar('|')) {
						if (matchChar('=')) {
							op = Token.OR;
							return Token.ASSIGNOP;
						}
						return Token.OR;
					}
					return Token.BIT_OR;
				case '&':
					if (matchChar('=')) {
						op = Token.BIT_AND;
						return Token.ASSIGNOP;
					}
					if (matchChar('&')) {
						if (matchChar('=')) {
							op = Token.AND;
							return Token.ASSIGNOP;
						}
						return Token.AND;
					}
					return Token.BIT_AND;
				case '=':
					if (matchChar('=')) {
						if (matchChar('=')) {
							error = "Syntax error";
							return Token.ERROR;
						}
						return Token.EQ;
					}
					return Token.ASSIGN;
				case '!':
					if (matchChar('=')) {
						if (matchChar('=')) {
							error = "Syntax error";
							return Token.ERROR;
						}
						return Token.NE;
					}
					return Token.NOT;
				case '<':
					if (matchChar('=')) {
						return Token.LE;
					}
					if (matchChar('<')) {
						if (matchChar('=')) {
							op = Token.BIT_LEFT;
							return Token.ASSIGNOP;
						}
						return Token.BIT_LEFT;
					}
					return Token.LT;
				case '>':
					if (matchChar('=')) {
						return Token.GE;
					}
					if (matchChar('>')) {
						if (matchChar('=')) {
							op = Token.BIT_RIGHT;
							return Token.ASSIGNOP;
						}
						return Token.BIT_RIGHT;
					}
					return Token.GT;
				case '*':
					if (matchChar('=')) {
						op = Token.MUL;
						return Token.ASSIGNOP;
					}
					return Token.MUL;
				case '/':
					if (matchChar('/')) {
						skipLine();
					} else {
						if (matchChar('*')) {
							do {
								c = getChar();
								if ((c == '*') && (matchChar('/'))) {
									break;
								}
							} while (c != ZERO);
						} else {
							if (matchChar('=')) {
								op = Token.DIV;
								return Token.ASSIGNOP;
							}
							return Token.DIV;
						}
					}
					break;
				case '%':
					if (matchChar('=')) {
						op = Token.MOD;
						return Token.ASSIGNOP;
					}
					return Token.MOD;
				case '+':
					if (matchChar('=')) {
						op = Token.ADD;
						return Token.ASSIGNOP;
					}
					if (matchChar('+')) {
						return Token.INCREMENT;
					}
					return Token.ADD;
				case '-':
					if (matchChar('=')) {
						op = Token.SUB;
						return Token.ASSIGNOP;
					}
					if (matchChar('-')) {
						return Token.DECREMENT;
					}
					return Token.SUB;
				default:
					error = "Unknown character";
					return Token.ERROR;
			}
		}
	}

	final int getLineno() {
		return lineno;
	}

	final Token getOperator() {
		return op;
	}

	String getString() {
		return strToken.toString();
	}

	String getErrorCode() {
		return error;
	}

	void handleError(String message, SourcePosition position) {
		throw new ParserException(message, position);
	}

	void setDebugMode(boolean paramBoolean) {
		isDebug = paramBoolean;
	}

	private final char getChar() {
		if (srcIdx >= srcLength) return ZERO;
		char c = src[srcIdx];
		srcIdx++;
		if ((c == '\n') && (lastlineidx < srcIdx)) {
			lineno++;
			lastlineidx = srcIdx;
		}
		return c;
	}

	private final char peekChar() {
		return (srcIdx >= srcLength) ? ZERO : src[srcIdx];
	}

	private final boolean matchChar(char paramChar) {
		char c = getChar();
		if (c == paramChar) {
			return true;
		}
		srcIdx--;
		return false;
	}

	private final boolean isSpace(char paramChar) {
		return (paramChar == ' ') || (paramChar == '\t');
	}

	private final boolean isDigit(char paramChar) {
		return ('0' <= paramChar) && (paramChar <= '9');
	}

	private final boolean isLineEnd(char paramChar) {
		return (paramChar == '\n') || (paramChar == '\r');
	}

	private final char nextLine() {
		char c = getChar();
		while (isLineEnd(c) && (c != ZERO)) {
			c = getChar();
		}
		return c;
	}

	private final void skipLine() {
		char c = getChar();
		while ((!isLineEnd(c)) && (c != ZERO)) {
			c = getChar();
		}
		srcIdx--;
	}
}