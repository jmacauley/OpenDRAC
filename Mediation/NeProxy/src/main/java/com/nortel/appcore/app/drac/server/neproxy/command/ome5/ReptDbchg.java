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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
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

/**
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

			if (command.indexOf("-CRS-") >= 0) {
				dbchgEvent.setReportType("connection");
				result = handleConnectionEvent(ne, values, dbchgEvent);
			}
			else if (command.indexOf("-OC") >= 0 || command.indexOf("-WAN") >= 0
			    || command.indexOf("-ETH") >= 0) {
				dbchgEvent.setReportType("facility");
				result = handleStateChangeEvent(ne, values, operation);
			}
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

	private boolean handleConnectionEvent(AbstractNe ne,
	    Map<String, String> values, Tl1XmlDbChangeEvent dbchgEvent) {
		try {
			// Notice that, in deleting case, the CKTID is null, so we have
			// to compare the AIDs in the database.
			// The AID coming from OME doesn't contain the type of facility (ie.
			// STS1-1-4-1-2) so extra database querying has to be done.
			Map<String, String> data = new HashMap<String, String>();

			String command = values.get("COMMAND");
			String cktid = values.get("CKTID");
			String neId = ne.getNeId();
			// String neIp = ne.getIpAddress();

			String fromAid = values.get("AID1");
			String toAid = values.get("AID2");

			String[] aidMap = fromAid.split("-");
			// GGL2
			boolean fromSideIsL2SS = ne.slotIsL2SS(aidMap[1], aidMap[2]);

			// Notify the internal utility that keeps track of all Xconnections
			// being created, not deleted
			if (command.startsWith("ENT")) {
				CheckAllComplete.INSTANCE.iAmDone(cktid);
			}

			String facilityRecord = DbUtility.INSTANCE.retrieveAFacility(ne,
			    aidMap[1], aidMap[2], aidMap[3]);
			int startTypeIndex = facilityRecord.indexOf("type=") + 6;
			int endTypeIndex = facilityRecord.indexOf('"', startTypeIndex);
			String aFacility = facilityRecord.substring(startTypeIndex, endTypeIndex);
			getParameters().put("replaceRate", aidMap[0]);
			data.put(AbstractFacilityXml.RATE_ATTR, aidMap[0]);
			data.put("sShelf", aidMap[1]);
			data.put("sSlot", aidMap[2]);
			data.put("sPort", aidMap[3]);
			data.put("sChannel", aidMap[4]);

			StringBuffer tempBuff = new StringBuffer(aFacility);
			for (int i = 1; i < aidMap.length; i++) {
				tempBuff.append("-" + aidMap[i]);
			}
			fromAid = tempBuff.toString();

			aidMap = toAid.split("-");
			// GGL2
			boolean toSideIsL2SS = ne.slotIsL2SS(aidMap[1], aidMap[2]);

			facilityRecord = DbUtility.INSTANCE.retrieveAFacility(ne, aidMap[1],
			    aidMap[2], aidMap[3]);
			startTypeIndex = facilityRecord.indexOf("type=") + 6;
			endTypeIndex = facilityRecord.indexOf('"', startTypeIndex);
			aFacility = facilityRecord.substring(startTypeIndex, endTypeIndex);
			tempBuff = new StringBuffer(aFacility);
			for (int i = 1; i < aidMap.length; i++) {
				tempBuff.append("-" + aidMap[i]);
			}
			toAid = tempBuff.toString();

			data.put("tShelf", aidMap[1]);
			data.put("tSlot", aidMap[2]);
			data.put("tPort", aidMap[3]);
			data.put("tChannel", aidMap[4]);

			String existConnection = DbUtility.INSTANCE.retrieveAXConnect(ne,
			    fromAid, toAid);
			if (existConnection != null) {
				if (command.startsWith("ENT")) {
					log.error(ne.getNeName() + ": connection " + fromAid + ":" + toAid
					    + " already exists");
					return false;
				}
			}
			else if (!command.startsWith("ENT")) {
				log.error(ne.getNeName() + ": connection " + fromAid + ":" + toAid
				    + " doesn't exist");
				log.debug("Trying the switched values: " + toAid + " to" + fromAid);
				existConnection = DbUtility.INSTANCE.retrieveAXConnect(ne, toAid,
				    fromAid);
				if (existConnection == null) {
					log.error(ne.getNeName() + ": connection " + toAid + ":" + fromAid
					    + " doesn't exist");
					return false;
				}
				String temp = fromAid;
				fromAid = toAid;
				toAid = temp;
			}

			if (cktid == null) {
				cktid = "N/A";
			}

			// Find the SWMATE key in the key word parameters
			String swMate = "";
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

			log.debug("Handling DRAC's connection event CKTID: " + cktid);
			getParameters().put("replaceSourceAddress", neId);
			getParameters().put("replaceTargetAddress", neId);
			getParameters().put("replaceId", cktid);
			getParameters().put("replaceType", values.get("POS1"));
			getParameters().put("replaceCommitted", "true");
			getParameters().put("replaceSwmate", swMate);

			data.put("swmate", swMate);
			data.put("cktid", cktid);
			data.put("source", fromAid);
			getParameters().put("replaceSourceAid", fromAid);

			data.put("target", toAid);
			getParameters().put("replaceTargetAid", toAid);

			// Update the database and re-use the "parameters"
			if (command.startsWith("ENT")) {
				data.put("operation", "add");
				DbUtility.INSTANCE.addOneXConnect(ne, getParameters());
			}
			else {
				data.put("operation", "delete");

				ByteArrayInputStream aByteStream = new ByteArrayInputStream(
				    existConnection.getBytes());
				SAXBuilder builder = new SAXBuilder();
				Document aDoc = builder.build(aByteStream);
				Element root = aDoc.getRootElement();
				data.put("cktid", root.getAttributeValue("id"));

				DbUtility.INSTANCE.deleteXConnect(ne, fromAid, toAid);
			}
			dbchgEvent.addDataElement(data);
			// DbUtility.INSTANCE.addNewEvent(dbchgEvent.eventNodeToString());

			// If it's a WAN interface, then make sure the data (i.e. provUnit...)
			// is up to date
			/*
			 * if (fromAid.indexOf("WAN") >= 0) getAndUpdateFacility(ne, "WAN",
			 * fromAid.substring(0, fromAid.lastIndexOf('-')), MODIFY); else if
			 * (toAid.indexOf("WAN") >= 0) getAndUpdateFacility(ne, "WAN",
			 * toAid.substring(0, toAid.lastIndexOf('-')), MODIFY);
			 */
			// GGL2 - ignoring L2SS-based WAN facilities for state changes
			if (fromAid.indexOf("WAN") >= 0 && !fromSideIsL2SS) {
				checkForDbChange(ne, Tl1CommandCode.RTRV_WAN,
				    fromAid.substring(0, fromAid.lastIndexOf('-')));
			}
			else if (toAid.indexOf("WAN") >= 0 && !toSideIsL2SS) {
				checkForDbChange(ne, Tl1CommandCode.RTRV_WAN,
				    toAid.substring(0, toAid.lastIndexOf('-')));
			}
		}
		catch (Exception e) {
			log.error("Failed to handle event", e);
			return false;
		}
		getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
		return true;
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
		// -------------- GGL2 ADDING L2SS SUPPORT; REMOVE THIS BELOW
		// ------------------
		// Map<String, String> paramList = new HashMap<String, String>();
		// paramList.put(ObjectElement.OPERATION_KEY,
		// Tl1CommandCode.RTRV_EQPT.toString());
		// paramList.put("AID", "L2SS-1-" + aidMap[2]);
		// try
		// {
		// ArrayList<Map<String, String>> temp = new ArrayList<Map<String,
		// String>>();
		// temp.add(new HashMap<String, String>());
		// List<Map<String, String>> result = ((NetworkElement)
		// ne).getTl1Session().sendToNE(paramList);
		// if (result != null)
		// {
		// return false;
		// }
		// }
		// catch (Exception e)
		// {
		// }

		String command = values.get("COMMAND");
		if (command.indexOf("STCHG-EQPT") >= 0) {
			// don't handle any thing, for now
			log.debug("Got the STCHG-EQPT report for " + fromAid);
		}
		else {
			log.debug("Handle state change " + command + " for " + fromAid);

			// GGL2 Handling of non-fixed WAN/ETH on LS22:
			// The WAN facilities will be created/deleted as required.
			// - On a Delete, remove the facility from the db
			// - On an Add, proceed to add to the db
			// - Silence anything else: on L2 deactivation, the issues a number of
			// STCHG and DBCHG events
			// on the WAN facility...moments before it is deleted! ...thus causing a
			// number of
			// subsequent retrieval errrors
			if (fromAid.indexOf("WAN") >= 0 && ne.slotIsL2SS(aidMap[1], aidMap[2])) {
				if (oper == CHANGE.DELETE) {
					try {
						DbNetworkElementFacility.INSTANCE.delete(ne, fromAid);
						// Do nothing (no event propagation please. The WAN is GONE).
						return false;
					}
					catch (Exception e) {
						log.error("Failed to delete " + fromAid + " from database.", e);
					}
				}

				else if (oper != CHANGE.ADD) {
					// Do nothing (no event propagation please. The WAN is GONE).
					return false;
				}

			}

			Map<String, String> aResult = getAndUpdateFacility(ne,
			    Tl1CommandCode.fromString("RTRV_" + aidMap[0]), fromAid, oper);
			if (aResult != null) {
				getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);
				return true;
			}
		}

		return false;
	}

	private boolean updateFacilityAttribute(AbstractNe ne,
	    Map<String, String> aResult, String aid, CHANGE oper) throws Exception {
		// GGL2 requires update - when WAN is created/deleted

		// GGL2 - break implicit L1/L2 mapping
		Map<String, String> facilityMap;
		int layer = 1;
		String layerStr = AbstractFacilityXml.LAYER1_ELEMENT;
		Element newFacElem = new Element(layerStr);

		if (aid.indexOf("WAN") >= 0) {
			facilityMap = OmeFacilityXML.wanNeToXmlMapping;
		}
		else if (aid.indexOf("ETH") >= 0) {
			layer = 2;
			layerStr = AbstractFacilityXml.LAYER2_ELEMENT;
			newFacElem.setName(layerStr);

			facilityMap = OmeFacilityXML.ethNeToXmlMapping;
		}
		else {
			facilityMap = OmeFacilityXML.commonNeToXmlMapping;
		}

		// Build the list of XML attributes based on the NE key/value.
		Map<String, String> currentNeAttrValue = new HashMap<String, String>();
		Iterator<String> keyIr = facilityMap.keySet().iterator();
		while (keyIr.hasNext()) {
			String neKey = keyIr.next();
			String xmlKey = facilityMap.get(neKey);
			String aValue = aResult.get(neKey);
			if (aValue != null) {
				currentNeAttrValue.put(xmlKey, aValue);
				newFacElem.setAttribute(xmlKey, aValue);
			}
		}

		String changedFac = DbUtility.INSTANCE.retrieveAFacility(ne, aid);

		XMLOutputter outXml = new XMLOutputter(Format.getCompactFormat());

		if (changedFac == null) {
			// Create new entry and setup the data for the event as well
			HashMap<String, String> eventData = new HashMap<String, String>();
			eventData.put(AbstractFacilityXml.AID_ATTR, aid);
			eventData.put("operation", CHANGE.ADD.asString());
			eventData.put(AbstractFacilityXml.ID_ATTR, Integer.toString(layer));

			newFacElem.setAttribute(AbstractFacilityXml.VALID_ATTR, "true");
			newFacElem.setAttribute(AbstractFacilityXml.ID_ATTR,
			    Integer.toString(layer));
			newFacElem.setAttribute(AbstractFacilityXml.AID_ATTR, aid);

			List<Attribute> staticAttr = OmeFacilityXML.getStaticAttributes();
			for (int i = 0; i < staticAttr.size(); i++) {
				Attribute anAttr = staticAttr.get(i);
				if (newFacElem.getAttributeValue(anAttr.getName()) == null) {
					newFacElem.setAttribute(anAttr.getName(), anAttr.getValue());
				}
				eventData.put(anAttr.getName(), anAttr.getValue());
			}
			String[] aidMap = aid.split("-");
			eventData.put(AbstractFacilityXml.PORT_ATTR, aidMap[3]);
			eventData.put(AbstractFacilityXml.SHELF_ATTR, aidMap[1]);
			eventData.put(AbstractFacilityXml.SLOT_ATTR, aidMap[2]);
			eventData.put(AbstractFacilityXml.TYPE_ATTR, aidMap[0]);

			newFacElem.setAttribute(AbstractFacilityXml.PORT_ATTR, aidMap[3]);
			newFacElem.setAttribute(AbstractFacilityXml.SHELF_ATTR, aidMap[1]);
			newFacElem.setAttribute(AbstractFacilityXml.SLOT_ATTR, aidMap[2]);
			newFacElem.setAttribute(AbstractFacilityXml.TYPE_ATTR, aidMap[0]);

			// GGL2 flag for L2SS-based facilities: ETH or WAN on ReptDbchg
			if (aid.indexOf("WAN") >= 0 || aid.indexOf("ETH") >= 0) {
				if (ne.slotIsL2SS(aidMap[1], aidMap[2])) {
					newFacElem.setAttribute(FacilityConstants.IS_L2SS_FACILITY, "true");

					if (aid.indexOf("ETH") >= 0) {
						eventData.put(AbstractFacilityXml.VCAT_ATTR,
						    FacilityConstants.VCAT_DISABLE_STR);
					}
				}
			}

			// PREPARE THE EVENT FOR LPCP_PORT
			dbchgEvent.addDataElement(eventData);
			// GGL2 - THIS HAS BEEN A BUG even before L2SS work. Added facility events
			// did not contain the specific facility attributes when sending the
			// event up to LPCP_PORT:
			dbchgEvent.addDataElement(currentNeAttrValue);

			String newFacStr = outXml.outputString(newFacElem);

			if (oper == CHANGE.ADD) {
				log.debug("Create new Facility: " + newFacStr);
				DbUtility.INSTANCE.addNewFacility(ne, newFacStr);
			}
		}
		else {
			OmeFacilityXML changedFacXML = new OmeFacilityXML(changedFac);
			Map<String, String> updateList = changedFacXML.updateFacilityAttr(aid,
			    currentNeAttrValue);
			if (updateList != null || oper != CHANGE.MODIFY) {
				// If there are no changes and the operation is ADD or DELETE, then
				// we still want to update the Valid field accordingly
				if (updateList == null) {
					updateList = new HashMap<String, String>();
				}
				if (oper == CHANGE.ADD) {
					updateList.put(AbstractFacilityXml.VALID_ATTR, "true");
				}
				else if (oper == CHANGE.DELETE) {
					updateList.put(AbstractFacilityXml.VALID_ATTR, "false");
				}

				updateList.put(AbstractFacilityXml.AID_ATTR, aid);
				updateList.put("operation", oper.asString());

				dbchgEvent.addDataElement(updateList);
				
				DbUtility.INSTANCE.updateAFacility(ne, updateList, aid);
			}
			else {
				
				return false;
			}
		}
		return true;

	}
}
