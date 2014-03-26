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

package com.nortel.appcore.app.drac.common.db;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbOpsHelperTest {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void testDeserialize() {
		DbOpsHelper.deserialize(DbOpsHelper.serialize("Testing")).toString();
	}

	@Test
	public void testElementToMap() throws Exception {
		DbOpsHelper.elementToMap(DbOpsHelper
		    .xmlToElement("<bob hello=\"hi\"></bob>"));
	}

	@Test
	public void testElementToXml() throws Exception {
		DbOpsHelper.elementToString(DbOpsHelper.xmlToElement("<bob>bb</bob>"));
	}

	@Test
	public void testMapToElementStringMapOfStringString() throws Exception {
		Map<String, String> m = new HashMap<String, String>();
		m.put("hi", "jake");
		log.debug("mapToElement :"
		    + DbOpsHelper.elementToString(DbOpsHelper.mapToElement("bob", m)));
	}

	@Test
	public void testMapToElementStringMapOfStringStringListOfString() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("hi", "jake");
		m.put("bigBad", "tom");
		DbOpsHelper.mapToElement("bob", m,
		    Arrays.asList(new String[] { "hi", "jake", "bob" }));
	}

	@Test
	public void testMapToNameValuePairElement() throws Exception {
		Map<String, String> m = new HashMap<String, String>();
		m.put("hi", "jake");
		m.put("big", "billy");
		log.debug("mapToNamevaluePairElement :"
		    + DbOpsHelper.elementToString(DbOpsHelper.mapToNameValuePairElement(
		        "bob", m)));
	}

	@Test
	public void testNameValuePairElementToMap() throws Exception {
		Map<String, String> m = new HashMap<String, String>();
		m.put("hi", "jake");
		m.put("big", "billy");
		Map<String, String> results = DbOpsHelper
		    .nameValuePairElementToMap(DbOpsHelper.mapToNameValuePairElement("bob",
		        m));
		log.debug("nameValuePairElementToMap :" + results);
	}

	@Test
	public void testXmlToElement() throws Exception {
		DbOpsHelper.xmlToElement("<bob hello=\"hi\"></bob>");
	}
}
