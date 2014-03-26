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

public final class RequestResponseConstants {
	public static final String RAW_DONT_CARE_NODE = "*";
	public static final String RAW_QUERY_ALARMS_REQUEST = "QueryAlarmsRequest";
	public static final String RAW_START_TIME = "startTime";
	public static final String RAW_END_TIME = "endTime";
	public static final String RAW_QUERY_CRITERIA = "criteria";
	public static final String RAW_QUERY_ALARMS_BY_DATE_TIME_AND_USER_GROUP = "QueryAlarmsByDateTimeAndUserGroupT";
	public static final String RAW_QUERY_RESERVATION_SCHEDULES_BY_DATE_TIME_AND_USER_GROUP = "QueryReservationSchedulesByDateTimeAndUserGroupT";
	public static final String RAW_QUERY_LOGS_REQUEST = "QueryLogsRequest";
	public static final String RAW_QUERY_LOGS_BY_DATE_TIME = "QueryLogsByDateTimeT";
	public static final String RAW_CANCEL_RESERVATION_OCCURRENCE_REQUEST = "CancelReservationOccurrenceRequest";
	public static final String RAW_RESERVATION_OCCURRENCE_ID = "occurrenceId";
	public static final String RAW_CANCEL_RESERVATION_SCHEDULE_REQUEST = "CancelReservationScheduleRequest";
	public static final String RAW_ADD_RESERVATION_OCCURRENCE_REQUEST = "AddReservationOccurrenceRequest";
	public static final String RAW_RESERVATION_SCHEDULE_ID = "reservationScheduleId";
	public static final String RAW_OCCURRENCE = "occurrence";
	public static final String RAW_RESOURCE = "resource";
	public static final String RAW_USER_INFO = "userInfo";
	public static final String RAW_RATE = "rate";
	public static final String RAW_SRLG_BUCKET_NUMBER = "srlg";
	public static final String RAW_SRLG_EXCLUSIONS = "srlgExclusions";
	public static final String RAW_SRLG_INCLUSIONS = "srlgInclusions";
	public static final String RAW_COST = "cost";
	public static final String RAW_METRIC = "metric";
	public static final String RAW_HOP = "hop";
	public static final String RAW_ROUTING_METRIC = "routingMetric";
	public static final String RAW_ROUTING_ALGORITHM = "routingAlgorithm";
	public static final String RAW_CREATE_RESERVATION_SCHEDULE_REQUEST = "CreateReservationScheduleRequest";
	public static final String RAW_RESERVATION_SCHEDULE = "reservationSchedule";
	public static final String RAW_DRAC_SERVICE = "service";
	public static final String RAW_NAME = "name";
	public static final String RAW_SCHEDULE_ID = "serviceId";
	public static final String RAW_SERVICE_ID = "scheduleId";
	public static final String RAW_SERVICE_TIME_EXTENSION = "minutesToExtend";
	
	public static final String RAW_RESERVATION_OCCURRENCE_DURATION = "reservationOccurrenceDuration";
	public static final String RAW_QUERY_RESERVATION_OCCURRENCE_REQUEST = "QueryReservationOccurrenceRequest";
	public static final String RAW_QUERY_RESERVATION_OCCURRENCES_REQUEST = "QueryReservationOccurrencesRequest";
	public static final String RAW_RECURRENCE = "recurrence";
	public static final String RAW_TYPE = "type";
	public static final String RAW_OPERATION_TYPE = "operationType";
	public static final String RAW_AFFECTED_RESOURCE_ID = "affectedResourceId";
	public static final String RAW_RECURRENCE_DAY = "day";
	public static final String RAW_RECURRENCE_MONTH = "month";
	public static final String RAW_RECURRENCE_WEEKDAY = "weekday";
	public static final String RAW_WEEKLY_RECURRENCE = "weeklyRecurrence";
	public static final String RAW_MONTHLY_RECURRENCE = "monthlyRecurrence";
	public static final String RAW_YEARLY_RECURRENCE = "yearlyRecurrence";
	public static final String RAW_QUERY_RESERVATION_SCHEDULE_REQUEST = "QueryReservationScheduleRequest";
	public static final String RAW_QUERY_RESERVATION_SCHEDULES_REQUEST = "QueryReservationSchedulesRequest";
	public static final String RAW_QUERY_ALARMS_RESPONSE = "QueryAlarmsResponse";
	public static final String RAW_ALARM = "alarm";
	public static final String RAW_ALARM_ID = "alarmId";
	public static final String RAW_ALARM_SOURCE_ID = "alarmSourceId";
	public static final String RAW_SEVERITY = "severity";
	public static final String RAW_IP_ADDRESS = "ipAddress";
	public static final String RAW_OCCURRENCE_DATETIME = "occurrenceTime";
	public static final String RAW_DATETIME = "dateTime";
	public static final String RAW_ALARM_RAISE_DATETIME = "timeRaised";
	public static final String RAW_ALARM_CLEARED_DATETIME = "timeCleared";
	public static final String IS_ALARM_CLEARED = "isCleared";
	public static final String RAW_QUERY_LOGS_RESPONSE = "QueryLogsResponse";
	public static final String RAW_ADD_RESERVATION_OCCURRENCE_RESPONSE = "AddReservationOccurrenceResponse";
	public static final String RAW_LOG = "log";
	public static final String RAW_CATEGORY = "category";
	public static final String RAW_BILLING_GROUP = "billingGroup";
	public static final String RAW_USER_GROUP = "userGroup";
	public static final String RAW_ADDITIONAL_INFO = "additionalInfo";
	public static final String RAW_CREATE_RESERVATION_SCHEDULE_RESPONSE = "CreateReservationScheduleResponse";
	public static final String RAW_RESERVATION_OCCURRENCE_STATUS_ROOT = "occurrenceInfo";
	public static final String RAW_QUERY_RESERVATION_OCCURRENCE_RESPONSE = "QueryReservationOccurrenceResponse";
	public static final String RAW_RESERVATION_OCCURRENCE = "occurrence";
	public static final String RAW_RESERVATION_SCHEDULE_STARTTIME = "reservationScheduleStartTime";
	public static final String RAW_RESERVATION_SCHEDULE_ENDTIME = "reservationScheduleEndTime";
	public static final String RAW_RESERVATION_SCHEDULE_STATUS = "reservationScheduleStatus";
	public static final String RAW_RESERVATION_SCHEDULE_NAME = "reservationScheduleName";
	public static final String RAW_RESERVATION_OCCURRENCE_STARTTIME = "occurrenceStartTime";
	public static final String RAW_RESERVATION_OCCURRENCE_ENDTIME = "occurrenceEndTime";
	public static final String RAW_RESERVATION_OCCURRENCE_STATUS = "occurrenceStatus";
	public static final String RAW_QUERY_RESERVATION_OCCURRENCES_RESPONSE = "QueryReservationOccurrencesResponse";
	public static final String RAW_QUERY_RESERVATION_SCHEDULE_RESPONSE = "QueryReservationScheduleResponse";
	
	public static final String RAW_QUERY_SERVICES_BY_SCHEDULE_ID_RESPONSE = "QueryServicesByScheduleIdResponse";	
	public static final String RAW_QUERY_ACTIVE_SERVICE_BY_SCHEDULE_ID_RESPONSE = "QueryActiveServiceByScheduleIdResponse";
	public static final String RAW_RESUME_SERVICE_ID_RESPONSE = "ResumeServiceByIdResponse";
	public static final String RAW_TERMINATE_SERVICE_BY_ID_RESPONSE = "TerminateServiceByIdResponse";
	
	
	public static final String RAW_IS_MATCHING_RECORD_FOUND = "isFound";
	public static final String RAW_RESULT = "result";
	public static final String RAW_REASON = "reason";
	public static final String RAW_ID = "id";
	public static final String RAW_USERID = "userId";
	public static final String RAW_LAYER = "layer";
	public static final String RAW_CONSTRAIN = "constrain";
	public static final String RAW_CHANNEL = "channel";
	public static final String RAW_SOURCE_CHANNEL = "sourceChannel";
	public static final String RAW_TARGET_CHANNEL = "targetChannel";
	public static final String RAW_SIGNALLING_TYPE = "signalingType";
	public static final String RAW_MTU = "mtu";
	public static final String RAW_SPEED = "speed";
	public static final String RAW_PHYSICAL_ADDR = "physicalAddress";
	public static final String RAW_AUTO_NEGOTIATION = "autoNegotiation";
	public static final String RAW_AUTO_NEGOTIATION_STATUS = "autoNegotiationStatus";
	public static final String RAW_ADVERTISED_DUPLEX = "advertisedDuplex";
	public static final String RAW_FLOW_CONTROL = "flowControl";
	public static final String RAW_TX_CONDITIONING = "txConditioning";
	public static final String RAW_STS = "sts";
	public static final String RAW_VCAT = "vcat";
	public static final String RAW_CONTROL_PAUSE_TX = "controlPauseTx";
	public static final String RAW_CONTROL_PAUSE_RX = "controlPauseRx";
	public static final String RAW_NEGOTIATED_DUPLEX = "negotiatedDuplex";
	public static final String RAW_NEGOTIATED_SPEED = "negotiatedSpeed";
	public static final String RAW_NEGOTIATED_PAUSE_TX = "negotiatedPauseTx";
	public static final String RAW_NEGOTIATED_PAUSE_RX = "negotiatedPauseRx";
	public static final String RAW_LINK_PARTNER_DUPLEX = "linkPartnerDuplex";
	public static final String RAW_LINK_PARTNER_SPEED = "linkPartnerSpeed";
	public static final String RAW_LINK_PARTNER_FLOW_CONTROL = "linkPartnerFlowControl";
	public static final String RAW_PASS_CONTROL_FRAME = "passControlFrame";
	public static final String RAW_INTERFACE_TYPE = "interfaceType";
	public static final String RAW_POLICING = "policing";
	public static final String RAW_ENCAPSULATION_TYPE = "encapsulationType";
	public static final String RAW_PRIORITY_MODE = "priorityMode";
	public static final String RAW_BANDWIDTH_THRESHOLD = "bandwidthThreshold";
	public static final String RAW_REMAINED_BANDWIDTH = "remainedBandwidth";
	public static final String RAW_BANDWIDTH_UTILIZATION = "bandwidthUtilization";
	public static final String RAW_LAG_ID = "lagId";
	public static final String RAW_QUERY_RESERVATION_SCHEDULES_RESPONSE = "QueryReservationSchedulesResponse";
	public static final String RAW_QUERY_SERVERS_RESPONSE = "QueryServersResponse";
	public static final String RAW_NUM_OF_ELEMENTS_INCLUDED = "numOfElementsIncluded";
	public static final String RAW_TOTAL_NUM_OF_MATCHING_ELEMENTS = "numOfElementsFound";
	public static final String RAW_STATUS = "status";
	public static final String RAW_DESCRIPTION = "description";
	public static final String RAW_DATE_TIME = "dateTime";
	public static final String RAW_WEB_SERVICE = "webService";
	public static final String RAW_TARGET_EPR = "targetEPR";
	public static final String RAW_SERVICE_VERSION = "version";
	public static final String RAW_QUERY_ENDPOINT_RESPONSE = "QueryEndpointResponse";
	public static final String RAW_QUERY_ENDPOINT_REQUEST = "QueryEndpointRequest";
	public static final String RAW_ENDPOINT = "endpoint";
	public static final String RAW_TNA = "tna";
	public static final String RAW_SOURCE_TNA = "sourceTna";
	public static final String RAW_TARGET_TNA = "targetTna";
	public static final String RAW_PHYSICAL_ADDRESS = "physicalAddress";
	public static final String RAW_CONTROL_APUSE_RX = "controlPauseRx";
	public static final String RAW_QUERY_RESERVATION_OCCURRENCES_BY_DATETIME_AND_USERGROUP = "QueryReservationOccurrencesByDateTimeAndUserGroupT";
	public static final String RAW_QUERY_ENDPOINTS_RESPONSE = "QueryEndpointsResponse";
	public static final String RAW_QUERY_ENDPOINTS_REQUEST = "QueryEndpointsRequest";
	public static final String RAW_IS_RECURRING = "isRecurring";
	public static final String RAW_YEARLY_RECURRENCE_TYPE = "YearlyRecurrenceT";
	public static final String RAW_MONTHLY_RECURRENCE_TYPE = "MonthlyRecurrenceT";
	public static final String RAW_WEEKLY_RECURRENCE_TYPE = "WeeklyRecurrenceT";
	public static final String RAW_DAILY_RECURRENCE_TYPE = "DailyRecurrenceT";
	public static final String RAW_DAY_OF_MONTH = "dayOfMonth";
	public static final String RAW_MONTH_AND_DAY = "monthAndDay";
	public static final String RAW_RECURRENCE_RANGE = "range";
	public static final String RAW_END_AFTER_DATE_RECURRENCE_RANGE = "EndsAfterDateT";
	public static final String RAW_END_DATE = "endDate";
	public static final String RAW_END_AFTER_NUM_OF_RECURRENCES = "numOfOccurrences";
	public static final String RAW_PATH_RESOURCE_TYPE = "PathT";
	public static final String RAW_PATH = "path";
	public static final String RAW_PATH_REQUEST_RESOURCE_TYPE = "PathRequestT";
	public static final String RAW_QUERY_RESERVATION_SCHEDULES_BY_DATETIME_AND_USER_GROUP = "QueryReservationSchedulesByDateTimeAndUserGroupT";
	public static final String RAW_QUERY_ENDPOINTS_BY_LAYER_AND_USER_GROUP = "QueryEndpointsByLayerAndUserGroupT";
	public static final String RAW_QUERY_ENDPOINTS_BY_WAVELENGTH = "QueryEndpointsByWavelengthT";
	public static final String RAW_QUERY_ENDPOINTS_BY_SITEID = "QueryEndpointsBySiteIdT";
	public static final String RAW_PROTECTION_TYPE = "protectionType";
	public static final String RAW_SHARED_RISK_RESERVATION_OCCURRENCE_GROUP = "sharedRiskReservationOccurrenceGroup";
	public static final String RAW_SOURCE_ENDPOINT_RESOURCE_GROUP = "sourceEndpointResourceGroup";
	public static final String RAW_TARGET_ENDPOINT_RESOURCE_GROUP = "targetEndpointResourceGroup";
	public static final String RAW_SOURCE_ENDPOINT_USER_GROUP = "sourceEndpointUserGroup";
	public static final String RAW_TARGET_ENDPOINT_USER_GROUP = "targetEndpointUserGroup";
	public static final String RAW_EMAIL_ADDRESS = "emailAddress";
	public static final String RAW_QUERY_PATH_AVAILABILITY_REQUEST = "QueryPathAvailabilityRequest";
	public static final String RAW_PATH_AVAILABILITY = "pathAvailability";
	public static final String RAW_COMPLETION_RESPONSE = "CompletionResponse";
	public static final String RAW_CONFIRM_RESERVATION_SCHEDULE_REQUEST = "ConfirmReservationScheduleRequest";
	public static final String RAW_ACTIVATE_RESERVATION_OCCURRENCE_REQUEST = "ActivateReservationOccurrenceRequest";
	public static final String RAW_CREDENTIALS = "Credentials";
	public static final String RAW_CERTIFICATE = "certificate";
	public static final String RAW_SOURCE_VLANID = "source_vlanId";
	public static final String RAW_TARGET_VLANID = "target_vlanId";
	public static final String RAW_SITEID = "siteId";
	public static final String RAW_WAVELENGTH = "wavelength";
	public static final String RAW_SERVER_CONFIG = "serverConfig";
	public static final String RAW_LOCAL_SERVER = "localServer";
	public static final String RAW_REMOTE_SERVER = "remoteServer";
	public static final String RAW_STATE = "state";
	public static final String RAW_MODE = "mode";
	public static final String RAW_REDUNDANCY_CONFIG_TYPE = "configurationType";
	public static final String RAW_SW_VERSION = "softwareVersion";
	public static final String RAW_ACTIVE_SERVER_DATETIME = "activeServerDateTime";
	public static final String RAW_DOMAIN = "domain";
	public static final String RAW_CANCABLE = "isCanceble";
	public static final String RAW_ACTIVATED = "activated";
	
	public static final String RAW_EXTEND_SERVICE_FOR_SCHEDULE_RESPONSE = "ExtendCurrentServiceForScheduleResponse";
	public static final String RAW_RESULT_STRING = "resultString";
	public static final String RAW_NR_MINUTES_EXTENDED = "minutesExtended";
	public static final String RAW_SCHEDULE_ID_STRING = "scheduleId";
	
	public static final String FACLABEL_ATTR = "userLabel";
	
	private RequestResponseConstants() {
		super();
	}
}
