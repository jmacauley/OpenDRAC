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

package com.nortel.appcore.app.drac.server.neproxy.command.hdx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.util.CheckAllComplete;

public final class CreateConnection extends AbstractCommandlet {
	public CreateConnection(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		CrossConnection xcon = (CrossConnection) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_KEY);

		

		NetworkElement ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(xcon.getSourceNeId());

		String fromAid = xcon.getSourceXcAid();
		String toAid = xcon.getTargetXcAid();
		String cktId = xcon.getType();
		String rate = xcon.getRate();
		String numberOfConnectionsInPath = xcon.getNumberOfConnectionsInPath();

		if (ne.getNeMode().toString().equalsIgnoreCase("sdh")) {
			String mapRate = AbstractNetworkElement.getSonetToSdhMap().get(rate);
			log.debug("Converting rate: " + rate + " to: " + mapRate);
			if (mapRate == null) {
				log.error("Unsupport rate " + rate + "(" + mapRate + ")");
				return false;
			}
			rate = mapRate.replace("-", "");
		}
		Tl1CommandCode commandCode = Tl1CommandCode.fromString("ENT-CRS-"
		    + rate.toUpperCase());
		Map<String, String> data = new HashMap<String, String>();
		// the aid for OME starts with rate, not facility
		data.put(ObjectElement.OPERATION_KEY, commandCode.toString());
		data.put("FROMAID", fromAid);
		data.put("TOAID", toAid);
		data.put("LABEL", "\"" + cktId + "\"");
		data.put("CCT", "2WAY");
		TL1RequestMessage message = new TL1RequestMessage(commandCode, data);
		TL1ResponseMessage response = null;
		boolean rc = true;
		try {
			log.debug("Sending the Add Connection to the NE");
			response = ne.getTl1Session().sendSyncMessage(message);
			if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
				log.debug("Connection created");
			}
		}
		catch (Exception e) {
			log.error("Failed in ENT_CRS", e);
			String errorCode = e.getMessage().split(":")[0];
			if (response != null) {
				getCandidate().setAdditionalErrorText(Arrays.toString(
				    response.getTextBlocks()));
			}
			getCandidate().setErrorCode(errorCode);
			rc = false;
		}
		if (numberOfConnectionsInPath != null) {
			try {
				int total = Integer.parseInt(numberOfConnectionsInPath);
				CheckAllComplete.INSTANCE.createElement(cktId, total);
				log.debug("Waiting for all " + total + " to complete");
				CheckAllComplete.INSTANCE.waitFor(cktId);
				log.debug("... " + cktId + " done");
			}
			catch (Exception e) {
				log.error("Unexpected error: ", e);
			}
		}
		return rc;
	}
}
