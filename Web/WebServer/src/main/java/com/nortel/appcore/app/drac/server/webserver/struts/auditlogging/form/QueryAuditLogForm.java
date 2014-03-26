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

package com.nortel.appcore.app.drac.server.webserver.struts.auditlogging.form;

import org.apache.struts.validator.ValidatorActionForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

/**
 * Created on 16-Aug-06
 */
public final class QueryAuditLogForm extends ValidatorActionForm {
	private static final long serialVersionUID = 1L;
	private String startTime = DracConstants.EMPTY_STRING;
	private String endTime = DracConstants.EMPTY_STRING;
	private String startdate = DracConstants.EMPTY_STRING;
	private String enddate = DracConstants.EMPTY_STRING;
	private String auditType = DracConstants.EMPTY_STRING;
	private String category = DracConstants.EMPTY_STRING;
	private String logType = DracConstants.EMPTY_STRING;
	private String severity = DracConstants.EMPTY_STRING;
	private String result = DracConstants.EMPTY_STRING;
	private String originator = DracConstants.EMPTY_STRING;
	private String ipAddress = DracConstants.EMPTY_STRING;
	private String billingGroup = DracConstants.EMPTY_STRING;
	private String resource = DracConstants.EMPTY_STRING;
	private String locale = "";


	/**
	 * @return the function
	 */
	public String getAuditType() {
		return auditType;
	}

	public String getBillingGroup() {
		return billingGroup;
	}

	public String getCategory() {
		return category;
	}

	public String getEnddate() {
		return enddate;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getLogType() {
		return logType;
	}

	public String getOriginator() {
		return originator;
	}

	public String getResource() {
		return resource;
	}

	public String getResult() {
		return result;
	}

	public String getSeverity() {
		return severity;
	}

	public String getStartdate() {
		return startdate;
	}

	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param function
	 *          the function to set
	 */
	public void setAuditType(String function) {
		this.auditType = function;
	}

	public void setBillingGroup(String billingGroup) {
		this.billingGroup = billingGroup;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setEnddate(String string) {
		enddate = string;
	}

	public void setEndTime(String string) {
		endTime = string;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public void setOriginator(String originator) {
		this.originator = originator;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public void setStartdate(String string) {
		startdate = string;
	}

	public void setStartTime(String string) {
		startTime = string;
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}
