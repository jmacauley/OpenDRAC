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
 * File: /help/en/createUserGroup.jsp
 *
 * Description:
 *   This page displays US English help data for the create user group
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.userGroupManagement.create"]}'/></b>
        allows the user to create a user group.
    <ul>
        <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.title.info"]}'/></b><br>
            Mandatory data that defines the minimum attributes of a user group.
        </li>
        <br />
        <ul>
            <li>
                "<i><c:out value='${sessionScope["drac.security.userGroupManagement.create.name"]}'/></i>"
                - A unique name used to refer to the user group.
            </li>
            <br />
    
            <li>
                "<i><c:out value='${sessionScope["drac.security.userGroupManagement.create.userGroupType"]}'/></i>"
                - The specific type a user group instance represents.
            </li>
            <br />

            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupType.user"]}'/></i>"
                    - Has access to basic user capabilities through the system.
                </li>
                <br />
        
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupType.sysAdmin"]}'/></i>"
                    - Has access to all capabilities through the system.
                </li>
                <br />

                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupType.grpAdmin"]}'/></i>"
                    - Has access to basic user and group administrator capabilities through the system.
                </li>
                <br />
            </ul>
    
            <li>
                "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.parentUserGroup"]}'/></i>"
                - The parent of the currently being defined User Group. This list is dynamic and is restricted to those user groups that are permitted as parents for the currently logged in user. 
            </li>
            <br />
        </ul>

        <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.membershipInfo"]}'/></b><br>
            Optional data that defines the membership attributes of a user group.
        </li>
        <br />
        <ul>
            <li>
                <b><c:out value='${sessionScope["drac.security.userGroup.userMemberShip"]}'/></b>
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.create.available.users"]}'/></i>"
                    - The users currently defined in the system that the currently logged on user is allowed to view and add to the user group being defined.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.membership"]}'/> <c:out value='${sessionScope["drac.security.userGroup.userMemberShip"]}'/></i>"
                    - The users currently defined in the system that belong to the user group being defined.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.viewUser"]}'/></i>"
                    - A hyperlink that allows the user to view full details on the selected user.
                </li>
                <br />
            </ul>
    
            <li>
                <b><c:out value='${sessionScope["drac.security.userGroup.resourceGroupmembership"]}'/></b>
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.create.available.resourcegroups"]}'/></i>"
                    - The resource groups currently defined in the system that the currently logged on user is allowed to view and link to the user group being defined.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.membership"]}'/> <c:out value='${sessionScope["drac.security.userGroup.resourceGroupmembership"]}'/></i>"
                    - The resource groups currently defined in the system that are linked to the user group being defined.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.viewResGrp"]}'/></i>"
                    - A hyperlink that allows the user to view full details on the selected resource group.
                </li>
                <br />
            </ul>
        </ul>

        <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.title.userGroupPolicy"]}'/></b><br>
            Optional data that defines the policy to be enforced on this user group.
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
                    - The maximum aggregate service size allowed (in Mbits). Limits the total instantaneous bandwidth allowed within the network on a per-user group basis when specified against a user group.
                </li>
                <br />
            </ul>
        </ul>
    </ul>
    </p> </span>
