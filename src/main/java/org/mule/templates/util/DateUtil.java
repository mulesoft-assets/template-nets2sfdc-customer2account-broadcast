package org.mule.templates.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

	public static String applyTimeZone(Date date, String format, String timeZoneOffset) {
		
		DateFormat formatter = new SimpleDateFormat(format, Locale.US);				
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneOffset);
		formatter.setTimeZone(timeZone);
		
		return formatter.format(date);
	}
}
