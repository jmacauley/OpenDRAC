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

package com.nortel.appcore.app.drac.database.dracdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;
import org.junit.Test;
import org.opendrac.test.TestHelper;

import com.nortel.appcore.app.drac.common.db.DbOpsHelper;

public class DbLightPathAlarmDetailsTest {

	@Test
	public void testGetInstance() throws Exception {
		TestHelper.INSTANCE.initialize();

		String xmlAlarmRecord = "<event name=\"alarm\" id=\"00-20-D8-DF-33-59_0100001345-0016-0357\" owner=\"TDEFAULT_PROXY\" time=\"1244207392630\" duration=\"0\">"
		    + " <eventInfo notificationType=\"CR\" occurredDate=\"2009-06-05\" occurredTime=\"13-09-51\" />"
		    + "<data><element name=\"description\" value=\"Loss of Multiframe\" /> "
		    + "<element name=\"aid\" value=\"STS3C-1-1-4-16\" /> "
		    + "<element name=\"facility\" value=\"WAN-1-1-4\" /> "
		    + "<element name=\"channel\" value=\"16\" /><element name=\"serviceId\" value=\"SERVICE-1244199745989\" /> "
		    + "<element name=\"serviceId\" value=\"SERVICE-1244203915445\" /></data> "
		    + "<node type=\"OME5\" id=\"00-20-D8-DF-33-59\" ip=\"145.145.67.67\" port=\"10001\" tid=\"Asd001A_OME3T\" mode=\"SDH\" status=\"aligned\" /></event>";

		DbLightPathAlarmDetails.INSTANCE.deleteAll();
		DbLightPathAlarmDetails.INSTANCE.add(
		    DbOpsHelper.xmlToElement(xmlAlarmRecord));
		Element reason = new Element("element");
		reason.setAttribute("name", "reason");
		reason.setAttribute("value", "cleared by audit");
		DbLightPathAlarmDetails.INSTANCE.appendAlarmData(
		    "00-20-D8-DF-33-59_0100001345-0016-0357", reason);
		DbLightPathAlarmDetails
		    .INSTANCE
		    .deleteServiceReference(
		        Arrays
		            .asList(new String[] { "00-20-D8-DF-33-59_0100001345-0016-0357" }),
		        "SERVICE-1244203915445");
		DbLightPathAlarmDetails.INSTANCE.getNeWithActiveAlarm();
		DbLightPathAlarmDetails.INSTANCE.updateAlarmDuration(
		    "00-20-D8-DF-33-59_0100001345-0016-0357", 45);
		DbLightPathAlarmDetails.INSTANCE.retrieve(null);
		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(DbLightPathAlarmDetails.TIME_GREATERTHAN_EQUALTO,
		    Long.valueOf(0));
		filter.put(DbLightPathAlarmDetails.TIME_LESSTHAN_EQUALTO, Long.valueOf(0));
		filter.put(DbLightPathAlarmDetails.ALARMID, "noWay");
		filter.put(DbLightPathAlarmDetails.DURATION, -42L);
		filter.put(DbLightPathAlarmDetails.NEID, "noWay");
		DbLightPathAlarmDetails.INSTANCE.retrieve(filter);
	}

}
