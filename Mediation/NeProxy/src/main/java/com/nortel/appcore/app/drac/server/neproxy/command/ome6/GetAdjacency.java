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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * @author nguyentd
 */
public final class GetAdjacency extends AbstractCommandlet {
	public GetAdjacency(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		/**
		 * OME 6 supports different flavors of adjacency data, each formatted
		 * differently. Use the MECHANISM flag to switch.
		 * <p>
		 * IIH (is-is hello) adjacency SECTion IIH example
		 * 
		 * <pre>
		 *   "OC12-1-11-1:TYP-SH-SL-PRT,SECT:TX_VERSION=\"2\",TX_TAG=\"AD_2_00:1b:25:2d:5b:e6_001_000_011_000_001_ME_1\",RX_VERSION=\"2\",
		 *       RX_ACTUAL=\"AD_2_00:1b:25:2d:5c:55_001_000_012_000_001_ME_1\",RX_RELIABILITY=RELIABLE,MECHANISM=IIH"
		 *   "OC12-1-12-1:TYP-SH-SL-PRT,SECT:TX_VERSION=\"2\",TX_TAG=\"AD_2_00:1b:25:2d:5b:e6_001_000_012_000_001_ME_1\",RX_VERSION=\"2\",
		 *       RX_ACTUAL=\"AD_2_00:1b:25:2d:5c:7a_001_000_011_000_001_ME_1\",RX_RELIABILITY=RELIABLE,MECHANISM=IIH"
		 * </pre>
		 * 
		 * LINE IIH example
		 * 
		 * <pre>
		 *  "OC48-1-3-1:TYP-SH-SL-PRT,LINE:TX_VERSION=\"2\",TX_TAG=\"AD_2_00:1b:25:2c:ab:48_001_000_003_000_001_ME_1\",RX_VERSION=\"\",
		 *      RX_ACTUAL=\"\",RX_EXPECTED=\"\",RECONFIG=DISABLE,RX_RELIABILITY=UNRELIABLE,MECHANISM=IIH"
		 * </pre>
		 * 
		 * Physical adjacency (manually provisioned) example (currently ignored)
		 * 
		 * <pre>
		 *    "ADJ-17-4-1:TYP-SH-SL-PRT,PHYS:TX_VERSION=\"2\",TX_TAG=\"SPOME022-17-4-1\",RX_VERSION=\"2\",RX_ACTUAL=\"\",
		 *       RX_RELIABILITY=UNRELIABLE,MECHANISM=DERIVED"
		 * </pre>
		 * 
		 * Layer 2 SONMP adjacency showing a empty far end (RX) (currently ignored)
		 * 
		 * <pre>
		 *  "ETH-1-38-3:TYP-SH-SL-PRT,L2PHYS:TX_VERSION=\"3\",TX_TAG=\"AD_3_153_047.134.000.118_00:21:E1:D8:0C:EE_02490371\",
		 *      RX_VERSION=\"\",RX_ACTUAL=\"\",RX_EXPECTED=,RECONFIG=ENABLE,RX_RELIABILITY=UNRELIABLE,MECHANISM=SONMP"
		 * </pre>
		 */

		try {
			String aCommand = (String) getParameters().get(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			getParameters().remove(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			NetworkElement ne = (NetworkElement) getParameters().get(
			    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

			Map<String, String> paramList = new HashMap<String, String>();

			for (Map.Entry<String, Object> e : getParameters().entrySet()) {
				paramList.put(e.getKey(), e.getValue().toString());
			}
			paramList.put(ObjectElement.OPERATION_KEY, aCommand);

			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);

			// GG ADJ
			if (result != null) {
				List<NetworkElementAdjacency> list = new ArrayList<NetworkElementAdjacency>();

				for (Map<String, String> m : result) {
					String mech = m.get("MECHANISM");
					// Only process IIH for OME
					if ("IIH".equalsIgnoreCase(mech)) {
						// NetworkElementAdjacency adj = new NetworkElementAdjacency();
						// adj.setNeid(ne.getNeId());
						// adj.setPort(m.get("AID"));
						// adj.setTxTag(m.get("TX_TAG"));
						// adj.setRxTag(m.get("RXACTUAL"));
						// adj.setType(m.get("LAYR"));
						// adj.setManual(false);

						// public NetworkElementAdjacency(String neId, String nePort,
						// String transmitTag, String receiveTag, String topoType, boolean
						// isManual) {
						// neid = neId;
						// port = nePort;
						// txTag = transmitTag;
						// rxTag = receiveTag;
						// type = topoType;
						// manual = isManual;
						// }

						list.add(new NetworkElementAdjacency(ne.getNeId(), m.get("AID"), m
						    .get("TX_TAG"), m.get("RXACTUAL"), m.get("LAYR"), false));
					}
				}

				try {
					// Best effort add of list:
					DbNetworkElementAdjacency.INSTANCE.add(list);
				}
				catch (Exception e) {
					log.error("Failed to add DbNetworkElementAdjacency", e);
				}
			}

			return true;
		}

		catch (Exception e) {
			log.error("Fail to process the NE Adjacency ", e);
		}

		return false;
	}

}
