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

import java.util.HashMap;

import org.apache.struts.validator.ValidatorForm;

import com.nortel.appcore.app.drac.server.webserver.struts.common.DracConstants;

public final class AuditLoggingForm extends ValidatorForm {
	private static final long serialVersionUID = 9193628082430055272L;
	private int serialNo = 0;
	private String auditId = DracConstants.EMPTY_STRING;
	private String userid = DracConstants.EMPTY_STRING;
	private String group = DracConstants.EMPTY_STRING;
	private String billingGroup = DracConstants.EMPTY_STRING;
	private String severity = "";
	private String address = "";
	private String type = "";
	private String result = "";
	private String category = "";
	private String description = "";
	private String resource = "";
	private long occurTime = 0;
	private String operation = DracConstants.EMPTY_STRING;
	private String time = DracConstants.EMPTY_STRING;
	private HashMap data = new HashMap();

	public String getAddress() {
		return address;
	}

	public String getAuditId() {
		return auditId;
	}

	public String getBillingGroup() {
		return billingGroup;
	}

	public String getCategory() {
		return category;
	}

	public HashMap getData() {
		return this.data;
	}

	public String getDescription() {
		return description;
	}

	public String getGroup() {
		return group;
	}

	public long getOccurTime() {
		return occurTime;
	}

	public String getOperation() {
		return operation;
	}

	public String getResource() {
		return resource;
	}

	public String getResult() {
		return result;
	}

	public int getSerialNo() {
		return serialNo;
	}

	public String getSeverity() {
		return severity;
	}

	public String getTime() {
		return time;
	}

	public String getType() {
		return type;
	}

	public String getUserid() {
		return userid;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setAuditId(String id) {
		this.auditId = id;
	}

	public void setBillingGroup(String billingGroup) {
		this.billingGroup = billingGroup;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setData(HashMap newHMap) {
		this.data = newHMap;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setOccurTime(long occurTime) {
		this.occurTime = occurTime;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setSerialNo(int slNo) {
		this.serialNo = slNo;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

}
