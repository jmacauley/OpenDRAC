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

package com.nortel.appcore.app.drac.server.ws.systemmonitoringservice.common;

import static com.nortel.appcore.app.drac.server.ws.common.NamespaceConstants.SYSTEM_MONITORING_SERVICE_NS;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.NETWORK_MONITORING_SERVICE;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.NETWORK_MONITORING_SERVICE_EPR;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.NETWORK_MONITORING_SERVICE_VERSION;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE_EPR;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.RES_ALLOC_AND_SCHEDULING_SERVICE_VERSION;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.SYSTEM_MONITORING_SERVICE;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.SYSTEM_MONITORING_SERVICE_EPR;
import static org.opendrac.drac.server.ws.common.NamespaceConstantsV3.SYSTEM_MONITORING_SERVICE_VERSION;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.info.ServerInfo;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandler;
import com.nortel.appcore.app.drac.server.ws.common.CommonMessageConstants;
import com.nortel.appcore.app.drac.server.ws.common.OperationFailedException;
import com.nortel.appcore.app.drac.server.ws.common.RequestResponseConstants;
import com.nortel.appcore.app.drac.server.ws.common.ServiceUtilities;

public class SystemMonitoringHelper {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final ServiceUtilities serviceUtil = new ServiceUtilities();
	protected String namespace = SYSTEM_MONITORING_SERVICE_NS;

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public OMElement prepareQueryServersResponseOMElement(RequestHandler rh,
	    String nsPrefix, String userTimeZoneIdPreference) throws Exception {
		if (nsPrefix.equals("xmlns")) {
			nsPrefix = "";
		}
		// Prepare response
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace ns = fac.createOMNamespace(namespace, nsPrefix);
		OMElement response = fac.createOMElement(
		    RequestResponseConstants.RAW_QUERY_SERVERS_RESPONSE, ns);

		OMElement serverConfig = fac.createOMElement(
		    RequestResponseConstants.RAW_SERVER_CONFIG, ns, response);

		createServerTime(userTimeZoneIdPreference, fac, ns, serverConfig);

		ServerInfo[] info = rh.getRedundancyServerInfo();
		ServerInfo localServerInfo = info[0];
		ServerInfo remoteServerInfo = info[1];

		createLocalRemoteInfo(fac, ns, serverConfig, localServerInfo,
		    remoteServerInfo);

		String hostName = new String(InetAddress.getLocalHost().getHostName());
		createV2WebServices(fac, ns, serverConfig, hostName);

		createV3WebServices(fac, ns, serverConfig, hostName);

		return response;
	}

	protected void createServerTime(String userTimeZoneIdPreference,
	    OMFactory fac, OMNamespace ns, OMElement serverConfig) {
		// active server datetime
		// TODO: We need a way to retrieve local datetime from "DracServer"
		OMElement dateTime = fac.createOMElement(
		    RequestResponseConstants.RAW_ACTIVE_SERVER_DATETIME, ns, serverConfig);
		dateTime.setText(serviceUtil.convertMillisToISO8601FormattedString(
		    System.currentTimeMillis(), userTimeZoneIdPreference));
	}

	protected void createLocalRemoteInfo(OMFactory fac, OMNamespace ns,
	    OMElement serverConfig, ServerInfo localServerInfo,
	    ServerInfo remoteServerInfo) throws OperationFailedException {
		if (localServerInfo != null) {
			// LOCAL SERVER
			OMElement localServer = fac.createOMElement(
			    RequestResponseConstants.RAW_LOCAL_SERVER, ns, serverConfig);

			// localServer->state
			OMElement localServerState = fac.createOMElement(
			    RequestResponseConstants.RAW_STATE, ns, localServer);
			localServerState.setText(localServerInfo.getState().name());

			// localServer->mode
			OMElement localServerMode = fac.createOMElement(
			    RequestResponseConstants.RAW_MODE, ns, localServer);
			localServerMode.setText(localServerInfo.getMode().name());

			// localServer->ipAddress
			OMElement localServerIPAddress = fac.createOMElement(
			    RequestResponseConstants.RAW_IP_ADDRESS, ns, localServer);
			localServerIPAddress.setText(localServerInfo.getIpAddress());

			// localServer->configType
			OMElement localServerConfigType = fac.createOMElement(
			    RequestResponseConstants.RAW_REDUNDANCY_CONFIG_TYPE, ns, localServer);
			localServerConfigType.setText(localServerInfo.getServerConfig().name());

			// localServer->swVersion
			OMElement localServerSwVersion = fac.createOMElement(
			    RequestResponseConstants.RAW_SW_VERSION, ns, localServer);
			localServerSwVersion.setText(localServerInfo.getSoftwareVersion());

			if (remoteServerInfo != null) {
				// REMOTE SERVER
				OMElement remoteServer = fac.createOMElement(
				    RequestResponseConstants.RAW_REMOTE_SERVER, ns, serverConfig);

				// remoteServer->state
				OMElement remoteServerState = fac.createOMElement(
				    RequestResponseConstants.RAW_STATE, ns, remoteServer);
				remoteServerState.setText(remoteServerInfo.getState().name());

				// remoteServer->mode
				OMElement remoteServerMode = fac.createOMElement(
				    RequestResponseConstants.RAW_MODE, ns, remoteServer);
				remoteServerMode.setText(remoteServerInfo.getMode().name());

				// remoteServer->ipAddress
				OMElement remoteServerIPAddress = fac.createOMElement(
				    RequestResponseConstants.RAW_IP_ADDRESS, ns, remoteServer);
				remoteServerIPAddress.setText(remoteServerInfo.getIpAddress());

				// remoteServer->configType
				OMElement remoteServerConfigType = fac.createOMElement(
				    RequestResponseConstants.RAW_REDUNDANCY_CONFIG_TYPE, ns,
				    remoteServer);
				remoteServerConfigType.setText(remoteServerInfo.getServerConfig()
				    .name());

				// remoteServer->swVersion
				OMElement remoteServerSwVersion = fac.createOMElement(
				    RequestResponseConstants.RAW_SW_VERSION, ns, remoteServer);
				remoteServerSwVersion.setText(remoteServerInfo.getSoftwareVersion());
			}
			else {
				
			}
		}
		else {
			
			Object[] args = new Object[1];
			args[0] = CommonMessageConstants.SYSTEM_CONFIG_RETRIEVAL_FAILURE;
			throw new OperationFailedException(
			    DracErrorConstants.WS_OPERATION_FAILED, args);
		}
	}

	protected void createV2WebServices(OMFactory fac, OMNamespace ns,
	    OMElement serverConfig, String hostName) throws UnknownHostException {
		// WEB Service
		OMElement webService1 = fac.createOMElement(
		    RequestResponseConstants.RAW_WEB_SERVICE, ns, serverConfig);
		// ResourceAllocationAndSchedulingService
		OMElement serviceName1 = fac.createOMElement(
		    RequestResponseConstants.RAW_NAME, ns, webService1);
		serviceName1.setText(RES_ALLOC_AND_SCHEDULING_SERVICE);
		OMElement epr1 = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_EPR, ns, webService1);

		epr1.setText("https://" + hostName + ":8443/axis2/services/"
		    + RES_ALLOC_AND_SCHEDULING_SERVICE);

		OMElement serviceVer1 = fac.createOMElement(
		    RequestResponseConstants.RAW_SERVICE_VERSION, ns, webService1);
		serviceVer1.setText(RES_ALLOC_AND_SCHEDULING_SERVICE_VERSION);

		// SystemMonitoringService
		OMElement webService2 = fac.createOMElement(
		    RequestResponseConstants.RAW_WEB_SERVICE, ns, serverConfig);
		OMElement serviceName2 = fac.createOMElement(
		    RequestResponseConstants.RAW_NAME, ns, webService2);
		serviceName2.setText(SYSTEM_MONITORING_SERVICE);
		OMElement epr2 = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_EPR, ns, webService2);

		epr2.setText("https://" + hostName + ":8443/axis2/services/"
		    + SYSTEM_MONITORING_SERVICE);

		OMElement serviceVer2 = fac.createOMElement(
		    RequestResponseConstants.RAW_SERVICE_VERSION, ns, webService2);

		serviceVer2.setText(SYSTEM_MONITORING_SERVICE_VERSION);

		// NetworkMonitoringService
		OMElement webService3 = fac.createOMElement(
		    RequestResponseConstants.RAW_WEB_SERVICE, ns, serverConfig);
		OMElement serviceName3 = fac.createOMElement(
		    RequestResponseConstants.RAW_NAME, ns, webService3);
		serviceName3.setText(NETWORK_MONITORING_SERVICE);

		OMElement epr3 = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_EPR, ns, webService3);
		epr3.setText("https://" + hostName + ":8443/axis2/services/"
		    + NETWORK_MONITORING_SERVICE);

		OMElement serviceVer3 = fac.createOMElement(
		    RequestResponseConstants.RAW_SERVICE_VERSION, ns, webService3);

		serviceVer3.setText(NETWORK_MONITORING_SERVICE_VERSION);
	}

	protected void createV3WebServices(OMFactory fac, OMNamespace ns,
	    OMElement serverConfig, String hostName) {
		// resource scheduling V3
		OMElement webService4 = fac.createOMElement(
		    RequestResponseConstants.RAW_WEB_SERVICE, ns, serverConfig);
		// ResourceAllocationAndSchedulingService
		OMElement serviceName4 = fac.createOMElement(
		    RequestResponseConstants.RAW_NAME, ns, webService4);
		serviceName4.setText(RES_ALLOC_AND_SCHEDULING_SERVICE);
		OMElement epr4 = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_EPR, ns, webService4);

		epr4.setText("https://" + hostName + ":8443/axis2/services/"
		    + RES_ALLOC_AND_SCHEDULING_SERVICE_EPR);

		OMElement serviceVer4 = fac.createOMElement(
		    RequestResponseConstants.RAW_SERVICE_VERSION, ns, webService4);
		serviceVer4.setText(RES_ALLOC_AND_SCHEDULING_SERVICE_VERSION);

		// network monitoring v3
		OMElement webService5 = fac.createOMElement(
		    RequestResponseConstants.RAW_WEB_SERVICE, ns, serverConfig);
		// ResourceAllocationAndSchedulingService
		OMElement serviceName5 = fac.createOMElement(
		    RequestResponseConstants.RAW_NAME, ns, webService5);
		serviceName5.setText(NETWORK_MONITORING_SERVICE);
		OMElement epr5 = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_EPR, ns, webService5);

		epr5.setText("https://" + hostName + ":8443/axis2/services/"
		    + NETWORK_MONITORING_SERVICE_EPR);

		OMElement serviceVer5 = fac.createOMElement(
		    RequestResponseConstants.RAW_SERVICE_VERSION, ns, webService5);
		serviceVer5.setText(NETWORK_MONITORING_SERVICE_VERSION);

		// system monitoring v3
		OMElement webService6 = fac.createOMElement(
		    RequestResponseConstants.RAW_WEB_SERVICE, ns, serverConfig);
		// ResourceAllocationAndSchedulingService
		OMElement serviceName6 = fac.createOMElement(
		    RequestResponseConstants.RAW_NAME, ns, webService6);
		serviceName6.setText(SYSTEM_MONITORING_SERVICE);
		OMElement epr6 = fac.createOMElement(
		    RequestResponseConstants.RAW_TARGET_EPR, ns, webService6);

		epr6.setText("https://" + hostName + ":8443/axis2/services/"
		    + SYSTEM_MONITORING_SERVICE_EPR);

		OMElement serviceVer6 = fac.createOMElement(
		    RequestResponseConstants.RAW_SERVICE_VERSION, ns, webService6);
		serviceVer6.setText(SYSTEM_MONITORING_SERVICE_VERSION);
	}
}
