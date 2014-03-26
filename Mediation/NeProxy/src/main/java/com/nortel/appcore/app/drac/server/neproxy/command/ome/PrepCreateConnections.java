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

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.Candidate;

// This class is used to setup conditions for the entire service, prior to provisioning (via CreateConnection)
// the individual cross connections for the service.
//
// The following conditions (not necessarily related) are currently handled:
//
// [1] Set up the Layer 2 entities on an L2SS card, prior to any cross connects
// made from its WAN. These steps include
// - creating a new backplane WAN
// - creating the VCS
// - creating the VCE and VCEMAP on one or both sides of the NE's service head/tail
//
// [2] For a VCAT service, delete the 1WAY loopback that was setup on an EPL ETH port
// (this silenced alarms on the port when no service is active). This used to be done right in the
// CreateConnection class, but for a VCAT connection with N members, only the first member
// would delete the loopback; the following N-1 requests would send a TL1 request that would fail.

public final class PrepCreateConnections extends AbstractCommandlet {

  private static final Logger log = LoggerFactory.getLogger(PrepCreateConnections.class);
	private static final String RXPRIOMASK_ALLBITSET = "11111111";
	private static final String RXPRIOMASK_ALLZERO = "0";

	private final HashSet<String> retryErrorCode = new HashSet<String>();
	private List<CrossConnection> xconList = null;
	private CrossConnection referenceXcon = null;
	private NetworkElement ne = null;
	private Element neMediationDataRecord = null;

	public PrepCreateConnections(Map<String, Object> param) {
		super(param);
		retryErrorCode.add("SARB"); // Status-All Resources Busy
		retryErrorCode.add("SRCI"); // Status-Request Command Inhibited
		retryErrorCode.add("SAIS"); // Status-Already Inservice State
		retryErrorCode.add("SAMS"); // Status-Already in Maintenance State

		xconList = (List<CrossConnection>) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_LIST_KEY);
	}

	protected static void removeLoopBack(NetworkElement ne, String portAid) {
		/**
		 * The loopback connection is always an STS3C connection based on the given
		 * facility
		 */
		// Append the fixed channel number of '1':
		String loopbackAid = portAid + "-1";

		// Replace original rate with STS3C
		loopbackAid = "STS3C"
		    + loopbackAid.substring(loopbackAid.indexOf('-'), loopbackAid.length());

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("FROMAID", loopbackAid);
		commandParam.put("TOAID", loopbackAid);
		commandParam.put("CCT", "1WAY");

		// String command = DLT-CRS-STS3C";
		TL1RequestMessage message = new TL1RequestMessage(
		    Tl1CommandCode.DLT_CRS_STS3C, commandParam);

		try {
			ne.getTl1Session().sendSyncMessage(message);
		}
		catch (Exception e) {
			log.error(ne.getNeName() + ": failed to delete loopback at "
			    + loopbackAid, e);
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
			log.error("PrepCreateConnections failed cannot find NE " + ne);
			getCandidate().setErrorCode("ERR_IUID");
			return false;
		}

		if (ne.getState().ordinal() < NeStatus.NE_ALIGNED.ordinal()) {

			getCandidate().setErrorCode("ERR_INRY");
			return false;
		}

		// Sufficient to look at the first xc (in a potential group of vcat members)
		referenceXcon = xconList.get(0);

		/*
		 * [1] L2 preparations
		 */
		if (referenceXcon.isFromSideL2SS() || referenceXcon.isToSideL2SS()) {
			neMediationDataRecord = new Element(ServiceXml.MEDIATION_DATA_RECORD_ID);
			neMediationDataRecord.setAttribute(CrossConnection.SOURCE_NEID, neid);
			neMediationDataRecord.setAttribute(CrossConnection.CALLID,
			    referenceXcon.getCallid());

			returnCode = prepL2SS();

			// Send back mediation data generated on activation (required for tear
			// down)
			if (neMediationDataRecord != null) {
				try {
					getCandidate().setXmlResult(
					    DbOpsHelper.elementToString(neMediationDataRecord));
				}
				catch (Exception e) {
					log.error(
					    "PrepCreateConnection: Exception sending mediation data to client: ",
					    e);
				}
			}
		}

		/*
		 * [2] Loopback handling on EPL ETH ports
		 */
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
			    "PrepCreateConnection::checkFacilityInMA - Failed to query facility state.",
			    e);
		}

		return false;
	}

	private boolean checkLoopbacks() {
		boolean returnCode = true;

		try {
			String fromPortAid = referenceXcon.getSourcePortAid();
			String toPortAid = referenceXcon.getTargetPortAid();
			boolean fromSideIsL2SS = referenceXcon.isFromSideL2SS();
			boolean toSideIsL2SS = referenceXcon.isToSideL2SS();

			String fromSideFacilityAidToCheckMAState = null;
			String toSideFacilityAidToCheckMAState = null;

			boolean fromChangeState = false;
			boolean toChangeState = false;

			// Alarm suppression on L2 EPL card
			if (!fromSideIsL2SS && Facility.isEth(fromPortAid)) {
				fromSideFacilityAidToCheckMAState = fromPortAid; // .replaceFirst("WAN",
				                                                 // "ETH");
				fromChangeState = checkFacilityInMA(fromSideFacilityAidToCheckMAState);
				removeLoopBack(ne, fromPortAid);
			}

			// Alarm suppression on L2 EPL card
			if (!toSideIsL2SS && Facility.isEth(toPortAid)) {
				toSideFacilityAidToCheckMAState = toPortAid; // .replaceFirst("WAN",
				                                             // "ETH");
				toChangeState = checkFacilityInMA(toSideFacilityAidToCheckMAState);
				removeLoopBack(ne, toPortAid);
			}

			if (fromChangeState) {
				changeFacilityState(Tl1CommandCode.RST_ETH,
				    fromSideFacilityAidToCheckMAState);
			}
			if (toChangeState) {
				changeFacilityState(Tl1CommandCode.RST_ETH,
				    toSideFacilityAidToCheckMAState);
			}

		}
		catch (Exception e) {
			returnCode = false;

			String errorCode = e.getMessage().split(":")[0];
			getCandidate().setErrorCode(errorCode);
			log.error(
			    "PrepCreateConnection: Error during removeLoopback - errorCode: "
			        + errorCode, e);
		}

		return returnCode;
	}

	private void createVCE(String vceAid, Map<String, String> requestParms)
	    throws Exception {
		// ENT-VCE:OME0237:VCE-1-13-3-2:139:::DETAILEDOMSTATS=DISABLE,SLBW=BWPRF-1-0,NTBW=BWPRF-1-0,PRBW=BWPRF-1-0,
		// TXCOSPRF=TXCOSPRF-1-0,GDBW=BWPRF-1-0,CRBW=BWPRF-1-0,RXCOSPRF=RXCOSPRF-1-30,STBW=BWPRF-1-0,BRBW=BWPRF-1-0,PLBW=BWPRF-1-0:IS;

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", vceAid);

		// Common parameters
		commandParam.put("DETAILEDOMSTATS", "DISABLE");

		// The 8 Nortel COS: (Critical, Network, Premium, Platinum, Gold, Silver,
		// Bronze, and Standard
		commandParam.put("CRBW", "BWPRF-1-0");
		commandParam.put("NTBW", "BWPRF-1-0");
		commandParam.put("PRBW", "BWPRF-1-0");
		commandParam.put("PLBW", "BWPRF-1-0");
		commandParam.put("GDBW", "BWPRF-1-0");
		commandParam.put("SLBW", "BWPRF-1-0");
		commandParam.put("BRBW", "BWPRF-1-0");
		commandParam.put("STBW", "BWPRF-1-0");

		// Default for 20GL2SS
		commandParam.put("TXCOSPRF", "TXCOSPRF-1-30");
		commandParam.put("RXCOSPRF", "RXCOSPRF-1-30");

		commandParam.put("PST", "IS");

		// Supplied parameters
		commandParam.putAll(requestParms);

		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.ENT_VCE,
		    commandParam);
		TL1ResponseMessage response = ne.getTl1Session().sendSyncMessage(message);

		if (!response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
			throw new Exception("VCE creation did not complete: "
			    + response.getCompletionCode());
		}
	}

	private void createVCEMap(String vcemapAid) throws Exception {
		// ENT-VCEMAP:OME0237:VCEMAP-1-13-4-2-0:143::RX:RXPRIOMASK=11111111,LOCLBL1=1234,ACCESS=ALLOW,TAGS2MATCH=1,RXPRIOSRC=LOCTAG1;
		// ENT-VCEMAP:OME0237:VCEMAP-1-13-4-2-0:143::RX:RXPRIOMASK=0,LOCLBL1=4096,ACCESS=ALLOW,TAGS2MATCH=1,RXPRIOSRC=LOCTAG1;

		Map<String, String> commandParam = new HashMap<String, String>();

		String vlanid = referenceXcon.getVlanId();
		String rxpriomask = RXPRIOMASK_ALLBITSET;

		// Convert user-friendly string flags to
		if (FacilityConstants.UNTAGGED_LOCLBL_FLAG.equals(vlanid)) {
			vlanid = FacilityConstants.UNTAGGED_LOCLBL_VALUE;
		}
		else if (FacilityConstants.ALLTAGGED_LOCLBL_FLAG.equals(vlanid)) {
			vlanid = FacilityConstants.ALLTAGGED_LOCLBL_VALUE;
		}

		// Inter-parameter adjustments
		if (FacilityConstants.UNTAGGED_LOCLBL_VALUE.equals(vlanid)) {
			rxpriomask = RXPRIOMASK_ALLZERO;
		}

		commandParam.put("AID", vcemapAid);
		commandParam.put("DIR", "RX");
		commandParam.put("RXPRIOMASK", rxpriomask);
		commandParam.put("LOCLBL1", vlanid);
		commandParam.put("ACCESS", "ALLOW");
		commandParam.put("TAGS2MATCH", "1");
		commandParam.put("RXPRIOSRC", "LOCTAG1");

		TL1RequestMessage message = new TL1RequestMessage(
		    Tl1CommandCode.ENT_VCEMAP, commandParam);
		TL1ResponseMessage response = ne.getTl1Session().sendSyncMessage(message);

		if (!response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
			throw new Exception("VCEMAP creation did not complete: "
			    + response.getCompletionCode());
		}
	}

	private void createVCS(String vcsAid) throws Exception {
		// ENT-VCS:OME0237:VCS-1-2:106:::DATATYPE=CUSTDATA,COSSUP=CR&NT&PR&PL&GD&SL&BR&ST,MTU=1600,TOPO=P2P,QGRP=1:IS;

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", vcsAid);

		commandParam.put("DATATYPE", "CUSTDATA");
		commandParam.put("COSSUP", "CR&NT&PR&PL&GD&SL&BR&ST");
		commandParam.put("MTU", "1600");
		commandParam.put("TOPO", "P2P");
		commandParam.put("QGRP", "1");
		commandParam.put("PST", "IS");

		// Implement retry cycle here.

		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.ENT_VCS,
		    commandParam);
		TL1ResponseMessage response = ne.getTl1Session().sendSyncMessage(message);

		if (!response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
			throw new Exception("VCS creation did not complete: "
			    + response.getCompletionCode());
		}
	}

	private void createWan(String wanAid, boolean vcatEnabled) throws Exception {
		// ENT-WAN:OME0237:WAN-1-13-101:75:::LCAS=DISABLE,VCAT=DISABLE,IFTYPE=UNI:IS;

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", wanAid);
		commandParam.put("LCAS", "DISABLE");
		commandParam.put("VCAT", (vcatEnabled ? "ENABLE" : "DISABLE"));
		commandParam.put("IFTYPE", "UNI");

		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.ENT_WAN,
		    commandParam);
		TL1ResponseMessage response = ne.getTl1Session().sendSyncMessage(message);

		// Implement retry cycle here.

		if (!response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
			throw new Exception("WAN creation did not complete: "
			    + response.getCompletionCode());
		}
	}

	private int getNextVCID(Candidate candidate, NetworkElement ne, String shelf) {
		int vcid = 0;

		// This id is used in forming the shelf-wide AID for a virtual circuit VCS:
		// VCS-shelf-vcid : VCS-[1..36]-[1..1048575]

		String tempAid = "VCS-" + shelf + "-ALL";

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", tempAid);

		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.RTRV_VCS,
		    commandParam);
		log.debug(ne.getNeName()
		    + "Retrieving VCS records to determine next available VCID");

		TL1ResponseMessage response = null;
		try {
			response = ne.getTl1Session().sendSyncMessage(message);
			List<Map<String, String>> respList = response.getPayload();
			if (respList == null || respList.isEmpty()) {
				vcid = 1;
			}
			else {
				BitSet bs = new BitSet(FacilityConstants.MAX_VCID);
				Iterator<Map<String, String>> resultIr = respList.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					// VCS-1-13001
					String vcsAid = aResult.get("AID");
					String[] vcsAidArray = vcsAid.split("-");
					bs.set(Integer.parseInt(vcsAidArray[2]) - 1);
				}

				int i = 0;
				while (bs.get(i)) {
					i++;
				}

				if (i == FacilityConstants.MAX_VCID) {
					log.error(ne.getNeName() + ": could not determine next VCID");
					vcid = 0;
				}
				else {
					vcid = i + 1;
				}

			}
		}
		catch (Exception e) {
			String errorCode = e.getMessage().split(":")[0];
			candidate.setErrorCode(errorCode);

			if (response != null) {
				candidate.setAdditionalErrorText(Arrays.toString(response
				    .getTextBlocks()));
			}
		}

		return vcid;
	}

	private int getNextWANPort(Candidate candidate, String shelf, String slot) {
		int wanPort = 0;

		String tempAid = "WAN-" + shelf + "-" + slot + "-ALL";

		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", tempAid);

		TL1RequestMessage message = new TL1RequestMessage(Tl1CommandCode.RTRV_WAN,
		    commandParam);
		log.debug(ne.getNeName()
		    + "Retrieving WAN ports  to determine next available.");

		TL1ResponseMessage response = null;
		try {
			response = ne.getTl1Session().sendSyncMessage(message);
			List<Map<String, String>> respList = response.getPayload();
			if (respList == null || respList.isEmpty()) {
				wanPort = FacilityConstants.MIN_WANPORT_anyL2SS;
			}
			else {
				BitSet bs = new BitSet(FacilityConstants.MAX_WANPORT_20GL2SS
				    - FacilityConstants.MIN_WANPORT_anyL2SS + 1);
				Iterator<Map<String, String>> resultIr = respList.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					// WAN-1-13-xxx
					String wanAid = aResult.get("AID");
					String[] wanAidArray = wanAid.split("-");
					bs.set(Integer.parseInt(wanAidArray[3])
					    - FacilityConstants.MIN_WANPORT_anyL2SS);
				}

				int i = 0;
				while (bs.get(i)) {
					i++;
				}

				if (i == FacilityConstants.MAX_WANPORT_20GL2SS
				    - FacilityConstants.MIN_WANPORT_anyL2SS + 1) {
					log.error(ne.getNeName() + ": no WAN port availability!");
				}
				else {
					wanPort = i + FacilityConstants.MIN_WANPORT_anyL2SS;
				}
			}
		}
		catch (Exception e) {
			String errorCode = e.getMessage().split(":")[0];
			candidate.setErrorCode(errorCode);

			if (response != null) {
				candidate.setAdditionalErrorText(Arrays.toString(response
				    .getTextBlocks()));
			}
		}

		return wanPort;
	}

	private boolean prepL2SS() {
		boolean returnCode = true;

		try {
			String sourcePortAid = referenceXcon.getSourcePortAid();
			String[] sourceAidMap = sourcePortAid.split("-");
			String sourceSlot = sourceAidMap[2];
			String sourcePort = sourceAidMap[3];

			String targetPortAid = referenceXcon.getTargetPortAid();
			String[] targetAidMap = targetPortAid.split("-");
			String targetSlot = targetAidMap[2];
			String targetPort = targetAidMap[3];

			// These will be created as needed:
			String sourceWanAidCreated = null;
			String sourceWanSlotCreated = null;
			String sourceWanPortCreated = null;
			String targetWanAidCreated = null;
			String targetWanSlotCreated = null;
			String targetWanPortCreated = null;

			// Shelf can be derived from either:
			String shelf = sourceAidMap[1];

			// Mapping off the L2SS card - need to create backplane WAN. Consider both
			// orientations:
			if (referenceXcon.isFromSideL2SS() == false) {
				if (sourceWanAidCreated == null) {
					sourceWanSlotCreated = targetSlot;
					// Create the backplane WAN port facing the SONET/SDH GFP network
					sourceWanPortCreated = Integer.toString(getNextWANPort(
					    getCandidate(), shelf, sourceWanSlotCreated));
					sourceWanAidCreated = "WAN-" + shelf + "-" + targetSlot + "-"
					    + sourceWanPortCreated;
					boolean vcatEnabled = "true".equalsIgnoreCase(referenceXcon
					    .getVcatRoutingOption());

					log.debug("PrepCreateConnection: " + ne.getNeName()
					    + " creating source WAN: " + sourceWanAidCreated + " vcat: "
					    + (vcatEnabled ? "true" : "false"));
					createWan(sourceWanAidCreated, vcatEnabled);
				}

				neMediationDataRecord.setAttribute(
				    FacilityConstants.L2_MEDIATION_SOURCE_WAN_AID, sourceWanAidCreated);
			}

			if (referenceXcon.isToSideL2SS() == false) {
				if (targetWanAidCreated == null) {
					targetWanSlotCreated = sourceSlot;
					// Create the backplane WAN port facing the SONET/SDH GFP network
					targetWanPortCreated = Integer.toString(getNextWANPort(
					    getCandidate(), shelf, targetWanSlotCreated));
					targetWanAidCreated = "WAN-" + shelf + "-" + sourceSlot + "-"
					    + targetWanPortCreated;
					boolean vcatEnabled = "true".equalsIgnoreCase(referenceXcon
					    .getVcatRoutingOption());

					log.debug("PrepCreateConnection: " + ne.getNeName()
					    + " creating target WAN: " + targetWanAidCreated + " vcat: "
					    + (vcatEnabled ? "true" : "false"));
					createWan(targetWanAidCreated, vcatEnabled);
				}

				neMediationDataRecord.setAttribute(
				    FacilityConstants.L2_MEDIATION_TARGET_WAN_AID, targetWanAidCreated);
			}

			// Create the VCS

			int nextVCID = getNextVCID(getCandidate(), ne, shelf);
			if (nextVCID == 0) {
				log.error("PrepCreateConnection: Failed to determine next VCID");
				return false;
			}
			String vcsAid = "VCS-" + shelf + "-" + nextVCID;
			neMediationDataRecord.setAttribute(
			    FacilityConstants.L2_MEDIATION_VCS_AID, vcsAid);
			log.debug("PrepCreateConnection: " + ne.getNeName() + " creating VCS: "
			    + vcsAid);
			createVCS(vcsAid);

			// Create the source VCE and VCEMAP

			String sourceVceSlot = sourceWanSlotCreated != null ? sourceWanSlotCreated
			    : sourceSlot;
			String sourceVcePort = sourceWanPortCreated != null ? sourceWanPortCreated
			    : sourcePort;
			String sourceVceAid = "VCE-" + shelf + "-" + sourceVceSlot + "-"
			    + sourceVcePort + "-" + nextVCID;
			HashMap<String, String> vceCommandParms = new HashMap<String, String>();
			neMediationDataRecord.setAttribute(
			    FacilityConstants.L2_MEDIATION_SOURCE_VCE_AID, sourceVceAid);
			log.debug("PrepCreateConnection: " + ne.getNeName()
			    + " creating source VCE: " + sourceVceAid);
			createVCE(sourceVceAid, vceCommandParms);
			String sourceVceMapAid = "VCEMAP-" + shelf + "-" + sourceVceSlot + "-"
			    + sourceVcePort + "-" + nextVCID + "-" + "1"; // mapId="1"
			neMediationDataRecord.setAttribute(
			    FacilityConstants.L2_MEDIATION_SOURCE_VCEMAP_AID, sourceVceMapAid);
			log.debug("PrepCreateConnection: " + ne.getNeName()
			    + " creating source VCEMAP: " + sourceVceMapAid);
			createVCEMap(sourceVceMapAid);

			// Create the target VCE and VCEMAP

			String targetVceSlot = targetWanSlotCreated != null ? targetWanSlotCreated
			    : targetSlot;
			String targetVcePort = targetWanPortCreated != null ? targetWanPortCreated
			    : targetPort;
			String targetVceAid = "VCE-" + shelf + "-" + targetVceSlot + "-"
			    + targetVcePort + "-" + nextVCID;
			vceCommandParms = new HashMap<String, String>();
			neMediationDataRecord.setAttribute(
			    FacilityConstants.L2_MEDIATION_TARGET_VCE_AID, targetVceAid);
			log.debug("PrepCreateConnection: " + ne.getNeName()
			    + " creating target VCE: " + targetVceAid);
			createVCE(targetVceAid, vceCommandParms);
			String targetVceMapAid = "VCEMAP-" + shelf + "-" + targetVceSlot + "-"
			    + targetVcePort + "-" + nextVCID + "-" + "1"; // mapId="1"
			neMediationDataRecord.setAttribute(
			    FacilityConstants.L2_MEDIATION_TARGET_VCEMAP_AID, targetVceMapAid);
			log.debug("PrepCreateConnection: " + ne.getNeName()
			    + " creating target VCEMAP: " + targetVceMapAid);
			createVCEMap(targetVceMapAid);
		}
		catch (Exception e) {
			returnCode = false;

			String errorCode = e.getMessage().split(":")[0];
			getCandidate().setErrorCode(errorCode);
			log.error(
			    "PrepCreateConnection: Error during L2 service activation - errorCode: "
			        + errorCode, e);
		}

		return returnCode;
	}

}
