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

package com.nortel.appcore.app.drac.server.nrb.impl.administration;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.NrbException;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType.RecurrenceFreq;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.State;
import com.nortel.appcore.app.drac.common.types.UserType;

public class LpcpAdminManagerTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
	@Test
	public void testGenerateServiceInfo() {
	  TestHelper.INSTANCE.initialize();
		GregorianCalendar start = new GregorianCalendar(2010, 0, 1, 15, 0, 0);
		GregorianCalendar end = new GregorianCalendar(2010, 2, 3, 15, 0, 0);
		RecurrenceType r = new RecurrenceType(RecurrenceFreq.FREQ_DAILY, 1, 0, null);

		Schedule s = new Schedule(ACTIVATION_TYPE.RESERVATION_AUTOMATIC, "unknown",
		    "unAssigned", State.SCHEDULE.EXECUTION_PENDING,
		    start.getTimeInMillis(), Long.valueOf(end.getTimeInMillis()),
		    60 * 60 * 1000, new UserType(null, null, null, null, null, null, null),
		    null, true, r, null);

		log.debug("Created serviceInfo from " + s.toDebugString());
		try {
      long[][] res = LpcpAdminManager.INSTANCE.generateServiceInfo(s);
      assertEquals(62, res.length);

      for (long[] r1 : res) {
      	log.debug("Schedule starts " + r1[0] + "(" + new Date(r1[0]).toString()
      	    + ") and ends at " + r1[1] + "(" + new Date(r1[1]).toString() + ")");
      }
    }
    catch (NrbException e) {
      log.error("Error: ", e);
     fail("See previous stack trace");
    }
	}

}
