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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractFacilityXml;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public class ReptLine extends AbstractCommandlet {
	public enum SdhType {
		STM1("STM1", "OC3"), STM4("STM4", "OC12"), STM16("STM16", "OC48"), STM64(
		    "STM64", "OC192");
		private String sonet;
		private String sdh;

		SdhType(String sdh, String sonet) {
			this.sdh = sdh;
			this.sonet = sonet;
		}
	}

	private static final int MODIFY = 0;
	private static final int ADD = 1;
	private static final int DELETE = 2;
	private static final String[] OPERATION_STR = { "modify", "add", "delete" };

	private Tl1XmlDbChangeEvent dbchgEvent;

	public ReptLine(Map<String, Object> param) {
		super(param);
	}

	/*
	 * processCompoundFields: command=REPT-LINE,data={slotId=501, SRLG=,
	 * RXSIGIP=0.0.0.0, signalNumber=1, subslotId=0, FECFRMT=FEC1, TPNTTEM=10G,
	 * D4PASS=DISABLE, FECSTATE=INACTIVE, METRIC2=128, ASTNSIG=NONE, TPNTACT=N,
	 * AID=OC192-1-501-0-1-1, shelfId=1, ASTNBLOCK=N, PST=IS,
	 * DATETIME=2006-06-05-10-39-22, entityType=OC192, operation=REPT-LINE,
	 * SSBITMDE=SONET, SDGTH=10E-8, PRIME=OSS, LPBKPORT=INACTIVE,
	 * TASTATE=INACTIVE, portId=1, COST=128, PROVSIGIP=0.0.0.0,
	 * uniqueKey=OC192-1-501-0-1-1, SFTH=10E-4, ACTION=UPDATE,
	 * neId=aa-40-00-00-00-01, rate=OC192, LABEL=,
	 * AIDFORMAT=TYP-SH-SL-SBSL-PRT-SIG, LPBKLK=LOCKED}
	 */
	@Override
	public boolean start() {
		try {
			AbstractNe ne = (AbstractNe) getParameters().get(
			    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
			HashMap<String, String> data = new HashMap<String, String>();
			ne.upDateLocalInfo();
			dbchgEvent = new Tl1XmlDbChangeEvent(ne);
			dbchgEvent.setReportType("facility");
			TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
			    AbstractNetworkElement.EVENTRECV_KEY);
			Map<String, String> values = anEvent.getPayloads().get(0);

			String[] temp = values.get("DATETIME").split("-");
			dbchgEvent.setOccurrentDate(temp[0] + "-" + temp[1] + "-" + temp[2]);
			dbchgEvent.setOccurrentTime(temp[3] + "-" + temp[4] + "-" + temp[5]);

			String neId = ne.getNeId();
			dbchgEvent.setEventId(neId + "_" + anEvent.getCtag());
			dbchgEvent.setOwnerId(ne.getTerminalId());

			// Convert the facility type, if necessary STM64-1-504-0-1-1
			String[] aidMap = values.get("AID").split("-");
			String facilityType = convertSdh(aidMap[0]);
			String aid = facilityType + "-" + aidMap[1] + "-" + aidMap[2] + "-"
			    + aidMap[3] + "-" + aidMap[4] + "-" + aidMap[5];

			Map<String, String> updateList = new HashMap();
			String state = values.get("PST");
			if (state != null) {
				updateList.put(AbstractFacilityXml.PRIMARYSTATE_ATTR, state);
				data.put(AbstractFacilityXml.PRIMARYSTATE_ATTR, state);
			}
			data.put(AbstractFacilityXml.AID_ATTR, aid);

			String command = values.get("ACTION");
			int operation;

			if (command.startsWith("ADD")) {
				operation = ADD;
				HdxFacilityXML aFacility = new HdxFacilityXML(ne);
				aFacility.updateFacilityInstance("1", aid, updateList);
				DbUtility.INSTANCE
				    .addNewFacility(ne, aFacility.rootNodeToString());

				List<Attribute> staticAttr = AbstractFacilityXml.getStaticAttributes();
				for (int i = 0; i < staticAttr.size(); i++) {
					Attribute attr = staticAttr.get(i);
					data.put(attr.getName(), attr.getValue());
				}
				data.put(AbstractFacilityXml.ID_ATTR, "1");
				data.put(AbstractFacilityXml.PORT_ATTR, aidMap[4]);
				data.put(AbstractFacilityXml.SHELF_ATTR, aidMap[1]);
				data.put(AbstractFacilityXml.SLOT_ATTR, aidMap[2]);
				data.put(AbstractFacilityXml.TYPE_ATTR, facilityType);

			}
			else if (command.startsWith("DEL")) {
				operation = DELETE;
				updateList.put(AbstractFacilityXml.VALID_ATTR, "false");
				DbUtility.INSTANCE.updateAFacility(ne, updateList, aid);
			}
			else {
				operation = MODIFY;
				// only care about the state for now and it was updated above
				DbUtility.INSTANCE.updateAFacility(ne, updateList, aid);
			}
			data.put("operation", OPERATION_STR[operation]);
			dbchgEvent.addDataElement(data);
			getParameters().put(AbstractCommandlet.RESULT_KEY, dbchgEvent);

			return true;
		}
		catch (Exception e) {
			log.error("Failed to process facility event", e);
			return false;
		}
	}

	private String convertSdh(String facType) {
		String rc = facType;
		if (facType.startsWith("OC")) {
			return rc;
		}

		for (SdhType type : SdhType.values()) {
			if (type.sdh.equalsIgnoreCase(facType)) {
				rc = type.sonet;
				break;
			}
		}
		return rc;
	}
}
