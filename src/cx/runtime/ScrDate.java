package cx.runtime;

import java.util.Calendar;
import java.util.Date;
import cx.exception.EvaluatorException;

public class ScrDate {
	private Date date = null;

	public Object callPropGet(String paramString) {
		throw new EvaluatorException("Undefined get property", paramString);
	}

	public Object callPropSet(String paramString, Object paramObject) {
		throw new EvaluatorException("Undefined set property", paramString);
	}

	public Object callMethod(String paramString, Object[] paramArrayOfObject) {
		Calendar localCalendar = Calendar.getInstance();
		localCalendar.setTime(date);
		switch (paramArrayOfObject.length) {
			case 0:
				if (paramString.equals("getDate")) return new Integer(localCalendar.get(5));
				if (paramString.equals("getTime")) return new Integer(localCalendar.get(14));
				if (paramString.equals("getHours")) return new Integer(localCalendar.get(10));
				if (paramString.equals("getMinutes")) return new Integer(localCalendar.get(12));
				if (paramString.equals("getSeconds")) return new Integer(localCalendar.get(13));
				if (paramString.equals("getYear")) return new Integer(localCalendar.get(1));
				if (paramString.equals("getMonth")) return new Integer(localCalendar.get(2));
				if (paramString.equals("getDay")) return new Integer(localCalendar.get(7));
			case 1:
				if ((!paramString.equals("toGMTString")) && (!paramString.equals("toLocaleString"))
						&& (paramString.equals("getTimezoneOffset")) && (!paramString.equals("parse"))) {
					int i = (int) ((Integer) paramArrayOfObject[0]).intValue();
					if (paramString.equals("setDate")) localCalendar.set(5, i);
					else if (paramString.equals("setTime")) localCalendar.set(14, i);
					else if (paramString.equals("setHours")) localCalendar.set(10, i);
					else if (paramString.equals("setMinutes")) localCalendar.set(12, i);
					else if (paramString.equals("setSeconds")) localCalendar.set(13, i);
					else if (paramString.equals("setYear")) localCalendar.set(1, i);
					else if (paramString.equals("setMonth")) localCalendar.set(2, i);
					date.setTime(localCalendar.get(14));
				}
				break;
			case 2:
			case 3:
				if ((!paramString.equals("UTC")) || (!paramString.equals("UTC")))
				;
				break;
			case 4:
			case 5:
				if ((!paramString.equals("UTC")) || (paramString.equals("UTC")))
				;
				break;
		}
		throw new EvaluatorException("Undefined method", paramString);
	}

	public void initialize(Object[] paramArrayOfObject) {
		try {
			Calendar localCalendar = Calendar.getInstance();
			int i;
			int j;
			int k;
			switch (paramArrayOfObject.length) {
				case 0:
					return;
				case 1:
					if (!(paramArrayOfObject[0] instanceof String)) break;
					break;
				case 3:
					i = (int) ((Integer) paramArrayOfObject[0]).intValue();
					j = (int) ((Integer) paramArrayOfObject[1]).intValue();
					k = (int) ((Integer) paramArrayOfObject[2]).intValue();
					localCalendar.set(i, j, k);
					date.setTime(localCalendar.get(14));
					return;
				case 6:
					i = (int) ((Integer) paramArrayOfObject[0]).intValue();
					j = (int) ((Integer) paramArrayOfObject[1]).intValue();
					k = (int) ((Integer) paramArrayOfObject[2]).intValue();
					int m = (int) ((Integer) paramArrayOfObject[3]).intValue();
					int n = (int) ((Integer) paramArrayOfObject[4]).intValue();
					int i1 = (int) ((Integer) paramArrayOfObject[5]).intValue();
					localCalendar.set(i, j, k, m, n, i1);
					date.setTime(localCalendar.get(14));
					return;
				case 2:
				case 4:
				case 5:
			}
		} catch (ClassCastException localClassCastException) {}
		throw new EvaluatorException("Undefined method", "constructor");
	}

	public Object cloneObject() {
		try {
			Object localObject = (Object) clone();
			return localObject;
		} catch (CloneNotSupportedException localCloneNotSupportedException) {}
		return null;
	}

	public boolean isEqual(Object paramObject) {
		return ((paramObject instanceof ScrDate)) && (((ScrDate) paramObject).date.equals(date));
	}

	public String stringValue() {
		return date.toString();
	}

	public Date getValue() {
		return date;
	}

	public void setValue(Date paramDate) {
		date = paramDate;
	}
}