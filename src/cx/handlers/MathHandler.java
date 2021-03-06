package cx.handlers;

import cx.Context;
import cx.ast.Visitor;
import cx.runtime.Handler;

/////////////Math Handler provides the global object Math with methods:
//Math {
//    random(); 
//    abs(number); 
//    ceil(number); 
//    floor(number); 
//    round(number);
//    sqrt(number); 
//    max(number,...); 
//    min(number,...); 
//    isNaN(number); 
//    parseInteger(string); 
//    parseDouble(string);
//}
//and static method formatDate() for creating a string representation.

////////////////Examples:
//var d = Math.round(0.53); // d = 1
//var d = Math.floor(0.23); // d = 0.0
//var d = Math.max(-1,0,1,2,4); // d = 4.0
//var d = Math.min(2,4,6,8,9); // d = 2.0
//var d = Math.parseInteger('2.3'); // d = 2

public class MathHandler implements Handler {

	public void init(Visitor cx) {
		// defines global object for math operations
		cx.set("Math", this);
	}

	public static enum MathMetod {
		random, abs, ceil, floor, round, sqrt, max, min, isNaN, parseInteger, parseDouble;

		public static MathMetod parse(final String str) {
			String guess = null;
			final int length = str.length();
			MathMetod method = null;

			switch (length) {
				case 3:
					switch (str.charAt(2)) {
						case 's':
							guess = "abs";
							method = abs;
							break;
						case 'x':
							guess = "max";
							method = max;
							break;
						case 'n':
							guess = "min";
							method = min;
							break;
					}
					break;
				case 4:
					if (str.charAt(0) == 'c') {
						guess = "ceil";
						method = ceil;
					}
					if (str.charAt(0) == 's') {
						guess = "sqrt";
						method = sqrt;
					}
					break;
				case 5:
					switch (str.charAt(0)) {
						case 'f':
							guess = "floor";
							method = floor;
							break;
						case 'r':
							guess = "round";
							method = round;
							break;
						case 'i':
							guess = "isNaN";
							method = isNaN;
							break;
					}
					break;
				case 6:
					if (str.charAt(0) == 'r') {
						guess = "random";
						method = random;
					}
					break;
				default:
					if (length == 12 && str.charAt(5) == 'I') {
						guess = "parseInteger";
						method = parseInteger;
					} else if (length == 11 && str.charAt(5) == 'D') {
						guess = "parseDouble";
						method = parseDouble;
					}

			}
			if ((guess != null) && !guess.equals(str)) {
				return null;
			}
			return method;
		}
	}

	private static class MathCall {
		final MathMetod method;

		MathCall(MathMetod method) {
			this.method = method;
		}
	}

	public Object[] supportedClasses() {
		return new Object[] { MathHandler.class, MathCall.class };
	}

	public void set(Object object, String variable, Object value) {
	}

	public Object get(Object thiz, String variable) {
		if (thiz instanceof MathHandler) {
			MathMetod method = MathMetod.parse(variable);
			if (method != null) {
				return new MathCall(method);
			}
		}
		return null;
	}

	public Object call(Object object, Object[] args) {
		if (!(object instanceof MathCall) || args == null) {
			return null;
		}

		MathCall mathCall = (MathCall) object;

		switch (mathCall.method) {
			case random:
				return Math.random();
			case abs:
				if (args.length == 1) {
					if (args[0] instanceof Double) {
						return Math.abs(((Double) args[0]).doubleValue());
					}
					Long l = Context.toLong(args[0]);
					if (l == null) {
						return null;
					}
					return Math.abs(l);
				}
				break;
			case ceil:
				if (args.length == 1) {
					if (args[0] instanceof Double) {
						return Math.ceil(((Double) args[0]).doubleValue());
					}
					return Context.toLong(args[0]);
				}
				break;
			case floor:
				if (args.length == 1) {
					if (args[0] instanceof Double) {
						return Math.floor(((Double) args[0]).doubleValue());
					}
					return Context.toLong(args[0]);
				}
				break;
			case round:
				if (args.length == 1) {
					if (args[0] instanceof Double) {
						return Math.round(((Double) args[0]).doubleValue());
					}
					return Context.toLong(args[0]);
				}
				break;
			case sqrt:
				if (args.length == 1) {
					double d;
					if (args[0] instanceof Double) {
						d = ((Double) args[0]).doubleValue();
					} else {
						Long l = Context.toLong(args[0]);
						if (l == null) {
							d = Double.NaN;
						} else {
							d = l.doubleValue();
						}
					}
					return Math.sqrt(d);
				}
				break;
			case max: {
				double r = Double.MIN_VALUE;
				for (Object o : args) {
					double d = Context.toDouble(o);
					r = Math.max(d, r);
				}
				return r;
			}
			case min: {
				double r = Double.MAX_VALUE;
				for (Object o : args) {
					double d = Context.toDouble(o);
					r = Math.min(d, r);
				}
				return r;
			}
			case isNaN:
				if (args.length == 1) {
					if (args[0] instanceof Double) {
						return Double.isNaN(((Double) args[0]).doubleValue());
					} else {
						return Double.isNaN(Context.toDouble(args[0]));
					}
				}
				break;
			case parseInteger:
				if (args.length == 1) {
					if (args[0] instanceof Number) {
						return ((Number) args[0]).longValue();
					} else {
						double d = Context.toDouble(args[0]);
						if (d != d) {
							return null;
						} else {
							return (long) d;
						}
					}
				}
				break;
			case parseDouble:
				if (args.length == 1) {
					if (args[0] instanceof Number) {
						return ((Number) args[0]).doubleValue();
					} else {
						return Context.toDouble(args[0]);
					}
				}
				break;
		}
		return null;
	}

	public String[] supportedStaticCalls() {
		return null;
	}

	public Object staticCall(String method, Object[] args) {
		return null;
	}

}