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

package com.nortel.appcore.app.drac.server.neproxy.command.ome6;

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
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * @author nguyentd
 */

public final class AlignNe extends AbstractInitializeNe {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	public AlignNe(AbstractNe ne) {
		super(ne);
	}

	@Override
	public boolean start() throws Exception {
		log.debug("Initializing " + getNe().getNeName());
		Map<String, String> parameter = new HashMap<String, String>();
		List<Map<String, String>> result;
		try {

			NetworkElement tl1Ne = (NetworkElement) getNe();

			String neId = handleRtrvRtgInfo(tl1Ne);
			getNe().setNeId(neId);
			log.debug("Got systemID for ne " + getNe().getNeName() + " ID=" + neId);

			parameter.clear();
			parameter.put(ObjectElement.OPERATION_KEY,
			    Tl1CommandCode.RTRV_SYS.toString());
			result = tl1Ne.getTl1Session().sendToNE(parameter);
			Map<String, String> aResult = result.get(0);
			tl1Ne.setNeMode(NETWORK_ELEMENT_MODE.valueOf(aResult.get("MODE")
			    .toUpperCase()));
			try {
				DbUtility.INSTANCE.invalidateFacility(getNe());
			}
			catch (Exception ie) {
				log.error("Failed to invalidate Facility data for " + getNe(), ie);
			}

			try {
				DbNetworkElementAdjacency.INSTANCE.delete(getNe().getNeId());
			}
			catch (Exception ae) {
				log.error("Failed to delete Adjacency data for " + getNe(), ae);
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
			for (Holder aCommandG : initCommandList) {
				if (aCommandG == null) {
					continue;
				}
				// String aCommand = (String) ir.next();
				String aCommand = aCommandG.getKeyAttr();
				if (aCommand == null) {
					continue;
				}
				// HashMap parameters = new HashMap((HashMap)
				// initCommandList.get(aCommand));
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

			log.debug("Done initializing ne: " + getNe());

		}
		catch (Exception e) {
			log.error("Failed to initialize NE " + getNe(), e);
			return false;
		}

		return true;
	}

	/**
	 * Wayne Pitman: The RTVR-RTG-INFO command can respond in two ways (with and
	 * without an extra colon at the beginning of the response), in order to
	 * accommodate both we took a big hammer and took control over sending and
	 * parsing the response. Our TL1 parser won't parse this type of response
	 * because its actually invalid according to the TL1 spec, the 3rd block
	 * should be name value pairs not position defined pairs. Our hammer is to go
	 * through the raw responses and pull out the system id ourselves.
	 */
	private String handleRtrvRtgInfo(NetworkElement tl1Ne) throws Exception {

		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_RTG_INFO.toString());
		parameter.put("NAME", "\"" + getNe().getNeName() + "\"");

		// We examine the raw TL1 output ourselves and pull out the MAC

		TL1ResponseMessage res = tl1Ne.getTl1Session().sendManualToNe(parameter);

		// block looks like
		// ",\"NEOME501_OCP\",NE-2f-80-1a-ea-27-11,00001600,YES"
		// OR in OME 5.2
		// "SHELF-1::,\"OME03959\",001B252C7D97|47.134.48.172,00001600,YES"
		// Or in OME 6.0
		// // "SHELF-1::,\"OMED2196\",0021E1D80C7F|1.1.1.1,00001600,YES"

		String mac = null;

		for (String block : res.getTextBlocks()) {
			try {
				String[] b = block.split(",");
				if (b.length > 2) {
					mac = b[b.length - 3];
					// Some releases include a | followed by the IP address, strip it off
					// if present.
					mac = new StringTokenizer(mac, "|").nextToken();
					log.debug("Identified MAC as " + mac + " for " + getNe());
					break;
				}
			}
			catch (Exception e) {
				log.error("Unable to parse response  " + block + " from " + getNe(), e);
			}
		}

		// No assumptions here eh?
		mac = mac.substring(0, 2) + "-" + mac.substring(2, 4) + "-"
		    + mac.substring(4, 6) + "-" + mac.substring(6, 8) + "-"
		    + mac.substring(8, 10) + "-" + mac.substring(10, mac.length());

		return mac;
	}

}
