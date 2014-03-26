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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%
String pageRef = "drac.service.alarms.list";
%>


<%@ include file="/common/header_struts.jsp"%>
<html:form action="/service/listAlarmsAction" method="POST">
<table width="50%" cellspacing="5" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <logic:notEmpty name="filterFrom" scope="request">
    <tr>
        <td nowrap align='center' class="gen">
        <bean:message key="drac.result.filter" arg0="${fn:escapeXml(requestScope['filterFrom'])}" arg1="${fn:escapeXml(requestScope['filterTo'])}"/>
        </td>
    </tr>
    </logic:notEmpty>
    <tr>
        <td nowrap align='center' class="gen">
            <logic:empty name="alarmList" scope="request">
                <bean:message key="drac.service.alarms.results.notavailable" />
            </logic:empty>
            <logic:notEmpty name="alarmList" scope="request">
            <% String occurTimeTitle = (String) session.getAttribute("drac.service.alarms.time") + (String) request.getAttribute("TZString");
               String clearTimeTitle = (String) session.getAttribute("drac.service.alarms.cleared") + (String) request.getAttribute("TZString"); %>
            <display:table id="alarm" name="alarmList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" requestURI="" sort="list">
                <c:set var="tdclass" value="row1c" />
                <c:if test="${alarm_rowNum % 2 == 0}">
                    <c:set var="tdclass" value="row2c" />
                </c:if>
                <display:column property="result" title="${sessionScope['drac.auditlogs.list.serialno']}" sortable ="true" class="${tdclass}"> </display:column>
                <display:column property="description" title="${sessionScope['drac.service.alarms.desc']}" sortable="true" class="${tdclass}"/>
                <display:column title="${sessionScope['drac.service.alarms.severity']}" sortable="true" class="${tdclass}">
                    <c:choose>
                        <c:when test="${alarm.severity eq 'CR'}">
                            <b><font color="red"><bean:message key="drac.service.alarms.severity.cr"/></font></b>
                        </c:when>
                        <c:when test="${alarm.severity eq 'MJ'}">
                            <font color="red"><bean:message key="drac.service.alarms.severity.mj"/></font>
                        </c:when>
                        <c:when test="${alarm.severity eq 'MN'}">
                            <font color="orange"><bean:message key="drac.service.alarms.severity.mn"/></font>
                        </c:when>
                        <c:when test="${alarm.severity eq 'CL'}">
                            <bean:message key="drac.service.alarms.severity.cl"/>
                        </c:when>
                        <c:otherwise>
                            ${alarm.severity}
                        </c:otherwise>
                    </c:choose>
                </display:column>
                <display:column property="occurredTime" title="<c:out value='${occurTimeTitle}'/>" sortable="true" sortProperty="occurredTimeMillis" class="${tdclass}"/>
                <display:column title="<c:out value='${clearTimeTitle}'/>" sortable="true" sortProperty="clearedTimeMillis" class="${tdclass}">
                    <c:choose>
                        <c:when test="${empty alarm.clearedTime}">
                            ${sessionScope['drac.service.alarms.notCleared']}
                        </c:when>
                        <c:otherwise>
                            ${alarm.clearedTime}
                        </c:otherwise>
                    </c:choose>
                </display:column>
                <display:column title="${sessionScope['drac.service.alarms.duration']}" sortable="true" sortProperty="duration" class="${tdclass}">
                    <c:choose>
                        <c:when test="${alarm.duration == 0}">
                            --
                        </c:when>
                        <c:otherwise>
                            ${alarm.duration}
                        </c:otherwise>
                    </c:choose>
                </display:column>
                <display:column property="serviceId" title="${sessionScope['drac.service.alarms.service']}" sortable ="true" class="${tdclass}"
                    href="/service/serviceDetails.do" paramId="sid" paramProperty="serviceId"/>
                <display:column escapeXml="true" property="scheduleName" title="${sessionScope['drac.service.alarms.scheduleName']}" sortable ="true" class="${tdclass}"
                    href="/schedule/querySchedule.do" paramId="sid" paramProperty="scheduleId" />

            </display:table>
            </logic:notEmpty>
        </td>
    </tr>
    <tr>
        <td>
            <img src="/images/spacer.gif" height="10" />
        </td>
    </tr>
</table>
</html:form>

<%@ include file="/common/footer.jsp"%>
