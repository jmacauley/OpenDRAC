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
 *
 * File: /general/serverStatus.jsp
 *
 * Description:
 *   This file sets the language locale to that specified on the input
 *   paramters.  If there are no input parameters it sets the default
 *   locale to en-US.
 *
 ****************************************************************************/

String pageRef = "drac.language";

%>

<%
/* Get the URL input parameters. */
String language = request.getParameter("language");
String country = request.getParameter("country");
String callingURL = request.getParameter("callingURL");

/* Set myURL since we are not validating context */
String pageURL = request.getRequestURI();
if (pageURL == null || "".equals(pageURL))
{
  /* Assign a default just in case we cannot resolve our URI. */
  pageURL = "/language.do";
}
request.setAttribute(DracConstants.MYURL, pageURL);

/* We now set the appropriate locale with en-US being the default. */
Locale newLocale = null;

if (country != null && language != null) {
  /* Set both country and language for locale. */
  newLocale = new Locale(language, country);
}
else if (country == null && language != null) {
  /* Set the language only for locale. */
  newLocale = new Locale(language);
}
else {
  /* Set the default locale to en-US. */
  newLocale = new Locale("en","US");
}

/* Store the local against the session for future reference. */
session.setAttribute("myLocale", newLocale);

/* Load the locale specific display information into the session. */
ResourceBundle bundle = null;
try {
  bundle = ResourceBundle.getBundle("DRAC", newLocale);
} catch (Exception e) {
  /* We are screwed as there is no resource bundle file to load. */
  %>
    <c:redirect url="/common/dracError.jsp">
      <c:param name="error" value="Language bundle file missing" />
    </c:redirect>
  <%
}

for (Enumeration e = bundle.getKeys();e.hasMoreElements();) {
  String key = (String)e.nextElement();
  String s = bundle.getString(key);
  session.setAttribute(key,s);
}

/* Now set the timezone for the locale. */
Date date = new Date();
TimeZone tz = TimeZone.getDefault();
boolean daylight = tz.inDaylightTime(date);
String timeZone = tz.getDisplayName(daylight, TimeZone.SHORT, newLocale);

/* Store the timezone string against the session for future reference. */
session.setAttribute("myTimeZone", tz);
session.setAttribute("myTimeZoneStr", timeZone);

/* Return to calling page if specified. */
if (callingURL == null)
{
  %>
    <%@ include file="/common/header_struts.jsp" %>

    <table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
      <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
      <tr>
        <td align="left" valign="middle">
          <jsp:include page='<%= (String) session.getAttribute("drac.language.file") %>' />
        </td>
      </tr>
    </table>

    <%@ include file="/common/footer.jsp" %>
  <%
}
else
{
  %>
    <html>
      <head>
        <meta http-equiv="REFRESH" content="0; URL=<%= callingURL %>">
      </head>
    </html>
  <%
}
%>
