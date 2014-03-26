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

import java.io.Serializable;
import java.rmi.Remote;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.GraphData;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.server.lpcp.Lpcp;
import com.nortel.appcore.app.drac.server.lpcp.LpcpEventServer;
import com.nortel.appcore.app.drac.server.lpcp.LpcpScheduler;
import com.nortel.appcore.app.drac.server.lpcp.routing.TopologyManager;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;

public final class LpcpInterfaceImpl implements LpcpServerInterface, Remote,
    Serializable {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final LpcpRemote lpcpRemote;
	private final Lpcp lpcp;
	private final AtomicBoolean paused = new AtomicBoolean();
	
	private final int port = RmiServerInfo.LPCP_PORT;

	public LpcpInterfaceImpl(Lpcp main) throws Exception {
		log.debug("Binding LpcpInterfaceImpl to port " + port);
		lpcpRemote = new LpcpRemote(port, this);
		lpcp = main;
	}

	@Override
	public void activateService(String serviceId) throws Exception {
		checkState();
		lpcp.activateService(serviceId);
	}

	@Override
	public List<AuditResult> auditModel() throws Exception {
		checkState();
		return lpcp.auditModel();
	}

	@Override
	public void cancelService(String[] serviceIds, SERVICE state)
	    throws Exception {
		checkState();
		lpcp.cancelService(serviceIds, state);
	}

	@Override
	public void confirmService(String serviceId) throws Exception {
		checkState();
		lpcp.confirmService(serviceId);
	}

	@Override
	public void correctModel() throws Exception {
		checkState();
		lpcp.auditCorrectModel();
	}

	@Override
	public ScheduleResult createSchedule(Map<SPF_KEYS, String> parms,
	    boolean queryOnly) throws Exception {
		checkState();

		try {
			return new LpcpScheduler(lpcp).doScheduleFrontEnd(parms, queryOnly);
		}
		catch (Exception e) {
			log.error("createSchedule failed", e);
			throw e;
		}
	}

	/**
	 * We handle delete NE requests and then tell the Neproxy to delete the NE so
	 * that we can clean up. When adding an NE, the request goes directly to
	 * NeProxy and we learn about it via events.
	 */
	@Override
	public void deleteNetworkElement(NetworkElementHolder oldNe) throws Exception {
		checkState();
		lpcp.handleDelNodeRequest(oldNe);
	}

	@Override
	public void editFacility(String neid, String aid, String tna,
	    String faclabel, String mtu, String srlg, String grp, String cost,
	    String metric2, String sigType, String constraints, String domainId,
	    String siteId) throws Exception {
		checkState();
		lpcp.editFacility(neid, aid, tna, faclabel, mtu, srlg, grp, cost, metric2,
		    sigType, constraints, domainId, siteId);
	}

	@Override
	public double getCurrentBandwidthUsage(String tna) throws Exception {
		checkState();
		return lpcp.getCurrentBandwidthUsage(tna);
	}

	@Override
	public GraphData getGraphData() throws Exception {
		dumpModelToLog();
		GraphData gd = TopologyManager.INSTANCE.getGraphData();
		
		return gd;
	}

	@Override
	public ServerInfoType getInfo() throws Exception {
		return lpcp.getInfo();
	}

	@Override
	public List<BandwidthRecord> getInternalBandwithUsage(long startTime,
	    long endTime) throws Exception {
		checkState();
		return lpcp.getInternalBandwithUsage(startTime, endTime);
	}

	@Override
	public String getLpcpDiscoveryStatus() throws Exception {
		return lpcp.getDiscStatus();
	}

	@Override
	public LpcpStatus getLpcpStatus() throws Exception {
		return lpcp.getStatus();
	}

	@Override
	public String getPeerIPAddress() throws Exception {
		return lpcp.getPeerIp();
	}

	public LpcpRemote getRemote() {
		return lpcpRemote;
	}

	@Override
	public String getSRLGListForServiceId(String serviceId) throws Exception {
		checkState();
		return lpcp.getSRLGListForServiceId(serviceId);
	}

	@Override
	public boolean isAlive() throws Exception {
		return true;
	}

	@Override
	public void registerForLpcpEventNotifications(LpcpEventCallback cb)
	    throws Exception {
		checkState();
		LpcpEventServer.INSTANCE.addListener(cb);
	}

	/**
	 * Called on operations we don't want to function when the server is not
	 * active. Its ok to let the debug and get methods to function when the server
	 * is not active, not a good idea to provision changes on an inactive server!
	 * 
	 * @throws Exception
	 */
	private void checkState() throws Exception {
		if (paused.get()) {
			throw new Exception(
			    "Operation cannot be executed at this time, server is paused, or not active.");
		}
	}

	private void dumpModelToLog() {
		try {
			Map<String, Map<String, LpcpFacility>> m = lpcp
			    .getDRACHierarchicalModel().getModel();
			log.debug("Managing "+m.keySet().size()+" Network elements:");
			for (String ne : m.keySet()) {
				log.debug(ne);
			}
			for (Map.Entry<String, Map<String, LpcpFacility>> e : m.entrySet()) {
				
				for (Map.Entry<String, LpcpFacility> e1 : e.getValue().entrySet()) {
				  
				}
			}
			
		}
		catch (Exception e) {
			log.error("Dump graph exception: ", e);
		}
	}
	
	
	@Override
	public ScheduleResult extendServiceTime(DracService service, Integer minutesToExtendService) throws Exception{
		try {
			return lpcp.extendServiceTime(service, minutesToExtendService );
		}
		catch (Exception e) {
			log.error("alterServiceTime failed", e);
			throw e;
		}
	}
}
