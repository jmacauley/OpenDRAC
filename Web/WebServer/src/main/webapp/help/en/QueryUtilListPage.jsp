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
 * File: /help/en/QueryUtilListPage.jsp
 *
 * Description:
 *   This page displays US English help data for the EndPoint Utilization Result functionality
 *
 ****************************************************************************/
%>

<span class="gen">
        <p>
                <i><b>"<c:out value='${sessionScope["drac.network.viewUtil"]}'/>"</b></i> lists the utilization results based on the chosen End Point.
        <ul>
                <li>
                        <i>"<c:out value='${sessionScope["drac.schedule.detail.sno"]}'/>"</i> lists the number of schedules matching the provided query filter.
                </li>
                <br />
                <li>
                        <i>"<c:out value='${sessionScope["drac.schedule.detail.name"]}'/>"</i> is the user given name for the Schedule.
                </li>
                <br />
                <li>
                        <i>"<c:out value='${sessionScope["drac.schedule.detail.serviceid"]}'/>"</i> is the unique ID of the service.
                </li>
                <br />
                <li>
                                            <i>"<c:out value='${sessionScope["drac.schedule.rate"]}'/>"</i> specifies the rate of the service.
                </li>
                <br />
                <li>
                        <i>"<c:out value='${sessionScope["drac.schedule.startdate"]}'/>"</i> specifies the start date of the service.
                </li>
                <br />
                <li>
                        <i>"<c:out value='${sessionScope["drac.schedule.create.starttime"]}'/>"</i> specifies the start time of the service.
                </li>
                <br />
                <li>
                        <i>"<c:out value='${sessionScope["drac.schedule.enddate"]}'/>"</i> specifies the end date of the service.
                </li>
                <br />
                <li>
                        <i>"<c:out value='${sessionScope["drac.schedule.create.endtime"]}'/>"</i>specifies the end time of the service.
                </li>
                <br />

        </ul>
        </p> </span>
                                            
