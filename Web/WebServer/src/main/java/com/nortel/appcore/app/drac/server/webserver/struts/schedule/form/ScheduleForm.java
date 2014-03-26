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

package com.nortel.appcore.app.drac.server.webserver.struts.schedule.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper;
import com.nortel.appcore.app.drac.server.webserver.struts.service.form.ServiceForm;

/**
 * Created on 24-Jul-06
 */
public final class ScheduleForm extends ValidatorForm {
	private static final long serialVersionUID = -6346493691618118761L;
	private boolean vcat;
	private String name = DracConstants.EMPTY_STRING;
	private String webSafeName = DracConstants.EMPTY_STRING;
	private String id = DracConstants.EMPTY_STRING;
	private String status = DracConstants.EMPTY_STRING;
	private boolean recurrence;
	private String rate = DracConstants.DASHES;
	private String protectionType = DracConstants.EMPTY_STRING;
	private String srcTNA = DracConstants.DASHES;
	private String srcFacLabel = DracConstants.DASHES;
	private String srcVlanId = DracConstants.EMPTY_STRING;
	private String destTNA = DracConstants.DASHES;
	private String destFacLabel = DracConstants.DASHES;
	private String destVlanId = DracConstants.EMPTY_STRING;
	private String srcCh = DracConstants.EMPTY_STRING;
	private String destCh = DracConstants.EMPTY_STRING;
	private String srlg = DracConstants.EMPTY_STRING;
	private List<String> srsg = new ArrayList<String>();

	private String startDate = DracConstants.EMPTY_STRING;
	private String endDate = DracConstants.EMPTY_STRING;
	private long startTimeMillis;
	private long endTimeMillis;
	private long duration;

	private String recurrenceType = DracConstants.EMPTY_STRING;
	private String firstOccurrence = DracConstants.EMPTY_STRING;
	private String lastOccurrence = DracConstants.EMPTY_STRING;

	// Day Field does double-duty for monthly and yearly
	private int[] weekDay = new int[0];
	private String recDay = DracConstants.EMPTY_STRING;
	private String recMonth = DracConstants.EMPTY_STRING;
	private ServiceForm[] services = new ServiceForm[0];

	private String userId = DracConstants.EMPTY_STRING;
	private String userGroup = DracConstants.EMPTY_STRING;

	private String error = "";
	private String createResult = "UNKNOWN";

	private boolean cancellable;
	private boolean expandable; // expired schedules cannot have services added to
	                            // it; ie not
	// "expandable"
	private boolean confirmable;
	private boolean activateableService;
	private boolean cancellableService;
	private String activationType = Schedule.ACTIVATION_TYPE.RESERVATION_AUTOMATIC
	    .name();

	public String getActivationType() {
		return activationType;
	}

	/**
	 * @return the createResult
	 */
	public String getCreateResult() {
		return createResult;
	}

	public String getDestCh() {
		return destCh;
	}

	public String getDestFacLabel() {
		return destFacLabel;
	}

	/**
	 * @return the destTNA
	 */
	public String getDestTNA() {
		return destTNA;
	}

	public String getDestVlanId() {
		if (FacilityConstants.UNTAGGED_LOCLBL_VALUE.equals(destVlanId)) {
			return FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}
		else if (FacilityConstants.ALLTAGGED_LOCLBL_VALUE.equals(destVlanId)) {
			return FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}

		return destVlanId;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @return the endTimeMillis
	 */
	public long getEndTimeMillis() {
		return endTimeMillis;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @return the firstOccurrence
	 */
	public String getFirstOccurrence() {
		return firstOccurrence;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the lastOccurrence
	 */
	public String getLastOccurrence() {
		return lastOccurrence;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public String getProtectionType() {
		return protectionType;
	}

	/**
	 * @return the rate
	 */
	public String getRate() {
		return rate;
	}

	/**
	 * @return the recDay
	 */
	public String getRecDay() {
		return recDay;
	}

	/**
	 * @return the recMonth
	 */
	public String getRecMonth() {
		return recMonth;
	}

	/**
	 * @return the recurrence
	 */
	public boolean getRecurrence() {
		return recurrence;
	}

	/**
	 * @return the recurrenceType
	 */
	public String getRecurrenceType() {
		return recurrenceType;
	}

	/**
	 * @return the serviceList
	 */
	public ServiceForm[] getServices() {
		return services;
	}

	public String getSrcCh() {
		return srcCh;
	}

	public String getSrcFacLabel() {
		return srcFacLabel;
	}

	/**
	 * @return the srcTNA
	 */
	public String getSrcTNA() {
		return srcTNA;
	}

	public String getSrcVlanId() {
		if (FacilityConstants.UNTAGGED_LOCLBL_VALUE.equals(srcVlanId)) {
			return FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}
		else if (FacilityConstants.ALLTAGGED_LOCLBL_VALUE.equals(srcVlanId)) {
			return FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}

		return srcVlanId;
	}

	public String getSrlg() {
		return srlg;
	}

	public List<String> getSrsg() {
		return srsg;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @return the startTimeMillis
	 */
	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the userGroup
	 */
	public String getUserGroup() {
		return userGroup;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	public String getWebSafeName() {
		return this.webSafeName;
	}

	/**
	 * @return the weekDay
	 */
	public int[] getWeekDay() {
		return weekDay;
	}

	public boolean isActivateableService() {
		return activateableService;
	}

	public boolean isCancellable() {
		return cancellable;
	}

	public boolean isCancellableService() {
		return cancellableService;
	}

	public boolean isConfirmable() {
		return confirmable;
	}

	public boolean isExpandable() {
		return expandable;
	}

	public boolean isVcat() {
		return vcat;
	}

	public void setActivateableService(boolean activateableService) {
		this.activateableService = activateableService;
	}

	public void setActivationType(String activationType) {
		this.activationType = activationType;
	}

	public void setCancellable(boolean cancellable) {
		this.cancellable = cancellable;
	}

	public void setCancellableService(boolean cancellableSerivce) {
		this.cancellableService = cancellableSerivce;
	}

	public void setConfirmable(boolean confirmable) {
		this.confirmable = confirmable;
	}

	/**
	 * @param createResult
	 *          the createResult to set
	 */
	public void setCreateResult(String createResult) {
		this.createResult = createResult;
	}

	public void setDestCh(String destCh) {
		this.destCh = destCh;
	}

	public void setDestFacLabel(String destFacLabel) {
		this.destFacLabel = destFacLabel;
	}

	/**
	 * @param destTNA
	 *          the destTNA to set
	 */
	public void setDestTNA(String destTNA) {
		this.destTNA = destTNA;
	}

	public void setDestVlanId(String destVlanId) {
		// maintain empty string on null
		if (destVlanId != null) {
			this.destVlanId = destVlanId;
		}
	}

	/**
	 * @param duration
	 *          the duration to set
	 */
	public void setDuration(long duration) {
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
	 * @param endTimeMillis
	 *          the endTimeMillis to set
	 */
	public void setEndTimeMillis(long endTimeMillis) {
		this.endTimeMillis = endTimeMillis;
	}

	/**
	 * @param error
	 *          the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	/**
	 * @param firstOccurrence
	 *          the firstOccurrence to set
	 */
	public void setFirstOccurrence(String firstOccurrence) {
		this.firstOccurrence = firstOccurrence;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param lastOccurrence
	 *          the lastOccurrence to set
	 */
	public void setLastOccurrence(String lastOccurrence) {
		this.lastOccurrence = lastOccurrence;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void setProtectionType(String protectionType) {
		this.protectionType = protectionType;
	}

	/**
	 * @param rate
	 *          the rate to set
	 */
	public void setRate(String rate) {
		this.rate = rate;
	}

	/**
	 * @param recDay
	 *          the recDay to set
	 */
	public void setRecDay(String recDay) {
		this.recDay = recDay;
	}

	/**
	 * @param recMonth
	 *          the recMonth to set
	 */
	public void setRecMonth(String recMonth) {
		this.recMonth = recMonth;
	}

	/**
	 * @param recurrence
	 *          the recurrence to set
	 */
	public void setRecurrence(boolean recurrence) {
		this.recurrence = recurrence;
	}

	/**
	 * @param recurrenceType
	 *          the recurrenceType to set
	 */
	public void setRecurrenceType(String recurrenceType) {
		this.recurrenceType = recurrenceType;
	}

	/**
	 * @param serviceList
	 *          the serviceList to set
	 */
	public void setServices(ServiceForm[] serviceList) {
		this.services = serviceList;
	}

	public void setSrcCh(String srcCh) {
		this.srcCh = srcCh;
	}

	public void setSrcFacLabel(String srcFacLabel) {
		this.srcFacLabel = srcFacLabel;
	}

	/**
	 * @param srcTNA
	 *          the srcTNA to set
	 */
	public void setSrcTNA(String srcTNA) {
		this.srcTNA = srcTNA;
	}

	public void setSrcVlanId(String srcVlanId) {
		// Maintain empty string on null
		if (srcVlanId != null) {
			this.srcVlanId = srcVlanId;
		}
	}

	public void setSrlg(String srlg) {
		this.srlg = srlg;
	}

	public void setSrsg(List<String> srsg) {
		this.srsg = srsg;
	}

	/**
	 * @param startDate
	 *          the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @param startTimeMillis
	 *          the startTimeMillis to set
	 */
	public void setStartTimeMillis(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	/**
	 * @param status
	 *          the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param userGroup
	 *          the userGroup to set
	 */
	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}

	/**
	 * @param userId
	 *          the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setVcat(boolean vcat) {
		this.vcat = vcat;
	}

	public void setWebSafeName(String webSafeName) throws Exception {
		this.webSafeName = DracHelper.encodeToUTF8(webSafeName);
	}

	/**
	 * @param weekDay
	 *          the weekDay to set
	 */
	public void setWeekDay(int[] weekDay) {
		this.weekDay = weekDay;
	}

}
