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

package com.nortel.appcore.app.drac.server;

import java.rmi.Naming;
import java.util.List;

import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.PROTOCOL_TYPE;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElement;
import com.nortel.appcore.app.drac.database.helper.test.DbTestPopulateDb;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.lpcp.Lpcp;
import com.nortel.appcore.app.drac.server.neproxy.NeProxyLauncher;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;
import com.nortel.appcore.app.drac.server.nrb.NrbInterfaceUtils;
import com.nortel.appcore.app.drac.server.nrb.impl.NrbLauncher;
import com.nortel.appcore.app.drac.sshclisimulator.SshCliSimulator;
import com.nortel.appcore.app.drac.tl1simulator.Tl1Simulator;

public final class IntegrationTestHelper {
  private static final Logger log = LoggerFactory.getLogger(IntegrationTestHelper.class);
	private static NeProxyLauncher neProxyLauncher;
	private static NrbLauncher nrbLauncher;
	private static Thread lcpThread;
	private static Thread neFactoryThread;
	private static Tl1Simulator neSim;
	private static boolean useSimulator;
	private static SshCliSimulator ssh1;

	// Used for exercising the Lpcp socket-based requests (all of which call into
	// the Lpcp INSTANCE)
	private static Lpcp lpcpInstance;

	private IntegrationTestHelper() {
	}

	public synchronized static Lpcp getLpcp() {
		/*
		 * This has to be deferred until the rmi port is selected and set via
		 * org.opendrac.rmi.port
		 */
		if (lpcpInstance == null) {
			lpcpInstance = new Lpcp(true);
		}
		return lpcpInstance;
	}

	/**
	 * Set up the server for an integration test. After calling this method, the
	 * database will be populated with fake system data, a rmi name service will
	 * be running on a port number defined by the system property
	 * "org.opendrac.controller.primary", the NRB_PORT will be running on a randomly selected
	 * port number, but its INSTANCE bound into the RMI server, the LPCP_PORT,
	 * redundancy and upgrade services will be up and running on there regular
	 * ports (or failing if those port numbers are already in use).
	 * 
	 * @throws Exception
	 */
	public static void setUpServerForIntergrationTests(boolean runSimulators,
	    String[] args) throws Exception {

		System.setProperty("org.opendrac.controller.primary", "localhost");
		System.setProperty("org.opendrac.controller.secondary", "localhost");

		useSimulator = runSimulators;

		TestHelper.INSTANCE.initialize();


		if (useSimulator) {
			neSim = new Tl1Simulator(
			    Tl1Simulator.OME6_CPL3_LONGTID_OME7L2_NETWORK_RANDOM_PORTS);
			// neSim = new
			// Tl1Simulator(Tl1Simulator.THREE_NODE_NETWORK_FIXED_PORTS);
			neSim.startSimulator();
		}


		/*
		 * Populate our database with records we can use to exercise with, if using
		 * the simulator skip NE data otherwise populate NE data as well.
		 */
		DbTestPopulateDb.populateTestSystem(!useSimulator);

		if (useSimulator) {
			/**
			 * Add NEs that point at our simulator.
			 */
			List<Integer> simulatedNePortList = neSim.getActualPortNumbers();
			for (Integer nePort : simulatedNePortList) {
				// Values marked as unknown will be discovered when the NE 'associates'
				NetworkElementHolder ne = new NetworkElementHolder("127.0.0.1",
				    nePort.toString(), "ADMIN", NeType.UNKNOWN, "UNKNOWN",
				    NeStatus.NE_UNKNOWN, null, CryptoWrapper.INSTANCE.encrypt(
				        "ADMIN"), 1, NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
				    PROTOCOL_TYPE.NETL1_PROTOCOL, true, "UNKNOWN", null, null);
				DbNetworkElement.INSTANCE.add(ne);
			}

			if (ssh1 != null) {
				for (Integer p : ssh1.getInusePorts()) {
					DbNetworkElement.INSTANCE.add(
					    new NetworkElementHolder("127.0.0.1", Integer.toString(p),
					        "ADMIN", NeType.UNKNOWN, "UNKNOWN", NeStatus.NE_UNKNOWN,
					        null, CryptoWrapper.INSTANCE.encrypt("ADMIN"), 1,
					        NETWORK_ELEMENT_MODE.Unknown, "DEFAULT_PROXY", "",
					        PROTOCOL_TYPE.FORCE10_PROTOCOL, true, "UNKNOWN", null, null));
				}
			}
		}

		/**
		 * Start Mediation running
		 */

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

		/**
		 * Start the LPCP_PORT running.
		 */
		lcpThread = new Thread() {
			@Override
			public void run() {
				setName("LPCP_PORT thread");
				log.debug("Starting LPCP_PORT running");
				getLpcp().start();
			}
		};
		lcpThread.setDaemon(true);
		lcpThread.start();



		neProxyLauncher = new NeProxyLauncher();
		neProxyLauncher.start();
		
		nrbLauncher = new NrbLauncher();
		nrbLauncher.startup();


		/*
		 * The NRB_PORT now spins until NeProxy and LPCP_PORT are up and running and won't
		 * bind itself until they are up and running, as a result, we just need to
		 * wait for the NRB_PORT to be up and running and we are good to go...
		 */

		log.debug("Looking up NRB_PORT");
		NrbInterfaceUtils.blockingWaitForNrbInterfaceToStart("localhost");

		log.debug("NRB_PORT is alive!");

		NrbInterface nrb = (NrbInterface) Naming.lookup(RmiServerInfo.NRB_RMI_BINDING_NAME);
		nrb.isAlive(LoginToken.getStaticToken());

		@SuppressWarnings("unused")
		LoginToken token = nrb.login(ClientLoginType.INTERNAL_LOGIN, "admin",
		    "myDrac".toCharArray(), "127.0.0.1", null, "123");

		// if (useSimulator)
		// {
		// /*
		// * wait for LPCP_PORT to report the same number of NEs as are in the
		// database... one way to making sure
		// * lpcp is up and really running.
		// */
		// int numNes = nrb.getNetworkElements(token, null).size();
		// GraphData gd = null;
		// while (gd == null || gd.getVertices().size() != numNes ||
		// gd.getEclipsedEdges().size() == 0)
		// {
		// Thread.sleep(3000);
		// gd = nrb.getGraphData(token);
		// }
		// }

		log.debug("Server setup complete");
	}

	/**
	 * @throws Exception
	 */
	public static void tearDownServerAfterTests() throws Exception {
		neProxyLauncher.stop();
		nrbLauncher.shutdown();
		lcpThread.interrupt();
		neFactoryThread.interrupt();
		if (useSimulator) {
			neSim.stopSimulator();
			if (ssh1 != null) {
				ssh1.stopSshServer(true);
			}
		}
	}

}
