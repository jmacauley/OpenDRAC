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

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
/****************************************************************************
 * OpenDRAC WEB GUI version 1.0
 *
 * File: /management/resourceGroupManagement/resourceGroupDetails.jsp
 *
 * Description:
 *
 ****************************************************************************/

String pageRef = "drac.security.resourceGroupManagement.detail";

%>

<%@ include file="/common/header_struts.jsp" %>
<%@ include file="/common/calendar.jsp" %>

<script type="text/javascript" src="/scripts/tabpane.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<script LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></script>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<html:form action="/management/resourceGroupManagement/handleEditResourceGroup.do">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
    <table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
    <tbody>
        <tr>
            <td>
                <!-- Header -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <logic:messagesPresent message="true">
                    <html:messages id="message" message="true">
                    <tr>
                        <td align="center" class="gen" colspan="3">
                            <font color="red"><b><bean:write name="message"/></b></font>
                        </td>
                    </tr>
                    </html:messages>
                    </logic:messagesPresent>
                    <bean:parameter id="success" name="successParam" value=""/>
                    <c:if test="${success != ''}">
                    <tr>
                        <td colspan="3" align="center" class="tbForm1" nowrap><font color="green"><b>${success}</b></font></td>
                    </tr>
                    </c:if>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.resourceGroupManagement.resourceGroup"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
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
                    <logic:notEmpty name="ResourceGroupForm" property="name" scope="request">
                    <tr>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.name"/>
                        </td>
                        <td class="tbForm1" nowrap align="left">
                            <b>${ResourceGroupForm.clientSafeName}</b>
                        </td>
                        <td class="tbForm1">
                            <bean:message key="drac.security.resourceGroupManagement.detail.parentResourceGroup"/>
                        </td>
                        <td class="tbForm1" nowrap>
                            <logic:empty name="ResourceGroupForm" property="parentResourceGroup">
                                &nbsp;
                            </logic:empty>
                            <logic:notEmpty name="ResourceGroupForm" property="parentResourceGroup">
                                <b><a href="/management/resourceGroupManagement/queryResourceGroup.do?rgName=${fn:escapeXml(ResourceGroupForm.parentResourceGroup)}">${fn:escapeXml(ResourceGroupForm.parentResourceGroup)}</a></b>
                            </logic:notEmpty>
                        </td>
                    </tr>
                    <tr>
                        <td class="tbForm1">
                            <bean:message key="drac.security.resourceGroupManagement.detail.defaultResourceGroup"/>
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.defaultResourceGroup)}</b>
                        </td>
                        <td valign="top" class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.lastModificationUserID"/>
                        </td>
                        <td valign="top" class="tbForm1" nowrap colspan="3">
                            <b><a href="/management/userManagement/editUser.do?uid=${fn:escapeXml(ResourceGroupForm.lastModificationUserID)}">${fn:escapeXml(ResourceGroupForm.lastModificationUserID)}</a></b>
                        </td>
                    </tr>
                    <tr>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.creationDate"/>
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.creationDate)}</b>&nbsp;<bean:message key="drac.text.at"/><br><b>${fn:escapeXml(ResourceGroupForm.creationTime)}</b>
                        </td>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.detail.lastModifiedDate"/>
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceGroupForm.lastModifiedDate)}</b>&nbsp;<bean:message key="drac.text.at"/><br><b>${fn:escapeXml(ResourceGroupForm.lastModifiedTime)}</b>
                        </td>
                    </tr>
                    </logic:notEmpty>
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
                    <tr>
                        <td align='left' valign="top" class="row2">
                            <div class="tab-pane" id="membershipPane">
                                <script type="text/javascript">
                                tp0 = new WebFXTabPane( document.getElementById( "membershipPane" ) );
                                </script>

                                <div class="tab-page" id="resourcesMemberTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.resourceGroup.resources"/></h2>
                                    <script type="text/javascript">tp0.addTabPage( document.getElementById( "resourcesMemberTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourcesMemberTable">
								    <c:choose>
								    	<c:when test="${ResourceGroupForm.defaultResourceGroup}">
								    		<tr><td class="tbForm1" align="center" colspan="4"><b><bean:message key="drac.security.resourceGroup.acces.to.all.message"/></b></td></tr>
								      	</c:when>
								      	<c:otherwise>

		                                        <logic:present name="editable">
		                                        <tr>
		                                            <td class="tbForm1" align="center" colspan="2">
		                                                <bean:message key="drac.security.resourceGroupManagement.create.available.resources"/><br>
		                                                <html:select property="availableTNAs" size="5" style="width:250px" styleId="availableResList" multiple="true" styleClass="gen">
		                                                    <html:options property="availableTNAs"/>
		                                                </html:select><br>
		                                                <a href="javascript:void(0)" onclick="return viewResource('availableResList')"><bean:message key="drac.security.resourceGroupManagement.detail.viewResource"/></a>
		                                            </td>
		                                            <td class="tbForm1" align="center" valign="middle">
		                                                <input type="button" id="addUserToMembership" onclick="moveItems('availableResList','resMembershipList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><p>
		                                                <input type="button" id="addUserToMembership" onclick="moveItems('resMembershipList','availableResList');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
		                                            </td>
		                                            <td class="tbForm1" align="center">
		                                                <bean:message key="drac.security.resourceGroupManagement.create.member.resources"/><br>
		                                                <html:select property="memberTNAs" size="5" style="width:250px" styleId="resMembershipList" multiple="true" styleClass="gen">
		                                                    <html:options property="memberTNAs"/>
		                                                </html:select><br>
		                                                <a href="javascript:void(0)" onclick="return viewResource('resMembershipList')"><bean:message key="drac.security.resourceGroupManagement.detail.viewResource"/></a>
		                                            </td>
		                                        </tr>
		                                        </logic:present>
		                                        <logic:notPresent name="editable" scope="request">
		                                        <tr>
		                                            <td class="tbForm1">
		                                                <bean:message key="drac.security.resourceGroupManagement.detail.member.resources"/><br>
		                                                <logic:iterate id="resource" name="ResourceGroupForm" property="memberTNAs">
		                                                    <b><html:link href="/management/resourceGroupManagement/queryResource.do" paramId="res" paramName="resource">${fn:escapeXml(resource)}</html:link></b><br>
		                                                </logic:iterate>
		                                            </td>
		                                        </tr>
		                                        </logic:notPresent>
									      </c:otherwise>
									    </c:choose>                                        
                                    </table>
                                </div>

                                <div class="tab-page" id="userGroupMemberTab" align="center">
                                    <h2 class="tab"><bean:message key="drac.security.resourceGroup.userGroups"/></h2>
                                    <script type="text/javascript">tp0.addTabPage( document.getElementById( "userGroupMemberTab" ) );</script>
                                    <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userGroupMemberTable">
                                        <logic:present name="editable">
                                        <tr>
                                            <td class="tbForm1" align="center">
                                                <bean:message key="drac.security.resourceGroupManagement.create.available.userGroups"/><br>
                                                <html:select property="availableUserGroups" size="5" style="width:250px" styleId="availableUserGroupsList" multiple="true" styleClass="gen">
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
                                                <html:select property="userGroupMembership" size="5" style="width:250px" styleId="membershipUserGroupsList" multiple="true" styleClass="gen">
                                                    <html:options property="userGroupMembership"/>
                                                </html:select>
                                                <a href="javascript:void(0)" onclick="return viewUserGroup('membershipUserGroupsList')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                            </td>
                                        </tr>
                                        </logic:present>
                                        <logic:notPresent name="editable" scope="request">
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
                                        </logic:notPresent>
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
                      <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                      <tr>
                          <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                          <td class="tbtbot"><center><b><bean:message key="drac.security.resourceGroupManagement.detail.resourceGroupPolicy" /></b></center></td>
                          <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                      </tr>
                  </tbody>
                  </table>
              </td>
          </tr>

          <tr>
             <td>
             <!-- Resource Group policy tab -->
             <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
             <tbody>
                <tr>
                    <td colspan='4' align='left' valign="top" class="row2">
                        <!-- Resource Group policy pane -->
                        <div class="tab-pane" id="resGroupPane">
                            <script type="text/javascript">
                            tp2 = new WebFXTabPane(document.getElementById("resGroupPane"));
                            </script>

                            <!-- Define the Resource Group Bandwidth Control Rules sub tab -->
                            <div class="tab-page" id="resGroupBandwidthTab" >
                                <h2 class="tab"><bean:message key="drac.security.policy.bandwidthControl"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById("resGroupBandwidthTab"));</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resGroupBandwidthTable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                        	<bean:message key="drac.security.policy.bw.maxservicebandwidth"/>
                                            
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <logic:present name="editable">
                                                <b><html:text style="width: 110px" property="maximumServiceSize" styleId="maximumServiceSize" onchange="javascript:checkData(1);"/></b>
                                            </logic:present>
                                            <logic:notPresent name="editable">
                                                <b>${fn:escapeXml(ResourceGroupForm.maximumServiceSize)}</b>
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
                                                <b><html:text style="width: 110px" property="maximumServiceDuration" styleId="maximumServiceDuration" onchange="javascript:checkData(2);"/></b>
                                            </logic:present>
                                            <logic:notPresent name="editable">
                                                <b>${fn:escapeXml(ResourceGroupForm.maximumServiceDuration)}</b>
                                            </logic:notPresent>
                                            <bean:message key="drac.security.globalPolicy.seconds"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.policy.bw.maxservicesize"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <logic:present name="editable">
                                                <b><html:text style="width: 110px" property="maximumServiceBandwidth" styleId="maximumServiceBandwidth" onchange="javascript:checkData(3);"/></b>
                                            </logic:present>
                                            <logic:notPresent name="editable">
                                                <b>${fn:escapeXml(ResourceGroupForm.maximumServiceBandwidth)}</b>
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
                                                <b><html:text style="width: 110px" property="maximumAggregateServiceSize" styleId="maximumAggregateServiceSize" onchange="javascript:checkData(4);"/></b>
                                            </logic:present>
                                            <logic:notPresent name="editable">
                                                <b>${fn:escapeXml(ResourceGroupForm.maximumAggregateServiceSize)}</b>
                                            </logic:notPresent>
                                            <bean:message key="drac.text.Mb"/>
                                        </td>
                                    </tr>
<!--
                                    <tr>
                                        <td class="tbForm1" colspan="2" align="right">
                                            <img src="/images/spacer.gif" height="164">
                                        </td>
                                    </tr>
-->
                                </table>
                            </div>

<!-- later
                            <div class="tab-page" id="resourceStateControlTab" align="center">
                            <h2 class="tab"><bean:message key="drac.security.policy.accessState"/></h2>
                            <script type="text/javascript">tp2.addTabPage( document.getElementById( "resourceStateControlTab" ) );</script>
                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceStateTable">
                                <tr>
                                   <td class="tbForm1" nowrap>
                                      <b><c:out value="${ResourceGroupForm.stateRule}" /></b>
                                   </td>
                                 </tr>
                                 <tr>
                                    <td class="tbForm1">
                                        <img src="/images/spacer.gif" height="137">
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
                                    <logic:present name="editable" scope="request">
                                    <!-- Display elements if editable to this user -->
                                    <tr>
                                        <td class="tbForm1">
                                            <bean:message key="drac.security.policy.systemAccess.permission"/>
                                        </td>
                                        <td class="tbForm1">
                                            <select id="resPermission" size="1">
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
                                            <input type="text" id="resStartTime" size="11" styleClass="gen" onchange="JavaScript:rgTimeChangeListener(this);" tabindex="3" /><a href="JavaScript:void(0);" onclick="JavaScript:resStartTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
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
                                        <td class="tbForm1" nowrap>
                                            <input type="text" id="resEndTime" size="11" styleClass="gen" onchange="JavaScript:rgTimeChangeListener(this);" tabindex="3" /><a href="JavaScript:void(0);" onclick="JavaScript:resEndTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
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
                                            <select size="7" id="resAccessMonth" style="width:120px" multiple >
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
                                            <input type="button" value="<bean:message key='drac.security.policy.button.add'/>"
                                            onclick="addRule('resourceSystemAccessRules','resPermission','resAccessMonth','resDate','resWeekly','resStartTime','resEndTime','resAccessRulesTable');"/>
                                        </td>
                                    </tr>
                                    </logic:present>
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <!-- Keep this select hidden always - it holds our new rules -->
                                            <html:select size="5" property="resourceSystemAccessRules" style="display:none" styleId="resourceSystemAccessRules" errorStyleClass="invalid" multiple="true">
                                                <html:options property="resourceSystemAccessRules"/>
                                            </html:select>

                                            <!-- Display a table of the current and new rules -->
                                            <table width="100%" id="resAccessRulesTable" border="1" class="forumline">
                                                <tr>
                                                    <logic:present name="editable" scope="request">
                                                    <!-- Display element if editable to this user -->
                                                    <th><bean:message key="drac.security.policy.systemAccess.delete"/></th>
                                                    </logic:present>
                                                    <th><bean:message key="drac.security.policy.systemAccess.permission"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.startTime"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.endTime"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.onDays"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.onDates"/></th>
                                                    <th><bean:message key="drac.security.policy.systemAccess.inMonths"/></th>
                                                </tr>
    
                                                <logic:iterate id="rule" name="ResourceGroupForm" scope="request" property="resRules" indexId="cnt">
                                                <tr>
                                                    <logic:present name="editable" scope="request">
                                                    <!-- Display element if editable to this user -->
                                                    <td class="row1" align="center"
                                                        onclick="Javascript:removeRule('resAccessRulesTable',this.parentNode.rowIndex,'resourceSystemAccessRules');">
                                                        <img src="/images/delete.gif">
                                                    </td>
                                                    </logic:present>
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
<!--
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="130">
                                        </td>
-->
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
            <!-- Drop shadow -->
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

    <logic:present name="editable">
    <table align="center">
        <tr><td><img src="/images/spacer.gif" height="10"/></td></tr>
        <tr>
            <td>
                <html:submit property="Save" onclick="doSubmit()">
                    <bean:message key="drac.text.submit"/>
                </html:submit>
            </td>
            <td>
                <html:button onclick="doReset()" property="">
                    <bean:message key="drac.text.reset"/>
                </html:button>
            </td>
            <%-- permission has been checked and passed through via 'editable' bean (flag). --%>
            <td>
                <html:button onclick="return confirmDelete('${ResourceGroupForm.webSafeName}');" property="Delete" >
                    <bean:message key="drac.text.delete"/>
                </html:button>
            </td>
        </tr>

    </table>
    </logic:present>

    <html:hidden property="locale" value="<c:out value='${myLanguage}'/>"/>
    <html:hidden property="resGroupName" value="${ResourceGroupForm.name}"/>
</html:form>

<script type="text/javascript" src="/scripts/handleExpand.js"></script>

<script language="javascript">

function confirmDelete(text)
{
    var agree=confirm('<bean:message key="drac.security.resourceGroupManagement.delete.confirm"/>' + "\n" + unescape(text));
    if (agree)
    {
        // delete command is not posted via form submission, so token must be added to the URI here:
        location.href="/management/resourceGroupManagement/delete.do?rgid="+text+"&CSRFToken="+"<c:out value='${sessionScope["CSRFToken"]}'/>";
    }
    else
    {
        return false;
    }
}


function doSubmit() {
    setAllSelected(document.getElementById('resMembershipList'));
    setAllSelected(document.getElementById('membershipUserGroupsList'));
    setAllSelected(document.getElementById('resourceSystemAccessRules'));
}

function setAllSelected(elem) {
    if (elem) {
        for (var i=0; i < elem.options.length; i++)
            elem.options[i].selected = true;
    }
}

function doReset() {
    location.href="/management/resourceGroupManagement/queryResourceGroup.do?rgName="+"${ResourceGroupForm.webSafeName}";
}

function checkData(bwr) {
   var doReset = false;
   var errMsg = " does not contain a valid value.\n\nPlease enter a numeric value only.";

   if (isNaN(document.getElementById("maximumServiceSize").value)) {
      doReset = true;
      alert("Maximum Service Size" + errMsg);
   } else if (isNaN(document.getElementById("maximumServiceDuration").value)) {
      doReset = true;
      alert("Maximum Service Duration" + errMsg);
   } else if (isNaN(document.getElementById("maximumServiceBandwidth").value)) {
      doReset = true;
      alert("Maximum Service Bandwidth" + errMsg);
   } else if (isNaN(document.getElementById("maximumAggregateServiceSize").value)) {
      doReset = true;
      alert("Maximum Aggregate Service Size" + errMsg);
   }

   if (doReset) {
      resetBandwidthRuleFields(bwr);
   }
}

function resourcesListsChanged() {
    return true;
}

function checkIfDataChanged() {
   if (document.getElementById("maximumServiceSize").value == document.getElementById("maximumServiceSize").defaultValue &&
       document.getElementById("maximumServiceDuration").value == document.getElementById("maximumServiceDuration").defaultValue &&
       document.getElementById("maximumServiceBandwidth").value == document.getElementById("maximumServiceBandwidth").defaultValue &&
       document.getElementById("maximumAggregateServiceSize").value == document.getElementById("maximumAggregateServiceSize").defaultValue &&
       !resourcesListsChanged()) {
      alert("There are no changes to Save.");
      return false;
   } else {
      return true;
   }
}

function resetBandwidthRuleFields(bwr) {
   if (bwr == "all") {
      document.getElementById("maximumServiceSize").value = document.getElementById("maximumServiceSize").defaultValue;
      document.getElementById("maximumServiceDuration").value = document.getElementById("maximumServiceDuration").defaultValue;
      document.getElementById("maximumServiceBandwidth").value = document.getElementById("maximumServiceBandwidth").defaultValue;
      document.getElementById("maximumAggregateServiceSize").value = document.getElementById("maximumAggregateServiceSize").defaultValue;
   } else {
      switch ( bwr ) {
         case 1:
            document.getElementById("maximumServiceSize").value = document.getElementById("maximumServiceSize").defaultValue;
            break;
         case 2:
            document.getElementById("maximumServiceDuration").value = document.getElementById("maximumServiceDuration").defaultValue;
            break;
         case 3:
            document.getElementById("maximumServiceBandwidth").value = document.getElementById("maximumServiceBandwidth").defaultValue;
            break;
         case 4:
            document.getElementById("maximumAggregateServiceSize").value = document.getElementById("maximumAggregateServiceSize").defaultValue;
            break;
      }
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
                fromElem.options[i] = null;
            }
        }

        // Make sure all options are selected, src and dest lists.
        // This is needed so that when swapping entries from one list to another and vice versa
        // the items need to be selected to get saved on the server side.
        for (var i=fromElem.options.length-1; i >= 0; i--) {
            fromElem.options[i].selected = true;
        }
        for (var i=toElem.options.length-1; i >= 0; i--) {
            toElem.options[i].selected = true;
        }
    }
}

function viewResource(elemId) {
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.open("/management/resourceGroupManagement/queryResource.do?res=" + escape(elem.options[elem.selectedIndex].value));
    }
    return false;
}

function viewUserGroup(elemId) {
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.open("/management/userGroupManagement/queryUserGroup.do?ugName=" + escape(elem.options[elem.selectedIndex].value));
    }
    return false;
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
            selectAllDates(false, 'resDate'); // reset all dates.
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
} /* rgTimeChangeListener */

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

</script>

<%@ include file="/common/footer.jsp"%>
