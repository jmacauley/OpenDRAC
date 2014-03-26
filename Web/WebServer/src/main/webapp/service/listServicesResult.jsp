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

<%@ page language="java" contentType="text/html; charset=iso-8859-1"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%
String pageRef = "drac.service.list.results";
%>


<%@ include file="/common/header_struts.jsp"%>
<html:form action="/service/listServicesResult" method="POST">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
<html:hidden property="startdate" value="${requestScope['filterFrom']}"/>
<html:hidden property="enddate" value="${requestScope['filterTo']}"/>
<html:hidden property="memberGroup" value="${requestScope['filterGroup']}"/>

<table width="50%" cellspacing="5" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align='center' class="gen">
        <bean:message key="drac.result.filter" arg0="${fn:escapeXml(requestScope['filterFrom'])}"  arg1="${fn:escapeXml(requestScope['filterTo'])}"/><br>
        <bean:message key="drac.service.list.filter.group.title"/>&nbsp;
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
        <td align='center' class="gen">
            <logic:empty name="serviceList" scope="request">
                        <bean:message key="drac.service.list.results.notavailable" />
            </logic:empty>
            <logic:notEmpty name="serviceList" scope="request">
            <% String startDateTitle = (String) session.getAttribute("drac.service.list.results.startTime") + (String) request.getAttribute("TZString");
               String endDateTitle = (String) session.getAttribute("drac.service.list.results.endTime") + (String) request.getAttribute("TZString"); %>
            <display:table id="service" name="serviceList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" requestURI="" sort="list">
                <c:set var="tdclass" value="row1" />
                <c:if test="${service_rowNum % 2 == 0}">
                    <c:set var="tdclass" value="row2" />
                </c:if>
                <display:column title="<input class=\"smallbox\" type=\"checkbox\" size=\"1px\" name=\"headerCB\" onClick=\"javascript:checkAllBoxes(this);\"/>" class="${tdclass}c">
                    <c:choose>
                        <c:when test="${!service.activateable && !service.cancellable}">
                            <input type="checkbox" disabled="true" class="smallbox" name="selectedItems" value="${service.serviceID}" onclick="Javascript:uncheckHeaderBox();" />
                        </c:when>
                        <c:otherwise>
                            <input type="checkbox" class="smallbox" name="selectedItems" value="${service.serviceID}" onclick="Javascript:uncheckHeaderBox();" />
                        </c:otherwise>
                    </c:choose>
                </display:column>

                <display:column  title="${sessionScope['drac.text.action']}" class="${tdclass}c">
                    <c:if test="${service.activateable}">
                    	<a href="javascript:doActivate('${service.serviceID}');" title="Cancel service" title="Cancel service">
                       		<img src="/images/confirm.gif" align="absmiddle" border="0"/>
                  		</a>
                    </c:if>
                    <c:if test="${!service.activateable}">
                        <img src="/images/no-confirm.gif" align="absmiddle">
                    </c:if>
                    <c:if test="${service.cancellable}">
                    	<a href="javascript:doCancel('${service.serviceID}');" title="Cancel service" title="Cancel service">
                        	<img src="/images/delete.gif" align="absmiddle" border="0"/>
                        </a>
                    </c:if>
                    <c:if test="${!service.cancellable}">
                        <img src="/images/no-delete.gif" align="absmiddle">
                    </c:if>
                </display:column>
                <display:column property="serviceID" title="${sessionScope['drac.service.list.results.serviceID']}" sortable ="true" class="${tdclass}Left"
                    href="/service/serviceDetails.do" paramId="sid" paramProperty="serviceID">
                </display:column>
                <display:column title="<c:out value='${startDateTitle}'/>" property="startDateForList" sortable="true" sortProperty="startTimeMillis" class="${tdclass}Left"/>
                <display:column title="<c:out value='${endDateTitle}'/>" property="endDateForList" sortProperty="endTimeMillis" sortable="true" class="${tdclass}Left"/>
                <display:column title="${sessionScope['drac.service.list.results.status']}" sortable ="true" class="${tdclass}Left">
                    <bean:message key="drac.reservation.status.${service.status}"/>
                </display:column>
                <display:column escapeXml="true" property="scheduleName" title="${sessionScope['drac.service.list.results.scheduleName']}" sortable ="true" class="${tdclass}Left"
                    href="/schedule/querySchedule.do" paramId="sid" paramProperty="scheduleId"></display:column>
                <c:choose>
                    <c:when test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                        <display:column escapeXml="true" property="userName" title="${sessionScope['drac.schedule.detail.userID']}" sortable ="true" class="${tdclass}Left"
                            href="/management/userManagement/editUser.do" paramId="uid" paramProperty="userName" />
                    </c:when>
                    <c:otherwise>
                        <display:column property="userName" title="${sessionScope['drac.schedule.detail.userID']}" sortable ="true" class="${tdclass}Left" />
                    </c:otherwise>
                </c:choose>

                <display:footer>
                    <tr>
                        <td colspan="9">
                            <input type="submit" class="gen" onclick="return displayWarning('confirm');" value="<bean:message key="drac.service.list.results.activate.selected"/>"/>&nbsp;
                            <input type="submit" class="gen" onclick="return displayWarning('cancel');" value="<bean:message key='drac.service.list.results.delete.selected'/>"/>
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

function doCancel(serviceId) {
    if (confirmDelete(serviceId)) {
        document.ListServicesForm.command.value = "cancel";
        document.ListServicesForm.selectedItem.value = serviceId;
        showLoading(true);
        document.ListServicesForm.submit();
    }
}

function doActivate(serviceId) {
    document.ListServicesForm.command.value = "confirm";
    document.ListServicesForm.selectedItem.value = serviceId;
    showLoading(true);
    document.ListServicesForm.submit();
}

function confirmDelete(text)
{
var agree=confirm('<bean:message key="drac.service.delete.confirm"/>' + "\n" + text);
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
        var agree=confirm('<bean:message key="drac.service.delete.confirm.multiple"/>');
        if (agree) {
            document.ListServicesForm.command.value = "cancelAll";
            showLoading(true);
            return true;
        } else {
            return false;
        }
    } else {
        document.ListServicesForm.command.value = "activateAll";
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
