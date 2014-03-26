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

import java.util.Map;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlAlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

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
		alarmEvent.setAlarmAid(values.get("AID"));
		alarmEvent.setEventId(ne.getNeId() + "_" + values.get("DGNTYPE"));
		alarmEvent.setOwnerId(ne.getTerminalId());
		alarmEvent.updateDescription(values.get("CONDDESCR"));
		// parameters.put(Commandlet.RESULT_KEY, alarmEvent.toString());
		getParameters().put(AbstractCommandlet.RESULT_KEY, alarmEvent);

		// if (ne.channel != null) {
		// StringBuffer sendEvent = new StringBuffer(buildEventHeader(ne));
		// sendEvent.append("/*\r\n");
		// sendEvent.append(alarmEvent.toString() + "\r\n*/\r\n;\r\n");
		// ne.channel.broadcast(sendEvent.toString());
		// }

		// update Database
		// try {
		// DbUtility.INSTANCE.addNewEvent(alarmEvent.eventNodeToString());
		// } catch (Exception e) {e.printStackTrace();}

		return true;
	}

	// private String buildEventHeader(AbstractNe ne)
	// {
	// String currentTimeStamp = new
	// java.sql.Timestamp(System.currentTimeMillis()).toString();
	// String alarmDate = " " + currentTimeStamp.substring(2, 10);
	// String alarmTime = " " + currentTimeStamp.substring(11, 19);
	// String currentDate = " " + currentTimeStamp.substring(2, 10);
	// String currentTime = " " + currentTimeStamp.substring(11, 19);
	//
	// StringBuilder message = new StringBuilder("\r\n   ");
	// message.append(ne.terminalId);
	// message.append(currentDate);
	// message.append(currentTime);
	// message.append("\r\n** ");
	//
	// message.append(Channel.REPLACE_ATAG_KEY);
	// message.append(" REPT ALM\r\n");
	// return message.toString();
	// }
}
