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

package com.nortel.appcore.app.drac.server.ws.systemmonitoringservice;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.*;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_CATEGORY;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_RESULT;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_SEVERITY;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_TYPE;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.server.ws.common.CommonConstants;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.systemmonitoringservice.request.QueryLogsRequest;

public final class Logs {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static enum ValidLogCategoryT {
		Na, Unknown, Authentication, Authorization, ReservationGroup, Reservation, Security, Ne, Endpoint, System
	}

	private static enum ValidLogResultT {
		Na, Unknown, Failed, Success
	}

	private static enum ValidLogSeverityT {
		Info, Warning, Minor, Major, Critical
	}

	private static enum ValidLogTypeT {
		Na, Unknown, Created, Modified, Deleted, Canceled, AlarmRaised, AlarmCleared, Managed, Unmanaged, Aligned, LoggedIn, LoggedOut, Executed, AccessCheck, Redundancy, Verified
	}

	private OMElement inputMessage;
	private final ServiceUtilities serviceUtil = new ServiceUtilities();
	private long startTime = -1;
	private long endTime = -1;
	private String originatorBillingGroup;
	private String severity;
	private String category;
	private String logType;
	private String result;
	private String affectedResourceId;
	private String originatorUserIdId;
	private String originatorIPAddress;

	public Logs(OMElement inputMsg, String ns, String nsPrefix)
	    throws InvalidInputException {
		this.inputMessage = inputMsg;

		OMElement selectedNode = serviceUtil.getNode(this.inputMessage, ns,
		    nsPrefix, QueryLogsRequest.QUERY_CRITERIA);
		if (selectedNode == null) {
			log.error("No query criteria found in the request.");
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_QUERY_CRITERIA;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// type attribute check only needed for V2 WSDL
		if (ns.equals(SYSTEM_MONITORING_SERVICE_NS)) {
		    OMAttribute criteria = selectedNode.getAttribute(new QName(
		            XML_SCHEMA_INSTANCE_NS, "type", XML_SCHEMA_INSTANCE_NS_PREFIX));

		    if (criteria == null) {
		        log.error("null criteria.");
		        Object[] args = new Object[1];
		        args[0] = RequestResponseConstants.RAW_QUERY_CRITERIA;
		        throw new InvalidInputException(
		                DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		    }
		    else {
		        String criteriaValue = criteria.getAttributeValue();

		        if (criteriaValue.indexOf(":") != -1) {
		            criteriaValue = criteriaValue.substring(criteriaValue.indexOf(":") + 1);
		        }
		        if (criteriaValue
		                .compareTo(RequestResponseConstants.RAW_QUERY_LOGS_BY_DATE_TIME) != 0) {
		            log.error("Unsupported query criteria = " + criteriaValue);
		            Object[] args = new Object[2];
		            args[0] = criteriaValue;
		            args[1] = RequestResponseConstants.RAW_TYPE;
		            throw new InvalidInputException(
		                    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
		                    args);
		        }
		    }
		}

		// startTime
		selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.START_TIME);
		if (selectedNode != null) {
			this.startTime = serviceUtil.convertToMillis(selectedNode);
		}
		else {
			log.error("No start time found in the request.");
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// endTime
		selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.END_TIME);
		if (selectedNode != null) {
			this.endTime = serviceUtil.convertToMillis(selectedNode);
		}
		else {
			log.error("No end time found in the request.");
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_END_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		if (this.startTime < 0) {
			log.error("Invalid value(s) for startTime");
			Object[] args = new Object[2];
			args[0] = this.startTime;
			args[1] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}
		if (this.endTime < 0) {
			log.error("Invalid value(s) for endTime");
			Object[] args = new Object[2];
			args[0] = this.endTime;
			args[1] = RequestResponseConstants.RAW_END_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}

		// category
		selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.CATEGORY);
		if (selectedNode != null) {
			setAuditCategory(selectedNode.getText().trim());
		}

		// operationType (logType)
		selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.OPERATION_TYPE);
		if (selectedNode != null) {
			setLogType(selectedNode.getText().trim());
		}

		// result
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.RESULT)) != null) {
			setResult(selectedNode.getText().trim());
		}

		// severity
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.SEVERITY)) != null) {
			setSeverity(selectedNode.getText().trim());
		}

		// originatorBillingGroup
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.ORIGINATOR_BILLING_GROUP)) != null) {
			String text = selectedNode.getText().trim();
			if (!text.isEmpty()) {
				this.originatorBillingGroup = text;
			}
		}

		// originator
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.ORIGINATOR_USER_ID)) != null) {
			String text = selectedNode.getText().trim();
			if (!text.isEmpty()) {
				this.originatorUserIdId = text;
			}
		}

		// originatorIP
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.ORIGINATOR_IP_ADDRESS)) != null) {
			String text = selectedNode.getText().trim();
			if (!text.isEmpty()) {
				this.originatorIPAddress = text;
			}
		}

		// affectedResource
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryLogsRequest.AFFECTED_RESOURCE_ID)) != null) {
			String text = selectedNode.getText().trim();
			if (!text.isEmpty()) {
				this.affectedResourceId = text;
			}
		}
	}

	public String getAffectedResourceId() {
		return this.affectedResourceId;
	}

	public String getAuditCategory() {
		return this.category;
	}

	public String getAuditResult() {
		return this.result;
	}

	public String getAuditSeverity() {
		return this.severity;
	}

	public String getAuditType() {
		return this.logType;
	}

	public long getEndTime() throws InvalidInputException {
		if (endTime < 0) {
			log.error("endTime is invalid.");
			Object[] args = new Object[2];
			args[0] = this.endTime;
			args[1] = RequestResponseConstants.RAW_END_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}
		return endTime;
	}

	public String getOriginatorBillingGroup() {
		return this.originatorBillingGroup;
	}

	public String getOriginatorIPAddress() {
		return this.originatorIPAddress;
	}

	public String getOriginatorUserIdId() {
		return this.originatorUserIdId;
	}

	public long getStartTime() throws InvalidInputException {
		if (startTime < 0) {
			log.error("startTime is invalid.");
			Object[] args = new Object[2];
			args[0] = startTime;
			args[1] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}
		return startTime;
	}

	public OMElement prepareResponseOMElement(List<LogRecord> logs, String ns,
	    String nsPrefix, String userTimeZoneIdPreference) {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespaceCommon = fac.createOMNamespace(ns, nsPrefix);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_QUERY_LOGS_RESPONSE, namespaceCommon);
		OMElement numOfElementsIncluded = fac.createOMElement(
		    RequestResponseConstants.RAW_NUM_OF_ELEMENTS_INCLUDED, namespaceCommon,
		    response);
		OMElement totalNumOfMatchingElements = fac.createOMElement(
		    RequestResponseConstants.RAW_TOTAL_NUM_OF_MATCHING_ELEMENTS,
		    namespaceCommon, response);
		if (logs == null) {
			numOfElementsIncluded.setText("0");
			totalNumOfMatchingElements.setText("0");
			return response;
		}
		if (logs.isEmpty()) {
			numOfElementsIncluded.setText("0");
			totalNumOfMatchingElements.setText("0");
			return response;
		}
		boolean reduceList = true;
		totalNumOfMatchingElements.setText(new Integer(logs.size()).toString());
		if (logs.size() < CommonConstants.MAX_NUM_OF_LOGS_TO_BE_RETRIEVED) {
			numOfElementsIncluded.setText(new Integer(logs.size()).toString());
			reduceList = false;
		}
		else {
			// There are more elements in the logs list than
			// MAX_NUM_OF_LOGS_TO_BE_RETRIEVED
			numOfElementsIncluded.setText(new Integer(
			    CommonConstants.MAX_NUM_OF_LOGS_TO_BE_RETRIEVED).toString());
		}

		int i = 0;

		for (LogRecord log : logs) {
			OMElement logTag = fac.createOMElement(RequestResponseConstants.RAW_LOG,
			    namespaceCommon, response);

			// category
			fac.createOMElement(RequestResponseConstants.RAW_CATEGORY,
			    namespaceCommon, logTag).setText(
			    getWsAuditCategory(log.getCategory()));

			// type
			fac.createOMElement(RequestResponseConstants.RAW_OPERATION_TYPE,
			    namespaceCommon, logTag).setText(getWsAuditType(log.getType()));

			// affectedResourceId
			fac.createOMElement(RequestResponseConstants.RAW_AFFECTED_RESOURCE_ID,
			    namespaceCommon, logTag).setText(log.getResource());

			// result
			fac.createOMElement(RequestResponseConstants.RAW_RESULT, namespaceCommon,
			    logTag).setText(getWsAuditResult(log.getResult()));

			// severity
			fac.createOMElement(RequestResponseConstants.RAW_SEVERITY,
			    namespaceCommon, logTag).setText(
			    getWsAuditSeverity(log.getSeverity()));

			// originatorUserId
			fac.createOMElement(RequestResponseConstants.RAW_USERID, namespaceCommon,
			    logTag).setText(log.getOriginator());

			// originatorIPAddress
			fac.createOMElement(RequestResponseConstants.RAW_IP_ADDRESS,
			    namespaceCommon, logTag).setText(log.getIp());

			// originatorBillingGroup
			fac.createOMElement(RequestResponseConstants.RAW_BILLING_GROUP,
			    namespaceCommon, logTag).setText(log.getBillingGroup().toString());

			// occurrenceTime
			fac.createOMElement(RequestResponseConstants.RAW_OCCURRENCE_DATETIME,
			    namespaceCommon, logTag).setText(
			    serviceUtil.convertMillisToISO8601FormattedString(log.getTime(),
			        userTimeZoneIdPreference));

			// description
			fac.createOMElement(RequestResponseConstants.RAW_DESCRIPTION,
			    namespaceCommon, logTag).setText(log.getDescription());
			i++;
			if (reduceList
			    && i == CommonConstants.MAX_NUM_OF_RESERVATION_OCCURRENCES_TO_BE_RETRIEVED) {
				break;
			}
		}
		// 
		return response;
	}

	public void setInputMessage(OMElement inputMsg) {
		this.inputMessage = inputMsg;
	}

	@Override
	public String toString() {
		return "startTime = " + startTime + ", endTime = " + endTime
		    + ", userGroup = " + originatorBillingGroup;
	}

	private String getAuditCategory(ValidLogCategoryT auditCategory) {
		if (auditCategory == null) {
			return null;
		}

		switch (auditCategory) {
		case Authentication:
			return LOG_CATEGORY.AUTHENTICATION.name();
		case Authorization:
			return LOG_CATEGORY.AUTHORIZATION.name();
		case Endpoint:
			return LOG_CATEGORY.ENDPOINT.name();
		case Na:
			return LOG_CATEGORY.NA.name();
		case Ne:
			return LOG_CATEGORY.NE.name();
		case Reservation:
			return LOG_CATEGORY.RESERVATION.name();
		case ReservationGroup:
			return LOG_CATEGORY.RESERVATIONGROUP.name();
		case Security:
			return LOG_CATEGORY.SECURITY.name();
		case System:
			return LOG_CATEGORY.SYSTEM.name();
		case Unknown:
		default:
			log.error("Default clause mapping " + auditCategory);
			return LOG_CATEGORY.UNKNOWN.name();
		}
	}

	private String getAuditResult(ValidLogResultT auditResult) {
		if (auditResult == null) {
			return null;
		}
		switch (auditResult) {
		case Na:
			return LOG_RESULT.NA.name();
		case Unknown:
			return LOG_RESULT.UNKNOWN.name();
		case Failed:
			return LOG_RESULT.FAILED.name();
		case Success:
			return LOG_RESULT.SUCCESS.name();
		default:
			return LOG_RESULT.UNKNOWN.name();
		}
	}

	private String getAuditSeverity(ValidLogSeverityT auditSeverity) {
		if (auditSeverity == null) {
			return null;
		}
		switch (auditSeverity) {
		case Info:
			return LOG_SEVERITY.INFO.name();
		case Minor:
			return LOG_SEVERITY.MINOR.name();
		case Major:
			return LOG_SEVERITY.MAJOR.name();
		case Warning:
			return LOG_SEVERITY.WARNING.name();
		case Critical:
			return LOG_SEVERITY.CRITICAL.name();
		default:
			log.error("Default clause mapping " + auditSeverity);
			return LOG_SEVERITY.INFO.name();
		}
	}

	private String getAuditType(ValidLogTypeT validLogType) {
		if (validLogType == null) {
			return null;
		}
		switch (validLogType) {
		case Na:
			return LOG_TYPE.NA.name();
		case Unknown:
			return LOG_TYPE.UNKNOWN.name();
		case Created:
			return LOG_TYPE.CREATED.name();
		case Modified:
			return LOG_TYPE.MODIFIED.name();
		case Deleted:
			return LOG_TYPE.DELETED.name();
		case Canceled:
			return LOG_TYPE.CANCELED.name();
		case AlarmRaised:
			return LOG_TYPE.ALARM_RAISED.name();
		case AlarmCleared:
			return LOG_TYPE.ALARM_CLEARED.name();
		case Managed:
			return LOG_TYPE.MANAGED.name();
		case Unmanaged:
			return LOG_TYPE.UNMANAGED.name();
		case Aligned:
			return LOG_TYPE.ALIGNED.name();
		case LoggedIn:
			return LOG_TYPE.LOGGED_IN.name();
		case LoggedOut:
			return LOG_TYPE.LOGGED_OUT.name();
		case Executed:
			return LOG_TYPE.EXECUTED.name();
		case AccessCheck:
			return LOG_TYPE.ACCESS_CHECK.name();
		case Redundancy:
			return LOG_TYPE.REDUNDANCY.name();
		case Verified:
			return LOG_TYPE.VERIFIED.name();
		default:
			log.error("Default clause mapping " + validLogType);
			return LOG_TYPE.UNKNOWN.name();
		}
	}

	private String getWsAuditCategory(LOG_CATEGORY auditCategory) {
		switch (auditCategory) {
		case NA:
			return ValidLogCategoryT.Na.name();
		case UNKNOWN:
			return ValidLogCategoryT.Unknown.name();
		case AUTHENTICATION:
			return ValidLogCategoryT.Authentication.name();
		case AUTHORIZATION:
			return ValidLogCategoryT.Authorization.name();
		case RESERVATIONGROUP:
			return ValidLogCategoryT.ReservationGroup.name();
		case RESERVATION:
			return ValidLogCategoryT.Reservation.name();
		case SECURITY:
			return ValidLogCategoryT.Security.name();
		case NE:
			return ValidLogCategoryT.Ne.name();
		case ENDPOINT:
			return ValidLogCategoryT.Endpoint.name();
		case SYSTEM:
			return ValidLogCategoryT.System.name();
		default:
			log.error("Default clause mapping " + auditCategory);
			return ValidLogCategoryT.Unknown.name();
		}
	}

	private String getWsAuditResult(LOG_RESULT auditResult) {
		switch (auditResult) {
		case NA:
			return ValidLogResultT.Na.name();
		case UNKNOWN:
			return ValidLogResultT.Unknown.name();
		case FAILED:
			return ValidLogResultT.Failed.name();
		case SUCCESS:
			return ValidLogResultT.Success.name();
		default:
			log.error("Default clause mapping " + auditResult);
			return ValidLogResultT.Unknown.name();
		}
	}

	private String getWsAuditSeverity(LOG_SEVERITY auditSeverity) {
		switch (auditSeverity) {
		case INFO:
			return ValidLogSeverityT.Info.name();
		case WARNING:
			return ValidLogSeverityT.Warning.name();
		case MINOR:
			return ValidLogSeverityT.Minor.name();
		case MAJOR:
			return ValidLogSeverityT.Major.name();
		case CRITICAL:
			return ValidLogSeverityT.Critical.name();
		default:
			log.error("Default clause mapping " + auditSeverity);
			return ValidLogSeverityT.Info.name();
		}
	}

	private String getWsAuditType(LOG_TYPE auditType) {
		switch (auditType) {
		case NA:
			return ValidLogTypeT.Na.name();
		case UNKNOWN:
			return ValidLogTypeT.Unknown.name();
		case CREATED:
			return ValidLogTypeT.Created.name();
		case MODIFIED:
			return ValidLogTypeT.Modified.name();
		case DELETED:
			return ValidLogTypeT.Deleted.name();
		case CANCELED:
			return ValidLogTypeT.Canceled.name();
		case ALARM_RAISED:
			return ValidLogTypeT.AlarmRaised.name();
		case ALARM_CLEARED:
			return ValidLogTypeT.AlarmCleared.name();
		case MANAGED:
			return ValidLogTypeT.Managed.name();
		case UNMANAGED:
			return ValidLogTypeT.Unmanaged.name();
		case ALIGNED:
			return ValidLogTypeT.Aligned.name();
		case LOGGED_IN:
			return ValidLogTypeT.LoggedIn.name();
		case LOGGED_OUT:
			return ValidLogTypeT.LoggedOut.name();
		case EXECUTED:
			return ValidLogTypeT.Executed.name();
		case ACCESS_CHECK:
			return ValidLogTypeT.AccessCheck.name();
		case REDUNDANCY:
			return ValidLogTypeT.Redundancy.name();
		case VERIFIED:
			return ValidLogTypeT.Verified.name();
		default:
			log.error("Default clause mapping " + auditType);
			return ValidLogTypeT.Unknown.name();
		}
	}

	private void setAuditCategory(String auditCategory)
	    throws InvalidInputException {
		if (auditCategory == null) {
			return;
		}
		if (auditCategory.trim().isEmpty()) {
			return;

		}
		try {
			ValidLogCategoryT v = ValidLogCategoryT.valueOf(auditCategory);
			category = getAuditCategory(v);
		}
		catch (Exception e) {
			log.error("Unable to map category " + auditCategory + " to enum ", e);
			Object[] args = new Object[2];
			args[0] = auditCategory;
			args[1] = RequestResponseConstants.RAW_CATEGORY;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args, e);
		}
	}

	private void setLogType(String operationType) throws InvalidInputException {
		if (operationType == null) {
			return;
		}
		if (operationType.trim().isEmpty()) {
			return;

		}
		try {
			ValidLogTypeT v = ValidLogTypeT.valueOf(operationType);
			logType = getAuditType(v);
		}
		catch (Exception e) {
			log.error("Unable to map operationType " + operationType
			    + " to enum, is invalid.", e);
			Object[] args = new Object[2];
			args[0] = operationType;
			args[1] = RequestResponseConstants.RAW_OPERATION_TYPE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args, e);
		}
	}

	private void setResult(String res) throws InvalidInputException {
		if (res == null) {
			return;
		}
		if (res.trim().isEmpty()) {
			return;

		}
		try {
			ValidLogResultT v = ValidLogResultT.valueOf(res);
			result = getAuditResult(v);
		}
		catch (Exception e) {
			log.error("Unable to map result " + res + " to enum,is invalid.", e);
			Object[] args = new Object[2];
			args[0] = res;
			args[1] = RequestResponseConstants.RAW_RESULT;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args, e);
		}
	}

	private void setSeverity(String sev) throws InvalidInputException {
		if (sev == null) {
			return;
		}
		if (sev.trim().isEmpty()) {
			return;

		}
		try {
			ValidLogSeverityT v = ValidLogSeverityT.valueOf(sev);
			severity = getAuditSeverity(v);
		}
		catch (Exception e) {
			log.error("Unable to map severity " + sev + " to enum,is invalid.", e);
			Object[] args = new Object[2];
			args[0] = sev;
			args[1] = RequestResponseConstants.RAW_RESULT;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args, e);
		}
	}
}
