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
 * File: /help/en/changePassword.jsp
 *
 * Description:
 *   This page displays US English help data for the change password
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.changePassword.title"]}'/></b>
        allows the user to change their password.
    </p>
    <ul>
        <li>
            "<i><c:out value='${sessionScope["drac.security.changePassword.old"]}'/></i>"
            - The user's current password.
        </li>
        <br />
        <li>
            "<i><c:out value='${sessionScope["drac.security.changePassword.new1"]}'/></i>"
            - The user's new password.
        </li>
        <br />
        <li>
            "<i><c:out value='${sessionScope["drac.security.changePassword.new2"]}'/></i>"
            - A repeat of the user's new password by way of confirmation. Must match the <i><c:out value='${sessionScope["drac.security.changePassword.new1"]}'/></i> exactly.
        </li>
    </ul>
<p>
Note that any password rules currently in effect (via global or local account policy settings) will be enforced when changing the password.</p>
</span>
