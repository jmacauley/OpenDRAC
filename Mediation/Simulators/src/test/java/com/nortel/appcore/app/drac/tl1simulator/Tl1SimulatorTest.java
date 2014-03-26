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

package com.nortel.appcore.app.drac.tl1simulator;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.Socket;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pitman
 */
public final class Tl1SimulatorTest {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Test
  public void testTl1Simulator() {
    try {
      Tl1Simulator cs = new Tl1Simulator();
      cs.startSimulator();

      Thread.sleep(1000 * 2);
      Socket s = new Socket(InetAddress.getLocalHost(), cs
          .getActualPortNumbers().get(0).intValue());
      s.getOutputStream().write("RTRV-SWVER:OME0039::5;".getBytes());
      s.getOutputStream().flush();
      Thread.sleep(1000 * 2);
      s.close();
      cs.stopSimulator();
    }
    catch (Exception e) {
      log.error("Error: ", e);
      fail("See stacktarce");
    }
  }

  @Test
  public void testTl1SimulatorEvents() {
    try {
      Tl1Simulator cs = new Tl1Simulator("!!!!OME6-OME0039:0");
      cs.startSimulator();
      log.debug("Running port numbers are " + cs.getActualPortNumbers());
      Socket s = new Socket(InetAddress.getLocalHost(), cs
          .getActualPortNumbers().get(0).intValue());
      s.getOutputStream().write("RTRV-SWVER:OME0307::5;".getBytes());
      s.getOutputStream().flush();
      Thread.sleep(1000 * 4);
      cs.stopSimulator();
    }
    catch (Exception e) {
      log.error("Error: ", e);
      fail("See stacktarce");
    }
  }

}
