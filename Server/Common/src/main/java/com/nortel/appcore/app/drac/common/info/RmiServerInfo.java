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

package com.nortel.appcore.app.drac.common.info;

public final class RmiServerInfo {

  private static final String RMI_HOST_URL = "//"
      + System.getProperty("org.opendrac.controller.primary", "localhost");

  public static final int RMI_REGISTRY_PORT = Integer.getInteger(
      "org.opendrac.rmi.port", 1099);

  public static final int NRB_PORT = Integer.getInteger("org.opendrac.nrb.port",
      8091);

  public static final int NEPROXY_PORT = Integer.getInteger(
      "org.opendrac.neproxy.port", 8090);

  public static final int LPCP_PORT = Integer.getInteger("org.opendrac.lpcp.port",
      8092);

  // Lpcp
  public static final String LPCP_RMI_NAME = "LpcpRemote";
  public static final String LPCP_RMI_BINDING_NAME = RMI_HOST_URL + ":"
      + RMI_REGISTRY_PORT + "/" + LPCP_RMI_NAME;

  // NeProxy
  public static final String NEPROXY_RMI_NAME = "NeProxyRemote";
  public static final String NEPROXY_RMI_BINDING_NAME = RMI_HOST_URL + ":"
      + RMI_REGISTRY_PORT + "/" + NEPROXY_RMI_NAME;

  // Nrb
  public static final String NRB_RMI_NAME = "NRBRemote";
  public static final String NRB_RMI_BINDING_NAME = RMI_HOST_URL + ":"
      + RMI_REGISTRY_PORT + "/" + NRB_RMI_NAME;

}
