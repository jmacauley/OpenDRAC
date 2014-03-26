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

package com.nortel.appcore.app.drac.server.ws.resallocandschedulingservice.request;

import com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;

public final class CreateReservationScheduleRequest{
	public static final String NAME = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_NAME;

	public static final String TYPE = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_TYPE;

	public static final String STARTTIME = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_START_TIME;

	public static final String ENDTIME = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_END_TIME;

	public static final String DURATION = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESERVATION_OCCURRENCE_DURATION;

	/***************************************************************************************************************************
	 * WSDL V2.0 Resource (i.e. Path) XPaths
	 ***************************************************************************************************************************/

	public static final String RESOURCE = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE;

	private static final String SOURCE_ENDPOINT_TNA = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_TNA;

	private static final String SOURCE_ENDPOINT_CHANNEL = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_CHANNEL;

	private static final String SOURCE_VLANID = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_VLANID;

	private static final String TARGET_VLANID = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_VLANID;

	private static final String TARGET_ENDPOINT_TNA = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_TNA;

	private static final String TARGET_ENDPOINT_CHANNEL = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_CHANNEL;

	private static final String RATE = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_RATE;

	private static final String SRLG_EXCLUSIONS = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SRLG_EXCLUSIONS;

	/*
	 * private static final String SRLG_INCLUSIONS = "srv:" +
	 * RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST + "/srv:"
	 * + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:" +
	 * RequestResponseConstants.RAW_RESOURCE + "/srv:" +
	 * RequestResponseConstants.RAW_SRLG_INCLUSIONS;
	 */
	private static final String COST = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_COST;
	private static final String METRIC = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_METRIC;

	private static final String HOP = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_HOP;

	private static final String ROUTING_METRIC = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_ROUTING_METRIC;

	private static final String ROUTING_ALGORITHM = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_ROUTING_ALGORITHM;

	private static final String PROTECTION_TYPE = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_PROTECTION_TYPE;

	private static final String SHARED_RISK_RESERVATION_OCCURRENCE_GROUP = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SHARED_RISK_RESERVATION_OCCURRENCE_GROUP;

	/***************************************************************************************************************************
	 * WSDL V3.0 PATH XPaths
	 ***************************************************************************************************************************/

	public static final String PATH_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH;

	private static final String SOURCE_ENDPOINT_TNA_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_TNA;

	private static final String SOURCE_ENDPOINT_CHANNEL_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_CHANNEL;

	private static final String SOURCE_VLANID_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_VLANID;

	private static final String TARGET_VLANID_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_VLANID;

	private static final String TARGET_ENDPOINT_TNA_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_TNA;

	private static final String TARGET_ENDPOINT_CHANNEL_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_CHANNEL;

	private static final String RATE_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_RATE;

	private static final String SRLG_EXCLUSIONS_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_SRLG_EXCLUSIONS;

	/*
	 * private static final String SRLG_INCLUSIONS = "srv:" +
	 * RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST + "/srv:"
	 * + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:" +
	 * RequestResponseConstants.RAW_PATH + "/srv:" +
	 * RequestResponseConstants.RAW_SRLG_INCLUSIONS;
	 */
	private static final String COST_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_COST;
	private static final String METRIC_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_METRIC;

	private static final String HOP_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_HOP;

	private static final String ROUTING_METRIC_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_ROUTING_METRIC;

	private static final String ROUTING_ALGORITHM_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_ROUTING_ALGORITHM;

	private static final String PROTECTION_TYPE_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_PROTECTION_TYPE;

	private static final String SHARED_RISK_RESERVATION_OCCURRENCE_GROUP_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:"
	    + RequestResponseConstants.RAW_RESERVATION_SCHEDULE
	    + "/srv:"
	    + RequestResponseConstants.RAW_PATH
	    + "/srv:"
	    + RequestResponseConstants.RAW_SHARED_RISK_RESERVATION_OCCURRENCE_GROUP;

	public static final String IS_RECURRING = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_IS_RECURRING;

	/***************************************************************************************************************************
	 * WSDL V2.0 Recurrence XPaths
	 ***************************************************************************************************************************/

	public static final String RECURRENCE_TYPE = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_TYPE;

	private static final String RECURRENCE_WEEKDAY = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_TYPE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE_WEEKDAY;

	private static final String RECURRENCE_DAY_OF_MONTH = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_TYPE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE_DAY;

	private static final String RECURRENCE_DAY_AND_MONTH = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_TYPE + "/srv:"
	    + RequestResponseConstants.RAW_DAY_OF_MONTH;

	public static final String RECURRENCE_RANGE = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE_RANGE;

	public static final String RECURRENCE_RANGE_END_DATE = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE_RANGE + "/srv:"
	    + RequestResponseConstants.RAW_END_DATE;

	public static final String RECURRENCE_RANGE_END_AFTER_NUM_OF_OCCURRENCES = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:"
	    + RequestResponseConstants.RAW_RESERVATION_SCHEDULE
	    + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE
	    + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE_RANGE
	    + "/srv:"
	    + RequestResponseConstants.RAW_END_AFTER_NUM_OF_RECURRENCES;

	/***************************************************************************************************************************
	 * WSDL V3.0 Recurrence XPaths
	 ***************************************************************************************************************************/

	public static final String RECURRENCE_TYPE_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_TYPE;

	private static final String RECURRENCE_WEEKDAY_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_WEEKLY_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE_WEEKDAY;

	private static final String RECURRENCE_DAY_OF_MONTH_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_MONTHLY_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE_DAY;

	private static final String RECURRENCE_DAY_AND_MONTH_V3 = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_YEARLY_RECURRENCE + "/srv:"
	    + RequestResponseConstants.RAW_MONTH_AND_DAY;

	public static final String BILLING_GROUP = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_BILLING_GROUP;

	public static final String SOURCE_ENDPOINT_RESOURCE_GROUP = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_ENDPOINT_RESOURCE_GROUP;

	public static final String TARGET_ENDPOINT_RESOURCE_GROUP = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_ENDPOINT_RESOURCE_GROUP;

	public static final String SOURCE_ENDPOINT_USER_GROUP = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_ENDPOINT_USER_GROUP;

	public static final String TARGET_ENDPOINT_USER_GROUP = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_ENDPOINT_USER_GROUP;

	public static final String EMAIL_ADDRESS = "srv:"
	    + RequestResponseConstants.RAW_CREATE_RESERVATION_SCHEDULE_REQUEST
	    + "/srv:" + RequestResponseConstants.RAW_RESERVATION_SCHEDULE + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_EMAIL_ADDRESS;

	public static String getSourceEndpointTna(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return SOURCE_ENDPOINT_TNA;
		}
		else {
			return SOURCE_ENDPOINT_TNA_V3;
		}
	}

	public static String getSourceEndpointChannel(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return SOURCE_ENDPOINT_CHANNEL;
		}
		else {
			return SOURCE_ENDPOINT_CHANNEL_V3;
		}
	}

	public static String getSourceVlanid(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return SOURCE_VLANID;
		}
		else {
			return SOURCE_VLANID_V3;
		}
	}

	public static String getTargetVlanid(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return TARGET_VLANID;
		}
		else {
			return TARGET_VLANID_V3;
		}
	}

	public static String getTargetEndpointTna(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return TARGET_ENDPOINT_TNA;
		}
		else {
			return TARGET_ENDPOINT_TNA_V3;
		}
	}

	public static String getTargetEndpointChannel(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return TARGET_ENDPOINT_CHANNEL;
		}
		else {
			return TARGET_ENDPOINT_CHANNEL_V3;
		}
	}

	public static String getRate(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return RATE;
		}
		else {
			return RATE_V3;
		}
	}

	public static String getSrlgExclusions(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return SRLG_EXCLUSIONS;
		}
		else {
			return SRLG_EXCLUSIONS_V3;
		}
	}

	public static String getCost(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return COST;
		}
		else {
			return COST_V3;
		}
	}

	public static String getMetric(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return METRIC;
		}
		else {
			return METRIC_V3;
		}
	}

	public static String getHop(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return HOP;
		}
		else {
			return HOP_V3;
		}
	}

	public static String getRoutingAlgorithm(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return ROUTING_ALGORITHM;
		}
		else {
			return ROUTING_ALGORITHM_V3;
		}
	}

	public static String getRoutingMetric(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return ROUTING_METRIC;
		}
		else {
			return ROUTING_METRIC_V3;
		}
	}

	public static String getProtectionType(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return PROTECTION_TYPE;
		}
		else {
			return PROTECTION_TYPE_V3;
		}
	}

	public static String getSharedRiskReservationOccurrenceGroup(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return SHARED_RISK_RESERVATION_OCCURRENCE_GROUP;
		}
		else {
			return SHARED_RISK_RESERVATION_OCCURRENCE_GROUP_V3;
		}
	}

	public static String getRecurrenceWeekday(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return RECURRENCE_WEEKDAY;
		}
		else {
			return RECURRENCE_WEEKDAY_V3;
		}
	}

	public static String getRecurrenceDayOfMonth(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return RECURRENCE_DAY_OF_MONTH;
		}
		else {
			return RECURRENCE_DAY_OF_MONTH_V3;
		}
	}

	public static String getRecurrenceDayAndMonth(String namespace) {
		if (namespace
		    .equals(NamespaceConstants.RES_ALLOC_AND_SCHEDULING_SERVICE_NS)) {
			return RECURRENCE_DAY_AND_MONTH;
		}
		else {
			return RECURRENCE_DAY_AND_MONTH_V3;
		}
	}

	private CreateReservationScheduleRequest() {
	}

}
