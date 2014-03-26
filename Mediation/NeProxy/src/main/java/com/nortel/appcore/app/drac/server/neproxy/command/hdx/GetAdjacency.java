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
 * (Contributors insert name & email here)
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
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.server.neproxy.command.hdx;

import java.util.Map;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

/**
 * Created on Jan 12, 2006
 * 
 * @author nguyentd
 */
public final class GetAdjacency extends AbstractCommandlet {
	// private StringBuffer result;

	// private static final int SUBSLOT_AID = 0;
	// private static final int SIGNAL_AID = 1;

	public GetAdjacency(Map<String, Object> param) {
		super(param);
	}

	// @Override
	// public String getResult()
	// {
	// // return result.toString();
	// return null;
	// }
	//
	@Override
	public boolean start() {
		// <node id="aa-00-00-00-00-08" intid="8" tid="Alkmaar" ip="47.128.38.59"
		// port="10001"
		// status="inService" />
		// <edge aid="OC192-1-5-1" source="aa-00-00-00-00-10"
		// sourceAid="OC192-1-5-1" sourceType="OC192"
		// sourceShelf="1" sourceSlot="5" sourcePort="1"
		// target="aa-00-00-00-00-09" targetAid="OC192-1-6-1" targetType="OC192"
		// targetShelf="1"
		// targetSlot="5" targetPort="1"/>
		try {
			// String aCommand = (String)
			// getParameters().get(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			// getParameters().remove(NePoxyDefinitionsParser.COMMANDNAME_KEY);
			// NetworkElement ne = (NetworkElement)
			// getParameters().get(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			// getParameters().remove(NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			//
			// Map<String, String> paramList = new HashMap<String, String>();
			// paramList.put(ObjectElement.OPERATION_KEY, aCommand);
			//
			// Iterator<String> anotherIr = getParameters().keySet().iterator();
			// while (anotherIr.hasNext())
			// {
			// String aParam = anotherIr.next();
			// String aValue = (String) getParameters().get(aParam);
			// paramList.put(aParam, aValue);
			// }
			//
			// List<String> data = new ArrayList<String>();
			// List<Map<String, String>> result =
			// ne.getTl1Session().sendToNE(paramList);
			// if (result != null)
			// {
			// Iterator<Map<String, String>> resultIr = result.iterator();
			// while (resultIr.hasNext())
			// {
			// Map<String, String> aResult = resultIr.next();
			// // Map facEntry = new HashMap();
			// String aid = aResult.get("AID");
			// String[] aidArray = aid.split("-");
			// // AD_2_TX:TX:TX:TX:TX:TX_Shelf_000_Slot_000_Port_ME_1
			// // AD_1_aa:40:00:00:00:07_001_000_501_000_003
			// String txTag = aResult.get("TX_TAG");
			// String[] txArray = txTag.split("_");
			//
			// // Use "int" instead of String to strip-off leading zero(s)
			// int sShelf = Integer.parseInt(txArray[3]);
			// int sSlot = Integer.parseInt(txArray[5]);
			// int sPort = Integer.parseInt(txArray[7]);
			// String rxTag = aResult.get("RX_ACTUAL");
			// String[] rxArray = rxTag.split("_");
			//
			// String rxAid = "";
			// String rxSystemID = "";
			// if (rxArray.length >= 8)
			// {
			// int tShelf = Integer.parseInt(rxArray[3]);
			// int tSlot = Integer.parseInt(rxArray[5]);
			// int tPort = Integer.parseInt(rxArray[7]);
			// rxAid = aidArray[0] + "-" + tShelf + "-" + tSlot + "-" + tPort;
			// rxSystemID = rxArray[2].replaceAll(":", "-");
			// }
			//
			// String temp = "<adjacencyInstance aid=\"" + aid + "\" " + "source=\"" +
			// txArray[2].replaceAll(":", "-")
			// + "\" " + "sourceAid =\"" + aidArray[0] + "-" + sShelf + "-" + sSlot +
			// "-" + sPort + "\" " +
			//
			// "target=\"" + rxSystemID + "\" " + "targetAid =\"" + rxAid +
			// "\" manualProvision=\"false\" />";
			//
			// data.add(temp);
			// }
			// DbUtility.INSTANCE.addNewAdjacency(ne, data);
			// }
			return true;
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
		return false;
	}
}
