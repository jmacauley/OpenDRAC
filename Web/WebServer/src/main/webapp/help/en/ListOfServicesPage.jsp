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

<%/****************************************************************************
			 * OpenDRAC Web GUI
			 *
			 * File: /help/en/ListOfServicesPage.jsp
			 *
			 * Description:
			 *   This page displays US English help data for the list services
			 *   funcationality.
			 *
			 ****************************************************************************/
%>

<span class="gen">
	<p>
		<i><b>"<c:out value='${sessionScope["drac.service.list.results"]}'/>"</b></i> lists all the services created within the duration specified in the list Service filter. Hyperlinks exist to "
		<c:out value='${sessionScope["drac.service.list.results.serviceID"]}'/>
		", and "
		<c:out value='${sessionScope["drac.service.list.results.delete"]}'/>
		" functions.
	<ul>

		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.service.list.results.delete"]}'/>"</i> is a context sensitive hyperlink to the "Delete Service" function.
		</li>
		<br />

		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.service.list.results.serviceID"]}'/>"</i> specifies the unique ID of the service.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.service.list.results.scheduleName"]}'/>"</i> specifies the user assigned schedule name of the service.
		</li>
		<br />

	</ul>
	</p> </span>
