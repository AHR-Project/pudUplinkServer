package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeZoneUtil {
	static {
		GregorianCalendar cal = new GregorianCalendar();
		timezoneOffset = -(cal.get(Calendar.ZONE_OFFSET)
		/* + cal .get(Calendar.DST_OFFSET) */);
	}

	private static final long timezoneOffset;

	/**
	 * @return the timezoneOffset
	 */
	public static final long getTimezoneOffset() {
		return timezoneOffset;
	}
}
