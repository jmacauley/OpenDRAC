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

package com.nortel.appcore.app.drac.common.security.policy.types;

import java.util.List;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class AccessControlRule extends AbstractRule {
  private static final long serialVersionUID = 1L;
  private String key;
  private List<String> value;

  public static final String ACCESSCONTROLRULE_ELEMENT = "accessControlRule";
  public static final String KEY_ELEMENT = "key";
  public static final String VALUE_ELEMENT = "value";

  public AccessControlRule() {
    super();
  }

  public AccessControlRule(String key, String value) {
    super();
    this.key = key;
    this.value = XmlUtility.stringToArray(value, ",");
  }

  @Override
  public void fromXML(Element node) throws Exception {
    /*********************************************************************/
    /* Constructs AccessControlRule element. */
    /*********************************************************************/
    // Element accessControlRule = node.getChild(ACCESSCONTROLRULE_ELEMENT);
    Element accessControlRule = node;

    if (accessControlRule != null) {

      super.fromXML(accessControlRule);

      this.key = accessControlRule.getChildText(KEY_ELEMENT);
      // 
      String values = accessControlRule.getChildText(VALUE_ELEMENT);
      if (values != null && !values.equals("")) {
        // 
        this.value = XmlUtility.stringToArray(values, ",");
      }
      else {
        if (this.key != null) {
          DracException ex = new DracException(
              DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
              new Object[] { "AccessControlRule " + KEY_ELEMENT });
          log.error(ex.getMessage());
          throw ex;
        }
      }
    }
    else {
      
    }
    // 
  }

  public String getKey() {
    return key;
  }

  public List<String> getValue() {
    return value;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(List<String> value) {
    this.value = value;
  }

  @Override
  public String toXMLString() {
    StringBuilder buf = new StringBuilder();
    buf.append("<");
    buf.append(ACCESSCONTROLRULE_ELEMENT);
    buf.append(super.toXMLString());
    buf.append(">");

    buf.append(" ");
    buf.append(XmlUtility.toXMLString(KEY_ELEMENT, key));

    buf.append(" ");
    buf.append(XmlUtility.toXMLString(VALUE_ELEMENT,
        XmlUtility.arrayToString(value, ",")));

    buf.append(" ");
    buf.append("</");
    buf.append(ACCESSCONTROLRULE_ELEMENT);
    buf.append(">");
    return buf.toString();
  }

}
