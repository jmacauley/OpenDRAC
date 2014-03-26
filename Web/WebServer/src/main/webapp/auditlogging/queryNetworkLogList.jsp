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

<%String pageRef = "drac.network.auditlogs.results";%>

<%@ include file="/common/header_struts.jsp"%>

<table width="98%" cellspacing="5" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align="center">
        <logic:empty name="netLogList" scope="request">
            <bean:message key="drac.auditlogs.empty"/>
        </logic:empty>
        <logic:notEmpty name="netLogList" scope="request">
        <display:table id="log" name="netLogList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" sort="list" requestURI="">
            <c:set var="tdclass" value="row1c" />
            <c:if test="${log_rowNum % 2 == 0}">
                <c:set var="tdclass" value="row2c" />
            </c:if>     
            <display:column property="serialNo" title="<c:out value='${sessionScope["drac.auditlogs.list.serialno"]}'/>" sortable ="true" class="${tdclass}" /> 
            <display:column property="auditId" title="<c:out value='${sessionScope["drac.auditlogs.list.auditId"]}'/>" class="${tdclass}"/> 
            <display:column property="userid" sortable="true" title="<c:out value='${sessionScope["drac.auditlogs.list.userID"]}'/>" class="${tdclass}"/> 
            <display:column property="group" title="<c:out value='${sessionScope["drac.auditlogs.list.group"]}'/>" class="${tdclass}"/> 
            <display:column property="billingGroup" title="<c:out value='${sessionScope["drac.auditlogs.list.billinggroup"]}'/>" class="${tdclass}"/> 
            <display:column property="operation" title="<c:out value='${sessionScope["drac.auditlogs.list.operation"]}'/>"  sortable="true" class="${tdclass}"/> 
            <display:column property="time" title="<c:out value='${sessionScope["drac.auditlogs.list.occuredtime"]}'/>"  class="${tdclass}"/> 
            <display:column property="data" title="<c:out value='${sessionScope["drac.auditlogs.security.list.networkdata"]}'" class="${tdclass}"/>
       </display:table>
       </logic:notEmpty>    
       </td>
    </tr>
</table>        

<%@ include file="/common/footer.jsp"%>
