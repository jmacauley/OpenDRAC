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

package com.nortel.appcore.app.drac.server.lpcp.scheduler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;
import com.nortel.appcore.app.drac.server.lpcp.rmi.NeProxyRmiMediator;

public final class AlarmAudit extends TimerTask {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void run() {
		
		try {
			/* Get the list of NEs that we think they still have active alarms. */
			LinkedList<String> neList = new LinkedList<String>(DbUtilityLpcpScheduler
			    .INSTANCE.getNeWithActiveAlarm());
			log.debug("Alarm Audit: List of NEs that have active service alarm: "
			    + neList);

			while (!neList.isEmpty()) {
				String neId = neList.remove(0);
				try {
					String xmlString = NeProxyRmiMediator.INSTANCE.getXmlAlarm(neId);
					Element outputData = XmlUtility.createDocumentRoot(xmlString)
					    .getChild(ClientMessageXml.NEEVENT_NODE);
					if (outputData != null) {
						@SuppressWarnings("unchecked")
						List<Element> events = outputData.getChildren();
						if (events != null) {
							Set<String> currentActive = new HashSet<String>();
							for (Element anEvent : events) {
								currentActive.add(anEvent.getAttributeValue("id"));
							}
							checkAlarm(neId, currentActive);
						}
					}
				}
				catch (Exception se) {
					log.error("Alarm Audit: failed to retrieve alarm from ne " + neId
					    + " with exception, will retry next audit ", se);
				}
			}
			
		}
		catch (Exception e) {
			log.error("Alarm Audit: Failed", e);
		}
	}

	/**
	 * Get all active service alarms from the database for the particular NE and
	 * comparing with the list from the NE, if the alarm from the database is not
	 * in the list from the NE, then it should be marked as clear.
	 */
	private void checkAlarm(String neId, Set<String> alarmList) {
		try {
			Map<String, String> alarmFromDb = DbUtilityLpcpScheduler.INSTANCE
			    .getActiveAlarmFromNe(neId);
			Iterator<String> ir = alarmFromDb.keySet().iterator();
			while (ir.hasNext()) {
				String anAlarmId = ir.next();
				if (!alarmList.contains(anAlarmId)) {
					// should clear this alarm
					long raisedTime = Long.parseLong(alarmFromDb.get(anAlarmId));
					long duration = System.currentTimeMillis() - raisedTime;
					DbUtilityLpcpScheduler.INSTANCE.updateAlarmDuration(anAlarmId,
					    duration);
					Element reason = new Element("element");
					reason.setAttribute("name", "reason");
					reason.setAttribute("value", "cleared by audit");
					DbUtilityLpcpScheduler.INSTANCE.appendAlarmData(anAlarmId,
					    reason);
					log.debug("Manually clear this alarm " + anAlarmId + " in alarm audit");
				}
			}
		}
		catch (Exception e) {
			log.error("Failed to process alarm for NE " + neId, e);
		}
	}
}