package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeZoneUtil {
	private static final long timezoneOffset;

	static {
		GregorianCalendar cal = new GregorianCalendar();
		timezoneOffset = -(cal.get(Calendar.ZONE_OFFSET));
	}

	/**
	 * @return the timezoneOffset
	 */
	public static final long getTimezoneOffset() {
		return timezoneOffset;
	}
}
