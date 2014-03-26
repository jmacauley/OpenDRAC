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
 * File: /help/en/userGroupDetails.jsp
 *
 * Description:
 *   This page displays US English help data for the user group details
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.userGroupManagement.detail"]}'/></b>
        provides the detailed description of the selected
        <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>. Information is presented via a series of groupings that group related information.
        <ul>
            <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/></b><br>
            Mostly mandatory data that defines the minimum attributes of a user group.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.name"]}'/></i>"
                    - The name of the
                    <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.parentUserGroup"]}'/></i>"
                    - The parent of the user group currently being viewed.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.userGroupType"]}'/></i>"
                    - The type of this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.lastModificationUserIDNoBR"]}'/></i>"
                    - The id of the user who last modified this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.creationDate"]}'/></i>"
                    - The date and time this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>
                    was created in the system.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.lastModifiedDate"]}'/></i>"
                    - The date and time this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>
                    was last modified in the system.
                </li>
                <br />
            </ul>
            <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.membershipInfo"]}'/></b><br>
            Optional information that defines which Users belong to this user group and which Resource Groups are linked to this user group.
            </li>
            <br />
            <ul>
                <li>
                    <b><c:out value='${sessionScope["drac.security.userGroup.users"]}'/></b>
                </li>
                <br />
                <ul>
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.userGroup.membership"]}'/> <c:out value='${sessionScope["drac.security.userGroup.users"]}'/></i>"
                        - The list of users which belong to this user group.
                    </li>
                    <br />
                </ul>
                <li>
                    <b><c:out value='${sessionScope["drac.security.userGroup.resourceGroupmembership"]}'/></b>
                </li>
                <br />
                <ul>
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.userGroup.membership"]}'/> <c:out value='${sessionScope["drac.security.userGroup.resourceGroupmembership"]}'/></i>"
                        - The list of resource groups that are linked to this user group.
                    </li>
                    <br />
                </ul>
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.detail.groupPolicy"]}'/></b><br>
        	Optional information that defines the policy to be enforced on this user group.
            </li>
            <br />

            <ul>
                <li>
                    <b><c:out value='${sessionScope["drac.security.policy.bandwidthControl"]}'/></b>
                </li>
                <br />
                <ul>
        		<li>
        			"<i><c:out value='${sessionScope["drac.security.policy.bw.maxservicesize"]}'/></i>"
        			- The maximum service size (in Mbits/second) that can be requested for a single service. An absent rule represents no restriction on service size.
        		</li>
        		<br />
        		<li>
        			"<i><c:out value='${sessionScope["drac.security.policy.bw.maxserviceduration"]}'/></i>"
        			- The maximum service duration (in seconds) that can be requested for a single service. An absent rule represents no restriction on duration.
        		</li>
        		<br />
        		<li>
        			"<i><c:out value='${sessionScope["drac.security.policy.bw.maxservicebandwidth"]}'/></i>"
        			- The maximum bandwidth (service size * service duration) (in Mbits) that can be requested for a single service. An absent rule represents no restriction on bandwidth.
        		</li>
        		<br />
        		<li>
        			"<i><c:out value='${sessionScope["drac.security.policy.bw.maxaggregateservicesize"]}'/></i>"
        			- The maximum aggregate service size allowed (in Mbits). Limits the total instantaneous bandwidth allowed within the network on a per-user group basis.
        		</li>
        		<br />
                </ul>

                <li>
                    <b><c:out value='${sessionScope["drac.security.policy.systemAccess"]}'/></b>
                </li>
                <br />
                <ul>
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.permission"]}'/></i>"
                        - Indicates the permission for the rule.
                        Values are either <c:out value='${sessionScope["drac.security.policy.grant"]}'/>
                        indicating that the rule should allow access or
                        <c:out value='${sessionScope["drac.security.policy.deny"]}'/> indicating that the rule should refuse access.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.startTime"]}'/></i>"
                        - The time at which the rule will start to come in effect.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.endTime"]}'/></i>"
                        - The time at which the rule will cease to be in effect.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.specifyDays"]}'/></i>"
                        - The user should select this radio button if they wish to define the rule(s) using days of the week.
                        If this radio is selected the
                        check boxes are active and the user should select from this choice which days the rule is in effect for.
                        Use "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.selectAll"]}'/></i>" to select all days.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.specifyDates"]}'/></i>"
                        - The user should select this radio button if they wish to define the rule(s) using days of the month (dates).
                        If this radio is selected the date selector is active.
                        The user should select from this choice which dates the rule is in effect for.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.inMonths"]}'/></i>"
                        - The month(s) of the year for which the rule is in effect.
                        This is a multi-select box. Use Ctrl and Shift while selecting months to select multiple values.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.button.add"]}'/></i>"
                        - A button to add the newly created rule to the list of current and new rules
                        that will be applied to the
                        <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>.
                        
                    </li>
                    <br />
                    Note: if no values are selected prior to clicking the "<i><c:out value='${sessionScope["drac.security.policy.button.add"]}'/></i>" button a default rule will be created.
                    This rule will be fully open in that permission will be granted for all days/dates in all months.
                </ul>

            </ul>
    </p>
 </span>
