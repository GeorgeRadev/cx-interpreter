package cx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import cx.ast.Node;
import cx.ast.NodeAccess;
import cx.ast.NodeArray;
import cx.ast.NodeAssign;
import cx.ast.NodeBinary;
import cx.ast.NodeBlock;
import cx.ast.NodeBreak;
import cx.ast.NodeCall;
import cx.ast.NodeContinue;
import cx.ast.NodeFalse;
import cx.ast.NodeFor;
import cx.ast.NodeFunction;
import cx.ast.NodeIf;
import cx.ast.NodeNumber;
import cx.ast.NodeObject;
import cx.ast.NodeReturn;
import cx.ast.NodeString;
import cx.ast.NodeSwitch;
import cx.ast.NodeTernary;
import cx.ast.NodeThrow;
import cx.ast.NodeTrue;
import cx.ast.NodeTry;
import cx.ast.NodeUnary;
import cx.ast.NodeVar;
import cx.ast.NodeVariable;
import cx.ast.NodeWhile;
import cx.ast.SourcePosition;
import cx.exception.ParserException;

public class Parser {
	boolean isDebug = false;
	private Scanner scanner;
	public boolean supportTryCatchThrow = false;

	public Parser(char[] paramArrayOfChar) {
		scanner = new Scanner(paramArrayOfChar);
	}

	public Parser(File parseFile) {
		try {
			long length = parseFile.length();
			if (length > Integer.MAX_VALUE) {
				throw new ParserException("File too big!");
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(parseFile), "UTF-8"),
					1026 * 32);
			char[] content = new char[(int) length];
			in.read(content);
			in.close();
			scanner = new Scanner(content);
		} catch (IOException e) {
			throw new ParserException("Error reading file!", e);
		}
	}

	public Parser(String paramArrayOfChar) {
		scanner = new Scanner(paramArrayOfChar.toCharArray());
	}

	public List<Node> parse() throws ParserException {
		final List<Node> statements = new ArrayList<Node>();
		try {
			scanner.setDebugMode(isDebug);

			while (true) {
				Token token = scanner.peekToken();
				if (token == Token.EOF || token == Token.ERROR) {
					break;
				}
				Node localNode;
				if (token == Token.FUNCTION) {
					scanner.getToken();
					localNode = parseFunction();
					// nameless functions are discarded
					if (((NodeFunction) localNode).name == null) {
						localNode = null;
					}
				} else {
					localNode = parseStatement();
				}
				if (localNode != null) {
					statements.add(localNode);
				}
			}

			if (scanner.getErrorCode() != null) handleError(scanner.getErrorCode(), scanner.getSrcPos());
			return statements;

		} catch (StackOverflowError localStackOverflowError) {
			handleError("Stack overflow", getSrcPos());
			localStackOverflowError.printStackTrace();
		} catch (ParserException localParserException) {
			handleError(localParserException.getMessage());
			localParserException.printStackTrace();
		} catch (Exception localException) {
			handleError(localException.getMessage());
			localException.printStackTrace();
		}
		return null;
	}

	Node handleError(String message, SourcePosition position) {
		scanner.handleError(message, position);
		return null;
	}

	void handleError(String message, SourcePosition position, Token paramInt) {
		System.out.println("Error token: " + paramInt);
		scanner.handleError(message, position);
	}

	void handleError(String paramString) {
		throw new ParserException(paramString);
	}

	public void setDebugMode(boolean paramBoolean) {
		isDebug = paramBoolean;
		scanner.setDebugMode(paramBoolean);
	}

	SourcePosition getSrcPos() {
		return scanner.getSrcPos();
	}

	private Node parseStatement() {
		Token token = scanner.peekToken();
		Node node = null;
		switch (token) {
			case IF:
				return parseIf();
			case FOR:
				return parseFor();
			case WHILE:
				return parseWhile();
			case DO:
				return parseDoWhile();
			case SWITCH:
				return parseSwitch();
			case TRY:
				if (supportTryCatchThrow) {
					return parseTry();
				} else {
					handleError("try...catch... throw not enabled!", getSrcPos());
				}
			case BREAK:
				scanner.getToken();
				node = new NodeBreak(getSrcPos());
				break;
			case CONTINUE:
				scanner.getToken();
				node = new NodeContinue(getSrcPos());
				break;
			case RETURN:
				node = parseReturn();
				break;
			case THROW:
				if (supportTryCatchThrow) {
					node = parseThrow();
				} else {
					handleError("try...catch... throw not enabled!", getSrcPos());
				}
				break;
			case VAR:
				node = parseVar(true);
				break;
			case L_CURLY:
				// test if the following is a Object declaration or a block
				// statement
				Token token1 = scanner.peekToken(2);
				if (token1 == Token.R_CURLY || token1 == Token.COMMA) {
					node = parseExpression();
					if (!scanner.matchToken(Token.SEMICOLON)) {
						handleError("Missing ';'", getSrcPos());
					}
					return node;
				}
				Token token2 = scanner.peekToken(3);
				if ((token1 == Token.STRING || token1 == Token.NAME || token1 == Token.NUMBER) && token2 == Token.COLON) {
					node = parseExpression();
					if (!scanner.matchToken(Token.SEMICOLON)) {
						handleError("Missing ';'", getSrcPos());
					}
					return node;
				}
				scanner.getToken();
				return parseBlock();
			case ERROR:
			case EOL:
			case SEMICOLON:
				scanner.getToken();
				return null;
			default:
				node = parseExpression();
		}
		if (!scanner.matchToken(Token.SEMICOLON)) {
			handleError("Missing ';'", getSrcPos());
		}
		return node;
	}

	private NodeBlock parseBlock() {
		if (isDebug) System.out.println("parseBlock()");
		NodeBlock localBlockNode = new NodeBlock(getSrcPos());
		Node localNode = null;
		Token i = scanner.peekToken();
		do {
			if (scanner.matchToken(Token.R_CURLY)) {
				break;
			}
			localNode = parseStatement();
			localBlockNode.add(localNode);
			i = scanner.peekToken();
		} while (i != Token.EOF);
		return localBlockNode;
	}

	private Node parseDoWhile() {
		if (isDebug) System.out.println("parseDoWhile()");
		SourcePosition localSourcePosition = getSrcPos();
		Node body = null;
		Node condition = null;
		scanner.getToken();// eat 'do'

		Token token = scanner.peekToken();

		if (token == Token.L_CURLY) {
			scanner.getToken();
			body = parseBlock();
		} else if (token == Token.WHILE) {
			body = null;
		} else {
			if (scanner.matchToken(Token.SEMICOLON)) {
				body = null;
			} else {
				body = parseStatement();
			}
		}

		if (!scanner.matchToken(Token.WHILE)) {
			handleError("expected 'while' !", scanner.getSrcPos());
		}
		if (!scanner.matchToken(Token.L_PAREN)) {
			handleError("expected '(' in 'while(...' !", scanner.getSrcPos());
		}
		if (scanner.peekToken() != Token.R_PAREN) {
			condition = parseExpression();
		}
		if (!scanner.matchToken(Token.R_PAREN)) {
			handleError("expected ')' in 'do...while(...)' !", scanner.getSrcPos());
		}
		if (!scanner.matchToken(Token.SEMICOLON)) {
			handleError("expected ';' for the 'do...while()' statement!", scanner.getSrcPos());
		}
		return new NodeWhile(localSourcePosition, condition, body, true);
	}

	private Node parseWhile() {
		if (isDebug) System.out.println("parseWhile()");
		SourcePosition localSourcePosition = getSrcPos();
		Node condition = null;
		Node body;
		scanner.getToken();// eat 'while'

		if (!scanner.matchToken(Token.L_PAREN)) {
			handleError("expected '(' in 'while(...' !", scanner.getSrcPos());
		}
		if (scanner.peekToken() != Token.R_PAREN) {
			condition = parseExpression();
		}
		if (!scanner.matchToken(Token.R_PAREN)) {
			handleError("expected ')' in 'while(...)' !", scanner.getSrcPos());
		}
		if (scanner.matchToken(Token.L_CURLY)) {
			body = parseBlock();
		} else {
			body = parseStatement();
		}
		return new NodeWhile(localSourcePosition, condition, body, false);
	}

	private Node parseSwitch() {
		if (isDebug) System.out.println("parseSwitch()");
		SourcePosition localSourcePosition = getSrcPos();
		Node switchValue = null;
		scanner.getToken();// eat 'switch'

		if (!scanner.matchToken(Token.L_PAREN)) {
			handleError("expected '(' in 'switch(...' !", scanner.getSrcPos());
		}
		if (scanner.peekToken() != Token.R_PAREN) {
			switchValue = parseExpression();
		} else {
			handleError("expected value for switch(...) !", scanner.getSrcPos());
		}
		if (!scanner.matchToken(Token.R_PAREN)) {
			handleError("expected ')' in 'switch(...)' !", scanner.getSrcPos());
		}
		if (!scanner.matchToken(Token.L_CURLY)) {
			handleError("expected '{' in 'switch(...){' !", scanner.getSrcPos());
		}
		// parse cases
		Token token;
		int defaultIndex = -1;
		final List<Object> caseValues = new ArrayList<Object>();
		final List<Integer> caseValueIndexes = new ArrayList<Integer>();
		final List<Node> caseStatements = new ArrayList<Node>();

		Object value = null;
		do {
			token = scanner.peekToken();
			if (token == Token.CASE || token == Token.DEFAULT) {
				// mark default
				scanner.getToken();
				if (token == Token.DEFAULT) {
					if (defaultIndex == -1) {
						defaultIndex = caseStatements.size();
					} else {
						handleError("only one default case for switch allowed !", scanner.getSrcPos());
					}
				} else {
					token = scanner.getToken();
					if (token == Token.NUMBER) {
						value = toNumber(scanner.getString());
					} else if (token == Token.STRING) {
						value = scanner.getString();
					} else {
						handleError("case value should be number or string!", scanner.getSrcPos());
					}
					caseValues.add(value);
					caseValueIndexes.add(caseStatements.size());
				}

				if (!scanner.matchToken(Token.COLON)) {
					handleError("expected ':' after case value!", scanner.getSrcPos());
				}

				token = scanner.peekToken();
				if (token == Token.CASE || token == Token.DEFAULT) {
					continue;
				}
				if (token == Token.R_CURLY) {
					break;
				}
			} else if (token == Token.R_CURLY) {
				break;
			}

			Node statement = parseStatement();
			caseStatements.add(statement);

		} while (token != Token.EOF && token != Token.ERROR);

		if (!scanner.matchToken(Token.R_CURLY)) {
			handleError("expected '}' in 'switch(...){...}' !", scanner.getSrcPos());
		}

		return new NodeSwitch(localSourcePosition, switchValue, defaultIndex, caseValues, caseValueIndexes,
				caseStatements);
	}

	private Node parseTry() {
		if (isDebug) System.out.println("parseTry");
		SourcePosition localSourcePosition = getSrcPos();
		List<Node> tryBody = null;
		List<Node> finallyBody = null;
		scanner.getToken();// eat 'try'

		Token token = scanner.peekToken();

		if (token == Token.L_CURLY) {
			scanner.getToken();
			tryBody = parseBlock().statements;
		} else {
			if (scanner.matchToken(Token.SEMICOLON)) {
				tryBody = null;
			} else {
				Node node = parseStatement();
				if (node != null) {
					tryBody = new ArrayList<Node>(1);
					tryBody.add(node);
				}
			}
		}

		final List<String> exceptionTypes = new ArrayList<String>();
		final List<String> exceptionVarNames = new ArrayList<String>();
		final List<Node> exceptionBodies = new ArrayList<Node>();

		boolean hasCatchFinally = false;
		do {
			if (scanner.matchToken(Token.CATCH)) {
				hasCatchFinally = true;
				if (!scanner.matchToken(Token.L_PAREN)) {
					handleError("expecting '(' after catch!", scanner.getSrcPos());
				}
				if (!scanner.matchToken(Token.NAME)) {
					handleError("expecting exception name in catch(...)!", scanner.getSrcPos());
				}
				String exceptionType = scanner.getString();
				if (!scanner.matchToken(Token.NAME)) {
					handleError("expecting variable after exception name in catch(Exception variable)!",
							scanner.getSrcPos());
				}
				String exceptionVar = scanner.getString();
				if (!scanner.matchToken(Token.R_PAREN)) {
					handleError("expecting variable after exception name in catch(Exception variable)!",
							scanner.getSrcPos());
				}
				token = scanner.peekToken();
				Node exceptionBody;
				if (token == Token.L_CURLY) {
					scanner.getToken();
					exceptionBody = parseBlock();
				} else {
					if (scanner.matchToken(Token.SEMICOLON)) {
						exceptionBody = null;
					} else {
						exceptionBody = parseStatement();
					}
				}
				exceptionTypes.add(exceptionType);
				exceptionVarNames.add(exceptionVar);
				exceptionBodies.add(exceptionBody);
				continue;

			} else if (scanner.matchToken(Token.FINALLY)) {
				hasCatchFinally = true;
				token = scanner.peekToken();
				if (token == Token.L_CURLY) {
					scanner.getToken();
					finallyBody = parseBlock().statements;
				} else {
					if (scanner.matchToken(Token.SEMICOLON)) {
						finallyBody = null;
					} else {
						Node node = parseStatement();
						if (node != null) {
							finallyBody = new ArrayList<Node>(1);
							finallyBody.add(node);
						}
					}
				}
				break;

			} else {
				break;
			}
		} while (true);

		if (!hasCatchFinally) {
			handleError("expecting 'catch' or 'finally' block after 'try'!", scanner.getSrcPos());
		}

		return new NodeTry(localSourcePosition, tryBody, exceptionTypes.toArray(new String[exceptionTypes.size()]),
				exceptionVarNames.toArray(new String[exceptionVarNames.size()]),
				exceptionBodies.toArray(new Node[exceptionBodies.size()]), finallyBody);
	}

	private Node parseFor() {
		if (isDebug) System.out.println("parseFor()");
		SourcePosition localSourcePosition = getSrcPos();
		NodeVar initialization = null;
		Node condition = null;
		List<Node> iterator = null;
		NodeVariable element = null;
		Node elements = null;
		List<Node> body = null;
		scanner.getToken();// eat 'for'

		if (!scanner.matchToken(Token.L_PAREN)) {
			handleError("expected '(' in 'for(...' !", scanner.getSrcPos());
		}
		boolean iteratingMode;
		{// check if it is "for(iter:array)" or "for(var iter:array)" syntax
			Token token1 = scanner.peekToken();
			Token token2 = scanner.peekToken(2);
			Token token3 = scanner.peekToken(3);
			iteratingMode = (token1 == Token.NAME && token2 == Token.COLON)
					|| (token1 == Token.VAR && token2 == Token.NAME && token3 == Token.COLON);
		}
		if (iteratingMode) {
			{// get variable name
				Token token = scanner.getToken();
				if (token == Token.VAR) {
					token = scanner.getToken();
				}
			}
			element = new NodeVariable(scanner.getSrcPos(), scanner.getString());
			scanner.getToken();// get :
			elements = parseExpression();
			if (!scanner.matchToken(Token.R_PAREN)) {
				handleError("expected ')' in 'for(i:object)' !", scanner.getSrcPos());
			}
		} else {
			Token token = scanner.peekToken();
			if (token == Token.VAR) {
				initialization = parseVar(true);
				if (!scanner.matchToken(Token.SEMICOLON)) {
					handleError("expected ';' in 'for(...;...' !", scanner.getSrcPos());
				}
			} else if (token == Token.SEMICOLON) {
				initialization = null;
				scanner.getToken();
			} else {
				initialization = parseVar(false);
				if (!scanner.matchToken(Token.SEMICOLON)) {
					handleError("expected ';' in 'for(...;...' !", scanner.getSrcPos());
				}
			}

			if (scanner.peekToken() != Token.SEMICOLON) {
				condition = parseExpression();
			}
			if (!scanner.matchToken(Token.SEMICOLON)) {
				handleError("expected second ';' in 'for(...;...;...' !", scanner.getSrcPos());
			}
			if (scanner.peekToken() != Token.R_PAREN) {
				iterator = parseArgumentList(Token.R_PAREN).elements;
			}
			if (!scanner.matchToken(Token.R_PAREN)) {
				handleError("expected ')' in 'for(;;)' !", scanner.getSrcPos());
			}
		}

		if (scanner.matchToken(Token.L_CURLY)) {
			body = parseBlock().statements;
		} else {
			Node node = parseStatement();
			if (node != null) {
				body = new ArrayList<Node>(1);
				body.add(node);
			}
		}
		return new NodeFor(localSourcePosition, initialization, (Node) condition, iterator, element, elements, body);
	}

	private Node parseIf() {
		if (isDebug) System.out.println("parseIf()");
		SourcePosition localSourcePosition = getSrcPos();
		scanner.getToken();// eat "if"

		if (scanner.getToken() == Token.L_PAREN) {
			Node condition = parseExpression();
			if (!scanner.matchToken(Token.R_PAREN)) {
				handleError("Missing ')'", scanner.getSrcPos());
			}

			List<Node> trueNode = null;
			if (scanner.matchToken(Token.L_CURLY)) {
				// parse block
				trueNode = parseBlock().statements;
			} else {
				Node node = parseStatement();
				if (node != null) {
					trueNode = new ArrayList<Node>(1);
					trueNode.add(node);
				}
			}

			List<Node> elseNode = null;
			if (scanner.matchToken(Token.ELSE)) {
				if (scanner.matchToken(Token.L_CURLY)) {
					// parse block
					elseNode = parseBlock().statements;
				} else {
					Node node = parseStatement();
					if (node != null) {
						elseNode = new ArrayList<Node>(1);
						elseNode.add(node);
					}
				}
			}
			return new NodeIf(localSourcePosition, condition, trueNode, elseNode);
		}
		return handleError("Missing '('", scanner.getSrcPos());
	}

	private NodeVar parseVar(boolean eatPrefix) {
		if (isDebug) System.out.println("parseVar()");
		if (eatPrefix) {
			// eat 'var'
			scanner.getToken();
		}
		NodeVar vars = new NodeVar(getSrcPos(), eatPrefix);

		Token current = scanner.peekToken();
		do {
			if (current != Token.NAME) {
				handleError("variable name after var expected!", scanner.getSrcPos());
			}
			Token ahead = scanner.peekToken(2);
			if (ahead == Token.ASSIGN) {
				// we have initialization value
				Node assign = parseAssignmentExpr();
				if (assign instanceof NodeAssign) {
					vars.addVar((NodeAssign) assign);
				} else {
					handleError("only assignment is allowed for setting a value to a var!", scanner.getSrcPos());
				}

			} else if (ahead == Token.SEMICOLON || ahead == Token.COMMA) {
				// we have just a name declaration
				scanner.getToken();// get name
				String varName = scanner.getString();
				Node value = null;
				vars.addVar(new NodeAssign(getSrcPos(), new NodeVariable(scanner.getSrcPos(), varName), value));

			} else {
				handleError("Uexpected token " + ahead, scanner.getSrcPos());
			}
			current = scanner.peekToken();
			if (current == Token.COMMA) {
				scanner.getToken();// eat comma
				current = scanner.peekToken();
			} else if (current == Token.SEMICOLON) {
				break;
			}
		} while (current != Token.EOF);
		return vars;
	}

	private Node parseExpression() {
		Node localNode = parseAssignmentExpr();
		return localNode;
	}

	private Node parseAssignmentExpr() {
		Node localObject = parseTernaryExpr();
		Token token = scanner.peekToken();
		if (token == Token.ASSIGN) {
			scanner.getToken();
			localObject = new NodeAssign(getSrcPos(), (Node) localObject, parseAssignmentExpr());
		} else if (token == Token.ASSIGNOP) {

			scanner.getToken();
			Operator operator = null;
			switch (scanner.getOperator()) {
				case ADD:
					operator = Operator.ADD;
					break;
				case SUB:
					operator = Operator.SUB;
					break;
				case MUL:
					operator = Operator.MUL;
					break;
				case DIV:
					operator = Operator.DIV;
					break;
				case MOD:
					operator = Operator.MOD;
					break;
				case BIT_LEFT:
					operator = Operator.BIT_LEFT;
					break;
				case BIT_RIGHT:
					operator = Operator.BIT_RIGHT;
					break;
				case BIT_RIGHTU:
					operator = Operator.BIT_RIGHTU;
					break;
				case BIT_OR:
					operator = Operator.BIT_OR;
					break;
				case BIT_AND:
					operator = Operator.BIT_AND;
					break;
				case AND:
					operator = Operator.AND;
					break;
				case OR:
					operator = Operator.OR;
					break;
				case BIT_XOR:
					operator = Operator.BIT_XOR;
					break;
				default:
					handleError("Unsupported assign operator", scanner.getSrcPos());
			}
			Node paramNode1 = (Node) localObject;
			Node paramNode2 = parseAssignmentExpr();
			localObject = new NodeAssign(getSrcPos(), paramNode1, new NodeBinary(getSrcPos(), paramNode1, operator,
					paramNode2));
		}
		return localObject;
	}

	private Node parseTernaryExpr() {
		Node condition = parseLogicalExpr();
		Token token = scanner.peekToken();
		if (token != Token.QUESTION) {
			return condition;
		} else {
			token = scanner.getToken();
			Node trueValue = parseExpression();
			token = scanner.getToken();
			if (token != Token.COLON) {
				handleError(": expected for ? operator!", scanner.getSrcPos());
			}
			Node falseValue = parseExpression();
			return new NodeTernary(getSrcPos(), condition, trueValue, falseValue);
		}
	}

	private Node parseLogicalExpr() {

		Node localObject = parseBitExpr();
		boolean more = false;
		do {
			Token token = scanner.peekToken();
			switch (token) {
				case OR:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.OR, parseBitExpr());
					more = true;
					break;
				case AND:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.AND, parseBitExpr());
					more = true;
					break;
				default:
					more = false;
			}
		} while (more);
		return localObject;
	}

	private Node parseBitExpr() {
		Node localObject = parseRelationalExpr();
		boolean more = false;
		do {
			Token token = scanner.peekToken();
			switch (token) {
				case BIT_OR:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.BIT_OR,
							parseRelationalExpr());
					more = true;
					break;
				case BIT_AND:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.BIT_AND,
							parseRelationalExpr());
					more = true;
					break;
				case BIT_XOR:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.BIT_XOR,
							parseRelationalExpr());
					more = true;
					break;
				default:
					more = false;
			}
		} while (more);
		return localObject;
	}

	private Node parseRelationalExpr() {
		Node localObject = parseShiftExpr();
		Token token = scanner.peekToken();
		switch (token) {
			case EQ:
				scanner.getToken();
				localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.EQ, parseShiftExpr());
				break;
			case NE:
				scanner.getToken();
				localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.NE, parseShiftExpr());
				break;
			case GE:
				scanner.getToken();
				localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.GE, parseShiftExpr());
				break;
			case GT:
				scanner.getToken();
				localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.GT, parseShiftExpr());
				break;
			case LE:
				scanner.getToken();
				localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.LE, parseShiftExpr());
				break;
			case LT:
				scanner.getToken();
				localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.LT, parseShiftExpr());
			default:
		}
		return localObject;
	}

	private Node parseShiftExpr() {
		Node localObject = parseBinaryExpr1();
		boolean more = false;
		do {
			Token token = scanner.peekToken();
			switch (token) {
				case BIT_LEFT:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.BIT_LEFT, parseBinaryExpr1());
					more = true;
					break;
				case BIT_RIGHT:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.BIT_RIGHT,
							parseBinaryExpr1());
					more = true;
					break;
				case BIT_RIGHTU:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.BIT_RIGHTU,
							parseBinaryExpr1());
					more = true;
					break;
				default:
					more = false;
			}
		} while (more);
		return localObject;
	}

	private Node parseBinaryExpr1() {
		Node localObject = parseBinaryExpr2();
		boolean more = false;
		do {
			Token token = scanner.peekToken();
			switch (token) {
				case ADD:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.ADD, parseBinaryExpr2());
					more = true;
					break;
				case SUB:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.SUB, parseBinaryExpr2());
					more = true;
					break;
				default:
					more = false;
			}
		} while (more);
		return localObject;
	}

	private Node parseBinaryExpr2() {
		Node localObject = parseUnaryExpr();
		boolean more = false;
		do {
			Token token = scanner.peekToken();
			switch (token) {
				case MUL:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.MUL, parseUnaryExpr());
					more = true;
					break;
				case DIV:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.DIV, parseUnaryExpr());
					more = true;
					break;
				case MOD:
					scanner.getToken();
					localObject = new NodeBinary(getSrcPos(), (Node) localObject, Operator.MOD, parseUnaryExpr());
					more = true;
					break;
				default:
					more = false;
			}
		} while (more);
		return localObject;
	}

	private Node parseUnaryExpr() {
		Node localObject = null;
		Token token = scanner.peekToken();
		switch (token) {
			case ADD:
				scanner.getToken();
				localObject = parseUnaryExpr();
				break;
			case SUB:
				scanner.getToken();
				localObject = new NodeUnary(getSrcPos(), Operator.NEGATE, parseUnaryExpr());
				break;
			case NOT:
				scanner.getToken();
				localObject = new NodeUnary(getSrcPos(), Operator.NOT, parseUnaryExpr());
				break;
			case INCREMENT:
				scanner.getToken();
				localObject = new NodeUnary(getSrcPos(), Operator.INC_PRE, parseUnaryExpr());
				break;
			case DECREMENT:
				scanner.getToken();
				localObject = new NodeUnary(getSrcPos(), Operator.DEC_PRE, parseUnaryExpr());
				break;
			case COMPLEMENT:
				scanner.getToken();
				localObject = new NodeUnary(getSrcPos(), Operator.COMPLEMENT, parseUnaryExpr());
				break;
			default:
				localObject = parseAccessExpr();
				token = scanner.peekToken();
				if (token == Token.INCREMENT) {
					scanner.getToken();
					localObject = new NodeUnary(getSrcPos(), Operator.INC_POST, (Node) localObject);
				} else if (token == Token.DECREMENT) {
					scanner.getToken();
					localObject = new NodeUnary(getSrcPos(), Operator.DEC_POST, (Node) localObject);
				}
				break;
		}
		return localObject;
	}

	private Node parseAccessExpr() {
		Node localNode = parsePrimaryExpr();
		Token token;

		do {
			token = scanner.peekToken();

			// look ahead for right-to-left elements
			if (token == Token.DOT) {
				// access element
				Node parentNode = localNode;
				token = scanner.getToken();
				localNode = parsePrimaryExpr();
				if (localNode instanceof NodeVariable) {
					// convert access name to string
					// to differ obj.element as obj["element"]
					// (not as obj[element] )
					localNode = new NodeString(localNode.position, ((NodeVariable) localNode).name);
				}
				Node access = new NodeAccess(getSrcPos(), parentNode, localNode);
				localNode = access;
				continue;
			}
			if (token == Token.L_BRACKET) {
				// access array
				Node parentNode = localNode;
				token = scanner.getToken();
				localNode = parseExpression();
				if (!scanner.matchToken(Token.R_BRACKET)) {
					handleError("Missing ']'", getSrcPos());
				}
				Node access = new NodeAccess(getSrcPos(), parentNode, localNode);
				localNode = access;
				continue;
			}
			if (token == Token.L_PAREN) {
				// call
				Node parentNode = localNode;
				token = scanner.getToken();
				NodeArray argiments = parseArgumentList(Token.R_PAREN);
				if (!scanner.matchToken(Token.R_PAREN)) {
					handleError("Missing ')'", getSrcPos());
				}
				Node call = new NodeCall(getSrcPos(), parentNode, argiments.elements);
				localNode = call;
				continue;
			}
			break;
		} while (true);
		return localNode;
	}

	private Node parsePrimaryExpr() {
		Token token = scanner.getToken();
		switch (token) {
			case STRING:
				return new NodeString(getSrcPos(), scanner.getString());
			case NUMBER:
				String number = scanner.getString();
				return new NodeNumber(getSrcPos(), number, toNumber(number));
			case NAME:
				return new NodeVariable(getSrcPos(), scanner.getString());
			case FUNCTION:
				return parseFunction();
			case L_PAREN: {
				Node localNode = parseExpression();
				if (!scanner.matchToken(Token.R_PAREN)) {
					handleError("Missing ')'", getSrcPos());
				}
				return localNode;
			}
			case NEW:
				return parseNew();
			case L_CURLY:
				NodeObject obj = new NodeObject(getSrcPos(), null);
				parseObject(obj);
				return obj;
			case L_BRACKET:
				return parseArray();
			case TRUE:
				return new NodeTrue(getSrcPos());
			case FALSE:
				return new NodeFalse(getSrcPos());
			default:
				handleError("unexpected token!", getSrcPos());
			case NULL:
		}
		return null;
	}

	private NodeFunction parseFunction() {
		if (isDebug) System.out.println("parseFunction()");
		SourcePosition position = scanner.getSrcPos();
		String functionName;
		NodeArray localListNode;
		NodeBlock localBlockNode;

		Token token = scanner.getToken();
		if (token == Token.NAME) {
			functionName = scanner.getString();
			token = scanner.getToken();
		} else {
			functionName = null;
		}
		if (token != Token.L_PAREN) {
			handleError("Missing function '('!", scanner.getSrcPos());
		}
		// get arguments
		localListNode = parseFormalArgumentList();
		if (!scanner.matchToken(Token.R_PAREN)) {
			handleError("Missing function(... ')'!", scanner.getSrcPos());
		}
		if (!scanner.matchToken(Token.L_CURLY)) {
			handleError("Missing function(...)'{'!", scanner.getSrcPos());
		}
		localBlockNode = parseBlock();

		NodeFunction localFunctionDeclNode = new NodeFunction(position, functionName, localListNode, localBlockNode);
		return localFunctionDeclNode;
	}

	private NodeObject parseNew() {
		Token token1 = scanner.peekToken();
		if (token1 == Token.L_CURLY) {
			NodeObject obj = new NodeObject(getSrcPos(), null);
			parseObject(obj);
			return obj;
		} else if (token1 == Token.NAME) {
			token1 = scanner.getToken();
			NodeObject obj = new NodeObject(getSrcPos(), scanner.getString());
			token1 = scanner.peekToken();
			if (token1 == Token.L_CURLY) {
				scanner.getToken();
				parseObject(obj);
			}
			return obj;
		}
		handleError("new expects parent object or object definition after!", getSrcPos());
		return null;
	}

	private NodeArray parseArgumentList(Token terminatingToken) {
		Token token;
		NodeArray result = new NodeArray(getSrcPos());
		Node element = null;
		do {
			token = scanner.peekToken();
			if (token == terminatingToken) {
				if (element != null) {
					result.add(element);
				}
				return result;
			}
			if (token == Token.COMMA) {
				token = scanner.getToken();
				result.add(element);
				element = null;
				token = scanner.peekToken();
				if (token == terminatingToken) {
					result.add(element);
				}
				continue;
			}
			element = parseExpression();

		} while (token != Token.EOF && token != Token.ERROR);
		handleError("Missing ')'", getSrcPos());
		return result;
	}

	private NodeArray parseFormalArgumentList() {
		Token token;
		NodeArray result = new NodeArray(getSrcPos());
		NodeVariable element = null;
		do {
			token = scanner.peekToken();
			if (token == Token.R_PAREN) {
				if (element != null) {
					result.add(element);
				}
				return result;

			} else if (token == Token.COMMA) {
				token = scanner.getToken();
				result.add(element);
				continue;

			} else if (token == Token.NAME) {
				token = scanner.getToken();
				element = new NodeVariable(getSrcPos(), scanner.getString());
			} else {
				handleError("syntax error: unexpected token: " + token, getSrcPos());
			}

		} while (token != Token.EOF && token != Token.ERROR);
		return (NodeArray) handleError("Missing ')'", getSrcPos());
	}

	private NodeReturn parseReturn() {
		scanner.getToken();// eat "return"
		Token token = scanner.peekToken();
		if (token == Token.SEMICOLON) {
			return new NodeReturn(getSrcPos(), null);
		}
		Node localNode = parseExpression();
		return new NodeReturn(getSrcPos(), localNode);
	}

	private NodeThrow parseThrow() {
		scanner.getToken();// eat "throw"
		Token token = scanner.peekToken();
		if (token == Token.SEMICOLON) {
			return new NodeThrow(getSrcPos(), null);
		}
		Node localNode = parseExpression();
		return new NodeThrow(getSrcPos(), localNode);
	}

	private NodeArray parseArray() {
		if (isDebug) System.out.println("parseArray()");
		// deals with [ expression, ...]
		NodeArray result = new NodeArray(getSrcPos());
		Token token = scanner.peekToken();
		if (token == Token.R_BRACKET) {
			token = scanner.getToken();
			return result;
		} else if (token == Token.COMMA) {
			result.add(null);
			token = scanner.getToken();
		}
		Node element = null;
		do {
			if (token == Token.R_BRACKET) {
				return result;
			}
			if (token == Token.COMMA) {
				token = scanner.peekToken();
				if (token == Token.R_BRACKET) {
					token = scanner.getToken();
					result.add(null);
					return result;
				}
			}
			element = parseExpression();
			result.add(element);
			token = scanner.getToken();

		} while (token != Token.EOF && token != Token.ERROR);
		handleError("Missing ']'", getSrcPos());
		return null;
	}

	private void parseObject(NodeObject result) {
		if (isDebug) System.out.println("parseObject()");
		// deals with: new parent { key:value, ...}
		Token token;
		Node element = null;
		do {
			token = scanner.peekToken();
			if (token == Token.R_CURLY) {
				token = scanner.getToken();
				return;
			} else if (token == Token.COMMA) {
				scanner.getToken();
				token = scanner.peekToken();
				if (token == Token.R_CURLY) {
					scanner.getToken();
					return;
				}
			} else if (token == Token.STRING || token == Token.NAME || token == Token.NUMBER) {
				token = scanner.getToken();
				String key = scanner.getString();
				if (!scanner.matchToken(Token.COLON)) {
					handleError("Syntax error: ':' expected!", getSrcPos());
				}
				element = parseExpression();
				result.put(key, element);
			} else {
				handleError("unexpected token: " + token, getSrcPos());
			}
		} while (token != Token.EOF && token != Token.ERROR);
		handleError("Missing '}'", getSrcPos());
	}

	public Number toNumber(String number) {
		try {
			return parseNumber(number);
		} catch (Exception e) {
			handleError(number + " is not a number!", getSrcPos());
			return null;
		}
	}

	public static Number parseNumber(String number) throws NumberFormatException {
		if (number.length() > 2 && number.charAt(0) == '0' && (number.charAt(1) == 'x' || number.charAt(1) == 'X')) {
			long l = Long.parseLong(number.substring(2), 16);
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
		} else if (number.indexOf('.') < 0 && number.indexOf('e') < 0 && number.indexOf('E') < 0) {
			long l = Long.parseLong(number, 10);
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
		} else {
			return Double.parseDouble(number);
		}
	}
}