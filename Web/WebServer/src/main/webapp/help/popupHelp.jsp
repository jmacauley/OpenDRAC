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

<%@ page errorPage="/common/dracError.jsp" import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
/*
 * OpenDRAC Web GUI
 * 
 */

/* Get the title of the page that is including the header. */
String pageRef = request.getParameter("title");
String pageTitle = "";
String pageHelp = "drac.help.default.file";

if (pageRef != null)
{
  pageTitle = (String) session.getAttribute(pageRef);
  pageHelp = pageRef + ".help";
}

/* Get the help file URL. */
String helpURL = (String) session.getAttribute(pageHelp);

/* Get the calling URL of this page for later referals. */
String myURL = request.getRequestURI();

/* Check to make sure the locale is set otherwise we will not have language
 * specific bundle resources loaded.
 */
Locale locale = (Locale) session.getAttribute("myLocale");

/* If the locale is not set then we need to set it. */
if (locale == null)
{
  %>
    <c:redirect url="/language.do">
      <c:param name="langauge" value="en" />
      <c:param name="country" value="US" />
         <c:param name="callingURL" value="${myURL}" /> 
	</c:redirect>
  <%
}

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <title>OpenDRAC WEB GUI :: <c:out value='${pageTitle}'/> </title>

    <link rel="stylesheet" type="text/css" href="/files/main.css" />
    <link rel="SHORTCUT ICON" href="/images/favicon.ico">

</head>

<body class="bodyline">

<!-- This table encompasses the contents of the page header. -->
<table border="0" cellpadding="0" cellspacing="0" width="98%" align="center">
  <tr>
    <td>
      <table border="0" cellpadding="0" cellspacing="0" class="tbt">
        <tr><td><img src="/images/spacer.gif" alt="" height="5"</td></tr>
        <tr>
          <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
          <td class="tbtbot"><b><c:out value='${sessionScope["drac.help.topic"]}'/> :: <c:out value='${pageTitle}' /></b><img src="/images/spacer.gif" alt="" width="8" height="22" align="absmiddle" /></td>
          <td class="tbtr"><img src="/images/spacer.gif" alt="" width="90" height="22" /></td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <table class="forumline" width="100%" cellspacing="1" cellpadding="0" align="center">
        <tr>
          <td class="row1" valign="top">
            <jsp:include page="<%= helpURL %>" />
	  </td>
	</tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
        <table border="0" cellpadding="0" cellspacing="0" class="tbb">
        <tr>
          <td class="tbbl"><img src="/images/spacer.gif" alt="" height="22" /></td>
          <td class="tbbbot" align="left"><img src="/images/spacer.gif" alt="" height="22" /></td>
          <td class="tbbbot" align="right"><img src="/images/spacer.gif" alt="" height="22" /></td>
          <td class="tbbr"><img src="/images/spacer.gif" alt="" height="22" /></td>
        </tr>
      </table>

      <table border="0" cellpadding="0" cellspacing="0" class="tbl">
        <tr>
          <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
          <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
          <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td align="right">
      <a href="javascript:window.close();" class="helpLink">
        <c:out value='${sessionScope["drac.text.close"]}'/>
      </a>
    </td>
  </tr>
</table>

