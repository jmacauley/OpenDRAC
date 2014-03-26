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
 * OpenDRAC WEB GUI version 1.0
 *
 * File: /management/globalPolicy.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page allows user to view and edit global policy
 *
 * TODO:
 *  Localize time strings, number validations,
 *  fix access control values
 *
 ****************************************************************************/


String pageRef = "drac.security.globalPolicy"; %>
<%@ include file="/common/header_struts.jsp" %>
<%@ include file="/common/calendar.jsp" %>

<script type="text/javascript" src="/scripts/tabpane.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<html:form method="POST" action="/management/editGlobalPolicy.do">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
    <table cellspacing="0" cellpadding="0" border="0" width="670" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5"/></td>
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
                <!-- error contents. -->
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

        <logic:present name="success" scope="request">
        <tr>
            <td>
                <table cellspacing="0" cellpadding="5" border="0" align="center">
                <tbody>
                    <tr>
                        <td align="center" class="tbForm1" nowrap>
                           <font color="green"><b><bean:message key="drac.security.globalPolicy.edit.success"/></b></font>
                        </td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        </logic:present>

        <tr>
            <td>
                <!-- Header. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.globalPolicy"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td class="tbForm1" nowrap align="center">
                            <!-- Start main pane -->
                            <div class="tab-pane" id="mainPane">
                                <script type="text/javascript">
                                    mp1 = new WebFXTabPane( document.getElementById( "mainPane" ) );
                                </script>

                                <div class="tab-page" id="authTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.globalPolicy.authentication"/></h2>
                                    <script type="text/javascript">mp1.addTabPage( document.getElementById( "authTab" ) );</script>
                                    &nbsp;<br>
                                    <table width="80%" border="0" cellpadding="2" cellspacing="2" >
                                        <tr>
                                            <td align="left" colspan="2">
                                                <b><bean:message key="drac.security.globalPolicy.authType"/></b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><img src="/images/spacer.gif" width="10"></td>
                                            <td align="left">
                                                <c:if test="${GlobalPolicyForm.supportAselect}">
                                                    <html:checkbox property="aselect" tabindex="2"/>&nbsp;<bean:message key="drac.security.globalPolicy.authType.aselect"/><br>
                                                </c:if>                                            
                                                <c:if test="${GlobalPolicyForm.supportInternal}">
                                                    <html:checkbox property="internal" tabindex="1"/>&nbsp;<bean:message key="drac.security.globalPolicy.authType.internal"/><br>
                                                </c:if>

                                                <c:if test="${GlobalPolicyForm.supportRadius}">
                                                    <html:checkbox property="radius" tabindex="3"/>&nbsp;<bean:message key="drac.security.globalPolicy.authType.radius"/>
                                                </c:if>
                                                <c:if test="${!GlobalPolicyForm.supportInternal}">
                                                    <html:hidden property="internal" value="false"/>
                                                </c:if>
                                                <c:if test="${!GlobalPolicyForm.supportAselect}">
                                                    <html:hidden property="aselect" value="false"/>
                                                </c:if>
                                                <c:if test="${!GlobalPolicyForm.supportRadius}">
                                                    <html:hidden property="radius" value="false"/>
                                                </c:if>
                                            </td>
                                        </tr>
                                        <tr><td>&nbsp;</td></tr>
                                        <tr>
                                            <td align="left" colspan="2">
                                                <b><bean:message key="drac.security.globalPolicy.dormancyPeriod"/></b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><img src="/images/spacer.gif" width="10"></td>
                                            <td align="left">
                                                <li><bean:message key="drac.security.globalPolicy.dormancy"/>&nbsp;
                                                <html:text style="width: 40px" size="1" property="dormancy" tabindex="7" errorStyleClass="invalid"/>
                                                &nbsp;<bean:message key="drac.security.globalPolicy.inactiveDays"/></li>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan="2">
                                                <img src="/images/spacer.gif" height="264">
                                            </td>
                                        </tr>
                                    </table>
                                </div> <!-- end auth tab -->

                                <!-- Internal Account Policy (security) tab -->
                                <div class="tab-page" id="securityTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.policy.sessionConfig"/></h2>
                                    <script type="text/javascript">mp1.addTabPage( document.getElementById( "securityTab" ) );</script>
            
                                    <!-- security pane -->
                                    <div class="tab-pane" id="securityPane">
                                        <script type="text/javascript">
                                        tp1 = new WebFXTabPane( document.getElementById( "securityPane" ) );
                                        </script>
            
                                        <!-- account policy sub tab -->
                                        <div class="tab-page" id="accountPolicyTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.policy.accountPolicy"/></h2>
                                            <script type="text/javascript">tp1.addTabPage( document.getElementById( "accountPolicyTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="accountPolicyTable">
                                                <tr>
                                                    <td class="tbForm1" align="left" colspan="2">
                                                        <b><bean:message key="drac.security.globalPolicy.inactivity"/></b>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><img src="/images/spacer.gif" width="10"></td>
                                                    <td class="tbForm1" align="left">
                                                        <li><bean:message key="drac.security.globalPolicy.inactivity.title"/>&nbsp;
                                                        <html:text tabindex="1" style = "width: 40px" property="inactivityPeriod" errorStyleClass="invalid"/>
                                                        <html:select tabindex="2" property="inactivityMetric" >
                                                            <html:option value="0"><bean:message key="drac.security.globalPolicy.metric.seconds"/></html:option>
                                                            <html:option value="1"><bean:message key="drac.security.globalPolicy.metric.minutes"/></html:option>
                                                            <html:option value="2"><bean:message key="drac.security.globalPolicy.metric.hours"/></html:option>
                                                            <html:option value="3"><bean:message key="drac.security.globalPolicy.metric.days"/></html:option>
                                                        </html:select>&nbsp;<bean:message key="drac.security.globalPolicy.inactivity.title2"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" align="left" colspan="2">
                                                        <b><bean:message key="drac.security.globalPolicy.invalidLogins"/></b>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td><img src="/images/spacer.gif" width="10"></td>
                                                    <td class="tbForm1" align="left">
                                                        <li><bean:message key="drac.security.globalPolicy.maxInvalidLogins"/>&nbsp;<html:text tabindex="3" style="width:40px" property="invalidLogins" errorStyleClass="invalid"/>
                                                        <li><bean:message key="drac.security.globalPolicy.lockoutPeriod"/>&nbsp;<html:text tabindex="4" style="width:40px" property="lockoutPeriod" errorStyleClass="invalid"/>
                                                        <html:select tabindex="5" property="lockoutMetric" >
                                                            <html:option value="0"><bean:message key="drac.security.globalPolicy.metric.seconds"/></html:option>
                                                            <html:option value="1"><bean:message key="drac.security.globalPolicy.metric.minutes"/></html:option>
                                                            <html:option value="2"><bean:message key="drac.security.globalPolicy.metric.hours"/></html:option>
                                                            <html:option value="3"><bean:message key="drac.security.globalPolicy.metric.days"/></html:option>
                                                        </html:select></li>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" align="left" colspan="2">
                                                        <b><bean:message key="drac.security.globalPolicy.ipAddressLocking"/></b>
                                                    </td>
                                                </tr>
                                                <tr id="internalAccountRow3">
                                                    <td class="tbForm1" nowrap colspan="2">
                                                        <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceGroupMemberTable">
                                                            <tr>
                                                                <td class="tbForm1" width="40%">
                                                                    <bean:message key="drac.security.userManagement.create.clientIPToLock"/><br>
                                                                    <html:text tabindex="6" style = "width: 155px" styleId="lockedIPsField" property="clientIPToLock" />
                                                                    &nbsp;<input type="button" id="addResGrpToMembership" onclick="moveIPItems('lockedIPsField','lockedClientIPsList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>" tabindex="7"/>
                                                                </td>
                                                                <td class="tbForm1" width="10%">
                                                                    <bean:message key="drac.security.userManagement.create.lockedClientIPs"/><br>
                                                                    <html:select property="lockedClientIPs" size="5" style="width:155px" styleId="lockedClientIPsList" multiple="true" tabindex="8">
                                                                        <html:options property="lockedClientIPs"/>
                                                                    </html:select>
                                                                </td>
                                                                <td class="tbForm1" align="left" valign="middle">
                                                                    &nbsp;<br><input type="button" id="removeResGrpFromMembership" onclick="removeItems('lockedClientIPsList');" value="<bean:message key='drac.security.policy.button.removeIP'/>" tabindex="9"/>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                <tr id="internalAccountRow4">
                                                    <td class="tbForm1" nowrap colspan="2">
                                                
                                                        <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceGroupMemberTablee">
                                                          
                                                            <tr>
                                                                <td class="tbForm1" width="40%">&#160;</td>
                                                                 <td class="tbForm1" width="10%">
                                                                    <bean:message key="drac.security.userManagement.temporary.lockedClientIPs"/><br>
                                                                    <html:select property="tempLockedClientIPs" size="5" style="width:155px" styleId="tempLockedClientIPsList" multiple="true" tabindex="8">
                                                                        <html:options property="tempLockedClientIPs"/>
                                                                    </html:select> 
                                                                </td>
                                                                <td class="tbForm1" align="left" valign="middle">
                                                                    &nbsp;<br><input type="button" id="removeTempLockedIP" onclick="removeItems('tempLockedClientIPsList');" value="<bean:message key='drac.security.policy.button.removeIP'/>" tabindex="9"/>
                                                                </td> 
                                                            </tr>
                                                             
                                                        </table>
                                                          
                                                    </td>
                                                </tr>   
                                                    <td colspan="2">
                                                        <img src="/images/spacer.gif" height="122">
                                                    </td>
                                                </tr>
                                                      -                                 
                                            </table>
                                        </div> <!-- end of account policy sub tab -->
            
                                        <!-- password policy sub tab -->
                                        <div class="tab-page" id="passwordPolicyTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.globalPolicy.passwordPolicy"/></h2>
                                            <script type="text/javascript">tp1.addTabPage( document.getElementById( "passwordPolicyTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="passwordPolicyTable">
                                                <tr>
                                                    <td class="tbForm1" width="50%">
                                                        <li><bean:message key="drac.security.globalPolicy.passwordAging"/>&nbsp;
                                                        <html:text style="width:40px" property="passwordAging" tabindex="1" errorStyleClass="invalid" />&nbsp;<bean:message key="drac.security.globalPolicy.metric.days"/></li>
                                                    </td>
                                                    <td class="tbForm1" colspan="2">
                                                        <li><bean:message key="drac.security.globalPolicy.passwordPolicy.passwordExpirationNotification"/>&nbsp;
                                                        <html:text style="width:40px" property="passwordExpirationNotification" tabindex="2" errorStyleClass="invalid" />&nbsp;<bean:message key="drac.security.globalPolicy.metric.days"/>&nbsp;<bean:message key="drac.security.globalPolicy.passwordPolicy.passwordExpirationNotification2"/></li>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" colspan="3">
                                                        <li><bean:message key="drac.security.globalPolicy.passwordPolicy.passwordHistory"/>&nbsp;
                                                        <html:text style="width:40px" property="passwordHistorySize" tabindex="3" errorStyleClass="invalid" />&nbsp;<bean:message key="drac.security.globalPolicy.passwordPolicy.passwordHistory2"/></li>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" colspan="3"><hr></td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" align="left" colspan="2">
                                                        <b><bean:message key="drac.security.globalPolicy.passwordRules"/></b>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.globalPolicy.passwordRules.minPasswordLength"/><br>
                                                        <html:text style="width:40px" property="minPasswordLength" styleId="minPasswordLength" tabindex="4"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.globalPolicy.passwordRules.minAlphaCharacters"/><br>
                                                        <html:text style="width:40px" property="minAlphaCharacters" styleId="minAlphaCharacters" tabindex="5"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.globalPolicy.passwordRules.minNumericCharacters"/><br>
                                                        <html:text style="width:40px" property="minNumericCharacters"  styleId="minNumericCharacters" tabindex="6"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.globalPolicy.passwordRules.minDifferentCharacters"/><br>
                                                        <html:text style="width:40px" property="minDifferentCharacters" styleId="minDifferentCharacters" tabindex="7"/>
                                                    </td>
                                                    <td class="tbForm1" colspan="2">
                                                        <bean:message key="drac.security.globalPolicy.passwordRules.minSpecialCharacters"/><br>
                                                        <html:text style="width:40px" property="minSpecialCharacters" styleId="minSpecialCharacters" tabindex="8"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.globalPolicy.passwordRules.mixedCaseCharacters"/><br>
                                                        <html:radio property="mixedCaseCharacters" value="yes" tabindex="9" styleId="mixedCaseCharactersYes"/><bean:message key="drac.text.yes"/>&nbsp;
                                                        <html:radio property="mixedCaseCharacters" value="no" tabindex="10" styleId="mixedCaseCharactersNo"/><bean:message key="drac.text.no"/>
                                                    </td>
                                                    <td class="tbForm1" colspan="2">
                                                        <bean:message key="drac.security.globalPolicy.passwordRules.permittedSpecialCharacters"/><br>
                                                        <html:text style="width:155px" property="specialCharacters" styleId="specialCharacters" tabindex="11"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" colspan="3"><hr></td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" align="left" colspan="2">
                                                        <b><bean:message key="drac.security.globalPolicy.invalidPasswords"/></b>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" width="40%">
                                                        <bean:message key="drac.security.globalPolicy.invalidateThisPassword"/><br>
                                                        <html:text style = "width: 155px" styleId="invalidPasswordField" property="invalidPassword" tabindex="12"/>
                                                        &nbsp;<input type="button" id="addResGrpToMembership" onclick="moveItems('invalidPasswordField','invalidPasswordsList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>" tabindex="13"/>
                                                    </td>
                                                    <td class="tbForm1" width="10%">
                                                        <bean:message key="drac.security.globalPolicy.invalidPasswords"/><br>
                                                        <html:select property="invalidPasswords" size="5" style="width:155px" styleId="invalidPasswordsList" multiple="true" tabindex="14">
                                                            <html:options property="invalidPasswords"/>
                                                        </html:select>
                                                    </td>
                                                    <td class="tbForm1" align="left" valign="middle">
                                                        <input type="button" id="removeResGrpFromMembership" onclick="removeItems('invalidPasswordsList');" value="<bean:message key='drac.security.policy.button.removeIP'/>" tabindex="15"/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div> <!-- end of password policy sub tab -->
                                    </div> <!-- end security pane -->
                                </div> <!-- end security tab -->

                                <!-- user group policy tab -->
                                <div class="tab-page" id="groupPolicyTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.groupPolicy"/></h2>
                                    <script type="text/javascript">mp1.addTabPage( document.getElementById( "groupPolicyTab" ) );</script>
            
                                    <!-- user group policy pane -->
                                    <div class="tab-pane" id="groupPane">
                                        <script type="text/javascript">
                                            tp1 = new WebFXTabPane( document.getElementById( "groupPane" ) );
                                        </script>
            
                                        <!-- Define the User Group Bandwidth Control Rules sub tab -->
                                        <div class="tab-page" id="groupBandwidthTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.policy.bandwidthControl"/></h2>
                                            <script type="text/javascript">tp1.addTabPage( document.getElementById( "groupBandwidthTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="groupBandwidthTable">
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                        <bean:message key="drac.security.policy.bw.maxservicesize"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="groupMaxServiceSize" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.groupMaxServiceSize}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.text.Mbps"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                      <bean:message key="drac.security.policy.bw.maxserviceduration"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="groupMaxServiceDuration" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.groupMaxServiceDuration}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.security.globalPolicy.seconds"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                      <bean:message key="drac.security.policy.bw.maxservicebandwidth"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="groupMaxServiceBandwidth" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.groupMaxServiceBandwidth}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.text.Mb"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                      <bean:message key="drac.security.policy.bw.maxaggregateservicesize"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="groupMaxAggregateServiceSize" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.groupMaxAggregateServiceSize}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.text.Mb"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2">
                                                        <img src="/images/spacer.gif" height="265">
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>

<!--
                                        <div class="tab-page" id="groupAccessControlTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.policy.accessControl"/></h2>
                                            <script type="text/javascript">tp1.addTabPage( document.getElementById( "groupAccessControlTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="groupAccessTable">
                                                <tr>
                                                    <td class="tbForm1" align="left" valign="top">
                                                        <b><bean:message key="drac.security.policy.access.application"/></b><br>
                                                        <select name="accessControlValue" id="accessControlValue" size="3" multiple>
                                                            <option value="schedules"><bean:message key="drac.security.schedules"/></option>
                                                            <option value="services"><bean:message key="drac.security.services"/></option>
                                                            <option value="network"><bean:message key="drac.security.network"/></option>
                                                            <option value="security"><bean:message key="drac.security.security"/></option>
                                                            <option value="administration"><bean:message key="drac.security.security.app"/></option>
                                                        </select>
                                                        <p>
                                                        <b><bean:message key="drac.security.policy.accessType"/></b><br>
                                                        <select name="accessControlKey" id="accessControlKey" size="1">
                                                            <option value="R"><bean:message key="drac.security.policy.access.read"/></option>
                                                            <option value="RW"><bean:message key="drac.security.policy.access.readwrite"/></option>
                                                            <option value="RWA"><bean:message key="drac.security.policy.access.admin"/></option>
                                                        </select>
                                                    </td>
                                                    <td class="tbForm1" align="center" valign="middle">
                                                        <input id="addAccessButton" type="button" value="<bean:message key='drac.security.policy.button.arrowRight'/>"
                                                            onclick="Javascript:addAccess('accessControlKey', 'accessControlValue', 'shownAccessRules', 'hiddenAccessRules');"/>
                                                        <p>
                                                        <input type="button" id="removeAccessButton" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"
                                                            onclick="Javascript:removeAccess('shownAccessRules', 'accessControlValue', 'hiddenAccessRules');"/>
                                                     </td>
                                                    <td valign="top" class="tbForm1" colspan="3">
                                                        <html:select size="7" styleId="hiddenAccessRules" property="groupAccessControlRules" style="display:none" errorStyleClass="invalid" multiple="true">
                                                            <html:options property="groupAccessControlRules" />
                                                        </html:select>
                                                        <br>
                                                        <select size="7" id="shownAccessRules" style="width: 300px">
                                                            <logic:iterate name="GlobalPolicyForm" property="accessControlRules" scope="request" id="rule">
                                                                <option>
                                                                <c:choose>
                                                                    <c:when test="${rule.key eq 'R'}">
                                                                        <bean:message key="drac.security.policy.access.read"/>=${rule.value}
                                                                    </c:when>
                                                                    <c:when test="${rule.key eq 'RW'}">
                                                                        <bean:message key="drac.security.policy.access.readwrite"/>=${rule.value}
                                                                    </c:when>
                                                                    <c:when test="${rule.key eq 'RWA'}">
                                                                        <bean:message key="drac.security.policy.access.admin"/>=${rule.value}
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        ${rule.key}=${rule.value}
                                                                    </c:otherwise>
                                                                </c:choose>
                                                                </option>
                                                            </logic:iterate>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" colspan="2">
                                                        <img src="/images/spacer.gif" height="161">
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
-->

                                        <!-- Define the User Group System Access Rules sub tab -->
                                        <div class="tab-page" id="groupSystemAccessTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.policy.systemAccess"/></h2>
                                            <script type="text/javascript">tp1.addTabPage( document.getElementById( "groupSystemAccessTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="groupSystemAccessTable">
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.permission"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <select tabindex="1" id="groupPermission" size="1">
                                                            <option value="grant"><bean:message key="drac.security.policy.grant"/></option>
                                                            <option value="deny"><bean:message key="drac.security.policy.deny"/></option>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.startTime"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <input tabindex="2" type="text" id="groupStartTime" size="11" styleClass="gen" onchange="JavaScript:gpTimeChangeListener(this);" /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:groupStartTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                                        <div id="groupStartTimeLayer"
                                                             style="position:absolute; z-index:10; overflow:auto; display:none;"
                                                             onmouseover="javascript:groupStartTimeMenu.overMenu(true);"
                                                             onmouseout="JavaScript:groupStartTimeMenu.overMenu(false);">
                                                            <select id="groupStartTimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:groupStartTimeMenu.textSet(this.value);" onkeypress="JavaScript:groupStartTimeMenu.comboKey();">
                                                                <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                                                                    <option value="<bean:write name="timeStrChoice" property="value"/>">
                                                                        <bean:write name="timeStrChoice" property="label"/>
                                                                    </option>
                                                                </logic:iterate>
                                                                <option value="23:59">23:59</option>
                                                            </select>
                                                        </div>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.endTime"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <input tabindex=3" type="text" id="groupEndTime" size="11" styleClass="gen" onchange="JavaScript:gpTimeChangeListener(this);" /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:groupEndTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                                        <div id="groupEndTimeLayer" style="position:absolute; z-index:10; overflow:auto; display:none;" onmouseover="javascript:groupEndTimeMenu.overMenu(true);" onmouseout="JavaScript:groupEndTimeMenu.overMenu(false);">
                                                            <select id="groupEndTimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:groupEndTimeMenu.textSet(this.value);" onkeypress="JavaScript:groupEndTimeMenu.comboKey();">
                                                                <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                                                                    <option value="<bean:write name="timeStrChoice" property="value"/>">
                                                                        <bean:write name="timeStrChoice" property="label"/>
                                                                    </option>
                                                                </logic:iterate>
                                                                <option value="23:59">23:59</option>
                                                            </select>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.specifyDays"/>
                                                    </td>
                                                    <td>
                                                        <input  type="radio" name="groupDaysOrDates" checked onclick="toggleDaysAndDates('userGrp', 'groupDaysOrDates');">
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                    </td>
                                                    <td class="tbForm1">
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="0" onclick="selectAllDaysOfWeek('groupWeekly')"><b>- <bean:message key="drac.security.policy.systemAccess.selectAll"/> -</b><br>
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="1" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.sun"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="2" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.mon"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="3" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.tue"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="4" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.wed"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="5" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.thu"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="6" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.fri"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="groupWeekly" value="7" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.sat"/></b>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.specifyDates"/>
                                                    </td>
                                                    <td>
                                                        <input  type="radio" name="groupDaysOrDates" onclick="toggleDaysAndDates('userGrp', 'groupDaysOrDates');">
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                    </td>
                                                    <!-- Display a disabled date picker by default -->
                                                    <td class="tbForm1" nowrap id="groupDatesInactive">
                                                        <table class="calendarInactive">
                                                            <tr class="cellsInactive">
                                                                <td align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.selectAll"/></td>
                                                            </tr>
                                                            <c:forEach var="row" begin="1" end="4">
                                                            <tr class="cellsInactive">
                                                                <c:forEach var="col" begin="1" end="7">
                                                                <td align="center">${col + (row-1)*7}</td>
                                                                </c:forEach>
                                                            </tr>
                                                            </c:forEach>
                                                            <tr class="cellsInactive">
                                                                <td class="cells" align="center">29</td>
                                                                <td class="cells" align="center">30</td>
                                                                <td class="cells" align="center">31</td>
                                                            </tr>
                                                            <tr class="cellsInactive">
                                                                <td align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.clearAll"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                    <!-- Hide an active date picker by default. -->
                                                    <td class="tbForm1" nowrap style="display:none" id="groupDates">
                                                        <table class="calendar">
                                                            <tr class="cells">
                                                                <td onclick="selectAllDates(true,'groupDate')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.selectAll"/></td>
                                                            </tr>
                                                            <c:forEach var="row" begin="1" end="4">
                                                            <tr class="cells">
                                                                <c:forEach var="col" begin="1" end="7">
                                                                <td name="groupDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" align="center">${col + (row-1)*7}</td>
                                                                </c:forEach>
                                                            </tr>
                                                            </c:forEach>
                                                            <tr class="cells">
                                                                <td name="groupDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">29</td>
                                                                <td name="groupDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">30</td>
                                                                <td name="groupDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">31</td>
                                                            </tr>
                                                            <tr class="cells">
                                                                <td onclick="selectAllDates(false,'groupDate')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.clearAll"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.inMonths"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <select tabindex="19" size="7" id="groupAccessMonth" style="width:120px" multiple >
                                                            <option value="1"> <bean:message key="drac.schedule.jan"/></option>
                                                            <option value="2"> <bean:message key="drac.schedule.feb"/></option>
                                                            <option value="3"> <bean:message key="drac.schedule.mar"/></option>
                                                            <option value="4"> <bean:message key="drac.schedule.apr"/></option>
                                                            <option value="5"> <bean:message key="drac.schedule.may"/></option>
                                                            <option value="6"> <bean:message key="drac.schedule.jun"/></option>
                                                            <option value="7"> <bean:message key="drac.schedule.jul"/></option>
                                                            <option value="8"> <bean:message key="drac.schedule.aug"/></option>
                                                            <option value="9"> <bean:message key="drac.schedule.sep"/></option>
                                                            <option value="10"> <bean:message key="drac.schedule.oct"/></option>
                                                            <option value="11"> <bean:message key="drac.schedule.nov"/></option>
                                                            <option value="12"> <bean:message key="drac.schedule.dec"/></option>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" align="left">
                                                        <input tabindex="20" type="button" value="<bean:message key='drac.security.policy.button.add'/>"
                                                               onclick="addRule('groupSystemAccessRules','groupPermission','groupAccessMonth','groupDate','groupWeekly','groupStartTime','groupEndTime','groupAccessRulesTable');"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" colspan="4">
                                                        <!-- Keep this select hidden always - it holds our new rules -->
                                                        <html:select size="5" property="groupSystemAccessRules" style="display:none" styleId="groupSystemAccessRules" errorStyleClass="invalid" multiple="true">
                                                            <html:options property="groupSystemAccessRules" />
                                                        </html:select>
                                                        <!-- Display a table of the current and new rules -->
                                                        <table width="100%" id="groupAccessRulesTable" border="1" class="forumline">
                                                            <tr>
                                                                <th><bean:message key="drac.security.policy.systemAccess.delete"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.permission"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.startTime"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.endTime"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.onDays"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.onDates"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.inMonths"/></th>
                                                            </tr>
                                                            <logic:iterate id="rule" name="GlobalPolicyForm" scope="request" property="groupRules" indexId="cnt">
                                                            <tr>
                                                                <td class="row1" align="center"
                                                                onclick="Javascript:removeRule('groupAccessRulesTable',this.parentNode.rowIndex,'groupSystemAccessRules');">
                                                                    <img src="/images/delete.gif">
                                                                </td>
                                                                <td class="row1" align="center">
                                                                    <c:if test="${rule.permission eq 'grant'}">
                                                                        <bean:message key="drac.security.policy.grant"/>
                                                                    </c:if>
                                                                    <c:if test="${rule.permission eq 'deny'}">
                                                                        <bean:message key="drac.security.policy.deny"/>
                                                                    </c:if>
                                                                </td>
                                                                <td class="row1" align="center" nowrap>${rule.startTime}</td>
                                                                <td class="row1" align="center" nowrap>${rule.endTime}</td>
                                                                <td class="row1" align="center">
                                                                    <c:choose>
                                                                        <c:when test="${fn:length(rule.dayOfWeek) == 0 || fn:length(rule.dayOfWeek) == 7}">
                                                                            -
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <logic:iterate id="weekday" property="dayOfWeek" name="rule">
                                                                                <c:choose>
                                                                                    <c:when test="${weekday eq '1'}">
                                                                                        <bean:message key="drac.calendar.short.sun"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '2'}">
                                                                                        <bean:message key="drac.calendar.short.mon"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '3'}">
                                                                                        <bean:message key="drac.calendar.short.tue"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '4'}">
                                                                                        <bean:message key="drac.calendar.short.wed"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '5'}">
                                                                                        <bean:message key="drac.calendar.short.thu"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '6'}">
                                                                                        <bean:message key="drac.calendar.short.fri"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '7'}">
                                                                                        <bean:message key="drac.calendar.short.sat"/>
                                                                                    </c:when>
                                                                                </c:choose>
                                                                            </logic:iterate>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                                <td class="row1" align="center">
                                                                    <c:choose>
                                                                        <c:when test="${fn:length(rule.dayOfWeek) > 0}">
                                                                            -
                                                                        </c:when>
                                                                        <c:when test="${fn:length(rule.days) == 0 || fn:length(rule.days) == 31}">
                                                                            <bean:message key="drac.security.policy.systemAccess.allDates"/>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <logic:iterate id="day" property="days" name="rule">
                                                                                ${day}&nbsp;
                                                                            </logic:iterate>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                                <td class="row1" align="center">
                                                                    <c:choose>
                                                                        <c:when test="${fn:length(rule.months) == 0 || fn:length(rule.months) == 12}">
                                                                            <bean:message key="drac.security.policy.systemAccess.everyMonth"/>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <logic:iterate id="month" property="months" name="rule">
                                                                                <c:choose>
                                                                                    <c:when test="${month eq '0'}">
                                                                                        <bean:message key="drac.calendar.short.jan"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '1'}">
                                                                                        <bean:message key="drac.calendar.short.feb"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '2'}">
                                                                                        <bean:message key="drac.calendar.short.mar"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '3'}">
                                                                                        <bean:message key="drac.calendar.short.apr"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '4'}">
                                                                                        <bean:message key="drac.calendar.short.may"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '5'}">
                                                                                        <bean:message key="drac.calendar.short.jun"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '6'}">
                                                                                        <bean:message key="drac.calendar.short.jul"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '7'}">
                                                                                        <bean:message key="drac.calendar.short.aug"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '8'}">
                                                                                        <bean:message key="drac.calendar.short.sep"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '9'}">
                                                                                        <bean:message key="drac.calendar.short.oct"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '10'}">
                                                                                        <bean:message key="drac.calendar.short.nov"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '11'}">
                                                                                        <bean:message key="drac.calendar.short.dec"/>
                                                                                    </c:when>
                                                                                </c:choose>
                                                                            </logic:iterate>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                            </tr>
                                                            </logic:iterate>
                                                        </table>
                                                     </td>
                                                </tr>
                                            </table>
                                        </div>
                                    </div> <!-- end user group policy pane -->
                                </div> <!-- end user group policy tab -->

                                <!-- Resource Group policy tab -->
                                <div class="tab-page" id="resourcePolicyTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.resourcePolicy"/></h2>
                                    <script type="text/javascript">mp1.addTabPage( document.getElementById( "resourcePolicyTab" ) );</script>
            
                                    <!-- Resource Group policy pane -->
                                    <div class="tab-pane" id="resourcePane">
                                        <script type="text/javascript">
                                            tp2 = new WebFXTabPane( document.getElementById( "resourcePane" ) );
                                        </script>
            
                                        <!-- Define the Resource Group Bandwidth Control Rules sub tab -->
                                        <div class="tab-page" id="resourceBandwidthTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.policy.bandwidthControl"/></h2>
                                            <script type="text/javascript">tp2.addTabPage( document.getElementById( "resourceBandwidthTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceBandwidthTable">
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                        <bean:message key="drac.security.policy.bw.maxservicesize"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="resourceMaxServiceSize" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.resourceMaxServiceSize}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.text.Mbps"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                        <bean:message key="drac.security.policy.bw.maxserviceduration"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="resourceMaxServiceDuration" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.resourceMaxServiceDuration}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.security.globalPolicy.seconds"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                        <bean:message key="drac.security.policy.bw.maxservicebandwidth"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="resourceMaxServiceBandwidth" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.resourceMaxServiceBandwidth}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.text.Mb"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" nowrap>
                                                        <bean:message key="drac.security.policy.bw.maxaggregateservicesize"/>
                                                    </td>
                                                    <td class="tbForm1" nowrap>
                                                        <logic:present name="editable">
                                                            <html:text style="width: 110px" property="resourceMaxAggregateServiceSize" tabindex="1" errorStyleClass="invalid"/>
                                                        </logic:present>
                                                        <logic:notPresent name="editable">
                                                            <b>${GlobalPolicyForm.resourceMaxAggregateServiceSize}</b>
                                                        </logic:notPresent>
                                                        <bean:message key="drac.text.Mb"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td colspan="2">
                                                        <img src="/images/spacer.gif" height="265">
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
            
<!--
                                        <div class="tab-page" id="resourceStateControlTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.policy.accessState"/></h2>
                                            <script type="text/javascript">tp2.addTabPage( document.getElementById( "resourceStateControlTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceStateTable">
                                                <tr>
                                                    <td class="tbForm1" align="right">
                                                        <b><bean:message key="drac.security.policy.accessState.resource.title"/></b>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <html:select size="1" property="resourceAccessState" style="width: 110px" errorStyleClass="invalid">
                                                            <html:option value="0"><bean:message key="drac.security.policy.access.state.open"/></html:option>
                                                            <html:option value="1"><bean:message key="drac.security.policy.access.state.closed"/></html:option>
                                                            <html:option value="2"><bean:message key="drac.security.policy.access.state.disabled"/></html:option>
                                                        </html:select>
                                                     </td>
                                                 </tr>
                                                <tr>
                                                    <td class="tbForm1" colspan="2">
                                                        <img src="/images/spacer.gif" height="269">
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
-->

                                        <!-- Define the Resource Group Resource Access Rules sub tab -->
                                        <div class="tab-page" id="resourceSystemAccessTab" align="center">
                                            <h2 class="tab"><bean:message key="drac.security.policy.systemAccess.resource"/></h2>
                                            <script type="text/javascript">tp2.addTabPage( document.getElementById( "resourceSystemAccessTab" ) );</script>
                                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceAccessTable">
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.permission"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <select tabindex="1" id="resPermission" size="1">
                                                            <option value="grant"><bean:message key="drac.security.policy.grant"/></option>
                                                            <option value="deny"><bean:message key="drac.security.policy.deny"/></option>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.startTime"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <input tabindex="2" type="text" id="resStartTime" size="11" styleClass="gen" onchange="JavaScript:gpTimeChangeListener(this);"
                                                               /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:resStartTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                                        <div id="resStartTimeLayer" style="position:absolute; z-index:100; overflow:auto; display:none;" onmouseover="javascript:resStartTimeMenu.overMenu(true);" onmouseout="JavaScript:resStartTimeMenu.overMenu(false);">
                                                            <select id="resStartTimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:resStartTimeMenu.textSet(this.value);" onkeypress="JavaScript:resStartTimeMenu.comboKey();">
                                                                <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                                                                    <option value="<bean:write name="timeStrChoice" property="value" />">
                                                                        <bean:write name="timeStrChoice" property="label" />
                                                                    </option>
                                                                </logic:iterate>
                                                                <option value="23:59">23:59</option>
                                                            </select>
                                                        </div>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.endTime"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <input tabindex="3" type="text" id="resEndTime" size="11" styleClass="gen" onchange="JavaScript:gpTimeChangeListener(this);"
                                                               /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:resEndTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                                        <div id="resEndTimeLayer" style="position:absolute; z-index:100; overflow:auto; display:none;" onmouseover="javascript:resEndTimeMenu.overMenu(true);" onmouseout="JavaScript:resEndTimeMenu.overMenu(false);">
                                                            <select id="resEndTimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:resEndTimeMenu.textSet(this.value);" onkeypress="JavaScript:resEndTimeMenu.comboKey();">
                                                                <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                                                                    <option value="<bean:write name="timeStrChoice" property="value"/>">
                                                                        <bean:write name="timeStrChoice" property="label"/>
                                                                    </option>
                                                                </logic:iterate>
                                                                <option value="23:59">23:59</option>
                                                            </select>
                                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.specifyDays"/>
                                                    </td>
                                                    <td>
                                                        <input  type="radio" name="resDaysOrDates" checked onclick="toggleDaysAndDates('resGrp', 'resDaysOrDates');">
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                    </td>
                                                    <td class="tbForm1">
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="0" onclick="selectAllDaysOfWeek('resWeekly')"><b>- <bean:message key="drac.security.policy.systemAccess.selectAll"/> -</b><br>
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="1" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.sun"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="2" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.mon"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="3" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.tue"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="4" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.wed"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="5" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.thu"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="6" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.fri"/></b><br>
                                                        <INPUT  TYPE="checkbox" name="resWeekly" value="7" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.sat"/></b>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.specifyDates"/>
                                                    </td>
                                                    <td>
                                                        <input  type="radio" name="resDaysOrDates" onclick="toggleDaysAndDates('resGrp', 'resDaysOrDates');">
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                    </td>
                                                    <!-- Display a disabled date picker by default -->
                                                    <td class="tbForm1" nowrap id="resDatesInactive">
                                                        <table class="calendarInactive">
                                                            <tr class="cellsInactive">
                                                                <td align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.selectAll"/></td>
                                                            </tr>
                                                            <c:forEach var="row" begin="1" end="4">
                                                            <tr class="cellsInactive">
                                                                <c:forEach var="col" begin="1" end="7">
                                                                <td align="center">${col + (row-1)*7}</td>
                                                                </c:forEach>
                                                            </tr>
                                                            </c:forEach>
                                                            <tr class="cellsInactive">
                                                                <td class="cells" align="center">29</td>
                                                                <td class="cells" align="center">30</td>
                                                                <td class="cells" align="center">31</td>
                                                            </tr>
                                                            <tr class="cellsInactive">
                                                                <td align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.clearAll"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                    <!-- Hide an active date picker by default. -->
                                                    <td class="tbForm1" nowrap style="display:none" id="resDates">
                                                        <table class="calendar">
                                                            <tr class="cells">
                                                                <td onclick="selectAllDates(true,'resDate')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.selectAll"/></td>
                                                            </tr>
                                                            <c:forEach var="row" begin="1" end="4">
                                                            <tr class="cells">
                                                                <c:forEach var="col" begin="1" end="7">
                                                                <td name="resDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" align="center">${col + (row-1)*7}</td>
                                                                </c:forEach>
                                                            </tr>
                                                            </c:forEach>
                                                            <tr class="cells">
                                                                <td name="resDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">29</td>
                                                                <td name="resDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">30</td>
                                                                <td name="resDate" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">31</td>
                                                            </tr>
                                                            <tr class="cells">
                                                                <td onclick="selectAllDates(false,'resDate')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.clearAll"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1">
                                                        <bean:message key="drac.security.policy.systemAccess.inMonths"/>
                                                    </td>
                                                    <td class="tbForm1">
                                                        <select tabindex="19" size="7" id="resAccessMonth" style="width:120px" multiple >
                                                            <option value="1"> <bean:message key="drac.schedule.jan"/></option>
                                                            <option value="2"> <bean:message key="drac.schedule.feb"/></option>
                                                            <option value="3"> <bean:message key="drac.schedule.mar"/></option>
                                                            <option value="4"> <bean:message key="drac.schedule.apr"/></option>
                                                            <option value="5"> <bean:message key="drac.schedule.may"/></option>
                                                            <option value="6"> <bean:message key="drac.schedule.jun"/></option>
                                                            <option value="7"> <bean:message key="drac.schedule.jul"/></option>
                                                            <option value="8"> <bean:message key="drac.schedule.aug"/></option>
                                                            <option value="9"> <bean:message key="drac.schedule.sep"/></option>
                                                            <option value="10"> <bean:message key="drac.schedule.oct"/></option>
                                                            <option value="11"> <bean:message key="drac.schedule.nov"/></option>
                                                            <option value="12"> <bean:message key="drac.schedule.dec"/></option>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" align="left">
                                                        <input tabindex="20" type="button" value="<bean:message key='drac.security.policy.button.add'/>"
                                                               onclick="addRule('resSystemAccessRules','resPermission','resAccessMonth','resDate','resWeekly','resStartTime','resEndTime','resAccessRulesTable');"/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="tbForm1" colspan="4">
                                                        <!-- Keep this select hidden always - it holds our new rules -->
                                                        <html:select size="5" property="resourceSystemAccessRules" style="display:none" styleId="resSystemAccessRules" errorStyleClass="invalid" multiple="true">
                                                            <html:options property="resourceSystemAccessRules"/>
                                                        </html:select>
                                                        <!-- Display a table of the current and new rules -->
                                                        <table width="100%" id="resAccessRulesTable" border="1" class="forumline">
                                                            <tr>
                                                                <th><bean:message key="drac.security.policy.systemAccess.delete"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.permission"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.startTime"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.endTime"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.onDays"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.onDates"/></th>
                                                                <th><bean:message key="drac.security.policy.systemAccess.inMonths"/></th>
                                                            </tr>
                                                            <logic:iterate id="rule" name="GlobalPolicyForm" scope="request" property="resRules" indexId="cnt">
                                                            <tr>
                                                                <td class="row1" align="center"
                                                                    onclick="Javascript:removeRule('resAccessRulesTable',this.parentNode.rowIndex,'resSystemAccessRules');">
                                                                    <img src="/images/delete.gif">
                                                                </td>
                                                                <td class="row1" align="center">
                                                                    <c:if test="${rule.permission eq 'grant'}">
                                                                        <bean:message key="drac.security.policy.grant"/>
                                                                    </c:if>
                                                                    <c:if test="${rule.permission eq 'deny'}">
                                                                        <bean:message key="drac.security.policy.deny"/>
                                                                    </c:if>
                                                                </td>
                                                                <td class="row1" align="center" nowrap>${rule.startTime}</td>
                                                                <td class="row1" align="center" nowrap>${rule.endTime}</td>
                                                                <td class="row1" align="center">
                                                                    <c:choose>
                                                                        <c:when test="${fn:length(rule.dayOfWeek) == 0 || fn:length(rule.dayOfWeek) == 7}">
                                                                            -
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <logic:iterate id="weekday" property="dayOfWeek" name="rule">
                                                                                <c:choose>
                                                                                    <c:when test="${weekday eq '1'}">
                                                                                        <bean:message key="drac.calendar.short.sun"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '2'}">
                                                                                        <bean:message key="drac.calendar.short.mon"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '3'}">
                                                                                        <bean:message key="drac.calendar.short.tue"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '4'}">
                                                                                        <bean:message key="drac.calendar.short.wed"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '5'}">
                                                                                        <bean:message key="drac.calendar.short.thu"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '6'}">
                                                                                        <bean:message key="drac.calendar.short.fri"/>
                                                                                    </c:when>
                                                                                    <c:when test="${weekday eq '7'}">
                                                                                        <bean:message key="drac.calendar.short.sat"/>
                                                                                    </c:when>
                                                                                </c:choose>
                                                                            </logic:iterate>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                                <td class="row1" align="center">
                                                                    <c:choose>
                                                                        <c:when test="${fn:length(rule.dayOfWeek) > 0}">
                                                                            -
                                                                        </c:when>
                                                                        <c:when test="${fn:length(rule.days) == 0 || fn:length(rule.days) == 31}">
                                                                            <bean:message key="drac.security.policy.systemAccess.allDates"/>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <logic:iterate id="day" property="days" name="rule">
                                                                                ${day}&nbsp;
                                                                            </logic:iterate>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                                <td class="row1" align="center">
                                                                    <c:choose>
                                                                        <c:when test="${fn:length(rule.months) == 0 || fn:length(rule.months) == 12}">
                                                                            <bean:message key="drac.security.policy.systemAccess.everyMonth"/>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <logic:iterate id="month" property="months" name="rule">
                                                                                <c:choose>
                                                                                    <c:when test="${month eq '0'}">
                                                                                        <bean:message key="drac.calendar.short.jan"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '1'}">
                                                                                        <bean:message key="drac.calendar.short.feb"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '2'}">
                                                                                        <bean:message key="drac.calendar.short.mar"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '3'}">
                                                                                        <bean:message key="drac.calendar.short.apr"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '4'}">
                                                                                        <bean:message key="drac.calendar.short.may"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '5'}">
                                                                                        <bean:message key="drac.calendar.short.jun"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '6'}">
                                                                                        <bean:message key="drac.calendar.short.jul"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '7'}">
                                                                                        <bean:message key="drac.calendar.short.aug"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '8'}">
                                                                                        <bean:message key="drac.calendar.short.sep"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '9'}">
                                                                                        <bean:message key="drac.calendar.short.oct"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '10'}">
                                                                                        <bean:message key="drac.calendar.short.nov"/>
                                                                                    </c:when>
                                                                                    <c:when test="${month eq '11'}">
                                                                                        <bean:message key="drac.calendar.short.dec"/>
                                                                                    </c:when>
                                                                                </c:choose>
                                                                            </logic:iterate>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                            </tr>
                                                            </logic:iterate>
                                                        </table>
                                                     </td>
                                                </tr>
                                            </table>
                                        </div>  <!-- end Resource Group System Access Rules sub tab -->
            
                                    </div>  <!-- end resource group policy pane -->
                                </div>  <!-- end resource group policy tab -->
                            </div>  <!-- end main pane -->
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

        <logic:present name="editable">
        <table align="center">
            <tr><td><img src="/images/spacer.gif" height="10"/></td></tr>
            <tr>
                <td>
                    <html:submit tabindex="98" property="Create" onclick="doSubmit()">
                        <bean:message key="drac.text.submit"/>
                    </html:submit>
                </td>
                <td>
                    <html:button tabindex="99" onclick="doReset()" property="">
                        <bean:message key="drac.text.reset"/>
                    </html:button>
                </td>
            </tr>
        </table>
        </logic:present>

        <html:hidden property="redirected" value="false"/>
    </tbody>
    </table>
</html:form>

<script language="javascript">
var previousClass = "";

function checkPasswordRules() {
    var obj, obj2;

    obj = document.getElementById('minPasswordLength');
    if (isNaN(obj.value) || obj.value == "") {
        obj.value = "0";
    }
    obj = document.getElementById('minAlphaCharacters');
    if (isNaN(obj.value) || obj.value == "") {
        obj.value = "0";
    }

    obj = document.getElementById('minNumericCharacters');
    if (isNaN(obj.value) || obj.value == "") {
        obj.value = "0";
    }

    obj = document.getElementById('minDifferentCharacters');
    if (isNaN(obj.value) || obj.value == "") {
        obj.value = "0";
    }

    obj = document.getElementById('minSpecialCharacters');
    if (document.getElementById('specialCharacters').value == "") {
        obj.value = "0";
    } else {
        if (isNaN(obj.value) || obj.value == "") {
            obj.value = "0";
        }
    }

    obj = document.getElementById('mixedCaseCharactersYes');
    obj2 = document.getElementById('mixedCaseCharactersNo');
    if (!obj.checked && !obj2.checked) {
        // Neither radio is selected. Force to Yes.
        obj.checked = true;
    }
}

function checkForValidValues() {
    checkPasswordRules();
}

function doSubmit() {
    setAllSelected(document.getElementById('hiddenAccessRules'));
    setAllSelected(document.getElementById('groupSystemAccessRules'));
    setAllSelected(document.getElementById('resSystemAccessRules'));
    setAllSelected(document.getElementById('lockedClientIPsList'));
    setAllSelected(document.getElementById('invalidPasswordsList'));
    checkForValidValues();
}

function doReset() {
    location.href="/management/globalPolicy.do";
}

function setAllSelected(elem) {
    if (elem) {
        for (var i=0; i < elem.options.length; i++) {
            elem.options[i].selected = true;
        }
    }
}

//
// remove an access control rule
//
function removeAccess(id, appId, hiddenId) {
    var appList = document.getElementById(appId);
    var accessList = document.getElementById(id);
    var hiddenElem = document.getElementById(hiddenId);
    if (!accessList || !appList || !hiddenElem) return;
    for (var i=accessList.options.length-1; i>=0; i--)
    {
        if (accessList.options[i].selected)
        {
            var textArray = accessList.options[i].text.split("=")[1].split(",");
            var valueArray = hiddenElem.options[i].value.split("=")[1].split(",");
            for (var j=0; j < textArray.length; j++)
            {
                // add items back to application list
                appList[appList.length] = new Option(textArray[j], valueArray[j]);
            }
            accessList.options[i] = null;
            hiddenElem.options[i] = null;
        }
    }
    document.getElementById("addAccessButton").disabled = false;

    accessList.selectedIndex = 0;
    if (accessList.options.length == 0)
        document.getElementById("removeAccessButton").disabled = true;
}

//
// add an access control rule
//
function addAccess(keyId, valueId, accessId, hiddenId) {
    var keyElem = document.getElementById(keyId);
    var valueList = document.getElementById(valueId);
    var accessElem = document.getElementById(accessId);
    var hiddenElem = document.getElementById(hiddenId);
    if (!keyElem || !valueList || !accessElem || !hiddenElem)
    {
        return;
    }
    if (valueList.selectedIndex < 0) return;
    var ruleValue = keyElem.options[keyElem.selectedIndex].value + "=";
    var ruleText = keyElem.options[keyElem.selectedIndex].text + "=";
    for (var i=0; i < valueList.options.length; i++)
    {
        if (valueList.options[i].selected)
        {
            ruleValue = ruleValue + valueList.options[i].value + ",";
            ruleText = ruleText + valueList.options[i].text + ",";
        }
    }
    ruleValue = ruleValue.substr(0, ruleValue.length-1);
    ruleText = ruleText.substr(0, ruleText.length-1);
    accessElem[accessElem.length] = new Option(ruleText, ruleValue);
    hiddenElem[hiddenElem.length] = new Option(ruleValue, ruleValue);
    document.getElementById("removeAccessButton").disabled = false;

    // remove selected apps
    for (var i=valueList.options.length-1; i>=0; i--)
    {
        if (valueList.options[i].selected)
        {
            valueList.options[i] = null;
        }
    }
    valueList.selectedIndex = 0;
    if (valueList.options.length == 0)
        document.getElementById("addAccessButton").disabled = true;

}

//
// Add a time based (system access) rule.
//
function addRule(idList, idPer, idMth, idD, idWeek, idFrT, idToT, idTable) {
    var selectElem = document.getElementById(idList);
    var permElem = document.getElementById(idPer);

    var monthElem = document.getElementById(idMth);
    var dateElems = document.getElementsByName(idD);
    var weeklyElems = document.getElementsByName(idWeek);

    var fromTimeElem = document.getElementById(idFrT);
    var toTimeElem = document.getElementById(idToT);

    var permText; // display me on the GUI (in the rules summary table).
    if (permElem.options[permElem.selectedIndex].value == "grant") {
        permText = "<c:out value='${sessionScope["drac.security.policy.grant"]}'/>";
    } else {
        permText = "<c:out value='${sessionScope["drac.security.policy.deny"]}'/>";
    }

    // Were any months selected?
    var months = "";
    var monthText = "";
    var numMonthsSelected = 0;

    // Loop over all 12 months.
    for (var i=0; i < monthElem.options.length; i++) {
        if (monthElem.options[i].selected) {
            numMonthsSelected++;
            months += monthElem.options[i].value-1 + ":";
//orig            months += monthElem.options[i].value + ":";
            if (monthElem.options[i].value == 1) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.jan"]}'/> ";
            } else if (monthElem.options[i].value == 2) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.feb"]}'/> ";
            } else if (monthElem.options[i].value == 3) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.mar"]}'/> ";
            } else if (monthElem.options[i].value == 4) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.apr"]}'/> ";
            } else if (monthElem.options[i].value == 5) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.may"]}'/> ";
            } else if (monthElem.options[i].value == 6) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.jun"]}'/> ";
            } else if (monthElem.options[i].value == 7) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.jul"]}'/> ";
            } else if (monthElem.options[i].value == 8) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.aug"]}'/> ";
            } else if (monthElem.options[i].value == 9) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.sep"]}'/> ";
            } else if (monthElem.options[i].value == 10) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.oct"]}'/> ";
            } else if (monthElem.options[i].value == 11) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.nov"]}'/> ";
            } else if (monthElem.options[i].value == 12) {
                monthText += "<c:out value='${sessionScope["drac.calendar.short.dec"]}'/> ";
            }
        }
    }
    if (months != "") {
        months = months.substring(0,months.lastIndexOf(":"));
    }
    
    if (numMonthsSelected == 0 || numMonthsSelected == 12) {
        monthText = "<c:out value='${sessionScope["drac.security.policy.systemAccess.everyMonth"]}'/>";
    }

    var numChecked = 0;

    // Were any days (of the week) selected?
    var daysText = ""; // Display to user
    var daysOfWeek = ""; // Send to server.

    // Loop over all 7 days (of the week).
    for (var i=0; i < weeklyElems.length; i++) {
        // Ignore position 0.
        if (i != 0) {
            if (weeklyElems[i].checked) {
                numChecked++;
                daysOfWeek += weeklyElems[i].value + ":";
    
                if (weeklyElems[i].value == 1) {
                    daysText += "<c:out value='${sessionScope["drac.calendar.short.sun"]}'/> ";
                } else if (weeklyElems[i].value == 2) {
                    daysText += "<c:out value='${sessionScope["drac.calendar.short.mon"]}'/> ";
                } else if (weeklyElems[i].value == 3) {
                    daysText += "<c:out value='${sessionScope["drac.calendar.short.tue"]}'/> ";
                } else if (weeklyElems[i].value == 4) {
                    daysText += "<c:out value='${sessionScope["drac.calendar.short.wed"]}'/> ";
                } else if (weeklyElems[i].value == 5) {
                    daysText += "<c:out value='${sessionScope["drac.calendar.short.thu"]}'/> ";
                } else if (weeklyElems[i].value == 6) {
                    daysText += "<c:out value='${sessionScope["drac.calendar.short.fri"]}'/> ";
                } else if (weeklyElems[i].value == 7) {
                    daysText += "<c:out value='${sessionScope["drac.calendar.short.sat"]}'/> ";
                }
            }
        }
    }

    if (daysOfWeek != "") {
        if (numChecked == 7) {
            // Do not send individual days if all were chosen.
            daysOfWeek = "";
        } else {
            daysOfWeek = daysOfWeek.substring(0, daysOfWeek.lastIndexOf(":"));
        }
    }
    
    if ((numChecked == 0) || (numChecked == 7)) {
        daysText = "-";
    }

    // Were any dates selected?
    var dates = "";
    var dateText = "";

    if ((numChecked != 0) && (numChecked != 7)) {
        // Looks like days of the week was used to select.
        dateText = "-";
    } else {
        numChecked = 0;
    
        // Loop over all selected dates.
        for (var i=0; i < dateElems.length; i++) {
            if (dateElems[i].className == "selectedM") {
                numChecked++;
    
                dates += parseInt(dateElems[i].innerHTML) + ":";
                dateText += parseInt(dateElems[i].innerHTML) + " ";
            }
        }
    
        if (dates != "") {
            dates = dates.substring(0, dates.lastIndexOf(":"));
        }
        if (numChecked == 0 || numChecked == 31) {
            dateText = "<c:out value='${sessionScope["drac.security.policy.systemAccess.allDates"]}'/>";
            // Do not send individual dates if all were chosen.
            dates = "";
        }
    }

    // Construct a new rule.
    var rule = permElem.options[permElem.selectedIndex].value + "={";
    rule += "[" + months + "],";
    rule += "[" + dates + "],";
    rule += "[" + daysOfWeek + "],";
    rule += fromTimeElem.value + "," + toTimeElem.value + "}";

    selectElem.options[selectElem.options.length] = new Option(rule);

    // Update table.
    var table = document.getElementById(idTable);
    if (table) {
        var index = table.rows.length;
        var row = table.insertRow(index);
        var cell0 = document.createElement('td');
        cell0.className = "row1";
        cell0.align = "center";
        //cell0.appendChild(document.createTextNode("cell0"));
        //cell0.addEventListener('click',removeRule(idTable,index,idList),false);
        cell0.innerHTML = "<img onclick=\"removeRule('" + idTable + "',this.parentNode.parentNode.rowIndex,'" + idList + "');\" src=\"/images/delete.gif\">";
        row.appendChild(cell0);

        var cell1 = document.createElement('td');
        cell1.className = "row1";
        cell1.align = "center";
        cell1.appendChild(document.createTextNode(permText));
        row.appendChild(cell1);

        var cell2 = document.createElement('td');
        cell2.className = "row1";
        cell2.align = "center";
        cell2.noWrap = true;
        cell2.appendChild(document.createTextNode(fromTimeElem.value));
        row.appendChild(cell2);

        var cell3 = document.createElement('td');
        cell3.className = "row1";
        cell3.align = "center";
        cell3.noWrap = true;
        cell3.appendChild(document.createTextNode(toTimeElem.value));
        row.appendChild(cell3);

        var cell4 = document.createElement('td');
        cell4.className = "row1";
        cell4.align = "center";
        cell4.appendChild(document.createTextNode(daysText));
        row.appendChild(cell4);

        var cell5 = document.createElement('td');
        cell5.className = "row1";
        cell5.align = "center";
        cell5.appendChild(document.createTextNode(dateText));
        row.appendChild(cell5);

        var cell6 = document.createElement('td');
        cell6.className = "row1";
        cell6.align = "center";
        cell6.appendChild(document.createTextNode(monthText));
        row.appendChild(cell6);
    }
}

//
// remove a system access rule
//
function removeRule(idTable, rowNum, idSelect) {
    var listElem = document.getElementById(idSelect);
    var table = document.getElementById(idTable);

    if (listElem) {
//orig    if ((listElem) && (listElem.selectedIndex >= 0)) {
        listElem.options[rowNum - 1] = null;
        //listElem.selectedIndex = 0;
    }

    if (table) {
        table.deleteRow(rowNum);
    }
}

//
// Changes display class when mouse is over element.
//
function cover(target) {
    previousClass = target.className;
    if (target.className == "selectedM") {
        target.className = "overSelected";
    } else {
        target.className = "over";
    }
}

//
// Restores previous display class when mouse goes off element.
//
function cout(target) {
    target.className = previousClass;
}

function clickDate(target) {
    if (target.className == "overSelected") {
        previousClass = "";
        target.className = "over";
    } else {
        previousClass = "selectedM";
        target.className = "selectedM";
    }
}

function selectAllDates(select, cellName) {
    var cells = document.getElementsByName(cellName);
    for (var i=0; i< cells.length; i++) {
        if (select) {
            cells[i].className = "selectedM";
        } else {
            cells[i].className = "";
        }
    }
}

//
//
//
function selectAllDays(select, cellName) {
    var cells = document.getElementsByName(cellName);

    for (var i=0; i<cells.length; i++) {
        if (select) {
            //cells[i].className = "selectedM";
        } else {
            cells[i].value = i;
            cells[i].checked = false;
        }
    }
}

//
//
//
function selectAllDaysOfWeek(name) {
    obj = document.getElementsByName(name);
    if (obj[0].checked) {
        for (var i=1; i < obj.length; i++) {
            obj[i].checked = true;
        }
    } else {
        for (var i=1; i < obj.length; i++) {
            obj[i].checked = false;
        }
    }
}

//
//
//
function selectAllCheck(name) {
    obj = document.getElementsByName(name);
    if (obj[0].checked) {
        obj[0].checked = false;
    }
}

//
//
//
function toggleDaysAndDates(userGroup, radioName) {
    var uGroup = userGroup;
    var byDay = document.getElementsByName(radioName)[0];
    var byDate = document.getElementsByName(radioName)[1];
    var obj;

    if (byDay.checked) {
        if (uGroup == 'userGrp') {
            // Reveal the inactive calendar.
            obj = document.getElementById("groupDatesInactive");
            obj.style.display = "";

            // Hide the active calendar.
            obj = document.getElementById("groupDates");
            obj.style.display = "none";
        
            // Enable all weekday checkboxes.
            obj = document.getElementsByName("groupWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = false;
            }
            
             // Reset all dates.
             selectAllDates(false, 'groupDate');
        }
        else if (uGroup == 'resGrp') {
            // Reveal the inactive calendar.
            obj = document.getElementById("resDatesInactive");
            obj.style.display = "";

            // Hide the active calendar.
            obj = document.getElementById("resDates");
            obj.style.display = "none";
        
            // Enable all weekday checkboxes.
            obj = document.getElementsByName("resWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = false;
            }
            
            // Reset all dates.
            selectAllDates(false, 'resDate');
        }
    }
    else if (byDate.checked) {
        if (uGroup == 'userGrp') {
            // Hide the inactive calendar.
            obj = document.getElementById("groupDatesInactive");
            obj.style.display = "none";

            // Reveal the active calendar.
            obj = document.getElementById("groupDates");
            obj.style.display = "";
        
            // Disable all the weekday checkboxes.
            obj = document.getElementsByName("groupWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = true;
            }

            // Reset all weekday checkboxes
            selectAllDays(false, 'groupWeekly');
        }
        else if (uGroup == 'resGrp') {
            // Hide the inactive calendar.
            obj = document.getElementById("resDatesInactive");
            obj.style.display = "none";

            // Reveal the active calendar.
            obj = document.getElementById("resDates");
            obj.style.display = "";
        
            // Disable all the weekday checkboxes.
            obj = document.getElementsByName("resWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = true;
            }

            // Reset all weekday checkboxes
            selectAllDays(false, 'resWeekly');
        }
    }
}

function sortByValue(a, b) {
    var x = a.value.toLowerCase();
    var y = b.value.toLowerCase();
    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
}

function removeItems(fromId) {
    var fromElem = document.getElementById(fromId);

    // Remove source list selected elements.
    for (var i=fromElem.options.length-1; i >= 0; i--) {
        if ( fromElem.options[i] != null && ( fromElem.options[i].selected == true ) ) {
            if (fromElem.options[i].selected) {
                fromElem.options[i] = null;
            }
        }
    }
}

function moveItems(fromId, toId) {
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
    }
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

// Initialize the pulldown start/end time menues.
var groupStartTimeMenu = new EditableList('groupStartTime', 'groupStartTimeLayer', 'groupStartTimes', 'groupSystemAccessTab');
var groupEndTimeMenu = new EditableList('groupEndTime', 'groupEndTimeLayer', 'groupEndTimes', 'groupSystemAccessTab');
var resStartTimeMenu = new EditableList('resStartTime', 'resStartTimeLayer', 'resStartTimes', 'resourceSystemAccessTab');
var resEndTimeMenu = new EditableList('resEndTime', 'resEndTimeLayer', 'resEndTimes', 'resourceSystemAccessTab');

// I think this is needed for the pulldown menues to function correctly.
function timeMouseSelect(e) {
    groupStartTimeMenu.mouseSelect(0);
    groupEndTimeMenu.mouseSelect(0);
    resStartTimeMenu.mouseSelect(0);
    resEndTimeMenu.mouseSelect(0);
}

document.onmousedown = timeMouseSelect;

// Set the time input boxes to default values.
// Leave blank.
//document.getElementById("groupStartTime").value = document.getElementById("groupStartTimes").options[0].text;
//document.getElementById("groupEndTime").value = document.getElementById("groupEndTimes").options[0].text;
//document.getElementById("resStartTime").value = document.getElementById("resStartTimes").options[0].text;
//document.getElementById("resEndTime").value = document.getElementById("resEndTimes").options[0].text;

dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
grpStartTimeId = "groupStartTime";
grpEndTimeId = "groupEndTime";
resStartTimeId = "resStartTime";
resEndTimeId = "resEndTime";

groupStartTimeMenu.setChangeListener(gpTimeChangeListener);
groupEndTimeMenu.setChangeListener(gpTimeChangeListener);
resStartTimeMenu.setChangeListener(gpTimeChangeListener);
resEndTimeMenu.setChangeListener(gpTimeChangeListener);

//
// gpTimeChangeListener() - registered for time change events.
//
function gpTimeChangeListener(elem) {
  dCal.setTimeString(elem.id, elem.value);
  gpCheckStartBeforeEnd(elem);
} /* gpTimeChangeListener */

//
//
//
function gpCheckStartBeforeEnd(elem) {
  var curStartTime;
  var curEndTime;
  var type;

  if ((elem.id).substring(0,5) == "group") {
      type = "grp"
  } else {
      type = "res"
  }

  if (type == "grp") {
      curStartTime = dCal.getTimeObject(grpStartTimeId)
      curEndTime = dCal.getTimeObject(grpEndTimeId);
  } else if (type == "res") {
      curStartTime = dCal.getTimeObject(resStartTimeId)
      curEndTime = dCal.getTimeObject(resEndTimeId);
  }

  var start = new Date();

  if (curStartTime != null) {
      start.setHours(curStartTime.getHours());
      start.setMinutes(curStartTime.getMinutes());
  } else {
      start.setHours(0);
      start.setMinutes(0);
  }
  start.setSeconds(0);

  var end = new Date();

  if (curEndTime != null) {
    end.setHours(curEndTime.getHours());
    end.setMinutes(curEndTime.getMinutes());
  } else {
    end.setHours(0);
    end.setMinutes(0);
  }
  end.setSeconds(0);

  if (start.getTime() >= end.getTime()) {
    if (elem.id == grpStartTimeId || elem.id == resStartTimeId) {
      // push end time forward 30 minutes.
      var startMillis = start.getTime();
      startMillis += (30 * 60 * 1000);
      var newTime = new Date(startMillis);

      if (type == "grp") {
          dCal.setTime(grpEndTimeId, newTime);
      } else if (type == "res") {
          dCal.setTime(resEndTimeId, newTime);
      }
    }
    else if (elem.id == grpEndTimeId || elem.id == resEndTimeId) {
      // push start time back 30 minutes.
      var endMillis = end.getTime();
      endMillis -= (30 * 60 * 1000);
      var newTime = new Date(endMillis);

      if (type == "grp") {
          dCal.setTime(grpStartTimeId, newTime);
      } else if (type == "res") {
          dCal.setTime(resStartTimeId, newTime);
      }
    }
  }
}

</script>

<%@ include file="/common/footer.jsp" %>
