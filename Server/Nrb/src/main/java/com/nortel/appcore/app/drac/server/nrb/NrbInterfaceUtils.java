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

package com.nortel.appcore.app.drac.server.nrb;

import java.rmi.Naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.info.RmiServerInfo;

/**
 * NRB_PORT helper stuff
 * 
 * @author pitman
 */
public final class NrbInterfaceUtils {

  private static final Logger log = LoggerFactory
      .getLogger(NrbInterfaceUtils.class);

  private NrbInterfaceUtils() {
    super();
  }

  public static void blockingWaitForNrbInterfaceToStart(String controllerIp)
      throws Exception {
    while (!isNrbInterfaceAlive(controllerIp)) {
      Thread.sleep(1 * 5000L);
    }
  }

  public static boolean isNrbInterfaceAlive(String controllerIp) {
    final String bindName = "//" + controllerIp + ":"
        + RmiServerInfo.RMI_REGISTRY_PORT + "/" + RmiServerInfo.NRB_RMI_NAME;
    try {
      
      NrbInterface nrb = (NrbInterface) Naming.lookup(bindName);
      
      return nrb == null ? false : true;
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return false;
    }
  }
}
