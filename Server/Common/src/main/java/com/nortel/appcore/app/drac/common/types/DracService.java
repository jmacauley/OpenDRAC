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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckable;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;

/**
 * Formally called SeviceTypeId, this class holds an instance of a DRAC service.
 * A service represents a single execution of dynamic bandwidth. Related
 * services can be grouped together under a Schedule, though the service is
 * functionally complete and contains all the necessary information inside it,
 * at the cost of some overlap between the Service and Schedule.
 * <p>
 * Created on Dec 5, 2005
 * 
 * @author nguyentd
 */
public final class DracService implements Serializable, PolicyCheckable,
    Comparable{
	private static final long serialVersionUID = 1;

	private String id = "unknown";
	/**
	 * Each Call is created by a Controller; thus for a Schedule that involve more
	 * than one Controllers (i.e. multiple domains), the size of the array is
	 * larger than 1.
	 */
	private CallIdType[] call;
	private State.SERVICE status = State.SERVICE.EXECUTION_PENDING;
	/**
	 * This attribute is used to referenced back to the parent Schedule
	 */
	private String scheduleId = "";
	private String scheduleName = "";
	private long scheduleStartTime;
	private long scheduleEndTime;
	private int scheduleRate;
	private SCHEDULE scheduleStatus = SCHEDULE.EXECUTION_PENDING;
	private String scheduleCreator = "";
	private int mbs;
	private List<CrossConnection> crossConnections = new ArrayList<CrossConnection>();
	

	/**
	 * The value of the following attributes are inherited from their parent
	 * Schedule if uninitialized.
	 */
	private long startTime;
	private long endTime;
	private UserType userType;
	private PathType path = new PathType();
	// for list services purposes
	private int resultNum;
	/**
	 * The following value is used to store the system overhead for service
	 * creation and deletion
	 */
	private int offset;
	private ACTIVATION_TYPE activationType = ACTIVATION_TYPE.RESERVATION_AUTOMATIC;

	public DracService() {
		this(ACTIVATION_TYPE.RESERVATION_AUTOMATIC);
	}

	public DracService(ACTIVATION_TYPE activationType) {
		setActivationType(activationType);
	}

	/**
	 * Compares two service objects.
	 */
	@Override
	public int compareTo(Object o) { // NO_UCD
		DracService s = (DracService) o;
		// compare by start times
		if (s.getStartTime() < this.getStartTime()) {
			return 1;
		}
		else if (s.getStartTime() > this.getStartTime()) {
			return -1;
		}
		else {
			// start times are equal, compare end time
			if (s.getEndTime() < this.getEndTime()) {
				return 1;
			}
			else if (s.getEndTime() > this.getEndTime()) {
				return -1;
			}
			else {
				// start and end times are the same, sort by schedule ID
				return s.getId().compareTo(this.getId());
			}
		}
	}

	/**
	 * @return the activationType
	 */
	public ACTIVATION_TYPE getActivationType() {
		return this.activationType;
	}

	/**
	 * @return the call
	 */
	public CallIdType[] getCall() {
		return call;
	}

	/**
	 * @return the destTNA
	 */
	public String getDestTNA() {
		String tna = "";
		if (this.path != null) {
			EndPointType target = this.path.getTargetEndPoint();
			if (target != null) {
				tna = target.getName();
			}
		}
		return tna;
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public int getMbs() {
		return mbs;
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * @return the path
	 */
	public PathType getPath() {
		return path;
	}

	public int getRate() {
		return this.path.getRate();
	}

	/**
	 * @return the resultNum
	 */
	public int getResultNum() {
		return resultNum;
	}

	public String getScheduleCreator() {
		return scheduleCreator;
	}

	/**
	 * @return the scheduleEndTime
	 */
	public long getScheduleEndTime() {
		return scheduleEndTime;
	}

	/**
	 * @return the scheduleId
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
	 * @return the scheduleRate
	 */
	public int getScheduleRate() {
		return scheduleRate;
	}

	/**
	 * @return the scheduleStartTime
	 */
	public long getScheduleStartTime() {
		return scheduleStartTime;
	}

	/**
	 * @return the scheduleStatus
	 */
	public SCHEDULE getScheduleStatus() {
		return scheduleStatus;
	}

	/**
	 * @return the srcTNA
	 */
	public String getSrcTNA() {
		String tna = "";
		if (this.path != null) {
			EndPointType source = this.path.getSourceEndPoint();
			if (source != null) {
				tna = source.getName();
			}
		}
		return tna;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the status
	 */
	public State.SERVICE getStatus() {
		return status;
	}

	/**
	 * @return the userInfo
	 */
	public UserType getUserInfo() {
		return userType;
	}

	/**
	 * @param call
	 *          the call to set
	 */
	public void setCall(CallIdType[] call) {
		this.call = call;
	}

	/**
	 * @param destTNA
	 *          the destTNA to set
	 */
	public void setDestTNA(String destTNA) {
		if (this.path != null) {
			EndPointType target = this.path.getTargetEndPoint();
			if (target != null) {
				target.setName(destTNA);
			}
		}
	}

	/**
	 * @param endTime
	 *          the endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public void setMbs(int rate) {
		this.mbs = rate;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * @param path
	 *          the path to set
	 */
	public void setPath(PathType path) {
		this.path = path;
	}

	public void setRate(int rate) {
		if (this.path != null) {
			this.path.setRate(rate);
		}
	}

	/**
	 * @param resultNum
	 *          the resultNum to set
	 */
	public void setResultNum(int resultNum) {
		this.resultNum = resultNum;
	}

	public void setScheduleCreator(String scheduleCreator) {
		this.scheduleCreator = scheduleCreator;
	}

	/**
	 * @param scheduleEndTime
	 *          the scheduleEndTime to set
	 */
	public void setScheduleEndTime(long scheduleEndTime) {
		this.scheduleEndTime = scheduleEndTime;
	}

	/**
	 * @param scheduleId
	 *          the scheduleId to set
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
	 * @param scheduleRate
	 *          the scheduleRate to set
	 */
	public void setScheduleRate(int scheduleRate) {
		this.scheduleRate = scheduleRate;
	}

	/**
	 * @param scheduleStartTime
	 *          the scheduleStartTime to set
	 */
	public void setScheduleStartTime(long scheduleStartTime) {
		this.scheduleStartTime = scheduleStartTime;
	}

	/**
	 * @param scheduleStatus
	 *          the scheduleStatus to set
	 */
	public void setScheduleStatus(SCHEDULE scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}

	/**
	 * @param srcTNA
	 *          the srcTNA to set
	 */
	public void setSrcTNA(String srcTNA) {
		if (this.path != null) {
			EndPointType source = this.path.getSourceEndPoint();
			if (source != null) {
				source.setName(srcTNA);
			}
		}
	}

	/**
	 * @param startTime
	 *          the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @param status
	 *          the status to set
	 */
	public void setStatus(State.SERVICE status) {
		this.status = status;
	}

	/**
	 * @param userType
	 *          the userInfo to set
	 */
	public void setUserInfo(UserType userType) {
		this.userType = userType;
	}

	// @Override
	// public String toString()
	// {
	// return "ServiceId:" + id + " sourceTNA=" + getSrcTNA() + "&targetTNA=" +
	// getDestTNA();
	// }

	@Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DracService [id=");
    builder.append(id);
    builder.append(", call=");
    builder.append(Arrays.toString(call));
    builder.append(", status=");
    builder.append(status);
    builder.append(", scheduleId=");
    builder.append(scheduleId);
    builder.append(", scheduleName=");
    builder.append(scheduleName);
    builder.append(", scheduleStartTime=");
    builder.append(scheduleStartTime);
    builder.append(", scheduleEndTime=");
    builder.append(scheduleEndTime);
    builder.append(", scheduleRate=");
    builder.append(scheduleRate);
    builder.append(", scheduleStatus=");
    builder.append(scheduleStatus);
    builder.append(", scheduleCreator=");
    builder.append(scheduleCreator);
    builder.append(", mbs=");
    builder.append(mbs);
    builder.append(", crossConnections=");
    builder.append(crossConnections);
    builder.append(", startTime=");
    builder.append(startTime);
    builder.append(", endTime=");
    builder.append(endTime);
    builder.append(", userType=");
    builder.append(userType);
    builder.append(", path=");
    builder.append(path);
    builder.append(", resultNum=");
    builder.append(resultNum);
    builder.append(", offset=");
    builder.append(offset);
    builder.append(", activationType=");
    builder.append(activationType);
    builder.append("]");
    return builder.toString();
  }

	/**
	 * @param activationType
	 *          the activationType to set
	 */
	private void setActivationType(ACTIVATION_TYPE activationType) {
		this.activationType = activationType;
		if (ACTIVATION_TYPE.PRERESERVATION_AUTOMATIC == this.activationType
		    || ACTIVATION_TYPE.PRERESERVATION_MANUAL == this.activationType) {
			status = State.SERVICE.CONFIRMATION_PENDING;
		}
		else if (ACTIVATION_TYPE.RESERVATION_AUTOMATIC == this.activationType) {
			status = State.SERVICE.EXECUTION_PENDING;
		}
		else if (ACTIVATION_TYPE.RESERVATION_MANUAL == this.activationType) {
			status = State.SERVICE.ACTIVATION_PENDING;
		}
	}
	public List<CrossConnection> getCrossConnections() {
		return crossConnections;
	}

	public void setCrossConnections(List<CrossConnection> crossConnections) {
		this.crossConnections = crossConnections;
	}

}
