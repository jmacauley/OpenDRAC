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

package com.nortel.appcore.app.drac.server.ws.common;

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

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.AlarmType;
import com.nortel.appcore.app.drac.server.ws.common.request.QueryAlarmsRequest;

public final class Alarms {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private OMElement inputMessage;
	private final ServiceUtilities serviceUtil = new ServiceUtilities();
	private long startTime = -1;
	private long endTime = -1;
	private UserGroupName userGroup;

	public Alarms(OMElement inputMsg, String ns, String nsPrefix)
	    throws Exception {
		this.inputMessage = inputMsg;
		OMElement selectedNode = null;
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryAlarmsRequest.QUERY_CRITERIA)) != null) {

			OMAttribute criteria = selectedNode.getAttribute(new QName(
			    XML_SCHEMA_INSTANCE_NS, "type", XML_SCHEMA_INSTANCE_NS_PREFIX));
			// xsi is "http://www.w3.org/2001/XMLSchema-instance" in the
			// instance document
			if (criteria != null) {
				String criteriaValue = criteria.getAttributeValue();
				if (criteriaValue == null) {
					log.error("null criteria.");
					Object[] args = new Object[1];
					args[0] = RequestResponseConstants.RAW_QUERY_CRITERIA;
					throw new InvalidInputException(
					    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
				}
				if (criteriaValue.indexOf(":") != -1) {
					criteriaValue = criteriaValue
					    .substring(criteriaValue.indexOf(":") + 1);
					
				}
				if (criteriaValue
				    .compareTo(RequestResponseConstants.RAW_QUERY_ALARMS_BY_DATE_TIME_AND_USER_GROUP) != 0) {
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
		else {
			log.error("No query criteria found in the request.");
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_QUERY_CRITERIA;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// startTime
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryAlarmsRequest.START_TIME)) != null) {
			this.startTime = serviceUtil.convertToMillis(selectedNode);
		}
		// endTime
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryAlarmsRequest.END_TIME)) != null) {
			this.endTime = serviceUtil.convertToMillis(selectedNode);
		}
		if (this.endTime < 0) {
			Object[] args = new Object[2];
			args[0] = this.endTime;
			args[1] = RequestResponseConstants.RAW_END_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}
		if (this.startTime < 0) {
			Object[] args = new Object[2];
			args[0] = this.startTime;
			args[1] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}

		// userGroup
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, ns, nsPrefix,
		    QueryAlarmsRequest.USER_GROUP)) != null) {
			this.userGroup = new UserGroupName(selectedNode.getText().trim());
		}

	}

	public long getEndTime() throws InvalidInputException {
		if (endTime < 0) {
			log.error("endTime is invalid");
			Object[] args = new Object[2];
			args[0] = this.endTime;
			args[1] = RequestResponseConstants.RAW_END_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}
		return endTime;
	}

	public long getStartTime() throws InvalidInputException {
		if (startTime < 0) {
			log.error("startTime is invalid");
			Object[] args = new Object[2];
			args[0] = this.startTime;
			args[1] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}
		return startTime;
	}

	public UserGroupName getUserGroup() {
		return this.userGroup;
	}

	public OMElement prepareResponseOMElement(List<AlarmType> alarms, String ns,
	    String nsPrefix, String userTimeZoneIdPreference) {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = fac.createOMNamespace(ns, nsPrefix);
		OMNamespace commonNs = fac.createOMNamespace(
		    NamespaceConstants.COMMON_DATA_TYPES_NS,
		    NamespaceConstants.DEFAULT_COMMON_NS_PREFIX);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_QUERY_ALARMS_RESPONSE, namespace);
		// add common namespace
		response.declareNamespace(commonNs);
		OMElement numOfElementsIncluded = fac.createOMElement(
		    RequestResponseConstants.RAW_NUM_OF_ELEMENTS_INCLUDED, namespace,
		    response);
		OMElement totalNumOfMatchingElements = fac.createOMElement(
		    RequestResponseConstants.RAW_TOTAL_NUM_OF_MATCHING_ELEMENTS, namespace,
		    response);
		if (alarms == null) {
			numOfElementsIncluded.setText("0");
			totalNumOfMatchingElements.setText("0");
			return response;
		}
		if (alarms.isEmpty()) {
			numOfElementsIncluded.setText("0");
			totalNumOfMatchingElements.setText("0");
			return response;
		}
		// totalNumOfMatchingElements
		totalNumOfMatchingElements.setText(new Integer(alarms.size()).toString());
		boolean reduceList = true;
		if (alarms.size() < CommonConstants.MAX_NUM_OF_ALARMS_TO_BE_RETRIEVED) {
			numOfElementsIncluded.setText(new Integer(alarms.size()).toString());
			reduceList = false;
		}
		else {
			numOfElementsIncluded.setText(new Integer(
			    CommonConstants.MAX_NUM_OF_ALARMS_TO_BE_RETRIEVED).toString());
		}
		int i = 0;
		for (AlarmType alarm : alarms) {
			OMElement alarmtag = fac.createOMElement(
			    RequestResponseConstants.RAW_ALARM, namespace, response);
			// id
			OMElement id = fac.createOMElement(RequestResponseConstants.RAW_ALARM_ID,
			    commonNs, alarmtag);
			id.setText(alarm.getId());

			// alarm source id (service id)
			OMElement alarmSourceId = fac.createOMElement(
			    RequestResponseConstants.RAW_ALARM_SOURCE_ID, commonNs, alarmtag);
			alarmSourceId.setText(alarm.getServiceId());

			// severity
			OMElement severity = fac.createOMElement(
			    RequestResponseConstants.RAW_SEVERITY, commonNs, alarmtag);
			severity.setText(alarm.getSeverity());

			// timeRaised
			OMElement timeRaised = fac
			    .createOMElement(RequestResponseConstants.RAW_ALARM_RAISE_DATETIME,
			        commonNs, alarmtag);
			timeRaised.setText(serviceUtil.convertMillisToISO8601FormattedString(
			    alarm.getOccurTime(), userTimeZoneIdPreference));

			// isCleared
			OMElement isCleared = fac.createOMElement(
			    RequestResponseConstants.IS_ALARM_CLEARED, commonNs, alarmtag);
			if (alarm.getDuration() > 0) {
				isCleared.setText(new Boolean("true").toString());
				long cleartime = alarm.getOccurTime() + alarm.getDuration();
				// timeCleared
				OMElement timeCleared = fac.createOMElement(
				    RequestResponseConstants.RAW_ALARM_CLEARED_DATETIME, commonNs,
				    alarmtag);
				timeCleared.setText(serviceUtil.convertMillisToISO8601FormattedString(
				    cleartime, userTimeZoneIdPreference));
			}
			else {
				isCleared.setText(new Boolean("false").toString());
			}

			// description
			OMElement description = fac.createOMElement(
			    RequestResponseConstants.RAW_DESCRIPTION, commonNs, alarmtag);
			description.setText(alarm.getDescription());
			// additionalInfo
			OMElement additionalInfo = fac.createOMElement(
			    RequestResponseConstants.RAW_ADDITIONAL_INFO, commonNs, alarmtag);
			additionalInfo.setText("Parent reservation scheduleName and id = "
			    + alarm.getScheduleName() + ", " + alarm.getScheduleId());
			i++;
			if (reduceList && i == CommonConstants.MAX_NUM_OF_ALARMS_TO_BE_RETRIEVED) {
				break;
			}
		}
		
		return response;
	}

	public void setInputMessage(OMElement inputMsg) {
		this.inputMessage = inputMsg;
	}

	@Override
	public String toString() {
		return "startTime = " + this.startTime + ", endTime = " + endTime
		    + ", userGroup = " + this.userGroup;
	}
}
