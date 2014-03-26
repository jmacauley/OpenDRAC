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

package com.nortel.appcore.app.drac.server.ws.systemmonitoringservice.request;

import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;

public final class QueryLogsRequest {
	public static final String QUERY_LOG_REQUEST = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST;

	public static final String START_TIME = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_START_TIME;

	public static final String END_TIME = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_END_TIME;

	public static final String QUERY_CRITERIA = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA;

	public static final String ORIGINATOR_BILLING_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_BILLING_GROUP;

	public static final String ORIGINATOR_IP_ADDRESS = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_IP_ADDRESS;

	public static final String ORIGINATOR_USER_ID = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_USERID;

	public static final String AFFECTED_RESOURCE_ID = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_AFFECTED_RESOURCE_ID;

	public static final String SEVERITY = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_SEVERITY;

	public static final String RESULT = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_RESULT;

	public static final String OPERATION_TYPE = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_OPERATION_TYPE;

	public static final String CATEGORY = "srv:"
	    + RequestResponseConstants.RAW_QUERY_LOGS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_CATEGORY;

	private QueryLogsRequest() {
	}

}
