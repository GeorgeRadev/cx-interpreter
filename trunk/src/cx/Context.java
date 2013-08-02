package cx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import cx.ast.Visitor;
import cx.exception.CXException;
import cx.exception.JumpBreak;
import cx.exception.JumpContinue;
import cx.exception.JumpReturn;
import cx.runtime.ContextFrame;
import cx.runtime.Function;
import cx.runtime.ObjectHandler;

public class Context implements Visitor {
	private static final String THIS = "this";

	private static final Long ZERO = 0L;

	private ContextFrame cx = null;
	public SourcePosition position = null;

	public Context() {
		cx = new ContextFrame();
	}

	public String toString() {
		return cx.toString();
	}

	private final List<ObjectHandler> handlers = new ArrayList<ObjectHandler>();

	public void addHandler(ObjectHandler handler) {
		if (handler != null) {
			handlers.add(handler);
			handler.init(this);
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
		} catch (JumpReturn returnJump) {
			// finish interpretation and set the result
			cx.result = returnJump.value;
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
		cx.set(varName, value);
	}

	public void visitBlock(NodeBlock paramBlockNode) {
		position = paramBlockNode.position;
		try {
			pushContext();

			for (Node statement : paramBlockNode.statements) {
				eval(statement);
			}

		} finally {
			popContext();
		}
	}

	public void visitVar(NodeVar varNode) {
		position = varNode.position;
		// define variables in current context
		for (NodeAssign node : varNode.vars) {
			if (node.left instanceof NodeVariable) {
				cx.frame.put(((NodeVariable) node.left).name, ZERO);
			}
		}
		for (Node node : varNode.vars) {
			node.accept(this);
		}
	}

	public void visitNumber(NodeNumber numberNode) {
		position = numberNode.position;
		String value = numberNode.value;
		// detect precision of the number
		if (value.indexOf('.') >= 0 || value.indexOf('e') >= 0) {
			cx.result = new Double(value);
			return;
		} else {
			char isHex = (value.length() > 2 && value.charAt(0) == '0') ? value.charAt(1) : '\0';
			long l;
			if (isHex == 'x' || isHex == 'X') {
				l = Long.parseLong(value.substring(2), 16);
			} else {
				l = Long.parseLong(value, 10);
			}
			if (l < Integer.MAX_VALUE) {
				cx.result = Integer.valueOf((int) l);
			} else {
				cx.result = Long.valueOf(l);
			}
		}
	}

	public void visitVariable(NodeVariable variableNode) {
		position = variableNode.position;
		cx.result = cx.get(variableNode.name);
	}

	public void visitAssign(NodeAssign assignNode) {
		position = assignNode.position;
		if (assignNode.left instanceof NodeVariable) {
			String varName = ((NodeVariable) assignNode.left).name;
			Object rhsObject = eval(assignNode.right);
			cx.set(varName, rhsObject);

		} else if (assignNode.left instanceof NodeAccess) {
			setNodeAccessValue((NodeAccess) assignNode.left, eval(assignNode.right));

		} else if (assignNode.left == null) {
			cx.result = null;
			return;
		}
	}

	public void visitBinary(NodeBinary binaryNode) {
		position = binaryNode.position;
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
		position = switchNode.position;
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

	public void visitFor(NodeFor paramForNode) {
		position = paramForNode.position;
		try {
			pushContext();
			eval(paramForNode.initialization);
			Object localScrBoolean = eval(paramForNode.condition);
			if (isTrue(localScrBoolean)) {
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
					localScrBoolean = eval(paramForNode.condition);
				} while (isTrue(localScrBoolean));
			}
		} finally {
			popContext();
		}
	}

	public void visitWhile(NodeWhile paramWhileNode) {
		position = paramWhileNode.position;
		try {
			pushContext();
			Object localScrBoolean = eval(paramWhileNode.condition);

			while (isTrue(localScrBoolean)) {
				try {
					eval(paramWhileNode.body);
				} catch (JumpBreak localBreakJump) {
					// break
					break;
				} catch (JumpContinue localContinueJump) {
					// continue
				}
				localScrBoolean = eval(paramWhileNode.condition);
			}
		} finally {
			popContext();
		}
	}

	public void visitIf(NodeIf paramIfNode) {
		position = paramIfNode.position;
		try {
			pushContext();
			Object condition = eval(paramIfNode.condition);

			if (isTrue(condition)) {
				eval(paramIfNode.body);
			} else {
				eval(paramIfNode.elseBody);
			}

		} finally {
			popContext();
		}
	}

	public void visitTernary(NodeTernary node) {
		position = node.position;

		Object condition = eval(node.condition);

		if (isTrue(condition)) {
			eval(node.trueValue);
		} else {
			eval(node.falseValue);
		}
	}

	public void visitReturn(NodeReturn paramReturnNode) {
		position = paramReturnNode.position;
		cx.result = eval(paramReturnNode.expression);
		throw new JumpReturn(cx.result);
	}

	public void visitBreak(NodeBreak paramBreakNode) {
		throw new JumpBreak();
	}

	public void visitContinue(NodeContinue paramContinueNode) {
		throw new JumpContinue();
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
		if (number instanceof Long) {
			return Long.valueOf(number.longValue() + 1L);
		}
		return Integer.valueOf(number.intValue() + 1);
	}

	private Number decrement(Number number) {
		if (number instanceof Double) {
			return new Double(number.doubleValue() - 1.0d);
		}
		if (number instanceof Long) {
			return Long.valueOf(number.longValue() - 1L);
		}
		return Integer.valueOf(number.intValue() - 1);
	}

	private Number negate(Number number) {
		if (number instanceof Double) {
			return new Double(number.doubleValue() * -1.0d);
		}
		if (number instanceof Long) {
			return Long.valueOf(number.longValue() * -1L);
		}
		return Integer.valueOf(number.intValue() * -1);
	}

	public void visitUnary(NodeUnary unary) {
		position = unary.position;
		Object result = eval(unary.expresion);
		switch (unary.operator) {
			case NOT:
				cx.result = isTrue(result) ? Boolean.FALSE : Boolean.TRUE;
				return;
			case INC_PRE:
				if ((result instanceof Number)) {
					Object newValue = increment((Number) result);
					if ((unary.expresion instanceof NodeVariable)) {
						cx.set(((NodeVariable) unary.expresion).name, newValue);
					} else if (unary.expresion instanceof NodeAccess) {
						setNodeAccessValue((NodeAccess) unary.expresion, newValue);
					}
					cx.result = newValue;
				}
				return;
			case INC_POST:
				if ((result instanceof Number)) {
					Object newValue = increment((Number) result);
					if (unary.expresion instanceof NodeVariable) {
						cx.set(((NodeVariable) unary.expresion).name, newValue);
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
						cx.set(((NodeVariable) unary.expresion).name, newValue);
					} else if (unary.expresion instanceof NodeAccess) {
						setNodeAccessValue((NodeAccess) unary.expresion, newValue);
					}
					cx.result = newValue;
				}
				return;
			case DEC_POST:
				if ((result instanceof Number)) {
					Object newValue = decrement((Number) result);
					if ((unary.expresion instanceof NodeVariable)) {
						cx.set(((NodeVariable) unary.expresion).name, newValue);
					} else if (unary.expresion instanceof NodeAccess) {
						setNodeAccessValue((NodeAccess) unary.expresion, newValue);
					}
					cx.result = result;
				}
				return;
			case NEGATE:
				if ((result instanceof Number)) {
					cx.result = negate((Number) result);
				}
				return;
			default:
		}
	}

	@SuppressWarnings("rawtypes")
	boolean isTrue(Object obj) {
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
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj) ? 1L : 0L;
		} else if (obj instanceof Number) {
			return ((Number) obj).longValue();
		} else if (obj instanceof String && ((String) obj).length() == 0) {
			return ZERO;
		} else {
			try {
				return Parser.parseNumber(obj.toString()).longValue();
			} catch (NumberFormatException e) {}
		}
		return null;
	}

	public static double toDouble(Object obj) {
		if (obj == null) {
			return 0.0d;
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj) ? 1d : 0d;
		} else if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		} else if (obj instanceof String && ((String) obj).length() == 0) {
			return 0.0d;
		} else {
			try {
				return Parser.parseNumber(obj.toString()).doubleValue();
			} catch (NumberFormatException e) {}
		}
		return Double.NaN;
	}

	@SuppressWarnings("rawtypes")
	public static String toString(Object obj) {
		if (obj == null) {
			return "";
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj) ? "true" : "";
		} else if (obj instanceof Number) {
			return ((Number) obj).doubleValue() == 0d ? "0" : obj.toString();
		} else if (obj instanceof String) {
			if (((String) obj).length() == 0) {
				return "";
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
						escapeString(buffer , element.toString() ); 
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
			return r == null || r.compareTo(l) == 0;

		} else if (left instanceof String) {
			String l = toString(left);
			if (l.length() <= 0) {
				return toLong(right) == ZERO;
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
				return toLong(right) == ZERO;
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
		} else if (left instanceof Number && right instanceof Number) {
			if (left instanceof Double || right instanceof Double) {
				double d = ((Number) left).doubleValue() + ((Number) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() + ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return Integer.valueOf((int) l);
				} else {
					return Long.valueOf(l);
				}
			}
		} else if (left instanceof String) {
			return (String) left + toString(right);
		} else if (left instanceof List) {
			((List) left).add(right);
			return left;
		}
		return null;
	}

	private Object subtract(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			if (left instanceof Double || right instanceof Double) {
				double d = ((Number) left).doubleValue() - ((Number) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() - ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return Integer.valueOf((int) l);
				} else {
					return Long.valueOf(l);
				}
			}
		}
		return null;
	}

	private Object multiply(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			if (left instanceof Double || right instanceof Double) {
				double d = ((Number) left).doubleValue() * ((Number) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() * ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return Integer.valueOf((int) l);
				} else {
					return Long.valueOf(l);
				}
			}
		}
		return null;
	}

	private Object divide(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			if (left instanceof Double || right instanceof Double) {
				double d = ((Number) left).doubleValue() / ((Number) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() / ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return Integer.valueOf((int) l);
				} else {
					return Long.valueOf(l);
				}
			}
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
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
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
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
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
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
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
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
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
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
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
			if (l < Integer.MAX_VALUE) {
				return Integer.valueOf((int) l);
			} else {
				return Long.valueOf(l);
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void visitArray(NodeArray arr) {
		position = arr.position;
		int len = arr.elements.size();
		List result = new ArrayList(len);
		for (Node node : arr.elements) {
			result.add(eval(node));
		}
		cx.result = result;
	}

	public void visitObject(NodeObject object) {
		position = object.position;
		String parentObjectName = object.parent;
		final ContextFrame newObject;
		if (parentObjectName != null && parentObjectName.length() > 0) {
			Object parent = cx.get(parentObjectName);
			newObject = (parent instanceof ContextFrame) ? new ContextFrame((ContextFrame) parent) : new ContextFrame();
		} else {
			newObject = new ContextFrame();
		}
		ContextFrame current = cx;
		cx = newObject;
		// add object elements
		for (String key : object.object.keySet()) {
			newObject.set(key, eval(object.object.get(key)));
		}
		cx = current;
		cx.result = newObject;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setNodeAccessValue(NodeAccess access, Object value) {
		final Object obj = eval(access.object);
		final Node element = access.element;

		if (obj instanceof List) {
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

				while (ix >= list.size()) {
					list.add(null);
				}
				list.set(ix, value);
			} catch (Exception e) {
				// not an integer index for addressing list
			}
			return;
		}

		String _element;
		if (element instanceof NodeVariable) {
			_element = ((NodeVariable) element).name;
		} else {
			_element = eval(element).toString();
		}
		if (obj instanceof Map) {
			((Map) obj).put(_element, value);
		} else if (obj instanceof ContextFrame) {
			((ContextFrame) obj).set(_element, value);
		} else {
			for (ObjectHandler handler : handlers) {
				if (handler.accept(obj)) {
					handler.set(obj, _element, value);
					break;
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void visitAccess(NodeAccess node) {
		position = node.position;
		final Object obj = eval(node.object);
		final Node element = node.element;

		cx.result = null;
		if (obj instanceof List) {
			final List list = ((List) obj);
			try {
				int ix;
				Object _element = eval(element);
				if (_element == null) {
					if (element instanceof NodeVariable) {
						_element = ((NodeVariable) element).name;
						if ("length".equals(_element)) {
							cx.result = list.size();
							return;
						}
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

				if (ix >= 0 && ix < list.size()) {
					cx.result = list.get(ix);
				} else {
					cx.result = null;
				}
			} catch (Exception e) {
				// not an integer index for addressing list
			}
			return;
		}

		String _element;
		if (element instanceof NodeVariable) {
			_element = ((NodeVariable) element).name;
			if ("length".equals(_element)) {
				if (obj instanceof Map) {
					cx.result = ((Map) obj).size();
					return;
				}
			}
		} else {
			_element = eval(element).toString();
		}
		if (obj instanceof Map) {
			cx.result = ((Map) obj).get(_element);
		} else if (obj instanceof ContextFrame) {
			cx.result = ((ContextFrame) obj).get(_element);
		} else {
			for (ObjectHandler handler : handlers) {
				if (handler.accept(obj)) {
					cx.result = handler.get(obj, _element);
					break;
				}
			}
		}
	}

	public void visitFunction(NodeFunction function) {
		position = function.position;
		String name = function.name;
		Function result = new Function(cx, function);
		if (name != null && name.length() > 0) {
			cx.set(name, result);
		}
		cx.result = result;
	}

	public void visitCall(NodeCall call) {
		position = call.position;

		Object function = eval(call.function);
		List<Node> arguments = call.arguments.elements;

		// evaluate arguments left to right
		int argsize = arguments.size();
		Object[] argValues = new Object[argsize];
		for (int i = 0; i < argsize; i++) {
			argValues[i] = eval(arguments.get(i));
		}

		if (call.function instanceof NodeVariable && "eval".equals(((NodeVariable) call.function).name)) {
			// eval() function
			if (argsize == 0) {
				return;
			}
			String code;
			if (argsize == 1) {
				code = toString(argValues[0]);
			} else {
				StringBuilder codebuf = new StringBuilder(4096);
				for (int i = 0; i < argsize; i++) {
					codebuf.append(toString(argValues[i]));
				}
				code = codebuf.toString();
			}

			Parser parser = new Parser(code);
			List<Node> evalCode = parser.parse();
			evaluate(evalCode);
			return;
		}

		try {
			pushContext();
			cx.result = null;
			// do the call
			if (function instanceof Function) {
				callFunction(((Function) function), argValues);

			} else {
				if (function == null && call.function instanceof NodeVariable) {
					String functionName = ((NodeVariable) call.function).name;
					for (ObjectHandler handler : handlers) {
						if (handler.acceptStaticCall(functionName, argValues)) {
							cx.result = handler.staticCall(functionName, argValues);
							break;
						}
					}
				} else {
					for (ObjectHandler handler : handlers) {
						if (handler.accept(function)) {
							cx.result = handler.call(function, argValues);
							break;
						}
					}
				}
			}
		} finally {
			popContext();
		}
	}

	public Object callFunction(Function function, Object[] args) {
		position = function.function.position;
		final String[] argumentNames = function.function.argumentNames;
		final int l = args.length;
		if (l != argumentNames.length) {
			return null;
		}
		// add this
		cx.frame.put(THIS, function.thiz);
		// add parameters
		for (int i = 0; i < l; i++) {
			cx.frame.put(argumentNames[i], args[i]);
		}
		// execute
		try {
			evaluate(function.function.body);
		} catch (JumpReturn result) {
			cx.result = result.value;
		}
		return cx.result;
	}

	public void visitTry(NodeTry tryNode) {
		position = tryNode.position;
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
			Object exception = cxException.object;
			if (exception != null) {
				String exceptionName = exception.getClass().getSimpleName();
				for (int i = 0, l = tryNode.exceptionTypes.length; i < l; i++) {
					String catchName = tryNode.exceptionTypes[i];
					if (exceptionName.equals(catchName)) {
						try {
							pushContext();
							cx.frame.put(tryNode.exceptionNames[i], exception);
							eval(tryNode.exceptionBodies[i]);
						} finally {
							popContext();
						}
						break;
					}
				}
			}
		} finally {
			if (needFinally) {
				eval(tryNode.finallyBody);
			}
		}
	}

	public void visitThrow(NodeThrow throwNode) {
		position = throwNode.position;
		Object exception = eval(throwNode.expresion);
		throw new CXException(exception);
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