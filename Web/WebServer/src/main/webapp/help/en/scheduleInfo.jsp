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
/* OpenDRAC Web GUI */
%>

<span class="gen">
	<p>
		<i>"Schedule Details"</i> provides the detailed description of the schedule specified.
	<ul>
		<li>
			"<i>Schedule Name</i>" refers to the name of the schedule provided by the user.
		</li>
		<br />
		<li>
			"<i>Source TNA</i>" refers to the source end point of the schedule.
		</li>
		<br />
		<li>
			"<i>Destination TNA</i>" refers to the destination end point of the schedule.
		</li>
		<br />
		<li>
			"<i>Start Date</i>" indicates the start date of the schedule
		</li>
		<br />
		<li>
			"<i>End Date</i>" indicates the end date of the schedule
		</li>
		<br />
		<li>
			"<i>Rate</i>" identifies the rate of the schedule
		</li>
		<br />
	</ul>
	</p>
	<p>
		<i>"Recurrence Details"</i> provides the information of the recurring schedules.
	<ul>
		<li>
			"<i>Type</i>" could be daily, weekly, monthly and yearly
		</li>
		<br />
		<li>
			"<i>Day</i>" could be any day between 1 and 31
		</li>
		<br />
		<li>
			"<i>Month</i>" could be any month in the year
		</li>
		<br />
		<li>
			"<i>WeekDays</i>" refers to the day in the week that services are provisioned
		</li>
		<br />
	</ul>
	</p>

	<p>
		<i>"Service Details"</i> provides the information of the services provisioned.
	<ul>
		<li>
			"<i>Service ID</i>" refers to the unique service ID generated within the schedule
		</li>
		<br />
		<li>
			"<i>Status</i>" is "Active" for a newly created service.The status turns &&&
		</li>
		<br />
	</ul>
	</p>

	<p>
		<i>"Call Details"</i> provides the information of the calls within the service.
	<ul>
		<li>
			"<i>Call ID</i>" refers to the unique call ID generated within the service.
		</li>
		<br />
		<li>
			"<i>Status</i>" is "Active" for a newly created call.The status turns "expired" once the services are provisioned.
		</li>
		<br />
	</ul>
	</p> </span>
