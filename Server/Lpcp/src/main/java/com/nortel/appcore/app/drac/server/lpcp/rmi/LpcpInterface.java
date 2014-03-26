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

import java.rmi.Remote;
import java.util.List;
import java.util.Map;

import org.opendrac.security.InternalLoginToken;

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
 * This class provides an external interface to the LPCP_PORT. These methods should
 * not be used directly by external parties, this is an internal interface only.
 * Callers of the interface must provide a valid credential to prevent the
 * interface from being used by outside parties.
 * 
 * @author pitman
 */
public interface LpcpInterface extends Remote {
	void activateService(InternalLoginToken token, String serviceId)
	    throws Exception;

	List<AuditResult> auditModel(InternalLoginToken token) throws Exception;

	void cancelService(InternalLoginToken token, String[] serviceIds,
	    SERVICE state) throws Exception;

	void confirmService(InternalLoginToken token, String serviceId)
	    throws Exception;

	void correctModel(InternalLoginToken token) throws Exception;

	ScheduleResult createSchedule(InternalLoginToken token,
	    Map<SPF_KEYS, String> parms, boolean queryOnly) throws Exception;

	void deleteNetworkElement(InternalLoginToken token, NetworkElementHolder oldNe)
	    throws Exception;

	/**
	 * This method will invoke the editFacility method on NeProxy and update its
	 * in memory model. Do not call the NeProxy version directly.
	 */
	void editFacility(InternalLoginToken token, String neid, String aid,
	    String tna, String faclabel, String mtu, String srlg, String grp,
	    String cost, String metric2, String sigType, String constraints,
	    String domainId, String siteId) throws Exception;

	double getCurrentBandwidthUsage(InternalLoginToken token, String tna)
	    throws Exception;

	GraphData getGraphData(InternalLoginToken token) throws Exception;

	ServerInfoType getInfo(InternalLoginToken token) throws Exception;

	/**
	 * Return the bandwidth usage of the internal links in the network for the
	 * adminConsole and administrator monitoring of the networks internal links
	 */
	List<BandwidthRecord> getInternalBandwithUsage(InternalLoginToken token,
	    long startTime, long endTime) throws Exception;

	String getLpcpDiscoveryStatus(InternalLoginToken token) throws Exception;

	LpcpStatus getLpcpStatus(InternalLoginToken token) throws Exception;

	String getPeerIPAddress(InternalLoginToken token) throws Exception;

	String getSRLGListForServiceId(InternalLoginToken token, String serviceId)
	    throws Exception;

	/**
	 * isAlive: Returns true if reachable, an exception if not. You don't think
	 * we'd ever return false do you?
	 * 
	 * @throws Exception
	 */
	boolean isAlive(InternalLoginToken token) throws Exception;

	void registerForLpcpEventNotifications(InternalLoginToken token,
	    LpcpEventCallback cb) throws Exception;

	
	ScheduleResult extendServiceTime(InternalLoginToken token, DracService service, Integer minutesToExtendService)throws Exception;
	
}
