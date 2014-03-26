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

package com.nortel.appcore.app.drac.server.neproxy.command.ome7;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
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
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private final HashSet<String> retryErrorCode = new HashSet<String>();
	private CrossConnection xcon = null;
	private Element neMediationDataRecord = null;
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

	@Override
	public boolean start() throws Exception {
		

		boolean returnCode = true;

		String neid = xcon.getSourceNeId();
		ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(neid);

		if (ne == null) {
			log.error("CreateConnection failed cannot find NE " + ne);
			getCandidate().setErrorCode("ERR_IUID");
			return false;
		}

		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
			
			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}

		String neMediationDataString = xcon.getMediationData();
		if (neMediationDataString != null) {
			try {
				neMediationDataRecord = XmlUtility
				    .createDocumentRoot(neMediationDataString);
			}
			catch (Exception e) {
				log.error("CreateConnection - error parsing input mediation data", e);
				getCandidate().setErrorCode("ERR_SIOE");
				return false;
			}
		}

		returnCode = createXC();

		// Set return status
		if (returnCode == false) {
			// if not already set above...
			if (getCandidate().getErrorCode() != null) {
				getCandidate().setErrorCode("ERR_SIOE");
			}
		}

		return returnCode;
	}

	private boolean createXC() throws Exception {
		boolean returnCode = false;
		String fromPortAid = xcon.getSourcePortAid();
		String toPortAid = xcon.getTargetPortAid();
		String cktId = xcon.getId();
		String rate = xcon.getRate();
		String cct = xcon.getType();
		String swmatePortAid = xcon.getSwMatePortAid();
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

		// Check for 'transformed' aids ETH to WAN
		if (neMediationDataRecord != null) {
			if (toSideIsL2SS) {
				toPortAid = neMediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_SOURCE_WAN_AID);
			}

			if (fromSideIsL2SS) {
				fromPortAid = neMediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_TARGET_WAN_AID);
			}
		}

		// We might not have an xc to create, in the case of L2SS hairpin
		if (fromPortAid == null && toPortAid == null) {
			return true;
		}

		// Create the connectable timeslot AIDs
		String fromXcAid = rate
		    + fromPortAid.substring(fromPortAid.indexOf('-'), fromPortAid.length())
		    + "-" + xcon.getSourceChannel();
		String toXcAid = rate
		    + toPortAid.substring(toPortAid.indexOf('-'), toPortAid.length()) + "-"
		    + xcon.getTargetChannel();

		data.put("FROMAID", fromXcAid);
		data.put("TOAID", toXcAid);

		data.put("CKTID", "\"" + cktId + "\"");

		if (cct != null && cct.length() > 0) {
			data.put("CCT", cct);
		}
		else {
			data.put("CCT", "2WAY");
		}

		if (swmatePortAid != null && swmatePortAid.length() > 0) {
			data.put(
			    "SWMATE",
			    rate
			        + swmatePortAid.substring(swmatePortAid.indexOf('-'),
			            swmatePortAid.length()) + "-" + xcon.getSwMateChannel());
		}

		if (aEnd != null && zEnd != null) {
			data.put("AEND", aEnd);
			data.put("ZEND", zEnd);
		}

		// Send the TL1 message:
		TL1RequestMessage message = new TL1RequestMessage(
		    Tl1CommandCode.fromString(commandCode), data);
		TL1ResponseMessage response = null;

		try {
			response = ne.getTl1Session().sendSyncMessage(message);

			if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
				log.debug("CreateConnection: NE " + ne.getNeName()
				    + " XC created from: " + fromXcAid + " to: " + toXcAid);
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

		return returnCode;
	}
}
