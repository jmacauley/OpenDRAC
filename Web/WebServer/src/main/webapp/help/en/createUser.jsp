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
 * File: /help/en/createUser.jsp
 *
 * Description:
 *   This page displays US English help data for the create user
 *   functionality.
 *
 ****************************************************************************/
%>

<span class="gen">
    <p>
        <b><c:out value='${sessionScope["drac.security.userManagement.create"]}'/></b> allows the current system user to create other system users. Information is presented via a number of 'tabs' that group related information.
        <ul>
            <li>
            <b><c:out value='${sessionScope["drac.security.userManagement.create.user"]}'/></b><br>
            Mostly mandatory data that defines the minimum attributes of a user.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.userID"]}'/></i>" - The unique label by which the user will be identified.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.accountState"]}'/></i>" - The state of the user account. Can be either;
                    <ul>
                        <li><i><c:out value='${sessionScope["drac.security.userManagement.create.accountState.enabled"]}'/></i> - the user account is enabled and can be used for authenticating a user, or</li>
                        <li><i><c:out value='${sessionScope["drac.security.userManagement.create.accountState.disabled"]}'/></i> - the user account is disabled and cannot be used for authenticating a user.</li>
                    </ul>
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.disabledReason"]}'/></i>" - An explanation as to why the account state is set to disabled. Displayed only if the Account State is <i>"disabled"</i> and is optional.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.wsdlCertificate"]}'/></i>”
                    - Web Services password. A user provisioned credential that will be used by this specific user to authenticate via the OpenDRAC WSDL interface. Optional.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.authenticationType"]}'/></i>" - The type of authentication supported for this user. Currently supported types are;
                    <ul>
                        <li><i><c:out value='${sessionScope["drac.security.authenticationType.internal"]}'/></i> - DRAC's internal/local authentication system, or</li>
                        <li><i><c:out value='${sessionScope["drac.security.authenticationType.aSelect"]}'/></i> - A-Select Authentication System, the open source authentication system for users in a Web environment.</li>
                    </ul>
                    Other authentication types may be supported in future releases.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.userPassword"]}'/></i>" - The password used to authenticate the user.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.confirmUserPassword"]}'/></i>" - The confirmation password. Must match the User Password.
                </li>
                <br />
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.userManagement.create.personalData"]}'/></b><br>
            Optional information fields that define personal attributes of the user.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.surname"]}'/></i>" - The user's last name.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.givenName"]}'/></i>" - The user's first name.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.commonName"]}'/></i>" - The user's commonly used name.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.title"]}'/></i>” - The user's title (e.g. Senior Software Architect).
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.email"]}'/></i>" - The user's e-mail address.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.phone"]}'/></i>" - The user's telephone number.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.postalAddress"]}'/></i>" - The user's full postal address.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.description"]}'/></i>" - A useful description for the user (anything can go in here).
                </li>
                <br />
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.userManagement.create.organizationData"]}'/></b><br>
            Optional information fields that define which organization the user belongs to. The attributes are self-descriptive.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.orgName"]}'/></i>"
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.owner"]}'/></i>"
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.seeAlso"]}'/></i>"
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.orgUnitName"]}'/></i>”
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.orgDescription"]}'/></i>"
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.category"]}'/></i>"
                </li>
                <br />
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.userManagement.detail.preferences"]}'/></b><br>
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.timezone"]}'/></i>" - The time zone the user would like to use when using the system.
                </li>
                <br />
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.userManagement.create.userGroupMembership"]}'/></b><br>
            Optional information fields that define which user group(s) the user belongs to. Although optional, if the user is not assigned to a user group he/she will be able to log in to the system but will not have access to any resources within it.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.userGroups.availableUserGroups"]}'/></i>" - The list of User Groups available to the (currently being defined) user.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.userGroups.memberUserGroups"]}'/></i>" - The list of User Groups to which the (currently being defined) user belongs.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userGroupManagement.detail.viewUserGrp"]}'/></i>" - A hyperlink that allows the logged in user to view User Group details. Select a User Group from either list and click the corresponding hyperlink.
                </li>
                <br />
            </ul>

            <li>
            <b><c:out value='${sessionScope["drac.security.policy.accountPolicy"]}'/></b><br>
            Optional information fields that define the local account policy to be enforced on the user. Global Security Settings take effect for any corresponding attributes that are not defined here.
            </li>
            <br />
            <ul>
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.dormantPeriod"]}'/></i>" - The period of time after which, if there has been no attempt to login to the system, the system will automatically disable a user's account.  A value of "0" indicates this check is disabled.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.inactivityPeriod"]}'/></i>" - The period of the time a user accessing the system through a GUI can be inactive after which their session will be automatically terminated.  A value of "0" indicates this check is disabled. This period can be specified in seconds, minutes, hours or days.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.passwordHistorySize"]}'/></i>" - The number of old passwords to maintain in a user's password history.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.maxInvalidLoginAttempts"]}'/></i>" - The maximum number of invalid login attempts permitted per user before the account lockout period is invoked.  A value of "0" indicates this check is disabled.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.lockoutPeriod"]}'/></i>" - The duration a user account will be disabled following multiple login failures (determined by Maximum Login Attempts). This period can be specified in seconds, minutes, hours or days.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.passwordAging"]}'/></i>” - The maximum age in days of a user password, i.e. the time duration between forced password changes.  A value of "0" indicates this check is disabled.
                </li>
                <br />
                <li>
                    "<i><c:out value='${sessionScope["drac.security.userManagement.create.passwordExpirationNotification"]}'/></i>" - The number of days before a user's password expires that the system will start to notify the user to change their password.
                </li>
                <br />
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
    
    </p> </span>
