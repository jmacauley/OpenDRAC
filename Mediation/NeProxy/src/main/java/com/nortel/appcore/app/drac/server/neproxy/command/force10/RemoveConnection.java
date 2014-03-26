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

package com.nortel.appcore.app.drac.server.neproxy.command.force10;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

public final class RemoveConnection extends AbstractCommandlet {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final Force10NetworkElement ne;
	private final CrossConnection xcon;

	public RemoveConnection(Map<String, Object> param) {
		super(param);
		xcon = (CrossConnection) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_KEY);
		ne = (Force10NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(xcon.getSourceNeId());
	}

	@Override
	public boolean start() throws Exception {
		try {
			if (ne == null) {
				log.error("Force10 RemoveConnection cannot find NE "
				    + xcon.getSourceNeId());
				getCandidate().setErrorCode("ERR_IUID");
				return false;
			}

			log.debug("Force10 RemoveConnection Processing "
			    + getParameters().toString() + " for " + ne.toDebugString());

			try {
				ne.lockCommunicationsWithNe();

				String vlanId = xcon.getVlanId();
				int vid = -1;
				try {
					vid = Integer.parseInt(vlanId);
				}
				catch (NumberFormatException nfe) {
					throw new Exception(
					    "Force10 RemoveConnection Could not extract VLAN id from the vlan field '"
					        + xcon.getVlanId() + "' when deleting the connection " + xcon
					        + " " + ne.toDebugString(), nfe);
				}

				try {
					ne.sendCommandWaitForCommandPrompt("configure\r\n", 2 * 60 * 1000);

					/**
					 * Remove the vlan before creating it, just in case... Will get
					 *
					 * <pre>
					 *  % Error: No such interface Vl 201.
					 * </pre>
					 *
					 * if the vlan does not exist, thats ok.
					 */
					ne.sendCommandWaitForCommandPrompt("no interface vlan " + vid
					    + "\r\n", 2 * 60 * 1000);
				}
				finally {
					/**
					 * if we got into the config menu we want to make sure we get out of
					 * it... send the "end" command even if we fail to configure something
					 * so that we'll get out of the config menu. If we didn't get into the
					 * config menu, the end command will not harm anything.
					 */
					ne.sendCommandWaitForCommandPrompt("end\r\n", 2 * 60 * 1000);
				}

				// Save the running config
                // JHM - commented out.
				// ne.saveRunningConfig();

			}
			finally {
				ne.unlockCommunicationsWithNe();
			}


			return true;
		}
		catch (Exception e) {
			log.error(
			    "Force10 RemoveConnection failed to remove connection! "
			        + ne.toDebugString(), e);
			throw e;
		}
	}

}
