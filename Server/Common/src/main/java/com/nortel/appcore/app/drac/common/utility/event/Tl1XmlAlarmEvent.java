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

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;

/**
 * Created on Aug 12, 2005
 * 
 * @author nguyentd
 */
public final class Tl1XmlAlarmEvent extends Tl1XmlEvent {
  // public static final String ALARM_CRITICAL = "critical";
  // public static final String ALARM_MAJOR = "major";
  // public static final String ALARM_MINOR = "minor";
  // public static final String ALARM_WARNING = "warning";
  private static final String AID_KEY = "aid";
  private static final String FACILITY_KEY = "facility";
  public static final String SERVICEID_KEY = "serviceId";
  private static final String CHANNEL_KEY = "channel";
  public static final String CLEAR_SEVERITY = "CL";
  public static final String CRITICAL_SEVERITY = "CR";
  public static final String MAJOR_SEVERITY = "MJ";
  public static final String MINOR_SEVERITY = "MN";

  // This constructor should have the root node as
  // <NetworkElementEvent><event>...</event><backslashNetworkElementEvent>
  public Tl1XmlAlarmEvent(AbstractNetworkElement ne) {
    super(ne, "alarm");
    this.ne = ne;
    root.getChild(EVENT_NODE).setAttribute(EVENT_NODE_ATTR_DURATION, "0");
  }

  // This constructor should have the root node as <event>...</event>
  public Tl1XmlAlarmEvent(Element root) {
    super(root);
    root.setAttribute(EVENT_NODE_ATTR_DURATION, "0");
    dataNode = root.getChild(Tl1XmlEvent.DATA_NODE);
  }

  public String getAlarmAid() {
    return getValue(AID_KEY);
  }

  public String getAlarmChannel() {
    return getValue(CHANNEL_KEY);
  }

  public List<String> getAlarmFacility() {
    return getValueList(FACILITY_KEY);
  }

  public String getAlarmSeverity() {
    return root.getChild(EVENTINFO_NODE).getAttributeValue(
        EVENTINFO_NODE_ATTR_NOTIFICATIONTYPE);
  }

  // public boolean isRaised()
  // {
  // if (getAlarmSeverity().equalsIgnoreCase(CLEAR_SEVERITY))
  // {
  // return false;
  // }
  // else
  // {
  // return true;
  // }
  // }

  public void setAlarmAid(String aid) {
    if (aid != null) {
      addDataElement(AID_KEY, aid);
    }
  }

  public void setAlarmSeverity(String severity) {
    if (severity != null) {
      setNotificationType(severity);
    }
  }

  public void setChannelNumber(String aChannel) {
    addDataElement(CHANNEL_KEY, aChannel);

  }

  public void setFacilityType(String aFacility) {
    addDataElement(FACILITY_KEY, aFacility);
  }

  private String getValue(String key) {
    if (dataNode != null) {
      List<Element> childen = dataNode.getChildren();
      for (int i = 0; i < childen.size(); i++) {
        Element elm = childen.get(i);
        String temp = elm.getAttributeValue(ELEMENT_NODE_ATTR_NAME);
        if (temp.equalsIgnoreCase(key)) {
          return elm.getAttributeValue(ELEMENT_NODE_ATTR_VALUE);
        }
      }
    }
    return null;
  }

  private List<String> getValueList(String key) {
    List<String> result = new ArrayList<String>();
    if (dataNode != null) {
      List<Element> childen = dataNode.getChildren();
      for (int i = 0; i < childen.size(); i++) {
        Element elm = childen.get(i);
        String temp = elm.getAttributeValue(ELEMENT_NODE_ATTR_NAME);
        if (temp.equalsIgnoreCase(key)) {
          result.add(elm.getAttributeValue(ELEMENT_NODE_ATTR_VALUE));
        }
      }
    }
    return result;
  }
}
