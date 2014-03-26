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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.security.policy.types.RootUserGroupName;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.PathType;
import com.nortel.appcore.app.drac.common.types.RecurrenceType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.Schedule.ACTIVATION_TYPE;
import com.nortel.appcore.app.drac.common.types.State.SCHEDULE;
import com.nortel.appcore.app.drac.common.types.UserType;

public class DbScheduleTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Test
  public void testExercise() throws Exception {
    TestHelper.INSTANCE.initialize();

    try {
      UserType uit = new UserType("unknown",
          RootUserGroupName.SYSTEM_ADMIN_GROUP, "admin", "admin", "admin",
          "admin", "email@myHouse");

      @SuppressWarnings("serial")
      PathType path = new PathType() {
        {
          getSourceEndPoint().setName("src");
          getTargetEndPoint().setName("dest");
        }
      };
      Map<String, String> m = new HashMap<String, String>();
      m.put("Testing", "For John");
      path.setNsvMap(m);

      Schedule sched = new Schedule(ACTIVATION_TYPE.PRERESERVATION_AUTOMATIC,
          "id", "user", SCHEDULE.EXECUTION_PENDING, System.currentTimeMillis(),
          null, 1000 * 60 * 60 * 24, uit, path, false, new RecurrenceType(),
          new ArrayList<DracService>());

      DbSchedule.INSTANCE.deleteAll();
      log.debug("adding schedule " + sched.toDebugString());
      DbSchedule.INSTANCE.add(sched);

      DbSchedule.INSTANCE.getServiceUsageForTNA("tna");
      DbSchedule.INSTANCE.getAuditScheduleStatus();
      DbSchedule.INSTANCE.getNextSchedule();
      DbSchedule.INSTANCE.getServiceUsageForTNA("id");
      DbSchedule.INSTANCE.getTableName();
      // DbSchedule.INSTANCE.queryScheduleFromServiceId("id");
      List<UserGroupName> groups = new ArrayList<UserGroupName>();
      groups.add(RootUserGroupName.SYSTEM_ADMIN_GROUP);

      log.debug("querySchedules returns:"
          + DbSchedule.INSTANCE.querySchedules(0, Long.MAX_VALUE, groups));
      DbSchedule.INSTANCE.queryServices(0, Long.MAX_VALUE, groups);
      DbSchedule.INSTANCE.queryServiceSummaryFromServiceId("id");

      DbSchedule.INSTANCE.queryUtilization("tna", 0, Long.MAX_VALUE);
      DbSchedule.INSTANCE.queryUtilization("id", 0, Long.MAX_VALUE);

      Map<String, String> rf = new HashMap<String, String>();
      DbSchedule.INSTANCE.retrieve(null);
      DbSchedule.INSTANCE.retrieve(rf);
      rf.put(DbKeys.ENDTIME_GREATERTHAN_EQUALTO,
          Long.toString(Long.MAX_VALUE));
      rf.put(DbSchedule.ENDTIME_LESSTHAN_EQUALTO, "0");
      rf.put(DbSchedule.STARTTIME_GREATERTHAN_EQUALTO,
          Long.toString(Long.MAX_VALUE));
      rf.put(DbKeys.STARTTIME_LESSTHAN_EQUALTO, "0");
      rf.put(DbSchedule.SCHD_NAME, "fred");

      DbSchedule.INSTANCE.retrieve(rf);

      Map<String, String> update = new HashMap<String, String>();
      DbSchedule.INSTANCE.update("id", update);
      update.put(DbSchedule.SCHD_STATUS, "junk");
      update.put(DbSchedule.SCHD_STARTTIME, "0");
      update.put(DbSchedule.SCHD_ENDTIME, "0");
      update.put(DbSchedule.SCHD_ENDTIME, "0");
      DbSchedule.INSTANCE.update("id", update);

      DbSchedule.INSTANCE.delete("id");
    }
    catch (Exception e) {
      log.error("Error: ", e);
      throw e;
    }
    finally {
      DbSchedule.INSTANCE.deleteAll();
    }
  }
}
