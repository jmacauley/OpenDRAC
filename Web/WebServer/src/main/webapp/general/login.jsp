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
String pageRef = "drac.general.login";

/* Get the refering page if there was one. */
String callingURL = request.getParameter("callingURL");
String defaultUserID = request.getParameter("userID");

if (defaultUserID == null) {
  defaultUserID = "";
}

if ( (callingURL == null) || ("".equals(callingURL)) ) {
    callingURL = "/welcome.do";
}

%>


<%@ include file="/common/header_struts.jsp" %>
<script type="text/javascript" src="/scripts/ajax.js"></script>

<form name='LoginForm' method="POST" action="/performLogin.do" onsubmit="return loginCheck()">
  <table width="80%" cellspacing="5" cellpadding="0" border="0" align="center" style="position:static!important;position:relative">
    <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
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
      <td align="center" class="gen"><bean:message key="drac.general.login.text"/></td>
    </tr>
    <tr />
    <tr>
      <td>
        <table cellspacing="5" cellpadding="0" border="0" align="center">
          <tr>
            <td align="right" class="gen">
              <bean:message key="drac.general.login.userID"/>:
            </td>
            <td align="left">
              <input type="text" name="userID" size="25" maxlength="100" value=""/>
            </td>
          </tr>
          
          <!-- Disabling the previous two-stage (userid / userid + passwd) login.
               The userId-only login page was presented for cases in which the 
               the userId authenticated via ASelect.
               The Ajax call to retrieve the userId status is considered a 
               vulnerability because it allows an attacker to harvest valid user 
               names independently of the corresponding passwords.
          -->
          <!-- tr id="passwordRow" style="display:none" -->
          <tr id="passwordRow" style="">

          
            <td align="right" class="gen">
              <bean:message key="drac.general.login.password"/>:
            </td>
            <td align="left">
              <input id="password" type="password" name="password" size="25" maxlength="256"/>
            </td>
          </tr>

	    <tr>
               <td align="right" class="gen">
               </td>

	      <td align="left">
	      		<input type="submit" name="login" class="button" value='<bean:message key="drac.general.login.submit"/>' /> &nbsp;&nbsp;
	      
		<input type="hidden" name="redirect" value="<c:out value="${callingURL}"/>" />
		<!-- input type='button' value='<bean:message key="drac.text.reset"/>' onclick="doReset()" -->
	      </td>
            </tr>


        </table>
      </td>
    </tr>
    <tr>
        <td>
        <div id="loadingArea" align="center" style="display:none;position:absolute;left:0px;top:175px!important;top:0px;width:100%">
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
  document.LoginForm.userID.focus();
  function loginCheck()
  {
    if(document.LoginForm.userID.value == "")
    {
      alert("<c:out value='${sessionScope["drac.general.login.error.missingUserID"]}'/>");
      document.LoginForm.userID.focus();
      return false;
    }

    if (document.getElementById("passwordRow").style.display != "none") {
        if (document.LoginForm.password.value == "") {
            alert('<c:out value='${sessionScope["drac.general.login.error.missingPassword"]}'/>');
            document.LoginForm.password.focus();
            return false;
        } else {
            showLoading(true);
            return true;
        }
    } else {
        var url = "/drac?action=getAuth&id=" + escape(document.LoginForm.userID.value);
        new AJAXInteraction(url, processAuthResponse).doGet();
        return false;
    }
  }


  function processAuthResponse(responseXML) {
    var auth = responseXML.getElementsByTagName("authType")[0].firstChild.nodeValue;
    if (auth == "ASELECT") {
        document.getElementById("passwordRow").style.display = "none";
        showLoading(true);
        document.LoginForm.submit();
    } else {
        document.getElementById("passwordRow").style.display = "";
        document.LoginForm.password.focus();
    }
  }

  function doReset() {
    showLoading(false);
    document.LoginForm.userID.value = "";
    document.LoginForm.password.value = "";
    document.getElementById("passwordRow").style.display = "none";
    document.LoginForm.userID.focus();
  }

  function showLoading(show) {
    if (show) {
        document.LoginForm.userID.blur();
        document.LoginForm.password.blur();
        document.getElementById("loadingArea").style.display = "";
    } else {
        document.getElementById("loadingArea").style.display = "none";
    }
  }

  showLoading(false);

</script>

<%@ include file="/common/footer.jsp" %>
