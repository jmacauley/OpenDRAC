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

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
/****************************************************************************
* OpenDRAC WEB GUI version 1.0
*
* File: /management/userGroupManagement/createUserGroupResult.jsp
*
* Description:
*
****************************************************************************/

String pageRef = "drac.security.userGroupManagement.create.result";

String userGroupName = StringEscapeUtils.escapeXml((String)request.getParameter("gid"));

%>

<%@ include file="/common/header_struts.jsp"%>
<script type="text/javascript" src="/scripts/tabpane.js"></script>

<html:form action="/management/userGroupManagement/queryUserGroup.do">
    <table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
        <tbody>
          <tr>
            <td align="center">
            <!-- Header -->
            <logic:notEmpty name="UserGroupForm" property="name" scope="request">
                <font color="green"><b>
                <bean:message key="drac.security.userGroupManagement.create.success" arg0="${fn:escapeXml(UserGroupForm.name)}" /><br>
                <bean:message key="drac.security.edit.warning"/></b></font>
            </logic:notEmpty>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
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
                  <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                  <td class="tbtbot"><center><b><bean:message key="drac.security.userGroupManagement.create.title.info" /></b></center></td>
                  <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                </tr>
                </tbody>
            </table>
            </td>
          </tr>
          <tr>
            <td>
            <!-- Contents -->
            <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                <logic:notEmpty name="UserGroupForm" property="name" scope="request">
                <tr>
                    <td class="tbForm1" nowrap>
                        <bean:message key="drac.security.userGroupManagement.detail.name" />
                    </td>
                    <td class="tbForm1" nowrap>
                        <a href="/management/userGroupManagement/queryUserGroup.do?ugName=<c:out value='${userGroupName}'/>"><b>${fn:escapeXml(UserGroupForm.name)}</b></a>
                    </td>
                    <td class="tbForm1" nowrap>
                        <bean:message key="drac.security.userGroupManagement.detail.parentUserGroup" />
                    </td>
                    <td class="tbForm1" nowrap>
                        <b>${fn:escapeXml(UserGroupForm.parentUserGroup)}</b>
                    </td>
                </tr>
                <tr>
                    <td class="tbForm1" nowrap>
                        <bean:message key="drac.security.userGroupManagement.detail.userGroupType" />
                    </td>
                    <td class="tbForm1" nowrap>
                        <b>${fn:escapeXml(UserGroupForm.userGroupType)}</b>
                    </td>
                    <td class="tbForm1" nowrap>
                        <bean:message key="drac.security.userGroupManagement.detail.lastModificationUserID" />
                    </td>
                    <td class="tbForm1" nowrap colspan="2">
                        <b>${fn:escapeXml(UserGroupForm.lastModificationUserID)}</b>
                    </td>
                </tr>
                <tr>
                    <td class="tbForm1" nowrap>
                        <bean:message key="drac.security.userGroupManagement.detail.creationDate" />
                    </td>
                    <td class="tbForm1" nowrap>
                        <b>${fn:escapeXml(UserGroupForm.creationDate)}</b> at<br><b>${fn:escapeXml(UserGroupForm.creationTime)}</b>
                    </td>
                    <td class="tbForm1" nowrap>
                        <bean:message key="drac.security.userGroupManagement.detail.lastModifiedDate" />
                    </td>
                    <td class="tbForm1" nowrap>
                        <b>${fn:escapeXml(UserGroupForm.lastModifiedDate)}</b> at<br><b>${fn:escapeXml(UserGroupForm.lastModifiedTime)}</b>
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
                            tp0 = new WebFXTabPane( document.getElementById( "membershipPane" ) );
                            </script>

                            <div class="tab-page" id="userMemberTab">
                                <h2 class="tab"><bean:message key="drac.security.userGroup.users"/></h2>
                                <script type="text/javascript">tp0.addTabPage(document.getElementById("userMemberTab"));</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userMemberTable">
                                    <tr>
                                        <td class="tbForm1">
                                            <bean:message key="drac.security.userGroup.membership"/>&nbsp;<bean:message key="drac.security.userGroup.users"/><br>
                                            <logic:empty name="UserGroupForm" property="userMembership">
                                                &nbsp;
                                            </logic:empty>
                                            <logic:notEmpty name="UserGroupForm" property="userMembership">
                                            <logic:iterate id="userID" name="UserGroupForm" property="userMembership">
                                                <b><html:link href="/management/userManagement/queryUser.do" paramId="uid" paramName="userID">${fn:escapeXml(userID)}</html:link></b><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <div class="tab-page" id="resourceGroupMemberTab">
                                <h2 class="tab"><bean:message key="drac.security.userGroup.resourceGroupmembership"/></h2>
                                <script type="text/javascript">tp0.addTabPage( document.getElementById( "resourceGroupMemberTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceGroupMemberTable">
                                    <tr>
                                        <td class="tbForm1">
                                            <bean:message key="drac.security.userGroup.membership"/>&nbsp;<bean:message key="drac.security.userGroup.resourceGroupmembership"/><br>
                                            <logic:empty name="UserGroupForm" property="resourceGroupMembership">
                                                &nbsp;
                                            </logic:empty>
                                            <logic:notEmpty name="UserGroupForm" property="resourceGroupMembership">
                                            <logic:iterate id="resGroup" name="UserGroupForm" property="resourceGroupMembership">
                                                <b><html:link href="/management/resourceGroupManagement/queryResourceGroup.do" paramId="rgName" paramName="resGroup">${fn:escapeXml(resGroup)}</html:link></b><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                </table>
                            </div>

<!--
                            <div class="tab-page" id="userGroupMemberTab">
                                <h2 class="tab"><bean:message key="drac.security.userGroup.userGroupMemberShip"/></h2>
                                <script type="text/javascript">tp0.addTabPage( document.getElementById( "userGroupMemberTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userGroupMemberTable">
                                    <tr>
                                        <td class="tbForm1">
                                            <bean:message key="drac.security.userGroup.membership"/>&nbsp;<bean:message key="drac.security.userGroup.userGroupMemberShip"/><br>
                                            <html:select property="userGroupMembership" size="5" style="width:300px" styleId="membershipUserGroupList">
                                                <html:options property="userGroupMembership"/>
                                            </html:select>
                                        </td>
                                    </tr>
                                </table>
                            </div>
-->
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
                <!-- Header -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.userGroupManagement.create.title.userGroupPolicy"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>

        <tr>
            <td>
                <!-- User Group Policy contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td colspan="4" align="left" valign="top" class="row2">
                            <div class="tab-pane" id="groupPane">
                                <script type="text/javascript">
                                tp1 = new WebFXTabPane( document.getElementById( "groupPane" ) );
                                </script>

                                <div class="tab-page" id="groupBandwidthTab">
                                <h2 class="tab"><bean:message key="drac.security.policy.bandwidthControl"/></h2>
                                <script type="text/javascript">tp1.addTabPage( document.getElementById( "groupBandwidthTab" ) );</script>
                                <table width="80%" cellspacing="1" cellpadding="5" border="0" id="groupBandwidthTable">
                                <tr>
                                    <td class="tbForm1" nowrap>
                                        <bean:message key="drac.security.policy.bw.maxservicebandwidth"/>
                                    </td>
                                    <td class="tbForm1">
                                       <b>${fn:escapeXml(UserGroupForm.maximumServiceSize)}</b>
                                       <bean:message key="drac.text.Mbps"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="tbForm1" nowrap>
                                        <bean:message key="drac.security.policy.bw.maxserviceduration"/>
                                    </td>
                                    <td class="tbForm1">
                                        <b>${fn:escapeXml(UserGroupForm.maximumServiceDuration)}</b>
                                        <bean:message key="drac.security.globalPolicy.seconds"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="tbForm1" nowrap>
                                    	<bean:message key="drac.security.policy.bw.maxservicesize"/>                                        
                                    </td>
                                    <td class="tbForm1">
                                        <b>${fn:escapeXml(UserGroupForm.maximumServiceBandwidth)}</b>
                                        <bean:message key="drac.text.Mb"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="tbForm1" nowrap>
                                        <bean:message key="drac.security.policy.bw.maxaggregateservicesize"/>
                                    </td>
                                    <td class="tbForm1">
                                        <b>${fn:escapeXml(UserGroupForm.maximumAggregateServiceSize)}</b>
                                        <bean:message key="drac.text.Mb"/>
                                    </td>
                                </tr>
                                </table>
                                </div>

<!--
                                <div class="tab-page" id="groupAccessControlTab">
                                <h2 class="tab"><bean:message key="drac.security.policy.accessControl"/></h2>
                                <script type="text/javascript">tp1.addTabPage( document.getElementById( "groupAccessControlTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="groupAccessTable">
                                    <tr>
                                        <td class="tbForm1" align="left" valign="top">
                                            <b><bean:message key="drac.security.policy.access.application"/></b><br>

                                            <p>
                                            <b><bean:message key="drac.security.policy.accessType"/></b><br>
                                        </td>
                                        <td valign="top" class="tbForm1" colspan="3">
                                        <html:select size="7" styleId="groupAccessControlRulesHidden" property="groupAccessControlRules" style="display:none" errorStyleClass="invalid" multiple="true">
                                            <html:options property="groupAccessControlRules" />
                                        </html:select>
                                        <br>
                                        <logic:iterate name="UserGroupForm" property="accessControlRules" scope="request" id="rule">
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
                                            </logic:iterate>
                                        </td>
                                    </tr>
                                </table>
                                </div>
-->

                                <div class="tab-page" id="groupSystemAccessTab">
                                <h2 class="tab"><bean:message key="drac.security.policy.systemAccess"/></h2>
                                <script type="text/javascript">tp1.addTabPage( document.getElementById( "groupSystemAccessTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="groupSystemAccessTable">
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <table width="100%" id="groupAccessRulesTable" border="1" class="forumline">
                                                <tr>
                                                    <th><bean:message key="drac.security.policy.systemAccess.permission"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.startTime"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.endTime"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.onDays"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.onDates"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.inMonths"/></th>
                                                </tr>
                                                <logic:iterate id="rule" name="UserGroupForm" scope="request" property="groupRules" indexId="cnt">
                                                <tr>
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
                                                                <bean:message key="drac.security.policy.systemAccess.everyday"/>
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

        </tbody>
    </table>
</html:form>

<script type="text/javascript" src="/scripts/handleExpand.js"></script>

<%@ include file="/common/footer.jsp"%>
