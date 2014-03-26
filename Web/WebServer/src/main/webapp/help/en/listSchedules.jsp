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
/* OpenDRAC Web GUI */
%>

<span class="gen">
	<p>
		<i><b>"<c:out value='${sessionScope["drac.schedule.list"]}'/>"</b></i> provides information on schedules created within the network. The <i>"Schedule Filter</i>" functionality provides a mechanism to view all schedules created/accessible within your
		user profile matching the supplied criteria.
	</p>
	<ul>
		<li>
			"<i>Member Group</i>" provides the ability to filter based on a specific user group in your user profile. Specifying the "All Member Groups" option will return a list of all schedules accessible from your profile.
		</li>
		<br />
		<li>
			"<i>Start Date</i>" specifies the start date for the query.
		</li>
		<br />
		<li>
			"<i>End Date</i>" specifies the start date for the query.
		</li>
	</ul>
	<p>
		The results of this query will provide schedules matching the specified filter.
	</p> </span>
