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

import java.util.List;

import javax.sql.DataSource;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;

/**
 * A NeProxyServerInterface is the implementation of the server side of the
 * NeProxy interface. We use an interface so we can stub out the real server for
 * junit testing.
 */
public interface NeProxyServerInterface {
	void addManualAdjacency(String sourceIEEE, String sourcePort,
	    String destIEEE, String destPort) throws Exception;

	void changeNetworkElementPassword(NetworkElementHolder updatedNE)
	    throws Exception;

	ProvisioningResultHolder createConnection(CrossConnection xcon)
	    throws Exception;

	ProvisioningResultHolder deleteConnection(CrossConnection xcon)
	    throws Exception;

	void deleteCrossConnection(CrossConnection xcon) throws Exception;

	void deleteManualAdjacency(String neIEEE, String port) throws Exception;

	void deleteManualAdjacency(String sourceIEEE, String sourcePort,
	    String destIEEE, String destPort) throws Exception;

	void deleteNetworkElement(NetworkElementHolder oldNe) throws Exception;

	/**
	 * Only to be called by LPCP_PORT which has to update its in-memory model as part
	 * of the facility edit process
	 */
	void editFacility(String neid, String aid, String tna, String faclabel,
	    String mtu, String srlg, String grp, String cost, String metric2,
	    String sigType, String constraints, String domainId, String siteId)
	    throws Exception;

	void enrollNetworkElement(NetworkElementHolder newNe) throws Exception;

	List<CrossConnection> getCrossConnections(String targetNeId) throws Exception;

	List<Facility> getFacilities(String targetNeId) throws Exception;

	List<NetworkElementHolder> getNetworkElements(String tid) throws Exception;

	String getXmlAlarm(String neId) throws Exception;

	boolean isAlive() throws Exception;

	// String getXmlAdjacency(String neId)
	// throws Exception;

	ProvisioningResultHolder postDeleteConnections(List<CrossConnection> xconList)
	    throws Exception;

	ProvisioningResultHolder prepCreateConnections(List<CrossConnection> xconList)
	    throws Exception;

	void registerForEventNotifications(NeProxyEventCallback cb) throws Exception;

	void toggleNetworkElementAssociation(NetworkElementHolder existingNe)
	    throws Exception;

	void updateNetworkElementPosition(String ip, String port, Double positionX,
	    Double positionY) throws Exception;

  void updateAddressAndPort(final String oldAddress, final int oldPort, final String newAddress, final int newPort) throws Exception;

}
