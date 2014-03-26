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
 * File: /help/en/listResourceGroupsResult.jsp
 *
 * Description:
 *   This page displays US English help data for the list resource groups result
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.resourceGroupManagement.list.results"]}'/></b>
        lists all the resource groups currently in the system that teh curently logged in user is allowed to see.
    <ul>
        <li>
            "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.list.results.delete"]}'/></i>"
            - An icon to delete the resource group entity. If the logged in user has the right to delete resource groups this icon will be activated, otherwise it will be greyed-out (inactive).
        </li>
        <br />
        <li>
            "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.list.results.name"]}'/></i>"
            - The unique name which represents a particular Resource Group.
            Click the hyperlinked <c:out value='${sessionScope["drac.security.resourceGroupManagement.list.results.name"]}'/>
             to display all details for that resource group.
        </li>
        <br />
    </ul>
    </p>
</span>
