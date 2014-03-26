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

import org.junit.Test;
import org.opendrac.test.TestHelper;

import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.types.ServiceXml;

public class DbLightPathAlarmSummariesTest {

	@Test
	public void testExercise() throws Exception {
		TestHelper.INSTANCE.initialize();

		DbLightPathAlarmSummaries.INSTANCE.getTableName();
		DbLightPathAlarmSummaries.INSTANCE.deleteAll();
		/*
		 * '00-20-D8-DF-33-8B_010003764 3-6212-0348','CR
		 * ',1240427856213,'SERVICE-1240427562766','00-20-D8-DF-33-8B'
		 */
		StringBuilder newAlarmBuff = new StringBuilder(50);
		newAlarmBuff.append("<alarm ");
		newAlarmBuff.append(ServiceXml.ID_ATTR
		    + "=\"00-20-D8-DF-33-8B_0100037643-6212-0348\" ");
		newAlarmBuff.append("severity=\"CR\" ");
		newAlarmBuff
		    .append("occurredTime=\"1240427856213\" serviceId=\"SERVICE-1240427562766\"/>");
		DbLightPathAlarmSummaries.INSTANCE.add("00-20-D8-DF-33-8B",
		    DbOpsHelper.xmlToElement(newAlarmBuff.toString()));
		DbLightPathAlarmSummaries.INSTANCE.deleteByServiceId(
		    "SERVICE-1240427562766");
		DbLightPathAlarmSummaries.INSTANCE.deleteAll();
	}

}
