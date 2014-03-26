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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public final class RecurrenceType implements Serializable {
	public enum RecurrenceFreq {
		FREQ_ONCE("Once"), FREQ_DAILY("Daily"), FREQ_WEEKLY("Weekly"), FREQ_MONTHLY(
		    "Monthly"), FREQ_YEARLY("Yearly");

		private final String s;

		RecurrenceFreq(String t) {
			s = t;
		}

		public static RecurrenceFreq parseString(String t) throws Exception {

			if (FREQ_ONCE.toString().equalsIgnoreCase(t)) {
				return RecurrenceFreq.FREQ_ONCE;
			}
			else if (FREQ_DAILY.toString().equalsIgnoreCase(t)) {
				return RecurrenceFreq.FREQ_DAILY;
			}
			else if (FREQ_WEEKLY.toString().equalsIgnoreCase(t)) {
				return RecurrenceFreq.FREQ_WEEKLY;
			}
			else if (FREQ_MONTHLY.toString().equalsIgnoreCase(t)) {
				return RecurrenceFreq.FREQ_MONTHLY;
			}
			else if (FREQ_YEARLY.toString().equalsIgnoreCase(t)) {
				return RecurrenceFreq.FREQ_YEARLY;
			}
			throw new Exception("Cannot convert <" + t + "> into a RecurrenceFreq");
		}

		@Override
		public String toString() {
			return s;
		}
	}

	private static final long serialVersionUID = 1;

	private final RecurrenceFreq type;
	/**
	 * Specify the day of the month in which the service will be occurred. The
	 * value is between <em>1 to 31</em>
	 */
	private final int day;

	/**
	 * Specify the month in which the service will be occurred. The value is
	 * between <em>1 to 12</em>
	 */
	private int month;

	/**
	 * Specify the day(s) in which the service will be occurred. This field is
	 * only valid when the service's type is <em>Weekly</em> . By default, Sunday
	 * if the first day of the week .
	 */
	private final int[] weekDay;

	public RecurrenceType() {
		this(RecurrenceFreq.FREQ_ONCE, 0, 0, null);
	}

	public RecurrenceType(RecurrenceFreq freq, int whichDay, int whichMonth,
	    int[] whichWeekdays) {
		type = freq;
		day = whichDay;
		month = whichMonth;
		weekDay = whichWeekdays;
	}

	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @return the type
	 */
	public RecurrenceFreq getType() {
		return type;
	}

	/**
	 * @return the weekDay
	 */
	public int[] getWeekDay() {
		return weekDay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RecurrenceType [type=");
		builder.append(type);
		builder.append(", day=");
		builder.append(day);
		builder.append(", month=");
		builder.append(month);
		builder.append(", weekDay=");
		builder.append(Arrays.toString(weekDay));
		builder.append("]");
		return builder.toString();
	}

}
