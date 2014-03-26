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
import java.util.HashSet;
import java.util.List;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

public final class ServiceForm extends ValidatorForm {
	private static final long serialVersionUID = 4728751085494915144L;
	private List memberGroupList = new ArrayList();
	private String memberGroup = DracConstants.EMPTY_STRING;
	private String startdate = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String startDateForList = DracConstants.EMPTY_STRING;
	private String endDateForList = DracConstants.EMPTY_STRING;
	private HashSet srcLayerList = new HashSet();
	private String srcLayer = DracConstants.EMPTY_STRING;
	private HashSet destLayerList = new HashSet();
	private String destLayer = DracConstants.EMPTY_STRING;
	private HashSet srcTNAList = new HashSet();
	private String srcTNA = DracConstants.EMPTY_STRING;
	private HashSet destTNAList = new HashSet();
	private String destTNA = DracConstants.EMPTY_STRING;
	private List scheduleNameList = new ArrayList();
	private List statusList = new ArrayList();
	private String status = DracConstants.EMPTY_STRING;
	private String userName = DracConstants.EMPTY_STRING;
	private String serviceID = DracConstants.EMPTY_STRING;
	private String selectedID = DracConstants.EMPTY_STRING;
	private long startTimeMillis = 0;
	private long endTimeMillis = 0;
	private int rate = 0;
	private CallForm[] calls = new CallForm[0];
	private String scheduleId = DracConstants.EMPTY_STRING;
	private String scheduleSrcLayer = DracConstants.EMPTY_STRING;
	private String scheduleDestLayer = DracConstants.EMPTY_STRING;
	private String scheduleSrcTNA = DracConstants.EMPTY_STRING;
	private String scheduleSrcFacLabel = DracConstants.EMPTY_STRING;
	private String scheduleSrcVlanId = DracConstants.EMPTY_STRING;
	private String scheduleDestTNA = DracConstants.EMPTY_STRING;
	private String scheduleDestFacLabel = DracConstants.EMPTY_STRING;
	private String scheduleDestVlanId = DracConstants.EMPTY_STRING;
	private String scheduleStartdate = DracConstants.EMPTY_STRING;
	private String scheduleEnddate = DracConstants.EMPTY_STRING;
	private String scheduleName = DracConstants.EMPTY_STRING;
	private String scheduleStatus = DracConstants.EMPTY_STRING;
	private String scheduleActivationType = Schedule.ACTIVATION_TYPE.RESERVATION_AUTOMATIC
	    .name();
	private int scheduleRate = 0;
	private boolean cancellable = false;

	// remove
	private boolean activateable = false;

	// For Add Service
	private String parentSchid = DracConstants.EMPTY_STRING;
	private String addStartTime = DracConstants.EMPTY_STRING;
	private String addEndTime = DracConstants.EMPTY_STRING;
	private String addStartDate = DracConstants.EMPTY_STRING;
	private String addEndDate = DracConstants.EMPTY_STRING;
	private String addRate = DracConstants.EMPTY_STRING;
	private String email = DracConstants.EMPTY_STRING;
	private String srlg = DracConstants.EMPTY_STRING;
	private ArrayList srsg = new ArrayList();
	private String srlgInclusions = DracConstants.EMPTY_STRING;
	private int cost = 0;
	private int metric = 0;
	private int hop = 0;
	private List routingAlgorithmList = new ArrayList();
	private String routingAlgorithm = DracConstants.EMPTY_STRING;
	private List routingMetricList = new ArrayList();
	private String routingMetric = DracConstants.EMPTY_STRING;
	private List callList = new ArrayList();
	private int resultNum = 0;
	private String message = "";

	public String getAddEndDate() {
		return addEndDate;
	}

	public String getAddEndTime() {
		return addEndTime;
	}

	public String getAddRate() {
		return addRate;
	}

	public String getAddStartDate() {
		return addStartDate;
	}

	public String getAddStartTime() {
		return addStartTime;
	}

	/**
	 * @return the callList
	 */
	public List getCallList() {
		return callList;
	}

	/**
	 * @return the calls
	 */
	public CallForm[] getCalls() {
		return calls;
	}

	public int getCost() {
		return cost;
	}

	public String getDestLayer() {
		return destLayer;
	}

	public HashSet getDestLayerList() {
		return destLayerList;
	}

	public String getDestTNA() {
		return destTNA;
	}

	public HashSet getDestTNAList() {
		return destTNAList;
	}

	public String getEmail() {
		return email;
	}

	public String getEnddate() {
		return enddate;
	}

	public String getEndDateForList() {
		return endDateForList;
	}

	/**
	 * @return the endTimeMillis
	 */
	public long getEndTimeMillis() {
		return endTimeMillis;
	}

	public int getHop() {
		return hop;
	}

	public String getMemberGroup() {
		return memberGroup;
	}

	public List getMemberGroupList() {
		return memberGroupList;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	public int getMetric() {
		return metric;
	}

	public String getParentSchid() {
		return parentSchid;
	}

	/**
	 * @return the rate
	 */
	public int getRate() {
		return rate;
	}

	/**
	 * @return the resultNum
	 */
	public int getResultNum() {
		return resultNum;
	}

	public String getRoutingAlgorithm() {
		return routingAlgorithm;
	}

	public List getRoutingAlgorithmList() {
		return routingAlgorithmList;
	}

	public String getRoutingMetric() {
		return routingMetric;
	}

	public List getRoutingMetricList() {
		return routingMetricList;
	}

	public String getScheduleActivationType() {
		return scheduleActivationType;
	}

	public String getScheduleDestFacLabel() {
		return scheduleDestFacLabel;
	}

	/**
	 * @return the scheduleDestLayer
	 */
	public String getScheduleDestLayer() {
		return scheduleDestLayer;
	}

	/**
	 * @return the scheduleDestTNA
	 */
	public String getScheduleDestTNA() {
		return scheduleDestTNA;
	}

	public String getScheduleDestVlanId() {
		if (FacilityConstants.UNTAGGED_LOCLBL_VALUE.equals(scheduleDestVlanId)) {
			return FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}
		else if (FacilityConstants.ALLTAGGED_LOCLBL_VALUE
		    .equals(scheduleDestVlanId)) {
			return FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}

		return scheduleDestVlanId;
	}

	/**
	 * @return the scheduleEnddate
	 */
	public String getScheduleEnddate() {
		return scheduleEnddate;
	}

	/**
	 * @return the scheduleId
	 */
	public String getScheduleId() {
		return scheduleId;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public List getScheduleNameList() {
		return scheduleNameList;
	}

	/**
	 * @return the scheduleRate
	 */
	public int getScheduleRate() {
		return scheduleRate;
	}

	public String getScheduleSrcFacLabel() {
		return scheduleSrcFacLabel;
	}

	/**
	 * @return the scheduleSrcLayer
	 */
	public String getScheduleSrcLayer() {
		return scheduleSrcLayer;
	}

	/**
	 * @return the scheduleSrcTNA
	 */
	public String getScheduleSrcTNA() {
		return scheduleSrcTNA;
	}

	public String getScheduleSrcVlanId() {
		if (FacilityConstants.UNTAGGED_LOCLBL_VALUE.equals(scheduleSrcVlanId)) {
			return FacilityConstants.UNTAGGED_LOCLBL_FLAG;
		}
		else if (FacilityConstants.ALLTAGGED_LOCLBL_VALUE.equals(scheduleSrcVlanId)) {
			return FacilityConstants.ALLTAGGED_LOCLBL_FLAG;
		}

		return scheduleSrcVlanId;
	}

	/**
	 * @return the scheduleStartdate
	 */
	public String getScheduleStartdate() {
		return scheduleStartdate;
	}

	/**
	 * @return the scheduleStatus
	 */
	public String getScheduleStatus() {
		return scheduleStatus;
	}

	public String getSelectedID() {
		return selectedID;
	}

	public String getServiceID() {
		return serviceID;
	}

	public String getSrcLayer() {
		return srcLayer;
	}

	public HashSet getSrcLayerList() {
		return srcLayerList;
	}

	public String getSrcTNA() {
		return srcTNA;
	}

	public HashSet getSrcTNAList() {
		return srcTNAList;
	}

	public String getSrlg() {
		return srlg;
	}

	public String getSrlgInclusions() {
		return srlgInclusions;
	}

	public ArrayList getSrsg() {
		return srsg;
	}

	public String getStartdate() {
		return startdate;
	}

	public String getStartDateForList() {
		return startDateForList;
	}

	/**
	 * @return the startTimeMillis
	 */
	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	public String getStatus() {
		return status;
	}

	public List getStatusList() {
		return statusList;
	}

	public String getUserName() {
		return userName;
	}

	// remove
	public boolean isActivateable() {
		return activateable;
	}

	public boolean isCancellable() {
		return cancellable;
	}

	public void resetValues() {

	}

	// remove
	public void setActivateable(boolean activateable) {
		this.activateable = activateable;
	}

	public void setAddEndDate(String addEndDate) {
		this.addEndDate = addEndDate;
	}

	public void setAddEndTime(String addEndTime) {
		this.addEndTime = addEndTime;
	}

	public void setAddRate(String addRate) {
		this.addRate = addRate;
	}

	public void setAddStartDate(String addStartDate) {
		this.addStartDate = addStartDate;
	}

	public void setAddStartTime(String addStartTime) {
		this.addStartTime = addStartTime;
	}

	/**
	 * @param callList
	 *          the callList to set
	 */
	public void setCallList(List callList) {
		this.callList = callList;
	}

	/**
	 * @param calls
	 *          the calls to set
	 */
	public void setCalls(CallForm[] calls) {
		this.calls = calls;
	}

	public void setCancellable(boolean cancellable) {
		this.cancellable = cancellable;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void setDestLayer(String destLayer) {
		this.destLayer = destLayer;
	}

	public void setDestLayerList(HashSet destLayerList) {
		this.destLayerList = destLayerList;
	}

	public void setDestTNA(String destTNA) {
		this.destTNA = destTNA;
	}

	public void setDestTNAList(HashSet destTNAList) {
		this.destTNAList = destTNAList;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	public void setEndDateForList(String endDateForList) {
		this.endDateForList = endDateForList;
	}

	/**
	 * @param endTimeMillis
	 *          the endTimeMillis to set
	 */
	public void setEndTimeMillis(long endTimeMillis) {
		this.endTimeMillis = endTimeMillis;
	}

	public void setHop(int hop) {
		this.hop = hop;
	}

	public void setMemberGroup(String memberGroup) {
		this.memberGroup = memberGroup;
	}

	public void setMemberGroupList(List memberGroupList) {
		this.memberGroupList = memberGroupList;
	}

	/**
	 * @param message
	 *          the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	public void setMetric(int metric) {
		this.metric = metric;
	}

	public void setParentSchid(String parentSchid) {
		this.parentSchid = parentSchid;
	}

	/**
	 * @param rate
	 *          the rate to set
	 */
	public void setRate(int rate) {
		this.rate = rate;
	}

	/**
	 * @param resultNum
	 *          the resultNum to set
	 */
	public void setResultNum(int resultNum) {
		this.resultNum = resultNum;
	}

	public void setRoutingAlgorithm(String routingAlgorithm) {
		this.routingAlgorithm = routingAlgorithm;
	}

	public void setRoutingAlgorithmList(ArrayList routingAlgorithmList) {
		this.routingAlgorithmList = routingAlgorithmList;
	}

	public void setRoutingMetric(String routingMetric) {
		this.routingMetric = routingMetric;
	}

	public void setRoutingMetricList(ArrayList routingMetricList) {
		this.routingMetricList = routingMetricList;
	}

	public void setScheduleActivationType(String scheduleActivationType) {
		this.scheduleActivationType = scheduleActivationType;
	}

	public void setScheduleDestFacLabel(String scheduleDestFacLabel) {
		this.scheduleDestFacLabel = scheduleDestFacLabel;
	}

	/**
	 * @param scheduleDestLayer
	 *          the scheduleDestLayer to set
	 */
	public void setScheduleDestLayer(String scheduleDestLayer) {
		this.scheduleDestLayer = scheduleDestLayer;
	}

	/**
	 * @param scheduleDestTNA
	 *          the scheduleDestTNA to set
	 */
	public void setScheduleDestTNA(String scheduleDestTNA) {
		this.scheduleDestTNA = scheduleDestTNA;
	}

	public void setScheduleDestVlanId(String scheduleDestVlanId) {
		// Maintain empty string on null
		if (scheduleDestVlanId != null) {
			this.scheduleDestVlanId = scheduleDestVlanId;
		}
	}

	/**
	 * @param scheduleEnddate
	 *          the scheduleEnddate to set
	 */
	public void setScheduleEnddate(String scheduleEnddate) {
		this.scheduleEnddate = scheduleEnddate;
	}

	/**
	 * @param scheduleId
	 *          the scheduleId to set
	 */
	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public void setScheduleNameList(ArrayList scheduleNameList) {
		this.scheduleNameList = scheduleNameList;
	}

	/**
	 * @param scheduleRate
	 *          the scheduleRate to set
	 */
	public void setScheduleRate(int scheduleRate) {
		this.scheduleRate = scheduleRate;
	}

	public void setScheduleSrcFacLabel(String scheduleSrcFacLabel) {
		this.scheduleSrcFacLabel = scheduleSrcFacLabel;
	}

	/**
	 * @param scheduleSrcLayer
	 *          the scheduleSrcLayer to set
	 */
	public void setScheduleSrcLayer(String scheduleSrcLayer) {
		this.scheduleSrcLayer = scheduleSrcLayer;
	}

	/**
	 * @param scheduleSrcTNA
	 *          the scheduleSrcTNA to set
	 */
	public void setScheduleSrcTNA(String scheduleSrcTNA) {
		this.scheduleSrcTNA = scheduleSrcTNA;
	}

	public void setScheduleSrcVlanId(String scheduleSrcVlanId) {
		// Maintain empty string on null
		if (scheduleSrcVlanId != null) {
			this.scheduleSrcVlanId = scheduleSrcVlanId;
		}
	}

	/**
	 * @param scheduleStartdate
	 *          the scheduleStartdate to set
	 */
	public void setScheduleStartdate(String scheduleStartdate) {
		this.scheduleStartdate = scheduleStartdate;
	}

	/**
	 * @param scheduleStatus
	 *          the scheduleStatus to set
	 */
	public void setScheduleStatus(String scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}

	public void setSelectedID(String selectedID) {

		this.selectedID = selectedID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public void setSrcLayer(String srcLayer) {
		this.srcLayer = srcLayer;
	}

	public void setSrcLayerList(HashSet srcLayerList) {
		this.srcLayerList = srcLayerList;
	}

	public void setSrcTNA(String srcTNA) {
		this.srcTNA = srcTNA;
	}

	public void setSrcTNAList(HashSet srcTNAList) {
		this.srcTNAList = srcTNAList;
	}

	public void setSrlg(String srlg) {
		this.srlg = srlg;
	}

	public void setSrlgInclusions(String srlgInclusions) {
		this.srlgInclusions = srlgInclusions;
	}

	public void setSrsg(ArrayList srsg) {
		this.srsg = srsg;
	}

	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public void setStartDateForList(String startDateForList) {
		this.startDateForList = startDateForList;
	}

	/**
	 * @param startTimeMillis
	 *          the startTimeMillis to set
	 */
	public void setStartTimeMillis(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStatusList(ArrayList statusList) {
		this.statusList = statusList;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
