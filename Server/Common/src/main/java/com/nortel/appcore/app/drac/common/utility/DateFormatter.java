/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.common.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateFormatter {
	public static final String COMMON_DATETIME_FORMAT = "EE MMM dd, yyyy hh:mm:ss aaa 'GMT'Z";

	// Not sure how Java supports the Z part yet.
	// i.e. user lastModifiedDate="2002-07-29T09:30:10"
	// creationDate="2002-06-30T09:30:10"
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String TIME_OF_DAY_RANGE = "HH:mm:ss";
	private static final String GUI_DATE_PATTERN = "EEE, dd MMM yyyy";
	private static final String GUI_TIME_PATTERN = "h:mm:ss aaa 'GMT'Z";
	private static DateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
	private static DateFormat timeOfDayFormatter = new SimpleDateFormat(
	    TIME_OF_DAY_RANGE);
	// private static DateFormat guiDateFormat = new
	// SimpleDateFormat(GUI_DATE_PATTERN);
	// private static DateFormat guiTimeFormat = new
	// SimpleDateFormat(GUI_TIME_PATTERN);
	private static String[] timeZoneNames;

	private DateFormatter() {
		super();
	}

	public static String dateToString(Calendar date) {
		// return formatter.format(date.getTime());
		return DateFormatter.dateToISO8601String(date);
	}

	public static Calendar getDateFromString(String dateStr) throws Exception {
		/*
		 * formatter.parse(dateStr); return (Calendar)
		 * formatter.getCalendar().clone();
		 */

		return DateFormatter.getDateFromISO8601String(dateStr);

	}

	/**
	 * Returns an array of timezones formatted with GMT and Id e.g. (GMT-05:00)
	 * Canada/Eastern (GMT-05:00) America/New York (GMT-08:00) America/Los Angeles
	 */
	public static String[] getGuiTimeZoneNames() {
		String[] ids = TimeZone.getAvailableIDs();
		DateFormatter.timeZoneNames = new String[ids.length];
		boolean negative = false;
		String gmt;
		int hrs;
		int mins;
		Date d = new Date();
		TimeZone tz = null;
		for (int i = 0; i < ids.length; i++) {
			tz = TimeZone.getTimeZone(ids[i]);
			mins = tz.getRawOffset() / 1000 / 60;
			negative = mins < 0;
			hrs = Math.abs(mins) / 60;
			if (tz.inDaylightTime(d)) {
				if (negative) {
					hrs--; // GMT-8 becomes GMT-7
				}
				else {
					hrs++; // GMT+2 becomes GMT+3;
				}
			}
			mins = Math.abs(mins) % 60;
			gmt = "GMT" + (negative ? "-" : "+") + (hrs < 10 ? "0" : "") + hrs + ":"
			    + (mins < 10 ? "0" : "") + mins;
			DateFormatter.timeZoneNames[i] = "(" + gmt + ") "
			    + tz.getID().replace('_', ' ');
		}

		// method exposes internal representation by returning array, therefore I do
		// this array copy
		final String[] result = new String[DateFormatter.timeZoneNames.length];
		System.arraycopy(DateFormatter.timeZoneNames, 0, result, 0,
		    Math.min(DateFormatter.timeZoneNames.length, result.length));
		return result;
	}

	public static Calendar getTimeOfDayRangeFromString(String dateStr)
	    throws Exception {

		if (dateStr == null) {
			return null;
		}

		timeOfDayFormatter.parse(dateStr);
		return (Calendar) timeOfDayFormatter.getCalendar().clone();
	}

	public static String getTimeZoneDisplayName(TimeZone tz) {
		String[] timeZoneIds = TimeZone.getAvailableIDs();
		String[] timeZoneNames = DateFormatter.getGuiTimeZoneNames();
		String timeZone = tz.getID();
		for (int i = 0; i < timeZoneIds.length; i++) {
			if (timeZoneIds[i].equals(timeZone)) {
				return timeZoneNames[i];
			}
		}

		return tz.getDisplayName();
	}

	public static String guiDateToString(Calendar date, Locale locale, TimeZone tz) {
		if (date == null) {
			return null;
		}
		DateFormat guiDateFormat = new SimpleDateFormat(GUI_DATE_PATTERN, locale);
		guiDateFormat.setTimeZone(tz);
		return guiDateFormat.format(date.getTime());
	}

	public static String guiTimeToString(Calendar date, Locale locale, TimeZone tz) {
		DateFormat guiTimeFormat = new SimpleDateFormat(GUI_TIME_PATTERN, locale);
		guiTimeFormat.setTimeZone(tz);
		return guiTimeFormat.format(date.getTime());
	}

	public static String timeOfDayRangeToString(Calendar date) {
		return timeOfDayFormatter.format(date.getTime());
	}

	private static String dateToISO8601String(Calendar date) {
		String dateStr = formatter.format(date.getTime());

		// convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
		// - note the added colon for the Timezone

		int indexQuote = dateStr.length() - 3;

		String result = dateStr;
		if (dateStr.charAt(indexQuote) != ':') {
			result = dateStr.substring(0, indexQuote + 1) + ":"
			    + dateStr.substring(indexQuote + 1);
		}

		return result;
	}


	private static Calendar getDateFromISO8601String(String dateStr)
	    throws Exception {
		int indexQuote = dateStr.length() - 3;

		String result = dateStr;

		if (dateStr.charAt(indexQuote) == ':') {
			result = dateStr.substring(0, indexQuote)
			    + dateStr.substring(indexQuote + 1);
		}

		formatter.parse(result);
		return (Calendar) formatter.getCalendar().clone();
	}

}
