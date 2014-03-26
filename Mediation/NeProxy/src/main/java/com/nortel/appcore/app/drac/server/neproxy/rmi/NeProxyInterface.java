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

package com.nortel.appcore.app.drac.server.neproxy.rmi;

import java.rmi.Remote;
import java.util.List;

import javax.sql.DataSource;

import org.opendrac.security.InternalLoginToken;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;

/**
 * @TODO add the following
 *       <p>
 *       event lister
 */
public interface NeProxyInterface extends Remote {

	void addManualAdjacency(InternalLoginToken token, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception;

	void changeNetworkElementPassword(InternalLoginToken token,
	    NetworkElementHolder updatedNE) throws Exception;

	ProvisioningResultHolder createConnection(InternalLoginToken token,
	    CrossConnection xcon) throws Exception;

	ProvisioningResultHolder deleteConnection(InternalLoginToken token,
	    CrossConnection xcon) throws Exception;

	void deleteCrossConnection(InternalLoginToken token, CrossConnection xcon)
	    throws Exception;

	void deleteManualAdjacency(InternalLoginToken token, String neIEEE,
	    String port) throws Exception;

	void deleteManualAdjacency(InternalLoginToken token, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception;

	void deleteNetworkElement(InternalLoginToken token, NetworkElementHolder oldNe)
	    throws Exception;

	/**
	 * Only to be called by LPCP_PORT which has to update its in-memory model as part
	 * of the facility edit process
	 */
	void editFacility(InternalLoginToken token, String neid, String aid,
	    String tna, String faclabel, String mtu, String srlg, String grp,
	    String cost, String metric2, String sigType, String constraints,
	    String domainId, String siteId) throws Exception;

	/**
	 * Add an NE to be managed.
	 */
	void enrollNetworkElement(InternalLoginToken token, NetworkElementHolder newNe)
	    throws Exception;

	List<CrossConnection> getCrossConnections(InternalLoginToken token,
	    String targetNeId) throws Exception;

	/**
	 * Get Facilities for a NE, fails if NE does not exist or is not aligned.
	 * Could remove and go directly to database.
	 */
	List<Facility> getFacilities(InternalLoginToken token, String targetNeId)
	    throws Exception;

	/**
	 * Return the single named NE, or all NEs if tid is null or exception.
	 */
	List<NetworkElementHolder> getNetworkElements(InternalLoginToken token,
	    String tid) throws Exception;

	/**
	 * Fetch the active (service) alarms for this NE from the database only if the
	 * NE is aligned.
	 */
	String getXmlAlarm(InternalLoginToken token, String neId) throws Exception;

	/**
	 * isAlive: Returns true if reachable, an exception if not. You don't think
	 * we'd ever return false do you?
	 * 
	 * @throws Exception
	 */
	boolean isAlive(InternalLoginToken token) throws Exception;

	ProvisioningResultHolder postDeleteConnections(InternalLoginToken token,
	    List<CrossConnection> xconList) throws Exception;

	ProvisioningResultHolder prepCreateConnections(InternalLoginToken token,
	    List<CrossConnection> xconList) throws Exception;

	/**
	 * Clients can register for event notifications or callbacks.
	 */
	void registerForEventNotifications(InternalLoginToken token,
	    NeProxyEventCallback cb) throws Exception;

	void toggleNetworkElementAssociation(InternalLoginToken token,
	    NetworkElementHolder existingNe) throws Exception;

	void updateNetworkElementPosition(InternalLoginToken token, String ip,
	    String port, Double positionX, Double positionY) throws Exception;

  void updateAddressAndPort(InternalLoginToken token, String oldAddress,
      int oldPort, String newAddress, int newPort) throws Exception;

}
