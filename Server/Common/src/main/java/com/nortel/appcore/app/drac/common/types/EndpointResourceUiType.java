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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.utility.StringParser;

// Includes JSP/Forms utility methods. Note that the html select components will use
// built-in bean interfaces to set/get form properties ... as String[]

public class EndpointResourceUiType implements Serializable, Comparable {
  /**
     * 
     */
  private static final long serialVersionUID = 1L;
  public static final String REMAINING = "remaining";
  public static final String SUBSET = "subset";
  String resourceId;
  String tna;
  String label;
  String site;
  String wavelength;

  public EndpointResourceUiType(EndPointType ep) {
    super();

    this.resourceId = ep.getId();
    this.tna = ep.getName();
    this.label = ep.getLabel();
    this.site = ep.getSite();
    this.wavelength = ep.getWavelength();
  }

  // JSP/forms utility method
  // typically used to generate 'members' endpoint list given members tna list
  public static List<EndpointResourceUiType> selectFrom(List<String> tnaList,
      List<EndpointResourceUiType> masterEndpointList) {
    // shallow copy for manipulation
    List<EndpointResourceUiType> result = new ArrayList<EndpointResourceUiType>(
        masterEndpointList);

    for (Iterator<EndpointResourceUiType> itr = result.iterator(); itr
        .hasNext();) {
      if (!tnaList.contains(itr.next().getTna())) {
        // concurrent collection removal using iterator:
        itr.remove();
      }
    }
    return result;
  }

  // JSP/forms utility method
  // typically: subsetResourceIdList represents 'member' resourceIds ... want to
  // split master
  // endpoint list into 'member' and 'available' endpoint lists
  public static Map<String, List<EndpointResourceUiType>> splitOn(
      List<String> subsetResourceIdList,
      List<EndpointResourceUiType> masterEndpointList) {
    Map<String, List<EndpointResourceUiType>> result = new HashMap<String, List<EndpointResourceUiType>>();

    List<EndpointResourceUiType> subsetList = new ArrayList<EndpointResourceUiType>();

    // shallow copy for manipulation
    List<EndpointResourceUiType> remainingList = new ArrayList<EndpointResourceUiType>(
        masterEndpointList);

    if (subsetResourceIdList != null && subsetResourceIdList.size() > 0) {
      for (Iterator<EndpointResourceUiType> itr = remainingList.iterator(); itr
          .hasNext();) {
        EndpointResourceUiType ep = itr.next();
        if (subsetResourceIdList.contains(ep.getResourceId())) {
          subsetList.add(ep);
          // concurrent collection removal using iterator:
          itr.remove();
        }
      }
    }

    result.put(REMAINING, remainingList);
    result.put(SUBSET, subsetList);
    return result;
  }

  // JSP/forms utility method
  public static String[] toTnaStringArray(List<EndpointResourceUiType> list) {
    return toTnaStringArray(list, false);
  }

  // JSP/forms utility method
  // The compound tna::label string is used for response to
  // listResourceGroups.do ...
  // results shown in collapsible dtree
  public static String[] toTnaStringArray(List<EndpointResourceUiType> list,
      boolean appendLabel) {
    List<String> tnas = new ArrayList<String>();
    for (EndpointResourceUiType ep : list) {
      String tna = ep.getTna();
      if (appendLabel) {
        String label = ep.getLabel();

        tna = StringParser.encodeForDRACSpecialChars(tna) + "::"
            + StringParser.encodeForDRACSpecialChars(label);
      }

      tnas.add(tna);
    }

    return tnas.toArray(new String[tnas.size()]);
  }

  @Override
  public int compareTo(Object obj) { // NO_UCD
    EndpointResourceUiType other = (EndpointResourceUiType) obj;
    String thisId = this.getResourceId();
    String otherId = other.getResourceId();
    return thisId.compareTo(otherId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final EndpointResourceUiType other = (EndpointResourceUiType) obj;
    if (resourceId == null) {
      if (other.resourceId != null) {
        return false;
      }
    }
    else if (!resourceId.equals(other.resourceId)) {
      return false;
    }
    return true;
  }

  public String getLabel() {
    return label;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getSite() {
    return site;
  }

  public String getTna() {
    return tna;
  }

  public String getWavelength() {
    return wavelength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (resourceId == null ? 0 : resourceId.hashCode());
    return result;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public void setSite(String site) {
    this.site = site;
  }

  public void setTna(String tna) {
    this.tna = tna;
  }

  public void setWavelength(String wavelength) {
    this.wavelength = wavelength;
  }

  @Override
  public String toString() {
    return this.getTna();
  }
}
