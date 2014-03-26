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
 * File: /help/en/globalPolicy.jsp
 *
 * Description:
 *   This page displays US English help data for the global security settings
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.globalPolicy"]}'/></b> allows the current system user to
        define security settings that apply globally to all users and/or user groups and/or resource groups. Information is presented via a number of 'tabs' that group related information.
        <ul>
            <li>
            <b><c:out value='${sessionScope["drac.security.globalPolicy.authentication"]}'/></b><br>
            Self defining.
            </li>
            <br />
            <ul>
                <li>
                <b><c:out value='${sessionScope["drac.security.globalPolicy.authType"]}'/></b><br>
                - The authentication types activated (or not) in the system.
                </li>
                <br />
                <li>
                <b><c:out value='${sessionScope["drac.security.globalPolicy.dormancyPeriod"]}'/></b><br>
                Self defining.
                </li>
                <br />
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.policy.sessionConfig"]}'/></b><br>
            Optional information fields that define the policy to be applied globally to internal accounts.
            </li>
            <br />
            <ul>
                <li>
                <b><c:out value='${sessionScope["drac.security.policy.accountPolicy"]}'/></b><br>
                Optional information fields that define the account policy to be enforced on users globally.
                </li>
                <br />
                <ul>
                    <li>
                    <b><c:out value='${sessionScope["drac.security.globalPolicy.inactivity"]}'/></b><br>
                    Self defining.
                    </li>
                    <br />
                    <li>
                    <b><c:out value='${sessionScope["drac.security.globalPolicy.invalidLogins"]}'/></b><br>
                    Self defining.
                    </li>
                    <br />
                    <li>
                    <b><c:out value='${sessionScope["drac.security.globalPolicy.ipAddressLocking"]}'/></b><br>
                    </li>
                    <br />
                    <ul>
                        <li>
                            "<i><c:out value='${sessionScope["drac.security.userManagement.create.clientIPToLock"]}'/></i>" - An IP address that is to be blocked from accessing the system.
                        </li>
                        <br />
                        <li>
                            "<i><c:out value='${sessionScope["drac.security.userManagement.create.lockedClientIPs"]}'/></i>" - The list of IP addresses currently blocked from accessing the system.
                        </li>
                        <br />
                    </ul>
                </ul>

                <li>
                <b><c:out value='${sessionScope["drac.security.globalPolicy.passwordPolicy"]}'/></b><br>
                Optional information fields that define the password policy to be enforced on users globally.
                Self defining.
                </li>
                <br />
                <ul>
                    <li>
                    <b><c:out value='${sessionScope["drac.security.globalPolicy.passwordRules"]}'/></b><br>
                    Self defining.
                    </li>
                    <br />
                    <li>
                    <b><c:out value='${sessionScope["drac.security.globalPolicy.invalidPasswords"]}'/></b><br>
                    Optional information fields that define the local account policy to be enforced on the user.
                    </li>
                    <br />
                    <ul>
                        <li>
                            "<i><c:out value='${sessionScope["drac.security.globalPolicy.invalidateThisPassword"]}'/></i>"
                            - A password that is not allowed to be used to access the system.
                        </li>
                        <br />
                        <li>
                            "<i><c:out value='${sessionScope["drac.security.globalPolicy.invalidPasswords"]}'/></i>"
                            - The list of passwords that are not allowed to be used to access the system.
                        </li>
                        <br />
                    </ul>
                </ul>
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.groupPolicy"]}'/></b><br>
            Optional information that defines the policy to be enforced on user groups globally.
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
                    <br />

            <li>
            <b><c:out value='${sessionScope["drac.security.resourcePolicy"]}'/></b><br>
            Optional information that defines the policy to be enforced on resource groups globally.
            The same policy details can be put in effect for Resource Groups as can be for User Groups.
            Please refer to the <b><c:out value='${sessionScope["drac.security.groupPolicy"]}'/></b> section for Resource Group policy details.
            </li>
            <br />
        </ul>
    
    </p> </span>
