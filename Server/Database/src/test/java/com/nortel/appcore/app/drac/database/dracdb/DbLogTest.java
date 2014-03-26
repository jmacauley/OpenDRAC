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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_CATEGORY;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;

public class DbLogTest {
	
  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  
  @Before
  public void setup(){
    TestHelper.INSTANCE.initialize();
  }

	@Test
	public void testGetInstance() throws Exception {

		try {
			DbLog.INSTANCE.deleteAll();

			DbLog.INSTANCE.generateLog(
			    new LogRecord(null, null, null, null,
			        LogKeyEnum.KEY_A_LOG_FOR_TESTING_ONLY));
			DbLog.INSTANCE.addLog(
			    new LogRecord(null, null, null, null,
			        LogKeyEnum.KEY_A_LOG_FOR_TESTING_ONLY));
			DbLog.INSTANCE.addLog(
			    new LogRecord("bob", "127.0.0.1", UserGroupName.USER_GROUP_NOTAPPL,
			        "fred", LogKeyEnum.KEY_AUTH_SUCCESS, new String[] { "bob" }));

			Map<String, String> m = new HashMap<String, String>();
			m.put("anyName", "anyValue");

			// fields longer than the database length will be shortened.
			String longString = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
			String res = longString + longString + longString;
			DbLog.INSTANCE.addLog(
			    new LogRecord("orig", "ip", new UserGroupName("ug"), res,
			        LogKeyEnum.KEY_CRITICAL_ALARM, new String[] { "bob" }, m));

			List<LogRecord> result = DbLog.INSTANCE.getLogs(0, Long.MAX_VALUE,
			    null);
			
			assertEquals("Not expected size", 4, result.size());

			Map<String, String> filter = new HashMap<String, String>();
			result = DbLog.INSTANCE.getLogs(0, Long.MAX_VALUE, filter);
			assertEquals("Not expected size", 4, result.size());
			filter.put(DbKeys.LogKeys.CATEGORY, LOG_CATEGORY.AUTHENTICATION.name());
			result = DbLog.INSTANCE.getLogs(0, Long.MAX_VALUE, filter);
			assertEquals("Not expected size", 1, result.size());

			// all possible filters to hit all of the tests.
			filter.put(DbKeys.LogKeys.ORIGINATOR, "");
			filter.put(DbKeys.LogKeys.BILLING_GROUP, "");
			filter.put(DbKeys.LogKeys.SEVERITY, "");
			filter.put(DbKeys.LogKeys.CATEGORY, "");
			filter.put(DbKeys.LogKeys.LOG_TYPE, "");
			filter.put(DbKeys.LogKeys.RESOURCE, "");
			filter.put(DbKeys.LogKeys.RESULT, "");
			filter.put(DbKeys.LogKeys.IP_ADDR, "");
			filter.put(DbKeys.LogKeys.DESC, "");
			result = DbLog.INSTANCE.getLogs(0, Long.MAX_VALUE, filter);
			assertEquals("Not expected size", 0, result.size());
			// invalid filter

			filter.put("badFilter", "badString");
			try {
				result = DbLog.INSTANCE.getLogs(0, Long.MAX_VALUE, filter);
				fail("bad");
			}
			catch (Exception e) {
			}

			DbLog.INSTANCE.deleteTimeLessThan(System.currentTimeMillis());
			// should be zero left
			result = DbLog.INSTANCE.getLogs(0, Long.MAX_VALUE, null);
			assertEquals("Not expected size", 0, result.size());

			
			DbLog.INSTANCE.deleteTimeLessThan(0);
		}
		finally {
			DbLog.INSTANCE.deleteAll();
		}
	}
	
  @Test
  public void testFindByResourceAndDescription() throws Exception {
    String scheduleId = "SCHEDULE-111111111111";
    final String serviceId = "SERVICE-1363871427980";
    DbLog.INSTANCE.addLog(new LogRecord(null, null, null, serviceId, LogKeyEnum.KEY_SERVICE_ACTIVATED));
    boolean isActivated = DbLog.INSTANCE.findServiceActivatedByResource(scheduleId);
    assertTrue(isActivated);
  }
	
	@Test
  public void testDoNotFindByResourceAndDescription() throws Exception {
    String scheduleId = "SCHEDULE-1363871427980";
    boolean isActivated = DbLog.INSTANCE.findServiceActivatedByResource(scheduleId);
    assertFalse(isActivated);
  }
}
