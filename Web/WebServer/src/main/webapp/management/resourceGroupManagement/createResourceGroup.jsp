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
             import="com.nortel.appcore.app.drac.server.webserver.struts.security.resourceGroupManagement.form.*"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
/****************************************************************************
 * OpenDRAC WEB GUI version 1.0
 *
 * File: /management/resourceGroupManagement/createResourceGroup.jsp
 * Author: Colin Hart
 *
 * Description:
 *  This page allows user to create a new resource group
 *
 ****************************************************************************/


String pageRef = "drac.security.resourceGroupManagement.create"; %>

<%@ include file="/common/header_struts.jsp" %>
<%@ include file="/common/calendar.jsp" %>

<script type="text/javascript" src="/scripts/tabpane.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<script language="JavaScript" src="/scripts/calendar.js"></script>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<%
    // Set the form-bean as an object we can use in any scriplets
    CreateResourceGroupForm createForm = (CreateResourceGroupForm) request.getAttribute("CreateResourceGroupForm");
%>

<html:form method="POST" action="/management/resourceGroupManagement/handleCreateResourceGroup.do" onsubmit="return doSubmit()">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
    <table cellspacing="0" cellpadding="0" border="0" width="550" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5"/></td>
        </tr>
        <logic:notPresent name="editable">
        <tr>
            <td align="center"><bean:message key="drac.security.resourceGroupManagement.create.text.notAllowed"/></td>
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
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                    <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.security.resourceGroupManagement.create.title.errors"/></b></font></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                </tr>
            </tbody>
            </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Resource Group error contents. -->
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
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td><img src="/images/spacer.gif" height="5"/></td>
        </tr>
        </logic:messagesPresent>

        <tr>
            <td align="center"><bean:message key="drac.security.resourceGroupManagement.create.title"/></td>
        </tr>
        <tr>
            <td>
                <!-- Header. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.resourceGroupManagement.create.title.info"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Resource Group information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td class="tbForm1">
                            <bean:message key="drac.security.resourceGroupManagement.create.name"/>
                        </td>
                        <td class="tbForm1">
                            <html:text tabindex="1" style="width: 200px" property="name" errorStyleClass="invalid"/>
                        </td>
<!--
                        <td class="tbForm1" valign="middle" align="left">
                            <html:checkbox property="defaultResourceGroup"></html:checkbox>
                            <bean:message key="drac.security.resourceGroupManagement.create.defaultResourceGroup"/>
                        </td>
-->
                    </tr>
                    <tr>
                        <td class="tbForm1">
                            <bean:message key="drac.security.resourceGroupManagement.create.parentResourceGroup"/>
                        </td>
                        <td class="tbForm1">
                            <html:select tabindex="2" styleId="resourceGroupComboBoxId" property="parentResourceGroup" onchange="retrieveResources();">
                                <logic:iterate id="parentResourceGroups" indexId="i" name="CreateResourceGroupForm" property="parentResourceGroups">
                                    <html:option value="${fn:escapeXml(parentResourceGroups)}">${fn:escapeXml(CreateResourceGroupForm.parentResourceGroups[i])}</html:option>
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
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
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
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.userGroupManagement.create.membershipInfo"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
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
                    <td align='left' valign="top" class="row2">
                        <div class="tab-pane" id="membershipPane">
                            <script type="text/javascript">
                            tp1 = new WebFXTabPane(document.getElementById("membershipPane"));
                            </script>
        
                            <div class="tab-page" id="resourcesTab" align="center">
                                <h2 class="tab"><bean:message key="drac.security.resourceGroup.resources"/></h2>
                                <script type="text/javascript">tp1.addTabPage( document.getElementById( "resourcesTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0">
                                    <tr>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.resourceGroupManagement.create.available.resources"/><br>
                                            <html:select tabindex="3" property="availableTNAs" size="5" style="width:250px" styleId="availableResList" multiple="true" styleClass="gen">
                                                <html:options property="availableTNAs"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewResource('availableResList')"><bean:message key="drac.security.resourceGroupManagement.detail.viewResource"/></a>
                                        </td>
                                        <td class="tbForm1" align="center" valign="middle">
                                            <input tabindex="4" type="button" id="addUserToMembership" onclick="moveItems('availableResList','resMembershipList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><p>
                                            <input tabindex="5" type="button" id="addUserToMembership" onclick="moveItems('resMembershipList','availableResList');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
                                        </td>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.resourceGroupManagement.create.member.resources"/><br>
                                            <html:select tabindex="6" property="memberTNAs" size="5" style="width:250px" styleId="resMembershipList" multiple="true" styleClass="gen">
                                                <html:options property="memberTNAs"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewResource('resMembershipList')"><bean:message key="drac.security.resourceGroupManagement.detail.viewResource"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <div class="tab-page" id="userGroupMemberTab" align="center">
                                <h2 class="tab"><bean:message key="drac.security.resourceGroup.userGroups"/></h2>
                                <script type="text/javascript">tp1.addTabPage(document.getElementById("userGroupMemberTab"));</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userGroupMemberTable">
                                    <tr>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.resourceGroupManagement.create.available.userGroups"/><br>
                                            <html:select tabindex="3" property="availableUserGroups" size="5" style="width:250px" styleId="availableUserGroups" multiple="true" styleClass="gen">
                                                <html:options property="availableUserGroups"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewUserGroup('availableUserGroups')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                        </td>
                                        <td class="tbForm1" align="center" valign="middle">
                                            <input tabindex="4" type="button" id="addUserToMembership" onclick="moveItems('availableUserGroups','userGroupsMembers');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><p>
                                            <input tabindex="5" type="button" id="addUserToMembership" onclick="moveItems('userGroupsMembers','availableUserGroups');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
                                        </td>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.resourceGroupManagement.create.member.userGroups"/><br>
                                            <html:select tabindex="6" property="referencedUserGroups" size="5" style="width:250px" styleId="userGroupsMembers" multiple="true" styleClass="gen">
                                                <html:options property="referencedUserGroups"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewUserGroup('userGroupsMembers')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </td>
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
        <tr>
            <td>
            <!-- Header. -->
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                    <td class="tbtbot"><center><b><bean:message key="drac.security.resourcePolicy"/></b></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                 </tr>
                </tbody>
            </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Resource Group Policy tab -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td colspan='4' align='left' valign="top" class="row2">

                            <!-- Resource Group policy pane -->
                            <div class="tab-pane" id="resGroupPane">
                                <script type="text/javascript">
                                tp2 = new WebFXTabPane(document.getElementById("resGroupPane"));
                                </script>

                                <!-- Define the Bandwidth Control Rules sub tab -->
                                <div class="tab-page" id="resGroupBandwidthTab">
                                    <h2 class="tab"><bean:message key="drac.security.policy.bandwidthControl"/></h2>
                                    <script type="text/javascript">tp2.addTabPage(document.getElementById("resGroupBandwidthTab"));</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resGroupBandwidthTable">
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.policy.bw.maxservicebandwidth"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="7" style = "width: 110px" property="resourceMaxServiceSize"  errorStyleClass="invalid"/>
                                                <bean:message key="drac.text.Mbps"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.policy.bw.maxserviceduration"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="8" style = "width: 110px" property="resourceMaxServiceDuration"  errorStyleClass="invalid"/>
                                                <bean:message key="drac.security.globalPolicy.seconds"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                            	<bean:message key="drac.security.policy.bw.maxservicesize"/>
                                                
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="9" style = "width: 110px" property="resourceMaxServiceBandwidth"  errorStyleClass="invalid"/>
                                                <bean:message key="drac.text.Mb"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.policy.bw.maxaggregateservicesize"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="10" style = "width: 110px" property="resourceMaxAggregateServiceSize"  errorStyleClass="invalid"/>
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
<!-- later
                                <div class="tab-page" id="resourceStateControlTab">
                                    <h2 class="tab"><bean:message key="drac.security.policy.accessState"/></h2>
                                    <script type="text/javascript">tp2.addTabPage(document.getElementById("resourceStateControlTab"));</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceStateTable">
                                        <tr>
                                            <td class="tbForm1" align="right">
                                                <b><bean:message key="drac.security.policy.accessState.resource.title"/></b>
                                            </td>
                                            <td class="tbForm1">
                                                <html:select tabindex="7" size="1" property="resourceAccessState" style="width: 110px" errorStyleClass="invalid">
                                                    <html:option value="0"><bean:message key="drac.security.policy.access.state.open"/></html:option>
                                                    <html:option value="1"><bean:message key="drac.security.policy.access.state.closed"/></html:option>
                                                    <html:option value="2"><bean:message key="drac.security.policy.access.state.disabled"/></html:option>
                                                </html:select>
                                             </td>
                                         </tr>
                                         <tr>
                                            <td class="tbForm1" colspan="2" align="right">
                                                <img src="/images/spacer.gif" height="269">
                                            </td>
                                         </tr>
                                    </table>
                                </div>
-->
        
                                <!-- Define the Resource Access Rules sub tab -->
                                <div class="tab-page" id="resourceSystemAccessTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.policy.systemAccess.resource"/></h2>
                                    <script type="text/javascript">tp2.addTabPage(document.getElementById("resourceSystemAccessTab"));</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceAccessTable">
                                        <tr>
                                            <td class="tbForm1">
                                                <bean:message key="drac.security.policy.systemAccess.permission"/>
                                            </td>
                                            <td class="tbForm1">
                                                <select tabindex="7" id="resPermission" size="1">
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
                                                <input tabindex="8" type="text" id="resStartTime" size="11" styleClass="gen" onchange="JavaScript:rgTimeChangeListener(this);"
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
                                                <input tabindex="10" type="text" id="resEndTime" size="11" styleClass="gen" onchange="JavaScript:rgTimeChangeListener(this);"
                                                        /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:resEndTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                                <div id="resEndTimeLayer" style="position:absolute; z-index:100; overflow:auto; display:none;" onmouseover="javascript:resEndTimeMenu.overMenu(true);" onmouseout="JavaScript:resEndTimeMenu.overMenu(false);">
                                                    <select id="resEndTimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:resEndTimeMenu.textSet(this.value);" onkeypress="JavaScript:resEndTimeMenu.comboKey();">
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
                                                <input type="radio" name="resDaysOrDates" checked onclick="toggleDaysAndDates('resGrp', 'resDaysOrDates');">
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1">
                                            </td>
                                            <td class="tbForm1">
                                                <INPUT TYPE="checkbox" name="resWeekly" value="0" onclick="selectAllDaysOfWeek('resWeekly')"><b>- <bean:message key="drac.security.policy.systemAccess.selectAll"/> -</b><br>
                                                <INPUT TYPE="checkbox" name="resWeekly" value="1" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.sun"/></b><br>
                                                <INPUT TYPE="checkbox" name="resWeekly" value="2" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.mon"/></b><br>
                                                <INPUT TYPE="checkbox" name="resWeekly" value="3" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.tue"/></b><br>
                                                <INPUT TYPE="checkbox" name="resWeekly" value="4" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.wed"/></b><br>
                                                <INPUT TYPE="checkbox" name="resWeekly" value="5" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.thu"/></b><br>
                                                <INPUT TYPE="checkbox" name="resWeekly" value="6" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.fri"/></b><br>
                                                <INPUT TYPE="checkbox" name="resWeekly" value="7" onclick="selectAllCheck('resWeekly')"><b><bean:message key="drac.schedule.sat"/></b>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="tbForm1">
                                                <bean:message key="drac.security.policy.systemAccess.specifyDates"/>
                                            </td>
                                            <td>
                                                <input type="radio" name="resDaysOrDates" onclick="toggleDaysAndDates('resGrp', 'resDaysOrDates');">
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
                                                        <td onclick="selectAllDates(true, 'resDay')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.selectAll"/></td>
                                                    </tr>
                                                    <c:forEach var="row" begin="1" end="4">
                                                    <tr class="cells">
                                                    <c:forEach var="col" begin="1" end="7">
                                                        <td name="resDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" align="center">${col + (row-1)*7}</td>
                                                    </c:forEach>
                                                    </tr>
                                                    </c:forEach>
                                                    <tr class="cells">
                                                        <td name="resDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">29</td>
                                                        <td name="resDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">30</td>
                                                        <td name="resDay" onmouseout="cout(this)" onmouseover="cover(this)" onclick="clickDate(this)" class="cells" align="center">31</td>
                                                    </tr>
                                                    <tr class="cells">
                                                        <td onclick="selectAllDates(false, 'resDay')" onmouseover="cover(this)" onmouseout="cout(this)" align="center" colspan="7"><bean:message key="drac.security.policy.systemAccess.clearAll"/></td>
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
                                                       onclick="addRule('resSystemAccessRules','resPermission','resAccessMonth','resDay','resWeekly','resStartTime','resEndTime','resAccessRulesTable');"/>
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
    <tr>
        <td><img src="/images/spacer.gif" height="10"/></td>
    </tr>
    <tr>
        <td>
            <html:submit tabindex="98" property="Create" styleId="Create">
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

</html:form>

<script language="javascript">
var previousClass = "";

function doSubmit() {
    setAllSelected(document.getElementById('resSystemAccessRules'));
    setAllSelected(document.getElementById('resMembershipList'));
    setAllSelected(document.getElementById('userGroupsMembers'));
    setAllSelected(document.getElementById('availableResList'));
    setAllSelected(document.getElementById('availableUserGroups'));

    var createButton = document.getElementById("Create");
    if (createButton != null)
    {
        createButton.disabled = true;
    }


    return true;
}

function doReset() {
    location.href="/management/resourceGroupManagement/createResourceGroup.do";
}

function setAllSelected(elem) {
    if (elem) {
        for (var i=0; i < elem.options.length; i++)
            elem.options[i].selected = true;
    }
}

function viewResource(elemId) {
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.location = "/management/resourceGroupManagement/queryResource.do?res=" + escape(elem.options[elem.selectedIndex].value);
    }
    return false;
}

function viewUserGroup(elemId) {
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.location = "/management/userGroupManagement/queryUserGroup.do?ugName=" + escape(elem.options[elem.selectedIndex].value);
    }
    return false;
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

//
// Add a time based (system access) rule
//
function addRule(idList, idPer, idMth, idD, idWeek, idFrT, idToT, idTable) {
    var selectElem = document.getElementById(idList);
    var permElem = document.getElementById(idPer);

    var monthElem = document.getElementById(idMth);
    var dayElems = document.getElementsByName(idD);
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
        months = months.substring(0,months.lastIndexOf(":"));
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
        for (var i=0; i < dayElems.length; i++) {
            if (dayElems[i].className == "selectedM") {
                numChecked++;
                dates += parseInt(dayElems[i].innerHTML) + ":";
                dateText += parseInt(dayElems[i].innerHTML) + " ";
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
function processResourceListResponse() {
    if (req.readyState == 4) {
        if (req.status == 200) {
            resourceListBox = document.getElementById("availableResList");
            resourceListBox.options.length = 0;
            var tnas = req.responseXML.getElementsByTagName("tna");
            if (tnas.length > 0) {
                for (var i=0; i < tnas.length; i++) {
                    tna = tnas[i].firstChild.nodeValue;
                    resourceListBox.options[i] = new Option(tna, tna);
                }
            }
        }
    }
}

//
//
//
function retrieveResources() {

    // Clear any existing entries in the members list; with this onChange,
    // any entries in the members list could become duplicates, or worse,
    // outside the permissible list for the newly selected parent RG.
    document.getElementById("resMembershipList").options.length = 0;

    parentResourceGroupSelect = document.getElementById("resourceGroupComboBoxId");    
    var url = "/drac?action=getTnaForResGrp&gid=" + escape(parentResourceGroupSelect.value);
        if (window.XMLHttpRequest) {
             req = new XMLHttpRequest();
        } else if (window.ActiveXObject) {
             req = new ActiveXObject("Microsoft.XMLHTTP");
        }
        req.open("GET", url, true);
        req.onreadystatechange = processResourceListResponse;
        req.send(null);
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
/*
        if (uGroup == 'userGrp') {
            obj = document.getElementById("groupDateText");
            obj.style.display = "none";
            obj = document.getElementById("groupDates");
            obj.style.display = "none";
        
            // Enable all weekday checkboxes.
            obj = document.getElementsByName("groupWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = false;
            }
            selectAllDates(false, 'groupDay'); // reset all dates.
        } else
*/
        if (uGroup == 'resGrp') {
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
            selectAllDates(false, 'resDay'); // reset all dates.
        }
    } else if (byDate.checked) {
/*
        if (uGroup == 'userGrp') {
            obj = document.getElementById("groupDateText");
            obj.style.display = "";
            obj = document.getElementById("groupDates");
            obj.style.display = "";
        
            // Disable all the weekday checkboxes.
            obj = document.getElementsByName("groupWeekly");
            for (var i=0; i < obj.length; i++) {
                obj[i].disabled = true;
            }
            selectAllDays(false, 'groupWeekly'); // reset all weekday checkboxes
        } else 
*/
        if (uGroup == 'resGrp') {
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
            selectAllDays(false, 'resWeekly'); // reset all weekday checkboxes
        }
    }
}

// Initialize the pulldown start/end time menues.
var resStartTimeMenu = new EditableList('resStartTime', 'resStartTimeLayer', 'resStartTimes', 'resourceSystemAccessTab');
var resEndTimeMenu = new EditableList('resEndTime', 'resEndTimeLayer', 'resEndTimes', 'resourceSystemAccessTab');

// Set the time input boxes to default values.
// Leave blank.
// document.getElementById("resStartTime").value = document.getElementById("resStartTimes").options[0].text;
// document.getElementById("resEndTime").value = document.getElementById("resEndTimes").options[0].text;

// I think this is needed for the pulldown menues to function correctly.
function timeMouseSelect(e) {
    resStartTimeMenu.mouseSelect(0);
    resEndTimeMenu.mouseSelect(0);
}

document.onmousedown = timeMouseSelect;

dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
resStartTimeId = "resStartTime";
resEndTimeId = "resEndTime";

resStartTimeMenu.setChangeListener(rgTimeChangeListener);
resEndTimeMenu.setChangeListener(rgTimeChangeListener);

//
// rgTimeChangeListener() - registered for time change events.
//
function rgTimeChangeListener(elem) {
  dCal.setTimeString(elem.id, elem.value);
  rgCheckStartBeforeEnd(elem);
} /* gpTimeChangeListener */

//
//
//
function rgCheckStartBeforeEnd(elem) {
  var curStartTime;
  var curEndTime;

  curStartTime = dCal.getTimeObject(resStartTimeId)
  curEndTime = dCal.getTimeObject(resEndTimeId);

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
    if (elem.id == resStartTimeId) {
      // push end time forward 30 minutes.
      var startMillis = start.getTime();
      startMillis += (30 * 60 * 1000);
      var newTime = new Date(startMillis);

      dCal.setTime(resEndTimeId, newTime);
    }
    else if (elem.id == resEndTimeId) {
      // push start time back 30 minutes.
      var endMillis = end.getTime();
      endMillis -= (30 * 60 * 1000);
      var newTime = new Date(endMillis);

      dCal.setTime(resStartTimeId, newTime);
    }
  }
}

retrieveResources();

</script>

<%@ include file="/common/footer.jsp" %>
