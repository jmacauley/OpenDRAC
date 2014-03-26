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

package com.nortel.appcore.app.drac.server.webserver.struts.security.form;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on Oct 26, 2006
 */
public final class RuleForm extends ValidatorForm {
	private static final long serialVersionUID = 6199523009651031841L;
	// system access rule fields
	private int[] months = {};
	private int[] days = {};
	private int[] dayOfWeek = {};
	private String startTime;
	private String endTime;
	private String permission = DracConstants.PERMISSION_GRANT_STATE;

	// access control rule fields
	private String key = "";
	private String value = "";

	/**
	 * @return the dayOfWeek
	 */
	public int[] getDayOfWeek() {
		return dayOfWeek;
	}

	/**
	 * @return the days
	 */
	public int[] getDays() {
		return days;
	}

	/**
	 * @return the toTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the months
	 */
	public int[] getMonths() {
		return months;
	}

	/**
	 * @return the permission
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * @return the fromTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param dayOfWeek
	 *          the dayOfWeek to set
	 */
	public void setDayOfWeek(int[] dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * @param days
	 *          the days to set
	 */
	public void setDays(int[] days) {
		this.days = days;
	}

	/**
	 * @param toTime
	 *          the toTime to set
	 */
	public void setEndTime(String toTime) {
		this.endTime = toTime;
	}

	/**
	 * @param key
	 *          the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @param months
	 *          the months to set
	 */
	public void setMonths(int[] months) {
		this.months = months;
	}

	/**
	 * @param permission
	 *          the permission to set
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * @param fromTime
	 *          the fromTime to set
	 */
	public void setStartTime(String fromTime) {
		this.startTime = fromTime;
	}

	/**
	 * @param value
	 *          the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
