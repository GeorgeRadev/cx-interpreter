package cx.runtime;

import java.util.Calendar;

public class DateHandler implements ObjectHandler {
	private static enum DateMetod {
		getDate, getTime, getHours, getMinutes, getSeconds, getYear, getMonth, getDay, setDate, setTime, setHours, setMinutes, setYear, setMonth, setDay
	}

	private static class DateCall {
		final Calendar calendar;
		final DateMetod method;

		DateCall(Calendar calendar, DateMetod method) {
			this.calendar = calendar;
			this.method = method;
		}
	}

	public boolean accept(Object object) {
		return object instanceof Calendar;
	}

	public void set(Object object, String variable, Object value) {
		if (object instanceof Calendar) {
			Calendar calendar = (Calendar) object;
			if ("date".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			} else if ("time".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			} else if ("hours".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			} else if ("minutes".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			} else if ("seconds".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			} else if ("year".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			} else if ("month".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			} else if ("day".equals(variable)) {
				calendar.set(Calendar.DATE, 0);
			}
		}
	}

	public Object get(Object thiz, String variable) {
		if (thiz instanceof Calendar) {
			if ("date".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getDate);
			}
			if ("time".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getTime);
			}
			if ("hours".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getHours);
			}
			if ("minutes".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getMinutes);
			}
			if ("seconds".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getSeconds);
			}
			if ("year".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getYear);
			}
			if ("month".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getMonth);
			}
			if ("day".equals(variable)) {
				return new DateCall((Calendar) thiz, DateMetod.getDay);
			}
		}
		return null;
	}

	public Object call(Object object, Object[] args) {
		if (!(object instanceof DateCall)) {
			return null;
		}
		DateCall dateCall = (DateCall) object;
		switch (args.length) {
			case 0:
				switch (dateCall.method) {
					case getDate:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case getTime:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case getHours:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case getMinutes:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case getYear:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case getMonth:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case getDay:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
				}
				break;
			case 1:
				switch (dateCall.method) {
					case setDate:
						// return dateCall.calendar.set(Calendar.DATE,
						// args[0].toString());
					case setTime:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case setHours:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case setMinutes:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case setYear:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case setMonth:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
					case setDay:
						return new Integer(dateCall.calendar.get(Calendar.DATE));
				}
				break;
		}
		return null;
	}

	public boolean acceptStaticCall(String method, Object[] args) {
		// TODO Auto-generated method stub
		return args != null && (("newDate".equals(method) && args.length <= 2));
	}

	public Object staticCall(String method, Object[] args) {
		if ("newDate".equals(method)) {
			if (args.length == 0) {
				return Calendar.getInstance();
			} else if (args.length == 0) {
				Calendar calendar = Calendar.getInstance();
				return calendar;
			}
		}
		return null;
	}
}