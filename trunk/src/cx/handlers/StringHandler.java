package cx.handlers;

import cx.Context;
import cx.ast.Visitor;
import cx.runtime.Handler;

//////////////////supported functions over String
//trim(); 
//substring(start); 
//substring(start,end); 
//replace(from,to);
//indexOf(char); 
//lastIndexOf(char); 
//startsWith(string); 
//endsWith(string);
//toLowerCase(); 
//toUpperCase();

//////////////////////examples
//var str = ' trim '; 
//str = str.trim();                         - str = 'trim'
//str = 'smallCammelCase'.toLowerCase();    - str = 'smallcammelcase'

public class StringHandler implements Handler {
	public static enum StringMethod {
		trim, substring, replace, indexOf, lastIndexOf, startsWith, endsWith, toLowerCase, toUpperCase;

		public static StringMethod parse(final String str) {
			String guess = null;
			final int l = str.length();
			StringMethod method = null;

			switch (l) {
				case 4:
					if ('t' == str.charAt(0)) {
						guess = "trim";
						method = trim;
					}
					break;
				case 7:
					switch (str.charAt(0)) {
						case 'i':
							guess = "indexOf";
							method = indexOf;
							break;
						case 'r':
							guess = "replace";
							method = replace;
							break;
					}
					break;
				case 8:
					if ('e' == str.charAt(0)) {
						guess = "endsWith";
						method = endsWith;
					}
					break;
				case 9:
					if ('s' == str.charAt(0)) {
						guess = "substring";
						method = substring;
					}
					break;
				case 10:
					if ('s' == str.charAt(0)) {
						guess = "startsWith";
						method = startsWith;
					}
					break;
				case 11:
					switch (str.charAt(2)) {
						case 'L':
							guess = "toLowerCase";
							method = toLowerCase;
							break;
						case 'U':
							guess = "toUpperCase";
							method = toUpperCase;
							break;
						case 's':
							guess = "lastIndexOf";
							method = lastIndexOf;
							break;
					}
					break;
			}
			if ((guess != null) && !guess.equals(str)) {
				return null;
			}
			return method;
		}
	}

	private static class StringCall {
		final String string;
		final StringMethod method;

		StringCall(String string, StringMethod method) {
			this.string = string;
			this.method = method;
		}
	}

	public void init(Visitor cx) {}

	public Object[] supportedClasses() {
		return new Object[] { String.class, StringCall.class };
	}

	public void set(Object object, String variable, Object value) {}

	public Object get(Object thiz, String variable) {
		StringMethod method = StringMethod.parse(variable);
		if (method != null) {
			return new StringCall((String) thiz, method);
		}
		return null;
	}

	public Object call(Object object, Object[] args) {
		if (!(object instanceof StringCall)) {
			return null;
		}

		StringCall stringCall = (StringCall) object;
		if (args.length == 2) {
			if (args[0] != null && args[1] != null) {
				switch (stringCall.method) {
					case replace: {
						String str1 = args[0].toString();
						String str2 = args[1].toString();
						if (str1.length() == 1 && str2.length() == 1) {
							return stringCall.string.replace(str1.charAt(0), str2.charAt(0));
						} else {
							return stringCall.string.replace(str1, str2);
						}
					}
					case substring: {
						int i1 = Context.toLong(args[0]).intValue();
						int i2 = Context.toLong(args[1]).intValue();
						String str = stringCall.string;
						int l = str.length();
						if (i2 > l) {
							i2 = l;
						}
						if (i1 < 0) {
							i1 = 0;
						}
						if (i1 > i2) {
							return "";
						}
						return str.substring(i1, i2);
					}
					case indexOf: {
						String index = args[0].toString();
						int i1 = Context.toLong(args[1]).intValue();
						String str = stringCall.string;
						int l = str.length();
						if (i1 > l) {
							i1 = l;
						}
						if (i1 < 0) {
							i1 = 0;
						}
						if (index.length() == 1) {
							return str.indexOf(index.charAt(0), i1);
						} else {
							return str.indexOf(index, i1);
						}
					}
					default:
						break;
				}
			}
		} else if (args.length == 1) {
			if (args[0] != null) {
				switch (stringCall.method) {
					case indexOf: {
						String index = args[0].toString();
						String str = stringCall.string;
						if (index.length() == 1) {
							return str.indexOf(index.charAt(0));
						} else {
							return str.indexOf(index);
						}
					}
					case endsWith: {
						String str = args[0].toString();
						return stringCall.string.endsWith(str);
					}
					case substring: {
						int i = Context.toLong(args[0]).intValue();
						String str = stringCall.string;
						int l = str.length();
						if (i < 0) {
							i = 0;
						}
						if (i > l) {
							return "";
						}
						return str.substring(i);
					}
					case startsWith: {
						String str = args[0].toString();
						return stringCall.string.startsWith(str);
					}
					case lastIndexOf: {
						String index = args[0].toString();
						String str = stringCall.string;
						if (index.length() == 1) {
							return str.lastIndexOf(index.charAt(0));
						} else {
							return str.lastIndexOf(index);
						}
					}
					default:
						break;
				}
			}
		}
		switch (stringCall.method) {
			case trim:
				return stringCall.string.trim();
			case toLowerCase:
				return stringCall.string.toLowerCase();
			case toUpperCase:
				return stringCall.string.toUpperCase();
			default:
				break;
		}
		return null;
	}

	public String[] supportedStaticCalls() {
		return new String[] { "toString", "chr" };
	}

	public Object staticCall(String method, Object[] args) {
		if ("toString".equals(method)) {
			StringBuilder buffer = new StringBuilder();
			for (Object obj : args) {
				buffer.append(obj);
			}
			return buffer.toString();

		} else if ("chr".equals(method)) {
			if (args.length == 1) {
				Long lc = Context.toLong(args[0]);
				char c = (lc == null) ? 0 : ((char) lc.longValue());
				return String.valueOf(c);
			}
		}
		return null;
	}
}