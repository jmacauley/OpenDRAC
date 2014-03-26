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

import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS;

import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;

public final class QueryPathAvailabilityRequest {
	public static final String STARTTIME = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_START_TIME;

	public static final String ENDTIME = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_END_TIME;

	public static final String RESOURCE = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE;

	private static final String PATH = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH;

	private static final String SOURCE_ENDPOINT_TNA = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_TNA;

	private static final String SOURCE_ENDPOINT_CHANNEL = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_CHANNEL;

	private static final String TARGET_ENDPOINT_TNA = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_TNA;

	// public static final String VLANID = "srv:" +
	// RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	// + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:" +
	// RequestResponseConstants.RAW_RESOURCE +
	// "/srv:"
	// + RequestResponseConstants.RAW_VLANID;

	private static final String TARGET_ENDPOINT_CHANNEL = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_CHANNEL;

	private static final String RATE = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_RATE;

	private static final String SRLG_EXCLUSIONS = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SRLG_EXCLUSIONS;

	private static final String COST = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_COST;

	private static final String METRIC = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_METRIC;

	private static final String HOP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_HOP;

	private static final String ROUTING_METRIC = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_ROUTING_METRIC;

	private static final String PROTECTION_TYPE = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_PROTECTION_TYPE;

	private static final String SHARED_RISK_RESERVATION_OCCURRENCE_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_RESOURCE + "/srv:"
	    + RequestResponseConstants.RAW_SHARED_RISK_RESERVATION_OCCURRENCE_GROUP;

	private static final String PATH_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH;

	private static final String SOURCE_ENDPOINT_TNA_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_TNA;

	private static final String SOURCE_ENDPOINT_CHANNEL_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_CHANNEL;

	private static final String TARGET_ENDPOINT_TNA_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_TNA;

	// public static final String VLANID = "srv:" +
	// RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	// + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:" +
	// RequestResponseConstants.RAW_RESOURCE +
	// "/srv:"
	// + RequestResponseConstants.RAW_VLANID;

	private static final String TARGET_ENDPOINT_CHANNEL_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_CHANNEL;

	private static final String RATE_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_RATE;

	private static final String SRLG_EXCLUSIONS_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_SRLG_EXCLUSIONS;

	private static final String COST_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_COST;

	private static final String METRIC_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_METRIC;

	private static final String HOP_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_HOP;

	private static final String ROUTING_METRIC_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_ROUTING_METRIC;

	private static final String PROTECTION_TYPE_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_PATH + "/srv:"
	    + RequestResponseConstants.RAW_PROTECTION_TYPE;

	private static final String SHARED_RISK_RESERVATION_OCCURRENCE_GROUP_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST
	    + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY
	    + "/srv:"
	    + RequestResponseConstants.RAW_PATH
	    + "/srv:"
	    + RequestResponseConstants.RAW_SHARED_RISK_RESERVATION_OCCURRENCE_GROUP;

	public static final String BILLING_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_BILLING_GROUP;

	public static final String SOURCE_ENDPOINT_RESOURCE_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_ENDPOINT_RESOURCE_GROUP;

	public static final String TARGET_ENDPOINT_RESOURCE_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_ENDPOINT_RESOURCE_GROUP;

	public static final String SOURCE_ENDPOINT_USER_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_SOURCE_ENDPOINT_USER_GROUP;

	public static final String TARGET_ENDPOINT_USER_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_TARGET_ENDPOINT_USER_GROUP;

	public static final String EMAIL_ADDRESS = "srv:"
	    + RequestResponseConstants.RAW_QUERY_PATH_AVAILABILITY_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_PATH_AVAILABILITY + "/srv:"
	    + RequestResponseConstants.RAW_USER_INFO + "/srv:"
	    + RequestResponseConstants.RAW_EMAIL_ADDRESS;

	private QueryPathAvailabilityRequest() {
	}

	public static String getPath(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return PATH_V3;
		}
		else {
			return PATH;
		}
	}

	public static String getSourceEndpointTna(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return SOURCE_ENDPOINT_TNA_V3;
		}
		else {
			return SOURCE_ENDPOINT_TNA;
		}
	}

	public static String getSourceEndpointChannel(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return SOURCE_ENDPOINT_CHANNEL_V3;
		}
		else {
			return SOURCE_ENDPOINT_CHANNEL;
		}
	}

	public static String getTargetEndpointTna(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return TARGET_ENDPOINT_TNA_V3;
		}
		else {
			return TARGET_ENDPOINT_TNA;
		}
	}

	public static String getTargetEndpointChannel(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return TARGET_ENDPOINT_CHANNEL_V3;
		}
		else {
			return TARGET_ENDPOINT_CHANNEL;
		}
	}

	public static String getRate(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return RATE_V3;
		}
		else {
			return RATE;
		}
	}

	public static String getSrlgExclusions(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return SRLG_EXCLUSIONS_V3;
		}
		else {
			return SRLG_EXCLUSIONS;
		}
	}

	public static String getCost(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return COST_V3;
		}
		else {
			return COST;
		}
	}

	public static String getMetric(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return METRIC_V3;
		}
		else {
			return METRIC;
		}
	}

	public static String getHop(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return HOP_V3;
		}
		else {
			return HOP;
		}
	}

	public static String getRoutingMetric(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return ROUTING_METRIC_V3;
		}
		else {
			return ROUTING_METRIC;
		}
	}

	public static String getProtectionType(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return PROTECTION_TYPE_V3;
		}
		else {
			return PROTECTION_TYPE;
		}
	}

	public static String getSharedRiskReservationOccurrenceGroup(String namespace) {
		if (namespace.equals(RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS)) {
			return SHARED_RISK_RESERVATION_OCCURRENCE_GROUP_V3;
		}
		else {
			return SHARED_RISK_RESERVATION_OCCURRENCE_GROUP;
		}
	}
}
