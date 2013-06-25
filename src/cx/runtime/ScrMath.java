package cx.runtime;

import cx.exception.EvaluatorException;

public class ScrMath {
	public Object callPropGet(String paramString) {
		throw new EvaluatorException("Undefined get property", paramString);
	}

	public Object callPropSet(String paramString, Object paramObject) {
		throw new EvaluatorException("Undefined set property");
	}

	public Object callMethod(String paramString, Object[] paramArrayOfObject) {
		try {
			switch (paramArrayOfObject.length) {
				case 0:
					if (paramString.equals("random")) return new Integer((int) Math.random());
					break;
				case 1:
					Integer localInteger1 = (Integer) paramArrayOfObject[0];
					if (paramString.equals("abs")) return new Integer(Math.abs(localInteger1.intValue()));
					if (paramString.equals("ceil")) return new Integer((int) Math.ceil(localInteger1.intValue()));
					if (paramString.equals("floor")) return new Integer((int) Math.floor(localInteger1.intValue()));
					if (paramString.equals("round")) return new Integer(Math.round(localInteger1.intValue()));
					if (paramString.equals("sqrt")) return new Integer((int) Math.sqrt(localInteger1.intValue()));
					break;
				case 2:
					Integer localInteger2 = (Integer) paramArrayOfObject[0];
					Integer localInteger3 = (Integer) paramArrayOfObject[1];
					if (paramString.equals("max"))
						return new Integer(Math.max(localInteger2.intValue(), localInteger3.intValue()));
					if (paramString.equals("min"))
						return new Integer(Math.min(localInteger2.intValue(), localInteger3.intValue()));
					break;
			}
		} catch (ClassCastException localClassCastException) {
			throw new EvaluatorException("Undefined class", paramString);
		}
		throw new EvaluatorException("Undefined method", paramString);
	}

	public void initialize(Object[] paramArrayOfObject) {
		throw new EvaluatorException("Undefined constructor", "constructor");
	}

	public Object cloneObject() {
		return null;
	}

	public boolean isEqual(Object paramObject) {
		return false;
	}

	public String stringValue() {
		return "";
	}
}