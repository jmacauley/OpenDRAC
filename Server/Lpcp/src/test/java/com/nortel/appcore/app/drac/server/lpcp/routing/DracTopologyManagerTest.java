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

package com.nortel.appcore.app.drac.server.lpcp.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.NetworkElementAdjacency;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.server.lpcp.routing.TopologyManager.DracGraph;

public class DracTopologyManagerTest {
  
  private final Logger log = LoggerFactory.getLogger(getClass());

	@Before
	public void setup() {
		TestHelper.INSTANCE.initialize();
	}

	@Test
	public void testConsolidate() throws Exception {
		String[][] adj = new String[][] {
		    // format key, ne, aid, transmit, receive, layer, ismanual
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-6-101_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-6-101",
		        "00-21-E1-D9-AA-1B_OC192-1-6-101",
		        "00-21-E1-D9-AC-20_OC192-1-6-101", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-6-102_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-6-102",
		        "00-21-E1-D9-AC-20_OC192-1-6-102",
		        "00-21-E1-D9-AA-1B_OC192-1-6-102", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-6-102_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-6-102",
		        "00-21-E1-D9-AA-1B_OC192-1-6-102",
		        "00-21-E1-D9-AC-20_OC192-1-6-102", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-24-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-24-1",
		        "00-21-E1-D9-AC-20_OC192-1-24-1", "00-20-D8-DF-3C-4B_OC192-1-6-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-6-101_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-6-101",
		        "00-21-E1-D9-AC-20_OC192-1-6-101",
		        "00-21-E1-D9-AA-1B_OC192-1-6-101", "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-88_OC192-1-10-1_manualLayrType",
		        "00-20-D8-DF-37-88", "OC192-1-10-1",
		        "00-20-D8-DF-37-88_OC192-1-10-1", "00-14-0D-03-4F-38_OC192-1-4-1",
		        "manualLayrType", "1" },
		    new String[] { "00-14-0D-03-50-34_OC192-1-14-1_manualLayrType",
		        "00-14-0D-03-50-34", "OC192-1-14-1",
		        "00-14-0D-03-50-34_OC192-1-14-1", "00-1B-25-2D-DA-65_OC192-1-9-1",
		        "manualLayrType", "1" },
		    new String[] { "00-14-0D-03-4F-38_OC192-1-4-1_manualLayrType",
		        "00-14-0D-03-4F-38", "OC192-1-4-1",
		        "00-14-0D-03-4F-38_OC192-1-4-1", "00-20-D8-DF-37-88_OC192-1-10-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-30-F2_OC192-1-5-1_manualLayrType",
		        "00-20-D8-DF-30-F2", "OC192-1-5-1",
		        "00-20-D8-DF-30-F2_OC192-1-5-1",
		        "00-60-38-de-81-5d_OC192-1-501-0-1-1", "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-30-F2_OC192-1-11-1_manualLayrType",
		        "00-20-D8-DF-30-F2", "OC192-1-11-1",
		        "00-20-d8-df-30-f2_OC192-1-11-1", "00-1b-25-2b-68-7c_OC192-1-10-1",
		        "manualLayrType", "1" },
		    new String[] { "00-11-58-FF-85-45_OC192-1-14-1_manualLayrType",
		        "00-11-58-FF-85-45", "OC192-1-14-1",
		        "00-11-58-FF-85-45_OC192-1-14-1", "00-1B-25-2B-69-A4_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-33-86_OC192-1-10-1_manualLayrType",
		        "00-20-D8-DF-33-86", "OC192-1-10-1",
		        "00-20-D8-DF-33-86_OC192-1-10-1",
		        "00-60-38-de-81-5d_OC192-1-503-0-3-1", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-18-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-18-1",
		        "00-21-E1-D9-AC-20_OC192-1-18-1", "00-21-E1-D9-CC-70_OC192-1-18-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-28-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-28-1",
		        "00-21-E1-D9-AC-20_OC192-1-28-1", "00-20-D8-DF-3B-FB_OC192-1-6-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-18-1_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-18-1",
		        "00-21-E1-D9-CC-70_OC192-1-18-1", "00-21-E1-D9-AC-20_OC192-1-18-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3C-37_OC192-1-13-1_manualLayrType",
		        "00-20-D8-DF-3C-37", "OC192-1-13-1",
		        "00-20-D8-DF-3C-37_OC192-1-13-1", "00-20-D8-DF-37-8D_OC192-1-10-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-8D_OC192-1-10-1_manualLayrType",
		        "00-20-D8-DF-37-8D", "OC192-1-10-1",
		        "00-20-D8-DF-37-8D_OC192-1-10-1", "00-20-D8-DF-3C-37_OC192-1-13-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-18-1_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-18-1",
		        "00-21-E1-D9-AA-1B_OC192-1-18-1", "00-21-E1-D9-D2-7F_OC192-1-18-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-18-1_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-18-1",
		        "00-21-E1-D9-D2-7F_OC192-1-18-1", "00-21-E1-D9-AA-1B_OC192-1-18-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-A1_OC192-1-9-1_manualLayrType",
		        "00-20-D8-DF-37-A1", "OC192-1-9-1",
		        "00-20-D8-DF-37-A1_OC192-1-9-1", "00-20-D8-DF-37-88_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-6-104_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-6-104",
		        "00-21-E1-D9-CC-70_OC192-1-6-104",
		        "00-21-E1-D9-D2-7F_OC192-1-6-104", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-6-104_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-6-104",
		        "00-21-E1-D9-D2-7F_OC192-1-6-104",
		        "00-21-E1-D9-CC-70_OC192-1-6-104", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-6-103_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-6-103",
		        "00-21-E1-D9-AC-20_OC192-1-6-103",
		        "00-21-E1-D9-AA-1B_OC192-1-6-103", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-6-103_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-6-103",
		        "00-21-E1-D9-AA-1B_OC192-1-6-103",
		        "00-21-E1-D9-AC-20_OC192-1-6-103", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-1-1_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-1-1",
		        "00-21-e1-d9-aa-1b_OC192-1-1-1", "00-21-e1-d9-d2-7f_OC192-1-1-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-1-1_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-1-1",
		        "00-21-e1-d9-d2-7f_OC192-1-1-1", "00-21-e1-d9-aa-1b_OC192-1-1-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-1-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-1-1",
		        "00-21-e1-d9-ac-20_OC192-1-1-1", "00-21-e1-d9-cc-70_OC192-1-1-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-1-1_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-1-1",
		        "00-21-e1-d9-cc-70_OC192-1-1-1", "00-21-e1-d9-ac-20_OC192-1-1-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-38-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-38-1",
		        "00-21-e1-d9-ac-20_OC192-1-38-1", "00-21-e1-d9-cc-70_OC192-1-38-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-38-1_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-38-1",
		        "00-21-e1-d9-cc-70_OC192-1-38-1", "00-21-e1-d9-ac-20_OC192-1-38-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-38-1_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-38-1",
		        "00-21-e1-d9-aa-1b_OC192-1-38-1", "00-21-e1-d9-d2-7f_OC192-1-38-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-38-1_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-38-1",
		        "00-21-e1-d9-d2-7f_OC192-1-38-1", "00-21-e1-d9-aa-1b_OC192-1-38-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-6-104_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-6-104",
		        "00-21-E1-D9-AC-20_OC192-1-6-104",
		        "00-21-E1-D9-AA-1B_OC192-1-6-104", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-6-104_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-6-104",
		        "00-21-E1-D9-AA-1B_OC192-1-6-104",
		        "00-21-E1-D9-AC-20_OC192-1-6-104", "manualLayrType", "1" },
		    new String[] { "00-14-0D-03-4F-38_OC192-1-6-1_manualLayrType",
		        "00-14-0D-03-4F-38", "OC192-1-6-1",
		        "00-14-0d-03-4f-38_OC192-1-6-1", "00-21-e1-d9-cc-70_OC192-1-27-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-32-83_OC192-1-11-1_manualLayrType",
		        "00-20-D8-DF-32-83", "OC192-1-11-1",
		        "00-20-D8-DF-32-83_OC192-1-11-1", "00-1B-25-2B-69-A4_OC192-1-6-1",
		        "manualLayrType", "1" },
		    new String[] { "00-14-0D-03-50-34_OC192-1-13-1_manualLayrType",
		        "00-14-0D-03-50-34", "OC192-1-13-1",
		        "00-14-0D-03-50-34_OC192-1-13-1", "00-17-D1-FF-C2-8E_OC192-1-6-1",
		        "manualLayrType", "1" },
		    new String[] { "00-1B-25-2B-69-A4_OC192-1-6-1_manualLayrType",
		        "00-1B-25-2B-69-A4", "OC192-1-6-1",
		        "00-1B-25-2B-69-A4_OC192-1-6-1", "00-20-D8-DF-32-83_OC192-1-11-1",
		        "manualLayrType", "1" },
		    new String[] { "00-17-D1-FF-C2-8E_OC192-1-6-1_manualLayrType",
		        "00-17-D1-FF-C2-8E", "OC192-1-6-1",
		        "00-17-D1-FF-C2-8E_OC192-1-6-1", "00-14-0D-03-50-34_OC192-1-13-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-28-1_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-28-1",
		        "00-21-E1-D9-AA-1B_OC192-1-28-1", "00-20-D8-DF-3B-FB_OC192-1-11-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3B-FB_OC192-1-11-1_manualLayrType",
		        "00-20-D8-DF-3B-FB", "OC192-1-11-1",
		        "00-20-D8-DF-3B-FB_OC192-1-11-1", "00-21-E1-D9-AA-1B_OC192-1-28-1",
		        "manualLayrType", "1" },
		    new String[] { "00-1B-25-2B-69-A4_OC192-1-5-1_manualLayrType",
		        "00-1B-25-2B-69-A4", "OC192-1-5-1",
		        "00-1B-25-2B-69-A4_OC192-1-5-1", "00-11-58-FF-85-45_OC192-1-14-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-88_OC192-1-5-1_manualLayrType",
		        "00-20-D8-DF-37-88", "OC192-1-5-1",
		        "00-20-D8-DF-37-88_OC192-1-5-1", "00-20-D8-DF-37-A1_OC192-1-9-1",
		        "manualLayrType", "1" },
		    new String[] { "00-11-58-FF-85-45_OC192-1-4-1_manualLayrType",
		        "00-11-58-FF-85-45", "OC192-1-4-1",
		        "00-11-58-FF-85-45_OC192-1-4-1", "00-20-D8-DF-3B-FB_OC192-1-14-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3B-FB_OC192-1-14-1_manualLayrType",
		        "00-20-D8-DF-3B-FB", "OC192-1-14-1",
		        "00-20-D8-DF-3B-FB_OC192-1-14-1", "00-11-58-FF-85-45_OC192-1-4-1",
		        "manualLayrType", "1" },
		    new String[] { "00-1B-25-2D-DA-65_OC192-1-9-1_manualLayrType",
		        "00-1B-25-2D-DA-65", "OC192-1-9-1",
		        "00-1B-25-2D-DA-65_OC192-1-9-1", "00-14-0D-03-50-34_OC192-1-14-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-6-102_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-6-102",
		        "00-21-E1-D9-CC-70_OC192-1-6-102",
		        "00-21-E1-D9-D2-7F_OC192-1-6-102", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-6-102_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-6-102",
		        "00-21-E1-D9-D2-7F_OC192-1-6-102",
		        "00-21-E1-D9-CC-70_OC192-1-6-102", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-6-103_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-6-103",
		        "00-21-E1-D9-D2-7F_OC192-1-6-103",
		        "00-21-E1-D9-CC-70_OC192-1-6-103", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-6-103_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-6-103",
		        "00-21-E1-D9-CC-70_OC192-1-6-103",
		        "00-21-E1-D9-D2-7F_OC192-1-6-103", "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-6-101_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-6-101",
		        "00-21-E1-D9-D2-7F_OC192-1-6-101",
		        "00-21-E1-D9-CC-70_OC192-1-6-101", "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3C-50_OC192-1-5-1_manualLayrType",
		        "00-20-D8-DF-3C-50", "OC192-1-5-1",
		        "00-20-D8-DF-3C-50_OC192-1-5-1", "00-1B-25-2B-69-A4_OC192-1-9-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-6-101_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-6-101",
		        "00-21-E1-D9-CC-70_OC192-1-6-101",
		        "00-21-E1-D9-D2-7F_OC192-1-6-101", "manualLayrType", "1" },
		    new String[] { "00-11-58-FF-85-45_OC192-1-11-1_manualLayrType",
		        "00-11-58-FF-85-45", "OC192-1-11-1",
		        "00-11-58-ff-85-45_OC192-1-11-1", "00-1b-25-2b-6d-8b_OC192-1-11-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3C-2D_OC192-1-10-1_manualLayrType",
		        "00-20-D8-DF-3C-2D", "OC192-1-10-1",
		        "00-20-D8-DF-3C-2D_OC192-1-10-1", "00-11-58-FF-85-45_OC192-1-12-1",
		        "manualLayrType", "1" },
		    new String[] { "00-11-58-FF-85-45_OC192-1-12-1_manualLayrType",
		        "00-11-58-FF-85-45", "OC192-1-12-1",
		        "00-11-58-FF-85-45_OC192-1-12-1", "00-20-D8-DF-3C-2D_OC192-1-10-1",
		        "manualLayrType", "1" },
		    new String[] { "00-1B-25-2B-69-A4_OC192-1-9-1_manualLayrType",
		        "00-1B-25-2B-69-A4", "OC192-1-9-1",
		        "00-1B-25-2B-69-A4_OC192-1-9-1", "00-20-D8-DF-3C-50_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-21-1_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-21-1",
		        "00-21-E1-D9-CC-70_OC192-1-21-1", "00-21-E1-D9-AC-20_OC192-1-21-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-21-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-21-1",
		        "00-21-E1-D9-AC-20_OC192-1-21-1", "00-21-E1-D9-CC-70_OC192-1-21-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-CC-70_OC192-1-34-1_manualLayrType",
		        "00-21-E1-D9-CC-70", "OC192-1-34-1",
		        "00-21-E1-D9-CC-70_OC192-1-34-1", "00-20-D8-DF-37-8D_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-8D_OC192-1-5-1_manualLayrType",
		        "00-20-D8-DF-37-8D", "OC192-1-5-1",
		        "00-20-D8-DF-37-8D_OC192-1-5-1", "00-21-E1-D9-CC-70_OC192-1-34-1",
		        "manualLayrType", "1" },
		    new String[] { "00-1B-25-2B-6D-8B_OC192-1-11-1_manualLayrType",
		        "00-1B-25-2B-6D-8B", "OC192-1-11-1",
		        "00-1b-25-2b-6d-8b_OC192-1-11-1", "00-11-58-ff-85-45_OC192-1-11-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-A1_OC192-1-5-1_manualLayrType",
		        "00-20-D8-DF-37-A1", "OC192-1-5-1",
		        "00-20-D8-DF-37-A1_OC192-1-5-1", "00-20-D8-DF-37-D8_OC192-1-10-1",
		        "manualLayrType", "1" },
		    new String[] { "00-14-0D-03-4F-38_OC192-1-13-1_manualLayrType",
		        "00-14-0D-03-4F-38", "OC192-1-13-1",
		        "00-14-0d-03-4f-38_OC192-1-13-1", "00-20-d8-df-3c-1e_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-D8_OC192-1-10-1_manualLayrType",
		        "00-20-D8-DF-37-D8", "OC192-1-10-1",
		        "00-20-D8-DF-37-D8_OC192-1-10-1", "00-20-D8-DF-37-A1_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3B-F6_OC192-1-11-1_manualLayrType",
		        "00-20-D8-DF-3B-F6", "OC192-1-11-1",
		        "00-20-D8-DF-3B-F6_OC192-1-11-1", "00-1B-25-2B-69-A4_OC192-1-4-1",
		        "manualLayrType", "1" },
		    new String[] { "00-1B-25-2B-69-A4_OC192-1-4-1_manualLayrType",
		        "00-1B-25-2B-69-A4", "OC192-1-4-1",
		        "00-1B-25-2B-69-A4_OC192-1-4-1", "00-20-D8-DF-3B-F6_OC192-1-11-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-7-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-7-1",
		        "00-21-E1-D9-AC-20_OC192-1-7-1", "00-20-D8-DF-3D-7A_OC192-1-6-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-21-1_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-21-1",
		        "00-21-E1-D9-AA-1B_OC192-1-21-1", "00-21-E1-D9-D2-7F_OC192-1-21-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-D2-7F_OC192-1-21-1_manualLayrType",
		        "00-21-E1-D9-D2-7F", "OC192-1-21-1",
		        "00-21-E1-D9-D2-7F_OC192-1-21-1", "00-21-E1-D9-AA-1B_OC192-1-21-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-11-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-11-1",
		        "00-21-E1-D9-AC-20_OC192-1-11-1", "00-20-D8-DF-37-47_OC192-1-6-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AC-20_OC192-1-27-1_manualLayrType",
		        "00-21-E1-D9-AC-20", "OC192-1-27-1",
		        "00-21-E1-D9-AC-20_OC192-1-27-1", "00-14-0D-03-50-34_OC192-1-9-1",
		        "manualLayrType", "1" },
		    new String[] { "00-14-0D-03-50-34_OC192-1-9-1_manualLayrType",
		        "00-14-0D-03-50-34", "OC192-1-9-1",
		        "00-14-0D-03-50-34_OC192-1-9-1", "00-21-E1-D9-AC-20_OC192-1-27-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3C-1E_OC192-1-10-1_manualLayrType",
		        "00-20-D8-DF-3C-1E", "OC192-1-10-1",
		        "00-20-d8-df-3c-1e_OC192-1-10-1", "00-20-d8-df-37-d3_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-11-58-FF-8A-F6_OC192-1-5-1_manualLayrType",
		        "00-11-58-FF-8A-F6", "OC192-1-5-1",
		        "00-11-58-FF-8A-F6_OC192-1-5-1", "00-60-38-DF-9F-1A_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-60-38-DF-9F-1A_OC192-1-5-1_manualLayrType",
		        "00-60-38-DF-9F-1A", "OC192-1-5-1",
		        "00-60-38-DF-9F-1A_OC192-1-5-1", "00-11-58-FF-8A-F6_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-21-E1-D9-AA-1B_OC192-1-32-1_manualLayrType",
		        "00-21-E1-D9-AA-1B", "OC192-1-32-1",
		        "00-21-E1-D9-AA-1B_OC192-1-32-1", "00-20-D8-DF-3C-2D_OC192-1-11-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-3C-2D_OC192-1-11-1_manualLayrType",
		        "00-20-D8-DF-3C-2D", "OC192-1-11-1",
		        "00-20-D8-DF-3C-2D_OC192-1-11-1", "00-21-E1-D9-AA-1B_OC192-1-32-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-51_OC192-1-10-1_manualLayrType",
		        "00-20-D8-DF-37-51", "OC192-1-10-1",
		        "00-20-D8-DF-37-51_OC192-1-10-1", "00-20-D8-DF-37-D8_OC192-1-5-1",
		        "manualLayrType", "1" },
		    new String[] { "00-20-D8-DF-37-D8_OC192-1-5-1_manualLayrType",
		        "00-20-D8-DF-37-D8", "OC192-1-5-1",
		        "00-20-D8-DF-37-D8_OC192-1-5-1", "00-20-D8-DF-37-51_OC192-1-10-1",
		        "manualLayrType", "1" },
		    new String[] { "00-17-D1-FF-C2-8E_OC192-1-9-1_manualLayrType",
		        "00-17-D1-FF-C2-8E", "OC192-1-9-1",
		        "00-17-D1-FF-C2-8E_OC192-1-9-1", "00-1B-25-2D-DA-65_OC192-1-6-1",
		        "manualLayrType", "1" },
		    new String[] { "00-1B-25-2D-DA-65_OC192-1-6-1_manualLayrType",
		        "00-1B-25-2D-DA-65", "OC192-1-6-1",
		        "00-1B-25-2D-DA-65_OC192-1-6-1", "00-17-D1-FF-C2-8E_OC192-1-9-1",
		        "manualLayrType", "1" },

		    // Empty tx/rx tag test

		    new String[] { "00-1B-25-2D-DA-65_OC192-1-6-9999_manualLayrType",
		        "00-1B-25-2D-DA-65", "OC192-1-6-9999",
		        "00-1B-25-2D-DA-65_OC192-1-6-9999", "", "manualLayrType", "1" },

		    // duplicate tx or rx tag test
		    new String[] { "00-1B-25-2D-DA-65_OC192-1-6-8888_manualLayrType",
		        "00-1B-25-2D-DA-65", "OC192-1-6-8888",
		        "00-1B-25-2D-DA-65_OC192-1-6-8888",
		        "00-17-D1-FF-C2-8E_OC192-1-9-8888", "manualLayrType", "1" },
		    new String[] { "00-1B-25-2D-DA-65_OC192-1-6-8888_manualLayrType",
		        "00-1B-25-2D-DA-65", "OC192-1-6-8888",
		        "00-1B-25-2D-DA-65_OC192-1-6-8888",
		        "00-17-D1-FF-C2-8E_OC192-1-9-8888", "manualLayrType", "1" },

		    // manual and network discovered sets.
		    new String[] { "", "a", "a", "a_a1", "b_b1", "manualLayrType", "1" }, //
		    new String[] { "", "b", "b", "b_b1", "a_a1", "manualLayrType", "1" },//
		    new String[] { "", "a", "a", "a_a", "b_b", "other", "0" },//
		    new String[] { "", "b", "b", "b_b", "a_a", "other", "0" },

		    // two sets of links from the same port!
		    new String[] { "", "c", "fake-1-1-1", "c_c1", "d_d1", "manualLayrType",
		        "1" },//
		    new String[] { "", "d", "fake-1-1-1", "d_d1", "c_c1", "manualLayrType",
		        "1" }, //
		    new String[] { "", "c", "fake-1-1-1", "e_e1", "f_f1", "manualLayrType",
		        "1" }, //
		    new String[] { "", "f", "fake-1-1-1", "f_f1", "e_e1", "manualLayrType",
		        "1" },//
		//
		};

		Set<String> neset = new TreeSet<String>();
		for (String[] i : adj) {
			neset.add(i[1]);
		}

		List<NetworkElementHolder> nes = new ArrayList<NetworkElementHolder>();
		for (String i : neset) {
			// generate fake NEs with the correct Ieee address
			nes.add(new NetworkElementHolder(i, "10000", "admin", NeType.OME7, i,
			    NeStatus.NE_ALIGNED, "", new CryptedString(""), 1,
			    NETWORK_ELEMENT_MODE.SONET, "", i, PROTOCOL_TYPE.NETL1_PROTOCOL,
			    true, "", null, null));
		}

		List<NetworkElementAdjacency> adjList = new ArrayList<NetworkElementAdjacency>();
		for (String[] a : adj) {
			adjList.add(new NetworkElementAdjacency(a[1], a[2], a[3], a[4], a[5],
			    a[6].equals("1")));
		}

		log.debug("Creating consolidated graph from " + nes.size()
		    + " Network elements and " + adjList.size() + " raw links ");
		DracGraph g = TopologyManager.INSTANCE.consolidate(nes,
		    new ArrayList<NetworkElementAdjacency>(adjList));
		log.debug("Got graph with " + g.getGraph().getVertexCount()
		    + " vertices and " + g.getGraph().getEdgeCount() + " edges ");
		log.debug("edges: " + g.getGraph().getEdges());

		// run again this time the graphs should not be changed and we don't try
		// to generate any events.

		// reset our adjacencies so they will consolidate
		for (NetworkElementAdjacency a : adjList) {
			a.setFlag(false);
		}
		g = TopologyManager.INSTANCE.consolidate(nes,
		    new ArrayList<NetworkElementAdjacency>(adjList));
	}

}
