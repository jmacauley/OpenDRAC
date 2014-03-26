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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogKeyEnumTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void test() throws Exception {
		for (LogKeyEnum e : LogKeyEnum.values()) {
			e.getCategory();
			e.getKey();
			e.getResult();
			e.getSeverity();
			e.getType();
			e.toString();
			e.getFormattedLogDescription(new String[] { "bob", "tom", "fred", "make",
			    "this", "end" });
			log.debug("key "
			    + e.getKey()
			    + " maps to '"
			    + e.getFormattedLogDescription(new String[] { "arg1", "arg2", "arg3",
			        "arg4", "arg5", "arg6" }, true) + "'");
		}
	}
}
