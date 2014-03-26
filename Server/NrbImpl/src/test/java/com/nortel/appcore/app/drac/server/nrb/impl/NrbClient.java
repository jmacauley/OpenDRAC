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

package com.nortel.appcore.app.drac.server.nrb.impl;

import java.net.InetAddress;
import java.rmi.Naming;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;

public final class NrbClient {

  private static final Logger log = LoggerFactory.getLogger(NrbClient.class);

  private static String rmiURI = ":" + RmiServerInfo.RMI_REGISTRY_PORT
      + "/" + RmiServerInfo.NRB_RMI_NAME;

  private NrbClient() {
  }

  /**
   * Simple stand alone client to make queries on the NRB_PORT.
   * 
   * @param args
   */
  public static void main(String[] args) {
    try {
      log.debug("Starting");
      String lookup = "//" + args[0] + rmiURI;
      log.debug("Looking up " + lookup);
      NrbInterface nrb = (NrbInterface) Naming.lookup(lookup);
      log.debug("Looked up remote end got " + nrb);

      String ip = null;
      try {
        InetAddress address = InetAddress.getLocalHost();
        ip = address.getHostAddress();
      }
      catch (Exception e) {
        log.error("Error: ", e);
      }

      LoginToken token = nrb.login(ClientLoginType.INTERNAL_LOGIN, args[1],
          args[2].toCharArray(), ip, null, "123");

      // active
      List<Schedule> schedules = nrb.getActiveSchedules(token);
      log.debug("Active Schedules: " + schedules);
      for (Schedule s : schedules) {
        log.debug("Schedule :" + s.toDebugString());
      }

      // all
      schedules = nrb.getSchedules(token, null);
      log.debug("getSchedules: " + schedules);
      for (Schedule s : schedules) {
        log.debug("Schedule :" + s.toDebugString());
      }
      log.debug("getServices: " + nrb.getServices(token, null));

      nrb.logout(token);
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
    log.debug("Done");
  }

}
