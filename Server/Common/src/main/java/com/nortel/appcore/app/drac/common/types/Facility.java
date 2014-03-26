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
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.FacilityConstants.SIGNAL_TYPE;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.db.DbOpsHelper;

/**
 * A facility (aka a port, aka an endpoint) holder. A facility has a number of
 * manditory and fixed fields, the rest are optional or product specific. We
 * store a facility in the database as a string in xml format. This class helps
 * carry the resulting record around.
 * 
 * @author pitman
 */
public final class Facility implements Serializable {
  private static final long serialVersionUID = 1L;
  private final TreeMap<String, String> map = new TreeMap<String, String>();

  /*
   * Build a facility from a NE and a map of attributes. NE related attributes
   * are added to the map.
   */
  public Facility(AbstractNetworkElement ne, Map<String, String> facilityMap)
      throws Exception {
    this(addInNe(ne, facilityMap));
  }

  /**
   * @param facilityMap
   */
  public Facility(Map<String, String> facilityMap) throws Exception {
    map.putAll(facilityMap);
    // include some pretty tight checking to make sure we don't have junk.
    validateFacility();
  }

  public static String getFacRateDisplay(String aid) {
    if (Facility.isEth(aid)) {
      if (aid.indexOf("ETH10G") >= 0) {
        return FacilityConstants.tenGE;
      }
      else if (aid.indexOf("ETH100") >= 0) {
        return FacilityConstants.FE;
      }
      else if (aid.indexOf("ETH") >= 0) {
        return FacilityConstants.GE;
      }
    }

    return null;
  }

  /*
   * Consolidate all aid 'is' methods to this one location
   */
  public static boolean isEth(String aid) {
    if (aid != null) {
      // Return true for all ETH variants: ETH, ETH10G, ETH100, ...
      return aid.indexOf("ETH") >= 0;
    }
    return false;
  }

  public static boolean isL2(String aid) {
    return Facility.isEth(aid) || Facility.isWan(aid);
  }

  public static boolean isWan(String aid) {
    if (aid != null) {
      return aid.indexOf("WAN") >= 0;
    }
    return false;
  }

  private static Map<String, String> addInNe(AbstractNetworkElement ne,
      Map<String, String> facilityMap) {
    Map<String, String> m = new TreeMap<String, String>(facilityMap);
    m.put(DbKeys.NetworkElementFacilityCols.FAC_NEID, ne.getNeId());
    m.put(DbKeys.NetworkElementFacilityCols.NEIP_FOR_FAC, ne.getIpAddress());
    m.put(DbKeys.NetworkElementFacilityCols.NEPORT_FOR_FAC,
        Integer.toString(ne.getPortNumber()));
    return m;
  }

  public Element asElement() {
    /*
     * Historically the xml version of a facility was named by the layer.
     * <layer0 attr=value /> we keep that going here.
     */
    Map<String, String> tmpMap = new TreeMap<String, String>(map);
    String rootLayer = tmpMap.get(DbKeys.NetworkElementFacilityCols.LAYER);
    tmpMap.remove(DbKeys.NetworkElementFacilityCols.LAYER);
    return DbOpsHelper.mapToElement(rootLayer, tmpMap);
  }

  public Map<String, String> asUnmodifiableMap() {
    return Collections.unmodifiableSortedMap(map);
  }

  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Facility)) {
      return false;
    }
    Facility other = (Facility) obj;
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

  public String get(String key) {
    return map.get(key);
  }

  public String getAid() {
    return map.get(DbKeys.NetworkElementFacilityCols.AID);
  }

  public String getApsid() {
    return map.get(DbKeys.NetworkElementFacilityCols.APSID);
  }

  public String getCardPec() {
    return map.get(FacilityConstants.CARDPEC_ATTR);
  }

  public String getCardType() {
    return map.get(FacilityConstants.CARDTYPE_ATTR);
  }

  public String getConstraint() {
    return map.get(DbKeys.NetworkElementFacilityCols.CONSTRAIN);
  }

  public String getCost() {
    return map.get(DbKeys.NetworkElementFacilityCols.COST);
  }

  public String getDomain() {
    return map.get(DbKeys.NetworkElementFacilityCols.DOMAIN);
  }

  public String getGroup() {
    return map.get(DbKeys.NetworkElementFacilityCols.GROUP);
  }

  public Layer getLayer() {
    // return map.get(DbKeys.NetworkElementFacilityCols.LAYER);
    return Layer.toEnum(map.get(DbKeys.NetworkElementFacilityCols.LAYER));
  }

  public String getMetric2() {
    return map.get(DbKeys.NetworkElementFacilityCols.METRIC);
  }

  public String getMtu() {
    return map.get(DbKeys.NetworkElementFacilityCols.MTU);
  }

  public String getNeId() {
    return map.get(DbKeys.NetworkElementFacilityCols.FAC_NEID);
  }

  public String getNeIp() {
    return map.get(DbKeys.NetworkElementFacilityCols.NEIP_FOR_FAC);
  }

  public String getNePort() {
    return map.get(DbKeys.NetworkElementFacilityCols.NEPORT_FOR_FAC);
  }

  public String getPort() {
    return map.get(DbKeys.NetworkElementFacilityCols.PORT);
  }

  public String getPortPec() {
    return map.get(FacilityConstants.PORTPEC_ATTR);
  }

  public String getPortType() {
    return map.get(FacilityConstants.PORTTYPE_ATTR);
  }

  public String getPrimaryKey() {
    return map.get(DbKeys.NetworkElementFacilityCols.PK);
  }

  public String getPrimaryState() {
    return map.get(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE);
  }

  public String getShelf() {
    return map.get(DbKeys.NetworkElementFacilityCols.SHELF);
  }

  public String getSigType() {
    return map.get(DbKeys.NetworkElementFacilityCols.SIGTYPE);
  }

  public String getSite() {
    return map.get(DbKeys.NetworkElementFacilityCols.SITE);
  }

  public String getSlot() {
    return map.get(DbKeys.NetworkElementFacilityCols.SLOT);
  }

  public String getSrlg() {
    return map.get(DbKeys.NetworkElementFacilityCols.SRLG);
  }

  public String getTna() {
    return map.get(DbKeys.NetworkElementFacilityCols.TNA);
  }

  public String getType() {
    return map.get(DbKeys.NetworkElementFacilityCols.TYPE);
  }

  public String getUserLabel() {
    return map.get(DbKeys.NetworkElementFacilityCols.USER_LABEL);
  }

  public String getVcat() {
    return map.get(DbKeys.NetworkElementFacilityCols.VCAT);
  }

  public String getWavelength() {
    return map.get(DbKeys.NetworkElementFacilityCols.WAVELENGTH);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (map == null ? 0 : map.hashCode());
    return result;
  }

  public boolean isEth() {
    return Facility.isEth(getAid());
  }

  public boolean isL2() {
    return Facility.isL2(getAid());
  }

  public boolean isWan() {
    return Facility.isWan(getAid());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Facility [map=");
    builder.append(map);
    builder.append("]");
    return builder.toString();
  }

  private void check(String key) throws Exception {
    String s = map.get(key);
    if (s == null || "".equals(s)) {
      throw new Exception("Facilitity: Missing manditory attribute '" + key
          + "' missing or empty in " + map);
    }
  }

  private void validateFacility() throws Exception {
    // Validation certain fields must be present.
    check(DbKeys.NetworkElementFacilityCols.AID);

    /*
     * Layer and Id are basically the same layer looks like "layer1" id looks
     * like "1".
     */
    check(DbKeys.NetworkElementFacilityCols.LAYER);

    check(DbKeys.NetworkElementFacilityCols.SHELF);
    check(DbKeys.NetworkElementFacilityCols.SLOT);
    check(DbKeys.NetworkElementFacilityCols.PORT);
    check(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE);
    check(DbKeys.NetworkElementFacilityCols.SIGTYPE);

    /*
     * this will make sure the signal type is one of the permitted enums,
     * otherwise value of will toss an exception
     */
    try {
      SIGNAL_TYPE.valueOf(map.get(DbKeys.NetworkElementFacilityCols.SIGTYPE));
    }
    catch (IllegalArgumentException iae) {
      throw new Exception("Facility: Manditory attribute '"
          + DbKeys.NetworkElementFacilityCols.SIGTYPE + "' is invalid " + map,
          iae);
    }

    /*
     * this will make sure the layer is one of the permitted enums, otherwise
     * value of will toss an exception
     */
    // Layer l = null;
    try {
      // l = Layer.toEnum(map.get(DbKeys.NetworkElementFacilityCols.LAYER));
      Layer.toEnum(map.get(DbKeys.NetworkElementFacilityCols.LAYER));
    }
    catch (IllegalArgumentException iae) {
      throw new Exception("Facility: Manditory attribute '"
          + DbKeys.NetworkElementFacilityCols.LAYER + "' is invalid " + map,
          iae);
    }

    check(DbKeys.NetworkElementFacilityCols.TNA);
    check(DbKeys.NetworkElementFacilityCols.SITE);
    check(DbKeys.NetworkElementFacilityCols.FAC_NEID);

    /* If the primary key is missing, create it from the NEID and AID */
    String s = map.get(DbKeys.NetworkElementFacilityCols.PK);

    String pk = EndPointType.encodeFacilityEndpointResourceId(
        map.get(DbKeys.NetworkElementFacilityCols.FAC_NEID),
        map.get(DbKeys.NetworkElementFacilityCols.AID));
    if (s == null || "".equals(s)) {
      map.put(DbKeys.NetworkElementFacilityCols.PK, pk);
    }

    if (!map.get(DbKeys.NetworkElementFacilityCols.PK).equals(pk)) {
      throw new Exception(
          "Consistancy error in facility record computed primary key is not equal to provided key computed "
              + pk + " map " + map);
    }
  }
}
