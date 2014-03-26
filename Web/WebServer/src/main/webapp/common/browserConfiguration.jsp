<%--

    <pre>
    The owner of the original code is Ciena Corporation.

    Portions created by the original owner are Copyright (C) 2004-2010
    the original owner. All Rights Reserved.

    Portions created by other contributors are Copyright (C) the contributor.
    All Rights Reserved.

    Contributor(s):
      (Contributors insert name & email here)

    This file is part of DRAC (Dynamic Resource Allocation Controller).

    DRAC is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    DRAC is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
    Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program.  If not, see <http://www.gnu.org/licenses/>.
    </pre>

--%>

<%@ page session="true" errorPage="/common/dracError.jsp"
         import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 ****************************************************************************/
String pageRef = "drac.common";
%>

<%
session.setAttribute("badBrowserConfig", "true");

/* Load the locale specific display information into the session. */
ResourceBundle bundle = null;
try 
{
	bundle = ResourceBundle.getBundle("DRAC", new Locale("en","US"));

	for (Enumeration e = bundle.getKeys();e.hasMoreElements();) 
        {
	  String key = (String)e.nextElement();
	  String s = bundle.getString(key);
	  session.setAttribute(key,s);
	}

} 
catch (Exception e) 
{
  /* the header/footer will be missing labels */
}

%>

   <%@ include file="/common/header_struts.jsp" %>
   <table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
      <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
      <tr>
        <td align="left" valign="middle">
          <jsp:include page='browserconfig_en_US.jsp' />
        </td>
      </tr>
    </table>

<%@ include file="/common/footer.jsp" %>
