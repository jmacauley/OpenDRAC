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

public final class NamespaceConstants {

	public static final String XML_SCHEMA_INSTANCE_NS_PREFIX = "xsi";

	// Namespace prefixes

	public static final String SOAP_ENVELOPE_NS_PREFIX = "soapenv";
	public static final String SOAP_ADDRESSING_NS_PREFIX = "wsa";
	public static final String SOAP_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
	// Commonly used XPaths
	public static final String SOAP_ENVELOPE_USER_ID = "/soapenv:Header/common:Credentials/common:userId";

	public static final String SOAP_ENVELOPE_CERTIFICATE = "/soapenv:Header/common:Credentials/common:certificate";
	public static final String SOAP_ENVELOPE_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String XML_SCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";
	// Services
	public static final String DEFAULT_NS_PREFIX = "tns";

	public static final String DEFAULT_COMMON_NS_PREFIX = "ct";
	public static final String RES_ALLOC_AND_SCHEDULING_SERVICE_NS = "http://www.nortel.com/drac/2007/07/03/ws/ResourceAllocationAndSchedulingServiceTypes";
	public static final String SYSTEM_MONITORING_SERVICE_NS = "http://www.nortel.com/drac/2007/07/03/ws/SystemMonitoringServiceTypes";
	public static final String NETWORK_MONITORING_SERVICE_NS = "http://www.nortel.com/drac/2007/07/03/ws/NetworkMonitoringServiceTypes";
	public static final String COMMON_DATA_TYPES_NS = "http://www.nortel.com/drac/2007/07/03/ws/ct/DracCommonTypes";
	public static final String RES_ALLOC_AND_SCHEDULING_SERVICE = "ResourceAllocationAndSchedulingService";
	public static final String RES_ALLOC_AND_SCHEDULING_SERVICE_VERSION = "2.0.0";
	public static final String SYSTEM_MONITORING_SERVICE = "SystemMonitoringService";
	public static final String SYSTEM_MONITORING_SERVICE_VERSION = "2.0.0";
	public static final String NETWORK_MONITORING_SERVICE = "NetworkMonitoringService";
	public static final String NETWORK_MONITORING_SERVICE_VERSION = "2.0.0";

	private NamespaceConstants() {
		super();
	}
}
