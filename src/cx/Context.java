package cx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import cx.ast.NodeSQL;
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
import cx.ast.Visitor;
import cx.exception.CXException;
import cx.exception.JumpBreak;
import cx.exception.JumpContinue;
import cx.exception.JumpReturn;
import cx.runtime.BreakPoint;
import cx.runtime.ContextFrame;
import cx.runtime.Function;
import cx.runtime.Handler;

/**
 * This is the interpreter implementation (Visitor interface implementation)
 * that supports the documented logic at :
 * https://code.google.com/p/cx-interpreter/
 */
public class Context implements Visitor {
	private static final String ARGUMENTS = "arguments";

	private static final Long ZERO = 0L;
	private static final Long ONE = 1L;
	private static final Double ZEROD = 0.0d;
	private static final Double ONED = 1.0d;
	private static final String EMPTY_STRING = "";
	private int[] breakpoints = null;
	private BreakPoint breakPoint = null;

	private ContextFrame cx = null;
	public SourcePosition position = null;

	public final DateFormat sqlDateFormater;

	public Context() {
		this("yyyy-MM-dd HH:mm:ss");
	}

	public Context(String sqlDateFormat) {
		sqlDateFormater = new SimpleDateFormat(sqlDateFormat);
		cx = new ContextFrame();
	}

	public String toString() {
		return cx.toString();
	}

	private final List<Handler> handlers = new ArrayList<Handler>();
	private final Map<Object, Handler> handlersClasses = new HashMap<Object, Handler>();
	private final Map<String, Handler> handlersStaticCalls = new HashMap<String, Handler>();

	public void addHandler(Handler handler) {
		if (handler != null) {
			handlers.add(handler);
			handler.init(this);
			Object[] supportedClasses = (Object[]) handler.supportedClasses();
			if (supportedClasses != null) {
				for (Object clazz : supportedClasses) {
					handlersClasses.put(clazz, handler);
				}
			}
			String[] supportedStaticCalls = handler.supportedStaticCalls();
			if (supportedStaticCalls != null) {
				for (String staticCall : supportedStaticCalls) {
					handlersStaticCalls.put(staticCall, handler);
				}
			}
		}
	}

	public Object evaluate(List<Node> nodes) {
		try {
			for (Node node : nodes) {
				node.accept(this);
			}
		} catch (JumpBreak breakJump) {
			// finish block execution
		} catch (JumpContinue continueJump) {
			// finish execution
		}
		return cx.result;
	}

	private Object eval(Node paramNode) {
		if (paramNode != null) {
			paramNode.accept(this);
		} else {
			cx.result = null;
		}
		return cx.result;
	}

	private Object eval(List<Node> paramNode) {
		if (paramNode != null) {
			for (int i = 0, l = paramNode.size(); i < l; ++i) {
				eval(paramNode.get(i));
			}
		} else {
			cx.result = null;
		}
		return cx.result;
	}

	private Object eval(Node[] paramNode) {
		if (paramNode != null) {
			for (int i = 0, l = paramNode.length; i < l; ++i) {
				eval(paramNode[i]);
			}
		} else {
			cx.result = null;
		}
		return cx.result;
	}

	private void pushContext() {
		cx = new ContextFrame(cx);
	}

	private void popContext() {
		cx.parent.result = cx.result;
		cx = cx.parent;
	}

	public Object get(String varName) {
		return cx.get(varName);
	}

	public void set(String varName, Object value) {
		cx.put(varName, value);
	}

	public void setBreakpoints(int[] breakpointlines, BreakPoint breakPoint) {
		if (breakpointlines != null) {
			Arrays.sort(breakpointlines);
		}
		this.breakpoints = breakpointlines;
		this.breakPoint = breakPoint;
	}

	private void setCurrentPosition(SourcePosition position) {
		this.position = position;
		if (breakpoints != null && breakPoint != null) {
			if (Arrays.binarySearch(breakpoints, position.lineNo) >= 0) {
				breakPoint.run(position.lineNo, cx);
			}
		}
	}

	public void visitBlock(NodeBlock paramBlockNode) {
		// setCurrentPosition(paramBlockNode.position);
		try {
			pushContext();
			eval(paramBlockNode.statements);
		} finally {
			popContext();
		}
	}

	public void visitVar(NodeVar varNode) {
		// setCurrentPosition(varNode.position);
		// define variables in current context
		if (varNode.defineLocaly) {
			for (NodeAssign node : varNode.vars) {
				if (node.left instanceof NodeVariable) {
					cx._put(((NodeVariable) node.left).name, ZERO);
				}
			}
		}
		for (Node node : varNode.vars) {
			node.accept(this);
		}
	}

	public void visitNumber(NodeNumber numberNode) {
		// setCurrentPosition(numberNode.position);
		String value = numberNode.value;

		// try hex
		char hexChar;
		if (value.length() > 2 && value.charAt(0) == '0' && ((hexChar = value.charAt(1)) == 'x' || hexChar == 'X')) {
			try {
				long l = Long.parseLong(value.substring(2), 16);
				cx.result = l;
				return;
			} catch (Exception e) {}
		}
		// try long
		try {
			long l = Long.parseLong(value, 10);
			cx.result = l;
			return;
		} catch (Exception e) {}
		// try double
		try {
			cx.result = Double.valueOf(value);
			return;
		} catch (Exception e) {}
		cx.result = null;
	}

	public void visitVariable(NodeVariable variableNode) {
		// setCurrentPosition(variableNode.position);
		cx.result = cx.get(variableNode.name);
	}

	public void visitAssign(NodeAssign assignNode) {
		setCurrentPosition(assignNode.position);
		if (assignNode.left instanceof NodeVariable) {
			String varName = ((NodeVariable) assignNode.left).name;
			Object rhsObject = eval(assignNode.right);
			cx.put(varName, rhsObject);

		} else if (assignNode.left instanceof NodeAccess) {
			setNodeAccessValue((NodeAccess) assignNode.left, eval(assignNode.right));

		} else if (assignNode.left == null) {
			cx.result = null;
		}
	}

	public void visitBinary(NodeBinary binaryNode) {
		// setCurrentPosition(binaryNode.position);
		Object left = eval(binaryNode.left);
		Object right = eval(binaryNode.right);

		switch (binaryNode.operator) {
			case ADD:
				cx.result = add(left, right);
				return;
			case SUB:
				cx.result = subtract(left, right);
				return;
			case MUL:
				cx.result = multiply(left, right);
				return;
			case DIV:
				cx.result = divide(left, right);
				return;
			case MOD:
				cx.result = mod(left, right);
				return;
			case BIT_OR:
				cx.result = bitOR(left, right);
				return;
			case BIT_AND:
				cx.result = bitAND(left, right);
				return;
			case BIT_XOR:
				cx.result = bitXOR(left, right);
				return;
			case BIT_LEFT:
				cx.result = bitLEFT(left, right);
				return;
			case BIT_RIGHT:
				cx.result = bitRIGHT(left, right);
				return;
			case BIT_RIGHTU:
				cx.result = bitRIGHTU(left, right);
				return;
			case OR:
				if (isTrue(left)) {
					cx.result = Boolean.TRUE;
				} else if (isTrue(right)) {
					cx.result = Boolean.TRUE;
				} else {
					cx.result = Boolean.FALSE;
				}
				return;
			case AND:
				cx.result = (isTrue(left) && isTrue(right)) ? Boolean.TRUE : Boolean.FALSE;
				return;
			case EQ:
				cx.result = equalWith(left, right);
				return;
			case NE:
				cx.result = isTrue(equalWith(left, right)) ? Boolean.FALSE : Boolean.TRUE;
				return;
			case GT:
				cx.result = greaterThan(left, right);
				return;
			case GE:
				cx.result = greaterEqual(left, right);
				return;
			case LT:
				cx.result = lowerThan(left, right);
				;
				return;
			case LE:
				cx.result = lowerEqual(left, right);
				return;
			default:
		}
		cx.result = null;
	}

	public void visitSwitch(NodeSwitch switchNode) {
		setCurrentPosition(switchNode.position);
		Object value = eval(switchNode.value);

		int executeIndex = switchNode.defaultIndex;
		if (value != null) {
			// find value that matches
			for (int i = 0, l = switchNode.caseValues.length; i < l; i++) {
				Object caseValue = switchNode.caseValues[i];
				if (caseValue != null) {
					if ((value instanceof Double && caseValue instanceof Double && value.equals(caseValue))
							|| (value instanceof Number && caseValue instanceof Number && ((Number) value).longValue() == ((Number) caseValue).longValue())
							|| (value.toString().equals(caseValue.toString()))) {
						executeIndex = i;
						break;
					}
				}
			}
		}
		if (executeIndex >= 0) {
			final Node[] statements = switchNode.caseStatements;
			int i = executeIndex, l = statements.length;
			if (executeIndex < l) {
				try {
					for (; i < l; i++) {
						eval(statements[i]);
					}
				} catch (JumpBreak localBreakJump) {
					// break
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void visitFor(NodeFor paramForNode) {
		setCurrentPosition(paramForNode.position);
		try {
			pushContext();
			if (paramForNode.elements != null) {
				// for ( element[NodeVariable] : elements)
				final String varName = paramForNode.element.name;
				final Object elements = eval(paramForNode.elements);
				if (elements instanceof List) {
					final List list = (List) elements;

					for (Object e : list) {
						cx.put(varName, e);
						try {
							eval(paramForNode.body);
						} catch (JumpBreak localBreakJump) {
							// break
							break;
						} catch (JumpContinue localContinueJump) {
							// continue
						}
					}
				} else if (elements instanceof Map) {
					final Map map = (Map) elements;

					for (Object e : map.keySet()) {
						cx.put(varName, e);
						try {
							eval(paramForNode.body);
						} catch (JumpBreak localBreakJump) {
							// break
							break;
						} catch (JumpContinue localContinueJump) {
							// continue
						}
					}
				}
			} else {
				eval(paramForNode.initialization);
				Object condition = eval(paramForNode.condition);
				if (isTrue(condition)) {
					do {
						try {
							eval(paramForNode.body);
						} catch (JumpBreak localBreakJump) {
							// break
							break;
						} catch (JumpContinue localContinueJump) {
							// continue
						}
						eval(paramForNode.iterator);
						condition = eval(paramForNode.condition);
					} while (isTrue(condition));
				}
			}
		} finally {
			popContext();
		}
	}

	public void visitWhile(NodeWhile paramWhileNode) {
		setCurrentPosition(paramWhileNode.position);
		Node condition = paramWhileNode.condition;
		try {
			pushContext();

			if (paramWhileNode.isDoWhile) {
				do {
					try {
						eval(paramWhileNode.body);
					} catch (JumpBreak localBreakJump) {
						// break
						break;
					} catch (JumpContinue localContinueJump) {
						// continue
						continue;
					}
				} while (condition == null || isTrue(eval(condition)));

			} else {
				while (condition == null || isTrue(eval(condition))) {
					try {
						eval(paramWhileNode.body);
					} catch (JumpBreak localBreakJump) {
						// break
						break;
					} catch (JumpContinue localContinueJump) {
						// continue
						continue;
					}
				}
			}

		} finally {
			popContext();
		}
	}

	public void visitIf(NodeIf paramIfNode) {
		setCurrentPosition(paramIfNode.position);
		try {
			pushContext();
			Object condition = eval(paramIfNode.condition);

			if (isTrue(condition)) {
				if (paramIfNode.body != null) {
					eval(paramIfNode.body);
				}
			} else {
				if (paramIfNode.elseBody != null) {
					eval(paramIfNode.elseBody);
				}
			}

		} finally {
			popContext();
		}
	}

	public void visitTernary(NodeTernary node) {
		setCurrentPosition(node.position);

		Object condition = eval(node.condition);

		if (isTrue(condition)) {
			eval(node.trueValue);
		} else {
			eval(node.falseValue);
		}
	}

	public void visitReturn(NodeReturn paramReturnNode) {
		setCurrentPosition(paramReturnNode.position);
		cx.result = eval(paramReturnNode.expression);
		throw new JumpReturn(cx.result);
	}

	public void visitBreak(NodeBreak paramBreakNode) {
		if (paramBreakNode.condition == null || isTrue(eval(paramBreakNode.condition))) {
			throw new JumpBreak();
		}
	}

	public void visitContinue(NodeContinue paramContinueNode) {
		if (paramContinueNode.condition == null || isTrue(eval(paramContinueNode.condition))) {
			throw new JumpContinue();
		}
	}

	public void visitTrue(NodeTrue paramTrueNode) {
		cx.result = Boolean.TRUE;
	}

	public void visitFalse(NodeFalse paramFalseNode) {
		cx.result = Boolean.FALSE;
	}

	public void visitString(NodeString paramStringNode) {
		cx.result = paramStringNode.value;
	}

	private Number increment(Number number) {
		if (number instanceof Double) {
			return new Double(number.doubleValue() + 1.0d);
		}
		return Long.valueOf(number.longValue() + 1L);
	}

	private Number decrement(Number number) {
		if (number instanceof Double) {
			return new Double(number.doubleValue() - 1.0d);
		}
		return Long.valueOf(number.longValue() - 1L);
	}

	private Number negate(Number number) {
		if (number instanceof Double) {
			return new Double(number.doubleValue() * -1.0d);
		}
		return Long.valueOf(number.longValue() * -1L);
	}

	private Number absolute(Number number) {
		if (number instanceof Double) {
			double d = number.doubleValue();
			if (d < 0) {
				return new Double(-d);
			} else {
				return number;
			}
		}
		long l = number.longValue();
		if (l < 0) {
			return Long.valueOf(-l);
		} else {
			return number;
		}
	}

	private Number complement(Number number) {
		return Long.valueOf(~number.longValue());
	}

	public void visitUnary(NodeUnary unary) {
		// setCurrentPosition(unary.position);
		Object result = eval(unary.expresion);
		switch (unary.operator) {
			case NOT:
				cx.result = isTrue(result) ? Boolean.FALSE : Boolean.TRUE;
				return;
			case INC_PRE:
				if ((result instanceof Number)) {
					Object newValue = increment((Number) result);
					if ((unary.expresion instanceof NodeVariable)) {
						cx.put(((NodeVariable) unary.expresion).name, newValue);
					} else if (unary.expresion instanceof NodeAccess) {
						setNodeAccessValue((NodeAccess) unary.expresion, newValue);
					}
					cx.result = newValue;
				} else {
					Long l = toLong(result);
					if (l == null) {
						double d = toDouble(result);
						if (d != d) {
							cx.result = null;
						} else {
							cx.result = d + 1;
						}
					} else {
						cx.result = ++l;
					}
				}
				return;
			case INC_POST:
				if ((result instanceof Number)) {
					Object newValue = increment((Number) result);
					if (unary.expresion instanceof NodeVariable) {
						cx.put(((NodeVariable) unary.expresion).name, newValue);
					} else if (unary.expresion instanceof NodeAccess) {
						setNodeAccessValue((NodeAccess) unary.expresion, newValue);
					}
					cx.result = result;
				}
				return;
			case DEC_PRE:
				if ((result instanceof Number)) {
					Object newValue = decrement((Number) result);
					if ((unary.expresion instanceof NodeVariable)) {
						cx.put(((NodeVariable) unary.expresion).name, newValue);
					} else if (unary.expresion instanceof NodeAccess) {
						setNodeAccessValue((NodeAccess) unary.expresion, newValue);
					}
					cx.result = newValue;
				} else {
					Long l = toLong(result);
					if (l == null) {
						double d = toDouble(result);
						if (d != d) {
							cx.result = null;
						} else {
							cx.result = d - 1;
						}
					} else {
						cx.result = --l;
					}
				}
				return;
			case DEC_POST:
				if ((result instanceof Number)) {
					Object newValue = decrement((Number) result);
					if ((unary.expresion instanceof NodeVariable)) {
						cx.put(((NodeVariable) unary.expresion).name, newValue);
					} else if (unary.expresion instanceof NodeAccess) {
						setNodeAccessValue((NodeAccess) unary.expresion, newValue);
					}
					cx.result = result;
				}
				return;
			case NEGATE:
				if ((result instanceof Number)) {
					cx.result = negate((Number) result);
				} else {
					Long l = toLong(result);
					if (l == null) {
						double d = toDouble(result);
						if (d != d) {
							cx.result = null;
						} else {
							cx.result = -d;
						}
					} else {
						cx.result = -l;
					}
				}
				return;
			case ABSOLUTE:
				if ((result instanceof Number)) {
					cx.result = absolute((Number) result);
				} else {
					Long l = toLong(result);
					if (l == null) {
						cx.result = absolute(toDouble(result));
					} else {
						cx.result = absolute(l);
					}
				}
				return;
			case COMPLEMENT:
				if (result != null) {
					if ((result instanceof Number)) {
						cx.result = complement((Number) result);
					} else {
						double d = toDouble(result);
						if (d != d) {
							cx.result = null;
						} else {
							cx.result = ~((long) d);
						}
					}
				}
				return;
			default:
		}
	}

	@SuppressWarnings("rawtypes")
	public static boolean isTrue(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof Boolean) {
			return (Boolean) obj;
		} else if (obj instanceof String) {
			return obj.toString().length() > 0;
		} else if (obj instanceof Number) {
			return ((Number) obj).doubleValue() != 0D;
		} else if (obj instanceof List) {
			return ((List) obj).size() > 0;
		} else if (obj instanceof Map) {
			return ((Map) obj).size() > 0;
		}
		return false;
	}

	public static Long toLong(Object obj) {
		if (obj == null) {
			return ZERO;
		} else if (obj instanceof Number) {
			return ((Number) obj).longValue();
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj) ? ONE : ZERO;
		} else if (obj instanceof String) {
			if (((String) obj).length() == 0) {
				return ZERO;
			} else {
				try {
					return Long.parseLong((String) obj);
				} catch (NumberFormatException e) {
					return null;
				}
			}
		} else {
			try {
				return Long.parseLong(obj.toString());
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}

	public static double toDouble(Object obj) {
		if (obj == null) {
			return ZEROD;
		} else if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		} else if (obj instanceof String) {
			if (((String) obj).length() == 0) {
				return ZEROD;
			} else {
				try {
					return Parser.parseNumber((String) obj).doubleValue();
				} catch (NumberFormatException e) {
					return Double.NaN;
				}
			}
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj) ? ONED : ZEROD;
		} else {
			try {
				return Parser.parseNumber(obj.toString()).doubleValue();
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		}
	}

	public static Number toNumber(Object obj) {
		if (obj == null) {
			return ZERO;
		} else if (obj instanceof Double) {
			return ((Number) obj).doubleValue();
		} else if (obj instanceof Number) {
			return ((Number) obj).longValue();
		} else if (obj instanceof String) {
			if (((String) obj).length() == 0) {
				return ZERO;
			} else {
				try {
					return Long.parseLong((String) obj);
				} catch (NumberFormatException e) {
					try {
						return Parser.parseNumber((String) obj).doubleValue();
					} catch (NumberFormatException ex) {}
				}
			}
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj) ? ONE : ZERO;
		} else {
			final String str = obj.toString();
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				try {
					return Parser.parseNumber(str).doubleValue();
				} catch (NumberFormatException ex) {}
			}
		}
		return Double.NaN;
	}

	@SuppressWarnings("rawtypes")
	public static String toString(Object obj) {
		if (obj == null) {
			return EMPTY_STRING;
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj) ? "true" : EMPTY_STRING;
		} else if (obj instanceof Number) {
			return ((Number) obj).doubleValue() == 0d ? "0" : obj.toString();
		} else if (obj instanceof String) {
			if (((String) obj).length() == 0) {
				return EMPTY_STRING;
			} else {
				return (String) obj;
			}
		} else if (obj instanceof List) {
			if (((List) obj).size() == 0) {
				return "[]";
			} else {
				StringBuilder buffer = new StringBuilder(1024);
				buffer.append('[');
				for (Object element : (List) obj) {

					if (element == null) {
						buffer.append("null");
					} else if (element instanceof Number) {
						buffer.append(element.toString());
					} else {
						escapeString(buffer, element.toString());
					}
					buffer.append(',');
				}
				buffer.setCharAt(buffer.length() - 1, ']');
				return buffer.toString();
			}
		}
		return obj.toString();
	}

	/**
	 * null = false = 0 = 0.0 = "". cast to the left when comparing.
	 */
	private Object equalWith(Object left, Object right) {
		if (left == null && !isTrue(right)) {
			return Boolean.TRUE;

		} else if (left instanceof Boolean) {
			return isTrue(left) == isTrue(right);

		} else if (left instanceof Double) {
			double l = toDouble(left);
			double r = toDouble(right);
			return l == r;

		} else if (left instanceof Number) {
			Long l = toLong(left);
			Long r = toLong(right);
			return r != null && r.compareTo(l) == 0;

		} else if (left instanceof String) {
			String l = toString(left);
			if (l.length() <= 0) {
				Long r = toLong(right);
				return r == null || r.longValue() == 0L;
			} else {
				return l.compareTo(toString(right)) == 0;
			}
		}
		return Boolean.FALSE;
	}

	private Object greaterThan(Object left, Object right) {
		if (left == null && !isTrue(right)) {
			return Boolean.FALSE;

		} else if (left instanceof Boolean) {
			return isTrue(left) && !isTrue(right);

		} else if (left instanceof Double) {
			double l = toDouble(left);
			double r = toDouble(right);
			return l > r;

		} else if (left instanceof Number) {
			Long l = toLong(left);
			Long r = toLong(right);
			return r == null || r.compareTo(l) < 0;

		} else if (left instanceof String) {
			String l = toString(left);
			if (l.length() <= 0) {
				return Boolean.FALSE;
			} else {
				return l.compareTo(toString(right)) > 0;
			}
		}
		return Boolean.FALSE;
	}

	private Object lowerThan(Object left, Object right) {
		if (left == null && isTrue(right)) {
			return Boolean.TRUE;

		} else if (left instanceof Boolean) {
			return !isTrue(left) && isTrue(right);

		} else if (left instanceof Double) {
			double l = toDouble(left);
			double r = toDouble(right);
			return l < r;

		} else if (left instanceof Number) {
			Long l = toLong(left);
			Long r = toLong(right);
			return r == null || r.compareTo(l) > 0;

		} else if (left instanceof String) {

			String l = toString(left);
			if (l.length() <= 0) {
				return toLong(right) != ZERO;
			} else {
				return l.compareTo(toString(right)) < 0;
			}
		}
		return Boolean.FALSE;
	}

	private Object greaterEqual(Object left, Object right) {
		if (left == null && !isTrue(right)) {
			return Boolean.TRUE;

		} else if (left instanceof Boolean) {
			return isTrue(left) || !isTrue(right);

		} else if (left instanceof Double) {
			double l = toDouble(left);
			double r = toDouble(right);
			return l >= r;

		} else if (left instanceof Number) {
			Long l = toLong(left);
			Long r = toLong(right);
			return r == null || r.compareTo(l) <= 0;

		} else if (left instanceof String) {
			String l = toString(left);
			if (l.length() <= 0) {
				Long r = toLong(right);
				return r == null || r.longValue() == 0L;
			} else {
				return l.compareTo(toString(right)) >= 0;
			}
		}
		return Boolean.FALSE;
	}

	private Object lowerEqual(Object left, Object right) {
		if (left == null) {
			return Boolean.TRUE;

		} else if (left instanceof Boolean) {
			return isTrue(right) || !isTrue(left);

		} else if (left instanceof Double) {
			double l = toDouble(left);
			double r = toDouble(right);
			return l <= r;

		} else if (left instanceof Number) {
			Long l = toLong(left);
			Long r = toLong(right);
			return r == null || r.compareTo(l) >= 0;

		} else if (left instanceof String) {
			String l = toString(left);
			if (l.length() <= 0) {
				return Boolean.TRUE;
			} else {
				return toString(left).compareTo(toString(right)) <= 0;
			}
		}
		return Boolean.FALSE;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object add(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number) {
			Number rvalue = toNumber(right);
			if (left instanceof Double || rvalue instanceof Double) {
				double d = ((Number) left).doubleValue() + ((Number) rvalue).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() + ((Number) rvalue).longValue();
				return Long.valueOf(l);
			}
		} else if (left instanceof String) {
			String rvalue = toString(right);
			if (rvalue != null) {
				return (String) left + rvalue;
			} else {
				return left;
			}
		} else if (left instanceof List) {
			((List) left).add(right);
			return left;
		} else if (left instanceof Boolean) {
			return ((Boolean) left).booleanValue() ^ isTrue(right);
		}
		return null;
	}

	private Object subtract(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number) {
			Number rvalue = toNumber(right);
			if (left instanceof Double || rvalue instanceof Double) {
				double d = ((Number) left).doubleValue() - ((Number) rvalue).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() - ((Number) rvalue).longValue();
				return Long.valueOf(l);
			}
		} else if (left instanceof Boolean) {
			return ((Boolean) left).booleanValue() ^ isTrue(right);
		}
		return null;
	}

	private Object multiply(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number) {
			Number rvalue = toNumber(right);
			if (left instanceof Double || rvalue instanceof Double) {
				double d = ((Number) left).doubleValue() * ((Number) rvalue).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() * ((Number) rvalue).longValue();
				return Long.valueOf(l);
			}
		} else if (left instanceof Boolean) {
			return ((Boolean) left).booleanValue() && isTrue(right);
		}
		return null;
	}

	private Object divide(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number) {
			Number rvalue = toNumber(right);
			double d = ((Number) left).doubleValue() / ((Number) rvalue).doubleValue();
			return new Double(d);
		} else if (left instanceof Boolean) {
			return ((Boolean) left).booleanValue() || isTrue(right);
		}
		return null;
	}

	private Object mod(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			long l = ((Number) left).longValue() % ((Number) right).longValue();
			return Long.valueOf(l);
		}
		return null;
	}

	private Object bitOR(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			long l = ((Number) left).longValue() | ((Number) right).longValue();
			return Long.valueOf(l);
		}
		return null;
	}

	private Object bitAND(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			long l = ((Number) left).longValue() & ((Number) right).longValue();
			return Long.valueOf(l);
		}
		return null;
	}

	private Object bitXOR(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			long l = ((Number) left).longValue() ^ ((Number) right).longValue();
			return Long.valueOf(l);
		}
		return null;
	}

	private Object bitLEFT(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			long l = ((Number) left).longValue() << ((Number) right).longValue();
			return Long.valueOf(l);
		}
		return null;
	}

	private Object bitRIGHT(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			long l = ((Number) left).longValue() >> ((Number) right).longValue();
			return Long.valueOf(l);
		}
		return null;
	}

	private Object bitRIGHTU(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			long l = ((Number) left).longValue() >>> ((Number) right).longValue();
			return Long.valueOf(l);
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void visitArray(NodeArray arr) {
		// setCurrentPosition(arr.position);
		int len = arr.elements.size();
		List result = new ArrayList(len);
		for (Node node : arr.elements) {
			result.add(eval(node));
		}
		cx.result = result;
	}

	public void visitObject(NodeObject object) {
		// setCurrentPosition(object.position);
		String parentObjectName = object.parent;
		final ContextFrame newObject;
		Object parent;
		if (parentObjectName != null && parentObjectName.length() > 0 && (parent = cx.get(parentObjectName)) != null
				&& parent instanceof ContextFrame) {
			newObject = new ContextFrame((ContextFrame) parent);
			// flatten parent variables into current object context
			ContextFrame.flattenAintoB(newObject.parent, newObject);
			// replace all functions with new ones that has the same this to
			// the new object
			for (Entry<String, Object> entry : newObject.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof Function) {
					Function function = (Function) value;
					Function newFunction = new Function(newObject, function.body);
					newObject._put(entry.getKey(), newFunction);
				}
			}
		} else {
			newObject = new ContextFrame();
		}
		ContextFrame current = cx;
		cx = newObject;
		// add object elements
		for (String key : object.object.keySet()) {
			newObject.put(key, eval(object.object.get(key)));
		}
		cx = current;
		cx.result = newObject;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setNodeAccessValue(NodeAccess access, Object value) {
		final Object obj = eval(access.object);
		final Node element = access.element;

		if (obj instanceof ContextFrame) {
			String _element;
			if (element instanceof NodeVariable) {
				_element = ((NodeVariable) element).name;
			} else {
				_element = eval(element).toString();
			}
			((ContextFrame) obj).put(_element, value);

		} else if (obj instanceof List) {
			final List list = ((List) obj);
			try {
				int ix;
				Object _element = eval(element);
				if (_element == null) {
					if (element instanceof NodeVariable) {
						_element = ((NodeVariable) element).name;
					} else {
						cx.result = null;
						return;
					}
				}
				if (_element instanceof Number) {
					ix = ((Number) _element).intValue();
				} else {
					ix = Integer.parseInt(element.toString());
				}
				if (ix < 0) {
					return;
				}

				while (ix >= list.size()) {
					list.add(null);
				}
				list.set(ix, value);
			} catch (Exception e) {
				// not an integer index for addressing list
			}
			return;

		} else if (obj instanceof String) {
			final String str = ((String) obj);
			try {
				int ix;
				Object _element = eval(element);
				if (_element == null) {
					if (element instanceof NodeVariable) {
						_element = ((NodeVariable) element).name;
					} else {
						cx.result = null;
						return;
					}
				}
				if (_element instanceof Number) {
					ix = ((Number) _element).intValue();
				} else {
					ix = Integer.parseInt(element.toString());
				}
				if (ix < 0) {
					return;
				}

				while (ix >= str.length()) {
					// list.add(null);
				}
				// s.set(ix, value);
			} catch (Exception e) {
				// not an integer index for addressing list
			}
			return;

		} else if (obj instanceof Map) {
			String _element;
			if (element instanceof NodeVariable) {
				_element = ((NodeVariable) element).name;
			} else {
				_element = eval(element).toString();
			}
			((Map) obj).put(_element, value);

		} else {
			String _element;
			if (element instanceof NodeVariable) {
				_element = ((NodeVariable) element).name;
			} else {
				_element = eval(element).toString();
			}
			if (obj != null) {
				Handler handler = handlersClasses.get(obj.getClass());
				if (handler != null) {
					handler.set(obj, _element, value);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void visitAccess(NodeAccess node) {
		// setCurrentPosition(node.position);

		// for objects that could have handlers (like String)
		// do not leave the function since there could be some handles

		final Object obj = eval(node.object);
		final Node element = node.element;

		cx.result = null;
		if (obj instanceof ContextFrame) {
			if (element instanceof NodeString) {
				String _element = ((NodeString) element).value;
				if ("length".equals(_element)) {
					cx.result = Long.valueOf(((ContextFrame) obj).size());
					return;
				} else {
					cx.result = ((ContextFrame) obj).get(_element);
					return;
				}
			}

			Object _element = eval(element);
			if (_element != null) {
				if (_element instanceof String) {
					cx.result = ((ContextFrame) obj)._get((String) _element);
					return;
				} else {
					cx.result = ((ContextFrame) obj)._get(_element.toString());
					return;
				}
			} else {
				cx.result = null;
				return;
			}

		} else if (obj instanceof List) {
			final List list = ((List) obj);

			if (element instanceof NodeString) {
				String _element = ((NodeString) element).value;
				if ("length".equals(_element)) {
					cx.result = Long.valueOf(list.size());
					return;
				}
			}

			final int ix;
			Object _element = eval(element);
			if (_element instanceof Number) {
				ix = ((Number) _element).intValue();
			} else if (_element instanceof String) {
				try {
					ix = Integer.parseInt((String) _element);
				} catch (Exception e) {
					// not an integer index for addressing list
					cx.result = null;
					return;
				}
			} else if (_element != null) {
				try {
					ix = Integer.parseInt(element.toString());
				} catch (Exception e) {
					// not an integer index for addressing list
					cx.result = null;
					return;
				}
			} else {
				cx.result = null;
				return;
			}
			int len = list.size();
			if (ix >= 0 && ix < len) {
				cx.result = list.get(ix);
				return;
			} else if (ix >= -len && ix < 0) {
				cx.result = list.get(len + ix);
				return;
			} else {
				cx.result = null;
				return;
			}

		} else if (obj instanceof String) {
			final String str = ((String) obj);

			if (element instanceof NodeNumber) {
				Number _element = ((NodeNumber) element).number;
				int ix = _element.intValue();
				int len = str.length();
				if (ix >= 0 && ix < len) {
					cx.result = Long.valueOf(str.charAt(ix));
					return;
				} else {
					cx.result = null;
					return;
				}

			} else if (element instanceof NodeString) {
				String _element = ((NodeString) element).value;
				if ("length".equals(_element)) {
					cx.result = Long.valueOf(str.length());
					return;
				}
				try {
					int ix = Integer.parseInt(_element);
					int len = str.length();
					if (ix >= 0 && ix < len) {
						cx.result = Long.valueOf(str.charAt(ix));
						return;
					} else if (ix >= -len && ix < 0) {
						cx.result = Long.valueOf(str.charAt(len + ix));
						return;
					} else {
						cx.result = null;
						return;
					}
				} catch (Exception e) {
					// not an integer index for addressing list
				}

			} else {
				Object _element = eval(element);
				if (_element instanceof Number) {
					int ix = ((Number) _element).intValue();
					int len = str.length();
					if (ix >= 0 && ix < len) {
						cx.result = Long.valueOf(str.charAt(ix));
						return;
					} else if (ix >= -len && ix < 0) {
						cx.result = Long.valueOf(str.charAt(len + ix));
						return;
					} else {
						cx.result = null;
						return;
					}

				} else if (_element != null) {
					int ix;
					try {
						ix = Integer.parseInt(element.toString());
					} catch (Exception e) {
						// not an integer index for addressing list
						cx.result = null;
						return;
					}
					int len = str.length();
					if (ix >= 0 && ix < len) {
						cx.result = Long.valueOf(str.charAt(ix));
						return;
					} else if (ix >= -len && ix < 0) {
						cx.result = Long.valueOf(str.charAt(len + ix));
						return;
					} else {
						cx.result = null;
						return;
					}

				} else {
					cx.result = null;
					return;
				}
			}

		} else if (obj instanceof Map) {
			if (element instanceof NodeString) {
				String _element = ((NodeString) element).value;
				if ("length".equals(_element)) {
					cx.result = Long.valueOf(((Map) obj).size());
					return;
				} else {
					cx.result = ((Map) obj).get(_element);
					return;
				}
			}

			Object _element = eval(element);
			if (_element != null) {
				cx.result = ((Map) obj).get(_element);
				return;
			} else {
				cx.result = null;
				return;
			}
		}
		if (obj != null) {
			Handler handler = handlersClasses.get(obj.getClass());
			if (handler != null) {
				String _element;
				if (element instanceof NodeString) {
					_element = ((NodeString) element).value;
				} else if (element instanceof NodeVariable) {
					_element = ((NodeVariable) element).name;
				} else {
					Object newValue = eval(element);
					if (newValue != null) {
						_element = newValue.toString();
					} else {
						cx.result = null;
						return;
					}
				}
				cx.result = handler.get(obj, _element);
				return;
			}
		}
		cx.result = null;
	}

	public void visitFunction(NodeFunction function) {
		// setCurrentPosition(function.position);
		String name = function.name;
		Function result = new Function(cx, function);
		if (name != null && name.length() > 0) {
			cx.put(name, result);
		}
		cx.result = result;
	}

	public void visitCall(NodeCall call) {
		setCurrentPosition(call.position);

		Object function = eval(call.function);
		List<Node> arguments = call.arguments;

		// evaluate arguments left to right
		int argsize = arguments.size();
		List<Object> argValues = new ArrayList<Object>(argsize);
		for (int i = 0; i < argsize; i++) {
			argValues.add(eval(arguments.get(i)));
		}

		// eval() function
		if (call.function instanceof NodeVariable && "eval".equals(((NodeVariable) call.function).name)) {
			if (argsize == 0) {
				return;
			}
			StringBuilder codebuf = new StringBuilder(4096);
			for (Object foreval : argValues) {
				codebuf.append(toString(foreval));
			}

			Parser parser = new Parser(codebuf.toString());
			List<Node> evalCode = parser.parse();
			evaluate(evalCode);
			return;
		}

		// do the call
		if (function instanceof Function) {
			ContextFrame previousFrame = cx;
			try {
				Function func = (Function) function;
				cx = new ContextFrame(func.thiz);
				if (cx != null) {
					callFunction(func, argValues);
				}
			} finally {
				previousFrame.result = cx.result;
				cx = previousFrame;
			}
		} else {
			if (function != null) {
				Handler handler = handlersClasses.get(function.getClass());
				if (handler != null) {
					cx.result = handler.call(function, argValues.toArray());
					return;
				}
			} else if (call.function instanceof NodeVariable) {
				cx.result = null;
				String functionName = ((NodeVariable) call.function).name;
				Handler handler = handlersStaticCalls.get(functionName);
				if (handler != null) {
					Object[] args = argValues.toArray();
					cx.result = handler.staticCall(functionName, args);
				}
			} else {
				cx.result = null;
			}
		}
	}

	public Object callFunction(Function function, List<Object> argValues) {
		setCurrentPosition(function.body.position);
		final String[] argumentNames = function.body.argumentNames;
		final int l = Math.min(argumentNames.length, argValues.size());
		// add this
		// cx.frame.put(THIS, function.thiz);
		// add parameters
		for (int i = 0; i < l; i++) {
			cx._put(argumentNames[i], argValues.get(i));
		}
		for (int i = l; i < argumentNames.length; i++) {
			cx._put(argumentNames[i], null);
		}
		// add arguments in context
		cx._put(ARGUMENTS, argValues);
		// execute
		try {
			evaluate(function.body.body);
			// void functions will return its context for chain calls
			cx.result = function.thiz;
		} catch (JumpReturn result) {
			cx.result = result.value;
		}
		return cx.result;
	}

	public void visitTry(NodeTry tryNode) {
		// setCurrentPosition(tryNode.position);
		boolean needFinally = true;
		try {
			eval(tryNode.tryBody);

		} catch (JumpReturn returnJump) {
			needFinally = false;
			eval(tryNode.finallyBody);
			throw returnJump;

		} catch (JumpBreak breakJump) {
			needFinally = false;
			eval(tryNode.finallyBody);
			throw breakJump;

		} catch (JumpContinue continueJump) {
			needFinally = false;
			eval(tryNode.finallyBody);
			throw continueJump;

		} catch (CXException cxException) {
			String exceptionName = cxException.name;
			if (exceptionName != null) {
				for (int i = 0, l = tryNode.exceptionTypes.length; i < l; i++) {
					String catchName = tryNode.exceptionTypes[i];
					if (catchName == null || exceptionName.equals(catchName)) {
						try {
							pushContext();
							cx._put(tryNode.exceptionNames[i], cxException.value);
							eval(tryNode.exceptionBodies[i]);
						} finally {
							popContext();
						}
						break;
					}
				}
			}
		} catch (Throwable exception) {
			String exceptionName = exception.getClass().getSimpleName();
			int i = 0, l = tryNode.exceptionTypes.length;
			for (; i < l; i++) {
				String catchName = tryNode.exceptionTypes[i];
				if (exceptionName.equals(catchName)) {
					try {
						pushContext();
						cx._put(tryNode.exceptionNames[i], exception.getMessage());
						eval(tryNode.exceptionBodies[i]);
					} finally {
						popContext();
					}
					break;
				}
			}
			if (i == l) {
				// there is no match for java exceptions
				// execute finally and throw the exception
				throw new RuntimeException(exception);
			}
		} finally {
			if (needFinally) {
				eval(tryNode.finallyBody);
			}
		}
	}

	public void visitThrow(NodeThrow throwNode) {
		setCurrentPosition(throwNode.position);
		Object exceptionValue;
		if (throwNode.expression != null) {
			exceptionValue = eval(throwNode.expression);
		} else {
			exceptionValue = null;
		}
		throw new CXException(throwNode.name, exceptionValue);
	}

	public void visitSQL(NodeSQL nodeSQL) {
		setCurrentPosition(nodeSQL.position);
		// get all SQL escape tokens and if there are names that are different
		// from the reserved words try to resolve them through the context, if
		// there is no resolution through the context - use the exact name

		StringBuilder sqlEscape = new StringBuilder(4096);
		Token[] right = nodeSQL.right;
		String[] rightStr = nodeSQL.rightStr;

		for (int i = 0, l = right.length; i < l; ++i) {
			Token t = right[i];
			if (t == Token.NAME) {
				// check for a reserved word
				if (!NodeSQL.openSQLReservedWordsMap.contains(rightStr[i].toUpperCase())) {
					// try to evaluate the name in the context
					try {
						Object value = eval(new NodeVariable(null, rightStr[i]));
						if (value instanceof Number) {
							sqlEscape.append(value.toString()).append(' ');
							continue;
						} else if (value instanceof String) {
							NodeSQL.escapeSQLString(sqlEscape, (String) value);
							sqlEscape.append(' ');
							continue;
						} else if (value instanceof Calendar) {
							String format = sqlDateFormater.format(((Calendar) value).getTime());
							sqlEscape.append('\'').append(format).append("' ");
							continue;
						}
					} catch (Exception e) {
						// error in evaluation - use the exact name
					}
				}
			}
			sqlEscape.append(rightStr[i]).append(' ');
		}

		if (nodeSQL.left instanceof NodeVariable) {
			String varName = ((NodeVariable) nodeSQL.left).name;
			Object rhsObject = sqlEscape.toString();
			cx.put(varName, rhsObject);

		} else if (nodeSQL.left instanceof NodeAccess) {
			setNodeAccessValue((NodeAccess) nodeSQL.left, sqlEscape.toString());

		} else if (nodeSQL.left == null) {
			cx.result = null;
		}
	}

	static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static void escapeString(StringBuilder builder, String str) {
		builder.append('"');
		for (int i = 0, l = str.length(); i < l; i++) {
			final char c = str.charAt(i);
			switch (c) {
				case '"':
					builder.append("\\\"");
					break;
				case '\\':
					builder.append("\\\\");
					break;
				case '/':
					builder.append("\\/");
					break;
				case '\b':
					builder.append("\\b");
					break;
				case '\f':
					builder.append("\\f");
					break;
				case '\n':
					builder.append("\\n");
					break;
				case '\r':
					builder.append("\\r");
					break;
				case '\t':
					builder.append("\\t");
					break;
				default:
					if ((c < 0x0020) || (c > 0x007e)) {
						builder.append("\\u");
						builder.append(hexDigit[((c >> 12) & 0xF)]);
						builder.append(hexDigit[((c >> 8) & 0xF)]);
						builder.append(hexDigit[((c >> 4) & 0xF)]);
						builder.append(hexDigit[(c & 0xF)]);
					} else {
						builder.append(c);
					}
					break;
			}
		}
		builder.append('"');
	}
}