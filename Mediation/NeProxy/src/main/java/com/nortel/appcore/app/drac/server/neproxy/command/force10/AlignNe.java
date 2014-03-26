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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.Holder;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class AlignNe extends AbstractInitializeNe {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final Force10NetworkElement ne = (Force10NetworkElement) getNe();

	public AlignNe(AbstractNe ne) {
		super(ne);
	}

	@Override
	public boolean start() throws Exception {
		try {
			log.debug("Force10 Alignment begins for " + ne.toString());

			clearDb();

			List<Holder> initCommandList = ne.getNeInitDefinition().get(
			    ne.getNeType());
			String neIp = ne.getIpAddress() + ":" + ne.getPortNumber();

			try {
				// Acquire the communications lock with the NE for the entire alignment.
				ne.lockCommunicationsWithNe();
				for (Holder h : initCommandList) {
					if (h == null) {
						continue;
					}

					String aCommand = h.getKeyAttr();
					if (aCommand == null) {
						continue;
					}
					// HashMap parameters = new HashMap((HashMap)
					// initCommandList.get(aCommand));
					Map<String, Object> parameters = new HashMap<String, Object>(
					    h.getCommandParam());
					String className = (String) parameters.get(NePoxyDefinitionsParser.CLASS_ATTR);
					parameters.remove(NePoxyDefinitionsParser.CLASS_ATTR);
					parameters.put(NePoxyDefinitionsParser.COMMANDNAME_KEY, aCommand);
					parameters.put(NePoxyDefinitionsParser.NETWORKELEMENT_KEY, getNe());
					

					AbstractCommandlet command = AbstractCommandlet.getCommandlet(
					    className, parameters);
					command.start();
				}
			}
			finally {
				// Release the lock.
				ne.unlockCommunicationsWithNe();
			}

			log.debug("...done initializing " + neIp);
			return true;
		}
		catch (Exception e) {
			log.error("Failed to align NE " + ne.toDebugString(), e);
			return false;
		}
	}

	private void clearDb() {

		try {
			DbUtility.INSTANCE.invalidateFacility(getNe());
		}
		catch (Exception ie) {
			log.error("Failed to invalidate Facility data for " + ne.getNeId(), ie);
		}

		try {
			DbNetworkElementAdjacency.INSTANCE.delete(ne.getNeId());
		}
		catch (Exception ae) {
			log.error("Failed to delete Adjacency data for " + ne.getNeId(), ae);
		}

		try {
			DbUtility.INSTANCE.deleteAllXConnection(getNe());
		}
		catch (Exception ae) {
			log.error("Failed to delete connection data for " + getNe(), ae);
		}

	}

}
