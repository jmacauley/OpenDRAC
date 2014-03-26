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
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckable;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;

/**
 * Renamed from ScheduleType to Schedule. Tried to make the object immutable at
 * the same time.
 * <p>
 * This class holds data associated with a top level schedule. A schedule
 * contains zero or more services, each service must have the same A and Z end
 * points and rate. A schedule groups together similar services. A recurring
 * schedule might hold hundreds of schedules which will fire at different times.
 * ie A schedule that runs daily will have a services that run on each day.
 * <p>
 * You might think that the schedule holds the common data and that a service
 * only holds the details of that particular instance (start/stop time), but in
 * reality the service is self contained and ignores the schedule. The LPCP_PORT does
 * not know about schedules, only services, schedules are managed at the NRB_PORT
 * software layer.
 * <p>
 * Created on Dec 5, 2005
 *
 * @author nguyentd
 */
public final class Schedule implements Serializable, PolicyCheckable {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public enum ACTIVATION_TYPE {
		PRERESERVATION_MANUAL, PRERESERVATION_AUTOMATIC, RESERVATION_MANUAL, RESERVATION_AUTOMATIC
	}

	private static final long serialVersionUID = 1;

	private State.SCHEDULE status;
	private String name;
	private long startTime;
	private long endTime;
	private long duration;
	private UserType userType;
	private RecurrenceType recurrence;
	private PathType path;
	private boolean recurring;
	private ACTIVATION_TYPE activationType;
	
	private boolean isActivated = false;

	private String id = "unknown";
	private List<DracService> serviceIdList;

	public Schedule() {
		status = State.SCHEDULE.EXECUTION_PENDING;
		name = "unAssigned";
		startTime = 0L;
		endTime = 0L;
		duration = 0L;
		userType = null;
		recurrence = new RecurrenceType();
		path = new PathType();
		recurring = true;
		activationType = ACTIVATION_TYPE.RESERVATION_AUTOMATIC;
		serviceIdList = null;
	}

	public Schedule(
			ACTIVATION_TYPE activation,
			String identifier,
			String userName,
			SCHEDULE scheduleStatus,
			long scheduleStartTime,
			Long scheduleEndTime,
			long scheduleDuration,
			UserType userInfoType,
			PathType schedulePath,
			boolean isRecurring,
			RecurrenceType recurrenceType,
			List<DracService> serviceIdTypeList) {

		id = identifier;
		status = scheduleStatus == null ? State.SCHEDULE.EXECUTION_PENDING
		    : scheduleStatus;
		name = userName == null ? "unAssigned" : userName;
		startTime = scheduleStartTime;
		duration = scheduleDuration;

		if (scheduleEndTime == null) {
			endTime = startTime + duration;
		}
		else {
			endTime = scheduleEndTime.longValue();
		}

		userType = userInfoType;
		recurrence = recurrenceType == null ? new RecurrenceType() : recurrenceType;
		path = schedulePath == null ? new PathType() : schedulePath;
		recurring = isRecurring;
		serviceIdList = serviceIdTypeList;
		activationType = activation == null ? ACTIVATION_TYPE.RESERVATION_AUTOMATIC
		    : activation;

	}

	/**
	 * @return the activationType
	 */
	public ACTIVATION_TYPE getActivationType() {
		return this.activationType;
	}

	public String getDestFacLabel() {
		String label = "";
		if (this.path != null) {
			EndPointType target = this.path.getTargetEndPoint();
			if (target != null) {
				label = target.getLabel();
			}
		}
		return label;
	}

	/**
	 * @return the layer
	 */
	public String getDestLayer() {
		String layer = "";
		if (this.path != null) {
			EndPointType target = this.path.getTargetEndPoint();
			if (target != null) {
				layer = target.getLayer();
			}
		}
		return layer;
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
	 * @return the duration
	 */
	public long getDurationLong() {
		return getDuration();
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

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the path
	 */
	public PathType getPath() {
		return path;
	}

	public int getRate() {
		int rate = 0;
		if (this.path != null) {
			rate = this.path.getRate();
		}
		return rate;
	}

	/**
	 * @return the recurrence
	 */
	public RecurrenceType getRecurrence() {
		return recurrence;
	}

	public List<DracService> getServiceIdArrayList() {
		// This method will return null if there are no services.
		return serviceIdList;
	}

	/**
	 * @return the serviceIdList
	 */
	public DracService[] getServiceIdList() {
		List<DracService> list = getServiceIdArrayList();

		if (list != null && !list.isEmpty()) {
			return list.toArray(new DracService[list.size()]);
		}

		return new DracService[0];
	}

	public String getSrcFacLabel() {
		String label = "";
		if (this.path != null) {
			EndPointType source = this.path.getSourceEndPoint();
			if (source != null) {
				label = source.getLabel();
			}
		}
		return label;
	}

	/**
	 * @return the layer
	 */
	public String getSrcLayer() {
		String layer = "";
		if (this.path != null) {
			EndPointType src = this.path.getSourceEndPoint();
			if (src != null) {
				layer = src.getLayer();
			}
		}
		return layer;
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
	public State.SCHEDULE getStatus() {
		return status;
	}

	/**
	 * @return the userInfo
	 */
	public UserType getUserInfo() {
		return userType;
	}

	public boolean isRecurring() {
		return recurring;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(State.SCHEDULE status) {
		this.status = status;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @param userType the userInfo to set
	 */
	public void setUserInfo(UserType userType) {
		this.userType = userType;
	}

	/**
	 * @param recurrence the recurrence to set
	 */
	public void setRecurrence(RecurrenceType recurrence) {
		this.recurrence = recurrence;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(PathType path) {
		this.path = path;
	}

	/**
	 * @param recurring the recurring to set
	 */
	public void setRecurring(boolean recurring) {
		this.recurring = recurring;
	}

	/**
	 * @param activationType the activationType to set
	 */
	public void setActivationType(ACTIVATION_TYPE activationType) {
		this.activationType = activationType;
	}

	/**
	 * @param serviceIdList the serviceIdList to set
	 */
	public void setServiceIdList(List<DracService> serviceIdList) {
		this.serviceIdList = serviceIdList;
	}

	public void setRate(int rate) {
		if (this.path != null) {
			this.path.setRate(rate);
		}
	}

  public boolean isActivated() {
    log.debug("ACTIVATION_TYPE: "+getActivationType());
    if (getActivationType() == ACTIVATION_TYPE.RESERVATION_AUTOMATIC
        || getActivationType() == ACTIVATION_TYPE.PRERESERVATION_AUTOMATIC) {
      return true;
    }
    return isActivated;
  }

  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }

  public String toDebugString() {
		return toDebugString("\n ScheduleType:\t");
	}

	public String toDebugString(String delim) {
		StringBuilder builder = new StringBuilder();
		builder.append("Schedule.toDebugString() Schedule [");

		builder.append(delim);
		builder.append("activationType=");
		builder.append(activationType);

		builder.append(delim);
		builder.append("startTime=");
		builder.append(startTime);
		builder.append(" (");
		builder.append(new Date(startTime).toString());
		builder.append(")");

		builder.append(delim);
		builder.append("endTime=");
		builder.append(endTime);
		builder.append(" (");
		builder.append(new Date(endTime).toString());
		builder.append(")");

		builder.append(delim);
		builder.append("duration=");
		builder.append(getDuration());
		builder.append(" (");
		builder.append(getDuration() / (60 * 1000));
		builder.append(" minutes)");

		builder.append(delim);
		builder.append("id=");
		builder.append(id);

		builder.append(delim);
		builder.append("name=");
		builder.append(name);

		builder.append(delim);
		builder.append("path=");
		builder.append(path);

		builder.append(delim);
		builder.append("recurrence=");
		builder.append(recurrence);

		builder.append(delim);
		builder.append("recurring=");
		builder.append(recurring);

		builder.append(delim);
		builder.append("serviceIdList=");
		builder.append(serviceIdList);

		builder.append(delim);
		builder.append("status=");
		builder.append(status);

		builder.append(delim);
		builder.append("userInfo=");
		builder.append(userType.toString());

		builder.append(delim);
		builder.append("]");
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Schedule [id=");
		builder.append(id);
		builder.append(", status=");
		builder.append(status);
		builder.append(", name=");
		builder.append(name);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", duration=");
		builder.append(getDuration());
		builder.append(", activationType=");
		builder.append(activationType);
		builder.append(", userInfo=");
		builder.append(userType);
		builder.append(", recurring=");
		builder.append(recurring);
		builder.append(", recurrence=");
		builder.append(recurrence);
		builder.append(", path=");
		builder.append(path);
		builder.append(", serviceIdList=");
		builder.append(serviceIdList);
		builder.append("]");
		return builder.toString();
	}

}
