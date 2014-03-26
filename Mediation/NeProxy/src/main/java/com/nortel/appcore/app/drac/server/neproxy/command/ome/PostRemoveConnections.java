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

package com.nortel.appcore.app.drac.server.neproxy.command.ome;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

// Also see: PrepCreateConnections

// This class is used to handle conditions for the entire service, following the deletion of (via
// RemoveConnection)
// the individual cross connections for the service.
//
// The following conditions (not necessarily related) are currently handled:
//
// [1] Remove the Layer 2 entities on an L2SS card. These steps include
// - delete the VCS
// - delete the VCE (and VCEMAP) on one or both sides of the NE's service head/tail
// - delete the associated WAN
//
// [2] For a VCAT service, add a 1WAY loopback on an EPL ETH port
// (this silences alarms on the port when no service is active). This used to be done right in the
// RemoveConnection class, but for a VCAT connection with N members, only the first member
// would add the loopback; the following N-1 requests would send a TL1 request that would fail.

public final class PostRemoveConnections extends AbstractCommandlet {
  private static final Logger log = LoggerFactory.getLogger(PostRemoveConnections.class);
	private static final String LOOPBACK_CKTID = "\"DRAC-loopback\"";

	private final HashSet<String> retryErrorCode = new HashSet<String>();
	private List<CrossConnection> xconList = null;
	private CrossConnection referenceXcon = null;
	private NetworkElement ne = null;
	private Element neMediationDataRecord = null;

	public PostRemoveConnections(Map<String, Object> param) {
		super(param);
		retryErrorCode.add("SARB"); // Status-All Resources Busy
		retryErrorCode.add("SRCI"); // Status-Request Command Inhibited

		xconList = (List<CrossConnection>) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_LIST_KEY);
	}

	protected static void addLoopBack(NetworkElement ne, String portAid) {
		// Append the fixed channel number of '1':
		String loopbackAid = portAid + "-1";

		// Replace original rate with STS3C
		loopbackAid = "STS3C"
		    + loopbackAid.substring(loopbackAid.indexOf('-'), loopbackAid.length());

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("FROMAID", loopbackAid);
		commandParam.put("TOAID", loopbackAid);
		commandParam.put("CCT", "1WAY");
		commandParam.put("CKTID", LOOPBACK_CKTID);

		TL1RequestMessage message = new TL1RequestMessage(
		    Tl1CommandCode.ENT_CRS_STS3C, commandParam);
		log.debug(ne.getNeName() + "Adding the loopback connection" + loopbackAid);
		try {
			ne.getTl1Session().sendSyncMessage(message);
		}
		catch (Exception e) {
			String errorCode = e.getMessage().split(":")[0];

			if ("IEAE".equalsIgnoreCase(errorCode)) {
				// already exists, fine
				log.warn(ne.getNeName() + ": failed to add loopback at " + loopbackAid
				    + ".. Already exists.", e);
			}
			else {
				log.error(
				    ne.getNeName() + ": failed to add loopback at " + loopbackAid, e);
			}
		}
	}

	@Override
	public boolean start() {

		boolean returnCode = true;

		if (xconList == null || xconList.size() == 0) {

			return true;
		}

		String neid = xconList.get(0).getSourceNeId();
		ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(neid);

		if (ne == null) {
			log.error("PostRemoveConnections failed cannot find NE " + ne);
			getCandidate().setErrorCode("ERR_IUID");
			return false;
		}

		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {
			log.error("PostRemoveConnections failed ne not aligned");
			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}

		// Sufficient to look at the first xc (in a potential group of vcat members)
		referenceXcon = xconList.get(0);

		String neMediationDataString = referenceXcon.getMediationData();
		if (neMediationDataString != null) {
			try {
				neMediationDataRecord = XmlUtility
				    .createDocumentRoot(neMediationDataString);
			}
			catch (Exception e) {
				log.error("PostRemoveConnections - error retrieving mediation data", e);
				getCandidate().setErrorCode("ERR_SIOE");
				return false;
			}
		}

		returnCode = postL2SS();

		returnCode = checkLoopbacks();

		// Set return status
		if (returnCode == false) {
			// if not already set above...
			if (getCandidate().getErrorCode() != null) {
				getCandidate().setErrorCode("ERR_SIOE");
			}
		}

		return returnCode;
	}

	private boolean checkLoopbacks() {
		boolean returnCode = true;

		try {
			String fromPortAid = referenceXcon.getSourcePortAid();
			String toPortAid = referenceXcon.getTargetPortAid();
			boolean fromSideIsL2SS = referenceXcon.isFromSideL2SS();
			boolean toSideIsL2SS = referenceXcon.isToSideL2SS();

			if (fromSideIsL2SS == false && Facility.isEth(fromPortAid)) {
				addLoopBack(ne, fromPortAid);
			}

			// Alarm suppression on EPL card
			if (toSideIsL2SS == false && Facility.isEth(toPortAid)) {
				addLoopBack(ne, toPortAid);
			}

		}
		catch (Exception e) {
			returnCode = false;

			String errorCode = e.getMessage().split(":")[0];
			getCandidate().setErrorCode(errorCode);
			log.error(
			    "PostRemoveConnection: Error during checkLoopbacks - errorCode: "
			        + errorCode, e);
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
		TL1ResponseMessage response = null;
		try {
			response = ne.getTl1Session().sendSyncMessage(message);

			if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
				log.debug("VCE out-of-service did not complete: "
				    + response.getCompletionCode());
			}
		}
		catch (Exception e) {
			// If it was already put into maintanance, we'll come here. Try to delete
			// anyways.
			log.debug("VCE out-of-service did not complete: " + vceAid);
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
		TL1ResponseMessage response = null;

		try {
			response = ne.getTl1Session().sendSyncMessage(message);

			if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
				// We'll arrive here if it was already in a maintanance state. Try to
				// delete anyways.
				log.debug("VCS out-of-service did not complete: "
				    + response.getCompletionCode());
			}
		}
		catch (Exception e) {
			log.debug("VCS out-of-service did not complete: " + vcsAid);
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
		TL1ResponseMessage response = null;
		try {
			response = ne.getTl1Session().sendSyncMessage(message);

			if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
				log.debug("WAN out-of-service did not complete: " + wanAid + " "
				    + response.getCompletionCode());
			}
		}
		catch (Exception e) {
			// The WAN may have already been in a maintanance state (if the L2SS eqpt
			// is OOS-MA);
			// in which case we'd receive an exception here. Try to delete anyways.
			log.debug("WAN out-of-service did not complete: " + wanAid);
		}

		// Now delete it:
		message = new TL1RequestMessage(Tl1CommandCode.DLT_WAN, commandParam);
		response = ne.getTl1Session().sendSyncMessage(message);

		if (response.getCompletionCode().equals(TL1Constants.COMPLETED) == false) {
			throw new Exception("WAN deletion did not complete: "
			    + response.getCompletionCode());
		}
	}

	private boolean postL2SS() {
		boolean returnCode = true;

		try {
			if (neMediationDataRecord != null) {
				// Delete the source VCE
				// Note that R6 automatically deletes the associated VCEMAP:
				String sourceVceAid = neMediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_SOURCE_VCE_AID);
				if (sourceVceAid != null) {
					log.debug("PostRemoveConnection: " + ne.getNeName()
					    + " deleting source VCE: " + sourceVceAid);
					deleteVCE(sourceVceAid);
				}

				// Delete the target VCE
				// Note that R6 automatically deletes the associated VCEMAP:
				String targetVceAid = neMediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_TARGET_VCE_AID);
				if (targetVceAid != null) {
					log.debug("PostRemoveConnection: " + ne.getNeName()
					    + " deleting target VCE: " + targetVceAid);
					deleteVCE(targetVceAid);
				}

				// Delete the VCS
				String vcsAid = neMediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_VCS_AID);
				if (vcsAid != null) {
					log.debug("PostRemoveConnection: " + ne.getNeName()
					    + " deleting VCS: " + vcsAid);
					deleteVCS(vcsAid);
				}

				// Delete source WAN
				String sourceWanAidCreated = neMediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_SOURCE_WAN_AID);
				if (sourceWanAidCreated != null) {
					log.debug("PostRemoveConnection: " + ne.getNeName()
					    + " deleting source WAN: " + sourceWanAidCreated);
					deleteWAN(sourceWanAidCreated);
				}

				// Delete target WAN
				String targetWanAidCreated = neMediationDataRecord
				    .getAttributeValue(FacilityConstants.L2_MEDIATION_TARGET_WAN_AID);
				if (targetWanAidCreated != null) {
					log.debug("PostRemoveConnection: " + ne.getNeName()
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
