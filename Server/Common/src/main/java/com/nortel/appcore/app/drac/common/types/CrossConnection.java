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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;

/**
 * Initially a layer 1 construct, but used at layer 0 and 2 to represent a in
 * device or in-switch connection between two or more ports.
 * <p>
 * Originally DRAC stored a Cross connect as a xml format in to the xml
 * database, it then migrated to a Map<String,String> and a xml structure, this
 * attempts to make the cross connect more concrete but still permit different
 * devices to store different data.
 * 
 * @author pitman
 */

public final class CrossConnection implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Map<String, String> map = new HashMap<String, String>();

  // The keys here should align with those used by the database columns!
  public static final String CKTID = DbKeys.NetworkElementConnectionCols.ID;
  public static final String RATE = DbKeys.NetworkElementConnectionCols.RATE;
  public static final String CCT_TYPE = DbKeys.NetworkElementConnectionCols.TYPE;
  public static final String SOURCE_CHANNEL = DbKeys.NetworkElementConnectionCols.SCHANNEL;
  public static final String TARGET_CHANNEL = DbKeys.NetworkElementConnectionCols.TCHANNEL;
  public static final String SOURCE_NEID = DbKeys.NetworkElementConnectionCols.SOURCE;
  public static final String TARGET_NEID = DbKeys.NetworkElementConnectionCols.TARGET;

  public static final String SWMATE_NEID = DbKeys.NetworkElementConnectionCols.SWMATE_NEID;
  public static final String SWMATE_CHANNEL = DbKeys.NetworkElementConnectionCols.SWMATE_CHANNEL;

  // Trouble! Want to use CrossConnection class ubiquitously everywhere ...
  // passed down
  // from user requests, and encapsulating connections discovered from the
  // network.
  // The problem is that the network reports the connection AIDs with the
  // channel appended
  // as well as reports the channel separately (?!). On user requests, it's
  // desirable to
  // keep a copy of the port/facility aid separate ... for the purpose of
  // lookups in the
  // resource model. So ...
  // These are the full XC AIDs that include the channel:
  public static final String SOURCE_XC_AID = DbKeys.NetworkElementConnectionCols.SOURCEAID;
  public static final String TARGET_XC_AID = DbKeys.NetworkElementConnectionCols.TARGETAID;
  public static final String SWMATE_XC_AID = DbKeys.NetworkElementConnectionCols.SWMATEAID;
  // These are just the port/facility AIDs, without the channel:
  public static final String SOURCE_PORT_AID = "sourcePortAid";
  public static final String TARGET_PORT_AID = "targetPortAid";
  public static final String SWMATE_PORT_AID = "swMatePortAid";

  // Not referenced by the database
  public static final String BLSR_AEND = "aEnd";
  public static final String BLSR_ZEND = "zEnd";
  public static final String REMOTEMAC = "REMOTEMAC";
  public static final String VLANID = "vlanId";
  public static final String RATE_IN_MBS = "RateInMbs";
  // see "GG 2009-02-03". Note however that CKTID = "DRAC-" + callid
  public static final String CALLID = "callId";
  public static final String VCATROUTINGOPTION = "VcatRoutingOption";
  public static final String MEDIATION_DATA = "mediationData";

  private boolean fromSideIsL2SS;
  private boolean toSideIsL2SS;

  private static final String DRAC_XCON_PREFIX = "DRAC-";
  private static final String DRAC_XCON_LOOPBACK = "DRAC-loopback";

  /*
   * Set in SchedulingThread used in mediation to bring the facility back to IS
   * when all path connections have been completely created
   */
  private String numberOfConnectionsInPath;

  public CrossConnection(Map<String, String> xc) {
    // Avoid storing entries in the map that are null or empty strings, keeps
    // the crap out.
    // for (Map.Entry<String, String> e : xc.entrySet())
    // {
    // if (e.getKey() != null && !"".equals(e.getKey()))
    // {
    // // Do we want to allow values equal to ""?
    // if (e.getValue() != null && !"".equals(e.getValue()))
    // {
    // map.put(e.getKey(), e.getValue());
    // }
    // }
    // }
    map.putAll(xc);

    /*
     * If the input parameters do not include the channel-based xc aid,
     * construct it. i.e. from user input
     */
    if (!map.containsKey(SOURCE_XC_AID)) {
      if (map.containsKey(SOURCE_PORT_AID) && map.containsKey(SOURCE_CHANNEL)) {
        map.put(SOURCE_XC_AID,
            map.get(SOURCE_PORT_AID) + "-" + map.get(SOURCE_CHANNEL));
      }
    }

    if (!map.containsKey(TARGET_XC_AID)) {
      if (map.containsKey(TARGET_PORT_AID) && map.containsKey(TARGET_CHANNEL)) {
        map.put(TARGET_XC_AID,
            map.get(TARGET_PORT_AID) + "-" + map.get(TARGET_CHANNEL));
      }
    }

    if (!map.containsKey(SWMATE_XC_AID)) {
      if (map.containsKey(SWMATE_PORT_AID) && map.containsKey(SWMATE_CHANNEL)) {
        String swmatePortAid = map.get(SWMATE_PORT_AID);
        String swmateChannel = map.get(SWMATE_CHANNEL);

        if (swmatePortAid != null && swmateChannel != null) {
          map.put(SWMATE_XC_AID,
              map.get(SWMATE_PORT_AID) + "-" + map.get(SWMATE_CHANNEL));
        }
      }
    }

    /*
     * And, vice versa. If the input parameters do not include the port aid,
     * construct it. i.e. from network discovery
     */
    if (!map.containsKey(SOURCE_PORT_AID) && map.containsKey(SOURCE_XC_AID)) {
      String xcAid = map.get(SOURCE_XC_AID);
      map.put(SOURCE_PORT_AID, xcAid.substring(0, xcAid.lastIndexOf('-')));
    }

    if (!map.containsKey(SOURCE_CHANNEL) && map.containsKey(SOURCE_XC_AID)) {
      String xcAid = map.get(SOURCE_XC_AID);

      if (xcAid != null) {
        map.put(SOURCE_CHANNEL,
            xcAid.substring(xcAid.lastIndexOf("-") + 1, xcAid.length()));
      }
    }

    if (!map.containsKey(TARGET_PORT_AID) && map.containsKey(TARGET_XC_AID)) {
      String xcAid = map.get(TARGET_XC_AID);
      map.put(TARGET_PORT_AID, xcAid.substring(0, xcAid.lastIndexOf('-')));
    }

    if (!map.containsKey(TARGET_CHANNEL) && map.containsKey(TARGET_XC_AID)) {
      String xcAid = map.get(TARGET_XC_AID);

      if (xcAid != null) {
        map.put(TARGET_CHANNEL,
            xcAid.substring(xcAid.lastIndexOf("-") + 1, xcAid.length()));
      }
    }

    if (!map.containsKey(SWMATE_PORT_AID) && map.containsKey(SWMATE_XC_AID)) {
      String xcAid = map.get(SWMATE_XC_AID);

      if (xcAid != null) {
        map.put(SWMATE_PORT_AID, xcAid.substring(0, xcAid.lastIndexOf('-')));
      }
    }

    if (!map.containsKey(SWMATE_CHANNEL) && map.containsKey(SWMATE_XC_AID)) {
      String xcAid = map.get(SWMATE_XC_AID);

      if (xcAid != null) {
        map.put(SWMATE_CHANNEL,
            xcAid.substring(xcAid.lastIndexOf("-") + 1, xcAid.length()));
      }
    }

  }

  /**
   * Returns true if and only if the cross connection label starts with "DRAC-"
   * meaning its either a loop back connection we've created to silence alarms
   * or a connection that is part of a service.
   */
  public static boolean isDracConnection(String cktid) {
    if (cktid != null && cktid.startsWith(DRAC_XCON_PREFIX)) {
      return true;
    }
    return false;
  }

  public static boolean isDracLoopback(String cktid) {
    if (cktid != null && cktid.startsWith(DRAC_XCON_LOOPBACK)) {
      return true;
    }
    return false;
  }

  /**
   * Sample output: <edge committed="true" id="DRAC-loopback" rate="STS3C"
   * sChannel="1" sPort="1" sShelf="1" sSlot="1" sSubslot="0"
   * source="00-1B-25-2D-5B-E6" sourceAid="OC12-1-1-1-1" swmate="" tChannel="1"
   * tPort="1" tShelf="1" tSlot="1" tSubslot="0" target="00-1B-25-2D-5B-E6"
   * targetAid="OC12-1-1-1-1" type="1WAY"/>
   */
  public Element asEdge() {
    return DbOpsHelper.mapToElement("edge", map);
  }

  public Map<String, String> asMap() {
    return new HashMap<String, String>(map);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CrossConnection)) {
      return false;
    }
    CrossConnection other = (CrossConnection) obj;
    if (map == null) {
      if (other.map != null) {
        return false;
      }
    }
    else if (!map.equals(other.map)) {
      return false;
    }
    return true;
  }

  public String getBlsrAend() {
    return map.get(BLSR_AEND);
  }

  public String getBlsrZend() {
    return map.get(BLSR_ZEND);
  }

  public String getCallid() {
    return map.get(CALLID);
  }

  public String getId() {
    return map.get(CKTID);
  }

  public String getMediationData() {
    return map.get(MEDIATION_DATA);
  }

  public String getNumberOfConnectionsInPath() {
    return this.numberOfConnectionsInPath;
  }

  public String getRate() {
    return map.get(RATE);
  }

  public String getRateinMbs() {
    return map.get(RATE_IN_MBS);
  }

  public String getRemotemac() {
    return map.get(REMOTEMAC);
  }

  public String getSourceChannel() {
    return map.get(SOURCE_CHANNEL);
  }

  public String getSourceNeId() {
    return map.get(SOURCE_NEID);
  }

  // WITHOUT channel
  public String getSourcePortAid() {
    return map.get(SOURCE_PORT_AID);
  }

  // WITH channel
  public String getSourceXcAid() {
    return map.get(SOURCE_XC_AID);
  }

  public String getSwMateChannel() {
    return map.get(SWMATE_CHANNEL);
  }

  public void setSwMateChannel(final String swmate) {
    map.put(SWMATE_CHANNEL, swmate);
  }

  public String getSwMateNeId() {
    return map.get(SWMATE_NEID);
  }

  // WITHOUT channel
  public String getSwMatePortAid() {
    return map.get(SWMATE_PORT_AID);
  }

  // WITH channel
  public String getSwMateXcAid() {
    return map.get(SWMATE_XC_AID);
  }

  public String getTargetChannel() {
    return map.get(TARGET_CHANNEL);
  }

  public String getTargetNeId() {
    return map.get(TARGET_NEID);
  }

  // WITHOUT channel
  public String getTargetPortAid() {
    return map.get(TARGET_PORT_AID);
  }

  // WITH channel
  public String getTargetXcAid() {
    return map.get(TARGET_XC_AID);
  }

  public String getType() {
    return map.get(CCT_TYPE);
  }

  public String getVcatRoutingOption() {
    return map.get(VCATROUTINGOPTION);
  }

  public String getVlanId() {
    return map.get(VLANID);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (map == null ? 0 : map.hashCode());
    return result;
  }

  public boolean isDracConnection() {
    return isDracConnection(getId());
  }

  public boolean isDracLoopback() {
    return isDracLoopback(getId());
  }

  public boolean isFromSideL2SS() {
    return this.fromSideIsL2SS;
  }

  public boolean isToSideL2SS() {
    return this.toSideIsL2SS;
  }

  public void setFromSideL2SS() {
    this.fromSideIsL2SS = true;
  }

  public void setMediationData(String data) {
    map.put(CrossConnection.MEDIATION_DATA, data);
  }

  public void setNumberOfConnectionsInPath(String num) {
    this.numberOfConnectionsInPath = num;
  }

  public void setToSideL2SS() {
    this.toSideIsL2SS = true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("CrossConnection [map=");
    // use a tree map so we see the attributes in order
    builder.append(new TreeMap<String, String>(map));
    builder.append("]");
    builder.append(" fromSideL2SS=" + isFromSideL2SS());
    builder.append(" toSideL2SS=" + isToSideL2SS());
    return builder.toString();
  }

}
