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

package com.nortel.appcore.app.drac.server.neproxy;

import java.rmi.Naming;
import java.util.List;

import javax.sql.DataSource;

import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracPolicyException;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterface;

/**
 * Methods that talk directly to the NeProxy. In olden days these messages would
 * get sent to LPCP_PORT which would proxy them to the NeProxy. This is more direct
 * and easier to trace.
 * 
 * @author pitman
 */
public enum NeProxyManager {
	INSTANCE;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private NeProxyInterface neProxyInterface;
	private InternalLoginToken token;

	public void addManualAdjacency(UserDetails userDetails, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception {
		mustBeAdminUser(userDetails, "addManualAdjacency");
		getNeProxyInterface().addManualAdjacency(token, sourceIEEE, sourcePort,
		    destIEEE, destPort);
	}

	public void changeNetworkElementPassword(UserDetails userDetails,
	    NetworkElementHolder updatedNE) throws Exception {
		mustBeAdminUser(userDetails, "changeNetworkElementPassword");
		getNeProxyInterface().changeNetworkElementPassword(token, updatedNE);
	}

	public void deleteCrossConnection(UserDetails userDetails,
	    CrossConnection xcon) throws Exception {
		mustBeAdminUser(userDetails, "deleteCrossConnection");
		getNeProxyInterface().deleteCrossConnection(token, xcon);
	}

	public void deleteManualAdjacency(UserDetails userDetails, String neIEEE,
	    String port) throws Exception {
		mustBeAdminUser(userDetails, "deleteManualAdjacency");
		getNeProxyInterface().deleteManualAdjacency(token, neIEEE, port);
	}

	public void deleteManualAdjacency(UserDetails userDetails, String sourceIEEE,
	    String sourcePort, String destIEEE, String destPort) throws Exception {
		mustBeAdminUser(userDetails, "deleteManualAdjacency");
		getNeProxyInterface().deleteManualAdjacency(token, sourceIEEE, sourcePort,
		    destIEEE, destPort);
	}

	public void enrollNetworkElement(UserDetails userDetails,
	    NetworkElementHolder newNe) throws Exception {
		mustBeAdminUser(userDetails, "enrollNetworkElement");
		getNeProxyInterface().enrollNetworkElement(token, newNe);
	}

	public List<Facility> getFacilities(UserDetails userDetails, String neId)
	    throws Exception {
		mustBeAdminUser(userDetails, "getFacilities");
		return getNeProxyInterface().getFacilities(token, neId);
	}

	/**
	 * Return void if alive toss and exception otherwise.
	 */
	public void isNeProxyAlive() throws Exception {
		// if we can get a reference we are good, it will verify the interface is
		// alive...
		getNeProxyInterface();
	}

	public void toggleNetworkElementAssociation(UserDetails userDetails,
	    NetworkElementHolder existingNe) throws Exception {
		mustBeAdminUser(userDetails, "toggleNetworkElementAssociation");
		getNeProxyInterface().toggleNetworkElementAssociation(token, existingNe);
	}

	public void updateNetworkElementPosition(UserDetails userDetails, String ip,
	    String port, Double positionX, Double positionY) throws Exception {
		mustBeAdminUser(userDetails, "updateNetworkElementPosition");
		getNeProxyInterface().updateNetworkElementPosition(token, ip, port,
		    positionX, positionY);
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
					Exception e = new Exception("Odd isAlive returned false");
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

		log.debug("Looking up NeProxy@"
		    + RmiServerInfo.NEPROXY_RMI_BINDING_NAME);
		// Obtain the password and lookup the interface in the registry.
		token = InternalLoginHelper.INSTANCE.getToken(InternalLoginTokenType.NEPROXY);
		neProxyInterface = (NeProxyInterface) Naming
		    .lookup(RmiServerInfo.NEPROXY_RMI_BINDING_NAME);

		// verify it.
		if (!neProxyInterface.isAlive(token)) {
			Exception e = new Exception("Odd isAlive returned false");
			log.error("Error: ", e);
			throw e;
		}
		// if we got here, its fine.
		return neProxyInterface;
	}

	/**
	 * Verify that the user is an admin class user, operations from the
	 * adminConsole must be performed by an admin user. Faster than using
	 * isAllowed().
	 */
	private void mustBeAdminUser(UserDetails userDetails, String op)
	    throws Exception {
		if (!userDetails.getUserPolicyProfile().getUserGroupType()
		    .equals(UserGroupProfileXML.UserGroupType.SYSTEM_ADMIN)) {
			throw new DracPolicyException(
			    DracErrorConstants.POLICY_ERROR_GENERAL_ACCESS_CONTROL, new String[] {
			        op, "Must be admin class user" });
		}
	}

  public void updateAddressAndPort(UserDetails userDetails, final String oldAddress, final int oldPort, final String newAddress, final int newPort) throws Exception {
    mustBeAdminUser(userDetails, "updateAddressAndPort");
    getNeProxyInterface().updateAddressAndPort(token, oldAddress, oldPort, newAddress, newPort);
    
  }

}
