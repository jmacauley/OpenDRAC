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

package com.nortel.appcore.app.drac.server.ws.networkmonitoringservice.request;

import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.NETWORK_MONITORING_SERVICE_V3_0_NS;

import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;

public final class QueryEndpointsRequest {
	public static final String QUERY_ENDPOINTS_REQUEST = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST;

	public static final String CRITERIA = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA;

	private static final String USER_GROUP = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_USER_GROUP;

	private static final String TYPE = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:" + "type";

	private static final String LAYER = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_LAYER;

	private static final String SITEID = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_SITEID;

	private static final String WAVELENGTH = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_QUERY_CRITERIA + "/srv:"
	    + RequestResponseConstants.RAW_WAVELENGTH;

	private static final String USER_GROUP_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_USER_GROUP;

	private static final String TYPE_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:" + "type";

	private static final String LAYER_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_LAYER;

	private static final String SITEID_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_SITEID;

	private static final String WAVELENGTH_V3 = "srv:"
	    + RequestResponseConstants.RAW_QUERY_ENDPOINTS_REQUEST + "/srv:"
	    + RequestResponseConstants.RAW_WAVELENGTH;

	public static String getUserGroupXPath(String namespace) {
		if (namespace.equals(NETWORK_MONITORING_SERVICE_V3_0_NS)) {
			return USER_GROUP_V3;
		}
		else {
			return USER_GROUP;
		}
	}

	public static String getTypeXPath(String namespace) {
		if (namespace.equals(NETWORK_MONITORING_SERVICE_V3_0_NS)) {
			return TYPE_V3;
		}
		else {
			return TYPE;
		}
	}

	public static String getLayerXPath(String namespace) {
		if (namespace.equals(NETWORK_MONITORING_SERVICE_V3_0_NS)) {
			return LAYER_V3;
		}
		else {
			return LAYER;
		}
	}

	public static String getSiteIDXPath(String namespace) {
		if (namespace.equals(NETWORK_MONITORING_SERVICE_V3_0_NS)) {
			return SITEID_V3;
		}
		else {
			return SITEID;
		}
	}

	public static String getWavelengthXPath(String namespace) {
		if (namespace.equals(NETWORK_MONITORING_SERVICE_V3_0_NS)) {
			return WAVELENGTH_V3;
		}
		else {
			return WAVELENGTH;
		}
	}

	private QueryEndpointsRequest() {
		super();
	}
}
