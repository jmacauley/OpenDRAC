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

package com.nortel.appcore.app.drac.server.lpcp.rmi;

import java.io.ByteArrayInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;
import com.nortel.appcore.app.drac.server.lpcp.Lpcp;
import com.nortel.appcore.app.drac.server.lpcp.LpcpEventHandler;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEvent;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEventCallback;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterface;

/**
 * Communicate with the NEProxy via the NeProxies RMI interface.
 * 
 * @author pitman
 */
public enum NeProxyRmiMediator implements NeProxyEventCallback {
  INSTANCE;
  private final Logger log = LoggerFactory.getLogger(getClass());
	private NeProxyInterface neProxyInterface;
	private InternalLoginToken token;
	private Lpcp lpcp;

	private NeProxyRmiMediator() {
		try {
			UnicastRemoteObject.exportObject(this);

			/*
			 * NOTE: Use public static Remote exportObject(Remote obj, int port) if
			 * you want to use a fixed port number to communicate with. We export this
			 * object as a callback with LPCP_PORT which is always on the same box as us,
			 * hence a anonymous port number will do for us.
			 */
		}
		catch (RemoteException e) {
			log.error(
			    "Failed to export this object for use as a RMI callbak! ", e);
		}
	}

	public ProvisioningResultHolder createConnection(CrossConnection xcon)
	    throws Exception {
		return getNeProxyInterface().createConnection(token, xcon);
	}

	public ProvisioningResultHolder deleteConnection(CrossConnection xcon)
	    throws Exception {
		return getNeProxyInterface().deleteConnection(token, xcon);
	}

	public void deleteNetworkElement(NetworkElementHolder oldNe) throws Exception {
		getNeProxyInterface().deleteNetworkElement(token, oldNe);
	}

	public void editFacility(String neid, String aid, String tna,
	    String faclabel, String mtu, String srlg, String grp, String cost,
	    String metric2, String sigType, String constraints, String domainId,
	    String siteId) throws Exception {
		getNeProxyInterface().editFacility(token, neid, aid, tna, faclabel, mtu,
		    srlg, grp, cost, metric2, sigType, constraints, domainId, siteId);
	}

	public List<CrossConnection> getCrossConnections(String neId)
	    throws Exception {
		return getNeProxyInterface().getCrossConnections(token, neId);
	}

	public List<Facility> getFacilities(String targetNeId) throws Exception {
		return getNeProxyInterface().getFacilities(token, targetNeId);
	}

	public NetworkElementHolder getNE(String neid) throws Exception {
		List<NetworkElementHolder> result = getNeProxyInterface()
		    .getNetworkElements(token, neid);
		if (result == null || result.isEmpty()) {
			return null;
		}
		if (result.size() != 1) {
			throw new Exception(
			    "getNE got multiple results back, expecting only one for " + neid
			        + " results=" + result);
		}
		return result.get(0);
	}

	/**
	 * Fetches either all NEs (if neid==null) or the named NE.
	 */
	public List<NetworkElementHolder> getNEs(String neid) throws Exception {
		return getNeProxyInterface().getNetworkElements(token, neid);
	}

	public String getXmlAlarm(String neId) throws Exception {
		return getNeProxyInterface().getXmlAlarm(token, neId);
	}

	public boolean isAlive() {
		try {
			// Getting the interface will invoke isAlive and will toss an exception if
			// it isn't alive.
			getNeProxyInterface();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return nothing if alive, exception if dead!
	 */
	public void isAliveWithException() throws Exception {
		/*
		 * getting the interface will invoke isAlive and will succeed or toss an
		 * exception, nothing else required.
		 */
		getNeProxyInterface();
	}

	public void listenForEvents(Lpcp lpcpInstance) throws Exception {
		lpcp = lpcpInstance;
		getNeProxyInterface().registerForEventNotifications(token, this);
	}

	/**
	 * Event callback: Events from LPCP_PORT arrive here after we register for them.
	 */
	@Override
	public void neProxyEventReceived(NeProxyEvent event) {
		try {
			
			ClientMessageXml data = new ClientMessageXml(new ByteArrayInputStream(
			    event.getXml().getBytes()));

			if (data.isAlarm()) {
				lpcp.getScheduler().processAlarm(data);
				return;
			}

			String xml = data.eventNodeToString();
			

			/*
			 * <message type="report" aTag="10" command="association">
			 * <NetworkElementEvent> <event name="association" id="TDEFAULT_PROXY_10"
			 * time="1147953763082"> <data><element name="description" value="N/A"
			 * /></data> <eventInfo notificationType="aligned"
			 * occurredDate="2006-05-18" occurredTime="08:02:43" /> <node type="OME"
			 * id="00-15-9B-FD-09-7E" ip="47.134.25.123" port="10001" tid="OME0307"
			 * mode="SDH" status="aligned" /> </event> <backslashNetworkElementEvent>
			 * </message>
			 */

			Map<String, Object> evt = new HashMap<String, Object>();
			evt.put("COMMAND", "AO");
			evt.put("OPERATION", "REPT");
			evt.put("EVENT", xml);

			
			LpcpEventHandler.getInstance(lpcp).handleCallback(evt);
		}
		catch (Exception e) {
			log.error(
			    "neProxyEventReceived: Something wrong processing neProxy event "
			        + event, e);
		}
	}

	public ProvisioningResultHolder postDeleteConnections(
	    List<CrossConnection> xconList) throws Exception {
		return getNeProxyInterface().postDeleteConnections(token, xconList);
	}

	public ProvisioningResultHolder prepCreateConnections(
	    List<CrossConnection> xconList) throws Exception {
		return getNeProxyInterface().prepCreateConnections(token, xconList);
	}

	/**
	 * Get a RMI handle to the NeProxy process. We need the local password to talk
	 * to it. Attempts to use a cached copy, if its invalid get a new one.
	 */
	private synchronized NeProxyInterface getNeProxyInterface() throws Exception {
		// Use our cached copy if its still valid.
		if (neProxyInterface != null) {
			try {
				if (!neProxyInterface.isAlive(token)) {
					Exception e = new Exception("Odd isAlive returned false!");
					log.error("Error: ", e);
					throw e;
				}
				// still alive, go for it.
				return neProxyInterface;
			}
			catch (Exception e) {
				// no longer alive;
				neProxyInterface = null;
			}
		}

		// Obtain the password and lookup the interface in the registry.
		token = InternalLoginHelper.INSTANCE.getToken(InternalLoginTokenType.NEPROXY);
		neProxyInterface = (NeProxyInterface) Naming
		    .lookup(RmiServerInfo.NEPROXY_RMI_BINDING_NAME);

		// verify it.
		if (!neProxyInterface.isAlive(token)) {
			throw new Exception("NeProxyInterface isAlive returned false!");
		}
		// if we got here, its fine.
		return neProxyInterface;
	}

}
