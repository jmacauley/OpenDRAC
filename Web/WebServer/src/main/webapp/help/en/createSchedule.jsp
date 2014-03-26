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
		<i>"Create Schedule"</i> allows the user to create schedules. The schedule could be a single or a recurring schedule.
	<ul>
		<li>
			"<i>Schedule Name</i>" is the optional name with which the user would like to refer to the schedule.
		</li>
		<br />

		<li>
			"<i>Rate</i>” is the rate at which the schedule is created.
		</li>
		<br />

		<li>
			"<i>Source Layer</i>" refers to the layer of the source end point. “Endpoints" identify network access points that can be dynamically interconnected to provide bandwidth between two points within the network. The "List Endpoints" functionality provides
			a mechanism to view all endpoints accessible within your user profile.
		</li>
		<br />

		<li>
			"<i>Destination Layer</i>" refers to the layer of the destination endpoint.
		</li>
		<br />


		<li>
			"<i>Source TNA</i>" refers to the source end point for the call creation
		</li>
		<br />

		<li>
			"<i>Destination TNA</i>" refers to the destination end point for the call creation.
		</li>
		<br />

		<li>
			"<i>“Utilization”</i>" refers to the utilization factor of the endpoint at the current time.
		</li>
		<br />

		<li>
			"<i>Start Time</i>" is the starting time for the schedule
		</li>
		<br />

		<li>
			"<i>End Time</i>" is the end time for the schedule
		</li>
		<br />

		<li>
			"<i>Start Date</i>" is the starting time for the schedule.
		</li>
		<br />

		<li>
			"<i>End Date</i>" is the end date for the schedule
		</li>
		<br />

		<li>
			"<i>Advanced Options</i>" are the optional parameters that the user can provide for creation of the schedule.
		</li>
		<br />

		<li>
			"<i>E-mail ID</i>" is the Email ID for the notification about the schedules created
		</li>
		<br />

		<li>
			"<i>SRLG.Cost,Metric and Hop </i>" are the optional parameters for the creation of the schedule
		</li>
		<br />

		<li>
			"<i>Recurrence </i>" The recurrence allows the user to create the recurring schedules. Below are the following options :
		</li>
		<br />

		<li>
			"<i>Daily</i>" refers to the daily schedules
		</li>
		<br />

		<li>
			"<i>weekly</i>" refers to the recurring schedules created for different days in a week.
		</li>
		<br />

		<li>
			"<i>Monthly</i>" refers to the monthly schedule created. Any number between 1-31 could be specified for a monthly schedule.
		</li>
		<br />

		<li>
			"<i>Yearly</i>" refers to the schedule that recur on a particular day of the month , yearly.
		</li>
		<br />

	</ul>
	</p> </span>
