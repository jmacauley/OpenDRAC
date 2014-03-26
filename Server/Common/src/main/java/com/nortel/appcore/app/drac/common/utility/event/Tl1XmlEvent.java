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

package com.nortel.appcore.app.drac.common.utility.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;

/**
 * Created on Aug 12, 2005
 * 
 * @author nguyentd
 */
public abstract class Tl1XmlEvent {
  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final String EVENT_NODE = "event";
  public static final String EVENT_NODE_ATTR_NAME = "name";
  public static final String EVENT_NODE_ATTR_ID = "id";
  public static final String EVENT_NODE_ATTR_TIME = "time";
  public static final String EVENT_NODE_ATTR_DURATION = "duration";
  public static final String EVENT_NODE_ATTR_OWNER = "owner";

  public static final String NEINFO_NODE = "node";
  public static final String NEINFO_NODE_ATTR_TYPE = "type";
  public static final String NEINFO_NODE_ATTR_ID = "id";
  public static final String NEINFO_NODE_ATTR_STATUS = "status";
  public static final String NEINFO_NODE_ATTR_IP = "ip";
  public static final String NEINFO_NODE_ATTR_PORT = "port";
  public static final String NEINFO_NODE_ATTR_TID = "tid";
  public static final String NEINFO_NODE_ATTR_MODE = "mode";

  public static final String EVENTINFO_NODE = "eventInfo";
  public static final String EVENTINFO_NODE_ATTR_NOTIFICATIONTYPE = "notificationType";
  public static final String EVENTINFO_NODE_ATTR_OCCRDATE = "occurredDate";
  public static final String EVENTINFO_NODE_ATTR_OCCRTIME = "occurredTime";

  public static final String DATA_NODE = "data";

  public static final String ELEMENT_NODE = "element";
  public static final String ELEMENT_NODE_ATTR_NAME = "name";
  public static final String ELEMENT_NODE_ATTR_VALUE = "value";

  public Element root;
  public Element neInfoNode;
  public Element descriptionNode;
  public Element dataNode;
  public Element eventInfoNode;
  public Element eventNode;
  private Map<String, Element> nodeList;
  public AbstractNetworkElement ne;

  public Tl1XmlEvent(AbstractNetworkElement networkElement, String eventType) {
    try {
      root = new Element("NetworkElementEvent");
      nodeList = new HashMap<String, Element>();
      eventNode = createEventNode(eventType);

      neInfoNode = new Element(Tl1XmlEvent.NEINFO_NODE);
      if (networkElement != null) {
        neInfoNode.setAttribute(Tl1XmlEvent.NEINFO_NODE_ATTR_TYPE,
            networkElement.getNeType().toString());
        neInfoNode.setAttribute(Tl1XmlEvent.NEINFO_NODE_ATTR_ID,
            networkElement.getNeId());
        neInfoNode.setAttribute(Tl1XmlEvent.NEINFO_NODE_ATTR_IP,
            networkElement.getIpAddress());
        neInfoNode.setAttribute(Tl1XmlEvent.NEINFO_NODE_ATTR_PORT,
            Integer.toString(networkElement.getPortNumber()));
        neInfoNode.setAttribute(Tl1XmlEvent.NEINFO_NODE_ATTR_TID,
            networkElement.getNeName());
        neInfoNode.setAttribute(Tl1XmlEvent.NEINFO_NODE_ATTR_MODE,
            networkElement.getNeMode().toString());
        neInfoNode.setAttribute(Tl1XmlEvent.NEINFO_NODE_ATTR_STATUS,
            networkElement.getNeStatus().getStateString());
      }

      eventNode.addContent(neInfoNode);
      root.addContent(eventNode);
      nodeList.put(NEINFO_NODE, neInfoNode);

    }
    catch (Exception e) {
      log.error("Failed to initialize the XML", e);
    }
  }

  public Tl1XmlEvent(Element root) {
    this.root = root;
  }

  public void addDataElement(Map<String, String> data) {
    Iterator<String> ir = data.keySet().iterator();
    while (ir.hasNext()) {
      String key = ir.next();
      String value = data.get(key);
      if (value == null) {
        value = "N/A";
      }
      try {
        Element anElement = new Element(Tl1XmlEvent.ELEMENT_NODE);
        anElement.setAttribute(Tl1XmlEvent.ELEMENT_NODE_ATTR_NAME, key);
        anElement.setAttribute(Tl1XmlEvent.ELEMENT_NODE_ATTR_VALUE, value);
        dataNode.addContent(anElement);
      }
      catch (Exception e) {
        log.error("Failed to add data", e);
      }
    }
  }

  public void addDataElement(String name, String value) {
    try {
      Element anElement = new Element(Tl1XmlEvent.ELEMENT_NODE);
      anElement.setAttribute(Tl1XmlEvent.ELEMENT_NODE_ATTR_NAME, name);
      anElement.setAttribute(Tl1XmlEvent.ELEMENT_NODE_ATTR_VALUE, value);
      dataNode.addContent(anElement);
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  public String eventNodeToString() {
    XMLOutputter outXml = new XMLOutputter(Format.getCompactFormat());
    return outXml.outputString(eventNode);
  }

  public Map<String, String> getEventData() {
    Map<String, String> data = new HashMap<String, String>();
    @SuppressWarnings("unchecked")
    List<Element> dataElement = root.getChild(DATA_NODE).getChildren();
    for (int i = 0; i < dataElement.size(); i++) {
      String key = dataElement.get(i).getAttributeValue(ELEMENT_NODE_ATTR_NAME);
      String value = dataElement.get(i).getAttributeValue(
          ELEMENT_NODE_ATTR_VALUE);
      data.put(key, value);
    }
    return data;
  }

  public String getEventId() {
    return root.getAttributeValue(Tl1XmlEvent.EVENT_NODE_ATTR_ID);
  }

  public String getEventTime() {
    return root.getAttributeValue(Tl1XmlEvent.EVENT_NODE_ATTR_TIME);
  }

  public String getNotificationType() {
    return root.getChild(EVENTINFO_NODE).getAttributeValue(
        EVENTINFO_NODE_ATTR_NOTIFICATIONTYPE);
  }

  public String getOccurrentDate() {
    return eventInfoNode.getAttributeValue(EVENTINFO_NODE_ATTR_OCCRDATE);
  }

  public String getOccurrentTime() {
    return eventInfoNode.getAttributeValue(EVENTINFO_NODE_ATTR_OCCRTIME);
  }

  public Element getRootNode() {
    return root;
  }

  public String getSourceNodeId() {
    return root.getChild(NEINFO_NODE).getAttributeValue(NEINFO_NODE_ATTR_ID);
  }

  public void setEventId(String id) {
    if (id != null) {
      eventNode.setAttribute(Tl1XmlEvent.EVENT_NODE_ATTR_ID, id);
    }

  }

  public void setEventTime() {
    eventNode.setAttribute(Tl1XmlEvent.EVENT_NODE_ATTR_TIME,
        Long.toString(System.currentTimeMillis()));

  }

  public void setNotificationType(String type) {
    eventInfoNode.setAttribute(EVENTINFO_NODE_ATTR_NOTIFICATIONTYPE, type);
  }

  public void setOccurrentDate(String date) {
    if (date != null) {
      eventInfoNode.setAttribute(EVENTINFO_NODE_ATTR_OCCRDATE, date);
    }
  }

  public void setOccurrentTime(String time) {
    if (time != null) {
      eventInfoNode.setAttribute(EVENTINFO_NODE_ATTR_OCCRTIME, time);
    }
  }

  public void setOwnerId(String id) {
    if (id != null) {
      eventNode.setAttribute(Tl1XmlEvent.EVENT_NODE_ATTR_OWNER, id);
    }
  }

  @Override
  public String toString() {
    try {

      return new XMLOutputter(Format.getCompactFormat()).outputString(root);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return "Tl1XmlEvent: Unable to build string representation "
          + e.toString();
    }
  }

  public void updateDescription(String description) {
    if (description != null) {
      descriptionNode.setAttribute(Tl1XmlEvent.ELEMENT_NODE_ATTR_VALUE,
          description);
    }
  }

  public void updateNeInfo(String status) {
    neInfoNode.setAttribute(NEINFO_NODE_ATTR_TYPE, ne.getNeType().toString());
    neInfoNode.setAttribute(NEINFO_NODE_ATTR_ID, ne.getNeId());
    neInfoNode.setAttribute(NEINFO_NODE_ATTR_IP, ne.getIpAddress());
    neInfoNode.setAttribute(NEINFO_NODE_ATTR_PORT,
        Integer.toString(ne.getPortNumber()));
    neInfoNode.setAttribute(NEINFO_NODE_ATTR_TID, ne.getNeName());
    neInfoNode.setAttribute(NEINFO_NODE_ATTR_MODE, ne.getNeMode().toString());
    neInfoNode.setAttribute(NEINFO_NODE_ATTR_STATUS, status);
  }

  /**
   * @param eventType
   */
  protected Element createEventNode(String eventType) {
    if (nodeList == null) {
      nodeList = new HashMap<String, Element>();
    }
    eventNode = new Element(Tl1XmlEvent.EVENT_NODE);
    eventNode.setAttribute(Tl1XmlEvent.EVENT_NODE_ATTR_NAME, eventType);
    eventNode.setAttribute(Tl1XmlEvent.EVENT_NODE_ATTR_ID, "N/A");
    eventNode.setAttribute(Tl1XmlEvent.EVENT_NODE_ATTR_OWNER, "N/A");
    eventNode.setAttribute(Tl1XmlEvent.EVENT_NODE_ATTR_TIME,
        Long.toString(System.currentTimeMillis()));
    nodeList.put(EVENT_NODE, eventNode);

    eventInfoNode = new Element(Tl1XmlEvent.EVENTINFO_NODE);
    eventInfoNode.setAttribute(
        Tl1XmlEvent.EVENTINFO_NODE_ATTR_NOTIFICATIONTYPE, "N/A");
    eventInfoNode.setAttribute(Tl1XmlEvent.EVENTINFO_NODE_ATTR_OCCRDATE, "N/A");
    eventInfoNode.setAttribute(Tl1XmlEvent.EVENTINFO_NODE_ATTR_OCCRTIME, "N/A");
    eventNode.addContent(eventInfoNode);
    nodeList.put(EVENTINFO_NODE, eventInfoNode);

    dataNode = new Element(Tl1XmlEvent.DATA_NODE);
    descriptionNode = new Element(Tl1XmlEvent.ELEMENT_NODE);
    descriptionNode.setAttribute(Tl1XmlEvent.ELEMENT_NODE_ATTR_NAME,
        "description");
    descriptionNode.setAttribute(Tl1XmlEvent.ELEMENT_NODE_ATTR_VALUE, "N/A");
    dataNode.addContent(descriptionNode);
    eventNode.addContent(dataNode);
    nodeList.put(DATA_NODE, dataNode);
    return eventNode;
  }
}
