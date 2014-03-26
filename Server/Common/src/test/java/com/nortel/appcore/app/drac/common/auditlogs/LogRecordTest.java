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

package com.nortel.appcore.app.drac.common.auditlogs;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_CATEGORY;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_RESULT;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_SEVERITY;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord.LOG_TYPE;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupName;

public final class LogRecordTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
	@Test
	public void testLogRecord() throws Exception {
		LogRecord l = new LogRecord(System.currentTimeMillis(), "orig", "ip",
		    new UserGroupName("ug"), LOG_SEVERITY.CRITICAL,
		    LOG_CATEGORY.AUTHENTICATION, LOG_TYPE.ACCESS_CHECK, "resource",
		    LOG_RESULT.SUCCESS, "description", null);
		l.toString();
		l.getBillingGroup();
		l.getCategory();
		l.getDescription();
		l.getIp();
		l.getOriginator();
		l.getResource();
		l.getResult();
		l.getSeverity();
		l.getTime();
		l.getType();
		l.getDetails();
		l.isBillingGroupSet();
		l.isOriginatorSet();
		

		Map<String, String> m = new HashMap<String, String>();
		m.put("testing", "junit");
		new LogRecord(System.currentTimeMillis(), "orig", "ip", new UserGroupName(
		    "ug"), LOG_SEVERITY.CRITICAL, LOG_CATEGORY.AUTHENTICATION,
		    LOG_TYPE.ACCESS_CHECK, "resource", LOG_RESULT.SUCCESS, "description", m);

		new LogRecord(System.currentTimeMillis(), null, null, null, null, null,
		    null, null, null, null, null);

		new LogRecord(null, null, null, null, LogKeyEnum.KEY_A_LOG_FOR_TESTING_ONLY);
		new LogRecord(null, null, null, null,
		    LogKeyEnum.KEY_A_LOG_FOR_TESTING_ONLY, new String[] { "" });
		new LogRecord("bob", "joe", UserGroupName.USER_GROUP_NOTAPPL, "resource",
		    LogKeyEnum.KEY_A_LOG_FOR_TESTING_ONLY, new String[] { "" }, m);
	}
}
