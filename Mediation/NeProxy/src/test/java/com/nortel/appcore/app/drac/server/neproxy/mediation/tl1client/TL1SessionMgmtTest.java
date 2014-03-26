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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.IPAddressInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.UserProfile;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.tl1simulator.Tl1Simulator;

/**
 * @author pitman
 */
public final class TL1SessionMgmtTest {
  private static final Logger log = LoggerFactory.getLogger(TL1SessionMgmtTest.class);

	private static Tl1Simulator neSim;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.debug("Creating NE simulator");
		neSim = new Tl1Simulator();
		neSim.startSimulator();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		log.debug("Stopping NE simulator");
		neSim.stopSimulator();
	}

	@Test
	public void testEstablishTL1Session() throws Exception {
		try {
			log.debug("testEstablishTL1Session");
			IPAddressInfo ipInfo = new IPAddressInfo("127.0.0.1", neSim
			    .getActualPortNumbers().get(0).intValue());
			UserProfile uProfile = new UserProfile("admin", CryptoWrapper
			    .INSTANCE.encrypt("admin"));
			NetworkElementInfo ne = new NetworkElementInfo("1232456", "", ipInfo,
			    uProfile);

			TL1SessionMgmt aSession = new TL1SessionMgmt(ne);
			aSession.manageNE();
			try {
				aSession.establishTL1Session();
			}
			catch (NoRouteToHostException nre) {
				throw new Exception("NoRouteToHost", nre);
			}
			catch (IOException ioe) {
				throw new Exception("FailedToConnect", ioe);
			}

			log.debug("testEstablishTL1Session established TL1 session, invoking test methods");

			aSession.getAlarmController();
			aSession.getAssociationController();
			aSession.getTl1Engine();
			aSession.isConnected();

			log.debug("testEstablishTL1Session invoking test methods2");

			aSession.sendSyncMessage(new TL1RequestMessage(Tl1CommandCode.RTRV_HDR,
			    new HashMap<String, String>()));

			log.debug("testEstablishTL1Session invoking test methods2a");
			Map<String, String> m = new HashMap<String, String>();
			m.put(ObjectElement.OPERATION_KEY, Tl1CommandCode.RTRV_NETYPE.toString());
			aSession.sendToNE(m);

			log.debug("testEstablishTL1Session invoking test methods2b");
			aSession.sendManualToNe(m);

			log.debug("testEstablishTL1Session invoking test methods3");

			aSession.logout();
			aSession.reloadNeInfo(ne);
			aSession.deleteTL1Session(false);

		}
		catch (Exception t) {
			log.error("Error: ", t);
			fail("Failure " + t.toString());
		}
	}

	/**
	 * Test method for
	 * {@link com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.TL1SessionMgmt#TL1SessionMgmt(com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTL1SessionMgmt() throws Exception {
		log.debug("testTL1SessionMgmt");

		IPAddressInfo ipInfo = new IPAddressInfo("127.0.0.1", neSim
		    .getActualPortNumbers().get(0).intValue());
		UserProfile uProfile = new UserProfile("admin", CryptoWrapper.INSTANCE
		    .encrypt("admin"));
		NetworkElementInfo ne = new NetworkElementInfo("1232456", "", ipInfo,
		    uProfile);
		TL1SessionMgmt a = new TL1SessionMgmt(ne);
		a.logout();

	}

}
