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

package com.nortel.appcore.app.drac.common.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.GenericJdomParser;

public final class DbOpsHelper {
  private static final Logger log = LoggerFactory.getLogger(DbOpsHelper.class);

  private DbOpsHelper() {
    super();
  }

  public static Object deserialize(byte[] bstuff) {
    return SerializationUtils.deserialize(bstuff);
  }

  public static Map<String, String> elementToMap(Element element) {
    Map<String, String> map = new HashMap<String, String>();

    // The root could be an attribute! e.g. Facility ... where root is <layer1>
    // or <layer2>
    if (element.getName().startsWith(DbKeys.NetworkElementFacilityCols.LAYER)) {
      map.put(DbKeys.NetworkElementFacilityCols.LAYER, element.getName());
    }

    List<Attribute> attrs = element.getAttributes();
    for (Attribute attr : attrs) {
      map.put(attr.getName(), attr.getValue());
    }

    return map;
  }

  public static String elementToString(Element element) throws Exception {
    return new XMLOutputter().outputString(element);
  }

  public static Element mapToElement(String elementRoot, Map<String, String> map) {
    return mapToElement(elementRoot, map, null);
  }

  // outKeys is to list the desired output names ... null means ALL
  public static Element mapToElement(String elementRoot, Map<String, String> map, List<String> outKeys) {
    Element element = new Element(elementRoot);

    for (Map.Entry<String, String> e : map.entrySet()) {
      String key = e.getKey();

      if (outKeys == null || outKeys.contains(key)) {
        element.setAttribute(key, e.getValue());
      }
    }
    return element;
  }

  public static Element mapToNameValuePairElement(String elementRoot, Map<String, String> map) {
    Element element = new Element(elementRoot);

    for (Map.Entry<String, String> e : map.entrySet()) {
      Element nsv = new Element("nsv");
      nsv.setAttribute("name", e.getKey());
      nsv.setAttribute("value", e.getValue());
      element.addContent(nsv);
    }
    return element;
  }

  public static Map<String, String> nameValuePairElementToMap(Element root) {
    Map<String, String> results = new HashMap<String, String>();
    for (Element e : (List<Element>) root.getChildren()) {
      if ("nsv".equals(e.getName())) {
        results.put(e.getAttribute("name").getValue(), e.getAttribute("value").getValue());
      }
    }
    return results;
  }

  public static byte[] serialize(Serializable o) {
    return SerializationUtils.serialize(o);
  }

  public static Element xmlToElement(String xml) throws Exception {
    try {
      GenericJdomParser jparser = new GenericJdomParser();
      jparser.parse(xml);
      return jparser.getRoot();
    }
    catch (Exception e) {
      log.error("Failed to parse xml " + xml, e);
      throw new Exception("Failed to parse xml " + xml, e);
    }
  }

  // Recent refactoring: this is where the 'ball of string' stopped.
  // Consider now the use of xml in passing data out of the mediation classes.
  public static Map<String, String> xmlToMap(String xml) throws Exception {
    try {
      GenericJdomParser jparser = new GenericJdomParser();
      jparser.parse(xml);
      Element element = jparser.getRoot();
      return elementToMap(element);

    }
    catch (Exception e) {
      log.error("Failed to parse xml " + xml, e);
      throw new Exception("Failed to parse xml " + xml, e);
    }
  }

}
