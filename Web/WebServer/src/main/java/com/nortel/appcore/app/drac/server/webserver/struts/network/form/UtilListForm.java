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

package com.nortel.appcore.app.drac.server.webserver.struts.network.form;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

public final class UtilListForm extends ValidatorForm {
	private static final long serialVersionUID = -3761378558905246453L;
	private String status = DracConstants.EMPTY_STRING;
	private String serviceID = DracConstants.EMPTY_STRING;
	private long startTimeMillis = 0;
	private long endTimeMillis = 0;
	private int rate = 0;
	private String scheduleId = DracConstants.EMPTY_STRING;
	private String scheduleName = DracConstants.EMPTY_STRING;
	private String startDateForList = DracConstants.EMPTY_STRING;
	private String endDateForList = DracConstants.EMPTY_STRING;

	public String getEndDateForList() {
		return endDateForList;
	}

	public long getEndTimeMillis() {
		return endTimeMillis;
	}

	public int getRate() {
		return rate;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public String getServiceID() {
		return serviceID;
	}

	public String getStartDateForList() {
		return startDateForList;
	}

	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	public String getStatus() {
		return status;
	}

	public void setEndDateForList(String endDateForList) {
		this.endDateForList = endDateForList;
	}

	public void setEndTimeMillis(long endTimeMillis) {
		this.endTimeMillis = endTimeMillis;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public void setStartDateForList(String startDateForList) {
		this.startDateForList = startDateForList;
	}

	public void setStartTimeMillis(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
