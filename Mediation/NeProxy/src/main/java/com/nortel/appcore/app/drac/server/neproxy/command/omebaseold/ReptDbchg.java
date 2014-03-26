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

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.OperationType;
import com.nortel.appcore.app.drac.common.utility.NetworkElementEventXML;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;
import com.nortel.appcore.app.drac.server.neproxy.util.CheckAllComplete;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;

/**
 * Created on Sep 9, 2005
 *
 * @author nguyentd
 */
public final class ReptDbchg extends AbstractCommandlet {
	enum CHANGE {
		MODIFY("modify"), ADD("add"), DELETE("delete");

		private final String operationString;

		CHANGE(String value) {
			operationString = value;
		}

		String asString() {
			return operationString;
		}
	}

	private CHANGE operation;
	private Tl1XmlDbChangeEvent dbchgEvent;

	public ReptDbchg(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() throws Exception {
		/*
		 * "DBCHGSEQ=1038,DATE=05-03-30,TIME=20-45-53,USERID=ADMIN,SOURCE=1,
		 * PRIORITY=GEN_TL1_CMD:ENT-CRS-STS1:STS1-1-9-1-8,STS1-1-10-1-8::
		 * CKTID=\"Performance Testing 8\":"
		 */
		AbstractNe ne = (AbstractNe) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		ne.upDateLocalInfo();
		dbchgEvent = new Tl1XmlDbChangeEvent(ne);
		TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
		    AbstractNetworkElement.EVENTRECV_KEY);
		Map<String, String> values = anEvent.getPayloads().get(0);
		// dbchgEvent.setReportType(anEvent.getCommandCode());

		log.debug("Process DBCHG event: " + values);
		dbchgEvent.setOccurrentDate(values.get("DATE"));
		dbchgEvent.setOccurrentTime(values.get("TIME"));

		String neId = ne.getNeId();
		dbchgEvent.setEventId(neId + "_" + values.get("DBCHGSEQ"));
		dbchgEvent.setOwnerId(ne.getTerminalId());
		String command = values.get("COMMAND");
		// String cktid = values.get("CKTID");
		dbchgEvent.updateDescription(values.get("PRIOR"));
		// dbchgEvent.setReportType(command);

		boolean result = false;
		if (command.indexOf("ENT-") >= 0 || command.indexOf("DLT-") >= 0) {
			if (command.indexOf("ENT-") >= 0) {
				operation = CHANGE.ADD;
			}
			else {
				operation = CHANGE.DELETE;
			}
            // Is this a cross connection operation?
			if (command.indexOf("-CRS-") >= 0) {
				dbchgEvent.setReportType("connection");
				result = handleConnectionEvent(ne, values, dbchgEvent);
			}
            // Is this a facility operation?
			else if (command.indexOf("-OC") >= 0 || command.indexOf("-WAN") >= 0
			    || command.indexOf("-ETH") >= 0) {
				dbchgEvent.setReportType("facility");
				result = handleStateChangeEvent(ne, values, operation);
			}
            // Is this a configuration change on a BLSR?
			else if (command.indexOf("-BLSRMAP") >= 0) {
				dbchgEvent.setReportType("facility");
				result = handleBlsrChangeEvent(ne, values, operation);
			}
			else if (command.indexOf("-FFP-") >= 0) {
				dbchgEvent.setReportType("facility");
				result = handleProtectionChangeEvent(ne, values, operation);
			}
		}
		else if (command.indexOf("-TNE") >= 0) {
			/*
			 * Provisioned Tunnel Endpoints operations ignored, this statement stops
			 * us entering the next bloc and thinking we are dealing with ETH or WAN
			 * changes
			 */

		}
		else if (command.indexOf("STCHG-") >= 0 || command.indexOf("ED-") >= 0
		    || command.indexOf("RST-") >= 0 || command.indexOf("RMV-") >= 0) {
			/*
			 * for the ED-ETH, ED-WAN, ..., we can process the event based on the data
			 * included in the report without going back to the NE for the changed
			 * attributes, but we perform a complete retrieval anyway, just in case...
			 */
			dbchgEvent.setReportType("facility");
			operation = CHANGE.MODIFY;
			result = handleStateChangeEvent(ne, values, operation);
		}

		return result;
	}

	private void checkForDbChange(AbstractNe ne, Tl1CommandCode mod, String aid) {

		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put(ObjectElement.OPERATION_KEY, mod.toString());
		paramList.put("AID", aid);
		try {
			ArrayList<Map<String, String>> temp = new ArrayList<Map<String, String>>();
			temp.add(new HashMap<String, String>());
			List<Map<String, String>> result = ((NetworkElement) ne).getTl1Session()
			    .sendToNE(paramList);
			if (result != null) {
				Map<String, String> aResult = result.get(0);
				String changedFac = DbUtility.INSTANCE.retrieveAFacility(ne, aid);
				if (changedFac == null) {

					return;
				}
				OmeFacilityXML changedFacXML = new OmeFacilityXML(changedFac);

				// Build the list of XML attributes based on the NE key/value.
				Map<String, String> currentNeAttrValue = new HashMap<String, String>();
				Iterator<String> keyIr = OmeFacilityXML.wanNeToXmlMapping.keySet()
				    .iterator();
				while (keyIr.hasNext()) {
					String neKey = keyIr.next();
					String xmlKey = OmeFacilityXML.wanNeToXmlMapping.get(neKey);
					String aValue = aResult.get(neKey);
					if (aValue != null) {
						currentNeAttrValue.put(xmlKey, aValue);
					}
				}
				Map<String, String> updateList = changedFacXML.updateFacilityAttr(aid,
				    currentNeAttrValue);
				if (updateList != null) {
					log.debug("Facility's attr to update in checkForDbChange:"
					    + updateList);
					DbUtility.INSTANCE.updateAFacility(ne, updateList, aid);
					updateList.put("operation", "modify");
					updateList.put("aid", aid);

					Tl1XmlDbChangeEvent additionalEvent = new Tl1XmlDbChangeEvent(ne);
					additionalEvent.setOccurrentDate(dbchgEvent.getOccurrentDate());
					additionalEvent.setOccurrentTime(dbchgEvent.getOccurrentTime());
					additionalEvent.setEventId(ne.getNeId() + "_"
					    + System.currentTimeMillis());
					additionalEvent.setOwnerId(ne.getTerminalId());
					additionalEvent.setNotificationType("facility");
					additionalEvent.updateDescription("PROXY_GEN");
					additionalEvent.addDataElement(updateList);
					getParameters().put(AbstractCommandlet.ADDITIONALEVENT_KEY,
					    additionalEvent);
				}
				else {

				}
			}
			else {
				log.error("Got empty result from querying new state of " + aid
				    + " from NE");
			}
		}
		catch (Exception e) {
			log.error("Failed to perform " + mod + " from " + ne.getNeName(), e);
		}
	}

	private Map<String, String> getAndUpdateFacility(AbstractNe ne,
	    Tl1CommandCode mod, String aid, CHANGE oper) throws Exception {
		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put(ObjectElement.OPERATION_KEY, mod.toString());
		paramList.put("AID", aid);
		// try
		// {
		ArrayList<Map<String, String>> temp = new ArrayList<Map<String, String>>();
		temp.add(new HashMap<String, String>());
		List<Map<String, String>> result = temp;
		if (oper != CHANGE.DELETE) {
			result = ((NetworkElement) ne).getTl1Session().sendToNE(paramList);
		}
		if (result != null) {
			Map<String, String> aResult = result.get(0);
			// the following call will update the database.
			if (updateFacilityAttribute(ne, aResult, aid, oper)) {
				// return the data for later use (i.e. sending an event)
				return aResult;
			}
		}
		else {
			log.error("Got empty result from querying new state of " + aid
			    + " from NE");
		}
		// }
		// catch (Exception e)
		// {
		// }
		return null;
	}

	private boolean handleBlsrChangeEvent(AbstractNe ne,
	    Map<String, String> values, CHANGE oper) {
		try {
			String workingAid = values.get("AID1");
			String protectionAid = values.get("AID2");
			NetworkElement omeNe = (NetworkElement) ne;

			// Set up the data for event
			HashMap<String, String> eventData = new HashMap<String, String>();
			eventData.put("workingAid", workingAid);
			eventData.put("proctectingAid", protectionAid);
			eventData.put("operation", oper.asString());
			dbchgEvent.addDataElement(eventData);
			getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
			// String command = values.get("COMMAND");
			if (oper == CHANGE.DELETE) {
				// don't handle any thing, for now
				log.debug("Got the DLT-BLSRMAP report for " + workingAid + ":"
				    + protectionAid);

				DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, workingAid,
				    AbstractFacilityXml.APSID_ATTR, "N/A");
				DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, protectionAid,
				    AbstractFacilityXml.APSID_ATTR, "N/A");
				omeNe.getRingName().remove(workingAid);
				omeNe.getRingName().remove(protectionAid);
				omeNe.setApsId(null);
				eventData.put(AbstractFacilityXml.APSID_ATTR, "N/A");
				return true;
			}

			log.debug("Got the ENT-BLSRMAP report for " + workingAid + ":"
			    + protectionAid);
			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY,
			    Tl1CommandCode.RTRV_BLSRMAP.toString());
			paramList.put("WRKGAID", workingAid);
			paramList.put("PROTAID", protectionAid);

			List<Map<String, String>> result = omeNe.getTl1Session().sendToNE(
			    paramList);
			if (result != null) {
				Iterator<Map<String, String>> ir = result.iterator();
				while (ir.hasNext()) {
					Map<String, String> blsrResult = ir.next();
					String ringName = blsrResult.get("LABEL");
					omeNe.getRingName().put(workingAid, ringName);
					omeNe.getRingName().put(protectionAid, ringName);

					// ringMap is expected to have the following format
					// "3:3/0:0/1:1/2:2/3:3"
					try {
						String ringMap = blsrResult.get("RINGMAP");
						log.debug("got ringMap info for " + ringName + " in " + ringMap);

						String apsIdStr = ringMap.split("/")[0].split(":")[0];
						omeNe.setApsId(new Integer(apsIdStr));

						// update the facility in the database with the apsId.
						DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, workingAid,
						    AbstractFacilityXml.APSID_ATTR, apsIdStr);
						DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, protectionAid,
						    AbstractFacilityXml.APSID_ATTR, apsIdStr);
						eventData.put(AbstractFacilityXml.APSID_ATTR, apsIdStr);
					}
					catch (Exception ex) {
						log.error("Fail to obtain the ring information", ex);
					}
				}
				return true;
			}

		}
		catch (Exception e) {
			log.error("Failed to handle BLSRMAP event", e);
		}
		return false;
	}

    /**
     * Retrieve facility from database matching the provided NE-Shelf-Slot-Port.
     *
     * Below are three examples of the facility XML record from the database.
     * The first is a layer 1 facility of a 10G interface card.  The second is
     * a layer 2 facility of a ETH10G card.  The third is a WAN adaptation
     * facility for encapsulating a layer 2 service in a layer 1 service.
     *
     * <WP CardPEC="NTK526LN" CardType="10G" aid="OC192-1-3-1" apsId="N/A" constrain="0" cost="1" domain="N/A" group="none" id="1" layer="layer1" manualProvision="false" metric="1" neidForFac="00-20-D8-DF-33-59" neipForFac="145.145.67.67" neportForFac="10001" operation="delete" pk="00-20-D8-DF-33-59_OC192-1-3-1" port="1" primaryState="OOS-AUMA" ps="N/A" shelf="1" signalingType="unassigned" siteId="N/A" slot="3" srlg="N/A" tna="N/A" type="OC192" userLabel="N/A" valid="false" />
     *
     * <WP AID="ETH10G-1-5-1" CardPEC="NTK533AA" CardType="XGE" PortPEC="NTTP80BA" PortType="P10GSEL" TYPE="ETH10G" actualUnit="0" advertisedDuplex="UNKNOWN" aid="ETH10G-1-5-1" anspeed="UNKNOWN" apsId="N/A" autoNegotiation="DISABLE" autoNegotiationStatus="DISABLED" constrain="0" controlPauseRx="DISABLE" controlPauseTx="ENABLE" cost="1" domain="N/A" etherDuplex="FULL" flowControl="ASYM" group="none" id="2" isEPL="true" layer="layer2" lcas="DISABLE" manualProvision="false" mapping="GFP-F" metric="1" mtu="9018" neidForFac="00-20-D8-DF-33-86" neipForFac="145.145.67.68" neportForFac="10001" operation="modify" passControlFrame="DISABLE" physicalAddress="0016CA4068D6" pk="00-20-D8-DF-33-86_ETH10G-1-5-1" port="1" primaryState="IS" provUnit="0" ps="N/A" rate="NONE" shelf="1" signalingType="UNI" siteId="Asd001A" slot="5" slrg="N/A" speed="10000" tna="Asd001A_OME4T_ETH10G-1-5-1" txConditioning="ENABLE" type="ETH10G" userLabel="10G test port OME4T" valid="false" vcat="ENABLE" />
     *
     * <WP actualUnit="0" aid="WAN-1-3-1" apsId="N/A" constrain="0" cost="1" domain="N/A" group="none" id="2" layer="layer2" lcas="DISABLE" manualProvision="false" mapping="GFP-F" metric="1" mode="SDH" neidForFac="00-20-D8-DF-33-59" neipForFac="145.145.67.67" neportForFac="10001" operation="delete" pk="00-20-D8-DF-33-59_WAN-1-3-1" port="1" primaryState="OOS-MA" provRxUnit="0" provUnit="0" ps="N/A" rate="NONE" shelf="1" signalingType="unassigned" siteId="N/A" slot="3" srlg="N/A" tna="N/A" type="WAN" userLabel="N/A" valid="false" vcat="DISABLE" />
     *
     * @param ne The network element.
     * @param shelf The self number.
     * @param slot The slot number.
     * @param port The port number.
     * @return Facility matching the NE-Shelf-Slot-Port database entry.
     */
    private String getFacility(AbstractNe ne, String shelf, String slot, String port) {

        /* Get the matching source facility identifier. */
        List<String> facilityRecords;
        try {
            facilityRecords = DbUtility.INSTANCE.retrieveFacilities(ne, shelf, slot, port);
        } catch (Exception ex) {
            log.error("getFacility: retrieveFacilities(" + ne.getNeName() + "," + shelf + "," + slot + "," + port + ")", ex);
            return null;
        }

        /*
         * Find the first non-WAN facility. If there is only WAN then we
         * will process it although we should never have a connection in
         * the DB on an WAN port since we store based on ETH.
         */
        String facilityRecord = null;
        Iterator it = facilityRecords.iterator();
        while (it.hasNext()) {
            facilityRecord = (String) it.next();
            String aFacility = getFacilityType(facilityRecord);
            if(!aFacility.startsWith("WAN")) {
                break;
            }
        }

        return facilityRecord;
    }

    /**
     * Get the facility type from the AID within the facility record.  We are
     * looking for facility type in the "type" attribute.
     *
     * @param facilityRecord The XML formatted facility string.
     * @return Facility type string.
     */
    private String getFacilityType(String facilityRecord) {
        int startTypeIndex = facilityRecord.indexOf("type=") + 6;
        int endTypeIndex = facilityRecord.indexOf('"', startTypeIndex);
        String facilityType = facilityRecord.substring(startTypeIndex, endTypeIndex);
        return facilityType;
    }

    /**
     * Return the facility AID associated with the facility record.
     *
     * @param facilityRecord The XML formatted facility string.
     * @return Facility AID.
     */
    private String getFacilityAID(String facilityRecord) {
        /* Parse out the facility type. */
        int startTypeIndex = facilityRecord.indexOf("aid=") + 5;
        int endTypeIndex = facilityRecord.indexOf('"', startTypeIndex);
        String facilityAID = facilityRecord.substring(startTypeIndex, endTypeIndex);
        return facilityAID;
    }

    /**
     * Handle incoming connection REPT DBCHG event from the network element.
     *
     * @param ne network element that generated the event.
     * @param values key/value parameters associated with the connection event.
     * @param dbchgEvent the TL1 event in XML format.
     * @return
     */
	private boolean handleConnectionEvent(AbstractNe ne,
            Map<String, String> values, Tl1XmlDbChangeEvent dbchgEvent) {

        log.error("handleConnectionEvent: This is a generic OME handler that should not be invoked by new OME releases.", new Exception());

        /* Temp storage for the data we need to update in the database. */
		Map<String, String> data = new HashMap<String, String>();

        /* Pull out the field from the event we care about. */
		String neId = ne.getNeId();             // IEEE system ID of NE.
		String command = values.get("COMMAND"); // Command that generate event.
		String cktid = values.get("CKTID");     // Name of Xconnect.
		String fromAid = values.get("AID1");    // Example - STS3C-1-1-1-1
		String toAid = values.get("AID2");      // Example - STS3C-1-12-1-1
		String swmateAid = null;

        /* Notice that, in deleting case, the CKTID is null, so we will
         * need to look up the associated facility.
         *
         * The AID coming from OME doesn't contain the type of facility (ie.
         * STS1-1-4-1-2) so we take the timeslot shelf-slot-port qualifier
         * and do a dayabase lookup for the sourcing facility.
         *
         * We may get multiple matching facilities for the case of ETH
         * and WAN which share the same shelf, slot, port identifiers.  We
         * are only interested in the ETH facility for connection DB
         * operations.
         */

		try {
			/* Notify the internal utility that keeps track of all Xconnections
			 * being created, not deleted.
             */
			if (command.startsWith("ENT")) {
				CheckAllComplete.INSTANCE.iAmDone(cktid);
			}

			/* Parse the source AID for the cross connect - notice we drop the
             * rate prefix (AID type) and the subport (i.e. timeslot) component.
             * We will use the shelf, slot, and port to look up the associated
             * facility identifier.
             */
			String[] fromAidArr = fromAid.split("-");
			String fromShelf = fromAidArr[1];
			String fromSlot = fromAidArr[2];
			String fromPort = fromAidArr[3];

            /* Get the matching source facility identifier. */
			String fromFacilityRecord = getFacility(ne, fromShelf, fromSlot, fromPort);

            if (fromFacilityRecord == null || fromFacilityRecord.isEmpty()) {
                log.error("No matching fromFacilityRecord for connection db change event from ne:"
                        + ne.getNeId() + " values:" + values);
                return false;
            }

            /* Parse out the facility AID. */
            fromAid = getFacilityAID(fromFacilityRecord);

			/* Rate of the cross connect. */
			getParameters().put("replaceRate", fromAidArr[0]);
			data.put(DbKeys.NetworkElementConnectionCols.RATE, fromAidArr[0]);

			/* Parse the destination AID for the cross connect - notice we drop
             * the rate prefix (AID type) and the subport (i.e. timeslot)
             * component. We will use the shelf, slot, and port to look up the
             * facility identifier.
             */
			String[] toAidArr = toAid.split("-");
			String toShelf = toAidArr[1];
			String toSlot = toAidArr[2];
			String toPort = toAidArr[3];

            /* Get the matching destination facility identifier. */
			String toFacilityRecord = getFacility(ne, toShelf, toSlot, toPort);

            /* If a matching facility does not exist then we can stop processing. */
            if (toFacilityRecord == null || toFacilityRecord.isEmpty()) {
                log.error("No matching toFacilityRecord for connection db change event from ne:"
                        + ne.getNeId() + " values:" + values);
                return false;
            }

            /* Get the facility AID. */
            toAid = getFacilityAID(toFacilityRecord);

			/* We have mapped the crossconnect timeslots to physical port
             * facilities so now we can check to see if there is a matching
             * crossconnect in our connection database.  The connections are
             * stored by forming a key composed of the NE IEEE system ID and
             * the two ports involved in the connection:
             *
             *         00-21-E1-D6-D5-DC_OC12-1-12-1-3_OC192-1-9-1-191
             *
             * We may need to query twice depending on the order of the ports
             * in the REPT DBCHG event versus how the database key was composed.
             */
            CrossConnection existConnection = DbUtility.INSTANCE.retrieveAnXConnect(ne, fromAid, toAid);

            /* Fail if we have a connection and are notified of a new one using same resources. */
			if (existConnection != null) {
				if (command.startsWith("ENT")) {
					log.error(ne.getNeName() + ": connection " + fromAid + ":" + toAid + " already exists");
					return false;
				}
			}

            /*
             * If there is no cross connect (existConnection == null) then
             * discard any event except creation of a new one.
             */
			else if (!command.startsWith("ENT")) {
				log.debug(ne.getNeName() + ": connection " + fromAid + ":" + toAid + " doesn't exist (" + command + ").");
				log.debug("Querying the switched values: " + toAid + " to" + fromAid);

                existConnection = DbUtility.INSTANCE.retrieveAnXConnect(ne, toAid, fromAid);

				if (existConnection == null) {
					log.error(ne.getNeName() + ": connection " + toAid + ":" + fromAid + " doesn't exist (" + command + ").");
					return false;
				}

                /* Swap the AID to match stored cross connect since it is reversed. */
				String temp = fromAid;
				fromAid = toAid;
				toAid = temp;
			}

            /* Connection identifier. */
			if (cktid == null) {
				cktid = "N/A";
			}

			/* Handle the connection switch mate (SWMATE) if this is a
             * protected connection.
             */
			String swMate = null;
			Object tempSwmate = values.get("KEYWORDPARAMS");
			if (tempSwmate != null) {
				String[] keyWords = ((String) tempSwmate).split(",");
				for (String keyWord : keyWords) {
					if (keyWord.indexOf("SWMATE") >= 0) {
						swMate = keyWord.split("=")[1];
						break;
					}
				}
			}

            /* Split the switch mate AID into shelf, slot, and port. */
			String[] swmateAidArr = null;
			String swmateFacilityRecord = null;
			String swmateShelf = null;
			String swmateSlot = null;
			String swmatePort = null;
			String swmateFacilityType = null;

			if (swMate != null && swMate.length() > 0) {
				swmateAidArr = swMate.split("-");
				swmateShelf = swmateAidArr[1];
				swmateSlot = swmateAidArr[2];
				swmatePort = swmateAidArr[3];

                /* Retrieve the associated facility record for switch mate. */
				swmateFacilityRecord = DbUtility.INSTANCE.retrieveAFacility(ne,
				    swmateShelf, swmateSlot, swmatePort);

                /* Get the facility AID associated with record. */
                swmateAid = getFacilityAID(swmateFacilityRecord);
			}

			log.debug("Handling DRAC's connection event CKTID: " + cktid);

			getParameters().put("replaceSourceAddress", neId);
			getParameters().put("replaceTargetAddress", neId);
			getParameters().put("replaceId", cktid);
			getParameters().put("replaceType", values.get("POS1"));
			getParameters().put("replaceCommitted", "true");

			data.put(DbKeys.NetworkElementConnectionCols.ID, cktid);

			data.put(DbKeys.NetworkElementConnectionCols.SOURCEAID, fromAid);
			getParameters().put("replaceSourceAid", fromAid);

			data.put(DbKeys.NetworkElementConnectionCols.TARGETAID, toAid);
			getParameters().put("replaceTargetAid", toAid);

			data.put(DbKeys.NetworkElementConnectionCols.SWMATEAID, swmateAid);
			getParameters().put("replaceSwmate", swmateAid);

			/* Update the database and re-use the "parameters". */
			if (command.startsWith("ENT")) {
				data.put(NetworkElementEventXML.OPERATION_ATTR,
				    OperationType.OPERATION_ADD.toString());
				DbUtility.INSTANCE.addOneXConnect(ne, getParameters());
			}
			else {
				data.put(NetworkElementEventXML.OPERATION_ATTR,
				    OperationType.OPERATION_DELETE.toString());
                data.put(DbKeys.NetworkElementConnectionCols.ID, existConnection.getId());

				DbUtility.INSTANCE.deleteXConnect(ne, fromAid, toAid);
			}

			dbchgEvent.addDataElement(data);

			/* *** FOR an Eth/Wan EPL port ***
			 *
			 * The act of cross-connecting can change some of the WAN
			 * attributes, such as provUnits, state, and most importantly, the
             * RATE (e.g. for WAN port pairs, connecting to one port at STS1
             * requires that the other take only STS1 ... same for STS3C).
			 *
			 * So, with the connection provisioned, check for db changes on the
             * WAN (...the NE doesn't emit a dbchange report?!)
             *
             * Ignoring L2SS-based WAN facilities for state changes.
             */

			Element fromElement = DbOpsHelper.xmlToElement(fromFacilityRecord);
			String eplFlag = fromElement.getAttributeValue(FacilityConstants.IS_EPL);
			if (eplFlag != null && Boolean.parseBoolean(eplFlag)) {
				/* The found fromFacilityRecord is an EthXXX/Wan EPL port */
				checkForEplWanDbChange(ne, Tl1CommandCode.RTRV_WAN,
				    FacilityConstants.WAN, getFacilityType(fromFacilityRecord),
                    fromShelf, fromSlot, fromPort);
			}

			Element toElement = DbOpsHelper.xmlToElement(toFacilityRecord);
			eplFlag = toElement.getAttributeValue(FacilityConstants.IS_EPL);
			if (eplFlag != null && Boolean.parseBoolean(eplFlag)) {
				// The found toFacilityRecord is an EthXXX/Wan EPL port
				checkForEplWanDbChange(ne, Tl1CommandCode.RTRV_WAN,
				    FacilityConstants.WAN, getFacilityType(toFacilityRecord),
                    toShelf, toSlot, toPort);
			}

			if (swmateFacilityRecord != null) {
				Element swmateElement = DbOpsHelper.xmlToElement(swmateFacilityRecord);
				eplFlag = swmateElement.getAttributeValue(FacilityConstants.IS_EPL);
				if (eplFlag != null && Boolean.parseBoolean(eplFlag)) {
					// The found swmateFacilityRecord is an EthXXX/Wan EPL port
					checkForEplWanDbChange(ne, Tl1CommandCode.RTRV_WAN,
					    FacilityConstants.WAN, swmateFacilityType, swmateShelf,
					    swmateSlot, swmatePort);
				}
			}
		}
		catch (Exception e) {
			log.error("Failed to handle connection db change event from ne:" + ne.getNeId()
			        + " values:" + values, e);
			return false;
		}

		getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);

		/*
         * LPCP_PORT connection event handling only considers OSS connections
         * (i.e. connections whose IDs don't begin with 'DRAC-'). Events for
         * DRAC-initiated connections are handled during service provisioning.
         */
		if (CrossConnection.isDracConnection(cktid)) {
			return false;
		}

		return true;
	}

	private void checkForEplWanDbChange(AbstractNe ne, Tl1CommandCode mod,
	    String wanType, String ethType, String shelf, String slot, String port) {
		// ethType can vary: ETH, ETH10G, ETH100, etc.

		String wanAid = wanType + "-" + shelf + "-" + slot + "-" + port;
		String ethAid = ethType + "-" + shelf + "-" + slot + "-" + port;

		log.debug("Checking for change in DB due to Connection event: aid: "
		    + wanAid);

		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put(ObjectElement.OPERATION_KEY, mod.toString());
		paramList.put("AID", wanAid);

		try {
			ArrayList<Map<String, String>> temp = new ArrayList<Map<String, String>>();
			temp.add(new HashMap<String, String>());
			List<Map<String, String>> wanResult = ((NetworkElement) ne)
			    .getTl1Session().sendToNE(paramList);

			if (wanResult != null) {
				Map<String, String> aWanResult = wanResult.get(0);

				String ethEplFac = DbUtility.INSTANCE.retrieveAFacility(ne, shelf,
				    slot, port);
				if (ethEplFac == null) {

					return;
				}
				com.nortel.appcore.app.drac.server.neproxy.command.ome8.OmeFacilityXML eplFacXML = new com.nortel.appcore.app.drac.server.neproxy.command.ome8.OmeFacilityXML(ethEplFac);

				// Build the list of EPL WAN->ETH*** XML attributes based on the NE
				// key/value.
				Map<String, String> currentNeAttrValue = new HashMap<String, String>();
				Iterator<String> keyIr = OmeFacilityXML.wanToEthEPLNeToXmlMapping
				    .keySet().iterator();
				while (keyIr.hasNext()) {
					String neKey = keyIr.next();
					String xmlKey = OmeFacilityXML.wanToEthEPLNeToXmlMapping.get(neKey);
					String aValue = aWanResult.get(neKey);
					if (aValue != null) {
						currentNeAttrValue.put(xmlKey, aValue);
					}
				}

				Map<String, String> updateList = eplFacXML.updateFacilityAttr(ethAid,
				    currentNeAttrValue);

				if (updateList != null) {
					// TODO: The WAN event from here up to LPCP_PORT should be screened
					// first...
					// to evaluate whether or not the updated attributes are of interest
					// to LPCP_PORT. WAN attributes of interest currently are:
					// - VCAT
					// - Basic Rate (?)

					log.debug("Facility's attr to update in checkForDbChange:"
					    + updateList);
					DbUtility.INSTANCE.updateAFacility(ne, updateList, ethAid);
					updateList.put("operation", "modify");
					updateList.put("aid", ethAid);

					Tl1XmlDbChangeEvent additionalEvent = new Tl1XmlDbChangeEvent(ne);
					additionalEvent.setOccurrentDate(dbchgEvent.getOccurrentDate());
					additionalEvent.setOccurrentTime(dbchgEvent.getOccurrentTime());
					additionalEvent.setEventId(ne.getNeId() + "_"
					    + System.currentTimeMillis());
					additionalEvent.setOwnerId(ne.getTerminalId());
					additionalEvent.setNotificationType("facility");
					additionalEvent.updateDescription("PROXY_GEN");
					additionalEvent.addDataElement(updateList);
					getParameters().put(AbstractCommandlet.ADDITIONALEVENT_KEY,
					    additionalEvent);
				}
				else {

				}
			}
			else {
				log.error("Got empty result from querying new state of " + wanAid
				    + " from NE");
			}
		}
		catch (Exception e) {
			log.error("Failed to perform " + mod + " from " + ne.getNeName(), e);
		}
	}

	private boolean handleProtectionChangeEvent(AbstractNe ne,
	    Map<String, String> values, CHANGE oper) {
		try {
			String workingAid = values.get("AID1");
			String protectionAid = values.get("AID2");
			NetworkElement omeNe = (NetworkElement) ne;

			// Set up the data for event
			HashMap<String, String> eventData = new HashMap<String, String>();
			eventData.put("workingAid", workingAid);
			eventData.put("proctectingAid", protectionAid);
			eventData.put("operation", oper.asString());
			dbchgEvent.addDataElement(eventData);
			getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
			// String command = values.get("COMMAND");
			if (oper == CHANGE.DELETE) {
				// don't handle any thing, for now
				log.debug("Got the DLT-FFP report for " + workingAid + ":"
				    + protectionAid);

				DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, workingAid,
				    AbstractFacilityXml.PROTECTIONSCHEME_ATTR, "N/A");
				DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, protectionAid,
				    AbstractFacilityXml.PROTECTIONSCHEME_ATTR, "N/A");
				omeNe.getProtectionAid().remove(workingAid);
				omeNe.getProtectionAid().remove(protectionAid);
				eventData.put(AbstractFacilityXml.PROTECTIONSCHEME_ATTR, "N/A");
				return true;
			}
			log.debug("Got the ENT-FFP report for " + workingAid + ":" + protectionAid);
			String[] aidMap = workingAid.split("-");
			Map<String, String> paramList = new HashMap<String, String>();
			paramList.put(ObjectElement.OPERATION_KEY, "RTRV-FFP-" + aidMap[0]);
			paramList.put("WRKGAID", aidMap[0] + "-1-ALL");

			List<Map<String, String>> result = omeNe.getTl1Session().sendToNE(
			    paramList);
			if (result != null) {
				Iterator<Map<String, String>> ir = result.iterator();
				while (ir.hasNext()) {
					Map<String, String> aResult = ir.next();
					String protectionScheme = aResult.get("PS");
					omeNe.getProtectionAid().add(workingAid);
					omeNe.getProtectionAid().add(protectionAid);

					DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, workingAid,
					    AbstractFacilityXml.PROTECTIONSCHEME_ATTR, protectionScheme);
					DbUtility.INSTANCE.updateOrAddFacilityAttr(ne, protectionAid,
					    AbstractFacilityXml.PROTECTIONSCHEME_ATTR, protectionScheme);
					eventData.put(AbstractFacilityXml.PROTECTIONSCHEME_ATTR,
					    protectionScheme);
				}
				return true;
			}
		}
		catch (Exception e) {
			log.error("Failed to handle FFP event", e);
		}
		return false;
	}

	private boolean handleStateChangeEvent(AbstractNe ne,
	    Map<String, String> values, CHANGE oper) throws Exception {
		String fromAid = values.get("AID1");
		String[] aidMap = fromAid.split("-");

		// Both equipment (STCHG-EQPT), and facility (STCHG-WAN, STCHG-OC192, ...)
		// state change reports are handle here. But we need to skip events that are
		// from the unsupported card like L2SS

		// Get the equipment type on this particular slot, assuming that it's a L2SS
		// if it's not, then continue
		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_EQPT.toString());
		paramList.put("AID", "L2SS-1-" + aidMap[2]);

		try {
			ArrayList<Map<String, String>> temp = new ArrayList<Map<String, String>>();
			temp.add(new HashMap<String, String>());
			List<Map<String, String>> result = ((NetworkElement) ne).getTl1Session()
			    .sendToNE(paramList);
			if (result != null) {
				log.debug("Skip event for this unsupported equipment: " + "L2SS-1-"
				    + aidMap[2]);
				return false;
			}
		}
		catch (Exception e) {
			log.debug("Slot " + aidMap[2]
			    + " isn;t a L2SS, go on with STCHG-EQPT event");
		}

		String command = values.get("COMMAND");
		if (command.indexOf("STCHG-EQPT") >= 0) {
			// don't handle any thing, for now
			log.debug("Got the STCHG-EQPT report for " + fromAid);
		}
		else {
			log.debug("Handle state change " + command + " for " + fromAid);
			Map<String, String> aResult = getAndUpdateFacility(ne,
			    Tl1CommandCode.fromString("RTRV_" + aidMap[0]), fromAid, oper);
			if (aResult != null) {
				getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
				return true;
			}
		}

		return false;
	}

	@Deprecated
	private boolean updateFacilityAttribute(AbstractNe ne,
	    Map<String, String> aResult, String aid, CHANGE oper) throws Exception {

		// TODO: Old OME package. Removing support for paired facilities to permit
		// code cleanup above.
		return false;

		// Map<String, String> facilityMap;
		// String pairingAid = null;
		// int layer = 1;
		// String layerStr = AbstractFacilityXML.LAYER1_ELEMENT;
		// String pairingLayer = "layer1/layer2";
		// String alterPairingLayer = "layer2";
		// Element newFacElem = new Element(AbstractFacilityXML.LAYER1_ELEMENT);
		// // The WAN and ETH facility needs more attentions
		// if (aid.indexOf("WAN") >= 0)
		// {
		// facilityMap = OmeFacilityXML.wanNeToXmlMapping;
		// pairingAid = aid.replace("WAN", "ETH");
		//
		// // for event
		// }
		// else if (aid.indexOf("ETH") >= 0)
		// {
		// facilityMap = OmeFacilityXML.ethNeToXmlMapping;
		// pairingAid = aid.replace("ETH", "WAN");
		// layer = 2;
		// layerStr = "layer1/layer2";
		// pairingLayer = AbstractFacilityXML.LAYER1_ELEMENT;
		// newFacElem.setName("layer2");
		// }
		// else
		// {
		// facilityMap = OmeFacilityXML.commonNeToXmlMapping;
		// }
		//
		// // Build the list of XML attributes based on the NE key/value.
		// Map<String, String> currentNeAttrValue = new HashMap<String, String>();
		// Iterator<String> keyIr = facilityMap.keySet().iterator();
		// while (keyIr.hasNext())
		// {
		// String neKey = keyIr.next();
		// String xmlKey = facilityMap.get(neKey);
		// String aValue = aResult.get(neKey);
		// if (aValue != null)
		// {
		// currentNeAttrValue.put(xmlKey, aValue);
		// newFacElem.setAttribute(xmlKey, aValue);
		// }
		// }
		//
		// String changedFac = DbUtility.INSTANCE.retrieveAFacility(ne, aid,
		// layerStr);
		// XMLOutputter outXml = new XMLOutputter(Format.getCompactFormat());
		// if (changedFac == null)
		// {
		// // Create new entry and setup the data for the event as well
		// HashMap<String, String> eventData = new HashMap<String, String>();
		// eventData.put(AbstractFacilityXML.AID_ATTR, aid);
		// eventData.put("operation", CHANGE.ADD.asString());
		// eventData.put(AbstractFacilityXML.ID_ATTR, Integer.toString(layer));
		//
		// newFacElem.setAttribute(AbstractFacilityXML.VALID_ATTR, "true");
		// newFacElem.setAttribute(AbstractFacilityXML.ID_ATTR,
		// Integer.toString(layer));
		// newFacElem.setAttribute(AbstractFacilityXML.AID_ATTR, aid);
		//
		// List<Attribute> staticAttr = OmeFacilityXML.getStaticAttributes();
		// for (int i = 0; i < staticAttr.size(); i++)
		// {
		// Attribute anAttr = staticAttr.get(i);
		// if (newFacElem.getAttributeValue(anAttr.getName()) == null)
		// {
		// newFacElem.setAttribute(anAttr.getName(), anAttr.getValue());
		// }
		// eventData.put(anAttr.getName(), anAttr.getValue());
		// }
		// String[] aidMap = aid.split("-");
		// eventData.put(AbstractFacilityXML.PORT_ATTR, aidMap[3]);
		// eventData.put(AbstractFacilityXML.SHELF_ATTR, aidMap[1]);
		// eventData.put(AbstractFacilityXML.SLOT_ATTR, aidMap[2]);
		// eventData.put(AbstractFacilityXML.TYPE_ATTR, aidMap[0]);
		// dbchgEvent.addDataElement(eventData);
		//
		// newFacElem.setAttribute(AbstractFacilityXML.PORT_ATTR, aidMap[3]);
		// newFacElem.setAttribute(AbstractFacilityXML.SHELF_ATTR, aidMap[1]);
		// newFacElem.setAttribute(AbstractFacilityXML.SLOT_ATTR, aidMap[2]);
		// newFacElem.setAttribute(AbstractFacilityXML.TYPE_ATTR, aidMap[0]);
		//
		// String newFacStr = outXml.outputString(newFacElem);
		//
		// if (pairingAid != null)
		// {
		// String pairingFac = DbUtility.INSTANCE.retrieveAFacility(ne,
		// pairingAid, pairingLayer);
		// // try one more time, in case the ETH was created before WAN
		// if (pairingFac == null)
		// {
		// pairingLayer = alterPairingLayer;
		// pairingFac = DbUtility.INSTANCE.retrieveAFacility(ne, pairingAid,
		// pairingLayer);
		// }
		//
		// if (pairingFac == null)
		// {
		// DbUtility.INSTANCE.addNewFacility(ne, newFacStr);
		// }
		// else
		// {
		// ByteArrayInputStream data = new
		// ByteArrayInputStream(pairingFac.getBytes());
		// SAXBuilder builder = new SAXBuilder();
		// Document pairingFacElem = builder.build(data);
		// switch (layer)
		// {
		// case 1:
		// newFacElem.addContent(pairingFacElem.removeContent(0));
		// newFacStr = outXml.outputString(newFacElem);
		// DbUtility.INSTANCE.replaceFacility(ne, pairingAid, newFacStr,
		// pairingLayer);
		// break;
		// case 2:
		// pairingFacElem.addContent(newFacElem);
		// newFacStr = outXml.outputString(pairingFacElem);
		// DbUtility.INSTANCE.replaceFacility(ne, pairingAid, newFacStr,
		// pairingLayer);
		// break;
		// }
		// }
		// }
		// else
		// {
		// if (oper == CHANGE.ADD)
		// {
		// DbUtility.INSTANCE.addNewFacility(ne, newFacStr);
		// }
		// }
		// }
		// else
		// {
		// OmeFacilityXML changedFacXML = new OmeFacilityXML(changedFac);
		// Map<String, String> updateList = changedFacXML.updateFacilityAttr(aid,
		// currentNeAttrValue);
		// if (updateList != null || oper != CHANGE.MODIFY)
		// {
		// // If there are no changes and the operation is ADD or DELETE, then
		// // we still want to update the Valid field accordingly
		// if (updateList == null)
		// {
		// updateList = new HashMap<String, String>();
		// }
		// if (oper == CHANGE.ADD)
		// {
		// updateList.put(AbstractFacilityXML.VALID_ATTR, "true");
		// }
		// else if (oper == CHANGE.DELETE)
		// {
		// updateList.put(AbstractFacilityXML.VALID_ATTR, "false");
		// }
		//
		// updateList.put(AbstractFacilityXML.AID_ATTR, aid);
		// updateList.put("operation", oper.asString());
		//
		// dbchgEvent.addDataElement(updateList);
		//
		// DbUtility.INSTANCE.updateAFacility(ne, updateList, aid, layerStr);
		// }
		// else
		// {
		//
		// return false;
		// }
		// }
		// return true;

	}
}
