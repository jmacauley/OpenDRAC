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

package com.nortel.appcore.app.drac.server.neproxy.command.omebaseold;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;

public final class RemoveConnection extends AbstractCommandlet {
	private static final String LOOPBACK_CKTID = "\"DRAC-loopback\"";
	private final HashSet<String> retryErrorCode = new HashSet<String>();

	private CrossConnection xcon = null;
	private Element mediationDataRecord = null;
	private NetworkElement ne = null;

	public RemoveConnection(Map<String, Object> param) {
		super(param);
		retryErrorCode.add("SARB"); // Status-All Resources Busy
		retryErrorCode.add("SRCI"); // Status-Request Command Inhibited

		xcon = (CrossConnection) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_KEY);
	}

	protected static void addLoopBack(NetworkElement ne, String aid) {
		if (aid == null) {
			return;
		}
		/**
		 * The loopback connection is always an STS3C connection based on the given
		 * facility
		 */
		// Replace the channel number with the fixed one: 1
		String tempAid = aid.substring(0, aid.lastIndexOf('-')) + "-1";
		// Replace what orginal rate with STS3C
		tempAid = "STS3C"
		    + tempAid.substring(tempAid.indexOf('-'), tempAid.length());

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("FROMAID", tempAid);
		commandParam.put("TOAID", tempAid);
		commandParam.put("CCT", "1WAY");
		commandParam.put("CKTID", LOOPBACK_CKTID);

		TL1RequestMessage message = new TL1RequestMessage(
		    Tl1CommandCode.ENT_CRS_STS3C, commandParam);
		log.debug(ne.getNeName() + "Adding the loopback connection" + tempAid);
		try {
			ne.getTl1Session().sendSyncMessage(message);
		}
		catch (Exception e) {
			String errorCode = e.getMessage().split(":")[0];

			if ("IEAE".equalsIgnoreCase(errorCode)) {
				// already exists, fine
				log.warn(ne.getNeName() + ": failed to add loopback at " + tempAid
				    + ".. Already exists.", e);
			}
			else {
				log.error(ne.getNeName() + ": failed to add loopback at " + tempAid, e);
			}
		}
	}

	@Override
	public boolean start() {

		boolean returnCode = true;

		ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(xcon.getSourceNeId());

		if (ne == null) {
			log.error("RemoveConnection failed cannot find NE " + ne);
			getCandidate().setErrorCode("ERR_IUID");
			return false;
		}

		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
			log.error("RemoveConnection failed ne not aligned");
			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}

		String mediationDataString = xcon.getMediationData();
		if (mediationDataString != null) {
			try {
				mediationDataRecord = XmlUtility
				    .createDocumentRoot(mediationDataString);
			}
			catch (Exception e) {
				log.error("RemoveConnection - error retrieving mediation data", e);
				getCandidate().setErrorCode("ERR_SIOE");
				return false;
			}
		}

		if (deleteXC() == false) {
			returnCode = false;
		}

		if (postRemoveConnection() == false) {
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

	private void deleteVCE(String vceAid) throws Exception {
		// RMV-VCE:OME0237:VCE-1-13-3-2:149:::;
		// DLT-VCE:OME0237:VCE-1-13-3-2:123:::AUTODLTCHILD=ALL;

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", vceAid);

		// OOS first:
		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.RMV_VCE,
		    commandParam);
		TL1ResponseMessage response = ne.getTl1Session().sendSyncMessage(message);

		if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
			throw new Exception("VCE out-of-service did not complete: "
			    + response.getCompletionCode());
		}

		// Now delete it:
		commandParam.put("AUTODLTCHILD", "ALL");

		message = new TL1RequestMessage(Tl1CommandCode.DLT_VCE, commandParam);
		response = ne.getTl1Session().sendSyncMessage(message);
		if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
			throw new Exception("VCE deletion did not complete: "
			    + response.getCompletionCode());
		}
	}

	private void deleteVCS(String vcsAid) throws Exception {
		// RMV-VCS:OME0237:VCS-1-2:149:::;
		// DLT-VCS:OME0237:VCS-1-2:149:::;

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", vcsAid);

		// OOS first
		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.RMV_VCS,
		    commandParam);
		TL1ResponseMessage response = ne.getTl1Session().sendSyncMessage(message);

		if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
			throw new Exception("VCS out-of-service did not complete: "
			    + response.getCompletionCode());
		}

		// Now delete it:
		message = new TL1RequestMessage(Tl1CommandCode.DLT_VCS, commandParam);
		response = ne.getTl1Session().sendSyncMessage(message);
		if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
			throw new Exception("VCS deletion did not complete: "
			    + response.getCompletionCode());
		}
	}

	private void deleteWAN(String wanAid) throws Exception {
		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", wanAid);

		// OOS first
		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.RMV_WAN,
		    commandParam);
		TL1ResponseMessage response = ne.getTl1Session().sendSyncMessage(message);

		if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
			throw new Exception("WAN out-of-service did not complete: "
			    + response.getCompletionCode());
		}

		// Now delete it:
		message = new TL1RequestMessage(Tl1CommandCode.DLT_WAN, commandParam);
		response = ne.getTl1Session().sendSyncMessage(message);

		if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
			throw new Exception("WAN deletion did not complete: "
			    + response.getCompletionCode());
		}
	}

	private boolean deleteXC() {
		boolean returnCode = false;

		String fromPortAid = xcon.getSourcePortAid();
		String toPortAid = xcon.getTargetPortAid();
		String rate = xcon.getRate();
		String cct = xcon.getType();
		String swmatePortAid = xcon.getSwMatePortAid();
		boolean fromSideIsL2SS = xcon.isFromSideL2SS();
		boolean toSideIsL2SS = xcon.isToSideL2SS();

		// Alarm suppression on EPL card
		boolean loopbackRequired = false;

		Tl1CommandCode commandCode = Tl1CommandCode.fromString("DLT-CRS-"
		    + rate.toUpperCase());
		HashMap<String, String> data = new HashMap<String, String>();
		data.put(ObjectElement.OPERATION_KEY, commandCode.toString());

		// L2SS aid transformations ETH/WAN
		if (mediationDataRecord != null) {
			if (toSideIsL2SS) {
				toPortAid = mediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_SOURCE_WAN_AID);
			}

			if (fromSideIsL2SS) {
				fromPortAid = mediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_TARGET_WAN_AID);
			}
		}

		// We might not have an xc to create, in the case of L2SS hairpin
		if (fromPortAid == null && toPortAid == null) {
			return true;
		}

		// Create the connectable timeslot AIDs.
		//
		// Here, we don't read the XcAid from the CrossConnection object because the
		// port aids may have been
		// modified above (L2SS aid transformations)
		String rateBasedFromXcAid = rate
		    + fromPortAid.substring(fromPortAid.indexOf('-'), fromPortAid.length())
		    + "-" + xcon.getSourceChannel();
		String portBasedFromXcAid = fromPortAid + "-" + xcon.getSourceChannel();
		String rateBasedToXcAid = rate
		    + toPortAid.substring(toPortAid.indexOf('-'), toPortAid.length()) + "-"
		    + xcon.getTargetChannel();
		String portBasedToXcAid = toPortAid + "-" + xcon.getTargetChannel();

		// OME takes xc aids beginning with Rate, not Port.
		data.put("FROMAID", rateBasedFromXcAid);
		data.put("TOAID", rateBasedToXcAid);

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

		TL1RequestMessage message = new TL1RequestMessage(commandCode, data);
		TL1ResponseMessage response = null;
		try {
			response = ne.getTl1Session().sendSyncMessage(message);

			if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
				loopbackRequired = true;
				returnCode = true;
			}
		}
		catch (Exception e) {
			String errorCode = e.getMessage().split(":")[0];
			/*
			 * If the NE doesn't have the connection anymore, then there is a
			 * mismatch, cleaning up by remove the entry in the DB
			 */
			if (errorCode.equalsIgnoreCase("IENE")) {
				try {
					log.error(
					    "Possible mismatch between NE and DRAC's DB. The entry in DB is deleted",
					    e);
					String existingConnection = DbUtility.INSTANCE
					    .retrieveAXConnect(ne, fromPortAid, toPortAid);
					if (existingConnection == null) {
						log.error("Trying to delete a connection that does not exist, either in our database or in the network ! ne:"
						    + ne.getNeId() + " xcon:" + xcon);
						// we can debate if this should be true or not...
						returnCode = true;
					}
					else {
						ByteArrayInputStream aByteStream = new ByteArrayInputStream(
						    existingConnection.getBytes());
						SAXBuilder builder = new SAXBuilder();
						Document aDoc = builder.build(aByteStream);
						Element root = aDoc.getRootElement();

						DbUtility.INSTANCE.deleteXConnect(ne, fromPortAid, toPortAid);

						Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
						dbchgEvent.setReportType("connection");
						Map<String, String> event = new HashMap<String, String>();
						event.put(AbstractFacilityXml.RATE_ATTR,
						    root.getAttributeValue("rate"));
						event.put("sShelf", root.getAttributeValue("sShelf"));
						event.put("sSlot", root.getAttributeValue("sSlot"));
						event.put("sPort", root.getAttributeValue("sPort"));
						event.put("sChannel", root.getAttributeValue("sChannel"));
						event.put("tShelf", root.getAttributeValue("tShelf"));
						event.put("tSlot", root.getAttributeValue("tSlot"));
						event.put("tPort", root.getAttributeValue("tPort"));
						event.put("tChannel", root.getAttributeValue("tChannel"));
						event.put("swmate", root.getAttributeValue("swmate"));
						event.put("source", fromPortAid);
						event.put("operation", "delete");
						event.put("target", toPortAid);
						event.put("cktid", root.getAttributeValue("id"));

						dbchgEvent.addDataElement(event);
						NeProxy.generateEvent(dbchgEvent,
						    ClientMessageXml.DBCHG_EVENT_VALUE);

						DbUtility.INSTANCE.generateLog(
						    new LogRecord(null, ne.getIpAddress(), null, null,
						        LogKeyEnum.KEY_NE_CONNECTION_MISMATCH, null, event));
					}

					loopbackRequired = true;
					returnCode = true;
				}
				catch (Exception ie) {
					log.error("Failed in trying to delete connection", ie);
				}
			}
			log.error("Fail to delete connection", e);
			if (retryErrorCode.contains(errorCode)) {
				/*
				 * Try three times and delay 5 seconds between each try (should be
				 * hardcoded like this)
				 */
				for (int i = 1; i <= 3; i++) {
					try {
						Thread.sleep(5000);
						response = ne.getTl1Session().sendSyncMessage(message);
						if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
							loopbackRequired = true;
							returnCode = true;
						}
					}
					catch (Exception ee) {
						log.error("Retry failed in DLT-CRS for " + i + " times", ee);
						errorCode = ee.getMessage().split(":")[0];
						if (!retryErrorCode.contains(errorCode)) {
							break;
						}
					}
				}
			}
			if (response != null) {
				getCandidate().setErrorCode(errorCode);
				getCandidate().setAdditionalErrorText(
				    Arrays.toString(response.getTextBlocks()));
			}
		}

		if (loopbackRequired) {
			// Here, use the original XC aids. The

			if (fromSideIsL2SS == false
			    && (portBasedFromXcAid.indexOf(FacilityConstants.WAN) >= 0 || portBasedFromXcAid
			        .indexOf(FacilityConstants.ETH) >= 0)) {
				addLoopBack(ne, portBasedFromXcAid);
			}

			// Alarm suppression on EPL card
			if (toSideIsL2SS == false
			    && (portBasedToXcAid.indexOf(FacilityConstants.WAN) >= 0 || portBasedToXcAid
			        .indexOf(FacilityConstants.ETH) >= 0)) {
				addLoopBack(ne, portBasedToXcAid);
			}
		}

		if (getCandidate().getErrorCode() != null) {
			getCandidate().setErrorCode("ERR_SIOE");
		}

		return returnCode;
	}

	private boolean postRemoveConnection() {
		boolean returnCode = true;

		try {
			String mediationDataString = xcon.getMediationData();
			if (mediationDataString != null) {

				@SuppressWarnings("unused")
				Element mediationDataElement = XmlUtility
				    .createDocumentRoot(mediationDataString);

				// Delete the source VCE
				// Note that R6 automatically deletes the associated VCEMAP:
				String sourceVceAid = mediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_SOURCE_VCE_AID);
				if (sourceVceAid != null) {
					log.debug("RemoveConnection: " + ne.getNeName()
					    + " deleting source VCE: " + sourceVceAid);
					deleteVCE(sourceVceAid);
				}

				// Delete the target VCE
				// Note that R6 automatically deletes the associated VCEMAP:
				String targetVceAid = mediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_TARGET_VCE_AID);
				if (targetVceAid != null) {
					log.debug("RemoveConnection: " + ne.getNeName()
					    + " deleting target VCE: " + targetVceAid);
					deleteVCE(targetVceAid);
				}

				// Delete the VCS
				String vcsAid = mediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_VCS_AID);
				if (vcsAid != null) {
					log.debug("RemoveConnection: " + ne.getNeName() + " deleting VCS: "
					    + vcsAid);
					deleteVCS(vcsAid);
				}

				// Delete source WAN
				String sourceWanAidCreated = mediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_SOURCE_WAN_AID);
				if (sourceWanAidCreated != null) {
					log.debug("RemoveConnection: " + ne.getNeName()
					    + " deleting source WAN: " + sourceWanAidCreated);
					deleteWAN(sourceWanAidCreated);
				}

				// Delete target WAN
				String targetWanAidCreated = mediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_TARGET_WAN_AID);
				if (targetWanAidCreated != null) {
					log.debug("RemoveConnection: " + ne.getNeName()
					    + " deleting target WAN: " + targetWanAidCreated);
					deleteWAN(targetWanAidCreated);
				}
			}
		}
		catch (Exception e) {
			returnCode = false;

			String errorCode = e.getMessage().split(":")[0];
			getCandidate().setErrorCode(errorCode);
			log.error(
			    "PostRemoveConnection: Error during L2 service deactivation - errorCode: "
			        + errorCode, e);
		}

		return returnCode;
	}
}
