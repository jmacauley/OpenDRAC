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
 * File: /help/en/createUserGroupResult.jsp
 *
 * Description:
 *   This page displays US English help data for the create user group result
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.result"]}'/></b>
        displays a read-only view of the created <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
        <ul>
            <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.title.info"]}'/></b><br>
            Mandatory data that defines the minimum attributes of a <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.create.name"]}'/></i>"
                    - A unique name used to refer to the <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.create.parentUserGroup"]}'/></i>"
                    - The parent of the new <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.create.userGroupType"]}'/></i>"
                    - The specific type this user group instance represents.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.lastModificationUserIDNoBR"]}'/></i>"
                    - The user id of the user that made the last modification to this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>. 
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.creationDate"]}'/></i>"
                    - The date and time this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/> was created in the system. 
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.lastModifiedDate"]}'/></i>"
                    - The date and time this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/> was last modified in the system. 
                </li>
                <br />
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.membershipInfo"]}'/></b><br>
            Optional information that defines which <c:out value='${sessionScope["drac.security.userGroup.users"]}'/>
            belong to this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>
            and which <c:out value='${sessionScope["drac.security.userGroup.resourceGroupmembership"]}'/>
            are linked to this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
            </li>
            <br />
            <ul>
                <li>
                    <b><c:out value='${sessionScope["drac.security.userGroup.users"]}'/></b>
                </li>
                <br />
                <ul>
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.membership"]}'/> <c:out value='${sessionScope["drac.security.userGroup.users"]}'/></i>"
                    - The user(s) currently defined in the system that belong to this
                    <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
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
                        - The <c:out value='${sessionScope["drac.security.userGroup.resourceGroupmembership"]}'/>
                         currently defined in the system that have been added to this
                         <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
                    </li>
                    <br />
                </ul>
            </ul>

            <li>
                <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.title.userGroupPolicy"]}'/></b><br>
                Optional information that defines the policy to be enforced on this <c:out value='${sessionScope["drac.security.userGroupManagement.userGroup"]}'/>.
            </li>
            <br />
            <ul>
                <li>
                    <c:out value='${sessionScope["drac.security.policy.bandwidthControl"]}'/>
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
                        - The maximum aggregate service size allowed (in Mbits).  Limits the total instantaneous bandwidth allowed on all endpoints within a resource group.
                    </li>
                    <br />
                </ul>

                <li>
                    <c:out value='${sessionScope["drac.security.policy.systemAccess"]}'/>
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
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.onDays"]}'/></i>"
                        - The days of the week the rule is in effect for.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.onDates"]}'/></i>"
                        - The dates the rule is in effect for.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.policy.systemAccess.inMonths"]}'/></i>"
                        - The month(s) of the year for which the rule is in effect.
                    </li>
                    <br />
                </ul>

        </ul>
    </p>
</span>
