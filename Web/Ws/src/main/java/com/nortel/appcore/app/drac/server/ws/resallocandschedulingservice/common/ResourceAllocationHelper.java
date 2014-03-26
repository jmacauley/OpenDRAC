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

package com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.common;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS;
import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.XML_SCHEMA_INSTANCE_NS;
import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.XML_SCHEMA_INSTANCE_NS_PREFIX;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlGDay;
import org.apache.xmlbeans.XmlGMonthDay;
import org.opendrac.drac.server.ws.common.NamespaceConstantsV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.PathType.PROTECTION_TYPE;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType.RecurrenceFreq;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.common.types.StatusType;
import com.nortel.appcore.app.drac.common.types.StatusType.StatusInfoType;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.types.TaskType.Result;
import com.nortel.appcore.app.drac.common.types.UserType;
import com.nortel.appcore.app.drac.common.utility.DateTimeConverter;
import com.nortel.appcore.app.drac.server.ws.common.CommonConstants;
import com.nortel.appcore.app.drac.server.ws.common.InvalidInputException;
import com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.ActivateReservationOccurrenceRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.AddReservationOccurrenceRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.AlterServiceRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.CancelReservationOccurrenceRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.CancelReservationScheduleRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.ConfirmReservationScheduleRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.CreateReservationScheduleRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.CreateScheduleInfoRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.QueryPathAvailabilityRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.QueryReservationOccurrenceRequest;
import com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request.QueryReservationScheduleRequest;

public final class ResourceAllocationHelper {
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private static enum ReservationOccurrenceCreationResult {
		Succeeded, Failed, Unknown
	}

	private static enum ReservationScheduleCreationResult {
		Succeeded, SucceededPartially, Failed, Unknown
	}

	private static enum ValidReservationOcurrenceStatusTypes {
		ConfirmationPending, ConfirmationTimedOut, ConfirmationCancelled, ActivationPending, ActivationTimedOut, ActivationCancelled, ExecutionPending, ExecutionInProgress, ExecutionTimedOut, ExecutionSucceeded, ExecutionFailed, ExecutionPartiallyCancelled, ExecutionCancelled, Unknown
	}

	private static enum ValidReservationScheduleStatusTypes {
		ConfirmationPending, ConfirmationTimedOut, ConfirmationCancelled, ExecutionPending, ExecutionInProgress, ExecutionTimedOut, ExecutionSucceeded, ExecutionPartiallySucceeded, ExecutionFailed, ExecutionPartiallyCancelled, ExecutionCancelled, Unknown
	}

	private static enum ValidReservationScheduleTypes {
		PreReservationScheduleManual, PreReservationScheduleAutomatic, ReservationScheduleManual, ReservationScheduleAutomatic, Unknown
	}

	private final ServiceUtilities serviceUtil = new ServiceUtilities();
	private static final String UNPROTECTED_PROTECTION_TYPE = "Unprotected";
	private static final String UNKNOWN_PROTECTION_TYPE = "Unknown";
	private static final String ONE_PLUS_ONE_PATH_PROTECTION_TYPE = "1Plus1Path";
	private static final String RESOURCE_TYPE = "resource type";
	private static final String RECURRENCE_TYPE = "recurrence type";
	private static final String RECURRENCE_RANGE_TYPE = "range type";
	private static final String CREATED_SUCCESSFULLY = "Created successfully";

	private String namespace = RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS;

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String parseActivateReservationOccurrenceRequestOMElement(
	    OMElement inputMsg) throws Exception {
		log.debug("parseActivateReservationOccurrenceRequestOMElement "
		    + inputMsg.toString());
		String nsPrefix = serviceUtil
		    .getServiceNamespacePrefix(inputMsg, namespace);
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    ActivateReservationOccurrenceRequest.OCCURRENCE_ID);
		if (selectedNode != null) {
			return selectedNode.getText().trim();

		}
		Object[] args = new Object[1];
		args[0] = RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ID;
		throw new InvalidInputException(
		    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
	}

	public DracService parseAddReservationOccurrenceRequestOMElement(
	    String userId, OMElement inputMsg) throws Exception {
		log.debug("parseAddReservationOccurrenceRequestOMElement "
		    + inputMsg.toString());
		DracService newOccurrence = new DracService();
		String nsPrefix = serviceUtil
		    .getServiceNamespacePrefix(inputMsg, namespace);

		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    AddReservationOccurrenceRequest.SCHEDULE_ID);
		if (selectedNode == null) {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ID;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		newOccurrence.setScheduleId(selectedNode.getText().trim());

		// occurrence start time
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    AddReservationOccurrenceRequest.RECURRENCE_STARTTIME);
		if (selectedNode != null) {
			newOccurrence.setStartTime(serviceUtil.convertToMillis(selectedNode));
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// shared risk reservation occurrence group
		selectedNode = serviceUtil
		    .getNode(
		        inputMsg,
		        namespace,
		        nsPrefix,
		        AddReservationOccurrenceRequest.SHARED_RISK_RESERVATION_OCCURRENCE_GROUP);
		if (selectedNode != null) {
			newOccurrence.getPath().setSharedRiskServiceGroup(
			    selectedNode.getText().trim());
		}

		// srlgExclusions
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    AddReservationOccurrenceRequest.SLRG_EXCLUSIONS);
		if (selectedNode != null) {
			newOccurrence.getPath().setSrlg(selectedNode.getText().trim());
		}

		return newOccurrence;
	}

	public String parseCancelReservationOccurrenceRequestOMElement(
	    OMElement inputMsg) throws Exception {
		log.debug("parseCancelReservationOccurrenceRequestOMElement " + inputMsg);
		String nsPrefix = serviceUtil
		    .getServiceNamespacePrefix(inputMsg, namespace);
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CancelReservationOccurrenceRequest.OCCURRENCE_ID);
		if (selectedNode != null) {
			return selectedNode.getText().trim();
		}

		Object[] args = new Object[1];
		args[0] = RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ID;
		throw new InvalidInputException(
		    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
	}

	public String parseCancelReservationScheduleRequestOMElement(
	    OMElement inputMsg) throws Exception {
		log.debug("parseCancelReservationScheduleRequestOMElement " + inputMsg);
		String nsPrefix = serviceUtil
		    .getServiceNamespacePrefix(inputMsg, namespace);
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CancelReservationScheduleRequest.SCHEDULE_ID);
		if (selectedNode != null) {
			return selectedNode.getText().trim();
		}
		Object[] args = new Object[1];
		args[0] = RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ID;
		throw new InvalidInputException(
		    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
	}

	public String parseConfirmReservationScheduleRequestOMElement(
	    OMElement inputMsg) throws Exception {
		log.debug("parseConfirmReservationScheduleRequestOMElement " + inputMsg);
		String nsPrefix = serviceUtil
		    .getServiceNamespacePrefix(inputMsg, namespace);
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    ConfirmReservationScheduleRequest.SCHEDULE_ID);
		if (selectedNode != null) {
			return selectedNode.getText().trim();
		}
		Object[] args = new Object[1];
		args[0] = RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ID;
		throw new InvalidInputException(
		    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
	}

	public PathType parsePathTypeSchedule(String userId, OMElement inputMsg) throws Exception {
		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
		PathType pathType = genPathTypeGeneric(inputMsg, nsPrefix);
		return pathType;
	}

	public String parseScheduleName(String userId, OMElement inputMsg) throws Exception {
		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
		return getServiceNameGeneric(inputMsg, nsPrefix);
	}

	public String parseScheduleId(String userId, OMElement inputMsg) throws Exception {
		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
		return getScheduleIdGeneric(inputMsg, nsPrefix);
	}
	
	public Integer parseTimeExtension(String userId, OMElement inputMsg) throws Exception {
		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
		String strValue =  getMinutesToExtendService(inputMsg, nsPrefix);
		return Integer.valueOf(strValue);
	}	
	public String parseScheduleIdForExtension(String userId, OMElement inputMsg) throws Exception {
		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
		return  getScheduleIdToExtendService(inputMsg, nsPrefix);
	}	
	
	public String parseServiceId(String userId, OMElement inputMsg) throws Exception {
		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);
		return getServiceIdGeneric(inputMsg, nsPrefix);
	}

	
	public Schedule parseCreateReservationScheduleRequestOMElement(String userId, OMElement inputMsg) throws Exception {
		log.debug("parseCreateReservationScheduleRequestOMElement instanceof "
		    + inputMsg.getClass().getName() + " contents: " + inputMsg.toString());

		String nsPrefix = serviceUtil.getServiceNamespacePrefix(inputMsg, namespace);

		String reservationType = null;

		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.TYPE);
		if (selectedNode != null) {
			reservationType = selectedNode.getText().trim();
		}

		ACTIVATION_TYPE type;
		if (reservationType != null) {
			if (reservationType
			    .equals(ValidReservationScheduleTypes.PreReservationScheduleManual
			        .name())) {
				type = ACTIVATION_TYPE.PRERESERVATION_MANUAL;
			}
			else if (reservationType
			    .equals(ValidReservationScheduleTypes.PreReservationScheduleAutomatic
			        .name())) {
				type = ACTIVATION_TYPE.PRERESERVATION_AUTOMATIC;
			}
			else if (reservationType
			    .equals(ValidReservationScheduleTypes.ReservationScheduleManual
			        .name())) {
				type = ACTIVATION_TYPE.RESERVATION_MANUAL;
			}
			else if (reservationType
			    .equals(ValidReservationScheduleTypes.ReservationScheduleAutomatic
			        .name())) {
				type = ACTIVATION_TYPE.RESERVATION_AUTOMATIC;
			}
			else {
				Object[] args = new Object[2];
				args[0] = reservationType;
				args[1] = RequestResponseConstants.RAW_TYPE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
				    args);
			}
		}
		else {
			type = ACTIVATION_TYPE.RESERVATION_AUTOMATIC;
		}

		// schedule name
		String scheduleName = "unAssigned";
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.NAME);
		if (selectedNode == null) {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_NAME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		if (selectedNode.getText() != null) {
			if (selectedNode.getText().trim().equals("")) {
				Object[] args = new Object[2];
				args[0] = "";
				args[1] = RequestResponseConstants.RAW_NAME;
				throw new InvalidInputException(
				    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
				    args);
			}
			scheduleName = selectedNode.getText().trim();
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_NAME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		long scheduleStartTime = 0;
		XmlCalendar origStartTime = null;
		// start time
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.STARTTIME);
		if (selectedNode != null) {
			origStartTime = new XmlCalendar(selectedNode.getText().trim());
			scheduleStartTime = serviceUtil.convertToMillis(selectedNode);
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		// duration (in minutes in the request message)
		long duration;
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.DURATION);
		if (selectedNode != null) {
			String dur = selectedNode.getText().trim();

			long durI = Long.parseLong(dur);
			duration = durI * 60 * 1000;
			log.debug("Parsing duration string <" + dur + "> into long " + durI
			    + " into millisecond time " + duration);

			// 
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_DURATION;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateReservationScheduleRequest.PATH_V3);
		}
		else {
			selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateReservationScheduleRequest.RESOURCE);

			if (selectedNode != null) {
				OMAttribute resourceType = selectedNode.getAttribute(new QName(
				    XML_SCHEMA_INSTANCE_NS, "type", XML_SCHEMA_INSTANCE_NS_PREFIX));
				if (resourceType != null) {
					String resourceTypeValue = resourceType.getAttributeValue();
					if (resourceTypeValue == null) {
						Object[] args = new Object[1];
						args[0] = RESOURCE_TYPE;
						throw new InvalidInputException(
						    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
						    args);
					}
					if (resourceTypeValue.indexOf(":") != -1) {
						resourceTypeValue = resourceTypeValue.substring(resourceTypeValue
						    .indexOf(":") + 1);
						log.debug("resource type = " + resourceTypeValue);
					}

					if (!resourceTypeValue
					    .equals(RequestResponseConstants.RAW_PATH_REQUEST_RESOURCE_TYPE)) {
						// Unsupported resource type value
						Object[] args = new Object[2];
						args[0] = resourceTypeValue;
						args[1] = RequestResponseConstants.RAW_TYPE;
						throw new InvalidInputException(
						    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
						    args);
					}
				}
			}
			else {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_RESOURCE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}

		PathType schedulePath = genPathType(inputMsg, nsPrefix);

		boolean isRecurring = false;

		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.IS_RECURRING)) != null) {
			isRecurring = new Boolean(selectedNode.getText().trim()).booleanValue();
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_IS_RECURRING;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		UserType scheduleUserInfoType = generateUserInfoType(inputMsg, nsPrefix,
		    userId);

		if (!isRecurring) {
			log.debug("Recurrence frequency is ONCE");
			return new Schedule(type, null, scheduleName,
			    State.SCHEDULE.EXECUTION_PENDING, scheduleStartTime,
			    Long.valueOf(scheduleStartTime + duration), duration,
			    scheduleUserInfoType, schedulePath, false, new RecurrenceType(), null);
		}

		RecurrenceType scheduleRecurrenceType;
		Long scheduleEndTime = null;

		// Schedule is a recurring schedule, check the type of the
		// recurrence. This section is ugly because there will be handling of
		// V2 and V3 WSDL interspersed
		String recurrenceTypeValue = null;
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateReservationScheduleRequest.RECURRENCE_TYPE)) != null) {

				OMAttribute recurrenceType = selectedNode.getAttribute(new QName(
				    XML_SCHEMA_INSTANCE_NS, "type", XML_SCHEMA_INSTANCE_NS_PREFIX));
				if (recurrenceType != null) {
					recurrenceTypeValue = recurrenceType.getAttributeValue();
					log.debug("recurrenceTypeValue = " + recurrenceTypeValue);
				}
			}
			else {
				Object[] args = new Object[1];
				args[0] = RECURRENCE_TYPE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}
		else // V3
		{
			if ((selectedNode = serviceUtil.getNode(inputMsg,
			    RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS, nsPrefix,
			    CreateReservationScheduleRequest.RECURRENCE_TYPE_V3)) != null) {
				recurrenceTypeValue = selectedNode.getText().trim();
				log.debug("recurrenceTypeValue = " + recurrenceTypeValue);
			}
		}

		if (recurrenceTypeValue == null) {
			Object[] args = new Object[1];
			args[0] = RECURRENCE_TYPE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		if (recurrenceTypeValue.indexOf(":") != -1) {
			recurrenceTypeValue = recurrenceTypeValue.substring(recurrenceTypeValue
			    .indexOf(":") + 1);
		}

		if (recurrenceTypeValue
		    .equals(RequestResponseConstants.RAW_DAILY_RECURRENCE_TYPE)) {
			// 
			scheduleRecurrenceType = new RecurrenceType(RecurrenceFreq.FREQ_DAILY, 0,
			    0, null);
		}
		else if (recurrenceTypeValue
		    .equals(RequestResponseConstants.RAW_WEEKLY_RECURRENCE_TYPE)) {
			// 
			int i = 0;
			ArrayList<Integer> weekdays = new ArrayList<Integer>();
			for (i = 0; i <= CommonConstants.MAX_NUM_OF_WEEKDAYS; i++) {
				if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
				    CreateReservationScheduleRequest.getRecurrenceWeekday(namespace)
				        + "[" + (i + 1) + "]")) != null) {
					weekdays.add(Integer.parseInt(selectedNode.getText().trim()));
				}
				else {
					break;
				}
			}
			if (weekdays.size() == 0) {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_RECURRENCE_WEEKDAY;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
			scheduleRecurrenceType = new RecurrenceType(RecurrenceFreq.FREQ_WEEKLY,
			    0, 0, serviceUtil.intArrayListToIntArray(weekdays));
		}
		else if (recurrenceTypeValue
		    .equals(RequestResponseConstants.RAW_MONTHLY_RECURRENCE_TYPE)) {
			// 
			if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateReservationScheduleRequest.getRecurrenceDayOfMonth(namespace))) != null) {
				String day = selectedNode.getText().trim();
				scheduleRecurrenceType = new RecurrenceType(
				    RecurrenceFreq.FREQ_MONTHLY, XmlGDay.Factory.newValue(day)
				        .getIntValue(), 0, null);
			}
			else {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_RECURRENCE_DAY;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}
		else if (recurrenceTypeValue
		    .equals(RequestResponseConstants.RAW_YEARLY_RECURRENCE_TYPE)) {

			// 
			if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateReservationScheduleRequest.getRecurrenceDayAndMonth(namespace))) != null) {
				int whichDay = XmlGMonthDay.Factory
				    .newValue(selectedNode.getText().trim()).getGDateValue().getDay();
				int whichMonth = XmlGMonthDay.Factory
				    .newValue(selectedNode.getText().trim()).getGDateValue().getMonth();
				if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
					whichMonth = whichMonth - 1; // offset
				}
				scheduleRecurrenceType = new RecurrenceType(RecurrenceFreq.FREQ_YEARLY,
				    whichDay, whichMonth, null);
			}
			else {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_DAY_OF_MONTH;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}
		else {
			// unsupported recurrence type value
			Object[] args = new Object[2];
			args[0] = recurrenceTypeValue;
			args[1] = RequestResponseConstants.RAW_TYPE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
			    args);
		}

		// range of recurrence
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.RECURRENCE_RANGE)) != null) {
			// 

			if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
				OMAttribute recurrenceRange = selectedNode.getAttribute(new QName(
				    XML_SCHEMA_INSTANCE_NS, "type", XML_SCHEMA_INSTANCE_NS_PREFIX));

				if (recurrenceRange != null) {
					String recurrenceRangeValue = recurrenceRange.getAttributeValue();
					if (recurrenceRangeValue == null) {
						Object[] args = new Object[1];
						args[0] = RECURRENCE_RANGE_TYPE;
						throw new InvalidInputException(
						    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
						    args);
					}
					if (recurrenceRangeValue.indexOf(":") != -1) {
						recurrenceRangeValue = recurrenceRangeValue
						    .substring(recurrenceRangeValue.indexOf(":") + 1);
						if (!recurrenceRangeValue
						    .equals(RequestResponseConstants.RAW_END_AFTER_DATE_RECURRENCE_RANGE)) {
							// unsupported recurrence range type value
							Object[] args = new Object[2];
							args[0] = recurrenceRangeValue;
							args[1] = RequestResponseConstants.RAW_TYPE;
							throw new InvalidInputException(
							    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
							    args);
						}
					}
				}
			}
			else // V3
			{
				if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
				    CreateReservationScheduleRequest.RECURRENCE_RANGE_END_DATE)) == null) {
					Object[] args = new Object[1];
					args[0] = RequestResponseConstants.RAW_END_DATE;
					throw new InvalidInputException(
					    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
				}
			}

			if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateReservationScheduleRequest.RECURRENCE_RANGE_END_DATE)) != null) {
				// xml type of the endsAfterDate is date
				// schedule end datetime is realy =
				// schedule.endDate
				// + schedule.startTime + duration
				// For example if schedule start time is
				// Apr-05-2007
				// 17:30 and duration is 30 min. and endDate is
				// June-10-2007
				// then the schedule end datetime should be
				// June-10-2007 18:00 (17:30+30min.)
				//
				log.debug("Setting reoccuring schedule end date based on raw end date of "
				    + selectedNode.getText().trim() + " and duration of " + duration);
				XmlDate endDate = XmlDate.Factory.newInstance();
				endDate.setStringValue(selectedNode.getText().trim());
				long endOfFirstOccurrence = origStartTime.getTimeInMillis() + duration;
				Calendar endTime = DateTimeConverter.getCalendarInUTC();
				endTime.setTimeInMillis(endOfFirstOccurrence);
				endTime.set(Calendar.DAY_OF_MONTH,
				    endDate.getCalendarValue().get(Calendar.DAY_OF_MONTH));
				endTime.set(Calendar.MONTH,
				    endDate.getCalendarValue().get(Calendar.MONTH));
				endTime.set(Calendar.YEAR, endDate.getCalendarValue()
				    .get(Calendar.YEAR));

				scheduleEndTime = Long.valueOf(endTime.getTimeInMillis());

			}
			else {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_END_DATE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_RECURRENCE_RANGE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		return new Schedule(type, null, scheduleName,
		    State.SCHEDULE.EXECUTION_PENDING, scheduleStartTime, scheduleEndTime,
		    duration, scheduleUserInfoType, schedulePath, true,
		    scheduleRecurrenceType, null);

	}

	public DracService parseQueryPathAvailabilityRequestOMElement(String userId,
	    OMElement inputMsg) throws Exception {
		log.debug("parseQueryPathAvailabilityRequestOMElement " + inputMsg);
		OMElement selectedNode = null;
		DracService service = new DracService();
		String nsPrefix = serviceUtil
		    .getServiceNamespacePrefix(inputMsg, namespace);

		// start time
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.STARTTIME)) != null) {
			service.setStartTime(serviceUtil.convertToMillis(selectedNode));
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_START_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		// end time
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.ENDTIME)) != null) {
			service.setEndTime(serviceUtil.convertToMillis(selectedNode));
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_END_TIME;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		if (namespace
		    .equals(NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    QueryPathAvailabilityRequest.getPath(namespace));
			if (selectedNode == null) {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_PATH;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}
		else // V2 using resource
		{
			if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    QueryPathAvailabilityRequest.RESOURCE)) != null) {
				OMAttribute resourceType = selectedNode.getAttribute(new QName("xsi",
				    "type"));
				// xsi is "http://www.w3.org/2001/XMLSchema-instance" in the
				// instance document
				if (resourceType != null) {
					String resourceTypeValue = resourceType.getAttributeValue();
					if (resourceTypeValue == null) {
						Object[] args = new Object[1];
						args[0] = RESOURCE_TYPE;
						throw new InvalidInputException(
						    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
						    args);
					}
					if (!resourceTypeValue
					    .equals(RequestResponseConstants.RAW_PATH_REQUEST_RESOURCE_TYPE)) {
						// unsupported resource type
						Object[] args = new Object[2];
						args[0] = resourceTypeValue;
						args[1] = RequestResponseConstants.RAW_TYPE;
						throw new InvalidInputException(
						    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
						    args);
					}
				}
			}
			else {
				Object[] args = new Object[1];
				args[0] = RequestResponseConstants.RAW_RESOURCE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
			}
		}

		// resource
		// source endpoint
		// channelNumber
		// channelNumber is an integer. 0 is not valid, so starts with channel
		// 1.
		// Value -1 means don't care, find an available channel...
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getSourceEndpointChannel(namespace))) != null) {
			int srcCh = -1;
			try {
				srcCh = Integer.parseInt(selectedNode.getText().trim());
			}
			catch (NumberFormatException e) {
				log.error("Source channel is not a number");
			}
			service.getPath().getSourceEndPoint().setChannelNumber(srcCh);
			log.debug("sourceChannelNumber = "
			    + service.getPath().getSourceEndPoint().getChannelNumber());
		}

		// if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		// QueryPathAvailabilityRequest.VLANID)) != null)
		// {
		// // Set vlanId
		// service.getPath().setVlanId(selectedNode.getText().trim());
		// 
		// }

		// tna
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getSourceEndpointTna(namespace))) != null) {
			service.getPath().getSourceEndPoint()
			    .setName(selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_SOURCE_TNA;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// target endpoint
		// channelNumber
		// channelNumber is an integer. 0 is not valid, so starts with channel
		// 1.
		// Value -1 means don't care, find an available channnel...
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getTargetEndpointChannel(namespace))) != null) {
			int destCh = -1;
			try {
				destCh = Integer.parseInt(selectedNode.getText().trim());
			}
			catch (NumberFormatException e) {
				log.error("Target channel is not a number");
			}
			service.getPath().getTargetEndPoint().setChannelNumber(destCh);
			log.debug("targetChannelNumber = "
			    + service.getPath().getTargetEndPoint().getChannelNumber());
		}

		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getTargetEndpointTna(namespace))) != null) {
			service.getPath().getTargetEndPoint()
			    .setName(selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_TARGET_TNA;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// rate
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getRate(namespace))) != null) {
			service.setRate(Integer.parseInt(selectedNode.getText().trim()));
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_RATE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// srlg exclusions
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getSrlgExclusions(namespace))) != null) {
			service.getPath().setSrlg(selectedNode.getText().trim());
		}

		// cost
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getCost(namespace))) != null) {
			int value = 0;
			try {
				value = Integer.parseInt(selectedNode.getText().trim());
				service.getPath().setCost(value);
			}
			catch (NumberFormatException e) {
				log.error("Cost is not a number, not setting");
			}

		}

		// metric
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getMetric(namespace))) != null) {
			int value = 0;
			try {
				value = Integer.parseInt(selectedNode.getText().trim());
				service.getPath().setMetric(value);
			}
			catch (NumberFormatException e) {
				log.error("Metric is not a number, not setting");
			}
		}

		// hop
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getHop(namespace))) != null) {
			int value = 0;
			try {
				value = Integer.parseInt(selectedNode.getText().trim());
				service.getPath().setHop(value);
			}
			catch (NumberFormatException e) {
				log.error("Hop is not a number, not setting");
			}
		}

		// routingMetric
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getRoutingMetric(namespace))) != null) {
			int value = 0;
			try {
				value = Integer.parseInt(selectedNode.getText().trim());
				service.getPath().setRoutingMetric(value);
			}
			catch (NumberFormatException e) {
				log.error("Routing metric  is not a number, not setting");
			}
		}

		// TODO: Routing algorithm needs to be set once the server side starts
		// to process it here
		// ....
		
		// RH: Added for query path availability
		
	// routingAlgorithm - ccat/vcat
    if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
        CreateScheduleInfoRequest.getRoutingAlgorithm(namespace))) != null) {
      String routingAlgorithm = selectedNode.getText().trim();
      if ("vcat".equalsIgnoreCase(routingAlgorithm)) {
        service.getPath().setVcatRoutingOption(true);
      }
      else if ("ccat".equalsIgnoreCase(routingAlgorithm)) {
        service.getPath().setVcatRoutingOption(false);
      }
    }
		

		// protectionType
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.getProtectionType(namespace))) != null) {
			String protectionType = selectedNode.getText().trim();
			if (!protectionType.isEmpty()) {
				// non empty string, check it
				if (protectionType.equals(UNPROTECTED_PROTECTION_TYPE)) {
					service.getPath().setProtectionType(PROTECTION_TYPE.UNPROTECTED);
				}
				else if (protectionType.equals(ONE_PLUS_ONE_PATH_PROTECTION_TYPE)) {
					service.getPath().setProtectionType(PROTECTION_TYPE.PATH1PLUS1);
				}
				else {
					// unsupported protection type value
					Object[] args = new Object[2];
					args[0] = protectionType;
					args[1] = RequestResponseConstants.RAW_PROTECTION_TYPE;
					throw new InvalidInputException(
					    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
					    args);
				}
			}
		}

		// Shared risk reservation occurrence group
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest
		        .getSharedRiskReservationOccurrenceGroup(namespace))) != null) {
			String value = selectedNode.getText().trim();
			if (!value.isEmpty()) {
				// non empty string, set it
				service.getPath().setSharedRiskServiceGroup(value);
			}
		}

		if (service.getUserInfo() == null) {
			UserType userType = new UserType(null, null, null, null, null, null, null);
			service.setUserInfo(userType);
		}

		// billingGroup
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.BILLING_GROUP)) != null) {
			service.getUserInfo().setBillingGroup(
			    new UserGroupName(selectedNode.getText().trim()));
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_BILLING_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// sourceEndpointResourceGroup
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.SOURCE_ENDPOINT_RESOURCE_GROUP)) != null) {
			service.getUserInfo().setSourceEndpointResourceGroup(
			    selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_SOURCE_ENDPOINT_RESOURCE_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// targetEndpointResourceGroup
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.TARGET_ENDPOINT_RESOURCE_GROUP)) != null) {
			service.getUserInfo().setTargetEndpointResourceGroup(
			    selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_TARGET_ENDPOINT_RESOURCE_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// sourceEndpointUserGroup
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.SOURCE_ENDPOINT_USER_GROUP)) != null) {
			service.getUserInfo().setSourceEndpointUserGroup(
			    selectedNode.getText().trim());
		}

		// targetEndpointUserGroup
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.TARGET_ENDPOINT_USER_GROUP)) != null) {
			service.getUserInfo().setTargetEndpointUserGroup(
			    selectedNode.getText().trim());
		}

		// e-mailAddress
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryPathAvailabilityRequest.EMAIL_ADDRESS)) != null) {
			service.getUserInfo().setEmailAddress(selectedNode.getText().trim());
		}
		// userId
		service.getUserInfo().setUserId(userId);

		return service;
	}

	public String parseQueryReservationOccurrenceRequestOMElement(
	    OMElement inputMsg, String nsPrefix) throws Exception {
		log.debug("parseQueryReservationOccurrenceRequestOMElement " + inputMsg);
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryReservationOccurrenceRequest.OCCURRENCE_ID);
		if (selectedNode != null) {
			return selectedNode.getText().trim();
		}
		Object[] args = new Object[1];
		args[0] = RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ID;
		throw new InvalidInputException(
		    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
	}

	public String parseQueryReservationScheduleRequestOMElement(
	    OMElement inputMsg, String nsPrefix) throws Exception {
		log.debug("parseQueryReservationScheduleRequestOMElement " + inputMsg);
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    QueryReservationScheduleRequest.SCHEDULE_ID);
		if (selectedNode != null) {
			return selectedNode.getText().trim();
		}
		Object[] args = new Object[1];
		args[0] = RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ID;
		throw new InvalidInputException(
		    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
	}

	public OMElement prepareAddReservationOccurrenceResponseOMElement(
	    String serviceId, String nsPrefix) {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}
		// Prepare response
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(namespace, nsPrefix);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_ADD_RESERVATION_OCCURRENCE_RESPONSE, ns);
		OMElement resOccurrenceId = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ID, ns, response);
		resOccurrenceId.setText(serviceId);
		
		return response;
	}
	
	public OMElement prepareExtendCurrentServiceForScheduleResponse(String nsPrefix, String resultStr, int minutesExtended){
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}

		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		OMNamespace omNamespace = omFactory.createOMNamespace(namespace, nsPrefix);
		OMNamespace schemInstanceNamespace = omFactory.createOMNamespace(XML_SCHEMA_INSTANCE_NS,
		        XML_SCHEMA_INSTANCE_NS_PREFIX);

		OMElement response = omFactory.createOMElement(
		        RequestResponseConstants.RAW_EXTEND_SERVICE_FOR_SCHEDULE_RESPONSE, omNamespace);

		// add common namespace decleration
		if (namespace.equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {			
			response.declareNamespace(schemInstanceNamespace);// XSI ns only needed for V2
		}
		OMElement resultStringEl = omFactory.createOMElement(RequestResponseConstants.RAW_RESULT_STRING, omNamespace, response);
		OMElement minutesExtendedEl = omFactory.createOMElement(RequestResponseConstants.RAW_NR_MINUTES_EXTENDED, omNamespace, response);
		
		resultStringEl.setText(resultStr);
		minutesExtendedEl.setText(""+minutesExtended);
		return response;	
	}
	
	public OMElement prepareCreateReservationScheduleResponseOMElement(
	    TaskType task, List<StatusType> statusInfoList, String nsPrefix,
	    String userTimeZoneIdPreference) {

		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}

		// Prepare response
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(namespace, nsPrefix);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_RESPONSE, ns);
		// ScheduleId
		OMElement resScheduleId = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ID, ns, response);
		resScheduleId.setText(task.getTaskId());

		// Schedule Creation Result
		OMElement scheduleCreationResult = fac.createOMElement(
		    RequestResponseConstants.RAW_RESULT, ns, response);
		Result reservationScheduleResult = task.getResult();

		

		switch (reservationScheduleResult) {
		case SUCCESS:
			scheduleCreationResult
			    .setText(ReservationScheduleCreationResult.Succeeded.name());
			break;
		case PARTIAL_SUCCESS:
			scheduleCreationResult
			    .setText(ReservationScheduleCreationResult.SucceededPartially.name());
			break;

		case FAILED:
			scheduleCreationResult.setText(ReservationScheduleCreationResult.Failed
			    .name());
			break;

		default:
			log.error("Unknown schedule creation result = "
			    + reservationScheduleResult);
			scheduleCreationResult.setText(ReservationScheduleCreationResult.Unknown
			    .name());
		}

		for (StatusType statusType : statusInfoList) {
			OMElement occurrenceInfo = fac.createOMElement(
			    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_STATUS_ROOT, ns,
			    response);
			// occurrenceResult
			OMElement occurrenceResult = fac.createOMElement(
			    RequestResponseConstants.RAW_RESULT, ns, occurrenceInfo);
			OMElement occurrenceCreationReason = fac.createOMElement(
			    RequestResponseConstants.RAW_REASON, ns, occurrenceInfo);
			StatusInfoType reservationOccurrenceResult = statusType.getType();
			if (statusType.getProperties(StatusType.MESSAGE) != null) {
				occurrenceCreationReason.setText(statusType
				    .getProperties(StatusType.MESSAGE));
			}
			else {
				occurrenceCreationReason.setText(CREATED_SUCCESSFULLY);
			}

			

			switch (reservationOccurrenceResult) {
			case INFO:
			case WARNING:
				occurrenceResult.setText(ReservationOccurrenceCreationResult.Succeeded
				    .name());
				break;

			case ERROR:
				occurrenceResult.setText(ReservationOccurrenceCreationResult.Failed
				    .name());
				break;

			default:
				log.error("Unknown reservation occurrence creation result = "
				    + reservationOccurrenceResult);
				occurrenceResult.setText(ReservationOccurrenceCreationResult.Unknown
				    .name());
				break;
			}

			if (statusType.getSubTaskId() != null) {
				OMElement occurrenceId = fac.createOMElement(
				    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ID, ns,
				    occurrenceInfo);
				occurrenceId.setText(statusType.getSubTaskId());
			}

			// startTime
			OMElement startTime = fac.createOMElement(
			    RequestResponseConstants.RAW_START_TIME, ns, occurrenceInfo);
			long time = new Long(statusType.getProperties(StatusType.START_TIME))
			    .longValue();
			startTime.setText(serviceUtil.convertMillisToISO8601FormattedString(time,
			    userTimeZoneIdPreference));

			// endTime
			OMElement endTime = fac.createOMElement(
			    RequestResponseConstants.RAW_END_TIME, ns, occurrenceInfo);
			time = new Long(statusType.getProperties(StatusType.END_TIME))
			    .longValue();

			endTime.setText(serviceUtil.convertMillisToISO8601FormattedString(time,
			    userTimeZoneIdPreference));
		}
		return response;
	}

	public OMElement prepareQueryReservationOccurrenceResponseOMElement(
	    DracService occurrence, String nsPrefix, String userTimeZoneIdPreference) {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(namespace, nsPrefix);
		OMNamespace xsiNs = fac.createOMNamespace(XML_SCHEMA_INSTANCE_NS,
		    XML_SCHEMA_INSTANCE_NS_PREFIX);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_QUERY_RESERVATION_OCCURRENCE_RESPONSE, ns);
		OMElement isFound = fac.createOMElement(
		    RequestResponseConstants.RAW_IS_MATCHING_RECORD_FOUND, ns, response);
		response.declareNamespace(xsiNs);

		if (occurrence == null) {
			isFound.setText("false");
			return response;
		}

		isFound.setText("true");
		OMElement resOccurrence = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE, ns, response);
		OMElement resScheduleId = fac
		    .createOMElement(RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ID,
		        ns, resOccurrence);
		resScheduleId.setText(occurrence.getScheduleId());
		// schedule start time
		OMElement scheduleStartTime = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_SCHEDULE_STARTTIME, ns,
		    resOccurrence);
		scheduleStartTime.setText(serviceUtil
		    .convertMillisToISO8601FormattedString(
		        occurrence.getScheduleStartTime(), userTimeZoneIdPreference));
		// schedule endTime
		OMElement scheduleEndTime = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_SCHEDULE_ENDTIME, ns,
		    resOccurrence);
		scheduleEndTime.setText(serviceUtil.convertMillisToISO8601FormattedString(
		    occurrence.getScheduleEndTime(), userTimeZoneIdPreference));
		// schedule status
		OMElement scheduleStatus = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_SCHEDULE_STATUS, ns,
		    resOccurrence);
		scheduleStatus.setText(getScheduleState(occurrence.getScheduleStatus()));

		OMElement reservationScheduleName = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_SCHEDULE_NAME, ns,
		    resOccurrence);
		reservationScheduleName.setText(occurrence.getScheduleName());
		// occcurenceId
		OMElement occurrenceId = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ID, ns,
		    resOccurrence);

		occurrenceId.setText(occurrence.getId());
		// occurrenceStartTime
		OMElement occurrenceStartTime = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_STARTTIME, ns,
		    resOccurrence);
		occurrenceStartTime.setText(serviceUtil
		    .convertMillisToISO8601FormattedString(occurrence.getStartTime(),
		        userTimeZoneIdPreference));
		// occurrenceEndTime
		OMElement occurrenceEndTime = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ENDTIME, ns,
		    resOccurrence);
		occurrenceEndTime.setText(serviceUtil
		    .convertMillisToISO8601FormattedString(occurrence.getEndTime(),
		        userTimeZoneIdPreference));
		// occurrenceStatus
		OMElement occurrenceStatus = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_STATUS, ns,
		    resOccurrence);
		occurrenceStatus.setText(getServiceState(occurrence.getStatus()));

		// resource
		OMElement resource = null;
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			// V2 uses RESOURCE type="path"
			resource = fac.createOMElement(RequestResponseConstants.RAW_RESOURCE, ns,
			    resOccurrence);
			// add type attribute to resource
			if (nsPrefix.equals("")) {
				resource.addAttribute("type",
				    RequestResponseConstants.RAW_PATH_RESOURCE_TYPE, xsiNs);
			}
			else {
				resource.addAttribute("type", nsPrefix + ":"
				    + RequestResponseConstants.RAW_PATH_RESOURCE_TYPE, xsiNs);
			}
		}
		else // V3 uses PATH
		{
			resource = fac.createOMElement(RequestResponseConstants.RAW_PATH, ns,
			    resOccurrence);
		}

		OMElement sourceTna = fac.createOMElement(
		    RequestResponseConstants.RAW_SOURCE_TNA, ns, resource);
		sourceTna.setText(occurrence.getSrcTNA());

		OMElement targetTna = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_TNA, ns, resource);
		targetTna.setText(occurrence.getDestTNA());

		PathType path = occurrence.getPath();

		preparePathOMElement(path, fac, ns, resource);

		// protectionType
		OMElement protectionType = null;
		if (path.getProtectionType().equals(PROTECTION_TYPE.PATH1PLUS1)) {
			protectionType = fac.createOMElement(
			    RequestResponseConstants.RAW_PROTECTION_TYPE, ns, resource);
			protectionType.setText(ONE_PLUS_ONE_PATH_PROTECTION_TYPE);
		}
		else if (path.getProtectionType().equals(PROTECTION_TYPE.UNPROTECTED)) {
			protectionType = fac.createOMElement(
			    RequestResponseConstants.RAW_PROTECTION_TYPE, ns, resource);
			protectionType.setText(UNPROTECTED_PROTECTION_TYPE);
		}
		else {
			log.error("Unknown protection type = "
			    + occurrence.getPath().getProtectionType());
			protectionType = fac.createOMElement(
			    RequestResponseConstants.RAW_PROTECTION_TYPE, ns, resource);
			protectionType.setText(UNKNOWN_PROTECTION_TYPE);
		}

		UserType userType = occurrence.getUserInfo();
		prepareUserInfoOMElement(userType, fac, xsiNs, resOccurrence);

		return response;
	}
			
	public OMElement prepareQueryActiveDracServiceResponseOMElement(DracService service, String nsPrefix,
	        String userTimeZoneIdPreference) {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}

		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		OMNamespace omNamespace = omFactory.createOMNamespace(namespace, nsPrefix);
		OMNamespace schemInstanceNamespace = omFactory.createOMNamespace(XML_SCHEMA_INSTANCE_NS,
		        XML_SCHEMA_INSTANCE_NS_PREFIX);

		OMElement response = omFactory.createOMElement(
		        RequestResponseConstants.RAW_QUERY_ACTIVE_SERVICE_BY_SCHEDULE_ID_RESPONSE, omNamespace);

		// add common namespace decleration
		if (namespace.equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			// XSI ns only needed for V2
			response.declareNamespace(schemInstanceNamespace);
		}
		OMElement isFound = omFactory.createOMElement(RequestResponseConstants.RAW_IS_MATCHING_RECORD_FOUND,
		        omNamespace, response);
		if (service == null) {
			isFound.setText("false");
			return response;
		}else{
			isFound.setText("true");
		}
		prepareSimpleServiceElement(service, omFactory, omNamespace, response);
		return response;
	}

	public OMElement prepareQueryDracServicesByScheduleIdResponseOMElement(DracService[] services, String nsPrefix,
	        String userTimeZoneIdPreference) {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}

		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		OMNamespace omNamespace = omFactory.createOMNamespace(namespace, nsPrefix);
		OMNamespace schemInstanceNamespace = omFactory.createOMNamespace(XML_SCHEMA_INSTANCE_NS,
		        XML_SCHEMA_INSTANCE_NS_PREFIX);

		OMElement response = omFactory.createOMElement(
		        RequestResponseConstants.RAW_QUERY_ACTIVE_SERVICE_BY_SCHEDULE_ID_RESPONSE, omNamespace);

		// add common namespace decleration
		if (namespace.equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			// XSI ns only needed for V2
			response.declareNamespace(schemInstanceNamespace);
		}
		OMElement isFound = omFactory.createOMElement(RequestResponseConstants.RAW_IS_MATCHING_RECORD_FOUND,
		        omNamespace, response);
		if (services == null) {
			isFound.setText("false");
			return response;
		}else{
			isFound.setText("true");
		}
		for(DracService service: services){
			prepareSimpleServiceElement(service, omFactory, omNamespace, response);
		}
		return response;
	}
	
	/**
	 * Create element defined by SimpleServiceT
	 * @param service
	 * @param omFactory
	 * @param omNamespace
	 * @param omElement
	 */
	private void prepareSimpleServiceElement(DracService service, OMFactory omFactory,
		    OMNamespace omNamespace, OMElement omElement){
		
		OMElement serviceElement = omFactory.createOMElement(RequestResponseConstants.RAW_DRAC_SERVICE,
		        omNamespace, omElement);

		// id
		OMElement id = omFactory.createOMElement(RequestResponseConstants.RAW_ID, omNamespace, serviceElement);
		id.setText(service.getId());
		// name
		OMElement serviceNameElement = omFactory.createOMElement(RequestResponseConstants.RAW_NAME, omNamespace, serviceElement);
		serviceNameElement.setText(service.getScheduleName());	
		// path RAW_PATH
		PathType pathType = service.getPath();
		preparePathOMElement(pathType, omFactory, omNamespace, serviceElement);
	}
	
	public OMElement prepareQueryReservationScheduleResponseOMElement(
	    Schedule schedule, String nsPrefix, String userTimeZoneIdPreference) {		
		
		// Prepare response
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}

		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(namespace, nsPrefix);
		OMNamespace xsiNs = fac.createOMNamespace(XML_SCHEMA_INSTANCE_NS,
		    XML_SCHEMA_INSTANCE_NS_PREFIX);

		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_QUERY_RESERVATION_SCHEDULE_RESPONSE, ns);

		// add common namespace decleration
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			// XSI ns only needed for V2
			response.declareNamespace(xsiNs);
		}

		OMElement isFound = fac.createOMElement(
		    RequestResponseConstants.RAW_IS_MATCHING_RECORD_FOUND, ns, response);
		if (schedule == null) {
			isFound.setText("false");
			return response;
		}
		
		boolean scheduleCanceble = State.isCancelable(schedule.getStatus());
		boolean scheduleActivated = schedule.isActivated();
		
		isFound.setText("true");
		OMElement resSchedule = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_SCHEDULE, ns, response);

		// id
		OMElement id = fac.createOMElement(RequestResponseConstants.RAW_ID, ns,
		    resSchedule);
		id.setText(schedule.getId());
		
		//cancable
		OMElement cancable = fac.createOMElement(RequestResponseConstants.RAW_CANCABLE, ns,
				resSchedule);
		cancable.setText(Boolean.toString(scheduleCanceble));

		// type
		OMElement type = fac.createOMElement(RequestResponseConstants.RAW_TYPE, ns,
		    resSchedule);
		
		// activated
		OMElement activated = fac.createOMElement(RequestResponseConstants.RAW_ACTIVATED, ns,
        resSchedule);
		activated.setText(Boolean.toString(scheduleActivated));
		
		
		switch (schedule.getActivationType()) {
		case PRERESERVATION_AUTOMATIC:
			type.setText(ValidReservationScheduleTypes.PreReservationScheduleAutomatic
			    .name());
			break;
		case PRERESERVATION_MANUAL:
			type.setText(ValidReservationScheduleTypes.PreReservationScheduleManual
			    .name());
			break;
		case RESERVATION_AUTOMATIC:
			type.setText(ValidReservationScheduleTypes.ReservationScheduleAutomatic
			    .name());
			break;
		case RESERVATION_MANUAL:
			type.setText(ValidReservationScheduleTypes.ReservationScheduleManual
			    .name());
			break;

		default:
			log.error("Unknown reservation schedule activation type = "
			    + schedule.getActivationType());
			type.setText(ValidReservationScheduleTypes.Unknown.name());
			break;
		}

		// status
		OMElement status = fac.createOMElement(RequestResponseConstants.RAW_STATUS,
		    ns, resSchedule);
		status.setText(getScheduleState(schedule.getStatus()));

		// name
		OMElement name = fac.createOMElement(RequestResponseConstants.RAW_NAME, ns,
		    resSchedule);
		name.setText(schedule.getName());

		// startTime
		OMElement startTime = fac.createOMElement(
		    RequestResponseConstants.RAW_START_TIME, ns, resSchedule);
		startTime.setText(serviceUtil.convertMillisToISO8601FormattedString(
		    schedule.getStartTime(), userTimeZoneIdPreference));

		// reservationOccurrenceDuration
		OMElement reservationOccurrenceDuration = fac.createOMElement(
		    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_DURATION, ns,
		    resSchedule);

		reservationOccurrenceDuration.setText(Long.toString(schedule
		    .getDurationLong() / (60 * 1000)));

		// resource
		OMElement resource = null;
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			// V2 uses RESOURCE type="path"
			resource = fac.createOMElement(RequestResponseConstants.RAW_RESOURCE, ns,
			    resSchedule);
			// add type attribute to resource
			if (nsPrefix.equals("")) {
				resource.addAttribute("type",
				    RequestResponseConstants.RAW_PATH_RESOURCE_TYPE, xsiNs);
			}
			else {
				resource.addAttribute("type", nsPrefix + ":"
				    + RequestResponseConstants.RAW_PATH_RESOURCE_TYPE, xsiNs);
			}
		}
		else // V3 uses PATH
		{
			resource = fac.createOMElement(RequestResponseConstants.RAW_PATH, ns,
			    resSchedule);
		}

		OMElement sourceTna = fac.createOMElement(
		    RequestResponseConstants.RAW_SOURCE_TNA, ns, resource);
		sourceTna.setText(schedule.getSrcTNA());

		OMElement targetTna = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_TNA, ns, resource);
		targetTna.setText(schedule.getDestTNA());

		PathType path = schedule.getPath();

		preparePathOMElement(path, fac, ns, resource);

		// protectionType
		OMElement protectionType = fac.createOMElement(
		    RequestResponseConstants.RAW_PROTECTION_TYPE, ns, resource);
		if (schedule.getPath().getProtectionType()
		    .equals(PROTECTION_TYPE.PATH1PLUS1)) {
			protectionType.setText(ONE_PLUS_ONE_PATH_PROTECTION_TYPE);
		}
		else if (schedule.getPath().getProtectionType()
		    .equals(PROTECTION_TYPE.UNPROTECTED)) {
			protectionType.setText(UNPROTECTED_PROTECTION_TYPE);
		}
		else {
			log.error("Unknown protection type = "
			    + schedule.getPath().getProtectionType());
			protectionType.setText(UNKNOWN_PROTECTION_TYPE);
		}

		UserType userType = schedule.getUserInfo();

		prepareUserInfoOMElement(userType, fac, ns, resSchedule);

		// isRecurring
		OMElement isRecurring = fac.createOMElement(
		    RequestResponseConstants.RAW_IS_RECURRING, ns, resSchedule);
		isRecurring.setText(new Boolean(schedule.isRecurring()).toString());

		if (schedule.isRecurring()) {
			if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
				prepareRecurrenceOMElementV2(schedule, fac, nsPrefix, ns, xsiNs,
				    resSchedule);
			}
			else if (namespace
			    .equals(NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
				prepareRecurrenceOMElementV3(schedule, fac, ns, resSchedule);
			}
		}

		// occurrenceId
		DracService[] serviceIdList = schedule.getServiceIdList();
		for (DracService element : serviceIdList) {
			OMElement occurrenceId = fac.createOMElement(
			    RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_ID, ns,
			    resSchedule);
			occurrenceId.setText(element.getId());
		}
		return response;
	}

	private void prepareUserInfoOMElement(UserType userType, OMFactory fac,
	    OMNamespace ns, OMElement element) {
		// userInfo
		OMElement userInfOM = fac.createOMElement(
		    RequestResponseConstants.RAW_USER_INFO, ns, element);
		// billingGroup
		OMElement billingGroup = fac.createOMElement(
		    RequestResponseConstants.RAW_BILLING_GROUP, ns, userInfOM);
		billingGroup.setText(userType.getBillingGroup().toString());

		// sourceEndpointResourceGroup
		OMElement sourceEndpointResourceGroup = fac.createOMElement(
		    RequestResponseConstants.RAW_SOURCE_ENDPOINT_RESOURCE_GROUP, ns,
		    userInfOM);
		sourceEndpointResourceGroup.setText(userType
		    .getSourceEndpointResourceGroup());

		// targetEndpointResourceGroup
		OMElement targetEndpointResourceGroup = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_ENDPOINT_RESOURCE_GROUP, ns,
		    userInfOM);
		targetEndpointResourceGroup.setText(userType
		    .getTargetEndpointResourceGroup());

		// sourceEndpointUserGroup
		String temp = userType.getSourceEndpointUserGroup();
		if (temp != null && !temp.equals("")) {
			OMElement sourceEndpointUserGroup = fac.createOMElement(
			    RequestResponseConstants.RAW_SOURCE_ENDPOINT_USER_GROUP, ns,
			    userInfOM);
			sourceEndpointUserGroup.setText(temp);
		}
		// targetEndpointUserGroup
		temp = userType.getTargetEndpointUserGroup();
		if (temp != null && !temp.equals("")) {
			OMElement targetEndpointUserGroup = fac.createOMElement(
			    RequestResponseConstants.RAW_TARGET_ENDPOINT_USER_GROUP, ns,
			    userInfOM);
			targetEndpointUserGroup.setText(temp);
		}
		// email address
		temp = userType.getEmailAddress();
		if (temp != null && !temp.equals("")) {
			OMElement emailAddress = fac.createOMElement(
			    RequestResponseConstants.RAW_EMAIL_ADDRESS, ns, userInfOM);
			emailAddress.setText(temp);
		}
		// userId
		OMElement userId = fac.createOMElement(RequestResponseConstants.RAW_USERID,
		    ns, element);
		userId.setText(userType.getUserId());
	}

	private void preparePathOMElement(PathType path, OMFactory fac,
	    OMNamespace ns, OMElement resource) {
		OMElement sourceChannel = fac.createOMElement(
		    RequestResponseConstants.RAW_SOURCE_CHANNEL, ns, resource);
		sourceChannel.setText(new Integer(path.getSourceEndPoint()
		    .getChannelNumber()).toString());

		OMElement targetChannel = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_CHANNEL, ns, resource);
		targetChannel.setText(new Integer(path.getTargetEndPoint()
		    .getChannelNumber()).toString());

		// // vlanId
		// OMElement vlanId =
		// fac.createOMElement(RequestResponseConstants.RAW_VLANID, ns, resource);
		// vlanId.setText(path.getVlanId());

		// rate
		OMElement rate = fac.createOMElement(RequestResponseConstants.RAW_RATE, ns,
		    resource);
		rate.setText(new Integer(path.getRate()).toString());

		// srlg exclusions
		if (!path.getSrlg().equals("")) {
			OMElement srlgExclusions = fac.createOMElement(
			    RequestResponseConstants.RAW_SRLG_EXCLUSIONS, ns, resource);
			srlgExclusions.setText(path.getSrlg());
		}

		// srlg inclusions
		if (!path.getSrlgInclusions().equals("")) {
			
			OMElement srlgInclusions = fac.createOMElement(
			    RequestResponseConstants.RAW_SRLG_INCLUSIONS, ns, resource);
			srlgInclusions.setText(path.getSrlgInclusions());
		}

		// cost
		OMElement cost = fac.createOMElement(RequestResponseConstants.RAW_COST, ns,
		    resource);
		cost.setText(new Integer(path.getCost()).toString());

		// metric
		OMElement metric = fac.createOMElement(RequestResponseConstants.RAW_METRIC,
		    ns, resource);
		metric.setText(new Integer(path.getMetric()).toString());

		// hop
		OMElement hop = fac.createOMElement(RequestResponseConstants.RAW_HOP, ns,
		    resource);
		hop.setText(new Integer(path.getHop()).toString());

		// routingMetric
		OMElement routingMetric = fac.createOMElement(
		    RequestResponseConstants.RAW_ROUTING_METRIC, ns, resource);
		routingMetric.setText(new Integer(path.getRoutingMetric()).toString());

		// sharedRiskReservationOccurrence group
		if (path.getSharedRiskServiceGroup() != null) {
			OMElement srrog = fac
			    .createOMElement(
			        RequestResponseConstants.RAW_SHARED_RISK_RESERVATION_OCCURRENCE_GROUP,
			        ns, resource);
			
			srrog.setText(path.getSharedRiskServiceGroup());
		}
	}

	/**
	 * Creates the recurrence element based on V2 schema
	 * 
	 * @param schedule
	 * @param fac
	 * @param nsPrefix
	 * @param ns
	 * @param xsiNs
	 * @param resSchedule
	 */
	private void prepareRecurrenceOMElementV2(Schedule schedule, OMFactory fac,
	    String nsPrefix, OMNamespace ns, OMNamespace xsiNs, OMElement resSchedule) {
		// this is a recurring schedule

		// recurrenceRoot
		OMElement recurrence = fac.createOMElement(
		    RequestResponseConstants.RAW_RECURRENCE, ns, resSchedule);

		// recurrence type
		OMElement recurrenceType = fac.createOMElement(
		    RequestResponseConstants.RAW_TYPE, ns, recurrence);

		

		if (schedule.getRecurrence().getType().equals(RecurrenceFreq.FREQ_DAILY)) {
			// add type attribute to resource
			if (nsPrefix.equals("")) {
				recurrenceType.addAttribute("type",
				    RequestResponseConstants.RAW_DAILY_RECURRENCE_TYPE, xsiNs);
			}
			else {
				recurrenceType.addAttribute("type", nsPrefix + ":"
				    + RequestResponseConstants.RAW_DAILY_RECURRENCE_TYPE, xsiNs);
			}
		}

		if (schedule.getRecurrence().getType().equals(RecurrenceFreq.FREQ_WEEKLY)) {
			if (nsPrefix.equals("")) {
				recurrenceType.addAttribute("type",
				    RequestResponseConstants.RAW_WEEKLY_RECURRENCE_TYPE, xsiNs);
			}
			else {
				recurrenceType.addAttribute("type", nsPrefix + ":"
				    + RequestResponseConstants.RAW_WEEKLY_RECURRENCE_TYPE, xsiNs);
			}
			for (int i = 0; i < schedule.getRecurrence().getWeekDay().length; i++) {
				// weekday
				OMElement weekday = fac
				    .createOMElement(RequestResponseConstants.RAW_RECURRENCE_WEEKDAY,
				        ns, recurrenceType);
				weekday.setText(new Integer(schedule.getRecurrence().getWeekDay()[i])
				    .toString());
			}
		}

		if (schedule.getRecurrence().getType().equals(RecurrenceFreq.FREQ_MONTHLY)) {
			if (nsPrefix.equals("")) {
				recurrenceType.addAttribute("type",
				    RequestResponseConstants.RAW_MONTHLY_RECURRENCE_TYPE, xsiNs);
			}
			else {
				recurrenceType.addAttribute("type", nsPrefix + ":"
				    + RequestResponseConstants.RAW_MONTHLY_RECURRENCE_TYPE, xsiNs);
			}
			// dayOfMonth
			OMElement dayOfMonth = fac.createOMElement(
			    RequestResponseConstants.RAW_DAY_OF_MONTH, ns, recurrenceType);
			dayOfMonth.setText(new Integer(schedule.getRecurrence().getDay())
			    .toString());
		}

		if (schedule.getRecurrence().getType().equals(RecurrenceFreq.FREQ_YEARLY)) {
			if (nsPrefix.equals("")) {
				recurrenceType.addAttribute("type",
				    RequestResponseConstants.RAW_YEARLY_RECURRENCE_TYPE, xsiNs);
			}
			else {
				recurrenceType.addAttribute("type", nsPrefix + ":"
				    + RequestResponseConstants.RAW_YEARLY_RECURRENCE_TYPE, xsiNs);
			}
			// month
			OMElement month = fac.createOMElement(
			    RequestResponseConstants.RAW_RECURRENCE_MONTH, ns, recurrenceType);
			month
			    .setText(new Integer(schedule.getRecurrence().getMonth()).toString());

			// dayOfMonth
			OMElement dayOfMonth = fac.createOMElement(
			    RequestResponseConstants.RAW_DAY_OF_MONTH, ns, recurrenceType);
			dayOfMonth.setText(new Integer(schedule.getRecurrence().getDay())
			    .toString());
		}
		// range
		OMElement recurrenceRange = fac.createOMElement(
		    RequestResponseConstants.RAW_RECURRENCE_RANGE, ns, recurrence);
		if (nsPrefix.equals("")) {
			recurrenceRange.addAttribute("type",
			    RequestResponseConstants.RAW_END_AFTER_DATE_RECURRENCE_RANGE, xsiNs);
		}
		else {
			recurrenceRange
			    .addAttribute("type", nsPrefix + ":"
			        + RequestResponseConstants.RAW_END_AFTER_DATE_RECURRENCE_RANGE,
			        xsiNs);
		}
		OMElement recurrenceEndDate = fac.createOMElement(
		    RequestResponseConstants.RAW_END_DATE, ns, recurrenceRange);

		Calendar endTime = DateTimeConverter.getCalendarInUTC();
		endTime.setTimeInMillis(schedule.getEndTime());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddZ");
		StringBuilder output = new StringBuilder();
		output.append(df.format(endTime.getTimeInMillis()));
		// Locate timezone part of the datetime
		int strlen = output.length();
		if (strlen > 2) {
			output.insert(strlen - 2, ":");
		}
		recurrenceEndDate.setText(output.toString());
	}

	/**
	 * Creates the recurrence element based on V3 schema
	 * 
	 * @param schedule
	 * @param fac
	 * @param ns
	 * @param resSchedule
	 */
	private void prepareRecurrenceOMElementV3(Schedule schedule, OMFactory fac,
	    OMNamespace ns, OMElement resSchedule) {
		// this is a recurring schedule

		// recurrenceRoot
		OMElement recurrence = fac.createOMElement(
		    RequestResponseConstants.RAW_RECURRENCE, ns, resSchedule);

		// recurrence type
		OMElement recurrenceType = fac.createOMElement(
		    RequestResponseConstants.RAW_TYPE, ns, recurrence);

		

		if (schedule.getRecurrence().getType().equals(RecurrenceFreq.FREQ_DAILY)) {
			recurrenceType
			    .setText(RequestResponseConstants.RAW_DAILY_RECURRENCE_TYPE);
		}
		else if (schedule.getRecurrence().getType()
		    .equals(RecurrenceFreq.FREQ_WEEKLY)) {
			recurrenceType
			    .setText(RequestResponseConstants.RAW_WEEKLY_RECURRENCE_TYPE);
			for (int i = 0; i < schedule.getRecurrence().getWeekDay().length; i++) {
				// weekday
				OMElement weekday = fac.createOMElement(
				    RequestResponseConstants.RAW_RECURRENCE_WEEKDAY, ns, recurrence);
				weekday.setText(new Integer(schedule.getRecurrence().getWeekDay()[i])
				    .toString());
			}
		}
		else if (schedule.getRecurrence().getType()
		    .equals(RecurrenceFreq.FREQ_MONTHLY)) {
			recurrenceType
			    .setText(RequestResponseConstants.RAW_MONTHLY_RECURRENCE_TYPE);
			// dayOfMonth
			OMElement dayOfMonth = fac.createOMElement(
			    RequestResponseConstants.RAW_DAY_OF_MONTH, ns, recurrence);
			dayOfMonth.setText(new Integer(schedule.getRecurrence().getDay())
			    .toString());
		}
		else if (schedule.getRecurrence().getType()
		    .equals(RecurrenceFreq.FREQ_YEARLY)) {
			recurrenceType
			    .setText(RequestResponseConstants.RAW_YEARLY_RECURRENCE_TYPE);
			OMElement monthAndDay = fac.createOMElement(
			    RequestResponseConstants.RAW_MONTH_AND_DAY, ns, recurrence);
			// recurrence uses 0 as January, GMonthday uses 1 as January
			monthAndDay.setText("--" + (schedule.getRecurrence().getMonth() + 1)
			    + "-" + schedule.getRecurrence().getDay());
		}
		// range
		OMElement recurrenceRange = fac.createOMElement(
		    RequestResponseConstants.RAW_RECURRENCE_RANGE, ns, recurrence);

		OMElement recurrenceEndDate = fac.createOMElement(
		    RequestResponseConstants.RAW_END_DATE, ns, recurrenceRange);

		Calendar endTime = DateTimeConverter.getCalendarInUTC();
		endTime.setTimeInMillis(schedule.getEndTime());
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddZ");
		StringBuilder output = new StringBuilder();
		output.append(df.format(endTime.getTimeInMillis()));
		// Locate timezone part of the datetime
		int strlen = output.length();
		if (strlen > 2) {
			output.insert(strlen - 2, ":");
		}
		recurrenceEndDate.setText(output.toString());
	}

	private UserType generateUserInfoType(OMElement inputMsg, String nsPrefix,
	    String userId) throws InvalidInputException {
		UserGroupName billingGroup = null;
		String sourceEndpointResourceGroup = null;
		String targetEndpointResourceGroup = null;
		String sourceEndpointUserGroup = null;
		String targetEndpointUserGroup = null;
		String email = null;

		// billingGroup
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.BILLING_GROUP);
		if (selectedNode == null) {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_BILLING_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		if (selectedNode.getText() != null) {
			if (selectedNode.getText().trim().equals("")) {
				Object[] args = new Object[2];
				args[0] = "";
				args[1] = RequestResponseConstants.RAW_BILLING_GROUP;
				throw new InvalidInputException(
				    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
				    args);
			}
			billingGroup = new UserGroupName(selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_BILLING_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// sourceEndpointResourceGroup
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.SOURCE_ENDPOINT_RESOURCE_GROUP);
		if (selectedNode == null) {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_SOURCE_ENDPOINT_RESOURCE_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		if (selectedNode.getText() != null) {
			if (selectedNode.getText().trim().equals("")) {
				Object[] args = new Object[2];
				args[0] = "";
				args[1] = RequestResponseConstants.RAW_SOURCE_ENDPOINT_RESOURCE_GROUP;
				throw new InvalidInputException(
				    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
				    args);
			}
			sourceEndpointResourceGroup = selectedNode.getText().trim();
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_SOURCE_ENDPOINT_RESOURCE_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// targetEndpointResourceGroup
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.TARGET_ENDPOINT_RESOURCE_GROUP);
		if (selectedNode == null) {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_TARGET_ENDPOINT_RESOURCE_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}
		if (selectedNode.getText() != null) {
			if (selectedNode.getText().trim().equals("")) {
				Object[] args = new Object[2];
				args[0] = "";
				args[1] = RequestResponseConstants.RAW_TARGET_ENDPOINT_RESOURCE_GROUP;
				throw new InvalidInputException(
				    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
				    args);
			}
			targetEndpointResourceGroup = selectedNode.getText().trim();
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_TARGET_ENDPOINT_RESOURCE_GROUP;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// sourceEndpointUserGroup
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.SOURCE_ENDPOINT_USER_GROUP)) != null) {
			sourceEndpointUserGroup = selectedNode.getText().trim();
		}

		// targetEndpointUserGroup
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.TARGET_ENDPOINT_USER_GROUP)) != null) {
			targetEndpointUserGroup = selectedNode.getText().trim();
		}

		// e-mailAddress
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.EMAIL_ADDRESS)) != null) {
			email = selectedNode.getText().trim();
		}

		// userId
		return new UserType(userId, billingGroup, sourceEndpointUserGroup,
		    targetEndpointUserGroup, sourceEndpointResourceGroup,
		    targetEndpointResourceGroup, email);
	}

	private String getServiceNameGeneric(OMElement inputMsg, String nsPrefix) throws InvalidInputException{
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateScheduleInfoRequest.getScheduleName());		
		return selectedNode.getText().trim();
	}
	
	private String getScheduleIdGeneric(OMElement inputMsg, String nsPrefix) throws InvalidInputException{
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateScheduleInfoRequest.getScheduleId());
		return selectedNode.getText().trim();
	}
	private String getMinutesToExtendService(OMElement inputMsg, String nsPrefix) throws InvalidInputException{
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
				AlterServiceRequest.getTimeServiceExtension());		
		return selectedNode.getText().trim();
	}	
	private String getScheduleIdToExtendService(OMElement inputMsg, String nsPrefix) throws InvalidInputException{
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
				AlterServiceRequest.getScheduleIdServiceExtension());		
		return selectedNode.getText().trim();
	}	
		
	private String getServiceIdGeneric(OMElement inputMsg, String nsPrefix) throws InvalidInputException{
		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
			    CreateScheduleInfoRequest.getServiceId());		
		return selectedNode.getText().trim();
	}
	
	private PathType genPathTypeGeneric(OMElement inputMsg, String nsPrefix) throws InvalidInputException {
		//CreateScheduleInfoRequest
		// resource
		// source endpoint
		// channelNumber
		// channelNumber is an integer. 0 is not valid, so starts with channel
		// 1.
		// Value -1 means don't care, find an available channnel...
		PathType p = new PathType();

		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getSourceEndpointChannel(namespace));
		if (selectedNode != null) {
			p.getSourceEndPoint().setChannelNumber(
			    Integer.parseInt(selectedNode.getText().trim()));
			log.debug("sourceChannelNumber = "
			    + p.getSourceEndPoint().getChannelNumber());
		}

		// tna
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getSourceEndpointTna(namespace));
		if (selectedNode != null) {
			p.getSourceEndPoint().setName(selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_SOURCE_TNA;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// vlanId - SOURCE and TARGET
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getSourceVlanid(namespace));
		if (selectedNode != null) {
			// set vlanId
			p.setSrcVlanId(selectedNode.getText().trim());
			log.debug("vlanId = " + p.getSrcVlanId());
		}

		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getTargetVlanid(namespace));
		if (selectedNode != null) {
			// set vlanId
			p.setDstVlanId(selectedNode.getText().trim());
			log.debug("vlanId = " + p.getDstVlanId());
		}

		// target endpoint
		// channelNumber
		// channelNumber is an integer. 0 is not valid, so starts with channel
		// 1.
		// Value -1 means don't care, find an available channnel...

		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getTargetEndpointChannel(namespace));
		if (selectedNode != null) {
			p.getTargetEndPoint().setChannelNumber(
			    Integer.parseInt(selectedNode.getText().trim()));
			log.debug("targetChannelNumber = "
			    + p.getTargetEndPoint().getChannelNumber());
		}

		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getTargetEndpointTna(namespace));
		if (selectedNode != null) {
			p.getTargetEndPoint().setName(selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_TARGET_TNA;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// srlg exclusions
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getSrlgExclusions(namespace));
		if (selectedNode != null) {
			p.setSrlg(selectedNode.getText().trim());
		}

		// cost
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getCost(namespace));
		if (selectedNode != null) {
			p.setCost(Integer.parseInt(selectedNode.getText().trim()));
		}

		// metric
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getMetric(namespace));
		if (selectedNode != null) {
			p.setMetric(Integer.parseInt(selectedNode.getText().trim()));
		}

		// hop
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getHop(namespace));
		if (selectedNode != null) {
			p.setHop(Integer.parseInt(selectedNode.getText().trim()));
		}

		// routingMetric
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getRoutingMetric(namespace))) != null) {
			p.setRoutingMetric(Integer.parseInt(selectedNode.getText().trim()));
		}

		// routingAlgorithm - ccat/vcat
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getRoutingAlgorithm(namespace))) != null) {
			String routingAlgorithm = selectedNode.getText().trim();
			if ("vcat".equalsIgnoreCase(routingAlgorithm)) {
				p.setVcatRoutingOption(true);
			}
			else if ("ccat".equalsIgnoreCase(routingAlgorithm)) {
				p.setVcatRoutingOption(false);
			}
		}

		// protectionType
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getProtectionType(namespace))) != null) {
			String protectionType = selectedNode.getText().trim();
			if (protectionType.equals(UNPROTECTED_PROTECTION_TYPE)) {
				p.setProtectionType(PROTECTION_TYPE.UNPROTECTED);
			}
			else if (protectionType.equals(ONE_PLUS_ONE_PATH_PROTECTION_TYPE)) {
				p.setProtectionType(PROTECTION_TYPE.PATH1PLUS1);
			}
			else {
				Object[] args = new Object[2];
				args[0] = protectionType;
				args[1] = RequestResponseConstants.RAW_PROTECTION_TYPE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
				    args);
			}
		}

		// Shared risk reservation occurrence group
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest
		        .getSharedRiskReservationOccurrenceGroup(namespace))) != null) {
			p.setSharedRiskServiceGroup(selectedNode.getText().trim());
		}

		// rate
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateScheduleInfoRequest.getRate(namespace));
		if (selectedNode != null) {
			p.setRate(Integer.parseInt(selectedNode.getText().trim()));
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_RATE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		return p;

	}
	
	private PathType genPathType(OMElement inputMsg, String nsPrefix)
	    throws InvalidInputException {
		// resource
		// source endpoint
		// channelNumber
		// channelNumber is an integer. 0 is not valid, so starts with channel
		// 1.
		// Value -1 means don't care, find an available channnel...
		PathType p = new PathType();

		OMElement selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getSourceEndpointChannel(namespace));
		if (selectedNode != null) {
			p.getSourceEndPoint().setChannelNumber(
			    Integer.parseInt(selectedNode.getText().trim()));
			log.debug("sourceChannelNumber = "
			    + p.getSourceEndPoint().getChannelNumber());
		}

		// tna
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getSourceEndpointTna(namespace));
		
    if (selectedNode == null) {
      throw new InvalidInputException(
          DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE,
          new Object[] { RequestResponseConstants.RAW_SOURCE_TNA });
    }
    else {
      p.getSourceEndPoint().setName(selectedNode.getText().trim());
    }

		// vlanId - SOURCE and TARGET
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getSourceVlanid(namespace));
		if (selectedNode != null) {
			// set vlanId
			p.setSrcVlanId(selectedNode.getText().trim());
			log.debug("vlanId = " + p.getSrcVlanId());
		}

		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getTargetVlanid(namespace));
		if (selectedNode != null) {
			// set vlanId
			p.setDstVlanId(selectedNode.getText().trim());
			log.debug("vlanId = " + p.getDstVlanId());
		}

		// target endpoint
		// channelNumber
		// channelNumber is an integer. 0 is not valid, so starts with channel
		// 1.
		// Value -1 means don't care, find an available channnel...

		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getTargetEndpointChannel(namespace));
		if (selectedNode != null) {
			p.getTargetEndPoint().setChannelNumber(
			    Integer.parseInt(selectedNode.getText().trim()));
			log.debug("targetChannelNumber = "
			    + p.getTargetEndPoint().getChannelNumber());
		}

		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getTargetEndpointTna(namespace));
		if (selectedNode != null) {
			p.getTargetEndPoint().setName(selectedNode.getText().trim());
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_TARGET_TNA;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		// srlg exclusions
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getSrlgExclusions(namespace));
		if (selectedNode != null) {
			p.setSrlg(selectedNode.getText().trim());
		}

		// cost
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getCost(namespace));
		if (selectedNode != null) {
			p.setCost(Integer.parseInt(selectedNode.getText().trim()));
		}

		// metric
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getMetric(namespace));
		if (selectedNode != null) {
			p.setMetric(Integer.parseInt(selectedNode.getText().trim()));
		}

		// hop
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getHop(namespace));
		if (selectedNode != null) {
			p.setHop(Integer.parseInt(selectedNode.getText().trim()));
		}

		// routingMetric
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getRoutingMetric(namespace))) != null) {
			p.setRoutingMetric(Integer.parseInt(selectedNode.getText().trim()));
		}

		// routingAlgorithm - ccat/vcat
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getRoutingAlgorithm(namespace))) != null) {
			String routingAlgorithm = selectedNode.getText().trim();
			if ("vcat".equalsIgnoreCase(routingAlgorithm)) {
				p.setVcatRoutingOption(true);
			}
			else if ("ccat".equalsIgnoreCase(routingAlgorithm)) {
				p.setVcatRoutingOption(false);
			}
		}

		// protectionType
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getProtectionType(namespace))) != null) {
			String protectionType = selectedNode.getText().trim();
			if (protectionType.equals(UNPROTECTED_PROTECTION_TYPE)) {
				p.setProtectionType(PROTECTION_TYPE.UNPROTECTED);
			}
			else if (protectionType.equals(ONE_PLUS_ONE_PATH_PROTECTION_TYPE)) {
				p.setProtectionType(PROTECTION_TYPE.PATH1PLUS1);
			}
			else {
				Object[] args = new Object[2];
				args[0] = protectionType;
				args[1] = RequestResponseConstants.RAW_PROTECTION_TYPE;
				throw new InvalidInputException(
				    DracErrorConstants.WS_UNSUPPORTED_PARAM_VALUE_IN_REQUEST_MESSAGE,
				    args);
			}
		}

		// Shared risk reservation occurrence group
		if ((selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest
		        .getSharedRiskReservationOccurrenceGroup(namespace))) != null) {
			p.setSharedRiskServiceGroup(selectedNode.getText().trim());
		}

		// rate
		selectedNode = serviceUtil.getNode(inputMsg, namespace, nsPrefix,
		    CreateReservationScheduleRequest.getRate(namespace));
		if (selectedNode != null) {
			p.setRate(Integer.parseInt(selectedNode.getText().trim()));
		}
		else {
			Object[] args = new Object[1];
			args[0] = RequestResponseConstants.RAW_RATE;
			throw new InvalidInputException(
			    DracErrorConstants.WS_MISSING_PARAMETER_IN_REQUEST_MESSAGE, args);
		}

		return p;
	}

	private String getScheduleState(SCHEDULE scheduleState) {

		switch (scheduleState) {
		case CONFIRMATION_PENDING:
			return ValidReservationScheduleStatusTypes.ConfirmationPending.name();
		case CONFIRMATION_TIMED_OUT:
			return ValidReservationScheduleStatusTypes.ConfirmationTimedOut.name();
		case CONFIRMATION_CANCELLED:
			return ValidReservationScheduleStatusTypes.ConfirmationCancelled.name();
		case EXECUTION_PENDING:
			return ValidReservationScheduleStatusTypes.ExecutionPending.name();
		case EXECUTION_INPROGRESS:
			return ValidReservationScheduleStatusTypes.ExecutionInProgress.name();
		case EXECUTION_SUCCEEDED:
			return ValidReservationScheduleStatusTypes.ExecutionSucceeded.name();
		case EXECUTION_PARTIALLY_SUCCEEDED:
			return ValidReservationScheduleStatusTypes.ExecutionPartiallySucceeded
			    .name();
		case EXECUTION_TIME_OUT:
			return ValidReservationScheduleStatusTypes.ExecutionTimedOut.name();
		case EXECUTION_FAILED:
			return ValidReservationScheduleStatusTypes.ExecutionFailed.name();
		case EXECUTION_PARTIALLY_CANCELLED:
			return ValidReservationScheduleStatusTypes.ExecutionPartiallyCancelled
			    .name();
		case EXECUTION_CANCELLED:
			return ValidReservationScheduleStatusTypes.ExecutionCancelled.name();
		default:
			log.error("Unknown schedule state = " + scheduleState);
			return ValidReservationScheduleStatusTypes.Unknown.name();
		}
	}

	private String getServiceState(SERVICE serviceState) {

		switch (serviceState) {
		case CONFIRMATION_PENDING:
			return ValidReservationOcurrenceStatusTypes.ConfirmationPending.name();
		case CONFIRMATION_TIMED_OUT:
			return ValidReservationOcurrenceStatusTypes.ConfirmationTimedOut.name();
		case CONFIRMATION_CANCELLED:
			return ValidReservationOcurrenceStatusTypes.ConfirmationCancelled.name();

		case ACTIVATION_PENDING:
			return ValidReservationOcurrenceStatusTypes.ActivationPending.name();
		case ACTIVATION_TIMED_OUT:
			return ValidReservationOcurrenceStatusTypes.ActivationTimedOut.name();
		case ACTIVATION_CANCELLED:
			return ValidReservationOcurrenceStatusTypes.ActivationCancelled.name();
		case EXECUTION_PENDING:
			return ValidReservationOcurrenceStatusTypes.ExecutionPending.name();
		case EXECUTION_INPROGRESS:
			return ValidReservationOcurrenceStatusTypes.ExecutionInProgress.name();
		case EXECUTION_TIMED_OUT:
			return ValidReservationOcurrenceStatusTypes.ExecutionTimedOut.name();
		case EXECUTION_SUCCEEDED:
			return ValidReservationOcurrenceStatusTypes.ExecutionSucceeded.name();
		case EXECUTION_FAILED:
			return ValidReservationOcurrenceStatusTypes.ExecutionFailed.name();
		case EXECUTION_PARTIALLY_CANCELLED:
			return ValidReservationOcurrenceStatusTypes.ExecutionPartiallyCancelled
			    .name();
		case EXECUTION_CANCELLED:
			return ValidReservationOcurrenceStatusTypes.ExecutionCancelled.name();
		default:
			log.error("Unknown service state = " + serviceState);
			return ValidReservationOcurrenceStatusTypes.Unknown.name();
		}
	}

}
