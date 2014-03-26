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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opendrac.test.TestHelper;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.Site;

public final class DbSitesTest {

	@Test
	public void testExercise() throws Exception {
		TestHelper.INSTANCE.initialize();

		try {
			DbSites.INSTANCE.deleteAll();

			Map<String, String> map = new HashMap<String, String>();
			map.put(DbKeys.SitesCols.DESCRIPTION, "test");
			map.put(DbKeys.SitesCols.ID, "999");
			map.put(DbKeys.SitesCols.LOCATION, "Ottawa");

			DbSites.INSTANCE.add(new Site("999", "Ottawa", "test"));
			Map<String, String> filter = new HashMap<String, String>();
			DbSites.INSTANCE.retrieve(filter);
			filter.put(DbKeys.SitesCols.DESCRIPTION, "test");
			DbSites.INSTANCE.retrieve(filter);

			DbSites.INSTANCE.retrieveAll();
			DbSites.INSTANCE.update(
			    new Site("999", "Ottawa, Ontario, Canada", "Nice place to live"));
		}
		finally {
			DbSites.INSTANCE.deleteAll();
		}
	}

}
