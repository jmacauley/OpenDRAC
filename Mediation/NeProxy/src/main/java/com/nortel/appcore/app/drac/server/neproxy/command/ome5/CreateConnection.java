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

package com.nortel.appcore.app.drac.server.neproxy.command.ome5;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
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
	// private static final String RXPRIOMASK_ALLBITSET = "11111111";
	// private static final String RXPRIOMASK_ALLZERO = "0";

	private final HashSet<String> retryErrorCode = new HashSet<String>();
	private CrossConnection xcon = null;
	private NetworkElement ne = null;

	public CreateConnection(Map<String, Object> param) {
		super(param);
		retryErrorCode.add("SARB"); // Status-All Resources Busy
		retryErrorCode.add("SRCI"); // Status-Request Command Inhibited
		retryErrorCode.add("SAIS"); // Status-Already Inservice State
		retryErrorCode.add("SAMS"); // Status-Already in Maintenance State

		xcon = (CrossConnection) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_KEY);
	}

	protected static void removeLoopBack(NetworkElement ne, String aid) {
		/**
		 * The loopback connection is always an STS3C connection based on the given
		 * facility
		 */
		// Replace the channel number with the fixed one: 1
		String tempAid = aid.substring(0, aid.lastIndexOf('-')) + "-1";
		// Replace what original rate with STS3C
		tempAid = "STS3C"
		    + tempAid.substring(tempAid.indexOf('-'), tempAid.length());

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("FROMAID", tempAid);
		commandParam.put("TOAID", tempAid);
		commandParam.put("CCT", "1WAY");
		// String command = DLT-CRS-STS3C";
		TL1RequestMessage message = new TL1RequestMessage(
		    Tl1CommandCode.DLT_CRS_STS3C, commandParam);
		
		try {
			ne.getTl1Session().sendSyncMessage(message);
		}
		catch (Exception e) {
			log.error(ne.getNeName() + ": failed to delete loopback at " + tempAid, e);
		}
	}

	@Override
	public boolean start() {
		

		boolean returnCode = true;

		String neid = xcon.getSourceNeId();
		ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(neid);

		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
			
			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}

		// The actual xc creation
		if (createXC() == false) {
			returnCode = false;
		}

		// Set return status
		if (!returnCode) {
			// if not already set above...
			if (getCandidate().getErrorCode() != null) {
				getCandidate().setErrorCode("ERR_SIOE");
			}
		}

		return returnCode;
	}

	private boolean changeFacilityState(Tl1CommandCode command, String aid) {
		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", aid);
		TL1RequestMessage message = new TL1RequestMessage(command, commandParam);
		
		try {
			ne.getTl1Session().sendSyncMessage(message);
			return true;
		}
		catch (Exception e) {
			/*
			 * The only valid exception is SAMS (Status, Already in Mainatenance
			 * State) and SAIS (Status, Already in Service
			 */
			String errorCode = e.getMessage().split(":")[0];
			if (retryErrorCode.contains(errorCode)) {
				return true;
			}
			log.error(ne.getNeName() + ": failed to change facility's state", e);
		}
		return false;
	}

	private boolean checkFacilityInMA(String aid) {
		try {
			// Make sure that the facility is not in maintainance mode before putting
			// it to OOS
			Facility facilityRecord = null;

			Map<String, String> filter = new HashMap<String, String>();
			filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());
			filter.put(DbKeys.NetworkElementFacilityCols.AID, aid);
			List<Facility> list = DbNetworkElementFacility.INSTANCE
			    .retrieveFacilities(filter);

			if (list.size() == 1) {
				facilityRecord = list.get(0);
				String state = facilityRecord.getPrimaryState();
				if (state.indexOf("MA") < 0) {
					this.changeFacilityState(Tl1CommandCode.RMV_ETH, aid);
					return true;
				}
			}
		}
		catch (Exception e) {
			log.error(
			    "CreateConnection::checkFacilityInMA - Failed to query facility state.",
			    e);
		}

		return false;
	}

	private boolean createXC() {
		boolean returnCode = false;

		String fromXcAid = xcon.getSourceXcAid();
		String toXcAid = xcon.getTargetXcAid();
		String cktId = xcon.getId();
		String rate = xcon.getRate();
		String cct = xcon.getType();
		String swmate = xcon.getSwMateXcAid();
		String aEnd = xcon.getBlsrAend();
		String zEnd = xcon.getBlsrZend();
		boolean fromSideIsL2SS = xcon.isFromSideL2SS();
		boolean toSideIsL2SS = xcon.isToSideL2SS();
		String numberOfConnectionsInPath = xcon.getNumberOfConnectionsInPath();

		String commandCode = "ENT-CRS-" + rate.toUpperCase();
		Map<String, String> data = new HashMap<String, String>();

		// the aid for OME starts with rate
		data.put(ObjectElement.OPERATION_KEY, Tl1CommandCode
		    .fromString(commandCode).toString());

		// Create the connectable timeslot AIDs
		fromXcAid = rate
		    + fromXcAid.substring(fromXcAid.indexOf('-'), fromXcAid.length());
		toXcAid = rate + toXcAid.substring(toXcAid.indexOf('-'), toXcAid.length());

		data.put("FROMAID", fromXcAid);
		data.put("TOAID", toXcAid);

		data.put("CKTID", "\"" + cktId + "\"");

		if (cct != null && cct.length() > 0) {
			data.put("CCT", cct);
		}
		else {
			data.put("CCT", "2WAY");
		}

		if (swmate != null && swmate.length() > 0) {
			data.put("SWMATE",
			    rate + swmate.substring(swmate.indexOf('-'), swmate.length()));
		}

		if (aEnd != null && zEnd != null) {
			data.put("AEND", aEnd);
			data.put("ZEND", zEnd);
		}

		String fromSideFacilityAidToCheckMAState = null;
		String toSideFacilityAidToCheckMAState = null;
		boolean fromChangeState = false;
		boolean toChangeState = false;

		// Alarm suppression on L2 EPL card
		if (!fromSideIsL2SS
		    && (fromXcAid.indexOf("WAN") >= 0 || fromXcAid.indexOf("ETH") >= 0)) {
			fromSideFacilityAidToCheckMAState = fromXcAid.replaceFirst("WAN", "ETH");
			fromChangeState = checkFacilityInMA(fromSideFacilityAidToCheckMAState);
			removeLoopBack(ne, fromXcAid);
		}

		// Alarm suppression on L2 EPL card
		if (!toSideIsL2SS
		    && (toXcAid.indexOf("WAN") >= 0 || toXcAid.indexOf("ETH") >= 0)) {
			toSideFacilityAidToCheckMAState = toXcAid.replaceFirst("WAN", "ETH");
			toChangeState = checkFacilityInMA(toSideFacilityAidToCheckMAState);
			removeLoopBack(ne, toXcAid);
		}

		// Send the TL1 message:
		TL1RequestMessage message = new TL1RequestMessage(
		    Tl1CommandCode.fromString(commandCode), data);
		TL1ResponseMessage response = null;

		try {
			response = ne.getTl1Session().sendSyncMessage(message);

			if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
				log.debug(ne.getNeName() + " XC created from: " + fromXcAid + " to: "
				    + toXcAid);
				returnCode = true;
			}

		}
		catch (Exception e) {
			log.error("Failed in ENT_CRS \n", e);
			String errorCode = e.getMessage().split(":")[0];

			if (retryErrorCode.contains(errorCode)) {
				/*
				 * Try three times with delay 5 seconds between
				 */
				for (int i = 1; i <= 3; i++) {
					try {
						Thread.sleep(5000);
						response = ne.getTl1Session().sendSyncMessage(message);
						if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
							
							returnCode = true;
						}
					}
					catch (Exception ee) {
						log.error("Retry failed in ENT_CRS for " + i + " times" + "\n"
						    + response.toString(), ee);
						errorCode = ee.getMessage().split(":")[0];
						if (!retryErrorCode.contains(errorCode)) {
							break;
						}
					}
				}
			}

			if (returnCode == false) {
				getCandidate().setErrorCode(errorCode);

				if (response != null) {
					getCandidate().setAdditionalErrorText(
					    Arrays.toString(response.getTextBlocks()));
				}
				else {
					getCandidate().setAdditionalErrorText(e.getMessage());
				}
			}
		}

		/*
		 * Bring the facility back to IS when all path connections have been
		 * completely created
		 */
		if (numberOfConnectionsInPath != null) {
			try {
				int total = Integer.parseInt(numberOfConnectionsInPath);
				CheckAllComplete.INSTANCE.createElement(cktId, total);
				
				CheckAllComplete.INSTANCE.waitFor(cktId);
				
			}
			catch (Exception e) {
				log.error("Unexpected error: ", e);
			}
		}
		if (fromChangeState) {
			changeFacilityState(Tl1CommandCode.RST_ETH,
			    fromSideFacilityAidToCheckMAState);
		}
		if (toChangeState) {
			changeFacilityState(Tl1CommandCode.RST_ETH,
			    toSideFacilityAidToCheckMAState);
		}

		return returnCode;
	}
}
