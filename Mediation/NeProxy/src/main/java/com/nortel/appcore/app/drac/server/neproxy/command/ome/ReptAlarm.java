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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * @author nguyentd
 */
public final class ReptAlarm extends AbstractCommandlet {
	public ReptAlarm(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		AbstractNe ne = (AbstractNe) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		ne.upDateLocalInfo();
		Tl1XmlAlarmEvent alarmEvent = new Tl1XmlAlarmEvent(ne);
		TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
		    AbstractNetworkElement.EVENTRECV_KEY);
		Map<String, String> values = anEvent.getPayloads().get(0);
		alarmEvent.setAlarmSeverity(values.get("NTFNCDE"));

		String temp = values.get("YEAR") + "-" + values.get("OCRDATE");
		alarmEvent.setOccurrentDate(temp);
		alarmEvent.setOccurrentTime(values.get("OCRTM"));
		String aid = values.get("AID");
		alarmEvent.setAlarmAid(aid);

		// Try to get the equipvalent aid of the facility if it's needed
		// For example, if AID=ETH-1-4-1 then the facility is the same, but
		// if the AID=STS1-1-4-1-1, then the equivalent facility might be
		// OC192-1-4-1 and the channel is 1. For the equipment alarm, just leave
		// it as is
		String[] aidMap = aid.split("-");
		try {
			List<String> facilityList = new ArrayList<String>();
			String facilityRecord;
			// Is there the "port" value?
			if (aidMap.length > 3) {
				facilityRecord = DbUtility.INSTANCE.retrieveAFacility(ne,
				    aidMap[1], aidMap[2], aidMap[3]);

				if (facilityRecord != null) {
					facilityList.add(facilityRecord);
				}
			}
			else {
				// There is no "port" value, so look for the first facility that matches
				// the given "shelf" and "slot"
				facilityList = DbUtility.INSTANCE.retrieveAFacility(ne, aidMap[1],
				    aidMap[2]);
			}
			if (facilityList.size() == 0) {
				// Can't find the facility, just log and return
				log.debug("Can't find the facility for " + aid);
				return false;
			}
			for (int i = 0; i < facilityList.size(); i++) {
				String aRec = facilityList.get(i);
				int startTypeIndex = aRec.indexOf("aid=") + 5;
				int endTypeIndex = aRec.indexOf('"', startTypeIndex);
				String aFacility = aRec.substring(startTypeIndex, endTypeIndex);
				alarmEvent.setFacilityType(aFacility);
				if (aidMap.length > 4) {
					// Set the channel as well
					alarmEvent.setChannelNumber(aidMap[4]);
				}
			}
		}
		catch (Exception e) {
			log.error("Failed to get a facility from db: " + aid, e);
		}
		alarmEvent.setEventId(ne.getNeId() + "_" + values.get("DGNTYPE"));
		alarmEvent.setOwnerId(ne.getTerminalId());
		alarmEvent.updateDescription(values.get("CONDDESCR"));
		getParameters().put(AbstractCommandlet.RESULT_KEY, alarmEvent);
		return true;
	}
}
