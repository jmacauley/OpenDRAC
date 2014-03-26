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

package org.opendrac.drac.server.ws.common;

public final class NamespaceConstantsV3 {

	public static final String WSS_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public static final String RES_ALLOC_AND_SCHEDULING_SERVICE_V3_0_NS = "http://www.opendrac.org/ws/ResourceAllocationAndSchedulingServiceTypes_v3.0";
	public static final String SYSTEM_MONITORING_SERVICE_V3_0_NS = "http://www.opendrac.org/ws/SystemMonitoringServiceTypes_v3.0";
	public static final String NETWORK_MONITORING_SERVICE_V3_0_NS = "http://www.opendrac.org/ws/NetworkMonitoringServiceTypes_v3.0";

	public static final String DEFAULT_SECURITY_NSPREFIX = "wsse";
	public static final String SOAP_HEADER_USERNAME_PATH = "/soapenv:Header/wsse:Security/wsse:UsernameToken/wsse:Username";

	public static final String SOAP_ENVELOPE_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String SOAP_ENVELOPE_NS_PREFIX = "soapenv";
	public static final String XML_SCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String XML_SCHEMA_INSTANCE_NS_PREFIX = "xsi";

	public static final String COMMON_DATA_TYPES_NS = "http://www.nortel.com/drac/2007/07/03/ws/ct/DracCommonTypes";

	public static final String RES_ALLOC_AND_SCHEDULING_SERVICE = "ResourceAllocationAndSchedulingService";
	public static final String RES_ALLOC_AND_SCHEDULING_SERVICE_VERSION = "3.0";
	public static final String RES_ALLOC_AND_SCHEDULING_SERVICE_EPR = RES_ALLOC_AND_SCHEDULING_SERVICE
	    + "_v" + RES_ALLOC_AND_SCHEDULING_SERVICE_VERSION;
	public static final String SYSTEM_MONITORING_SERVICE = "SystemMonitoringService";
	public static final String SYSTEM_MONITORING_SERVICE_VERSION = "3.0";
	public static final String SYSTEM_MONITORING_SERVICE_EPR = SYSTEM_MONITORING_SERVICE
	    + "_v" + SYSTEM_MONITORING_SERVICE_VERSION;
	public static final String NETWORK_MONITORING_SERVICE = "NetworkMonitoringService";
	public static final String NETWORK_MONITORING_SERVICE_VERSION = "3.0";
	public static final String NETWORK_MONITORING_SERVICE_EPR = NETWORK_MONITORING_SERVICE
	    + "_v" + NETWORK_MONITORING_SERVICE_VERSION;

	private NamespaceConstantsV3() {
	}
}
