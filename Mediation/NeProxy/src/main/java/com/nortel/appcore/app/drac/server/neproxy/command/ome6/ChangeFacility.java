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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
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

public final class ChangeFacility extends AbstractCommandlet {
	private static final HashSet<String> PARAMETER_LIST = new HashSet<String>();
	public static final HashMap<String, String> NE_ATTRIBUTES = new HashMap<String, String>();
	public static final HashMap<String, String> XML_ATTRIBUTES = new HashMap<String, String>();

	static {
		PARAMETER_LIST.addAll(AbstractFacilityXml.COMMON_PARAMETER_LIST);
		PARAMETER_LIST.add(ClientMessageXml.AUTONEGOTIATION_KEY);
		PARAMETER_LIST.add(ClientMessageXml.MTU_KEY);

		NE_ATTRIBUTES.put(ClientMessageXml.MTU_KEY, OmeFacilityXML.NEKEY_MTU);
		// neAttributes.put(ClientMessageXML.AUTONEGOTIATION_KEY,
		// OmeFacilityXML.NEKEY_AN);

		XML_ATTRIBUTES.putAll(AbstractFacilityXml.COMMON_XML_ATTRIBUTES);
	}

	public ChangeFacility(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		boolean rc = false;
		

		String targetNeId = (String) getParameters().get(ClientMessageXml.NEID_KEY);
		final NetworkElement ne = (NetworkElement) DiscoverNePool.INSTANCE
		    .getNeByTidOrIdOrIpandPort(targetNeId);
		Iterator<String> ir = PARAMETER_LIST.iterator();
		final String aid = (String) getParameters().get(ClientMessageXml.AID_KEY);
		HashMap<String, String> modifyNeAttribute = new HashMap<String, String>();
		HashMap<String, String> modifyDBAttribute = new HashMap<String, String>();
		while (ir.hasNext()) {
			String aParamKey = ir.next();
			String aParamValue = (String) getParameters().get(aParamKey);
			// Determine the owner of the atrribute(s) (i.e it can be controlled
			// by the NE or by DRAC. It it's the latter case, then just modify
			// the value in the DB only.
			if (aParamValue != null) {
				if (NE_ATTRIBUTES.containsKey(aParamKey)) {
					modifyNeAttribute.put(NE_ATTRIBUTES.get(aParamKey), aParamValue);
				}
				else {
					modifyDBAttribute.put(XML_ATTRIBUTES.get(aParamKey), aParamValue);
				}
			}
		}

		if (!modifyNeAttribute.isEmpty()) {
			TL1ResponseMessage response = null;
			try {
				String changedFac = DbUtility.INSTANCE.retrieveAFacility(ne, aid);
				if (changedFac == null) {
					log.debug("Only support attribute MTU " + aid);
				}
				else {
					int startTypeIndex = changedFac.indexOf("mtu=") + 5;
					int endTypeIndex = changedFac.indexOf('"', startTypeIndex);
					String tempMtu = changedFac.substring(startTypeIndex, endTypeIndex);

					startTypeIndex = changedFac.indexOf("primaryState=") + 14;
					endTypeIndex = changedFac.indexOf('"', startTypeIndex);
					String tempState = changedFac.substring(startTypeIndex, endTypeIndex);

					if (tempMtu.equals(modifyNeAttribute.get(OmeFacilityXML.NEKEY_MTU))) {
						log.debug("No change in MTU for " + aid);
					}
					else {
						Map<String, String> param = new HashMap<String, String>();
						param.put(ObjectElement.OPERATION_KEY,
						    Tl1CommandCode.ED_ETH.toString());
						param.put(OmeFacilityXML.NEKEY_MTU,
						    modifyNeAttribute.get(OmeFacilityXML.NEKEY_MTU));
						param.put("AID", aid);

						// put the facility to OOS if it's not in OSS-MA
						boolean performStateChange = false;
						if (tempState.indexOf("MA") < 0) {
							changeFacilityState(ne, Tl1CommandCode.RMV_ETH, aid);
							performStateChange = true;
						}
						else {
							
						}

						TL1RequestMessage message = new TL1RequestMessage(
						    Tl1CommandCode.ED_ETH, param);

						log.debug("Sending the Edit Ether to the NE");
						response = ne.getTl1Session().sendSyncMessage(message);
						if (response.getCompletionCode().equals(TL1Constants.COMPLETED)) {
							log.debug("Facility modified");

							/*
							 * Event will be sent out when the NE updates its database (i.e.
							 * we will receive the dbchange event)
							 */
							rc = true;
						}
						else {
							log.error("Failed in ED-ETH");
							getCandidate().setErrorCode("ERR_SIOE");
						}
						// put the facility bask to IS if it wasn't in OOS-MA
						if (performStateChange) {
							changeFacilityState(ne, Tl1CommandCode.RST_ETH, aid);
						}
					}
				}
			}
			catch (Exception e) {
				log.error("Failed in ED-ETH", e);
				getCandidate().setErrorCode("ERR_SIOE");
				if (response != null) {
					getCandidate().setAdditionalErrorText(
					    Arrays.toString(response.getTextBlocks()));
				}
			}
		}

		if (modifyDBAttribute != null) {
			try {
				log.debug("Modify the Facility attribute in DB " + modifyDBAttribute);
				DbUtility.INSTANCE.updateAFacility(ne, modifyDBAttribute, aid);
				ne.upDateLocalInfo();
				Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
				dbchgEvent.setReportType("facility");
				Map<String, String> data = new HashMap<String, String>();
				data.put("aid", aid);
				data.putAll(modifyDBAttribute);
				data.put("operation", "modify");
				dbchgEvent.addDataElement(data);
				NeProxy.generateEvent(dbchgEvent, ClientMessageXml.DBCHG_EVENT_VALUE);

				DbUtility.INSTANCE.generateFacilityUpdatedLog(ne, aid, data);
				// May 20, 2008 - Update for SURFnet
				//
				// When a L2 (ETH/WAN) facility is 'configured' into DRAC, the reqt is
				// to
				// silence alarms that would otherwise be raised while the facility
				// remains
				// unconnected. This step is accomplished by provisioning a
				// unidirectional
				// loopback connection across the facility, named with a predetermined
				// label (used elsewhere for identifying the loopback as originating
				// from
				// DRAC).
				//
				// The facility is considered as 'configured' for DRAC when the client
				// side
				// ETH signaling type is set as per:
				//
				// - any value -> UNI: owned by DRAC as client
				// - UNI -> any value: not owned by DRAC as client
				//
				// The loopback provisioning/deletion will be done best-effort only. Any
				// failure will not be seen by the user in response to the edit-facility
				// operation (only the logs will reflect the failure). This scenario
				// could
				// occur, for example, if someone edits the facility while a real
				// service
				// is still inProgress.
				//

				// GGL2
				String[] aidMap = aid.split("-");
				String shelf = aidMap[1];
				String slot = aidMap[2];

				// automatic loopbacks only for ETH with existing WAN
				if (!ne.slotIsL2SS(shelf, slot)) {
					final String signalType = modifyDBAttribute
					    .get(AbstractFacilityXml.SIGNALTYPE_ATTR);

					if (signalType != null) {
						if (Facility.isEth(aid)) {
							/*
							 * Put this in a new thread to ensure that the edit facility
							 * operation response time is not affected, particularly if the
							 * assoc is down (the ENT/DLT-CRS commands can be slow to
							 * return/timeout).
							 */
							final Thread thread = new Thread(new Runnable() {
								@Override
								public void run() {
									/*
									 * Note: the aid that we're dealing with here is that of a
									 * facility/port. e.g. ETH-1-1-1 The existing methods for
									 * adding and deleting loopbacks deal with payload/channel
									 * aids. e.g. ETH-1-1-1-1 We can safely append a payload value
									 * of '1' to this aid (since this is a Layer 2 aid). Also, the
									 * aid type qualifier at the front end is not important since
									 * it is stripped off by the add/removeLoopback methods. I
									 * will just prepend 'na'.
									 */
									String modifyAid = "na"
									    + aid.substring(aid.indexOf('-'), aid.length()) + "-1";

									if (FacilityConstants.SIGNAL_TYPE.UNI.toString().equals(
									    signalType)) {
										PostRemoveConnections.addLoopBack(ne, modifyAid);
									}
									else {
										PrepCreateConnections.removeLoopBack(ne, modifyAid);
									}
								}
							}, "DRAC-loopback thread");
							thread.setDaemon(true);
							thread.start();
						}
					}
				}

				rc = true;
			}
			catch (Exception e) {
				log.error("Failed in modify facility DB", e);
				getCandidate().setErrorCode("ERR_SIOE");
			}
		}

		return rc;
	}

	private boolean changeFacilityState(NetworkElement ne,
	    Tl1CommandCode command, String aid) {
		Map<String, String> commandParam = new HashMap<String, String>();
		commandParam.put("AID", aid);
		TL1RequestMessage message = new TL1RequestMessage(command, commandParam);
		log.debug(ne.getNeName() + ": Bring the " + aid + " to " + command);
		try {
			ne.getTl1Session().sendSyncMessage(message);
			return true;
		}
		catch (Exception e) {
			// String errorCode = e.getMessage().split(":")[0];
			log.error(ne.getNeName() + ": failed to change facility's state", e);
		}
		return false;
	}

}
