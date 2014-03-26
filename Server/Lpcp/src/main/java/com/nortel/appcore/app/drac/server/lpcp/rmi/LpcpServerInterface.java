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

import java.util.List;
import java.util.Map;

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
import com.nortel.appcore.app.drac.server.nrb.LpcpEventCallback;

/**
 * A LpcpServerInterface is the implementation of the server side of the LPCP_PORT
 * interface. We use an interface so we can stub out the real server for junit
 * testing.
 */
public interface LpcpServerInterface {
	void activateService(String serviceId) throws Exception;

	List<AuditResult> auditModel() throws Exception;

	void cancelService(String[] serviceIds, SERVICE state) throws Exception;

	void confirmService(String serviceId) throws Exception;

	void correctModel() throws Exception;

	ScheduleResult createSchedule(Map<SPF_KEYS, String> parms, boolean queryOnly)
	    throws Exception;

	void deleteNetworkElement(NetworkElementHolder oldNe) throws Exception;

	/**
	 * This method will invoke the editFacility method on NeProxy and update its
	 * in memory model. Do not call the NeProxy version directly.
	 */
	void editFacility(String neid, String aid, String tna, String faclabel,
	    String mtu, String srlg, String grp, String cost, String metric2,
	    String sigType, String constraints, String domainId, String siteId)
	    throws Exception;

	double getCurrentBandwidthUsage(String tna) throws Exception;

	GraphData getGraphData() throws Exception;

	ServerInfoType getInfo() throws Exception;

	List<BandwidthRecord> getInternalBandwithUsage(long startTime, long endTime)
	    throws Exception;

	String getLpcpDiscoveryStatus() throws Exception;

	LpcpStatus getLpcpStatus() throws Exception;

	String getPeerIPAddress() throws Exception;

	String getSRLGListForServiceId(String serviceId) throws Exception;

	boolean isAlive() throws Exception;

	void registerForLpcpEventNotifications(LpcpEventCallback cb) throws Exception;

	
	ScheduleResult extendServiceTime(DracService service,Integer minutesToExtendService) throws Exception;
}
