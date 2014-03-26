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

package com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice;

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
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.server.ws.common.CommonConstants;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.QueryReservationSchedulesRequest;

public final class ReservationSchedules {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private OMElement inputMessage;
	private long startTime = -1;
	private long endTime = -1;
	private final ServiceUtilities serviceUtil = new ServiceUtilities();
	private UserGroupName userGroup;

	public ReservationSchedules(OMElement inputMsg, String namespace)
	    throws InvalidInputException {
		this.inputMessage = inputMsg;
		OMElement selectedNode = null;
		String nsPrefix = serviceUtil
		    .getServiceNamespacePrefix(inputMsg, namespace);

		if ((selectedNode = serviceUtil.getNode(this.inputMessage, namespace,
		    nsPrefix, QueryReservationSchedulesRequest.QUERY_CRITERIA)) != null) {
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
				    .compareTo(RequestResponseConstants.RAW_QUERY_RESERVATION_SCHEDULES_BY_DATE_TIME_AND_USER_GROUP) != 0) {
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

		// userGroup
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, namespace,
		    nsPrefix, QueryReservationSchedulesRequest.USER_GROUP_NAME)) != null) {
			this.userGroup = new UserGroupName(selectedNode.getText().trim());
		}

		// 

		// startTime
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, namespace,
		    nsPrefix, QueryReservationSchedulesRequest.START_TIME)) != null) {
			this.startTime = serviceUtil.convertToMillis(selectedNode);
		}
		// endTime
		if ((selectedNode = serviceUtil.getNode(this.inputMessage, namespace,
		    nsPrefix, QueryReservationSchedulesRequest.END_TIME)) != null) {
			this.endTime = serviceUtil.convertToMillis(selectedNode);
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

	public OMElement prepareResponseOMElement(List<Schedule> schedules,
	    String nsPrefix, String namespace) {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}

		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(namespace, nsPrefix);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_QUERY_RESERVATION_SCHEDULES_RESPONSE, ns);
		OMElement numOfElements = fac.createOMElement(
		    RequestResponseConstants.RAW_NUM_OF_ELEMENTS_INCLUDED, ns, response);
		OMElement totalNumOfElements = fac.createOMElement(
		    RequestResponseConstants.RAW_TOTAL_NUM_OF_MATCHING_ELEMENTS, ns,
		    response);
		if (schedules == null) {
			numOfElements.setText("0");
			totalNumOfElements.setText("0");
			return response;
		}
		if (schedules.isEmpty()) {
			numOfElements.setText("0");
			totalNumOfElements.setText("0");
			return response;
		}
		boolean reduceList = true;
		totalNumOfElements.setText(new Integer(schedules.size()).toString());
		if (schedules.size() < CommonConstants.MAX_NUM_OF_RESERVATIONS_TO_BE_RETRIEVED) {
			numOfElements.setText(new Integer(schedules.size()).toString());
			reduceList = false;
		}
		else {
			numOfElements.setText(new Integer(
			    CommonConstants.MAX_NUM_OF_RESERVATIONS_TO_BE_RETRIEVED).toString());
		}

		int i = 0;
		for (Schedule schedule : schedules) {
			OMElement reservationSchedule = fac.createOMElement(
			    RequestResponseConstants.RAW_RESERVATION_SCHEDULE, ns, response);
			OMElement resScheduleId = fac.createOMElement(
			    RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ID, ns,
			    reservationSchedule);
			resScheduleId.setText(schedule.getId());

			OMElement resScheduleName = fac.createOMElement(
			    RequestResponseConstants.RAW_RESERVATION_SCHEDULE_NAME, ns,
			    reservationSchedule);
			resScheduleName.setText(schedule.getName());

			i++;
			if (reduceList
			    && i == CommonConstants.MAX_NUM_OF_RESERVATIONS_TO_BE_RETRIEVED) {
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
