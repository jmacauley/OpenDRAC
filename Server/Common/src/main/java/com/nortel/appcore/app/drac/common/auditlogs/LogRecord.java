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

package com.nortel.appcore.app.drac.common.auditlogs;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;

/**
 * Holder class that holds a single log record, for reading, writing to the
 * database.
 * 
 * @author pitman
 */

public final class LogRecord implements Serializable {
	public enum LOG_CATEGORY {
		// The DbLog schema must be updated if these are changed!
		NA, UNKNOWN, AUTHENTICATION, AUTHORIZATION, RESERVATIONGROUP, RESERVATION, SECURITY, NE, ENDPOINT, SYSTEM;
	}

	public enum LOG_RESULT {
		// The DbLog schema must be updated if these are changed!
		NA, UNKNOWN, FAILED, SUCCESS;
	}

	public enum LOG_SEVERITY {
		// The DbLog schema must be updated if these are changed!
		INFO, WARNING, MINOR, MAJOR, CRITICAL;
	}

	public enum LOG_TYPE {
		// The DbLog schema must be updated if these are changed!
		NA, UNKNOWN, CREATED, MODIFIED, DELETED, CANCELED, ALARM_RAISED, ALARM_CLEARED, MANAGED, UNMANAGED, ALIGNED, LOGGED_IN, LOGGED_OUT, EXECUTED, ACCESS_CHECK, REDUNDANCY, VERIFIED;
	}

	private static final long serialVersionUID = 1L;
	private final long time;
	private final String originator;
	private final String ip;
	private final UserGroupName billingGroup;
	private final LOG_SEVERITY severity;
	private final LOG_CATEGORY category;
	private final LOG_TYPE type;
	private final String resource;
	private final LOG_RESULT result;
	private final String description;
	private final Map<String, String> details = new TreeMap<String, String>();

	private static final String NOTAPPL = "N/A";
	private static final String SYSTEM_USER = "DRAC";
	private static final String LOCALHOST = "localhost";

	// private static final String RES_SYSTEM_SETTINGS = "System settings";

	public LogRecord(String userId, String ipAddr, UserGroupName logBillingGroup,
	    String logResource, LogKeyEnum key) {
		this(System.currentTimeMillis(), userId, ipAddr, logBillingGroup, key
		    .getSeverity(), key.getCategory(), key.getType(), logResource, key
		    .getResult(), key.getFormattedLogDescription(null), null);
	}

	public LogRecord(String userId, String ipAddr, UserGroupName logBillingGroup,
	    String logResource, LogKeyEnum key, String[] args) {
		this(System.currentTimeMillis(), userId, ipAddr, logBillingGroup, key
		    .getSeverity(), key.getCategory(), key.getType(), logResource, key
		    .getResult(), key.getFormattedLogDescription(args), null);
	}

	public LogRecord(String userId, String ipAddr, UserGroupName logBillingGroup,
	    String logResource, LogKeyEnum key, String[] args,
	    Map<String, String> logDetails) {
		this(System.currentTimeMillis(), userId, ipAddr, logBillingGroup, key
		    .getSeverity(), key.getCategory(), key.getType(), logResource, key
		    .getResult(), key.getFormattedLogDescription(args), logDetails);
	}

	/**
	 * protected this is only to be used when reading existing records from the
	 * database! log writers must use a constructor that uses a LogKeyEnum to
	 * force all generated logs to use the enum and have keys from the resource
	 * bundle.
	 */
	protected LogRecord(long logTime, String logOriginator, String logIp,
	    UserGroupName logBillingGroup, LOG_SEVERITY logSeverity,
	    LOG_CATEGORY logCategory, LOG_TYPE logType, String logResource,
	    LOG_RESULT logResult, String logDescription,
	    Map<String, String> logDetails) {
		time = logTime;
		originator = logOriginator == null ? LogRecord.SYSTEM_USER : logOriginator
		    .trim();
		ip = logIp == null ? LogRecord.LOCALHOST : logIp.trim();
		billingGroup = logBillingGroup == null ? UserGroupName.USER_GROUP_NOTAPPL
		    : logBillingGroup;
		severity = logSeverity == null ? LOG_SEVERITY.MINOR : logSeverity;
		category = logCategory == null ? LOG_CATEGORY.UNKNOWN : logCategory;
		type = logType == null ? LOG_TYPE.UNKNOWN : logType;
		resource = logResource == null ? LogRecord.NOTAPPL : logResource.trim();
		result = logResult == null ? LOG_RESULT.UNKNOWN : logResult;
		description = logDescription == null ? LogRecord.NOTAPPL : logDescription
		    .trim();
		if (logDetails != null) {
			details.putAll(logDetails);
		}
	}

	public UserGroupName getBillingGroup() {
		return billingGroup;
	}

	public LOG_CATEGORY getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, String> getDetails() {
		return Collections.unmodifiableMap(details);
	}

	public String getIp() {
		return ip;
	}

	public String getOriginator() {
		return originator;
	}

	public String getResource() {
		return resource;
	}

	public LOG_RESULT getResult() {
		return result;
	}

	public LOG_SEVERITY getSeverity() {
		return severity;
	}

	public long getTime() {
		return time;
	}

	public LOG_TYPE getType() {
		return type;
	}

	public boolean isBillingGroupSet() {
		return !NOTAPPL.equals(billingGroup.toString());
	}

	public boolean isOriginatorSet() {
		return !SYSTEM_USER.equals(originator);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LogRecord [time=");
		sb.append(time);
		sb.append(", severity=");
		sb.append(severity);
		sb.append(", type=");
		sb.append(type);
		sb.append(", category=");
		sb.append(category);
		sb.append(", result=");
		sb.append(result);
		sb.append(", ip=");
		sb.append(ip);
		sb.append(", originator=");
		sb.append(originator);
		sb.append(", resource=");
		sb.append(resource);
		sb.append(", billingGroup=");
		sb.append(billingGroup);
		sb.append(", description=");
		sb.append(description);
		sb.append(", details=");
		sb.append(details);
		sb.append("]");
		return sb.toString();
	}

}
