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

package com.nortel.appcore.app.drac.server.neproxy.command.cpl;

import java.util.HashMap;
import java.util.Iterator;
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
		log.debug("CPL AlignNE: Initializing CPL NE: " + getNe().getNeName());
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_RTG_INFO.toString());
		parameter.put("NAME", "\"" + getNe().getNeName() + "\"");
		List<Map<String, String>> result;
		try {
			Map<String, String> aResult;
			NetworkElement tl1Ne = (NetworkElement) getNe();

			String neId = handleRtrvRtgInfo2(tl1Ne);
			log.debug("CPL AlignNE: Got systemID: " + neId);
			getNe().setNeId(neId);

			try {
				parameter.clear();
				parameter.put(ObjectElement.OPERATION_KEY,
				    Tl1CommandCode.RTRV_SHELF.toString());
				parameter.put("AID", "ALL");
				result = tl1Ne.getTl1Session().sendToNE(parameter);
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					aResult = resultIr.next();
					tl1Ne.setNeMode(NETWORK_ELEMENT_MODE.valueOf(aResult.get("OSCMODE")
					    .toUpperCase()));
					String osId = aResult.get("OSID");

					// AID is in "SHELF-n" format
					String aid = aResult.get("AID");
					String[] aidMap = aid.split("-");
					log.debug("CPL AlignNE: Got shelf: " + aid + " with osId: " + osId);
					tl1Ne.getShelves().put(aidMap[1], osId);
				}
			}
			catch (Exception e) {
				log.error("CPL AlignNE: Failed to get NE mode", e);
			}

			try {
				DbUtility.INSTANCE.invalidateFacility(getNe());
			}
			catch (Exception ie) {
				log.error("CPL AlignNE: Failed to invalidate Facility data for "
				    + getNe().getNeId(), ie);
			}

			try {
				DbNetworkElementAdjacency.INSTANCE.delete(getNe().getNeId());
			}
			catch (Exception ae) {
				log.error("CPL AlignNE: Failed to delete Adjacency data for "
				    + getNe().getNeId(), ae);
			}

			try {
				DbUtility.INSTANCE.deleteAllXConnection(getNe());
			}
			catch (Exception ae) {
				log.error("Failed to delete connection data for " + getNe(), ae);
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
				Map<String, Object> initParameter = new HashMap<String, Object>(
				    aCommandG.getCommandParam());
				String className = (String) initParameter.get(NePoxyDefinitionsParser.CLASS_ATTR);
				initParameter.remove(NePoxyDefinitionsParser.CLASS_ATTR);
				initParameter.put(NePoxyDefinitionsParser.COMMANDNAME_KEY, aCommand);
				initParameter.put(NePoxyDefinitionsParser.NETWORKELEMENT_KEY, getNe());
				
				AbstractCommandlet command = AbstractCommandlet.getCommandlet(
				    className, initParameter);
				command.start();
			}

			log.debug("CPL AlignNE: done initializing");
		}
		catch (Exception e) {
			log.error("CPL AlignNE: Failed to initialize NE", e);
			return false;
		}
		return true;
	}

	/**
	 * Wayne Pitman: The RTVR-RTG-INFO command can respond in two ways (with and
	 * without an extra colon at the beginning of the response), in order to
	 * accommodate both we took a big hammer and took control over sending and
	 * parsing the response.
	 */

	// private String handleRtrvRtgInfo(NetworkElement tl1Ne)
	// throws Exception
	// {
	// SimpleHashtable parameter = new SimpleHashtable();
	// parameter.put(ObjectElement.OPERATION_KEY, "RTRV-RTG-INFO");
	// parameter.put("NAME", "\"" + ne.getNeName() + "\"");
	//
	// // We examine the raw TL1 output ourselves and pull out the MAC
	//
	// TL1ResponseMessage res = tl1Ne.getTl1Session().sendManualToNe(parameter);
	//
	// // CPL block looks like
	// // "SHELF-11:,ROADMSITE,0017D19E74E8,00011700,NO"
	//
	// String mac = null;
	//
	// for (String block : res.getTextBlocks())
	// {
	// try
	// {
	// String[] b = block.split(",");
	// if (b.length > 2)
	// {
	// mac = b[b.length - 3];
	// // Some releases include a | followed by the IP address, strip it off if
	// present.
	// mac = new StringTokenizer(mac, "|").nextToken();
	// break;
	// }
	// }
	// catch (Exception e)
	// {
	// }
	// }
	//
	// // No assumptions here eh?
	// mac = mac.substring(0, 2) + "-" + mac.substring(2, 4) + "-" +
	// mac.substring(4, 6) + "-" +
	// mac.substring(6, 8) + "-"
	// + mac.substring(8, 10) + "-" + mac.substring(10, mac.length());
	// return mac;
	// }
	private String handleRtrvRtgInfo2(NetworkElement tl1Ne) throws Exception {
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_RTG_INFO.toString());
		parameter.put("NAME", "\"" + getNe().getNeName() + "\"");

		List<Map<String, String>> result = tl1Ne.getTl1Session()
		    .sendToNE(parameter);
		Map<String, String> aResult = result.get(0);
		String neId = new StringTokenizer(aResult.get("NESYSID"), "|").nextToken();
		neId = neId.substring(0, 2) + "-" + neId.substring(2, 4) + "-"
		    + neId.substring(4, 6) + "-" + neId.substring(6, 8) + "-"
		    + neId.substring(8, 10) + "-" + neId.substring(10, neId.length());
		return neId;
	}
}
