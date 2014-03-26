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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * @author vnguyen This factory serves XML document helper: creating xml
 *         doc/root, toString, etc...
 */
public final class XmlUtility {


  private XmlUtility() {
    super();
  }

  public static String arrayToString(List<?> list, String delim) {
    return arrayToString(list, delim, null);
  }

  public static String arrayToString(List<?> list, String delim,
      String defaultString) {
    if (list == null || list.isEmpty()) {
      return defaultString;
    }

    StringBuilder buf = new StringBuilder();
    for (Iterator<?> it = list.iterator(); it.hasNext();) {
      buf.append(it.next().toString());

      if (it.hasNext()) {
        buf.append(delim);
      }
    }
    return buf.toString();
  }

  public static String convertStringToXMLLiterals(String query) {
    // return query.replaceAll(QUOTE, QUOTE_XML).replaceAll(APOSTROPHE,
    // APOSTROPHE_XML);
    return StringParser.encodeForXMLSpecialChars(query);

  }

  public static String convertXMLLiteralsToString(String query) {
    // return query.replaceAll(QUOTE_XML, QUOTE).replaceAll(APOSTROPHE_XML,
    // APOSTROPHE);
    return StringParser.decodeForXMLSpecialChars(query);
  }

  public static Element createDocumentRoot(ByteArrayInputStream xmlData)
      throws Exception {
    SAXBuilder builder = new SAXBuilder();
    Document aDoc = builder.build(xmlData);
    if (aDoc == null) {
      throw new Exception("doc should not be null");
    }
    return aDoc.getRootElement();
  }

	public static List<Element> createDocumentRoot(List<String> input)
	    throws Exception {
		if (input == null) {
			return null;
		}
		List<Element> results = new ArrayList<Element>(input.size());
		for (String e : input) {
			results.add(createDocumentRoot(e));
		}
		return results;
	}

	public static Element createDocumentRoot(String xmlString) throws Exception {
		ByteArrayInputStream xmlData = new ByteArrayInputStream(
		    xmlString.getBytes());
		return XmlUtility.createDocumentRoot(xmlData);
	}
	
	public static String elementToString(Element node) {
		return rootNodeToString(node);
	}

  public static List<String> elementToString(List<Element> node) {
    if (node == null) {
      return null;
    }

    ArrayList<String> results = new ArrayList<String>(node.size());
    for (Element e : node) {
      results.add(rootNodeToString(e));
    }
    return results;
  }

  public static String rootNodeToString(Element node) {
    return new XMLOutputter(Format.getCompactFormat()).outputString(node);
  }

  public static List<String> stringToArray(String str, String delim) {
    if (str == null) {
      return null;
    }

    StringTokenizer tokens = new StringTokenizer(str, delim);

    List<String> results = new ArrayList<String>(tokens.countTokens());
    while (tokens.hasMoreElements()) {
      results.add(tokens.nextToken());
    }
    return results;
  }

  public static String toXMLString(String tag, Object data) {
    StringBuilder buf = new StringBuilder();
    buf.append("<" + tag + ">");
    if (data != null) {
      buf.append(StringParser.encodeForXMLSpecialChars(data.toString()));
    }
    buf.append("</" + tag + ">");
    return buf.toString();
  }

}
