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

package com.nortel.appcore.app.drac.server.neproxy;

import static org.junit.Assert.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginToken;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.OperationNotSupportedException;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.helper.test.DbTestPopulateDb;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEvent;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEventCallback;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyInterface;
import com.nortel.appcore.app.drac.sshclisimulator.SshCliSimulator;
import com.nortel.appcore.app.drac.tl1simulator.Tl1Simulator;

public final class NeFactoryTest {
  private static final Logger log = LoggerFactory.getLogger(NeFactoryTest.class);
	public static class EventListener implements NeProxyEventCallback {
		public EventListener() throws RemoteException {
			UnicastRemoteObject.exportObject(this);
		}

		@Override
		public void neProxyEventReceived(NeProxyEvent event) throws RemoteException {
			log.debug("Received event " + event);
		}
	}

	private static Tl1Simulator neSim;
	private static Thread neFactoryThread;
	private static NeProxyInterface neProxy;
	private static InternalLoginToken token;
	private static SshCliSimulator ssh1;

	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		TestHelper.INSTANCE.initialize();
		new NeProxyLauncher().start();

		neSim = new Tl1Simulator(
		    Tl1Simulator.OME6_CPL3_LONGTID_OME7L2_NETWORK_RANDOM_PORTS);
		neSim.startSimulator();


		/*
		 * Populate our database with records we can use to exercise with, if using
		 * the simulator skip NE data otherwise populate NE data as well.
		 */
		DbTestPopulateDb.populateTestSystem(false);

		/**
		 * Add NEs that point at our simulator.
		 */
		List<Integer> simulatedNePortList = neSim.getActualPortNumbers();
		for (Integer nePort : simulatedNePortList) {
			// Values marked as unknown will be discovered when the NE 'associates'
			DbNetworkElement.INSTANCE.add(
			    new NetworkElementHolder("127.0.0.1", nePort.toString(), "ADMIN",
			        NeType.UNKNOWN, "UNKNOWN", NeStatus.NE_UNKNOWN, null,
			        CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
			        NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
			        PROTOCOL_TYPE.NETL1_PROTOCOL, true, "Unknown", "UNKNOWN", null, null));
		}

		if (ssh1 != null) {
			for (Integer p : ssh1.getInusePorts()) {
				DbNetworkElement.INSTANCE.add(
				    new NetworkElementHolder("127.0.0.1", Integer.toString(p), "ADMIN",
				        NeType.FORCE10, "UNKNOWN", NeStatus.NE_UNKNOWN, null,
				        CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
				        NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
				        PROTOCOL_TYPE.FORCE10_PROTOCOL, true, "Unknown", "UNKNOWN", null, null));
			}
		}

		neFactoryThread = new Thread() {
			@Override
			public void run() {
				setName("NeFactory Thread");
				log.debug("Starting NeFactory running");
				new NeProxyLauncher().start();
			}
		};
		neFactoryThread.setDaemon(true);
		neFactoryThread.start();

		Thread.sleep(1000);

		int tries = 0;
		while (tries < 15) {
			try {
				neProxy = (NeProxyInterface) Naming.lookup(RmiServerInfo.NEPROXY_RMI_BINDING_NAME);
				token = InternalLoginHelper.INSTANCE.getToken(InternalLoginTokenType.NEPROXY);
				neProxy.isAlive(token);
				log.debug("NeFactory is running!");
				break;
			}
			catch (Exception e) {
				log.debug("Could not talk to NeProxy, sleeping and retrying ", e);
				Thread.sleep(2 * 1000);
			}
			tries++;
		}

		/*
		 * In order to do some useful tests, bring the Neproxy up and running and
		 * wait until it has had a chance to align the NEs its managing. We poll to
		 * determine when its done to be sure.
		 */

		while (true) {
			List<NetworkElementHolder> neList = neProxy.getNetworkElements(token,
			    null);
			if (neList == null || neList.isEmpty()) {
				//
			}
			else {
				boolean allAligned = true;
				for (NetworkElementHolder ne : neList) {
					if (!NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
						allAligned = false;
					}
				}
				if (allAligned) {
					log.debug("Waiting for NeProxy " + neList.size()
					    + " Nes are now aligned ");
					break;
				}
				log.debug("Waiting for NeProxy not all NEs are aligned " + neList);

				// It appears to take about 12 seconds to align, on my machine, sleep a
				// bit more
				// Longer on mine: sleep even longer here
				Thread.sleep(10 * 1000);
			}
		}
	}

	@AfterClass
	public static void tearDownAfterTests() throws Exception {
		neFactoryThread.interrupt();
		neSim.stopSimulator();
		Thread.sleep(1 * 1000);
	}

	
	@Before
	public void setup() throws Exception {
		neProxy.registerForEventNotifications(token, new EventListener());
	}

	@Test
	public void testAddDeleteAdjacency() throws Exception {
		neProxy.addManualAdjacency(token, "00-21-E1-D6-D6-70", "bog",
		    "00-1B-25-2C-E9-6E", "bog");
		neProxy.deleteManualAdjacency(token, "00-21-E1-D6-D6-70", "bog",
		    "00-1B-25-2C-E9-6E", "bog");
	}

	@Test
	public void testAddDeleteNetworkElement() throws Exception {
		NetworkElementHolder ne = new NetworkElementHolder("127.0.0.1", "80",
		    "admin", NeType.UNKNOWN, "", NeStatus.NE_UNKNOWN, null, CryptoWrapper
		        .INSTANCE.encrypt("admin"), 1, NETWORK_ELEMENT_MODE.Unknown,
		    "", "", PROTOCOL_TYPE.NETL1_PROTOCOL, true, "Unknown", "", null, null);
		neProxy.enrollNetworkElement(token, ne);
		neProxy.deleteNetworkElement(token, ne);
	}

	@Test
	public void testEditFacility() throws Exception {
		TestHelper.INSTANCE.initialize();

		boolean doneOne = false;
		for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null)) {
			if (NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
				try {
					List<Facility> facs = neProxy.getFacilities(token, ne.getId());

					if (facs == null || facs.isEmpty()) {
						fail("Could not find a facility to edit!");
						break;
					}
					Facility f = facs.get(0);
					neProxy.editFacility(token, f.getNeId(), f.getAid(), f.getTna(),
					    f.getUserLabel(), f.getMtu(), f.getSrlg(), f.getGroup(),
					    f.getCost(), f.getMetric2(), f.getSigType(), f.getConstraint(),
					    f.getDomain(), f.getSite());
					doneOne = true;
					break;
				}
				catch (Exception e) {
					log.debug(
					    "Failed to edit a facility, could be a test setup problem, will try more",
					    e);
				}
			}
		}
		if (!doneOne) {
			fail("Failed to edit a NE facility of an NE");
		}
	}

	@Test
	public void testGetCrossConnections() throws Exception {
		TestHelper.INSTANCE.initialize();

		boolean doneOne = false;
		for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null)) {
			if (NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
				List<CrossConnection> result = neProxy.getCrossConnections(token,
				    ne.getId());
				
				doneOne = true;
				break;
			}
		}

		if (!doneOne) {
			fail("Failed to fetch getCrossConnections from the NE");
		}
	}

	@Test
	public void testGetFacilities() throws Exception {
		log.debug("Facilities :" + neProxy.getFacilities(token, "00-21-E1-D6-D6-70"));

		try {
			neProxy.getFacilities(token, "non-existant");
			org.junit.Assert.fail("expected this to fail");
		}
		catch (Exception e) {
			// Ok expected this to fail.
			neProxy.isAlive(token);
		}
	}

	// @Test
	// public void testGetXmlAdjacency()
	// throws Exception
	// {
	// if (TestHelper.disabled())
	// {
	// return;
	// }
	//
	// boolean doneOne = false;
	// for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null))
	// {
	// if (NeStatus.NE_ALIGNED.toString().equals(ne.getStatus()))
	// {
	// String result = neProxy.getXmlAdjacency(token, ne.getId());
	// 
	// doneOne = true;
	// break;
	// }
	// }
	// if (!doneOne)
	// {
	// fail("Failed to fetch adjacency from the NE");
	// }
	// }

	@Test
	public void testGetNetworkElements() throws Exception {
		List<NetworkElementHolder> neList = neProxy.getNetworkElements(token, null);
		if (neList == null || neList.isEmpty()) {
			fail("Expected a non empty NE list");
			return;
		}
		List<NetworkElementHolder> single = neProxy.getNetworkElements(token,
		    neList.get(0).getId());
		if (single == null || single.size() != 1) {
			fail("Expected single NE ");
		}

		try {
			neProxy.getNetworkElements(token, "-no-way-this-should-match-anything");
			fail("expected this to fail!");
		}
		catch (Exception e) {
			// Ok expected this to fail.
			neProxy.isAlive(token);
		}
	}

	@Test
	public void testGetXmlAlarm() throws Exception {
		TestHelper.INSTANCE.initialize();

		boolean doneOne = false;
		for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null)) {
			if (NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
				try {
					String result = neProxy.getXmlAlarm(token, ne.getId());
					
					doneOne = true;
					// break;
				}
				catch (OperationNotSupportedException onse) {
					log.debug("getXMLAlarm not supported for this ne, no problem", onse);
				}
				catch (Exception e) {
					log.error("failed to fetch alarms for NE", e);
				}
			}
		}
		if (!doneOne) {
			fail("Failed to fetch alarms from the NE");
		}
	}

	@Test
	public void testIsAlive() throws Exception {
		neProxy.isAlive(token);
	}

	@Test
	public void testToggleNetworkElementAssociation() throws Exception {
		TestHelper.INSTANCE.initialize();

		boolean doneOne = false;
		for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null)) {
			if (NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
				try {
					neProxy.toggleNetworkElementAssociation(token, ne);
					doneOne = true;
					break;
				}
				catch (OperationNotSupportedException onse) {
					log.debug(
					    "toggleNetworkElementAssociation not supported for this ne, no problem",
					    onse);
				}
			}
		}
		if (!doneOne) {
			fail("Failed to find/toggle the association of an NE");
		}
	}

	/**
	 * Odd method name, we want the "z" test cases to run at the end as they alter
	 * the state of the network and will cause other tests to fail or behave
	 * oddly. Not sure if junit runs the tests in alphabetical order or class
	 * order, but since I have eclipse sort methods in the class file, either
	 * approach yields the same order.
	 */
	@Test
	public void ztestChangeNetworkElementPassword() throws Exception {
		TestHelper.INSTANCE.initialize();

		boolean doneOne = false;
		for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null)) {
			if (NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
				neProxy.changeNetworkElementPassword(token, ne);
				doneOne = true;
				break;
			}
		}

		if (!doneOne) {
			fail("Failed to changeNetworkElementPassword of an NE");
		}
	}

	/**
	 * Odd method name, we want the "z" test cases to run at the end as they alter
	 * the state of the network and will cause other tests to fail or behave
	 * oddly. Not sure if junit runs the tests in alphabetical order or class
	 * order, but since I have eclipse sort methods in the class file, either
	 * approach yields the same order.
	 */
	@Test
	public void zTestCreateConnection() throws Exception {
		TestHelper.INSTANCE.initialize();

		boolean doneOne = false;
		for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null)) {
			if (NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
				Map<String, String> xcMap = new HashMap<String, String>();
				xcMap.put(CrossConnection.SOURCE_NEID, ne.getId());
				xcMap.put(CrossConnection.TARGET_NEID, ne.getId());
				xcMap.put(CrossConnection.SOURCE_PORT_AID, "OC192-1-1-1");
				xcMap.put(CrossConnection.TARGET_PORT_AID, "OC48-1-1");
				xcMap.put(CrossConnection.RATE, "STS1");
				xcMap.put(CrossConnection.CKTID, "UserLabel");
				xcMap.put(CrossConnection.SOURCE_CHANNEL, "1");
				xcMap.put(CrossConnection.TARGET_CHANNEL, "1");
				xcMap.put(CrossConnection.CCT_TYPE, "2WAY");

				try {
					ProvisioningResultHolder resp = neProxy.createConnection(token,
					    new CrossConnection(xcMap));
					if (resp.getExceptionText() == null) {
						log.debug("create connection worked: " + resp.toString());
						doneOne = true;
						break;
					}
					log.error("Failed to create connection " + resp.toString());
				}
				catch (Exception e) {
					log.error("Failed to create connection ", e);
				}
			}
		}

		if (!doneOne) {
			fail("Failed to create connection from the NE");
		}
	}

	/**
	 * Odd method name, we want the "z" test cases to run at the end as they alter
	 * the state of the network and will cause other tests to fail or behave
	 * oddly. Not sure if junit runs the tests in alphabetical order or class
	 * order, but since I have eclipse sort methods in the class file, either
	 * approach yields the same order.
	 */
	@Test
	public void zTestDeleteConnection() throws Exception {
		TestHelper.INSTANCE.initialize();

		boolean doneOne = false;
		for (NetworkElementHolder ne : neProxy.getNetworkElements(token, null)) {
			if (NeStatus.NE_ALIGNED.equals(ne.getNeStatus())) {
				Map<String, String> xcMap = new HashMap<String, String>();
				xcMap.put(CrossConnection.SOURCE_NEID, ne.getId());
				xcMap.put(CrossConnection.TARGET_NEID, ne.getId());
				xcMap.put(CrossConnection.SOURCE_PORT_AID, "OC192-1-1-1");
				xcMap.put(CrossConnection.TARGET_PORT_AID, "OC48-1-1");
				xcMap.put(CrossConnection.RATE, "STS1");
				xcMap.put(CrossConnection.CKTID, "UserLabel");
				xcMap.put(CrossConnection.SOURCE_CHANNEL, "1");
				xcMap.put(CrossConnection.TARGET_CHANNEL, "1");
				xcMap.put(CrossConnection.CCT_TYPE, "2WAY");

				try {
					ProvisioningResultHolder resp = neProxy.deleteConnection(token,
					    new CrossConnection(xcMap));
					if (resp.getExceptionText() == null) {
						log.debug("delete connection worked: " + resp.toString());
						doneOne = true;
						break;
					}
					log.error("Failed to delete connection " + resp.toString());
				}
				catch (Exception e) {
					log.error("Failed to delete connection ", e);
				}
			}
		}

		if (!doneOne) {
			fail("Failed to delete connection from the NE");
		}
	}

}
