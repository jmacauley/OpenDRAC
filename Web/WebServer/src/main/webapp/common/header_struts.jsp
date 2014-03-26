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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page
	import="java.util.*,com.nortel.appcore.app.drac.server.requesthandler.*"%>
<%@ page
	import="com.nortel.appcore.app.drac.server.webserver.struts.common.*"%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>

<%
    /*
     * OpenDRAC Web GUI
     *
     * Filename: /common/header.jsp
     *
     * This file demonstrates use of the OpenDRAC WEB interface page header, menuing
     * system, and page footer.  The opening an closing of the HTML page will be
     * handled by the included header files.  Make sure these are included in all
     * HTML output to the end user.
     *
     * This script expects that the global variables pageName amd language have
     * been set in the including .jsp.
     */

    String myLanguage = "";
    String contentLanguage = "";

    // session parms
    Locale locale = (Locale) session.getAttribute(DracConstants.MYLOCALE);

    // request parms
    String myURL = (String) request.getAttribute(DracConstants.MYURL);

    // Get the language code for the calendar.
    if (locale != null)
    {
        myLanguage = locale.toString().substring(0, 2);
        contentLanguage = locale.toString().replace('_', '-');
    }

    // Get the title for the including page.
    // pageRef is defined in the calling pages
    String pageTitle = "";
    if (pageRef != null){
        pageTitle = (String) session.getAttribute(pageRef);
        if (pageTitle == null)
        {
            pageTitle = "";
        }
    }
    pageContext.setAttribute("pageRef",pageRef);
    request.setAttribute("pageRef",pageRef);
    //request.getSession().setAttribute("pageRef",pageRef);
    /* Build the user login status for the main table title bar. */
    String userStatus = (String) null;
    String unreadMsg = "";
    String activeUserID = (String) session.getAttribute(DracConstants.USER_ID);

    if (activeUserID != null)
    {
        String queryString = request.getQueryString();

        userStatus = "<a href='/logout.do?callingURL=" + myURL + (queryString != null ? ("?" + queryString) : "")
                + "' class='userStatus'><img src='/images/logout_icon.gif' title='Log out' align='absmiddle'> <b>"
                + (String) session.getAttribute("drac.header.activeUser") + " [ "
                + StringEscapeUtils.escapeXml(activeUserID) + " ]</b></a>";
        String count = (String) session.getAttribute("unreadCount");
        if ((count != null) && !count.equals("0"))
        {
            unreadMsg = "<a href='/messages/inbox.do'><img src='/images/newmail.gif' align='absmiddle' title='" + count
                    + " " + (String) session.getValue("drac.messages.unreadMessages") + "'></a>";
        }
    }
    else
    {
        String callURL = (String) request.getParameter("callingURL");
        String inActiveUser = (String) session.getAttribute("drac.header.inActiveUser");
        if (inActiveUser == null)
        {
           inActiveUser = "";
        }

        if ("/login.do".equals(myURL) && (callURL == null))
        {
            userStatus = "<a href='/login.do' class='userStatus'><img src='/images/login_icon.gif' title='Log in' align='absmiddle'> <b>"
                    + inActiveUser + "</b></a>";
        }
        else
        {
            userStatus = "<a href='/login.do?callingURL=" + callURL
                    + "' class='userStatus'><img src='/images/login_icon.gif' title='Log in' align='absmiddle'> <b>"
                    + inActiveUser + "</b></a>";
        }

    }

    response.setHeader("Pragma", "No-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-cache");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<title>OpenDRAC Web Console <c:out value="${pageTitle}" /></title>
	<link rel="stylesheet" type="text/css" href="/files/main.css" />
	<link rel="stylesheet" type="text/css" href="/files/menu.css" />
	<link rel="StyleSheet" type="text/css" href="/files/tab.webfx.css" />
	<link rel="stylesheet" type="text/css" href="/files/calendar.css" />
	<link rel="stylesheet" href="/files/dhtmlwindow.css" type="text/css" />
	<link rel="SHORTCUT ICON" href="/images/favicon.ico">

  <script type="text/javascript" src="/scripts/ie5.js"></script>
  <script type="text/javascript" src="/scripts/DropDownMenuX.js"></script>
  <script type="text/javascript" src="/scripts/utilities.js"></script>
  <script language="JavaScript"><%@ include file="/scripts/serverDate.jsp"%></script>
  <script language="JavaScript"><%@ include file="/scripts/serverTime.jsp"%></script>
  <script type="text/javascript" src="/scripts/onLoad.js"></script>
</head>

    <body class="bodyline">

        <!-- This table encompasses the contents of the page header. -->
        <table border="0" cellpadding="0" cellspacing="0" width="98%" align="center">
            <!-- DRAC/Nortel banner. -->
            <tr>
                <td>
                    <table cellpadding=0 cellspacing=0 class="banner" width="100%">
                        <tr>
                            <td align="left" >
                               <div class="dracLogo">OpenDRAC</div>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <!-- Shadow under banner. -->
            <tr>
                <td>
                    <table border="0" cellpadding="0" cellspacing="0" class="tbl">
                        <tr>
                            <td class="tbll">
                                <img src="/images/spacer.gif" alt="" width="8" height="4" />
                            </td>
                            <td class="tblbot">
                                <img src="/images/spacer.gif" alt="" height="4" />
                            </td>
                            <td class="tblr">
                                <img src="/images/spacer.gif" alt="" width="8" height="4" />
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <c:if test="${!empty sessionScope['authObj']}">
            <c:if test="${sessionScope['authObj'].userPolicyProfile.userProfile.authenticationData.authenticationState eq 'VALID'}">
            <!-- Main horizontal menu bar. -->
            <tr>
                <td>
                    <table border="0" cellpadding="0" cellspacing="0" class="tbn">
                        <tr>
                            <td class="tbnl">
                                <img src="/images/spacer.gif" alt="" width="76" height="40" />
                            </td>
                            <td class="tbnbot" height="40">
                                <!-- Menu is stored in a separate file to make this more readable. -->
                                <jsp:include page="/common/menu.jsp">
                                    <jsp:param name="callingURL" value='<c:out value="${myURL}" />' />
                                </jsp:include>
                            </td>
                            <td class="tbnr">
                                <img src="/images/spacer.gif" alt="" width="20" height="40" />
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <!-- Shadow under banner. -->
            <tr>
                <td>
                    <table border="0" cellpadding="0" cellspacing="0" class="tbl">
                        <tr>
                            <td class="tbll">
                                <img src="/images/spacer.gif" alt="" width="8" height="4" />
                            </td>
                            <td class="tblbot">
                                <img src="/images/spacer.gif" alt="" width="8" height="4" />
                            </td>
                            <td class="tblr">
                                <img src="/images/spacer.gif" alt="" width="8" height="4" />
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            </c:if>
            </c:if>
            <!-- User status information. -->
            <tr>
                <td class="nav" align="right">
                    <c:out value="${unreadMsg}" />
                    &nbsp;&nbsp;
                    <c:out value="${userStatus}" />
                    &nbsp;&nbsp;
                </td>
            </tr>

            <!-- Check to make sure this stuff is going to work. -->
            <noscript>
            <tr>
                <td>
                    <br />
                    <font face="Arial, Helvetica, Sans-serif" size=1> You are accessing the OpenDRAC web GUI with javascript disabled. Please enable javascript in your browser before continuing. </font>
                    <br />
                </td>
            </tr>
            </noscript>

            <!-- Main page contents - Fancy top and title bar. -->
            <tr>
                <td>
                    <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                        <tr>
                            <td colspan="3">
                                <img src="/images/spacer.gif" alt="" height="5" />
                            </td>
                        </tr>
                        <tr>
                            <td class="tbtl">
                                <img src="/images/spacer.gif" alt="" width="22" height="19" />
                            </td>
                            <td class="tbtbot">
                                <b><c:out value="${pageTitle}" /></b>
                                <img src="/images/spacer.gif" alt="" width="8" height="22" align="absmiddle" />
                            </td>
                            <td class="tbtr">
                                <img src="/images/spacer.gif" alt="" width="90" height="19" />
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <!-- This table encompasses the contents of the page header. -->
                    <table class="forumline" width="100%" cellspacing="1" cellpadding="0" align="center">
                        <tr>
                            <td class="row1" valign="top">
                                <!-- Display the page sensitive help URL. -->

<% 
     if (!("true".equals((String) session.getAttribute("badBrowserConfig"))))
     {
%>


                                <table width="16" height="16" cellspacing="0" cellpadding="0" border="0" align="right" border="0">
                                    <tr>
                                        <td align="right">
                                            <a class="helpLink" href="/help/popupHelp.do?title=<c:out value='${pageRef}' />"
                                                onClick="window.open('/help/popupHelp.do?title=<c:out value="${pageRef}" />', 'DRACHelp', 'toolbar=no, directories=no, location=no, status=yes, menubar=no, resizable=yes, scrollbars=yes, width=500, height=300'); return false"> 
                                                   <img src="/images/helpicon.gif" title="<c:out value='${session.getAttribute("drac.text.help")}'/>"/></a> <%--  --%>
                                        </td>
                                    </tr>
                                </table>
<%
}
%>
