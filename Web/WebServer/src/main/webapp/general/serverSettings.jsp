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

<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /general/serverSettings.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page allows admin user to change some server settings
 *
 ****************************************************************************/


String pageRef = "drac.general.serverSettings"; 
%>
<%@ include file="/common/header_struts.jsp" %>

<html:form method="POST" action="/editServerSettings.do" >
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
<c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'SYSTEM_ADMIN'}">
    <table cellspacing="0" cellpadding="0" border="0" width="650" align="center">
        <tbody>
            <tr>
                <td><img src="/images/spacer.gif" height="5" /></td>
            </tr>
        <logic:messagesPresent>
	        <tr>
	            <td>
	            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
	                <tbody>
	                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
	                <tr>
	                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
	                    <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.schedule.create.title.errors"/></b></font></center></td>
	                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
	                </tr>
	                </tbody>
	            </table>
	            </td>
	        </tr>
	        <tr>
	          <td>
	            <!-- Schedule information contents. -->
	            <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
	            <tbody>
	              <tr>
	                <td align="center" class="tbForm1" nowrap>
	                    <html:errors/>
	                </td>
	            </tr>
	            </tbody>
	            </table>
	          </td>
	        </tr>
	        <tr>
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
	    </logic:messagesPresent>
	    <logic:present name="success" scope="request">
           <tr>
              <td>
                <!-- Schedule information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center">
                <tbody>
                  <tr>
                    <td align="center" class="tbForm1" nowrap>
                       <font color="green"><b><bean:message key="drac.general.serverSettings.success"/></b></font>
                    </td>
                </tr>
                </tbody>
                </table>
              </td>
            </tr>
	    </logic:present>      
      </tbody>
    </table>
    <table cellspacing="0" cellpadding="5" border="0" align="center">
        <tr>
            <td class="tbForm1" nowrap align="right">
                <b><bean:message key="drac.general.serverSettings.confirmationTimeout"/></b>
            </td>
            <td class="tbForm1" nowrap align="left">
                <a href="javascript:void(0)" tabindex="-1" onclick="spinner('confirmation',false)"><img src="/images/arrow_beak_down.gif"></a>
                <html:text size="4" property="confirmationTimeout" tabindex="1" styleId="confirmation" styleClass="gen" errorStyleClass="invalid"/>
                <a href="javascript:void(0)" tabindex="-1" onclick="spinner('confirmation',true)"><img src="/images/arrow_beak_up.gif"></a>
                <bean:message key="drac.text.minutes"/>
            </td>
        </tr>
        <tr>
            <td class="tbForm1" nowrap align="right">
                <b><bean:message key="drac.general.serverSettings.scheduleOffset"/></b>
            </td>
            <td class="tbForm1" nowrap align="left">
                <a href="javascript:void(0)" tabindex="-1" onclick="spinner('offset',false)"><img src="/images/arrow_beak_down.gif"></a>
                <html:text size="4" property="scheduleOffset" tabindex="2" styleId="offset" styleClass="gen" errorStyleClass="invalid"/>
                <a href="javascript:void(0)" tabindex="-1" onclick="spinner('offset',true)"><img src="/images/arrow_beak_up.gif"></a>
                <bean:message key="drac.text.milliseconds"/>
            </td>
        </tr>
    </table>
    <table align="center">
        <tr><td><img src="/images/spacer.gif" height="10"/></td></tr>
        <tr>
            <td>
                <html:submit tabindex="3" >
                    <bean:message key="drac.text.submit"/>
                </html:submit>
            </td>
            <td>
                <html:reset tabindex="4" >
                    <bean:message key="drac.text.reset"/>
                </html:reset>
            </td>
        </tr>
    </table>
</c:if>
<c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'SYSTEM_ADMIN'}">
    <table align="center">
        <tr><td><img src="/images/spacer.gif" height="10"/></td></tr>
        <tr>
            <td class="tbForm1" nowrap align="center">
                <b><font color="red"><bean:message key="drac.general.serverSettings.wrongAuth"/></font></b>
            </td>
        </tr>
    </table>
</c:if>

</html:form>

<script language="javascript">
function spinner(objectId, add)
{
    var elem = document.getElementById(objectId);
    //alert(elem.value);
    if (elem != null) {
        if (elem.disabled == true) return;
        if (isNaN(elem.value))
           elem.value = 10;
        var n = new Number(elem.value);
        if (add) {
            n++;
            elem.value = n.valueOf();
        } else if (n > 1) {
            n--;
            elem.value = n.valueOf();
        }
    }
}
</script>




<%@ include file="/common/footer.jsp" %>

