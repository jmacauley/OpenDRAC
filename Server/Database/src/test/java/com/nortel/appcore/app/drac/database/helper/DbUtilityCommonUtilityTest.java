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

package com.nortel.appcore.app.drac.database.helper;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.database.dracdb.DbSchedule;
import com.nortel.appcore.app.drac.database.dracdb.DbSchedule.AuditScheduleStatusHolder;

public final class DbUtilityCommonUtilityTest {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setup() {
		TestHelper.INSTANCE.initialize();
	}

	@Test
	public void test() throws Exception {
		DbUtilityCommonUtility inst = DbUtilityCommonUtility.INSTANCE;

		inst.getNextSchedule();
		inst.findExpandableSchedules();
		inst.queryAllServiceAlarms(0, Long.MAX_VALUE);
		inst.queryCallStatus("123");
		inst.queryConfirmationTimeout();
		try {
			inst.querySchedule("123");
			fail("expected this to fail");
		}
		catch (Exception e) {
			// ok
		}
		try {
			inst.queryScheduleFromServiceId("123");
			fail("expected this to fail");
		}
		catch (Exception e) {
			// ok
		}
		inst.queryScheduleOffset();
		inst.queryServiceSummaryFromServiceId("123");
		inst.queryUtilization("tna", 0, 1000);
	}

	@Test
	public void testGetAuditScheduleStatus() throws Exception {
		List<AuditScheduleStatusHolder> x = DbSchedule.INSTANCE
		    .getAuditScheduleStatus();
		log.debug("getAuditScheduleStatus returns " + x);
	}

}
