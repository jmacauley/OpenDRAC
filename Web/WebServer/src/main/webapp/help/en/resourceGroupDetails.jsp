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
 * File: /help/en/resourceGroupDetails.jsp
 *
 * Description:
 *   This page displays US English help data for the resource group details
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail"]}'/></b>
        provides the detailed description of the specified
        <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>.
        <ul>
            <li>
            <b><c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/></b><br>
            Mostly mandatory data that defines the minimum attributes of a resource group.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.name"]}'/></i>"
                    - The name of the <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.parentResourceGroup"]}'/></i>"
                    - The parent of the resource group currently being viewed.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.defaultResourceGroup"]}'/></i>"
                    - Indicates whether this is a <c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.defaultResourceGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.lastModificationUserIDNoBR"]}'/></i>"
                    - The id of the user who last modified this <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/>.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.creationDate"]}'/></i>"
                    - The date and time this <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/> was created in the system.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.lastModifiedDate"]}'/></i>"
                    - The date and time this <c:out value='${sessionScope["drac.security.resourceGroupManagement.resourceGroup"]}'/> was last modified in the system.
                </li>
                <br />
            </ul>
            <li>
            <b><c:out value='${sessionScope["drac.security.userGroupManagement.create.membershipInfo"]}'/></b><br>
            Optional information that defines which Resources belong to this resource group and which User Groups are linked to this resource group.
            </li>
            <br />
            <ul>
                <li>
                    <b><c:out value='${sessionScope["drac.security.resourceGroup.resources"]}'/></b>
                </li>
                <br />
                <ul>
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.create.available.resources"]}'/></i>"
                    - The resources currently defined in the system that the currently logged on user is allowed to view and add to the resource group being defined.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.create.member.resources"]}'/></i>"
                    - The resources currently defined in the system that belong to the resource group being defined.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.viewResource"]}'/></i>"
                        - A hyperlink that allows the user to view full details on the selected resource.
                    </li>
                    <br />
                </ul>
                <li>
                    <b><c:out value='${sessionScope["drac.security.resourceGroup.userGroups"]}'/></b>
                </li>
                <br />
                <ul>
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.create.available.userGroups"]}'/></i>"
                        - The user groups currently defined in the system that the currently logged on user is allowed to view and add to the resource group being defined.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.resourceGroupManagement.create.member.userGroups"]}'/></i>"
                        - The user groups currently defined in the system that the resource group being defined has been added to.
                    </li>
                    <br />
                    <li>
                        "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.viewUserGrp"]}'/></i>"
                        - A hyperlink that allows the user to view full details on the selected user group.
                    </li>
                    <br />
                </ul>
            </ul>
            <li>
                <b><c:out value='${sessionScope["drac.security.resourceGroupManagement.detail.resourceGroupPolicy"]}'/></b><br>
                Optional information that defines the policy to be enforced on this resource group.
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
                        - The maximum aggregate service size allowed (in Mbits).  Limits the total instantaneous bandwidth allowed on all endpoints within a resource group.
                    </li>
                    <br />
                </ul>

                <li>
                    <b><c:out value='${sessionScope["drac.security.policy.systemAccess.resource"]}'/></b>
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
        </ul>
    </p>
</span>
