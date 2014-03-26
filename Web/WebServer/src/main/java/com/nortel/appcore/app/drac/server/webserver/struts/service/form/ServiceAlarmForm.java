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

package com.nortel.appcore.app.drac.server.webserver.struts.service.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.validator.ValidatorActionForm;

/**
 * Created on 14-Nov-06
 */
public final class ServiceAlarmForm extends ValidatorActionForm {
	private static final long serialVersionUID = 4650950430783107485L;
	// fields for result
	private String id = "";
	private String serviceId = "";
	private String severity = "";
	private String occurredTime = "";
	private long occurredTimeMillis;
	private boolean cleared;
	private String clearedTime = "";
	private long clearedTimeMillis;
	private String description = "";
	private String scheduleName = "";
	private String scheduleId = "";
	private double duration;
	private int result;

	// fields for query
	private String groupFilter = "";
	private List memberGroupList = new ArrayList();
	private String startTime = "";
	private String startDate = "";
	private String endTime = "";
	private String endDate = "";

	/**
	 * @return the clearedTime
	 */
	public String getClearedTime() {
		return clearedTime;
	}

	/**
	 * @return the clearedTimeMillis
	 */
	public long getClearedTimeMillis() {
		return clearedTimeMillis;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @return the groupFilter
	 */
	public String getGroupFilter() {
		return groupFilter;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the memberGroupList
	 */
	public List getMemberGroupList() {
		return memberGroupList;
	}

	/**
	 * @return the occurredTime
	 */
	public String getOccurredTime() {
		return occurredTime;
	}

	/**
	 * @return the occurredTimeMillis
	 */
	public long getOccurredTimeMillis() {
		return occurredTimeMillis;
	}

	/**
	 * @return the result
	 */
	public int getResult() {
		return result;
	}

	/**
	 * @return the webSafeScheduleName
	 */
	public String getScheduleId() {
		return scheduleId;
	}

	/**
	 * @return the scheduleName
	 */
	public String getScheduleName() {
		return scheduleName;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @return the cleared
	 */
	public boolean isCleared() {
		return cleared;
	}

	/**
	 * @param cleared
	 *          the cleared to set
	 */
	public void setCleared(boolean cleared) {
		this.cleared = cleared;
	}

	/**
	 * @param clearedTime
	 *          the clearedTime to set
	 */
	public void setClearedTime(String clearedTime) {
		this.clearedTime = clearedTime;
	}

	/**
	 * @param clearedTimeMillis
	 *          the clearedTimeMillis to set
	 */
	public void setClearedTimeMillis(long clearedTimeMillis) {
		this.clearedTimeMillis = clearedTimeMillis;
	}

	/**
	 * @param description
	 *          the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param duration
	 *          the duration to set
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * @param endDate
	 *          the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @param endTime
	 *          the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param groupFilter
	 *          the groupFilter to set
	 */
	public void setGroupFilter(String groupFilter) {
		this.groupFilter = groupFilter;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param memberGroupList
	 *          the memberGroupList to set
	 */
	public void setMemberGroupList(List memberGroupList) {
		this.memberGroupList = memberGroupList;
	}

	/**
	 * @param occurredTime
	 *          the occurredTime to set
	 */
	public void setOccurredTime(String occurredTime) {
		this.occurredTime = occurredTime;
	}

	/**
	 * @param occurredTimeMillis
	 *          the occurredTimeMillis to set
	 */
	public void setOccurredTimeMillis(long occurredTimeMillis) {
		this.occurredTimeMillis = occurredTimeMillis;
	}

	/**
	 * @param result
	 *          the result to set
	 */
	public void setResult(int result) {
		this.result = result;
	}

	/**
	 * @param webSafeScheduleName
	 *          the webSafeScheduleName to set
	 */
	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	/**
	 * @param scheduleName
	 *          the scheduleName to set
	 */
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	/**
	 * @param serviceId
	 *          the serviceId to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * @param severity
	 *          the severity to set
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @param startDate
	 *          the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @param startTime
	 *          the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

}
