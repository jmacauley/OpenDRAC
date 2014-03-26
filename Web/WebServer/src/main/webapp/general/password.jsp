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

<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ page session="true" errorPage="/common/dracError.jsp" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /general/login.jsp
 *
 * Description:
 *   This page displays the login screen and posts results to
 *   verifyLogin.jsp for authentication.
 *
 ****************************************************************************/

/* Get the refering page if there was one. */
String callingURL = (String) request.getParameter("callingURL");
if (callingURL == null) {
    callingURL = (String) request.getAttribute("callingURL");
}

String defaultUserID = StringEscapeUtils.escapeXml((String) request.getAttribute("userID"));
String paramUserID = StringEscapeUtils.escapeXml((String)request.getParameter("userID"));

if (defaultUserID == null)
{
    if (paramUserID == null) {
        defaultUserID = "";
    } else {
        defaultUserID = paramUserID;
    }
}

if (callingURL == null) {
    callingURL = "";
}


String pageRef = "drac.general.login";

%>

<%@ include file="/common/header_struts.jsp" %>

<form name='LoginForm' method="POST" action="/performLogin.do" onsubmit="return loginCheck()">
    <input type="hidden" name="userID" value="<%=defaultUserID %>"/>
    <input type="hidden" name="redirect" value="<%=callingURL%>"/>
  <table width="80%" cellspacing="5" cellpadding="0" border="0" align="center" style="position:static!important;position:relative">
    <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
    <tr />
    <logic:messagesPresent message="true">
        <html:messages id="message" message="true">
        <tr>
            <td align="center" class="gen">
                <font color="red"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
    </logic:messagesPresent>
    <tr>
      <td>
        <table cellspacing="5" cellpadding="0" border="0" align="center">
          <tr>
            <td>&nbsp;</td>
            <td align="left" >
              Enter password for <c:out value='${defaultUserID}'/>
            </td>
          </tr>
          <tr>
            <td align="right" class="gen">
              <bean:message key="drac.general.login.password"/>:
            </td>
            <td align="left">
              <input type="password" name="password" size="25" maxlength="256" />
            </td>
          </tr>
        </table>
      </td>
    </tr>
    <tr align="center">
      <td>
        <input type="submit" name="login" class="button" value='<bean:message key="drac.general.login.submit"/>' /> &nbsp;
        &nbsp;<input type='reset' value='<bean:message key="drac.text.reset"/>'>&nbsp;
        &nbsp;<input type="button" value='<bean:message key="drac.text.cancel"/>' onclick="Javascript:doCancel()">
      </td>
    </tr>
    <tr align="center">
        <td>
        <div id="loadingArea" style="display:none;position:absolute;left:0px;top:175px!important;top:0px;width:100%">
          <table cellspacing="0" cellpadding="0" border="0" align="center" width="150">
            <tr>
                <td>
                <!-- Header. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                    <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                    </tr>
                    </tbody>
                </table>
                </td>
            </tr>

            <tr>
                <td>
                    <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm" bgcolor="#EFEFEF">
                    <tbody>
                        <tr>
                            <td class="cat" align="center">&nbsp;<br>
                            <img src="/images/loading_small.gif" align="left"><b><bean:message key="drac.general.login.loading.msg1"/><br>
                            <bean:message key="drac.general.login.loading.msg2"/><br>&nbsp;</b></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
              <td>
                <!-- Drop shadow. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbl">
                <tbody>
                  <tr>
                    <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                    <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                    <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                  </tr>
                </tbody>
                </table>
              </td>
            </tr>
          </table>
        </div>
        </td>
    </tr>
  </table>
</form>

<br />

<script language="JavaScript">
  document.LoginForm.password.focus();

  function loginCheck()
  {
    if(document.LoginForm.password.value == "")
    {
      alert('<c:out value='${sessionScope["drac.general.login.error.missingPassword"]}'/>');
      document.LoginForm.password.focus();
      return false;
    }

    showLoading(true);
    return true;
  }

  function doCancel()
  {
    location.href="/login.do";
  }

  function showLoading(show) {
    if (show) {
        document.LoginForm.password.blur();
        document.getElementById("loadingArea").style.display = "";
    } else {
        document.getElementById("loadingArea").style.display = "none";
    }
  }

  showLoading(false);
</script>

<%@ include file="/common/footer.jsp" %>
