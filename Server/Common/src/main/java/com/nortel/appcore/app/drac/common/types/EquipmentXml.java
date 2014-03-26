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

import java.util.Map;
import java.util.TreeMap;

import com.nortel.appcore.app.drac.common.FacilityConstants;

// GGL2 new class for retrieved eqpt
public class EquipmentXml {
  private final String componentAid;
  private final String shelf;
  private final String slot;
  private final String port;

  // Example of a response:
  // operation=RTRV-EQPT,
  // neId=00-21-E1-D6-D8-2C,
  // PROVPEC=NTK535FA,
  // PST=OOS-AUMA,
  // CPTYPE=SuperMux 24 Port I/O 1xXFP/10xSFP,
  // AID=SMUX-1-4,
  // EQPTPROFILE=GFP-F,
  // TMGID=SHELF-1,
  // CARRIER2=NONE,
  // SST=UEQ,
  // CARRIER1=NONE

  private final TreeMap<String, String> map = new TreeMap<String, String>();

  public EquipmentXml(Map<String, String> map) throws Exception {
    this.map.putAll(map);
    validateFacility();

    String[] aid = map.get("AID").split("-");
    this.componentAid = aid[0];
    this.shelf = aid[1];
    this.slot = aid[2];

    // e.g. card = 20GL2SS-1-13, port = P10GEL-1-13-1
    if (aid.length > 3) {
      this.port = aid[3];
    }
    else {
      this.port = null;
    }
  }

  public static void addEqptAttributes(Map<String, String> attrs,
      EquipmentXml cardEqpt, EquipmentXml portEqpt) {
    // Sample TL1 response:
    // "20GL2SS-1-13::PROVPEC=NTK531HA,CTYPE=\"20G L2SS 16 Port I/O
    // 2xXFP/8xSFP\",MA
    // PMODE=AU3_VT15,WANCONFIG=8HI56LOCAP,CARRIER1=NONE,CARRIER2=NONE:OOS-AUMA,UEQ"
    //
    // "P10GEL-1-13-1::PROVPEC=NTTP81AA,CTYPE=\"10GBASE-SR/SW, 850 nm, XFP,
    // 0/70C, L
    // C, MSA\":OOS-AUMA,UEQ"
    //
    // Card Type = 20GL2SS
    // Card PEC = NTK531HA
    // Port Type = P10GEL
    // Port PEC = NTTP81AA

    if (cardEqpt != null) {
      attrs.put(FacilityConstants.CARDTYPE_ATTR, cardEqpt.getComponentAid());
      attrs.put(FacilityConstants.CARDPEC_ATTR, cardEqpt.getProvPec());
    }

    if (portEqpt != null) {
      attrs.put(FacilityConstants.PORTTYPE_ATTR, portEqpt.getComponentAid());
      attrs.put(FacilityConstants.PORTPEC_ATTR, portEqpt.getProvPec());
    }
  }

  /**
   * @return the componentAid
   */
  public String getComponentAid() {
    return componentAid;
  }

  public String getCpType() {
    return map.get("CPTYPE");
  }

  public String getPort() {
    return port;
  }

  public String getProvPec() {
    return map.get("PROVPEC");
  }

  /**
   * @return the shelf
   */
  public String getShelf() {
    return shelf;
  }

  /**
   * @return the slot
   */
  public String getSlot() {
    return slot;
  }

  public boolean isCard() {
    return this.port == null;
  }

  public boolean isPort() {
    return this.port != null;
  }

  @Override
  public String toString() {
    return "<eqptInstance " + "componentAid=\"" + getComponentAid() + "\" "
        + "\" " + "shelf=\"" + getShelf() + "\" " + "slot=\"" + getSlot()
        + "\"/>";
  }

  private void checkKeyPresent(String key) throws Exception {
    String s = map.get(key);
    if (s == null || "".equals(s)) {
      throw new Exception("EquipmentXml: Missing attribute '" + key
          + "' missing or empty in " + map);
    }
  }

  private void validateFacility() throws Exception {
    // Validation certain fields must be present.
    checkKeyPresent("AID");
  }
}
