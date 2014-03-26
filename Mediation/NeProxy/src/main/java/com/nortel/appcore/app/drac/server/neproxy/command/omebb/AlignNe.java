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

package com.nortel.appcore.app.drac.server.neproxy.command.omebb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.Holder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class AlignNe extends AbstractInitializeNe {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public AlignNe(AbstractNe ne) {
		super(ne);
	}

	@Override
	public boolean start() throws Exception {
		log.debug("Initializing " + getNe().getNeName());

		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_RTG_INFO.toString());
		parameter.put("NAME", getNe().getNeName());
		List<Map<String, String>> result;
		try {
			NetworkElement tl1Ne = (NetworkElement) getNe();
			result = tl1Ne.getTl1Session().sendToNE(parameter);
			Map<String, String> aResult = result.get(0);
			StringTokenizer systemIdToken = new StringTokenizer(
			    aResult.get("NESYSID"), "|");
			String neId = systemIdToken.nextToken();
			neId = neId.substring(0, 2) + "-" + neId.substring(2, 4) + "-"
			    + neId.substring(4, 6) + "-" + neId.substring(6, 8) + "-"
			    + neId.substring(8, 10) + "-" + neId.substring(10, neId.length());
			getNe().setNeId(neId);
			
			parameter.clear();
			parameter.put(ObjectElement.OPERATION_KEY,
			    Tl1CommandCode.RTRV_SYS.toString());
			result = tl1Ne.getTl1Session().sendToNE(parameter);
			aResult = result.get(0);
			tl1Ne.setNeMode(NETWORK_ELEMENT_MODE.valueOf(aResult.get("MODE")
			    .toUpperCase()));
			try {
				DbUtility.INSTANCE.invalidateFacility(getNe());
			}
			catch (Exception ie) {
				log.error(
				    "Failed to invalidate Facility data for " + getNe().getNeId(), ie);
			}

			try {
				DbNetworkElementAdjacency.INSTANCE.delete(getNe().getNeId());
			}
			catch (Exception ae) {
				log.error("Failed to delete Adjacency data for " + getNe().getNeId(),
				    ae);
			}

			List<Holder> initCommandList = getNe().getNeInitDefinition().get(
			    getNe().getNeType());
			String neIp = getNe().getIpAddress();
			for (int i = 0; i < initCommandList.size(); i++) {
				Holder aCommandG = initCommandList.get(i);
				if (aCommandG == null) {
					continue;
				}
				String aCommand = aCommandG.getKeyAttr();
				if (aCommand == null) {
					continue;
				}
				Map<String, Object> parameters = new HashMap<String, Object>(
				    aCommandG.getCommandParam());
				String className = (String) parameters.get(NePoxyDefinitionsParser.CLASS_ATTR);
				parameters.remove(NePoxyDefinitionsParser.CLASS_ATTR);
				parameters.put(NePoxyDefinitionsParser.COMMANDNAME_KEY, aCommand);
				parameters.put(NePoxyDefinitionsParser.NETWORKELEMENT_KEY, getNe());
				

				AbstractCommandlet command = AbstractCommandlet.getCommandlet(
				    className, parameters);
				command.start();
			}

			log.debug("...done initializing");

		}
		catch (Exception e) {
			log.error("Failed to initialize NE", e);
			return false;
		}

		return true;
	}
}