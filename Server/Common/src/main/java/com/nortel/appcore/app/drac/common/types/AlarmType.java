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

/**
 * Created on 14-Nov-06
 */
public final class AlarmType implements Serializable {
	private static final long serialVersionUID = -2772762692003319580L;
	private String serviceId = "";
	private final String id;
	private String severity = "";
	private long occurTime;
	private long duration;
	private String description = "";
	private String scheduleName = "";
	private String scheduleId = "";
	

	public AlarmType(String id, String serviceId) {
		this.id = id;
		this.serviceId = serviceId;
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
	public long getDuration() {
		return duration;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the occurTime
	 */
	public long getOccurTime() {
		return occurTime;
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
	public void setDuration(long duration) {
		this.duration = duration;
	}

	// /**
	// * @param id the id to set
	// */
	// public void setId(String id)
	// {
	// this.id = id;
	// }

	/**
	 * @param occurTime
	 *          the occurTime to set
	 */
	public void setOccurTime(long occurTime) {
		this.occurTime = occurTime;
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

	// /**
	// * @param serviceId the serviceId to set
	// */
	// public void setServiceId(String serviceId)
	// {
	// this.serviceId = serviceId;
	// }

	/**
	 * @param severity
	 *          the severity to set
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

}
