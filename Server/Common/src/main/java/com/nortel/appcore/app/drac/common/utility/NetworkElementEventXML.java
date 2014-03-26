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

package com.nortel.appcore.app.drac.common.utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkElementEventXML {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String EVENT_INFO_ELEMENT = "eventInfo";
  public static final String DATA_ELEMENT = "data";
  public static final String NOTIFICATION_TYPE_ATTR = "notificationType";

  public static final String IPADDR_ATTR = "ip";
  public static final String PORT_ATTR = "port";
  public static final String STATUS_ATTR = "status";

  // NetworkElementEvent node attributes
  public static final String ID_ATTR = "id";
  public static final String TID_ATTR = "tid";

  // Input data node attributes
  public static final String OPERATION_ATTR = "operation";
  public static final String NAME_KEY = "NAME";
  public static final String VALUE_KEY = "VALUE";

  public Element root;

  public HashMap<String, String> getInputDataMap() {
    HashMap<String, String> data = new HashMap<String, String>();
    Element dataElem = root.getChild(DATA_ELEMENT);
    if (dataElem != null) {
      
    }
    Iterator<?> ir = dataElem.getChildren().iterator();
    while (ir.hasNext()) {
      Element element = (Element) ir.next();
      if (element != null) {
        log.debug("DARRYL: " + element + " "
            + element.getAttributeValue(NAME_KEY) + " "
            + element.getAttributeValue(VALUE_KEY));
        data.put(element.getAttributeValue(NAME_KEY),
            element.getAttributeValue(VALUE_KEY));
      }
    }
    return data;
  }

  public Map<String, String> getNodeMap() {
    try {
      final Map<String, String> map = new HashMap<String, String>();
      Element nodeElement = root.getChild(ClientMessageXml.NODE_ELEMENT);
      if (nodeElement != null) {
        map.put(TID_ATTR, nodeElement.getAttributeValue(TID_ATTR));
        map.put(ID_ATTR, nodeElement.getAttributeValue(ID_ATTR));
        map.put(IPADDR_ATTR, nodeElement.getAttributeValue(IPADDR_ATTR));
        map.put(PORT_ATTR, nodeElement.getAttributeValue(PORT_ATTR));
        map.put(STATUS_ATTR, nodeElement.getAttributeValue(STATUS_ATTR));
      }
      return map;
    }
    catch (Exception e) {
      log.error("Error: (returning empty map)", e);
      return new HashMap<String, String>();
    }

  }

  public String getNotificationType() {
    String type = "";
    Element eventInfo = root.getChild(EVENT_INFO_ELEMENT);
    if (eventInfo != null) {
      type = eventInfo.getAttributeValue(NOTIFICATION_TYPE_ATTR);
    }
    return type;
  }

}
