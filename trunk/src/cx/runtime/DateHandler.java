package cx.runtime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import cx.Context;

public class DateHandler implements ObjectHandler {
	public static enum DateMetod {
		year, month, day, hour, minute, second, millisecond, zone, time;

		public static DateMetod parse(final String str) {
			String guess = null;
			final int length = str.length();
			DateMetod method = null;

			switch (length) {

				case 3:
					if (str.charAt(0) == 'd') {
						guess = "day";
						method = day;
					}
					break;
				case 4:
					switch (str.charAt(0)) {
						case 'y':
							guess = "year";
							method = year;
							break;
						case 'h':
							guess = "hour";
							method = hour;
							break;
						case 'z':
							guess = "zone";
							method = zone;
							break;
						case 't':
							guess = "time";
							method = time;
							break;
					}
					break;
				case 5:
					if (str.charAt(0) == 'm') {
						guess = "month";
						method = month;
					}
					break;
				case 6:
					if (str.charAt(0) == 'm') {
						guess = "minute";
						method = minute;
					} else if (str.charAt(0) == 's') {
						guess = "second";
						method = second;
					}
					break;
				case 11:
					if (str.charAt(0) == 'm') {
						guess = "millisecond";
						method = millisecond;
					}
					break;
			}
			if ((guess != null) && !guess.equals(str)) {
				return null;
			}
			return method;
		}
	}
	TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("GMT");

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
			Long l = Context.toLong(value);
			if (l == null) {
				return;
			}
			DateMetod method = DateMetod.parse(variable);
			if (method != null) {
				switch (method) {
					case year:
						calendar.set(Calendar.DATE, l.intValue());
						break;
					case month:
						calendar.set(Calendar.MONTH, l.intValue());
						break;
					case day:
						calendar.set(Calendar.DAY_OF_MONTH, l.intValue());
						break;
					case hour:
						calendar.set(Calendar.HOUR_OF_DAY, l.intValue());
						break;
					case minute:
						calendar.set(Calendar.MINUTE, l.intValue());
						break;
					case second:
						calendar.set(Calendar.SECOND, l.intValue());
						break;
					case millisecond:
						calendar.set(Calendar.MILLISECOND, l.intValue());
						break;
					case zone:
						calendar.set(Calendar.ZONE_OFFSET, l.intValue());
						break;
					case time:
						calendar.setTimeInMillis(l.intValue());
						break;
				}
			}
		}
	}

	public Object get(Object thiz, String variable) {
		if (thiz instanceof Calendar) {
			DateMetod method = DateMetod.parse(variable);
			if (method != null) {
				return new DateCall((Calendar) thiz, method);
			}
		}
		return null;
	}

	public Object call(Object object, Object[] args) {
		if (!(object instanceof DateCall)) {
			return null;
		}

		DateCall dateCall = (DateCall) object;
		if (args.length == 0) {
			Long l = Context.toLong(args[0]);
			if (l == null) {
				return null;
			}
			long result = 0;
			switch (dateCall.method) {
				case year:
					result = dateCall.calendar.get(Calendar.YEAR);
					break;
				case month:
					result = dateCall.calendar.get(Calendar.MONTH);
					break;
				case day:
					result = dateCall.calendar.get(Calendar.DAY_OF_MONTH);
					break;
				case hour:
					result = dateCall.calendar.get(Calendar.HOUR_OF_DAY);
					break;
				case minute:
					result = dateCall.calendar.get(Calendar.MINUTE);
					break;
				case second:
					result = dateCall.calendar.get(Calendar.SECOND);
					break;
				case millisecond:
					result = dateCall.calendar.get(Calendar.MILLISECOND);
					break;
				case zone:
					result = dateCall.calendar.get(Calendar.ZONE_OFFSET);
					break;
				case time:
					result = dateCall.calendar.getTimeInMillis();
					break;
			}
			return result;
		}
		return null;
	}

	public boolean acceptStaticCall(String method, Object[] args) {
		return args != null
				&& (("newDate".equals(method) && args.length <= 2) || ("formatDate".equals(method) && args.length == 2));
	}

	public Object staticCall(String method, Object[] args) {
		if ("newDate".equals(method)) {
			if (args.length == 0) {
				// newDate() return current Calendar
				return Calendar.getInstance(UTC_TIMEZONE);
			} else if (args.length == 1) {
				if (args[0] instanceof Calendar) {
					// newDate(calendar) return new instance of Calendar
					return ((Calendar) args[0]).clone();
				} else {
					return null;
				}
			} else if (args.length == 2) {
				// newDate(string,format) try to parse str with format
				try {
					SimpleDateFormat formater = new SimpleDateFormat("" + args[1]);
					Calendar calendar = Calendar.getInstance(UTC_TIMEZONE);
					calendar.setTimeInMillis(formater.parse("" + args[0]).getTime());
					return calendar;
				} catch (Exception e) {
					return null;
				}
			}
		} else if ("formatDate".equals(method) && args.length == 2 && args[0] instanceof Calendar) {
			// formatDate(calendar ,format) try to parse str with format
			try {
				SimpleDateFormat formater = new SimpleDateFormat("" + args[1]);
				return formater.format(((Calendar) args[0]).getTime());
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}