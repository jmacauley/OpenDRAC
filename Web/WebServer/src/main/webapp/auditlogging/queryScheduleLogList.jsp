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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page language="java" contentType="text/html; charset=iso-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<%@ page session="true" errorPage="/common/dracError.jsp" %>

<%String pageRef = "drac.schedule.auditlogs.results";%>

<%@ include file="/common/header_struts.jsp"%>

<table width="98%" cellspacing="5" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align="center">
        <logic:empty name="schLogList" scope="request">
            <bean:message key="drac.auditlogs.empty"/>
        </logic:empty>
        <logic:notEmpty name="schLogList" scope="request">
        <display:table id="log" name="schLogList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" sort="list" requestURI="">
            <c:set var="tdclass" value="row1c" />
            <c:if test="${log_rowNum % 2 == 0}">
                <c:set var="tdclass" value="row2c" />
            </c:if>
            <display:column property="time" sortable="true" sortProperty="occurTime" title='<c:out value='${sessionScope["drac.auditlogs.list.time"]}'/><c:out value='${sessionScope["TZString"]}'/>' class="${tdclass}"> </display:column>
            <display:column property="userid" sortable="true" title='<c:out value='${sessionScope["drac.auditlogs.list.userID"]}'/>' class="${tdclass}"> </display:column>
            <display:column property="address" sortable="true" title='<c:out value='${sessionScope["drac.auditlogs.list.address"]}'/>' class="${tdclass}"> </display:column>
            <display:column property="billingGroup" sortable="true" title='<c:out value='${sessionScope["drac.auditlogs.list.billinggroup"]}'/>' class="${tdclass}"> </display:column>
            <display:column property="severity" sortable="true" title='<c:out value='${sessionScope["drac.auditlogs.list.severity"]}'/>' class="${tdclass}"> </display:column>
            <display:column property="type" sortable="true" title='<c:out value='${sessionScope["drac.auditlogs.list.type"]}'/>' class="${tdclass}"> </display:column>
            <display:column property="resource" href="/schedule/querySchedule.do" paramId="sid" paramProperty="resource" sortable="true" title='<c:out value='${sessionScope["drac.auditlogs.list.resource"]}'/>' class="${tdclass}"> </display:column>
            <display:column property="description" title='<c:out value='${sessionScope["drac.auditlogs.list.desc"]}'/>' class="${tdclass}"> </display:column>
        </display:table>
      </logic:notEmpty>
      </td>
    </tr
</table>


<%@ include file="/common/footer.jsp"%>

