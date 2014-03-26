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
<%@ page import="com.nortel.appcore.app.drac.common.utility.DateFormatter" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /general/welcome.jsp
 *
 * Description:
 *   This page displays the DRAC warning/welcome text to the user after login.
 *
 ****************************************************************************/

String pageRef = "drac.general.welcome";

%>
<script type="text/javascript" src="/scripts/utilities.js"></script>

<%@ include file="/common/header_struts.jsp" %>

<html:form action="/welcome.do">

<table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
    <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
    <tr>
        <td align="left" valign="middle">
            <jsp:include page='<%= (String) session.getAttribute("drac.general.welcome.file") %>' />
        </td>
    </tr>

    <tr>
        <td><table cellspacing="1" cellpadding="5" border="0" align="center" class="tbmain">
                <tr>
                    <th colspan="2" align="center"><bean:message key="drac.general.welcome.audit"/></th>
                </tr>
                <logic:notEmpty name="passwordExpiry" scope="request">
                <tr align="center">
                    <td class="row1"  colspan="2">
                        <b><span class="genmed"><font color="red">
                        <c:if test="${passwordExpiry gt 0}">
                            <bean:message key="drac.general.welcome.warning.passwordExpiring" arg0="${requestScope['passwordExpiry']}"/>
                        </c:if>
                        <c:if test="${passwordExpiry le 0}">
                            <bean:message key="drac.general.welcome.warning.passwordExpiringToday"/>
                        </c:if>
                        </font><span><br>
                        <a href="/management/changePassword.do"><bean:message key="drac.general.welcome.warning.passwordExpiring.link"/></a></b>
                    </td>
                </tr>
                </logic:notEmpty>
                <tr>
                    <td class="row1" align="left"><b><bean:message key="drac.general.welcome.audit.lastLoginDate"/></b></td>
                    <td class="row1" align="left">${LoginForm.lastLoginDate}</td>
                </tr>
                <tr>
                    <td class="row2" align="left"><b><bean:message key="drac.general.welcome.timezone"/></b></td>
                    <td class="row2" align="left">${LoginForm.timeZone}</td>
                </tr>
                <tr>
                    <td class="row1" align="left"><b><bean:message key="drac.general.welcome.audit.lastLogin"/></b></td>
                    <td class="row1" align="left">${LoginForm.lastLoginAddress}</td>
                </tr>
                <tr>
                    <td class="row2" align="left"><b><bean:message key="drac.general.welcome.audit.numAttempts"/></b></td>
                    <td class="row2" align="left">${LoginForm.numInvalidLogin}</td>
                </tr>
                <tr>
                    <td class="row1" valign="top" align="left"><b><bean:message key="drac.general.welcome.audit.invalidAddress"/></b></td>
                    <td class="row1" align="left">
                        <logic:iterate id="ip" indexId="cnt" name="LoginForm" property="locationOfInvalidAttempts">
                            ${ip}<br>
                        </logic:iterate>
                        &nbsp;
                    </td>
                </tr>

           <!-- A table showing timezone offsets. Remove --
                <tr>
                   <th colspan="2" align="center"><bean:message key="drac.general.welcome.timezone.offsets"/></th>
                </tr>
                <tr>
                    <td class="row1" align="left"><b><bean:message key="drac.general.welcome.timezone.offset.pref"/></b></td>
                    <td class="row1" align="left"><span id="offset1"></span></td>
                </tr>
                <tr>
                    <td class="row2" align="left"><b><bean:message key="drac.general.welcome.timezone.offset.server"/></b></td>
                    <td class="row2" align="left"><span id="offset2"></span></td>
                </tr>
                <tr>
                    <td class="row1" align="left"><b><bean:message key="drac.general.welcome.timezone.offset.local"/></b></td>
                    <td class="row1" align="left"><span id="offset3"></span></td>
                </tr>
           -->

            </table>
        </td>
    </tr>

    <tr>
        <td>&nbsp;</td>
    </tr>

    <logic:messagesPresent message="true">
    <tr>
        <td><table cellspacing="1" cellpadding="5" border="0" align="center" class="tbmain">
            <tr>
                <th colspan="2" align="center"><bean:message key="drac.general.welcome.warning.title"/></th>
            </tr>
            <html:messages id="message" message="true">
            <tr>
                <td align="center" class="row1">
                    <font color="red"><b><bean:write name="message"/></b></font>
                </td>
            </tr>
            </html:messages>
        </table></td>
    </tr>
    </logic:messagesPresent>

    <tr>
        <td>&nbsp;</td>
    </tr>

</table>

</html:form>


<%@ include file="/common/footer.jsp" %>

<!-- Timezone offsets table above. Remove --
<script>

var field = document.getElementById("offset1");
field.innerHTML = getGMTDisplay(<c:out value='${userPreferredTimeZoneOffset}'/>);

field = document.getElementById("offset2");
field.innerHTML = getGMTDisplay(<c:out value='${serverTimeZoneOffset}'/>);

field = document.getElementById("offset3");
field.innerHTML = getGMTDisplay((new Date).getTimezoneOffset());

</script>
-->
