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
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
 /****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /schedule/listSchedulesResult.jsp
 *
 * Description:
 *   This page diaplays the results of a list schedules query.
 *
 ****************************************************************************/

String pageRef = "drac.schedule.list.results";
%>

<%@ include file="/common/header_struts.jsp"%>

<html:form action="/schedule/listSchedulesResult.do" method="POST">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>

<html:hidden property="startdate" value="${requestScope['filterFrom']}"/>
<html:hidden property="enddate" value="${requestScope['filterTo']}"/>
<html:hidden property="group" value="${requestScope['filterGroup']}"/>

<table width="98%" cellspacing="5" cellpadding="0" border="0" align="center" style="position:static!important;position:relative">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>

    <tr>
        <td align='center' class="gen">
        <bean:message key="drac.result.filter" arg0="${fn:escapeXml(requestScope['filterFrom'])}" arg1="${fn:escapeXml(requestScope['filterTo'])}"/><br>
        <bean:message key="drac.schedule.list.filter.group.title"/>&nbsp;
            <c:if test="${requestScope['filterGroup'] eq 'all'}">
                <i><bean:message key="drac.schedule.list.filter.group.all"/></i>
            </c:if>
            <c:if test="${requestScope['filterGroup'] ne 'all'}"><i>${fn:escapeXml(requestScope['filterGroup'])}</i></c:if>

        </td>
    </tr>
    <logic:messagesPresent message="true">
        <html:messages id="message" message="true" property="errors">
        <tr>
            <td align="center" class="gen">
                <font color="red"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
        <html:messages id="message" message="true" property="success">
        <tr>
            <td align="center" class="gen">
                <font color="green"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
    </logic:messagesPresent>
    <tr>
        <td align='center' class="gen" colspan="2">
            <logic:empty name="scheduleList" scope="request">
                        <bean:message key="drac.schedule.list.results.notavailable" />
            </logic:empty>
            <logic:notEmpty name="scheduleList" scope="request">
            <% String startDateTitle = (String) session.getAttribute("drac.schedule.startdate") + (String) request.getAttribute("TZString");
               String endDateTitle = (String) session.getAttribute("drac.schedule.enddate") + (String) request.getAttribute("TZString"); %>

            <display:table id="sched" name="scheduleList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" requestURI="" sort="list">
                <c:set var="tdclass" value="row1" />
                <c:if test="${sched_rowNum % 2 == 0}">
                    <c:set var="tdclass" value="row2" />
                </c:if>
                <display:column title="<input class=\"smallbox\" type=\"checkbox\" size=\"1px\" name=\"headerCB\" onClick=\"javascript:checkAllBoxes(this);\"/>" class="${tdclass}c">
                    <c:choose>
                        <c:when test="${!sched.confirmable && !sched.cancellable}">
                            <input type="checkbox" disabled="true" class="smallbox" name="selectedItems" value="${sched.id}" onclick="Javascript:uncheckHeaderBox();" />
                        </c:when>
                        <c:otherwise>
                            <input type="checkbox" class="smallbox" name="selectedItems" value="${sched.id}" onclick="Javascript:uncheckHeaderBox();" />
                        </c:otherwise>
                    </c:choose>
                </display:column>

                <display:column  title="${sessionScope['drac.text.action']}" class="${tdclass}c">
                    <c:if test="${sched.confirmable}">
                    	<a href="javascript:doConfirm('${sched.id}');" title="Confirm schedule" alt="Confirm schedule">
                        	<img src="/images/confirm.gif" align="absmiddle" border="0"/>
                        </a>
                    </c:if>
                    <c:if test="${!sched.confirmable}">
                        <img src="/images/no-confirm.gif" align="absmiddle">
                    </c:if>
                    <c:if test="${sched.cancellable}">
                    	<a href="javascript:doCancel('${sched.id}','${sched.webSafeName}');" title="Cancel schedule" alt="Cancel schedule">
                        	<img src="/images/delete.gif" align="absmiddle" border="0"/>
                        </a>
                    </c:if>
                    <c:if test="${!sched.cancellable}">
                        <img src="/images/no-delete.gif" align="absmiddle">
                    </c:if>
                </display:column>
                <display:column escapeXml="true" property="name" title="${sessionScope['drac.schedule.list.results.name']}" sortable ="true" class="${tdclass}Left"
                        href="/schedule/querySchedule.do" paramId="sid" paramProperty="id"/>
                <c:choose>
                    <c:when test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                        <display:column escapeXml="true" property="userId" title="${sessionScope['drac.schedule.detail.userID']}" sortable ="true" class="${tdclass}Left"
                            href="/management/userManagement/editUser.do" paramId="uid" paramProperty="userId"/>
                    </c:when>
                    <c:otherwise>
                        <display:column escapeXml="true" property="userId" title="${sessionScope['drac.schedule.detail.userID']}" sortable ="true" class="${tdclass}Left"/>
                    </c:otherwise>
                </c:choose>
                <display:column title="<%=startDateTitle%>" property="startDate" sortable="true" sortProperty="startTimeMillis" class="${tdclass}Left"/>
                <display:column title="<%=endDateTitle%>" property="endDate" sortProperty="endTimeMillis" sortable="true" class="${tdclass}Left"/>
                <display:column title="${sessionScope['drac.schedule.list.results.status']}" sortable="true" class="${tdclass}Left">
                    <bean:message key="drac.reservation.status.${sched.status}"/>
                </display:column>
                <display:column title="${sessionScope['drac.schedule.rate']}" sortable="true" class="${tdclass}Left">
                    <div align="right">${sched.rate}&nbsp;<bean:message key="drac.text.Mbps" /></div>
                </display:column>
                <display:column title="${sessionScope['drac.schedule.list.results.recurring']}" sortable="true" class="${tdclass}c">
                    <c:choose>
                        <c:when test="${sched.recurrence != false}">
                            <bean:message key="drac.text.yes" />
                        </c:when>
                        <c:otherwise>
                            <bean:message key="drac.text.no" />
                        </c:otherwise>
                    </c:choose>
                </display:column>
                <display:footer>
                    <tr>
                        <td colspan="10">
                            <input type="submit" class="gen" onclick="return displayWarning('confirm');" value="<bean:message key='drac.schedule.list.results.confirm.selected'/>"/>&nbsp;
                            <input type="submit" class="gen" onclick="return displayWarning('cancel');" value="<bean:message key='drac.schedule.list.results.delete.selected'/>"/>
                        </td>
                    </tr>
                </display:footer>
            </display:table>
            </logic:notEmpty>
        </td>
    </tr>
    <%@ include file="/common/loading.jsp"%>
    <tr>
        <td>
            <img src="/images/spacer.gif" height="10" />
        </td>
    </tr>
</table>

<html:hidden property="command" styleId="command" value=""/>
<html:hidden property="selectedItem" styleId="selectedItem" value=""/>

<script LANGUAGE="JavaScript">

function doCancel(scheduleId, scheduleName) {
    if (confirmDelete(scheduleName)) {
        document.ListSchedulesForm.command.value = "cancel";
        document.ListSchedulesForm.selectedItem.value = scheduleId;
        showLoading(true);
        document.ListSchedulesForm.submit();
    }
}

function doConfirm(scheduleId) {
    document.ListSchedulesForm.command.value = "confirm";
    document.ListSchedulesForm.selectedItem.value = scheduleId;
    showLoading(true);
    document.ListSchedulesForm.submit();
}

function confirmDelete(text)
{
    var agree=confirm('<bean:message key="drac.schedule.delete.confirm"/>' + "\n" + unescape(text));
    if (agree) {
        return true;
    } else {
        return false;
    }
}

function displayWarning(action) {
    //comboBox = document.getElementById("actionBox");
    //if (comboBox.options[comboBox.selectedIndex].value == "cancel") {
    if (action == "cancel") {
        var agree=confirm('<bean:message key="drac.schedule.delete.confirm.multiple"/>');
        if (agree) {
            document.ListSchedulesForm.command.value = "cancelAll";
            showLoading(true);
            return true;
        } else {
            return false;
        }
    } else {
        document.ListSchedulesForm.command.value = "confirmAll";
        showLoading(true);
        return true;
    }
}



function checkAllBoxes(headerCheckBox)
{
    var checkBoxes = document.getElementsByName('selectedItems');
    for ( i = 0; i < checkBoxes.length; i++)
        if (checkBoxes[i].disabled == false) {
            checkBoxes[i].checked = headerCheckBox.checked;
        }
}

function uncheckHeaderBox()
{
    document.getElementsByName("headerCB")[0].checked = false;
}
</script>
</html:form>



<%@ include file="/common/footer.jsp"%>
