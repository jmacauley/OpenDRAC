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
<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%
String pageRef = "drac.service.add.list";
%>

<%@ include file="/common/header_struts.jsp"%>
<html:form action="/service/addServiceList" method="POST">
    <table width="50%" cellspacing="5" cellpadding="0" border="0"
        align="center">
        <tr>
            <td>
                <img src="/images/spacer.gif" height="5" />
            </td>
        </tr>
        <tr>
            <td class="gen">
                <logic:empty name="scheduleList" scope="request">
                    <div align="center"><bean:message key="drac.service.add.list.none" /></div>
                </logic:empty>
                <logic:notEmpty name="scheduleList" scope="request">
                    <% String startDateTitle = (String) session.getAttribute("drac.schedule.startdate") + (String) request.getAttribute("TZString");
                       String endDateTitle = (String) session.getAttribute("drac.schedule.enddate") + (String) request.getAttribute("TZString"); %>
                    <div align="center"><bean:message key="drac.service.add.list.message" /></div>
                    <display:table id="schedule" name="scheduleList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" requestURI="" sort="list">
                        <c:set var="tdclass" value="row1" />
                        <c:if test="${schedule_rowNum % 2 == 0}">
                            <c:set var="tdclass" value="row2" />
                        </c:if>
                        <display:column title="${sessionScope['drac.service.add.button']}" class="${tdclass}c">
                        <a href="/service/addService.do?sid=${schedule.id}"><img src="/images/plus.gif" align="absmiddle"> </a></display:column>
                        <display:column escapeXml="true" property="name" title="${sessionScope['drac.schedule.name']}" sortable ="true" class="${tdclass}Left"
                        href="/schedule/querySchedule.do" paramId="sid" paramProperty="id"></display:column>
                        <c:choose>
                            <c:when test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                <display:column escapeXml="true" property="userId" title="${sessionScope['drac.schedule.detail.userID']}" sortable ="true" class="${tdclass}"
                                    href="/management/userManagement/editUser.do" paramId="uid" paramProperty="userId"/>
                            </c:when>
                            <c:otherwise/>
                        </c:choose>
                        <display:column property="rate" title="${sessionScope['drac.schedule.rate']}" class="${tdclass}"></display:column>
                        <display:column property="startDate" title="<c:out value='${startDateTitle}'/>" sortable="true" sortProperty="startTimeMillis" class="${tdclass}"></display:column>
                        <display:column property="endDate" title="<c:out value='${endDateTitle}'/>" sortable="true" sortProperty="endTimeMillis" class="${tdclass}"></display:column>
                        <display:column escapeXml="true" property="srcTNA" title="${sessionScope['drac.schedule.sourcePort']}" class="${tdclass}"></display:column>
                        <display:column escapeXml="true" property="destTNA" title="${sessionScope['drac.schedule.destPort']}" class="${tdclass}"></display:column>
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
