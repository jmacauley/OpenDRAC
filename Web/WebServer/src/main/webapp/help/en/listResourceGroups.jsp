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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page errorPage="/common/dracError.jsp"%>

<%
/****************************************************************************
 * OpenDRAC WEB GUI version 1.0
 *
 * File: /help/en/listResourceGroups.jsp
 *
 * Description:
 *   This page displays US English help data for the list resource groups
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.resourceGroupManagement.list"]}'/></b>
        provides information on all <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>s
        available within the system. The "<c:out value='${sessionScope["drac.security.list.searchCriteria"]}'/>"
        functionality provides a mechanism to filter the <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>s
        returned by the query.
    </p>
    <ul>
        <li>
            "<i><c:out value='${sessionScope["drac.security.list.searchBy"]}'/></i>"
            - The attribute by which the query is formed.
            Defaults to "<c:out value='${sessionScope["drac.security.resourceGroupManagement.option.all"]}'/>"
            meaning all <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>s
            will be returned by the query.
        </li>
        <br />
        <li>
            "<i><c:out value='${sessionScope["drac.security.list.searchFor"]}'/></i>" allows the user to specify a string for the query.
            This field is only available if the "<c:out value='${sessionScope["drac.security.list.searchBy"]}'/>"
            field has changed from its default value.
        </li>
    </ul>
    <p>
        The result of this query will be a list of
        <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>s matching the specified filter.
    </p>
</span>
