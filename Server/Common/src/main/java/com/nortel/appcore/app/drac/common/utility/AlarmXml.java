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

package com.nortel.appcore.app.drac.common.utility;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.AlarmType;

public final class AlarmXml {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private final Element root;
	public static final String ID_ATTR = "id";
	public static final String SEVERITY_ATTR = "severity";
	public static final String TIME_ATTR = "time";
	public static final String DESC_ATTR = "description";
	public static final String DURATION_ATTR = "duration";
	public static final String SCHEDULE_NAME_ATTR = "scheduleName";
	public static final String SCHEDULE_ID_ATTR = "scheduleId";

	public AlarmXml(String xmlString) throws Exception {
		log.debug(xmlString);
		ByteArrayInputStream data = new ByteArrayInputStream(xmlString.getBytes());
		SAXBuilder builder = new SAXBuilder();
		Document aDoc = builder.build(data);
		root = aDoc.getRootElement();
	}

	public List<AlarmType> rootNodeToAlarmList() {
		List<AlarmType> alarmList = new ArrayList<AlarmType>();
		List<Element> alarms = root.getChildren();
		Iterator<Element> it = alarms.iterator();
		Element alarm = null;
		AlarmType alarmType = null;
		while (it.hasNext()) {
			alarm = it.next();
			if (alarm != null) {
				String id = alarm.getAttributeValue(ID_ATTR);
				String severity = alarm.getAttributeValue(SEVERITY_ATTR);
				String description = alarm.getAttributeValue(DESC_ATTR);

				long time = 0;
				try {
					time = Long.parseLong(alarm.getAttributeValue(TIME_ATTR));
				}
				catch (NumberFormatException nfe) {
					log.error("Time is not in number format");
				}
				long duration = 0;
				try {
					duration = Long.parseLong(alarm.getAttributeValue(DURATION_ATTR));
				}
				catch (NumberFormatException nfe) {
					log.error("Duration is not in number format");
				}
				List<Element> servicesAffected = alarm.getChildren();
				Iterator<Element> it2 = servicesAffected.iterator();
				while (it2.hasNext()) {
					Element serviceElement = it2.next();
					if (serviceElement != null) {
						String serviceId = serviceElement.getAttributeValue(ID_ATTR);
						String scheduleName = serviceElement
						    .getAttributeValue(SCHEDULE_NAME_ATTR);
						String scheduleId = serviceElement
						    .getAttributeValue(SCHEDULE_ID_ATTR);
						alarmType = new AlarmType(id, serviceId);
						alarmType.setSeverity(severity);
						alarmType.setDescription(description);
						alarmType.setOccurTime(time);
						alarmType.setDuration(duration);
						alarmType.setScheduleName(scheduleName);
						alarmType.setScheduleId(scheduleId);
						alarmList.add(alarmType);
					}
				}
			}
		}
		return alarmList;
	}
}
