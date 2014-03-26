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

<%@ page errorPage="/common/dracError.jsp"%>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /help/en/listUsersResult.jsp
 *
 * Description:
 *   This page displays French help data for the list users
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
	<p>
		<i><b>"<c:out value='${sessionScope["drac.schedule.list.results"]}'/>"</b></i> lists all the schedules created within the duration specified in the list Schedule filter. Hyperlinks exist to "
		<c:out value='${sessionScope["drac.schedule.detail"]}'/>
		", and "
		<c:out value='${sessionScope["drac.schedule.delete"]}'/>
		" functions.
	<ul>
		<li>
			<i>"<c:out value='${sessionScope["drac.text.entry"]}'/>"</i> lists the number of schedules matching the provided query filter.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.list.results.delete"]}'/>"</i> is a context sensitive hyperlink to the "Delete Schedule" function.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.list.results.status"]}'/>"</i> an indication of the status of the schedule.The newly created schedule is "Active".When all the services within a schedule expires, the status of the schedule turns
			"Inactive".
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.list.results.scheduleID"]}'/>"</i> specifies the unique ID generated for the schedule.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.list.results.name"]}'/>"</i> specifies the user assigned schedule name.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.list.results.recurring"]}'/>"</i> indicates if this is a recurring schedule. Recurring schedule details can be found on the <i><b>"Schedule Details"</b></i> page by selecting the hyperlink associated
			with the schedule ID.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.rate"]}'/>"</i> specifies the rate of service requested in
			<c:out value='${sessionScope["drac.text.Mbps"]}'/>
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.sourceTNA"]}'/>"</i> specifies the source endpoint of the service.
		</li>
		<br />
		<li>
			<i>"<c:out value='${sessionScope["drac.schedule.destinationTNA"]}'/>"</i> specifies the destination endpoint of the service.
		</li>
	</ul>
	</p> </span>
