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
 * File: /help/en/listUserGroupsResult.jsp
 *
 * Description:
 *   This page displays US English help data for the list user groups result
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.userGroupManagement.list.results"]}'/></b>
        lists all the User Groups currently defined in the system the logged in user is allowed to view.
    <ul>
        <li>
            "<i><c:out value='${sessionScope["drac.security.userGroupManagement.list.results.delete"]}'/></i>"
            - An icon to delete the user group entity. If the logged in user has the right to delete user groups this icon will be activated, otherwise it will be greyed-out (inactive).
        </li>
        <br />
        <li>
            "<i><c:out value='${sessionScope["drac.security.userGroupManagement.list.results.name"]}'/></i>"
            - The name which represents a particular User Group.
            Click the hyperlinked <c:out value='${sessionScope["drac.security.userGroupManagement.list.results.name"]}'/>
             to display all details for that user group.
        </li>
        
    </ul>
    </p>
</span>
