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

<%@ page import="java.util.*,com.nortel.appcore.app.drac.common.security.authentication.types.UserDetails" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
 
<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /common/menu.jsp
 *
 * Description:
 *   This page displays the menu contents.
 *
 ****************************************************************************/

String callingURL = request.getParameter("callingURL");
String refer = "";
if (callingURL != null)
{
  refer = "?callingURL=" + callingURL;
}
%>

    <table border="0" cellspacing="0" cellpadding="0" align="center">
      <tr><td><img src="/images/spacer.gif" border="0" width="5" height="10" /></td></tr>
    </table>
    <table cellspacing="0" cellpadding="0" id="mainMenu" class="ddmx" align="left">
        <tr>
            <td><img src="/images/spacer.gif" border="0" width="5" /></td>
            <!-- General menu. -->
            <td>
                <a class="item1" href="javascript:void(0)"><bean:message key="drac.menu.general"/>&nbsp;&nbsp;<img src="/images/menu_title_beak.gif" /></a>
                <div class="section">
                    <!-- <a class="item2" href="/login.do"><bean:message key="drac.menu.general.login"/></a> -->
                    <a class="item2" href="/welcome.do"><bean:message key="drac.menu.general.welcome"/></a>
                    <a class="item2" href="/serverStatus.do"><bean:message key="drac.menu.general.serverStatus"/></a>
                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'SYSTEM_ADMIN'}">
                        <a class="item2" href="/serverSettings.do"><bean:message key="drac.menu.general.serverSettings"/></a>
                    </c:if>
                    <a class="item2" href="/viewLogs.do"><bean:message key="drac.menu.general.auditLogs"/></a>
                    <a class="item2" href="/reportingOverview.do"><bean:message key="drac.menu.general.reporting"/></a>
                    <a class="item2" href="/logout.do<c:out value="${refer}" />"><bean:message key="drac.menu.general.logout"/></a>
                </div>
            </td>
            <td><img src="/images/spacer.gif" border="0" width="5" /></td>
            <!-- Schedule menu. -->
            <td>
                <a class="item1" href="javascript:void(0)"><bean:message key="drac.menu.schedule"/>&nbsp;&nbsp;<img src="/images/menu_title_beak.gif" /></a>
                <div class="section">
                    <a class="item2" href="/schedule/listSchedules.do"><bean:message key="drac.menu.schedule.list"/></a>
                    <a class="item2" href="/schedule/createSchedule.do"><bean:message key="drac.menu.schedule.create"/></a>
                    <a class="item2" href="/schedule/createAdvancedSchedule.do"><bean:message key="drac.menu.schedule.createAdvanced"/></a>
                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                        <a class="item2" href="/schedule/listCreateProgress.do"><bean:message key="drac.menu.schedule.createProgress.list"/></a>
                    </c:if>
                    <a class="item2" href="/schedule/advancedSearch.do"><bean:message key="drac.menu.schedule.advanced"/></a>
                </div>
            </td>
            <td><img src="/images/spacer.gif" border="0" width="5" /></td>
            <!-- Service menu. -->
            <td>
                <a class="item1" href="javascript:void(0)"><bean:message key="drac.menu.service"/>&nbsp;&nbsp;<img src="/images/menu_title_beak.gif" /></a>
                <div class="section">
                    <a class="item2" href="/service/listServices.do"><bean:message key="drac.menu.service.manage.list"/></a>
                    <a class="item2" href="/service/addServiceList.do"><bean:message key="drac.menu.service.manage.add"/></a>
                    <a class="item2" href="/service/listAlarms.do"><bean:message key="drac.menu.service.alarms"/></a>
                   <!-- <a class="item2" href="/notImplemented.do"><bean:message key="drac.menu.service.advanced"/></a> --> 
                </div>
            </td>
            <td><img src="/images/spacer.gif" border="0" width="5"/></td>
            <!-- Network menu. -->
            <td>
                <a class="item1" href="javascript:void(0)"><bean:message key="drac.menu.network"/>&nbsp;&nbsp;<img src="/images/menu_title_beak.gif" /></a>
                <div class="section">
                    <a class="item2" href="/network/listEndpoints.do"><bean:message key="drac.menu.network.list"/></a>
                    <a class="item2" href="/network/queryUtilization.do"><bean:message key="drac.menu.network.utilization"/></a>
                   <!-- <a class="item2" href="/notImplemented.do?pageName=drac.menu.network.advanced"><bean:message key="drac.menu.network.advanced"/></a> -->
                </div>
            </td>
            <td><img src="/images/spacer.gif" border="0" width="5"/></td>
            <!-- Security menu. -->
            <!-- Display only menu items the user has access to based on permissions. -->
            <td>
                <a class="item1" href="javascript:void(0)"><bean:message key="drac.menu.security"/>&nbsp;&nbsp;<img src="/images/menu_title_beak.gif" /></a>
                <div class="section">
                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                        <a class="item2 arrow" href="javascript:void(0)"><bean:message key="drac.menu.security.userManagement"/><img src="/images/menu_arrow1.gif" width="10" height="12" alt="" /></a>
                        <div class="section">
                            <a class="item2" href="/management/userManagement/listUsers.do"><bean:message key="drac.menu.security.userManagement.list"/></a>
                            <a class="item2" href="/management/userManagement/createUser.do"><bean:message key="drac.menu.security.userManagement.create"/></a>
                        </div>
                        <a class="item2 arrow" href="javascript:void(0)"><bean:message key="drac.menu.security.userGroupManagement"/><img src="/images/menu_arrow1.gif" width="10" height="12" alt="" /></a>
                        <div class="section">
                            <a class="item2" href="/management/userGroupManagement/listUserGroups.do"><bean:message key="drac.menu.security.userGroupManagement.list"/></a>
                            <a class="item2" href="/management/userGroupManagement/createUserGroup.do"><bean:message key="drac.menu.security.userGroupManagement.create"/></a>
                        </div>
                        <a class="item2 arrow" href="javascript:void(0)"><bean:message key="drac.menu.security.resourceGroupManagement"/><img src="/images/menu_arrow1.gif" width="10" height="12" alt="" /></a>
                        <div class="section">
                            <a class="item2" href="/management/resourceGroupManagement/listResourceGroups.do"><bean:message key="drac.menu.security.resourceGroupManagement.list"/></a>
                            <a class="item2" href="/management/resourceGroupManagement/createResourceGroup.do"><bean:message key="drac.menu.security.resourceGroupManagement.create"/></a>
                        </div>
                        <a class="item2" href="/management/globalPolicy.do"><bean:message key="drac.menu.security.globalPolicy"/></a>
                    </c:if>
                    <a class="item2" href="/management/viewUserProfile.do"><bean:message key="drac.menu.security.viewUserProfile"/></a>
                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                        <a class="item2" href="/management/userGroupManagement/listUserGroups.do"><bean:message key="drac.menu.security.userGroupManagement.list.user"/></a>
                        <a class="item2" href="/management/resourceGroupManagement/listResourceGroups.do"><bean:message key="drac.menu.security.resourceGroupManagement.list.user"/></a>
                    </c:if>
                    <a class="item2" href="/management/changePassword.do"><bean:message key="drac.menu.security.changePassword"/></a>
                </div>
            </td>
            <td><img src="/images/spacer.gif" border="0" width="5"/></td>
            <!-- Language menu. -->
            <!--td>
                <a class="item1" href="javascript:void(0)"><bean:message key="drac.menu.language"/>&nbsp;&nbsp;<img src="/images/menu_title_beak.gif" /></a>
                <div class="section">
                    <a class="item2" href="/language.do?language=en&country=US&CSRFToken=<c:out value='${sessionScope["CSRFToken"]}'/>"><bean:message key="drac.menu.language.english"/></a>
                    <a class="item2" href="/language.do?language=de&country=DE&CSRFToken=<c:out value='${sessionScope["CSRFToken"]}'/>"><bean:message key="drac.menu.language.german"/></a>
                    <a class="item2" href="/language.do?language=es&country=MX&CSRFToken=<c:out value='${sessionScope["CSRFToken"]}'/>"><bean:message key="drac.menu.language.spanish"/></a>
                    <a class="item2" href="/language.do?language=fr&country=FR&CSRFToken=<c:out value='${sessionScope["CSRFToken"]}'/>"><bean:message key="drac.menu.language.french"/></a>
                    <a class="item2" href="/language.do?language=it&country=IT"><bean:message key="drac.menu.language.italian"/></a>
                </div>
            </td-->
            <td><img src="/images/spacer.gif" border="0" width="5"/></td>
            <!-- Help menu. -->
            <td>
                <a class="item1" href="javascript:void(0)"><bean:message key="drac.menu.help"/>&nbsp;&nbsp;
                <img src="/images/menu_title_beak.gif" /></a>
                <div class="section">
                    <%-- <a class="item2" href="/help/help.do"><bean:message key="drac.menu.help.help"/></a>--%>
                    <!-- a class="item2" href="/help/release.do"><bean:message key="drac.menu.help.release"/></a-->
                    <a class="item2"href="/help/download.do"><bean:message key="drac.menu.help.download"/></a>
                    <a class="item2" href="/help/browser.do"><bean:message key="drac.menu.help.browser"/></a>
                    <a class="item2" href="/help/about.do"><bean:message key="drac.menu.help.about"/></a>
                </div>
            </td>
            <td><img src="/images/spacer.gif" border="0" width="5"/></td>
        </tr>
    </table>
