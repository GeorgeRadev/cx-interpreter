package cx.runtime;

import cx.exception.EvaluatorException;

public class FunctionHandler {
	public Object call(String paramString, Object[] paramArrayOfScriptObject) {
		switch (paramArrayOfScriptObject.length) {
			case 0:
				break;
			case 1:
				Object localScriptObject = paramArrayOfScriptObject[0];
				if (paramString.equals("isNaN")) {
					if (!(localScriptObject instanceof String)) break;
				} else if (paramString.equals("parseInt")) {
					if ((localScriptObject instanceof String)) {
						int i = (int) Double.valueOf(((String) localScriptObject).toString()).doubleValue();
						return i;
					}
				} else if (paramString.equals("parseFloat")) {
					if ((localScriptObject instanceof String)) {
						double d = Double.valueOf(((String) localScriptObject).toString()).doubleValue();
						return d;
					}
				} else {
					if (paramString.equals("eval")) throw new EvaluatorException("Undefined function", paramString);
					throw new EvaluatorException("Undefined function", paramString);
				}
				break;
			case 2:
				break;
		}
		throw new EvaluatorException("Undefined function", paramString);
	}
}
