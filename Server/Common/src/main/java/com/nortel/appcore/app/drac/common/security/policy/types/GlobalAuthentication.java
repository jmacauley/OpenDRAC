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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.security.policy.types.UserProfile.AuthenticationType;

public final class GlobalAuthentication implements Serializable {

  public static class SupportedAuthenticationType implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Logger log = LoggerFactory.getLogger(getClass());
    public static final String AUTHENTICATIONTYPE__ELEMENT = "authenticationType";
    public static final String TYPE_ATTR = "type";
    public static final String SUPPORTED_ATTR = "supported";
    private UserProfile.AuthenticationType type;
    private Boolean supported;

    public SupportedAuthenticationType() {
      type = UserProfile.AuthenticationType.INTERNAL;
      supported = Boolean.TRUE;
    }

    public void fromXML(Element element) throws Exception {
      /********************************************************/
      /* Constructs GlobalAuthentication object. */
      /********************************************************/
      String authenTypeStr = element.getAttributeValue(TYPE_ATTR);

      if (authenTypeStr != null && !authenTypeStr.equals("")) {
        if (authenTypeStr.equals(AuthenticationType.INTERNAL.toString())) {
          this.type = AuthenticationType.INTERNAL;
        }
        else if (authenTypeStr.equals(AuthenticationType.A_SELECT.toString())) {
          this.type = AuthenticationType.A_SELECT;
        }
        else if (authenTypeStr.equals(AuthenticationType.RADIUS.toString())) {
          this.type = AuthenticationType.RADIUS;
        }
        else {
          log.error("Invalid authentication type: '" + authenTypeStr + "'");
        }
      }
      this.supported = Boolean.valueOf(element
          .getAttributeValue(SUPPORTED_ATTR));
      // 
    }

    // public Boolean getSupported()
    // {
    // return supported;
    // }

    public UserProfile.AuthenticationType getType() {
      return this.type;
    }

    public boolean isSupported() {
      return this.supported.equals(Boolean.TRUE);
    }

    public void setSupported(Boolean supported) {
      this.supported = supported;
    }

    public String toXMLString() {
      StringBuilder buf = new StringBuilder(40);
      buf.append("<");
      buf.append(AUTHENTICATIONTYPE__ELEMENT);
      buf.append(" " + TYPE_ATTR + "=\"" + this.type.toString() + "\"");
      buf.append(" " + SUPPORTED_ATTR + "=\"" + this.supported.toString()
          + "\"");
      buf.append(">");
      buf.append("</" + AUTHENTICATIONTYPE__ELEMENT + ">");
      return buf.toString();
    }
  }

  private static final long serialVersionUID = 1L;
  public static final String AUTHENTICATION_LIST_ELEMENT = "authenticationList";
  private final List<SupportedAuthenticationType> supportedAuthenticationTypes;

  public GlobalAuthentication() {
    this.supportedAuthenticationTypes = new ArrayList<SupportedAuthenticationType>();
  }

  public void fromXML(Element root) throws Exception {
    /********************************************************/
    /* Constructs GlobalAuthentication object. */
    /********************************************************/

    Element authenListElement = root
        .getChild(GlobalAuthentication.AUTHENTICATION_LIST_ELEMENT);

    if (authenListElement == null) {
      return;
    }

    List<Element> elements = authenListElement.getChildren();

    if (elements == null) {
      return;
    }

    SupportedAuthenticationType supportedType;

    for (Element element : elements) {
      supportedType = new SupportedAuthenticationType();
      supportedType.fromXML(element);
      this.supportedAuthenticationTypes.add(supportedType);
    }

    // 
  }

  public List<SupportedAuthenticationType> getAllAuthenticationTypes() {

    // we guarantee that at least internal authentication is supported
    // the rest of it should be read from our policy db

    if (this.supportedAuthenticationTypes.isEmpty()) {
      this.supportedAuthenticationTypes.add(new SupportedAuthenticationType());
    }

    return supportedAuthenticationTypes;
  }

  public List<SupportedAuthenticationType> getOnlySupportedAuthenticationTypes() {

    // we guarantee that at least internal authentication is supported
    // the rest of it should be read from our policy db

    List<SupportedAuthenticationType> result = new ArrayList<SupportedAuthenticationType>();
    for (SupportedAuthenticationType supportedType : this.supportedAuthenticationTypes) {
      if (supportedType.isSupported()) {
        result.add(supportedType);
      }
    }

    return result;
  }

  public void setAuthenticationTypes(List<SupportedAuthenticationType> t) {
    supportedAuthenticationTypes.addAll(t);
  }

  public String toXMLString() {
    StringBuilder buf = new StringBuilder();
    buf.append("<");
    buf.append(GlobalAuthentication.AUTHENTICATION_LIST_ELEMENT);
    buf.append(">");

    for (SupportedAuthenticationType supportedAuthenticationType : this.supportedAuthenticationTypes) {
      buf.append("\n");
      buf.append(supportedAuthenticationType.toXMLString());
    }

    buf.append("\n");
    buf.append("</");
    buf.append(GlobalAuthentication.AUTHENTICATION_LIST_ELEMENT);
    buf.append(">");
    return buf.toString();
  }

}
