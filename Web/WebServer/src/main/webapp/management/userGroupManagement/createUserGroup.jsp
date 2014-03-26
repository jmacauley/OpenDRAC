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
             import="com.nortel.appcore.app.drac.server.webserver.struts.security.userGroupManagement.form.*"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
/****************************************************************************
 * OpenDRAC WEB GUI version 1.0
 *
 * File: /management/userGroupManagement/createUserGroupPage.jsp
 * Author: Colin Hart
 *
 * Description:
 *  This page allows user to create a user group
 *
 ****************************************************************************/

String pageRef = "drac.security.userGroupManagement.create"; %>

<%@ include file="/common/header_struts.jsp" %>
<%@ include file="/common/calendar.jsp" %>

<%
    // Set the form-bean as an object we can use in any scriplets
    CreateUserGroupForm createForm = (CreateUserGroupForm) request.getAttribute("CreateUserGroupForm");
%>

<script type="text/javascript" src="/scripts/tabpane.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<script language="JavaScript" src="/scripts/calendar.js"></script>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<html:form method="POST" action="/management/userGroupManagement/handleCreateUserGroup.do" onsubmit="return doSubmit()">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
    <table cellspacing="0" cellpadding="0" border="0" width="550" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5" /></td>
        </tr>
        <logic:notPresent name="editable">
        <tr>
            <td align="center"><bean:message key="drac.security.userGroupManagement.create.text.notAllowed"/></td>
        </tr>
        </logic:notPresent>
        <logic:present name="editable">
        <logic:messagesPresent>
        <tr>
            <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.security.userManagement.create.title.errors"/></b></font></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
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
                <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
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
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.security.resourceGroupManagement.create.title.errors"/></b></font></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
            </tbody>
            </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- User Group information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td align="center" class="tbForm1" nowrap><html:errors/></td>
                    </tr>
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
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td><img src="/images/spacer.gif" height="5" /></td>
        </tr>
        </logic:messagesPresent>

        <tr>
            <td align="center"><bean:message key="drac.security.userGroupManagement.create.title"/></td>
        </tr>
        <tr>
            <td>
                <!-- Header. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.userGroupManagement.create.title.info"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- User Group information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td class="tbForm1">
                            <bean:message key="drac.security.userGroupManagement.create.name"/>
                        </td>
                        <td class="tbForm1">
                            <html:text tabindex="1" style="width: 200px" property="name"  errorStyleClass="invalid" />
                        </td>
                    </tr>
                    <tr>
                        <td class="tbForm1">
                            <bean:message key="drac.security.userGroupManagement.create.userGroupType"/>
                        </td>
                        <td class="tbForm1">
                            <html:select tabindex="2" property="userGroupType" styleId="userGroupSelect">
                            	<html:option value="2"><bean:message key="drac.security.userGroupType.grpAdmin"/></html:option>
                                 <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'SYSTEM_ADMIN'}">
                                	<html:option value="1"><bean:message key="drac.security.userGroupType.sysAdmin"/></html:option>
                                </c:if>
                                <html:option value="0"><bean:message key="drac.security.userGroupType.user"/></html:option>
                            </html:select>
                        </td>
                    </tr>
                    <tr>
                        <td class="tbForm1">
                            <bean:message key="drac.security.userGroupManagement.create.parentUserGroup"/>
                        </td>
                        <td class="tbForm1">
                            <html:select tabindex="3" property="parentUserGroup">
                                <logic:iterate id="availableUserGroup" indexId="i" name="CreateUserGroupForm" property="availableUserGroups">
                                    <html:option value="${fn:escapeXml(availableUserGroup)}">${fn:escapeXml(CreateUserGroupForm.availableUserGroups[i])}</html:option>
                                </logic:iterate>
                            </html:select>
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
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>

        <tr>
            <td>
                <!-- Header. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.userGroupManagement.create.membershipInfo"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Membership Information -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td align='left' valign="top" class="row2">
                        <div class="tab-pane" id="membershipPane">
                            <script type="text/javascript">
                            tp1 = new WebFXTabPane( document.getElementById( "membershipPane" ) );
                            </script>

                            <div class="tab-page" id="userMemberTab" >
                                <h2 class="tab"><bean:message key="drac.security.userGroup.userMemberShip"/></h2>
                                <script type="text/javascript">tp1.addTabPage( document.getElementById( "userMemberTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userMemberTable">
                                    <tr>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.userGroupManagement.create.available.users"/><br>
                                            <html:select tabindex="4" property="availableUsers" size="5" style="width:250px" styleId="availableUserList" multiple="true" styleClass="gen">
                                                <html:options property="availableUsers"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewUser('availableUserList')"><bean:message key="drac.security.userGroupManagement.detail.viewUser"/></a>
                                        </td>
                                        <td class="tbForm1" align="center" valign="middle">
                                            <input tabindex="5" type="button" id="addUserToMembership" onclick="moveItems('availableUserList','membershipUserList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><p>
                                            <input tabindex="6" type="button" id="removeUserFromMembership" onclick="moveItems('membershipUserList','availableUserList');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
                                        </td>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.userGroup.membership"/>&nbsp;<bean:message key="drac.security.userGroup.users"/><br>
                                            <html:select tabindex="7" property="userMembership" size="5" style="width:250px" styleId="membershipUserList" multiple="true" styleClass="gen">
                                                <html:options property="userMembership"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewUser('membershipUserList')"><bean:message key="drac.security.userGroupManagement.detail.viewUser"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <div class="tab-page" id="resourceGroupMemberTab" align="center">
                                <h2 class="tab"><bean:message key="drac.security.userGroup.resourceGroupmembership"/></h2>
                                <script type="text/javascript">tp1.addTabPage( document.getElementById( "resourceGroupMemberTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceGroupMemberTable">
                                    <tr>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.userGroupManagement.create.available.resourcegroups"/><br>
                                            <html:select tabindex="4" property="availableResourceGroups" size="5" style="width:250px" styleId="availableResGroupList" multiple="true" styleClass="gen">
                                                <html:options property="availableResourceGroups"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewResGroup('availableResGroupList')"><bean:message key="drac.security.userGroupManagement.detail.viewResGrp"/></a>
                                        </td>
                                        <td class="tbForm1" align="center" valign="middle">
                                            <input tabindex="5" type="button" id="addResGrpToMembership" onclick="moveItems('availableResGroupList','membershipResGroupList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><P>
                                            <input tabindex="6" type="button" id="removeResGrpFromMembership" onclick="moveItems('membershipResGroupList','availableResGroupList');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
                                        </td>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.userGroup.membership"/>&nbsp;<bean:message key="drac.security.userGroup.resourceGroupmembership"/><br>
                                            <html:select tabindex="7" property="resourceGroupMembership" size="5" style="width:250px" styleId="membershipResGroupList" multiple="true" styleClass="gen">
                                                <html:options property="resourceGroupMembership"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewResGroup('membershipResGroupList')"><bean:message key="drac.security.userGroupManagement.detail.viewResGrp"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </div>

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
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>

        <tr>
            <td>
                <!-- Header. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.userGroupManagement.create.title.userGroupPolicy"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- user group policy tab -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td colspan='4' align='left' valign="top" class="row2">
                            <!-- user group policy pane -->
                            <div class="tab-pane" id="groupPane">
                                <script type="text/javascript">
                                    tp2 = new WebFXTabPane( document.getElementById( "groupPane" ) );
                                </script>

                               <!-- Define the Bandwidth Control Rules sub tab -->
                                <div class="tab-page" id="groupBandwidthTab" >
                                    <h2 class="tab"><bean:message key="drac.security.policy.bandwidthControl"/></h2>
                                    <script type="text/javascript">tp2.addTabPage( document.getElementById( "groupBandwidthTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="groupBandwidthTable">
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.policy.bw.maxservicebandwidth"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="8" style = "width: 110px" property="groupMaxServiceSize"  errorStyleClass="invalid"/>
                                                <bean:message key="drac.text.Mbps"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.policy.bw.maxserviceduration"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="9" style = "width: 110px" property="groupMaxServiceDuration"  errorStyleClass="invalid"/>
                                                <bean:message key="drac.security.globalPolicy.seconds"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                            	<bean:message key="drac.security.policy.bw.maxservicesize"/>                                                
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="10" style = "width: 110px" property="groupMaxServiceBandwidth"  errorStyleClass="invalid"/>
                                                <bean:message key="drac.text.Mb"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.policy.bw.maxaggregateservicesize"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="11" style = "width: 110px" property="groupMaxAggregateServiceSize"  errorStyleClass="invalid"/>
                                                <bean:message key="drac.text.Mb"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="164">
                                            </td>
                                         </tr>
                                    </table>
                                </div>
        
<!--
                                <div class="tab-page" id="groupAccessControlTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.policy.accessControl"/></h2>
                                    <script type="text/javascript">tp2.addTabPage( document.getElementById( "groupAccessControlTab" ) );</script>
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
                                                </select>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="160">
                                            </td>
                                         </tr>
                                    </table>
                                </div>
-->
        
                                <!-- Define the System Access Rules sub tab -->
                                <div class="tab-page" id="groupSystemAccessTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.policy.systemAccess"/></h2>
                                    <script type="text/javascript">tp2.addTabPage( document.getElementById( "groupSystemAccessTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="groupSystemAccessTable">
                                        <tr>
                                            <td class="tbForm1">
                                                <bean:message key="drac.security.policy.systemAccess.permission"/>
                                            </td>
                                            <td class="tbForm1">
                                                <select tabindex="8" id="groupPermission" size="1">
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
                                                <input tabindex="9" type="text" id="groupStartTime" size="11" styleClass="gen" onchange="JavaScript:ugTimeChangeListener(this);"
                                                        /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:groupStartTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                                <div id="groupStartTimeLayer" style="position:absolute; z-index:100; overflow:auto; display:none;" onmouseover="javascript:groupStartTimeMenu.overMenu(true);" onmouseout="JavaScript:groupStartTimeMenu.overMenu(false);">
                                                    <select id="groupStartTimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:groupStartTimeMenu.textSet(this.value);" onkeypgroups="JavaScript:groupStartTimeMenu.comboKey();">
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
                                                <input tabindex="10" type="text" id="groupEndTime" size="11" styleClass="gen" onchange="JavaScript:ugTimeChangeListener(this);"
                                                        /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:groupEndTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                                <div id="groupEndTimeLayer" style="position:absolute; z-index:100; overflow:auto; display:none;" onmouseover="javascript:groupEndTimeMenu.overMenu(true);" onmouseout="JavaScript:groupEndTimeMenu.overMenu(false);">
                                                    <select id="groupEndTimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:groupEndTimeMenu.textSet(this.value);" onkeypress="JavaScript:groupEndTimeMenu.comboKey();">
                                                        <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                                                            <option value="<bean:write name="timeStrChoice" property="value" />">
                                                                <bean:write name="timeStrChoice" property="label" />
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
                                                <input type="radio" name="groupDaysOrDates" checked onclick="toggleDaysAndDates('userGrp', 'groupDaysOrDates');">
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1">
                                            </td>
                                            <td class="tbForm1">
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="0" onclick="selectAllDaysOfWeek('groupWeekly')"><b>- <bean:message key="drac.security.policy.systemAccess.selectAll"/> -</b><br>
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="1" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.sun"/></b><br>
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="2" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.mon"/></b><br>
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="3" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.tue"/></b><br>
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="4" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.wed"/></b><br>
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="5" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.thu"/></b><br>
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="6" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.fri"/></b><br>
                                                <INPUT TYPE="checkbox" name="groupWeekly" value="7" onclick="selectAllCheck('groupWeekly')"><b><bean:message key="drac.schedule.sat"/></b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1">
                                                <bean:message key="drac.security.policy.systemAccess.specifyDates"/>
                                            </td>
                                            <td>
                                                <input type="radio" name="groupDaysOrDates" onclick="toggleDaysAndDates('userGrp', 'groupDaysOrDates');">
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
                                                        <td onclick="selectAllDates(true,'groupDay')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.selectAll"/></td>
                                                    </tr>
                                                    <c:forEach var="row" begin="1" end="4">
                                                        <tr class="cells">
                                                        <c:forEach var="col" begin="1" end="7">
                                                            <td name="groupDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" align="center">${col + (row-1)*7}</td>
                                                        </c:forEach>
                                                        </tr>
                                                    </c:forEach>
                                                    <tr class="cells">
                                                        <td name="groupDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">29</td>
                                                        <td name="groupDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">30</td>
                                                        <td name="groupDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">31</td>
                                                    </tr>
                                                    <tr class="cells">
                                                        <td onclick="selectAllDates(false,'groupDay')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.clearAll"/></td>
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
                                            <td class="tbForm1" colspan="4" align="left">
                                                <input tabindex="20" type="button" value="<bean:message key='drac.security.policy.button.add'/>"
                                                       onclick="addRule('groupSystemAccessRules','groupPermission','groupAccessMonth','groupDay','groupWeekly','groupStartTime','groupEndTime','groupAccessRulesTable');"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" colspan="4">
                                                <html:select size="5" property="groupSystemAccessRules" style="display:none" styleId="groupSystemAccessRules" errorStyleClass="invalid" multiple="true">
                                                    <html:options property="groupSystemAccessRules" />
                                                </html:select>
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
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
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
                    <bean:message key="drac.text.submit" />
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

</html:form>

<script language="javascript">
var previousClass = "";

function doSubmit() {
    setAllSelected(document.getElementById('availableUserList'));
    setAllSelected(document.getElementById('availableResGroupList'));
    setAllSelected(document.getElementById('membershipUserList'));
    setAllSelected(document.getElementById('membershipResGroupList'));
    setAllSelected(document.getElementById('hiddenAccessRules'));
    setAllSelected(document.getElementById('groupSystemAccessRules'));

    var createButton = document.getElementById("Create");
    if (createButton != null)
    {
        createButton.disabled = true;
    }

    return true;
}

function doReset() {
    location.href="/management/userGroupManagement/createUserGroup.do";
}

function setAllSelected(elem) {
    if (elem) {
        for (var i=0; i < elem.options.length; i++) {
            elem.options[i].selected = true;
        }
    }
}

function viewUser(elemId) {
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.location = "/management/userManagement/queryUser.do?uid=" + escape(elem.options[elem.selectedIndex].value);
    }
    return false;
}

function viewResGroup(elemId) {
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.location = "/management/resourceGroupManagement/queryResourceGroup.do?rgName=" + escape(elem.options[elem.selectedIndex].value);
    }
    return false;
}

// remove an access control rule
function removeAccess(id, appId, hiddenId) {
    var appList = document.getElementById(appId);
    var accessList = document.getElementById(id);
    var hiddenElem = document.getElementById(hiddenId);
    if (!accessList || !appList || !hiddenElem) return;
    for (var i=accessList.options.length-1; i>=0; i--) {
        if (accessList.options[i].selected) {
            var textArray = accessList.options[i].text.split("=")[1].split(",");
            var valueArray = hiddenElem.options[i].value.split("=")[1].split(",");
            for (var j=0; j < textArray.length; j++) {
                // add items back to application list
                appList.options[appList.options.length] = new Option(textArray[j], valueArray[j]);
            }
            accessList.options[i] = null;
            hiddenElem.options[i] = null;
        }
    }
    document.getElementById("addAccessButton").disabled = false;

    accessList.selectedIndex = 0;
    if (accessList.options.length == 0) {
        document.getElementById("removeAccessButton").disabled = true;
    }
}

// add an access control rule
function addAccess(keyId, valueId, accessId, hiddenId) {
    var keyElem = document.getElementById(keyId);
    var valueList = document.getElementById(valueId);
    var accessElem = document.getElementById(accessId);
    var hiddenElem = document.getElementById(hiddenId);
    if (!keyElem || !valueList || !accessElem || !hiddenElem) {
        return;
    }
    if (valueList.selectedIndex < 0) return;
    var ruleValue = keyElem.options[keyElem.selectedIndex].value + "=";
    var ruleText = keyElem.options[keyElem.selectedIndex].text + "=";
    for (var i=0; i < valueList.options.length; i++) {
        if (valueList.options[i].selected) {
            ruleValue = ruleValue + valueList.options[i].value + ",";
            ruleText = ruleText + valueList.options[i].text + ",";
        }
    }
    ruleValue = ruleValue.substr(0, ruleValue.length-1);
    ruleText = ruleText.substr(0, ruleText.length-1);
    accessElem.options[accessElem.options.length] = new Option(ruleText, ruleValue);
    hiddenElem.options[hiddenElem.options.length] = new Option(ruleValue, ruleValue);

    document.getElementById("removeAccessButton").disabled = false;

    // remove selected apps
    for (var i=valueList.options.length-1; i>=0; i--) {
        if (valueList.options[i].selected) {
            valueList.options[i] = null;
        }
    }
    valueList.selectedIndex = 0;
    if (valueList.options.length == 0) {
        document.getElementById("addAccessButton").disabled = true;
    }
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

    var permText;
    if (permElem.options[permElem.selectedIndex].value == "grant") {
        permText = "<c:out value='${sessionScope["drac.security.policy.grant"]}'/>";
    } else {
        permText = "<c:out value='${sessionScope["drac.security.policy.deny"]}'/>";
    }
    
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
        months = months.substring(0, months.lastIndexOf(":"));
    }
    
    if (numMonthsSelected == 0 || numMonthsSelected == 12) {
        monthText = "<c:out value='${sessionScope["drac.security.policy.systemAccess.everyMonth"]}'/>";
    }
    
    var numChecked = 0;

    // Were any days (of the week) selected?
    var daysText = "";
    var daysOfWeek = "";

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

    // Update table
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
        cell2.appendChild(document.createTextNode(fromTimeElem.value));
        row.appendChild(cell2);

        var cell3 = document.createElement('td');
        cell3.className = "row1";
        cell3.align = "center";
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
        cell5.noWrap = true;
        cell5.appendChild(document.createTextNode(dateText));
        row.appendChild(cell5);

        var cell6 = document.createElement('td');
        cell6.className = "row1";
        cell6.align = "center";
        cell6.noWrap = true;
        cell6.appendChild(document.createTextNode(monthText));
        row.appendChild(cell6);
    }
}

//
// remove a time-based (system access) rule
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
//orig            cells[i].value = i+1;
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
            selectAllDates(false, 'groupDay'); // reset all dates.
        }
/*
        else if (uGroup == 'resGrp') {
            obj = document.getElementById("resDateText");
            obj.style.display = "none";
            obj = document.getElementById("resDates");
            obj.style.display = "none";
        
            // Enable all weekday checkboxes.
            obj = document.getElementsByName("resWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = false;
            }
            selectAllDates(false, 'resDate'); // reset all dates.
        }
*/
    } else if (byDate.checked) {
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
            selectAllDays(false, 'groupWeekly'); // reset all weekday checkboxes
        }
/*
        else if (uGroup == 'resGrp') {
            obj = document.getElementById("resDateText");
            obj.style.display = "";
            obj = document.getElementById("resDates");
            obj.style.display = "";
        
            // Disable all the weekday checkboxes.
            obj = document.getElementsByName("resWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = true;
            }
            selectAllDays(false, 'resWeekly'); // reset all weekday checkboxes
        }
*/
    }
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
    var srcList = document.getElementById(fromId);
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
        for (var i=0; i < fromElem.options.length; i++) {
            if (fromElem.options[i].selected) {
                // Incorporate into working list.
                newDestList[len] = new Option(srcList.options[i].text, srcList.options[i].value, srcList.options[i].defaultSelected, srcList.options[i].selected);
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
                if (fromElem.options[i].selected) {
                    fromElem.options[i] = null;
                }
            }
        }

        fromElem.options.selectedIndex = 0;
    }
}

// Initialize the pulldown start/end time menues.
var groupStartTimeMenu = new EditableList('groupStartTime', 'groupStartTimeLayer', 'groupStartTimes', 'groupSystemAccessTab');
var groupEndTimeMenu = new EditableList('groupEndTime', 'groupEndTimeLayer', 'groupEndTimes', 'groupSystemAccessTab');

// I think this is needed for the pulldown menues to function correctly.
function timeMouseSelect(e) {
    groupStartTimeMenu.mouseSelect(0);
    groupEndTimeMenu.mouseSelect(0);
}

document.onmousedown = timeMouseSelect;

// Set the time input boxes to default values.
// Leave blank.
// document.getElementById("groupStartTime").value = document.getElementById("groupStartTimes").options[0].text;
// document.getElementById("groupEndTime").value = document.getElementById("groupEndTimes").options[0].text;

dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
grpStartTimeId = "groupStartTime";
grpEndTimeId = "groupEndTime";

groupStartTimeMenu.setChangeListener(ugTimeChangeListener);
groupEndTimeMenu.setChangeListener(ugTimeChangeListener);

//
// ugTimeChangeListener() - registered for time change events.
//
function ugTimeChangeListener(elem) {
  dCal.setTimeString(elem.id, elem.value);
  ugCheckStartBeforeEnd(elem);
} /* gpTimeChangeListener */

//
//
//
function ugCheckStartBeforeEnd(elem) {
  var curStartTime;
  var curEndTime;

  curStartTime = dCal.getTimeObject(grpStartTimeId)
  curEndTime = dCal.getTimeObject(grpEndTimeId);

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
    if (elem.id == grpStartTimeId) {
      // push end time forward 30 minutes.
      var startMillis = start.getTime();
      startMillis += (30 * 60 * 1000);
      var newTime = new Date(startMillis);

      dCal.setTime(grpEndTimeId, newTime);
    }
    else if (elem.id == grpEndTimeId) {
      // push start time back 30 minutes.
      var endMillis = end.getTime();
      endMillis -= (30 * 60 * 1000);
      var newTime = new Date(endMillis);

      dCal.setTime(grpStartTimeId, newTime);
    }
  }
}

</script>

<%@ include file="/common/footer.jsp" %>
