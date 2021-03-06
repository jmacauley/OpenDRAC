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
 * OpenDRAC WEB GUI version 0.1 Beta
 *
 * File: /help/en/listSecurityAuditLogsResults.jsp
 *
 * Description:
 *   This page displays US English help data for the list Security Audit Logs functionality
 *
 ****************************************************************************/
%>

<span class="gen">
	<p>
		<i><b>"<c:out value='${sessionScope["drac.security.auditlogs.results"]}'/>"</b></i> lists all the security audit logs created within the duration specified in the list Audit logs filter.
	<ul>
		<li>
			<i>"<c:out value='${sessionScope["drac.text.entry"]}'/>"</i> lists the number of security audit logs matching the provided query filter.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.auditlogs.list.auditId"]}'/>"</i> is a  unique ID generated for the audit
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.auditlogs.list.userID"]}'/>"</i> is the login ID used for this security operation
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.auditlogs.list.group"]}'/>"</i> specifies the group that the user belongs to
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.auditlogs.list.billinggroup"]}'/>"</i> specifies the billing group that the user belongs to
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.auditlogs.list.operation"]}'/>"</i> represents the opearation performed by the user.Possible values being, login, logout, login failure, logout failure, user added, user deleted and user properties modified.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.auditlogs.list.occuredtime"]}'/>"</i> specifies the time at which the log has been generated
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.auditlogs.security.list.securitydata"]}'/>"</i> provides the details of the audit .This invloves the IP address of the machine where the user logs in.
		</li>
		<br />
		
	</ul>
	</p> </span>
