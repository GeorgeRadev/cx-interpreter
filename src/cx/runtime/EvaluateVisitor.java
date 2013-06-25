package cx.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cx.Context;
import cx.ast.Node;
import cx.ast.NodeAccess;
import cx.ast.NodeArray;
import cx.ast.NodeAssign;
import cx.ast.NodeBinary;
import cx.ast.NodeBlock;
import cx.ast.NodeBreak;
import cx.ast.NodeCall;
import cx.ast.NodeCase;
import cx.ast.NodeCaseList;
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
import cx.ast.NodeTrue;
import cx.ast.NodeUnary;
import cx.ast.NodeVar;
import cx.ast.NodeVariable;
import cx.ast.NodeWhile;
import cx.ast.Visitor;
import cx.exception.JumpBreak;
import cx.exception.JumpContinue;
import cx.exception.JumpReturn;
import cx.util.SourcePosition;

public class EvaluateVisitor implements Visitor {
	private Context cx = null;
	private SourcePosition position = null;
	private final boolean isDebug;

	public EvaluateVisitor(Context paramContext, boolean isDebug) {
		cx = paramContext;
		this.isDebug = isDebug;
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
		cx = new Context(cx);
	}

	private void popContext() {
		cx.parent.result = cx.result;
		cx = cx.parent;
	}

	public void visitBlock(NodeBlock paramBlockNode) {
		position = paramBlockNode.position;
		try {
			pushContext();

			for (Node statement : paramBlockNode.statements) {
				eval(statement);
			}
			if (isDebug) {
				System.out.println("{");
				System.out.println(position);
				cx.dumpContext();
				System.out.println("}");
			}
		} finally {
			popContext();
		}
	}

	public void visitVar(NodeVar varNode) {
		position = varNode.position;
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
				cx.result = new Integer((int) l);
			} else {
				cx.result = new Long(l);
			}
		}
	}

	public void visitVariable(NodeVariable variableNode) {
		position = variableNode.position;
		cx.result = cx.getVariable(variableNode.name);
	}

	public void visitAssign(NodeAssign assignNode) {
		position = assignNode.position;
		if (assignNode.left instanceof NodeVariable) {
			String varName = ((NodeVariable) assignNode.left).name;
			Object rhsObject = eval(assignNode.right);
			cx.setVariable(varName, rhsObject);

		} else if (assignNode.left instanceof NodeAccess) {
			// TODO: implement

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
				if (left == null && right == null) {
					cx.result = true;
					return;
				} else if (left != null) {
					cx.result = left.equals(right);
					return;
				}
				cx.result = false;
				return;
			case NE:
				if (left == null && right == null) {
					cx.result = false;
					return;
				} else if (left != null) {
					cx.result = !left.equals(right);
					return;
				}
				cx.result = true;
				return;
			case GT:
				cx.result = greaterThan(left, right);
				return;

			case GE:
				cx.result = greaterEqual(left, right);
				return;
			case LT:
				cx.result = lowerThan(left, right);
				return;
			case LE:
				cx.result = lowerEqual(left, right);
				return;
		}
		cx.result = null;
	}

	public void visitSwitch(NodeSwitch paramSwitchNode) {}

	public void visitCaseList(NodeCaseList paramCaseListNode) {}

	public void visitCase(NodeCase paramCaseNode) {}

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
			Object localScrBoolean = eval(paramIfNode.condition);

			if (isTrue(localScrBoolean)) {
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
		if (cx.result != null) throw new JumpReturn(cx.result);
		throw new JumpReturn(null);
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
			return new Long(number.longValue() + 1L);
		}
		return new Integer(number.intValue() + 1);
	}

	private Number decrement(Number number) {
		if (number instanceof Double) {
			return new Double(number.doubleValue() - 1.0d);
		}
		if (number instanceof Long) {
			return new Long(number.longValue() - 1L);
		}
		return new Integer(number.intValue() - 1);
	}

	public void visitUnary(NodeUnary paramUnaryExprNode) {
		position = paramUnaryExprNode.position;
		Object result = eval(paramUnaryExprNode.expresion);
		switch (paramUnaryExprNode.operator) {
			case NOT:
				if ((result instanceof Boolean)) cx.result = (!((Boolean) result) ? Boolean.TRUE : Boolean.FALSE);
				break;
			case INC_PRE:
				if ((result instanceof Number)) {
					cx.result = increment((Number) result);
					if ((paramUnaryExprNode.expresion instanceof NodeVariable))
						cx.setVariable(((NodeVariable) paramUnaryExprNode.expresion).name, cx.result);
				}
				break;
			case INC_POST:
				if ((result instanceof Number)) {
					cx.result = result;
					if ((paramUnaryExprNode.expresion instanceof NodeVariable))
						cx.setVariable(((NodeVariable) paramUnaryExprNode.expresion).name, increment((Number) result));
				}
				break;
			case DEC_PRE:
				if ((result instanceof Number)) {
					cx.result = decrement((Number) result);
					if ((paramUnaryExprNode.expresion instanceof NodeVariable))
						cx.setVariable(((NodeVariable) paramUnaryExprNode.expresion).name, cx.result);
				}
				break;
			case DEC_POST:
				if ((result instanceof Number)) {
					cx.result = result;
					if ((paramUnaryExprNode.expresion instanceof NodeVariable))
						cx.setVariable(((NodeVariable) paramUnaryExprNode.expresion).name, decrement((Number) result));
				}
				break;
		}
	}

	@SuppressWarnings("rawtypes")
	boolean isTrue(Object obj) {
		return obj != null
				&& ((obj instanceof Boolean && ((Boolean) obj))
						|| (obj instanceof Number && ((Number) obj).longValue() != 0L)
						|| (obj instanceof String && obj.toString().length() > 0)
						|| (obj instanceof List && ((List) obj).size() > 0) || (obj instanceof Map && ((Map) obj).size() > 0));
	}

	private Object greaterThan(Object left, Object right) {
		if (left == null && right == null) {
			return Boolean.FALSE;
		} else if (left instanceof Boolean) {
			return isTrue(left) && !isTrue(right);
		} else if (left instanceof String) {
			if (right == null) {
				return Boolean.TRUE;
			}
			return left.toString().compareTo(right.toString()) > 0;
		} else if (left instanceof Number) {
			double l = ((Number) left).doubleValue();
			double r;
			if (right instanceof Boolean) {
				r = isTrue(left) ? 1 : 0;
			} else if (right instanceof Number) {
				r = ((Number) right).doubleValue();
			} else if (right instanceof String) {
				try {
					r = Double.parseDouble(right.toString());
				} catch (Exception e) {
					return Boolean.TRUE;
				}
				return l > r;
			}
		}
		return Boolean.TRUE;
	}

	private Object greaterEqual(Object left, Object right) {
		if (left == null && right == null) {
			return Boolean.TRUE;
		} else if (left instanceof Boolean) {
			return isTrue(left) || !isTrue(right);
		} else if (left instanceof String) {
			if (right == null) {
				return Boolean.TRUE;
			}
			return left.toString().compareTo(right.toString()) >= 0;
		} else if (left instanceof Number) {
			double l = ((Number) left).doubleValue();
			double r;
			if (right instanceof Boolean) {
				r = isTrue(left) ? 1 : 0;
			} else if (right instanceof Number) {
				r = ((Number) right).doubleValue();
			} else if (right instanceof String) {
				try {
					r = Double.parseDouble(right.toString());
				} catch (Exception e) {
					return left.toString().compareTo(right.toString()) < 0;
				}
				return l >= r;
			}
		}
		return Boolean.TRUE;
	}

	private Object lowerThan(Object left, Object right) {
		if (left == null && right == null) {
			return Boolean.FALSE;
		} else if (left instanceof Boolean) {
			return !isTrue(left) && isTrue(right);
		} else if (left instanceof String) {
			if (right == null) {
				return Boolean.FALSE;
			}
			return left.toString().compareTo(right.toString()) < 0;
		} else if (left instanceof Number) {
			double l = ((Number) left).doubleValue();
			double r;
			if (right instanceof Boolean) {
				r = isTrue(left) ? 1 : 0;
			} else if (right instanceof Number) {
				r = ((Number) right).doubleValue();
			} else if (right instanceof String) {
				try {
					r = Double.parseDouble(right.toString());
				} catch (Exception e) {
					return left.toString().compareTo(right.toString()) < 0;
				}
				return l < r;
			}
		}
		return Boolean.TRUE;
	}

	private Object lowerEqual(Object left, Object right) {
		if (left == null && right == null) {
			return Boolean.TRUE;
		} else if (left instanceof Boolean) {
			return !isTrue(left) || isTrue(right);
		} else if (left instanceof String) {
			if (right == null) {
				return Boolean.FALSE;
			}
			return left.toString().compareTo(right.toString()) < 0;
		} else if (left instanceof Number) {
			double l = ((Number) left).doubleValue();
			double r;
			if (right instanceof Boolean) {
				r = isTrue(left) ? 1 : 0;
			} else if (right instanceof Number) {
				r = ((Number) right).doubleValue();
			} else if (right instanceof String) {
				try {
					r = Double.parseDouble(right.toString());
				} catch (Exception e) {
					return left.toString().compareTo(right.toString()) < 0;
				}
				return l <= r;
			}
		}
		return Boolean.TRUE;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object add(Object left, Object right) {
		if (left == null && right == null) {
			return null;
		} else if (right == null) {
			return left;
		} else if (left instanceof Number && right instanceof Number) {
			if (left instanceof Double || right instanceof Double) {
				double d = ((Double) left).doubleValue() + ((Double) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() + ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return new Integer((int) l);
				} else {
					return new Long(l);
				}
			}
		} else if (left instanceof String) {
			return left.toString() + right.toString();
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
				double d = ((Double) left).doubleValue() - ((Double) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() - ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return new Integer((int) l);
				} else {
					return new Long(l);
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
				double d = ((Double) left).doubleValue() * ((Double) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() * ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return new Integer((int) l);
				} else {
					return new Long(l);
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
				double d = ((Double) left).doubleValue() / ((Double) right).doubleValue();
				return new Double(d);
			} else {
				long l = ((Number) left).longValue() / ((Number) right).longValue();
				if (l < Integer.MAX_VALUE) {
					return new Integer((int) l);
				} else {
					return new Long(l);
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
				return new Integer((int) l);
			} else {
				return new Long(l);
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
				return new Integer((int) l);
			} else {
				return new Long(l);
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
				return new Integer((int) l);
			} else {
				return new Long(l);
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
				return new Integer((int) l);
			} else {
				return new Long(l);
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
				return new Integer((int) l);
			} else {
				return new Long(l);
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
				return new Integer((int) l);
			} else {
				return new Long(l);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void visitObject(NodeObject object) {
		position = object.position;
		int len = object.object.size();
		Map result = new HashMap(len);
		for (String key : object.object.keySet()) {
			result.put(key, eval(object.object.get(key)));
		}
		cx.result = result;
	}

	public void visitAccess(NodeAccess node) {
		// TODO Auto-generated method stub
	}

	public void visitFunction(NodeFunction paramFunctionDeclNode) {
		position = paramFunctionDeclNode.position;
		// TODO Auto-generated method stub
	}

	public void visitCall(NodeCall paramFunctionNode) {
		// TODO Auto-generated method stub
	}
}