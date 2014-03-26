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

package com.nortel.appcore.app.drac.database.helper.test;

import org.junit.Before;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPathAlarmDetails;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;

public class DbTestPopulateDbTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setup() {
		TestHelper.INSTANCE.initialize();
	}

	@Test
	public void testClearAllDRACDatabases() throws Exception {
		DbTestPopulateDb.clearAllDRACDatabases();
	}

	@Test
	public void testPopulateTestSystem() throws Exception {
		DbTestPopulateDb.populateTestSystem(true);
		log.debug("Got NEs: " + DbNetworkElement.INSTANCE.retrieveAll());
		log.debug("Got Facilities: "
		    + DbNetworkElementFacility.INSTANCE.retrieve(null));
		log.debug("Got Adjacencies "
		    + DbNetworkElementAdjacency.INSTANCE.retrieve());
		log.debug("Got Alarms "
		    + XmlUtility.elementToString(DbLightPathAlarmDetails.INSTANCE
		        .retrieve(null)));
		log.debug("Got Schedules: " + DbSchedule.INSTANCE.retrieve(null));
		log.debug("Got Services: " + DbLightPath.INSTANCE.retrieve(null));
	}
}
