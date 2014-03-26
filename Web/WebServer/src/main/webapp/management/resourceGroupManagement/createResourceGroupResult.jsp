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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
/****************************************************************************
 * OpenDRAC WEB GUI version 1.0
 *
 * File: /management/resourceGroupManagement/createResourceGroupResult.jsp
 * Author: Colin Hart
 *
 * Description:
 *  This page returns result of creating a resource group
 *
 ****************************************************************************/

String pageRef = "drac.security.resourceGroupManagement.create.result";

String resourceGroupName = StringEscapeUtils.escapeXml((String)request.getParameter("rgid"));
%>


<%@ include file="/common/header_struts.jsp" %>
<script type="text/javascript" src="/scripts/tabpane.js"></script>

<html:form action="/management/resourceGroupManagement/queryResourceGroup.do">
    <table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
    <tbody>
        <tr>
            <td align="center">
                <!-- Header -->
                <logic:notEmpty name="ResourceGroupForm" property="name" scope="request">
                <font color="green"><b>
                <bean:message key="drac.security.resourceGroupManagement.create.success" arg0="${fn:escapeXml(ResourceGroupForm.name)}" /><br>
                <bean:message key="drac.security.edit.warning"/></b></font>
                </logic:notEmpty>
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <logic:messagesPresent message="true">
                    <html:messages id="message" message="true">
                    <tr>
                        <td align="center" class="gen">
                            <font color="red"><b><bean:write name="message"/></b></font>
                        </td>
                    </tr>
                    </html:messages>
                    </logic:messagesPresent>
                    <logic:messagesNotPresent>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.resourceGroupManagement.create.title.info"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                    </tr>
                    </logic:messagesNotPresent>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Contents -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <logic:notEmpty name="ResourceGroupForm" property="name" scope="request">
                    <tr>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.name" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <a href="/management/resourceGroupManagement/queryResourceGroup.do?rgName=<c:out value='${resourceGroupName}'/>"><b>${fn:escapeXml(ResourceGroupForm.name)}</b></a>
                        </td>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.parentResourceGroup" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.parentResourceGroup)}</b>
                        </td>
                    </tr>
                    <tr>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.defaultResourceGroup" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.defaultResourceGroup)}</b>
                        </td>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.lastModificationUserID" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.lastModificationUserID)}</b>
                        </td>
                    </tr>
                    <tr>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.creationDate" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.creationDate)}</b> at<br><b>${fn:escapeXml(ResourceGroupForm.creationTime)}</b>
                        </td>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.lastModifiedDate" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.lastModifiedDate)}</b> at<br><b>${fn:escapeXml(ResourceGroupForm.lastModifiedTime)}</b>
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

        <tr>
            <td>
            <!-- Header -->
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
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td align='left' valign="top" class="row2">
                            <div class="tab-pane" id="membershipPane">
                                <script type="text/javascript">
                                tp0 = new WebFXTabPane( document.getElementById( "membershipPane" ) );
                                </script>
    
                                <div class="tab-page" id="resourcesMemberTab">
                                    <h2 class="tab"><bean:message key="drac.security.resourceGroup.resources"/></h2>
                                    <script type="text/javascript">tp0.addTabPage( document.getElementById( "resourcesMemberTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourcesMemberTable">
<!-- is never editable here
                                        <logic:present name="editable">
                                        <tr>
                                            <td class="tbForm1" nowrap align="center">
                                                <bean:message key="drac.security.resourceGroupManagement.create.member.resources"/><br>
                                                <logic:empty name="ResourceGroupForm" property="memberTNAs">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="ResourceGroupForm" property="memberTNAs">
                                                    <logic:iterate id="resourceID" name="ResourceGroupForm" property="memberTNAs">
                                                        <b>${fn:escapeXml(resourceID)}</b><br>
                                                    </logic:iterate>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
                                        </tr>
                                        </logic:present>

                                        <logic:notPresent name="editable" scope="request">
-->
                                        <tr>
                                            <td class="tbForm1">
                                                <bean:message key="drac.security.resourceGroupManagement.detail.member.resources"/><br>
                                                <logic:empty name="ResourceGroupForm" property="memberTNAs">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="ResourceGroupForm" property="memberTNAs">
                                                <logic:iterate id="resource" name="ResourceGroupForm" property="memberTNAs">
                                                    <b><html:link href="/management/resourceGroupManagement/queryResource.do" paramId="res" paramName="resource">${fn:escapeXml(resource)}</html:link></b><br>
                                                </logic:iterate>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
<!--
                                        </logic:notPresent>
-->
                                    </table>
                                </div>

                                <div class="tab-page" id="userGroupMemberTab">
                                    <h2 class="tab"><bean:message key="drac.security.resourceGroup.userGroups"/></h2>
                                    <script type="text/javascript">tp0.addTabPage( document.getElementById( "userGroupMemberTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userGroupMemberTable">
<!-- is never editable on this page
                                        <logic:present name="editable">
                                        <tr>
                                            <td class="tbForm1" nowrap align="center">
                                                <bean:message key="drac.security.resourceGroupManagement.create.available.userGroups"/><br>
                                                <html:select property="availableUserGroups" size="5" style="width:250px" styleId="availableUserGroupsList" multiple="true">
                                                    <html:options property="availableUserGroups"/>
                                                </html:select>
                                                <a href="javascript:void(0)" onclick="return viewUserGroup('availableUserGroupsList')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                            </td>
                                            <td class="tbForm1" align="center" valign="middle">
                                                <input type="button" id="addUserGrpToMembership" onclick="moveItems('availableUserGroupsList','membershipUserGroupsList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><p>
                                                <input type="button" id="removeUserGroupFromMembership" onclick="moveItems('membershipUserGroupsList','availableUserGroupsList');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
                                            </td>
                                            <td class="tbForm1" align="center">
                                                <bean:message key="drac.security.resourceGroupManagement.detail.member.addToUserGroups"/><br>
                                                <html:select property="userGroupMembership" size="5" style="width:250px" styleId="membershipUserGroupsList" multiple="true">
                                                    <html:options property="userGroupMembership"/>
                                                </html:select>
                                                <a href="javascript:void(0)" onclick="return viewUserGroup('membershipUserGroupsList')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                            </td>
                                        </tr>
                                        </logic:present>
                                        <logic:notPresent name="editable" scope="request">
-->
                                        <tr>
                                            <td class="tbForm1">
                                                <bean:message key="drac.security.membership"/>&nbsp;<bean:message key="drac.security.resourceGroup.userGroups"/><br>
                                                <logic:empty name="ResourceGroupForm" property="userGroupMembership">
                                                    &nbsp;
                                                </logic:empty>
                                                <logic:notEmpty name="ResourceGroupForm" property="userGroupMembership">
                                                <logic:iterate id="refUgName" name="ResourceGroupForm" property="userGroupMembership">
                                                    <html:link href="/management/userGroupManagement/queryUserGroup.do"
                                                        paramId="ugName" paramName="refUgName"><b>${fn:escapeXml(refUgName)}</b>
                                                    </html:link><br>
                                                </logic:iterate>
                                                </logic:notEmpty>
                                            </td>
                                        </tr>
<!--
                                        </logic:notPresent>
-->
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
                <!-- Drop shadow -->
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
                <!-- Header -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.resourceGroupManagement.detail.resourceGroupPolicy" /></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Resource Group policy contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td colspan='4' align='left' valign="top" class="row2">
                            <div class="tab-pane" id="resourcePane">
                                <script type="text/javascript">
                                tp2 = new WebFXTabPane( document.getElementById( "resourcePane" ) );
                                </script>
    
                                <div class="tab-page" id="resourceBandwidthTab">
                                <h2 class="tab"><bean:message key="drac.security.policy.bandwidthControl"/></h2>
                                    <script type="text/javascript">tp2.addTabPage( document.getElementById( "resourceBandwidthTab" ) );</script>
                                    <table width="80%" cellspacing="1" cellpadding="5" border="0" id="resourceBandwidthTable">
                                        <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.policy.bw.maxservicesize"/>
                                        </td>
                                        <td class="tbForm1">
                                        	<b>${fn:escapeXml(ResourceGroupForm.maximumServiceBandwidth)}</b>                                            
                                            <bean:message key="drac.text.Mbps"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.policy.bw.maxserviceduration"/>
                                        </td>
                                        <td class="tbForm1">
                                            <b>${fn:escapeXml(ResourceGroupForm.maximumServiceDuration)}</b>
                                            <bean:message key="drac.security.globalPolicy.seconds"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.policy.bw.maxservicebandwidth"/>
                                        </td>
                                        <td class="tbForm1">
                                            <b>${fn:escapeXml(ResourceGroupForm.maximumServiceSize)}</b>
                                            <bean:message key="drac.text.Mb"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                        	<bean:message key="drac.security.policy.bw.maxaggregateservicesize"/>
                                        </td>
                                        <td class="tbForm1">
                                            <b>${fn:escapeXml(ResourceGroupForm.maximumAggregateServiceSize)}</b>
                                            <bean:message key="drac.text.Mb"/>
                                        </td>
                                    </tr>
                                    </table>
                                </div>  <!-- end of resourceBandwidthTab -->
    
<!--
                                <div class="tab-page" id="resourceStateControlTab">
                                <h2 class="tab"><bean:message key="drac.security.policy.accessState"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "resourceStateControlTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceStateTable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <b><c:out value="${ResourceGroupForm.stateRule}"/></b>
                                        </td>
                                    </tr>
                                </table>
                                </div>
-->
    
                                <!-- Define the Resource Group Resource Access Rules sub tab -->
                                <div class="tab-page" id="resourceSystemAccessTab">
                                    <h2 class="tab"><bean:message key="drac.security.policy.systemAccess.resource"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "resourceSystemAccessTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceAccessTable">
                                        <tr>
                                            <td class="tbForm1" colspan="4">
                                                <table width="100%" id="resAccessRulesTable" border="1" class="forumline">
                                                    <tr>
                                                        <th><bean:message key="drac.security.policy.systemAccess.permission"/></th>
                                                        <th><bean:message key="drac.security.policy.systemAccess.startTime"/></th>
                                                        <th><bean:message key="drac.security.policy.systemAccess.endTime"/></th>
                                                        <th><bean:message key="drac.security.policy.systemAccess.onDays"/></th>
                                                        <th><bean:message key="drac.security.policy.systemAccess.onDates"/></th>
                                                        <th><bean:message key="drac.security.policy.systemAccess.inMonths"/></th>
                                                    </tr>
        
                                                <logic:iterate id="rule" name="ResourceGroupForm" scope="request" property="resRules" indexId="cnt">
                                                <tr>
                                                    <td class="row1" align="center">
                                                        <c:if test="${rule.permission eq 'grant'}">
                                                            <bean:message key="drac.security.policy.grant"/>
                                                        </c:if>
                                                        <c:if test="${rule.permission eq 'deny'}">
                                                            <bean:message key="drac.security.policy.deny"/>
                                                        </c:if>
                                                    </td>
                                                    <td class="row1" nowrap align="center">${rule.startTime}</td>
                                                    <td class="row1" nowrap align="center">${rule.endTime}</td>
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
                                </div> <!-- end of resourceSystemAccessTab -->
                            </div> <!-- end of resource pane -->
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
    </tbody>
    </table>
    <html:hidden property="locale" value="<c:out value='${myLanguage}'/>" />
</html:form>

<%@ include file="/common/footer.jsp"%>
