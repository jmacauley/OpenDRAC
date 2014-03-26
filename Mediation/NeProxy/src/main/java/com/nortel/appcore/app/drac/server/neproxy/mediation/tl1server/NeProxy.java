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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlEvent;
import com.nortel.appcore.app.drac.server.neproxy.NeProxyEventServer;
import com.nortel.appcore.app.drac.server.neproxy.rmi.NeProxyEvent;

/**
 * Created on Jul 29, 2005
 * 
 * @author nguyentd
 */
public enum NeProxy {

  INSTANCE(System
      .getProperty("org.opendrac.proxy.terminal.id", "DEFAULT_PROXY"));

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String tid;

  private NeProxy(String targetId) {
    tid = targetId;
  }

  public static void generateEvent(Tl1XmlEvent anEvent, String eventType) {
    NeProxyEvent e = new NeProxyEvent(buildEventHeader(anEvent, eventType));
    NeProxyEventServer.INSTANCE.publishEvent(e);
  }

  private static String buildEventHeader(Tl1XmlEvent anEvent, String eventType) {
    Element root = new Element("message");
    root.addContent(anEvent.getRootNode().detach());
    root.setAttribute("type", "report");
    root.setAttribute("aTag", "REPLACEATAG");
    root.setAttribute("command", eventType);
    return new XMLOutputter(Format.getCompactFormat()).outputString(root);

    // ClientMessageXML message = new ClientMessageXML();
    // message.addEventData(anEvent.getRootNode(), eventType);
    // return message.rootNodeToString();
  }

  /**
   * @return the tid
   */
  public String getTid() {
    return tid;
  }

}
