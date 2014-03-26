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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /management/userManagement/userDetails.jsp
 * Author: Colin Hart
 *
 * Description:
 *  This page returns read-only details of a user.
 *
 * Note:
 *  This page has basically the same contents as /management/userManagement/createUserResult.jsp
 *
 ****************************************************************************/

String pageRef = "drac.security.userManagement.detail";

%>

<%@ include file="/common/header_struts.jsp" %>

<script type="text/javascript" src="/scripts/tabpane.js"></script>

<html:form action="/management/userManagement/queryUser.do" >
    <table cellspacing="0" cellpadding="0" border="0" width="590" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5" /></td>
        </tr>
        <logic:messagesPresent message="true">
        <html:messages id="message" message="true">
        <tr>
            <td align="center" class="gen" colspan="3">
                <font color="red"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
        </logic:messagesPresent>
        <logic:messagesNotPresent message="true">
        <tr>
            <td>
                <div align="center">
                    <b><html:link href="/management/userManagement/editUser.do"
                    paramId="uid" paramName="UserForm" paramProperty="userID"><bean:message key="drac.security.userManagement.detail.editLabel"/></html:link></b>
                </div>
            </td>
        </tr>
        
        <tr>
            <td>
                <!-- Header -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center>
                            <b><bean:message key="drac.security.userManagement.detail.userInformation"/></b></center>
                        </td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- user information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                <logic:notEmpty name="UserForm" scope="request">
                    <tr>
                        <td colspan="5" align="left" valign="top" class="row2">
                            <div class="tab-pane" id="userPane" align="center">
                                <script type="text/javascript">
                                tp2 = new WebFXTabPane(document.getElementById("userPane"));
                                </script>

                                <div class="tab-page" id="userDetailsTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.userManagement.detail.user"/></h2>
                                    <script type="text/javascript">tp2.addTabPage(document.getElementById("userDetailsTab"));</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userDetailsTable">
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.userID"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <b>${fn:escapeXml(UserForm.userID)}</b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.authentication.accountState"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="accountState">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="accountState">
                                                    <b>${fn:escapeXml(UserForm.accountState)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <c:if test="${UserForm.accountState eq 'enabled'}">
                                            <td class="tbForm1" nowrap>
                                                &nbsp;
                                            </td>
                                            </c:if>
                                            <c:if test="${UserForm.accountState eq 'disabled'}">
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.disabledReason"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="disabledReason">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="disabledReason">
                                                    <b>${fn:escapeXml(UserForm.disabledReason)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            </c:if>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.authentication.authenticationType"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="authenticationType">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="authenticationType">
                                                    <b>${fn:escapeXml(UserForm.authenticationType)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <c:if test="${UserForm.authenticationType eq 'INTERNAL'}">
                                        <tr>
                                            <td valign="top" class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.internalAuthentication.expirationDate"/>
                                            </td>
                                            <td valign="top" class="tbForm1" nowrap>
                                                <b>${fn:escapeXml(UserForm.expirationDate)}</b>
                                            </td>
                                        </tr>
                                        </c:if>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.creationDate"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <b>${fn:escapeXml(UserForm.creationDate)}</b>&nbsp;<bean:message key="drac.text.at"/>&nbsp;<b>${fn:escapeXml(UserForm.creationTime)}</b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.lastModifiedDate"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <b>${fn:escapeXml(UserForm.lastModifiedDate)}</b>&nbsp;<bean:message key="drac.text.at"/>&nbsp;<b>${fn:escapeXml(UserForm.lastModifiedTime)}</b>
                                            </td>
                                        </tr>

                                    </table>
                                </div>  <!-- end div user details -->
    
                                <div class="tab-page" id="personalDataTab">
                                    <h2 class="tab"><bean:message key="drac.security.userManagement.create.personalData"/></h2>
                                    <script type="text/javascript">tp2.addTabPage(document.getElementById("personalDataTab"));</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" align="center" id="resourceStateTable">
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.surname"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="surname">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="surname">
                                                    <b>${fn:escapeXml(UserForm.surname)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.email"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="email">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="email">
                                                    <b>${fn:escapeXml(UserForm.email)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap width="20%">
                                                <bean:message key="drac.security.userManagement.detail.givenName"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="givenName">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="givenName">
                                                    <b>${fn:escapeXml(UserForm.givenName)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <td class="tbForm1" nowrap width="20%">
                                                <bean:message key="drac.security.userManagement.create.phone"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="phone">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="phone">
                                                    <b>${fn:escapeXml(UserForm.phone)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.commonName"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="commonName">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="commonName">
                                                    <b>${fn:escapeXml(UserForm.commonName)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.postalAddress"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="postalAddress">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="postalAddress">
                                                    <b>${fn:escapeXml(UserForm.postalAddress)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.title"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="title">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="title">
                                                    <b>${fn:escapeXml(UserForm.title)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.description"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="description">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="description">
                                                    <b>${fn:escapeXml(UserForm.description)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <c:if test="${UserForm.authenticationType eq 'INTERNAL'}">
                                        <tr>
                                            <td class="tbForm1" colspan="4" align="right">
                                                <img src="/images/spacer.gif" height="48">
                                            </td>
                                        </tr>
                                        </c:if>
                                        <c:if test="${UserForm.authenticationType eq 'A_SELECT'}">
                                        <tr>
                                            <td class="tbForm1" colspan="4" align="right">
                                                <img src="/images/spacer.gif" height="24">
                                            </td>
                                        </tr>
                                        </c:if>
                                    </table>
                                </div> <!-- end div personal data -->
    
                                <div class="tab-page" id="organizationTab">
                                    <h2 class="tab"><bean:message key="drac.security.userManagement.create.organizationData"/></h2>
                                    <script type="text/javascript">tp2.addTabPage(document.getElementById("organizationTab"));</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" align="center" id="organizationTable">
                                        <tr>
                                            <td class="tbForm1" nowrap width="20%">
                                                <bean:message key="drac.security.userManagement.detail.organization.orgName"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="orgName">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="orgName">
                                                    <b>${fn:escapeXml(UserForm.orgName)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <td class="tbForm1" nowrap width="20%">
                                                <bean:message key="drac.security.userManagement.detail.organization.orgUnitName"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="orgUnitName">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="orgUnitName">
                                                    <b>${fn:escapeXml(UserForm.orgUnitName)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.organization.owner"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="owner">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="owner">
                                                    <b>${fn:escapeXml(UserForm.owner)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.organization.orgDescription"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="orgDescription">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="orgDescription">
                                                    <b>${fn:escapeXml(UserForm.orgDescription)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.organization.seeAlso"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="seeAlso">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="seeAlso">
                                                    <b>${fn:escapeXml(UserForm.seeAlso)}</b>
                                                </logic:notEmpty>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.detail.organization.category"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="category">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="category">
                                                    <b>${fn:escapeXml(UserForm.category)}</b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <c:if test="${UserForm.authenticationType eq 'INTERNAL'}">
                                        <tr>
                                            <td class="tbForm1" colspan="4" align="right">
                                                <img src="/images/spacer.gif" height="70">
                                            </td>
                                        </tr>
                                        </c:if>
                                        <c:if test="${UserForm.authenticationType eq 'A_SELECT'}">
                                        <tr>
                                            <td class="tbForm1" colspan="4" align="right">
                                                <img src="/images/spacer.gif" height="32">
                                            </td>
                                        </tr>
                                        </c:if>
                                    </table>
                                </div>  <!-- end div org data -->
    
                                <div class="tab-page" id="preferencesTab">
                                    <h2 class="tab"><bean:message key="drac.security.userManagement.detail.preferences"/></h2>
                                    <script type="text/javascript">tp2.addTabPage( document.getElementById( "preferencesTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="preferencesTable">
                                        <tr>
                                            <td class="tbForm1" align="left" width="120">
                                                <bean:message key="drac.security.userManagement.create.timezone"/>
                                            </td>
                                            <td class="tbForm1" align="left">
                                                <b>${fn:escapeXml(UserForm.timeZone)}</b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="110">
                                            </td>
                                        </tr>
                                    </table>
                                </div>  <!-- end preferences data -->

                                <div class="tab-page" id="userGroupMembershipTab">
                                    <h2 class="tab"><bean:message key="drac.security.userManagement.create.userGroupMembership"/></h2>
                                    <script type="text/javascript">tp2.addTabPage(document.getElementById("userGroupMembershipTab"));</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userGroupMembershipTable">
                                        <tr>
                                            <td class="tbForm1" nowrap width="30%">
                                                <bean:message key="drac.security.userManagement.create.userGroups.memberUserGroups"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <logic:empty name="UserForm" property="memberUserGroupName">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="UserForm" property="memberUserGroupName">
                                                <logic:iterate id="memberUserGroupName" name="UserForm" property="memberUserGroupName">
                                                    <html:link href="/management/userGroupManagement/queryUserGroup.do"
                                                               paramId="ugName" paramName="memberUserGroupName"><b>${fn:escapeXml(memberUserGroupName)}</b></html:link><br>
                                                </logic:iterate>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <c:if test="${UserForm.authenticationType eq 'INTERNAL'}">
                                        <tr>
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="122">
                                            </td>
                                        </tr>
                                        </c:if>
                                        <c:if test="${UserForm.authenticationType eq 'A_SELECT'}">
                                        <tr>
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="100">
                                            </td>
                                        </tr>
                                        </c:if>
                                    </table>
                                </div>  <!-- end div user group membership -->

                                <div class="tab-page" id="policyDetailsTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.userManagement.edit.accountPolicy"/></h2>
                                    <script type="text/javascript">tp2.addTabPage( document.getElementById( "policyDetailsTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="policyDetailsTable">
                                        <tr>
                                            <td class="tbForm1" nowrap colspan="4" id="dormantPeriodTD">
                                                <bean:message key="drac.security.userManagement.detail.dormancy"/>
                                                <logic:notEmpty name="UserForm" property="dormantPeriodGP">
                                                    <b>${fn:escapeXml(UserForm.dormantPeriodGP)}</b>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="dormantPeriod">
                                                    <b><font color="red">${fn:escapeXml(UserForm.dormantPeriod)}</font></b>
                                                </logic:notEmpty>
                                                <bean:message key="drac.security.globalPolicy.inactiveDays"/>
                                            </td>
                                        </tr>
    
                                        <c:if test="${UserForm.authenticationType eq 'INTERNAL'}">
                                        <tr id="aSelectFillerRow" style="display:none">
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="215">
                                            </td>
                                        </tr>
                                        <tr id="internalAccountRow2">
                                            <td class="tbForm1" nowrap width="20%">
                                                <bean:message key="drac.security.userManagement.create.inactivityPeriod"/>
                                            </td>
                                            <td class="tbForm1" nowrap align="right" id="inactivityPeriodTD">
                                                <logic:notEmpty name="UserForm" property="inactivityPeriodGP">
                                                    <b>${fn:escapeXml(UserForm.inactivityPeriodGP)}</b>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="inactivityPeriod">
                                                    <b><font color="red">${fn:escapeXml(UserForm.inactivityPeriod)}</font></b>
                                                </logic:notEmpty>
                                                <c:if test="${UserForm.inactivityMetric eq '0'}">
                                                    <bean:message key="drac.security.userManagement.create.seconds"/>
                                                </c:if>
                                                <c:if test="${UserForm.inactivityMetric eq '1'}">
                                                    <bean:message key="drac.security.userManagement.create.minutes"/>
                                                </c:if>
                                                <c:if test="${UserForm.inactivityMetric eq '2'}">
                                                    <bean:message key="drac.security.userManagement.create.hours"/>
                                                </c:if>
                                                <c:if test="${UserForm.inactivityMetric eq '3'}">
                                                    <bean:message key="drac.security.userManagement.create.days"/>
                                                </c:if>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.maxInvalidLoginAttempts"/>
                                            </td>
                                            <td class="tbForm1" nowrap align="right" id="maxInvalidLoginAttemptsTD">
                                                <logic:notEmpty name="UserForm" property="maxInvalidLoginAttemptsGP">
                                                    <b>${fn:escapeXml(UserForm.maxInvalidLoginAttemptsGP)}</b>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="maxInvalidLoginAttempts">
                                                    <b><font color="red">${fn:escapeXml(UserForm.maxInvalidLoginAttempts)}</font></b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <tr id="internalAccountRow3">
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.lockoutPeriod"/>
                                            </td>
                                            <td class="tbForm1" nowrap align="right" id="lockoutPeriodTD">
                                                <logic:notEmpty name="UserForm" property="lockoutPeriodGP">
                                                    <b>${fn:escapeXml(UserForm.lockoutPeriodGP)}</b>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="lockoutPeriod">
                                                    <b><font color="red">${fn:escapeXml(UserForm.lockoutPeriod)}</font></b>
                                                </logic:notEmpty>
                                                <c:if test="${UserForm.lockoutMetric eq '0'}">
                                                    <bean:message key="drac.security.userManagement.create.seconds"/>
                                                </c:if>
                                                <c:if test="${UserForm.lockoutMetric eq '1'}">
                                                    <bean:message key="drac.security.userManagement.create.minutes"/>
                                                </c:if>
                                                <c:if test="${UserForm.lockoutMetric eq '2'}">
                                                    <bean:message key="drac.security.userManagement.create.hours"/>
                                                </c:if>
                                                <c:if test="${UserForm.lockoutMetric eq '3'}">
                                                    <bean:message key="drac.security.userManagement.create.days"/>
                                                </c:if>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.passwordHistorySize"/>
                                            </td>
                                            <td class="tbForm1" nowrap align="right" id="passwordHistorySizeTD">
                                                <logic:notEmpty name="UserForm" property="passwordHistorySizeGP">
                                                    <b>${fn:escapeXml(UserForm.passwordHistorySizeGP)}</b>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="passwordHistorySize">
                                                    <b><font color="red">${fn:escapeXml(UserForm.passwordHistorySize)}</font></b>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        <tr id="internalAccountRow6">
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.passwordAging"/>
                                            </td>
                                            <td class="tbForm1" nowrap align="right" id="passwordAgingTD">
                                                <logic:notEmpty name="UserForm" property="passwordAgingGP">
                                                    <b>${fn:escapeXml(UserForm.passwordAgingGP)}</b>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="passwordAging">
                                                    <b><font color="red">${fn:escapeXml(UserForm.passwordAging)}</font></b>
                                                </logic:notEmpty>
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.passwordExpirationNotification"/>
                                            </td>
                                            <td class="tbForm1" nowrap id="passwordExpirationNotificationTD">
                                                <logic:notEmpty name="UserForm" property="passwordExpirationNotificationGP">
                                                    <b>${fn:escapeXml(UserForm.passwordExpirationNotificationGP)}</b>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="passwordExpirationNotification">
                                                    <b><font color="red">${fn:escapeXml(UserForm.passwordExpirationNotification)}</font></b>
                                                </logic:notEmpty>
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </td>
                                        </tr>
                                        <tr id="internalAccountRow6">
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.lockedClientIPs"/>
                                            </td>
                                            <td class="tbForm1" nowrap align="right" id="lockedClientIPsTD">
                                                <logic:notEmpty name="UserForm" property="lockedClientIPsGP">
                                                <logic:iterate id="lockedClientIPsGP" name="UserForm" property="lockedClientIPsGP">
                                                    <b>${fn:escapeXml(lockedClientIPsGP)}</b><br>
                                                </logic:iterate>
                                                </logic:notEmpty>
                                                <logic:notEmpty name="UserForm" property="lockedClientIPs">
                                                <logic:iterate id="lockedClientIPs" name="UserForm" property="lockedClientIPs">
                                                    <b><font color="red">${fn:escapeXml(lockedClientIPs)}</b></font><br>
                                                </logic:iterate>
                                                </logic:notEmpty>
                                                <logic:empty name="UserForm" property="lockedClientIPsGP">
                                                    <logic:empty name="UserForm" property="lockedClientIPs">
                                                        <b><bean:message key="drac.text.none"/></b>
                                                    </logic:empty>
                                                </logic:empty>
                                            </td>
                                        </tr>
                                        </c:if>
                                        <tr id="infoForLocalAndGlobalPolicy" style="display:none">
                                            <td class="tbForm1" nowrap colspan="4">
                                                <bean:message key="drac.security.userManagement.detail.footnoteLocalandGlobal0"/><br>
                                                <bean:message key="drac.security.userManagement.detail.footnoteLocalandGlobal1"/>
                                            </td>
                                        </tr>
                                        <tr id="infoForLocalPolicy" style="display:none">
                                            <td class="tbForm1" nowrap colspan="4">
                                                <bean:message key="drac.security.userManagement.detail.footnoteLocal"/>
                                            </td>
                                        </tr>
                                        <tr id="infoForGlobalPolicy">
                                            <td class="tbForm1" nowrap colspan="4">
                                                <bean:message key="drac.security.userManagement.detail.footnoteGlobal"/>
                                            </td>
                                        </tr>
                                        <c:if test="${UserForm.authenticationType eq 'A_SELECT'}">
                                        <tr>
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="100">
                                            </td>
                                        </tr>
                                        </c:if>
                                    </table>
                                </div>  <!-- end div account policy details -->
    
                            </div>
                        </td>
                    </tr>
                </logic:notEmpty>
                </tbody>
                </table>
            </td>
        </tr>

        <tr>
            <td>
                <!-- Drop shadow. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbl">
                    <tbody>
                        <tr>
                            <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                            <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                            <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </logic:messagesNotPresent>
    </tbody>
</table>

</html:form>

<script language="javascript">

//
// Decide if the user is using the global policy.
// Look at all global policy elements.
//
function checkWhichPolicyBeingUsed(text) {
    var usingGlobalPolicy = false;
    var usingLocalPolicy = false;
    var usageIndicator = "";

    if (document.getElementById("inactivityPeriodTD") == null) {
           //document.UserForm.authenticationType.value == "A-Select") {
        if (document.getElementById("dormantPeriodTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
    } else {
        if (document.getElementById("dormantPeriodTD").innerHTML.indexOf(text) == -1) {
            // This element is not displaying the local account policy value;
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("inactivityPeriodTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("maxInvalidLoginAttemptsTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("lockoutPeriodTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("lockedClientIPsTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("passwordAgingTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("passwordExpirationNotificationTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("passwordHistorySizeTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
    }

    if (usingLocalPolicy && usingGlobalPolicy) {
        usageIndicator = "3";
    } else if (usingLocalPolicy) {
        usageIndicator = "2";
    } else {
        usageIndicator = "1";
    }

    return usageIndicator;
}

//
// Decide which footnote to display on the Account Policy tab.
// Will either be the one regarding mixed local and global policy or
// local policy only or the one regarding global policy only.
//
function toggleDisplayFootnote() {
    var policyUsage;

    // Check if we have at least one policy details element on the page.
    // We may not have if the user is not able to view this users details.
    if (document.getElementById("dormantPeriodTD") != null) {
        // Browser check included here as I could not find how to use id tag in the notEmpty Struts tag.
        if (navigator.userAgent.indexOf("MSIE") != -1) {
            policyUsage = checkWhichPolicyBeingUsed("FONT color=red");
        } else if (navigator.userAgent.indexOf("Firefox") != -1) {
            policyUsage = checkWhichPolicyBeingUsed("font color=\"red\"");
        }
        
        if (policyUsage == "3") {
            // Account Policy elements are defined locally and globally.
            document.getElementById("infoForLocalAndGlobalPolicy").style.display = "";
            document.getElementById("infoForGlobalPolicy").style.display = "none";
            document.getElementById("infoForLocalPolicy").style.display = "none";
        } else if (policyUsage == "2") {
            // Account Policy elements are defined locally only.
            document.getElementById("infoForLocalAndGlobalPolicy").style.display = "none";
            document.getElementById("infoForGlobalPolicy").style.display = "none";
            document.getElementById("infoForLocalPolicy").style.display = "";
        } else if (policyUsage == "1") {
            // Account Policy elements are defined globally only.
            document.getElementById("infoForLocalAndGlobalPolicy").style.display = "none";
            document.getElementById("infoForGlobalPolicy").style.display = "";
            document.getElementById("infoForLocalPolicy").style.display = "none";
        }
    }
}

// onload
toggleDisplayFootnote();

</script>

<%@ include file="/common/footer.jsp"%>
