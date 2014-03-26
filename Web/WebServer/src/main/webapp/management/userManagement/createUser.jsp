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
             import="com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.*"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<%
/****************************************************************************
 * OpenDRAC WEB GUI
 *
 * File: /management/userManagement/createUser.jsp
 * Author: Colin Hart
 *
 * Description:
 *  This page allows user to create a new user
 *
 ****************************************************************************/

String pageRef = "drac.security.userManagement.create"; %>

<%@ include file="/common/header_struts.jsp" %>

<script type="text/javascript" src="/scripts/tabpane.js"></script>

<%
    // Set the form-bean as an object we can use in any scriplets.
    CreateUserForm createForm = (CreateUserForm) request.getAttribute("CreateUserForm");
%>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<html:form method="POST" action="/management/userManagement/handleCreateUser.do" onsubmit="return doSubmit()">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
    <table cellspacing="0" cellpadding="0" border="0" width="690" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5"/></td>
        </tr>
        <logic:notPresent name="editable">
        <tr>
            <td align="center"><bean:message key="drac.security.userManagement.create.text.notAllowed"/></td>
        </tr>
        </logic:notPresent>
        <logic:present name="editable">
        <logic:messagesPresent>
        <tr>
            <td>
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.security.userManagement.create.title.errors"/></b></font></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- User information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
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
        </logic:messagesPresent>
        <logic:messagesPresent message="true">
        <tr>
            <td>
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.security.userManagement.create.title.errors"/></b></font></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- User information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <html:messages id="message" message="true">
                        <tr>
                            <td align="center" class="tbForm1" nowrap><font color="red"><b><bean:write name="message"/></b></font></td>
                        </tr>
                    </html:messages>
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
        </logic:messagesPresent>

        <tr>
            <td align="center"><bean:message key="drac.security.userManagement.create.text"/></td>
        </tr>

        <tr>
            <td>
                <!-- Header -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.userManagement.create.title.info"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>

        <tr>
            <td>
            <!-- user information contents. -->
            <table cellspacing="0" cellpadding="5" border="0" align="center" width="500" class="tbForm">
            <tbody>
                <tr>
                    <td colspan="5" align="left" valign="top" class="row2">
                        <div class="tab-pane" id="editUserPane" align="center">
                            <script type="text/javascript">
                            tp2 = new WebFXTabPane( document.getElementById( "editUserPane" ) );
                            </script>

                            <div class="tab-page" id="userDetailsTab" align="center">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.edit.user"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "userDetailsTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userDetailsTable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.userID"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="1" style="width: 175px" property="userID" errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.accountState"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:select tabindex="2" property="accountState" styleId="accountStateSelect" onchange="handleChangeAccountState()" >
                                                <html:option value="0"><bean:message key="drac.security.userManagement.create.accountState.enabled"/></html:option>
                                                <html:option value="1"><bean:message key="drac.security.userManagement.create.accountState.disabled"/></html:option>
                                            </html:select>
                                        </td>
                                    </tr>
                                    <tr id="disabledLayer" style="display:none">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.disabledReason"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="3" style="width: 175px" property="disabledReason" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.wsdlCertificate"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:password tabindex="4" style="width: 175px" property="wsdlCertificate"  errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.authenticationType"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:select tabindex="5" property="authType" styleId="authenticationTypeSelect" onchange="handleChangeAuthenticationType()" >
                                                <logic:iterate id="auth" name="CreateUserForm" property="availAuthenticationTypes">
                                                    <html:option value="${auth}">${auth}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </td>
                                    </tr>
                                    <tr id="password1Col">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.userPassword"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:password tabindex="6" style="width: 175px" property="userPassword" styleId="userPassword"  errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    <tr id="password2Col">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.confirmUserPassword"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:password tabindex="7" style="width: 175px" property="confirmUserPassword" styleId="confirmUserPassword"  errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="45">
                                        </td>
                                    </tr>
                                </table>
                            </div>  <!-- end div user details -->

                            <div class="tab-page" id="personalDataTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.create.personalData"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "personalDataTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceStateTable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.surname"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="1" style="width: 175px" property="surname" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.email"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="5" style="width: 175px" property="email" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                          <bean:message key="drac.security.userManagement.create.givenName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="2" style="width: 175px" property="givenName" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.phone"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="6" style="width: 175px" property="phone" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.commonName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="3" style="width: 175px" property="commonName" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.postalAddress"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="7" style="width: 175px" property="postalAddress" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.title"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="4" style="width: 175px" property="title" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.description"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="8" style="width: 175px" property="description" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="109">
                                        </td>
                                    </tr>
                                </table>
                            </div> <!-- end div personal data -->

                            <div class="tab-page" id="organizationTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.detail.organizationData"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "organizationTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="organizationTable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                             <html:text tabindex="1" style="width: 175px" property="orgName"  errorStyleClass="invalid"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgUnitName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="4" style="width: 175px" property="orgUnitName"  errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.owner"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                                <html:text tabindex="2" style="width: 175px" property="owner" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgDescription"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="5" style="width: 175px" property="orgDescription" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.seeAlso"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="3" style="width: 175px" property="seeAlso" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.category"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="6" style="width: 175px" property="category" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="141">
                                        </td>
                                    </tr>
                                </table>
                            </div>  <!-- end div org data -->

                            <div class="tab-page" id="preferencesTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.detail.preferences"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "preferencesTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="preferencesTable">
                                    <tr>
                                        <td class="tbForm1" align="left">
                                            <bean:message key="drac.security.userManagement.create.timezone"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:select tabindex="1" property="timeZone">
                                                <logic:iterate id="timeZoneId" indexId="i" name="CreateUserForm" property="timeZoneIds">
                                                    <html:option value="${timeZoneId}">${CreateUserForm.timeZoneNames[i]}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="205">
                                        </td>
                                    </tr>
                                </table>
                            </div>  <!-- end preferences data -->

                            <div class="tab-page" id="userGroupMembershipTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.create.userGroupMembership"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "userGroupMembershipTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userGroupMembershipTable">
                                <tr>
                                    <td class="tbForm1" align="center">
                                        <bean:message key="drac.security.userManagement.create.userGroups.availableUserGroups"/><br>
                                        <html:select tabindex="1" property="availableUserGroups" size="5" style="width:200px" styleId="availableUserGroups" multiple="true" styleClass="gen">
                                            <html:options property="availableUserGroups"/>
                                        </html:select><br>
                                        <a href="javascript:void(0)" tabindex="-1" onclick="return viewUserGroup('availableUserGroups')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                    </td>
                                    <td class="tbForm1" align="center" valign="middle">
                                        <input tabindex="2" type="button" id="addUserToMembership" onclick="moveItems('availableUserGroups','userGroupsMembers');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><p>
                                        <input tabindex="2" type="button" id="addUserToMembership" onclick="moveItems('userGroupsMembers','availableUserGroups');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
                                    </td>
                                    <td class="tbForm1" align="center">
                                        <bean:message key="drac.security.userManagement.create.userGroups.memberUserGroups"/><br>
                                        <html:select tabindex="3" property="memberUserGroups" size="5" style="width:200px" styleId="userGroupsMembers" multiple="true" styleClass="gen">
                                            <html:options property="memberUserGroups"/>
                                        </html:select><br>
                                        <a href="javascript:void(0)" tabindex="-1" onclick="return viewUserGroup('userGroupsMembers')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="tbForm1" colspan="3">
                                        <img src="/images/spacer.gif" height="130">
                                    </td>
                                </tr>
                                </table>
                            </div>  <!-- end div user group membership -->

                            <div class="tab-page" id="policyDetailsTab" align="center">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.edit.accountPolicy"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "policyDetailsTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="policyDetailsTable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.dormantPeriod"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="1" styleId="dormantPeriodField" style="width: 55px" property="dormantPeriod" />
                                            <bean:message key="drac.security.userManagement.create.days"/>
                                        </td>
                                    </tr>
                                    <tr id="aSelectFillerRow" style="display:none">
                                        <td class="tbForm1" colspan="2" align="right">
                                            <img src="/images/spacer.gif" height="215">
                                        </td>
                                    </tr>
                                    <tr id="internalAccountRow1">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.inactivityPeriod"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="2" style="width: 55px" styleId="inactivityPeriodField" property="inactivityPeriod" />
                                            <html:select tabindex="3" property="inactivityMetric" >
                                                <html:option value="0"><bean:message key="drac.security.globalPolicy.metric.seconds"/></html:option>
                                                <html:option value="1"><bean:message key="drac.security.globalPolicy.metric.minutes"/></html:option>
                                                <html:option value="2"><bean:message key="drac.security.globalPolicy.metric.hours"/></html:option>
                                                <html:option value="3"><bean:message key="drac.security.globalPolicy.metric.days"/></html:option>
                                            </html:select>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordHistorySize"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="4" style="width: 55px" styleId="passwordHistorySizeField" property="passwordHistorySize" />
                                        </td>
                                    </tr>
                                    <tr id="internalAccountRow2">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.maxInvalidLoginAttempts"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="5" style="width: 55px" styleId="maxInvalidLoginField" property="maxInvalidLoginAttempts" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.lockoutPeriod"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="6" style="width: 55px" styleId="lockoutPeriodField" property="lockoutPeriod" />
                                            <html:select tabindex="7" property="lockoutMetric" >
                                                <html:option value="0"><bean:message key="drac.security.globalPolicy.metric.seconds"/></html:option>
                                                <html:option value="1"><bean:message key="drac.security.globalPolicy.metric.minutes"/></html:option>
                                                <html:option value="2"><bean:message key="drac.security.globalPolicy.metric.hours"/></html:option>
                                                <html:option value="3"><bean:message key="drac.security.globalPolicy.metric.days"/></html:option>
                                            </html:select>
                                        </td>
                                    </tr>
                                    <tr id="internalAccountRow3">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordAging"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="8" style="width: 55px" styleId="passwordAgingField" property="passwordAging" />
                                            <bean:message key="drac.security.userManagement.create.days"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordExpirationNotification"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="9" style="width: 55px" styleId="passwordExpirationNotifcationField" property="passwordExpirationNotification" />
                                            <bean:message key="drac.security.userManagement.create.days"/>
                                        </td>
                                    </tr>
                                    <tr id="lockedIPsRow">
                                        <td class="tbForm1" nowrap colspan="4">
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceGroupMemberTable">
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                        <bean:message key="drac.security.userManagement.create.clientIPToLock"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <html:text tabindex="10" style="width: 155px" styleId="lockedIPsField" property="clientIPToLock" />
                                                    </td>
                                                    <td class="tbForm1" align="center" valign="middle">
                                                        <input tabindex="11" type="button" id="addResGrpToMembership" onclick="moveIPItems('lockedIPsField','lockedClientIPsList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.userManagement.create.lockedClientIPs"/><br>
                                                        <html:select tabindex="12" property="lockedClientIPs" size="5" style="width:155px" styleId="lockedClientIPsList" multiple="true">
                                                            <html:options property="lockedClientIPs"/>
                                                        </html:select>
                                                    </td>
                                                    <td class="tbForm1" align="center" valign="middle">
                                                        &nbsp;<br><input tabindex="13" type="button" id="removeResGrpFromMembership" onclick="removeIPItems('lockedClientIPsList');" value="<bean:message key='drac.security.policy.button.removeIP'/>"/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </div>  <!-- end div account policy details -->

                        </div>
                    </td>
                </tr>
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

        </logic:present>
    </tbody>
    </table>

    <logic:present name="editable">
    <table align="center">
        <tr><td><img src="/images/spacer.gif" height="10"/></td></tr>
        <tr>
            <td>
                <html:submit tabindex="98" property="Create" styleId="Create">
                <bean:message key="drac.text.submit"/>
                </html:submit>
            </td>
            <td>
                <html:reset tabindex="99" property="Reset" >
                <bean:message key="drac.text.reset"/>
                </html:reset>
            </td>
        </tr>
    </table>
    </logic:present>
</html:form>

<script language="javascript">

function doSubmit() 
{
    setAllSelected(document.getElementById('userGroupsMembers'));
    setAllSelected(document.getElementById('lockedClientIPsList'));

    var createButton = document.getElementById("Create");
    if (createButton != null)
    {
        createButton.disabled = true;
    }
    

    return true;
}

function setAllSelected(elem) {
    if (elem)
    {
        for (var i=0; i < elem.options.length; i++)
            elem.options[i].selected = true;
    }
}

function handleChangeAccountState() {
    obj = document.getElementById("accountStateSelect");

    if (obj.value == "0") {
        obj = document.getElementById("disabledLayer");
        obj.style.display = "none";
    } else {
        obj = document.getElementById("disabledLayer");
        obj.style.display = "";
    }
}

function handleChangeAuthenticationType() {
    obj = document.getElementById("authenticationTypeSelect");

    if (obj.value == "Internal") {
        hideInternalAccountOptions(false);
        obj = document.getElementById("aSelectFillerRow");
        obj.style.display = "none";
    } else {
        hideInternalAccountOptions(true);
        obj = document.getElementById("aSelectFillerRow");
        obj.style.display = "";
    }
}

function hideInternalAccountOptions(hide) {
    var display = "";
    if (hide) display = "none";
    obj = document.getElementById("password1Col");
    obj.style.display = display;
    obj = document.getElementById("password2Col");
    obj.style.display = display;
    obj = document.getElementById("internalAccountRow1");
    obj.style.display = display;
    obj = document.getElementById("internalAccountRow2");
    obj.style.display = display;
    obj = document.getElementById("internalAccountRow3");
    obj.style.display = display;
    obj = document.getElementById("lockedIPsRow");
    obj.style.display = display;
}

function sortByValue(a, b) {
    var x = a.value.toLowerCase();
    var y = b.value.toLowerCase();
    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
}

function moveItems(fromId, toId) {
    var fromElem = document.getElementById(fromId);
    var toElem = document.getElementById(toId);

    // Working variables.
    var newDestList = new Array(toElem.options.length);
    var len = 0;

    if (fromElem && toElem) {
        // Populate the working list.
        for (len=0; len < toElem.options.length; len++) {
            if (toElem.options[len] != null) {
                newDestList[len] = new Option(toElem.options[len].text, toElem.options[len].value, toElem.options[len].defaultSelected, toElem.options[len].selected);
            }
        }

        // Decide what has been selected and incorporate into working list.
        for (var i=0; i < fromElem.options.length; i++) {
            if (fromElem.options[i].selected) {
                // Incorporate into working list.
                newDestList[len] = new Option(fromElem.options[i].text, fromElem.options[i].value, fromElem.options[i].defaultSelected, fromElem.options[i].selected);
                len++;
            }
        }

        // Sort the working list.
        newDestList.sort(sortByValue);

        // Populate the destination list with the items from the working list.
        for (var j=0; j < newDestList.length; j++) {
            if (newDestList[j] != null) {
                toElem.options[j] = newDestList[j];
            }
        }

        // Remove source list selected elements.
        for (var i=fromElem.options.length-1; i >= 0; i--) {
            if ( fromElem.options[i] != null && ( fromElem.options[i].selected == true ) ) {
//fromElem.options[i].selected = false;
                fromElem.options[i] = null;
            }
        }

        // maybe --> fromElem.options.selectedIndex = 0;

        // Make sure all options are selected, src and dest lists.
        // This is needed so that when swapping entries from one list to another and vice versa
        // the items need to be selected to get saved onthe server side.
        for (var i=fromElem.options.length-1; i >= 0; i--) {
            fromElem.options[i].selected = true;
        }
        for (var i=toElem.options.length-1; i >= 0; i--) {
            toElem.options[i].selected = true;
        }

    }
}

function removeIPItems(fromId) {
    var fromElem = document.getElementById(fromId);

    // Remove source list selected elements.
    for (var i=fromElem.options.length-1; i >= 0; i--) {
        if ( fromElem.options[i] != null && ( fromElem.options[i].selected == true ) ) {
            if (fromElem.options[i].selected) {
                fromElem.options[i] = null;
            }
        }
    }

//    fromElem.options.selectedIndex = 0;
}

function moveIPItems(fromId, toId) {    
    var regexIP = /^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$/;
    var fromElem = document.getElementById(fromId);
    var toElem = document.getElementById(toId);

    // Working variables.
    var destList = document.getElementById(toId);
    var newDestList = new Array(destList.options.length);
    var len = 0;

    if (fromElem && toElem) {
        // Populate the working list.
        for (len=0; len < destList.options.length; len++) {
            if (destList.options[len] != null) {
                newDestList[len] = new Option(destList.options[len].text, destList.options[len].value, destList.options[len].defaultSelected, destList.options[len].selected);
            }
        }

        // Decide what has been selected and incorporate into working list.
        if (fromElem.value.match(regexIP)) {
            newDestList[len] = new Option(fromElem.value, fromElem.value);

            // Sort the working list.
            newDestList.sort(sortByValue);

            // Populate the destination list with the items from the working list.
            for (var j=0; j < newDestList.length; j++) {
                if (newDestList[j] != null) {
                    toElem.options[j] = newDestList[j];
                }
            }
            fromElem.value = "";
        } else {
            alert("That is not a valid IP address.");
            fromElem.focus();
        }
    }
}

function viewUserGroup(elemId) {
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.location = "/management/userGroupManagement/queryUserGroup.do?ugName=" + escape(elem.options[elem.selectedIndex].value);
    }
    return false;
}

// onload
handleChangeAuthenticationType();
handleChangeAccountState();

</script>
<%@ include file="/common/footer.jsp" %>

