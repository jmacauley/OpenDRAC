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

package com.nortel.appcore.app.drac.server.lpcp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.server.lpcp.rmi.NeProxyRmiMediator;
import com.nortel.appcore.app.drac.server.lpcp.routing.HierarchicalModel;
import com.nortel.appcore.app.drac.server.lpcp.routing.TopologyManager;

public final class DiscoverThread implements Runnable {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private boolean done;
	private boolean started;
	private final String tid;
	private final String neid;
	private final String ip;
	private final TopologyManager topologyMgr;
	private final HierarchicalModel modelMgr;

	public DiscoverThread(String neId, String neTid, String neIp, String nePort,
	    TopologyManager topologyMgr, HierarchicalModel heirModelMgr) {
		neid = neId;
		tid = neTid;
		ip = neIp;
		this.topologyMgr = topologyMgr;
		modelMgr = heirModelMgr;
	}

	public String getIp() {
		return ip;
	}

	public String getTid() {
		return tid;
	}

	public boolean isDone() {
		return done;
	}

	public boolean isStarted() {
		return started;
	}

	@Override
	public void run() {
		started = true;
		done = discoverNE();
	}

	private boolean discoverNE() {
		boolean discovered = false;
		NetworkElementHolder ne = null;
		boolean proxyDiscovered = false;

		try {
			log.debug("discoverNe running for " + toString());
			/*
			 * STAGE 0: RTRV-NE
			 */

			ne = NeProxyRmiMediator.INSTANCE.getNE(neid);

			// First, we need to ensure that the proxy has fully discovered everything
			// about the NE
			while (!proxyDiscovered) {
				ne = NeProxyRmiMediator.INSTANCE.getNE(neid);
				NeStatus neStatus = ne.getNeStatus();
				if (NeStatus.NE_ALIGNED.equals(neStatus)) {
					proxyDiscovered = true;
				}
				else {
					log.debug("Waiting for NE: " + neid + "(" + tid
					    + ")... current status: " + neStatus);
					Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
				}
			}

			/*
			 * STAGE 1: RTRV-FAC
			 */

			// retrieve facilities
			List<Facility> facList = NeProxyRmiMediator.INSTANCE.getFacilities(
			    neid);

			// String fac = Facility.asXml(facList, neid, ip, port);
			//
			// // facilitiesXML = proxyMediator.getFacilitiesXML(tid, status);//
			// Should be
			// // neid );
			// String properXML = "<nodeList>" + fac + "</nodeList>";

			modelMgr.parseAndAddToModel(facList);

			// Retrieve all xcons
			/*
			 * STAGE 3: RTRV-CRS
			 */

			// Populate trackers in the hierarchical model
			List<CrossConnection> xcons = NeProxyRmiMediator.INSTANCE
			    .getCrossConnections(neid);
			modelMgr.populateTrackers(xcons);

			/*
			 * STAGE 4: DONE
			 */
			discovered = true;

			// If routing graph links were skipped because the facilities were
			// not yet loaded, we need to invoke another consolidation
			// request here:
			topologyMgr.requestConsolidation();

		}
		catch (Exception e) {
			log.error("Unexpected exception caught in DiscoverThread: ", e);
		}
		return discovered;
	}

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("DiscoverThread [done=");
    builder.append(done);
    builder.append(", started=");
    builder.append(started);
    builder.append(", tid=");
    builder.append(tid);
    builder.append(", neid=");
    builder.append(neid);
    builder.append(", ip=");
    builder.append(ip);
    builder.append(", topologyMgr=");
    builder.append(topologyMgr);
    builder.append(", modelMgr=");
    builder.append(modelMgr);
    builder.append("]");
    return builder.toString();
  }
}
