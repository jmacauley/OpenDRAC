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

package com.nortel.appcore.app.drac.common.security.policy.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class AccessRule extends AbstractRule {

	public static class TimeOfDayRange implements Serializable {
		private static final long serialVersionUID = 1L;
		public static final String TIMEOFDAYRANGE_ELEMENT = "timeOfDayRange";
		public static final String TIMEOFDAYRANGE_START_ATTR = "start";
		public static final String TIMEOFDAYRANGE_END_ATTR = "end";

		private Calendar start;
		private Calendar end;

		public TimeOfDayRange(Calendar start, Calendar end) {
			super();
			this.start = start;
			this.end = end;
		}

		public void fromXML(Element root) throws Exception {
			String startStr = root.getChildText(TIMEOFDAYRANGE_START_ATTR);
			String endStr = root.getChildText(TIMEOFDAYRANGE_END_ATTR);

			this.start = DateFormatter.getTimeOfDayRangeFromString(startStr);
			this.end = DateFormatter.getTimeOfDayRangeFromString(endStr);
		}

		public Calendar getEnd() {
			return end;
		}

		public Calendar getStart() {
			return start;
		}

		public void setEnd(Calendar end) {
			this.end = end;
		}

		public void setStart(Calendar start) {
			this.start = start;
		}

		@Override
		public String toString() {
			return "TimeOfDayRange(start=" + this.start + "/end=" + this.end + ")";
		}

		public String toXMLString() {
			StringBuilder buf = new StringBuilder();
			buf.append("<");
			buf.append(TIMEOFDAYRANGE_ELEMENT);
			buf.append(">");

			if (this.start != null) {
				buf.append("\n");
				buf.append(XmlUtility.toXMLString(TIMEOFDAYRANGE_START_ATTR,
				    DateFormatter.timeOfDayRangeToString(this.start)));
			}

			if (this.end != null) {
				buf.append("\n");
				buf.append(XmlUtility.toXMLString(TIMEOFDAYRANGE_END_ATTR,
				    DateFormatter.timeOfDayRangeToString(this.end)));
			}

			buf.append("\n");
			buf.append("</");
			buf.append(TIMEOFDAYRANGE_ELEMENT);
			buf.append(">");
			return buf.toString();
		}
	}

	private static final long serialVersionUID = 1L;
	public static final String SYSTEMACCESSRULE_ELEMENT = "systemAccessRule";

	public static final String RESOURCEACCESSRULE_ELEMENT = "resourceAccessRule";
	public static final String MONTH_ELEMENT = "month";
	public static final String DAYOFMONTH_ELEMENT = "dayOfMonth";

	public static final String DAYOFWEEK_ELEMENT = "dayOfWeek";
	private AccessPermission permission;
	private List<Integer> month;
	private List<Integer> dayOfMonth;
	private List<Integer> dayOfWeek;
	private List<TimeOfDayRange> timeOfDayRangeList;

	private String accessRuleElementString = SYSTEMACCESSRULE_ELEMENT;

	public AccessRule(AccessPermission permission) {
		super();
		this.permission = permission;
		this.month = new ArrayList<Integer>();
		this.dayOfMonth = new ArrayList<Integer>();
		this.dayOfWeek = new ArrayList<Integer>();
		this.timeOfDayRangeList = new ArrayList<TimeOfDayRange>();
	}

	// public void addDayOfMonth(Integer time)
	// {
	// this.dayOfMonth.add(time);
	// }

	// public void addDayOfWeek(Integer time)
	// {
	// this.dayOfWeek.add(time);
	// }

	// public void addMonth(Integer month)
	// {
	// this.month.add(month);
	// }

	// public void addTimeOfDayRange(TimeOfDayRange time)
	// {
	// this.timeOfDayRangeList.add(time);
	// }

	@SuppressWarnings("unchecked")
	@Override
	public void fromXML(Element node) throws Exception {

		/*********************************************************************/
		/* Constructs authenticationData attribute. */
		/*********************************************************************/
		// Element accessRule = node.getChild(SYSTEMACCESSRULE_ELEMENT);
		if (node == null) {
			return;
		}

		super.fromXML(node);

		this.permission = AccessPermission.fromXMLToObject(node);

		List<Element> temp = node.getChildren(MONTH_ELEMENT);
		String text = null;
		if (temp != null) {
			for (Element element : temp) {
				text = element.getTextTrim();
				if (!text.equals("")) {
					this.month.add(Integer.parseInt(text));
				}
			}
		}

		temp = node.getChildren(DAYOFMONTH_ELEMENT);
		if (temp != null) {
			for (Element element : temp) {
				text = element.getTextTrim();
				if (!text.equals("")) {
					this.dayOfMonth.add(Integer.parseInt(text));
				}
			}
		}

		temp = node.getChildren(DAYOFWEEK_ELEMENT);
		if (temp != null) {
			for (Element element : temp) {
				text = element.getTextTrim();
				if (!text.equals("")) {
					this.dayOfWeek.add(Integer.parseInt(text));
				}
			}
		}

		this.timeOfDayRangeList = getTimeFromXML(node);

		// 
	}

	public String getAccessRuleElementString() {
		return accessRuleElementString;
	}

	public List<Integer> getDayOfMonth() {
		return dayOfMonth;
	}

	public List<Integer> getDayOfWeek() {
		return dayOfWeek;
	}

	public List<Integer> getMonth() {
		return month;
	}

	public AccessPermission getPermission() {
		return permission;
	}

	public List<TimeOfDayRange> getTimeOfDayRange() {
		return timeOfDayRangeList;
	}

	public boolean isEmpty() {

		if (!timeOfDayRangeList.isEmpty()) {
			return false;
		}
		if (!dayOfWeek.isEmpty()) {
			return false;
		}
		if (!dayOfMonth.isEmpty()) {
			return false;
		}
		if (!month.isEmpty()) {
			return false;
		}

		return true;
	}

	public void setAccessRuleElementString(String accessRuleElementString) {
		this.accessRuleElementString = accessRuleElementString;
	}

	public void setDayOfMonth(List<Integer> dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public void setDayOfWeek(List<Integer> dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public void setMonth(List<Integer> month) {
		this.month = month;
	}

	public void setTimeOfDayRange(List<TimeOfDayRange> timeOfDayRanges) {
		this.timeOfDayRangeList = timeOfDayRanges;
	}

	// public void setPermission(AccessPermission permission)
	// {
	// this.permission = permission;
	// }

	@Override
	public String toString() {
		return toXMLString();
	}

	@Override
	public String toXMLString() {
		StringBuilder buf = new StringBuilder(20);
		buf.append("<");
		buf.append(getAccessRuleElementString());
		buf.append(super.toXMLString());
		buf.append(">");

		buf.append("\n");
		buf.append(this.permission.toXMLString());

		for (Integer integer : this.month) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(MONTH_ELEMENT, integer));
		}

		for (Integer integer : this.dayOfMonth) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(DAYOFMONTH_ELEMENT, integer));
		}

		for (Integer integer : this.dayOfWeek) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(DAYOFWEEK_ELEMENT, integer));
		}

		for (TimeOfDayRange timeOfDayRange : this.timeOfDayRangeList) {
			buf.append("\n");
			buf.append(timeOfDayRange.toXMLString());
		}

		buf.append("\n");
		buf.append("</");
		buf.append(getAccessRuleElementString());
		buf.append(">");
		return buf.toString();
	}

	private List<TimeOfDayRange> getTimeFromXML(Element root) throws Exception {

		List<TimeOfDayRange> timeList = new ArrayList<TimeOfDayRange>();
		@SuppressWarnings("unchecked")
		List<Element> accessRules = root
		    .getChildren(TimeOfDayRange.TIMEOFDAYRANGE_ELEMENT);

		if (accessRules == null) {
			return timeList;
		}

		TimeOfDayRange time = null;
		for (Element element : accessRules) {
			time = new TimeOfDayRange(null, null);
			time.fromXML(element);
			if (time.start != null && time.end != null) {
				timeList.add(time);
			}
			else {
				log.error("Invalid timeOfDayRange ...");
			}
		}

		return timeList;
	}

}
