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

<%@ page session="true" errorPage="/common/dracError.jsp"
             import="com.nortel.appcore.app.drac.server.webserver.struts.security.userManagement.form.*"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
/****************************************************************************
 * OpenDRAC WEB GUI version 1.0
 *
 * File: /management/userManagement/editUser.jsp
 * Author: Colin Hart
 *
 * Description:
 *  This page allows user to edit a user
 *
 ****************************************************************************/

String pageRef = "drac.security.userManagement.edit";

%>

<%@ include file="/common/header_struts.jsp" %>

<script type="text/javascript" src="/scripts/tabpane.js"></script>

<%
    // Set the form-bean as an object we can use in any scriplets.
    EditUserForm editForm = (EditUserForm) request.getAttribute("EditUserForm");
%>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<html:form method="POST" action="/management/userManagement/handleEditUser.do">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
    <table cellspacing="0" cellpadding="0" border="0" width="690" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5"/></td>
        </tr>
        <logic:messagesPresent>
        <tr>
            <td>
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.security.userManagement.create.title.errors"/></b></font></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- User information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <td align="center" class="tbForm1" nowrap>
                            <html:errors/>
                        </td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Drop shadow. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbl">
                <tbody>
                    <tr>
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        </logic:messagesPresent>

        <logic:messagesPresent message="true">
        <tr>
            <td>
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.security.userManagement.create.title.errors"/></b></font></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt=""/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- User information contents. -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <html:messages id="message" message="true">
                    <tr>
                        <td align="center" class="tbForm1" nowrap><font color="red"><b><bean:write name="message"/></b></font></td>
                    </tr>
                    </html:messages>
                </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <!-- Drop shadow. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbl">
                <tbody>
                    <tr>
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        </logic:messagesPresent>

        <logic:present name="editSuccess" scope="request">
        <tr>
            <td>
                <table cellspacing="0" cellpadding="5" border="0" align="center">
                <tbody>
                    <tr>
                        <td align="center" class="tbForm1" nowrap>
                           <font color="green"><b><bean:message key="drac.security.userManagement.edit.success"/></b></font>
                        </td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        </logic:present>

        <tr>
            <td align="center"><bean:message key="drac.security.userManagement.edit.text"/></td>
        </tr>

        <tr>
            <td>
                <!-- Header -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.userManagement.create.title.info"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>

        <tr>
            <td>
            <!-- Contents -->
            <table cellspacing="0" cellpadding="5" border="0" align="center" width="500" class="tbForm">
            <tbody>
                <tr>
                    <td colspan="5" align="left" valign="top" class="row2">
                        <div class="tab-pane" id="editUserPane" align="center">
                            <script type="text/javascript">
                            tp2 = new WebFXTabPane( document.getElementById( "editUserPane" ) );
                            </script>

                            <div class="tab-page" id="userDetailsTab" align="center">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.edit.user"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "userDetailsTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userDetailsTable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.userID"/>
                                            <html:hidden property="userID" value="${EditUserForm.userID}"/>
                                        </td>
                                        <td class="tbForm1" nowrap colspan="2">
                                            <b>${fn:escapeXml(EditUserForm.userID)}</b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap width="30%">
                                            <bean:message key="drac.security.userManagement.create.accountState"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                        <logic:present name="editable">
                                        <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                            <!-- Only allow admins to edit -->
                                            <html:select tabindex="1" property="accountState" styleId="accountStateSelect" onchange="handleChangeAccountState()" >
                                                <html:option value="enabled"><bean:message key="drac.security.userManagement.create.accountState.enabled"/></html:option>
                                                <html:option value="disabled"><bean:message key="drac.security.userManagement.create.accountState.disabled"/></html:option>
                                            </html:select>
                                        </c:if>
                                        <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                                            <!-- Regular user always sees as read only -->
                                            <logic:empty name="EditUserForm" property="accountState">
                                                &nbsp;
                                            </logic:empty>
                                            <logic:notEmpty name="EditUserForm" property="accountState">
                                                <b>${EditUserForm.accountState}</b>
                                            </logic:notEmpty>
                                        </c:if>
                                        </logic:present>
                                        <logic:notPresent name="editable">
                                            <!-- User can read only -->
                                            <logic:empty name="EditUserForm" property="accountState">
                                                &nbsp;
                                            </logic:empty>
                                            <logic:notEmpty name="EditUserForm" property="accountState">
                                                <b>${EditUserForm.accountState}</b>
                                            </logic:notEmpty>
                                        </logic:notPresent>
                                        </td>
                                    </tr>
                                    <tr id="disabledLayer" style="display:none">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.disabledReason"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="2" style="width: 175px" property="disabledReason" />
                                        </td>
                                    </tr>
                                    <logic:present name="editable">
                                    <!-- Allow user to edit -->
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.detail.wsdlCertificate"/>
                                        </td>
                                        <td valign="top" class="tbForm1" nowrap>
                                            <html:password tabindex="3" style="width: 175px" property="wsdlCertificate"  errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    </logic:present>
                                    <logic:notPresent name="editable">
                                    <!-- Cannot edit wsdl cert but send it down anyway -->
                                    <html:hidden property="wsdlCertificate" value="${EditUserForm.wsdlCertificate}"/>
                                    </logic:notPresent>
                                    <!-- Always read only -->
                                    <tr>
                                        <td class="tbForm1" nowrap id="authenticationTypeSelect">
                                            <bean:message key="drac.security.userManagement.create.authenticationType"/>
                                            <html:hidden property="authType" value="${EditUserForm.authType}"/>
                                        </td>
                                        <td valign="top" class="tbForm1" nowrap>
                                            <b>${EditUserForm.authType}</b>
                                        </td>
                                    </tr>

                                    <logic:present name="editable">
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">                                    <!-- Allow user to change password here -->
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <!-- Allow Admin users to change password here -->
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.userPassword"/>
                                        </td>
                                        <td class="tbForm1" nowrap id="password1Col">
                                            <html:password tabindex="4" style="width: 175px" property="password" styleId="userPassword"  errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.confirmUserPassword"/>
                                        </td>
                                        <td class="tbForm1" nowrap id="password1Col">
                                            <html:password tabindex="5" style="width: 175px" property="password2" styleId="confirmUserPassword"  errorStyleClass="invalid"/>
                                        </td>
                                    </tr>
                                    </c:if>
                                    </c:if>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                                    <!-- Cannot change password here but send it down anyway -->
                                    <html:hidden property="password" value="${EditUserForm.password}"/>
                                    <html:hidden property="password2" value="${EditUserForm.password2}"/>
                                    </c:if>
                                    </logic:present>
                                    <logic:notPresent name="editable">
                                    <!-- Cannot change password here but send it down anyway -->
                                    <html:hidden property="password" value="${EditUserForm.password}"/>
                                    <html:hidden property="password2" value="${EditUserForm.password2}"/>
                                    </logic:notPresent>

                                    <!-- Always read only -->
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr id="passwordExpirationDate">
                                        <td valign="top" class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.detail.internalAuthentication.expirationDate"/><br>
                                        </td>
                                        <td valign="top" class="tbForm1" nowrap>
                                            <b>${EditUserForm.expirationDate}</b>
                                        </td>
                                    </tr>
                                    </c:if>
                                    <tr>
                                        <td valign="top" class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.detail.creationDate"/>
                                        </td>
                                        <td valign="top" class="tbForm1" nowrap colspan="2">
                                            <b>${EditUserForm.creationDate}</b>&nbsp;<bean:message key="drac.text.at"/>&nbsp;<b>${EditUserForm.creationTime}</b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td valign="top" class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.detail.lastModifiedDate"/>
                                        </td>
                                        <td valign="top" class="tbForm1" nowrap>
                                            <b>${EditUserForm.lastModifiedDate}</b>&nbsp;<bean:message key="drac.text.at"/>&nbsp;<b>${EditUserForm.lastModifiedTime}</b>
                                        </td>
                                    </tr>
                                </table>
                            </div>  <!-- end div user details -->

                            <div class="tab-page" id="personalDataTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.create.personalData"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "personalDataTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceStateTable">
                                <logic:present name="editable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.surname"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="1" style="width: 175px" property="surname" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.email"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="5" style="width: 175px" property="email" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.givenName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="2" style="width: 175px" property="givenName" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.phone"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="6" style="width: 175px" property="phone" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.commonName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="3" style="width: 175px" property="commonName" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.postalAddress"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="7" style="width: 175px" property="postalAddress" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.title"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="4" style="width: 175px" property="title" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.description"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="8" style="width: 175px" property="description" />
                                        </td>
                                    </tr>
                                </logic:present>
                                <logic:notPresent name="editable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.surname"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.surname)}</b>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.email"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.email)}</b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.givenName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.givenName)}</b>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.phone"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.phone)}</b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.commonName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.commonName)}</b>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.postalAddress"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.postalAddress)}</b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.title"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.title)}</b>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.description"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.description)}</b>
                                        </td>
                                    </tr>
                                </logic:notPresent>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="109">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <c:if test="${EditUserForm.authType eq 'A-Select'}">
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="14">
                                        </td>
                                    </tr>
                                    </c:if>
                                    </c:if>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="26">
                                        </td>
                                    </tr>
                                    </c:if>
                                </table>
                            </div> <!-- end div personal data -->

                            <div class="tab-page" id="organizationTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.detail.organizationData"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "organizationTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="organizationTable">
                                <logic:present name="editable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="1" style="width: 175px" property="orgName"  errorStyleClass="invalid" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgUnitName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="4" style="width: 175px" property="orgUnitName"  errorStyleClass="invalid" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.owner"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="2" style="width: 175px" property="owner"  />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgDescription"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="5" style="width: 175px" property="orgDescription" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.seeAlso"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="3" style="width: 175px" property="seeAlso" />
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.category"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <html:text tabindex="6" style="width: 175px" property="category" />
                                        </td>
                                    </tr>
                                </logic:present>
                                <logic:notPresent name="editable">
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.orgName)}</b>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgUnitName"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.orgUnitName)}</b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.owner"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.owner)}</b>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.orgDescription"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.orgDescription)}</b>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.seeAlso"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.seeAlso)}</b>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.category"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <b>${fn:escapeXml(EditUserForm.category)}</b>
                                        </td>
                                    </tr>
                                </logic:notPresent>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="141">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <c:if test="${EditUserForm.authType eq 'A-Select'}">
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="46">
                                        </td>
                                    </tr>
                                    </c:if>
                                    </c:if>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                                    <tr>
                                        <td class="tbForm1" colspan="4" align="right">
                                            <img src="/images/spacer.gif" height="58">
                                        </td>
                                    </tr>
                                    </c:if>
                                </table>
                            </div>  <!-- end div org data -->

                            <div class="tab-page" id="preferencesTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.detail.preferences"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "preferencesTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="preferencesTable">
                                    <tr>
                                        <td class="tbForm1" align="left">
                                            <bean:message key="drac.security.userManagement.create.timezone"/>
                                        </td>
                                    <logic:present name="editable">
                                        <td class="tbForm1" nowrap>
                                            <html:select tabindex="1" property="timeZone">
                                                <logic:iterate id="timeZoneId" indexId="i" name="EditUserForm" property="timeZoneIds">
                                                    <html:option value="${timeZoneId}">${EditUserForm.timeZoneNames[i]}</html:option>
                                                </logic:iterate>
                                            </html:select>
                                        </td>
                                    </logic:present>
                                    <logic:notPresent name="editable">
                                        <td class="tbForm1" nowrap>
                                            <b>${EditUserForm.timeZone}</b>
                                        </td>
                                    </logic:notPresent>
                                    </tr>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="205">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <c:if test="${EditUserForm.authType eq 'A-Select'}">
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="110">
                                        </td>
                                    </tr>
                                    </c:if>
                                    </c:if>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                                    <tr>
                                        <td class="tbForm1" colspan="4">
                                            <img src="/images/spacer.gif" height="122">
                                        </td>
                                    </tr>
                                    </c:if>
                                </table>
                            </div>  <!-- end preferences data -->

                            <div class="tab-page" id="userGroupMembershipTab">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.create.userGroupMembership"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "userGroupMembershipTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="userGroupMembershipTable">
                                <logic:present name="editable">
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                    <!-- Only allow admins to edit -->
                                    <tr>
                                        <td class="tbForm1" align="center" >
                                            <bean:message key="drac.security.userManagement.create.userGroups.availableUserGroups"/><br>
                                            <html:select tabindex="1" property="availableUserGroups" size="3" style="width:200px" styleId="availableUserGroups" multiple="true">
                                                <html:options property="availableUserGroups"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewUserGroup('availableUserGroups')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                        </td>
                                        <td class="tbForm1" align="center" valign="middle">
                                            <input tabindex="2" type="button" id="addUserToMembership" onclick="moveItems('availableUserGroups','userGroupsMembers');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/><p>
                                            <input tabindex="3" type="button" id="addUserToMembership" onclick="moveItems('userGroupsMembers','availableUserGroups');" value="<bean:message key='drac.security.policy.button.arrowLeft'/>"/>
                                        </td>
                                        <td class="tbForm1" align="center">
                                            <bean:message key="drac.security.userManagement.create.userGroups.memberUserGroups"/><br>
                                            <html:select tabindex="4" property="memberUserGroups" size="3" style="width:200px" styleId="userGroupsMembers" multiple="true">
                                                <html:options property="memberUserGroups"/>
                                            </html:select><br>
                                            <a href="javascript:void(0)" tabindex="-1" onclick="return viewUserGroup('userGroupsMembers')"><bean:message key="drac.security.userGroupManagement.detail.viewUserGrp"/></a>
                                        </td>
                                    </tr>
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr>
                                        <td class="tbForm1" colspan="3">
                                            <img src="/images/spacer.gif" height="149">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <c:if test="${EditUserForm.authType eq 'A-Select'}">
                                    <tr>
                                        <td class="tbForm1" colspan="3">
                                            <img src="/images/spacer.gif" height="54">
                                        </td>
                                    </tr>
                                    </c:if>
                                    </c:if>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                                    <!-- Regular user always sees as read only -->
                                    <tr>
                                        <td class="tbForm1" nowrap width="20%">
                                            <bean:message key="drac.security.userManagement.create.userGroups.memberUserGroups"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <logic:empty name="EditUserForm" property="memberUserGroups">
                                                &nbsp;
                                            </logic:empty>
                                            <logic:notEmpty name="EditUserForm" property="memberUserGroups">
                                            <logic:iterate id="memberUserGroups" name="EditUserForm" property="memberUserGroups">
                                                <html:link href="/management/userGroupManagement/queryUserGroup.do"
                                                           paramId="ugName" paramName="memberUserGroups"><b>${memberUserGroups}</b></html:link><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="132">
                                        </td>
                                    </tr>
                                    </c:if>
                                    </c:if>
                                </logic:present>
                                <logic:notPresent name="editable">
                                    <!-- Display as read only -->
                                    <tr>
                                        <td class="tbForm1" nowrap width="20%">
                                            <bean:message key="drac.security.userManagement.create.userGroups.memberUserGroups"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <logic:empty name="EditUserForm" property="memberUserGroups">
                                                &nbsp;
                                            </logic:empty>
                                            <logic:notEmpty name="EditUserForm" property="memberUserGroups">
                                            <logic:iterate id="memberUserGroups" name="EditUserForm" property="memberUserGroups">
                                                <html:link href="/management/userGroupManagement/queryUserGroup.do"
                                                           paramId="ugName" paramName="memberUserGroups"><b>${memberUserGroups}</b></html:link><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="132">
                                        </td>
                                    </tr>
                                    </c:if>
                                </logic:notPresent>
                                </table>
                            </div>  <!-- end div user group membership -->

                            <div class="tab-page" id="policyDetailsTab" align="center">
                                <h2 class="tab"><bean:message key="drac.security.userManagement.edit.accountPolicy"/></h2>
                                <script type="text/javascript">tp2.addTabPage( document.getElementById( "policyDetailsTab" ) );</script>
                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="policyDetailsTable">
                                <logic:present name="editable">
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                        <!-- Only allow admins to edit -->
                                        <tr>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.dormantPeriod"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="1" styleId="dormantPeriodField" style="width: 55px" property="dormantPeriod" />
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </td>
                                        </tr>
                                        <c:if test="${EditUserForm.authType eq 'Internal'}">
                                        <tr id="internalAccountRow1">
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.inactivityPeriod"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="2" style="width: 55px" styleId="inactivityPeriodField" property="inactivityPeriod" />
                                                <html:select tabindex="3" property="inactivityMetric" >
                                                    <html:option value="0"><bean:message key="drac.security.globalPolicy.metric.seconds"/></html:option>
                                                    <html:option value="1"><bean:message key="drac.security.globalPolicy.metric.minutes"/></html:option>
                                                    <html:option value="2"><bean:message key="drac.security.globalPolicy.metric.hours"/></html:option>
                                                    <html:option value="3"><bean:message key="drac.security.globalPolicy.metric.days"/></html:option>
                                                </html:select>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.lockoutPeriod"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="4" style="width: 55px" styleId="lockoutPeriodField" property="lockoutPeriod" />
                                                <html:select tabindex="5" property="lockoutMetric" >
                                                    <html:option value="0"><bean:message key="drac.security.globalPolicy.metric.seconds"/></html:option>
                                                    <html:option value="1"><bean:message key="drac.security.globalPolicy.metric.minutes"/></html:option>
                                                    <html:option value="2"><bean:message key="drac.security.globalPolicy.metric.hours"/></html:option>
                                                    <html:option value="3"><bean:message key="drac.security.globalPolicy.metric.days"/></html:option>
                                                </html:select>
                                            </td>
                                        </tr>
                                        <tr id="internalAccountRow2">
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.maxInvalidLoginAttempts"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="6" style="width: 55px" styleId="maxInvalidLoginField" property="maxInvalidLoginAttempts" />
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.passwordHistorySize"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="7" style="width: 55px" styleId="passwordHistorySizeField" property="passwordHistorySize" />
                                            </td>
                                        </tr>
                                        <tr id="internalAccountRow3">
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.passwordAging"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="8" style="width: 55px" styleId="passwordAgingField" property="passwordAging" />
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <bean:message key="drac.security.userManagement.create.passwordExpirationNotification"/>
                                            </td>
                                            <td class="tbForm1" nowrap>
                                                <html:text tabindex="9" style="width: 55px" styleId="passwordExpirationNotifcationField" property="passwordExpirationNotification" />
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </td>
                                        </tr>
                                        <tr id="lockedIPsRow">
                                            <td class="tbForm1" nowrap colspan="4">
                                                <table width="95%" cellspacing="1" cellpadding="5" border="0" id="resourceGroupMemberTable">
                                                    <tr>
                                                        <td class="tbForm1" nowrap>
                                                            <bean:message key="drac.security.userManagement.create.clientIPToLock"/>
                                                        </td>
                                                        <td class="tbForm1" nowrap>
                                                            <html:text tabindex="10" style="width: 155px" styleId="lockedIPsField" property="clientIPToLock" />
                                                        </td>
                                                        <td class="tbForm1" align="center" valign="middle">
                                                            <input tabindex="11" type="button" id="addResGrpToMembership" onclick="moveIPItems('lockedIPsField','lockedClientIPsList');" value="<bean:message key='drac.security.policy.button.arrowRight'/>"/>
                                                        </td>
                                                        <td class="tbForm1">
                                                            <bean:message key="drac.security.userManagement.create.lockedClientIPs"/><br>
                                                            <html:select tabindex="12" property="lockedClientIPs" size="5" style="width:155px" styleId="lockedClientIPsList" multiple="true">
                                                                <html:options property="lockedClientIPs"/>
                                                            </html:select>
                                                        </td>
                                                        <td class="tbForm1" align="center" valign="middle">
                                                            <input tabindex="13" type="button" id="removeResGrpFromMembership" onclick="removeIPItems('lockedClientIPsList');" value="<bean:message key='drac.security.policy.button.removeIP'/>"/>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr>
                                        </c:if>
                                        <c:if test="${EditUserForm.authType eq 'A-Select'}">
                                        <tr>
                                            <td class="tbForm1" colspan="2">
                                                <img src="/images/spacer.gif" height="110">
                                            </td>
                                        </tr>
                                        </c:if>
                                    </c:if>
                                    <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'USER'}">
                                    <!-- Regular user always sees as read only -->
                                    <tr>
                                        <td class="tbForm1" nowrap colspan="4" id="dormantPeriodTD">
                                            <logic:notEmpty name="EditUserForm" property="dormantPeriodGP">
                                                <c:if test="${EditUserForm.dormantPeriodGP eq '0'}">
                                                <bean:message key="drac.security.userManagement.detail.noDormancy"/>
                                                </c:if>
                                                <c:if test="${EditUserForm.dormantPeriodGP gt '0'}">
                                                <bean:message key="drac.security.userManagement.detail.dormancy"/>
                                                <b>${EditUserForm.dormantPeriodGP}</b>
                                                <bean:message key="drac.security.globalPolicy.inactiveDays"/>
                                                </c:if>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="dormantPeriod">
                                                <c:if test="${EditUserForm.dormantPeriod eq '0'}">
                                                <bean:message key="drac.security.userManagement.detail.noDormancy"/>
                                                </c:if>
                                                <c:if test="${EditUserForm.dormantPeriod gt '0'}">
                                                <bean:message key="drac.security.userManagement.detail.dormancy"/>
                                                <b><font color="red">${EditUserForm.dormantPeriod}</font></b>
                                                <bean:message key="drac.security.globalPolicy.inactiveDays"/>
                                                </c:if>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr id="internalAccountRow1">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.inactivityPeriod"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="inactivityPeriodTD">
                                            <logic:notEmpty name="EditUserForm" property="inactivityPeriodGP">
                                                <b>${EditUserForm.inactivityPeriodGP}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="inactivityPeriod">
                                                <b><font color="red">${EditUserForm.inactivityPeriod}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <c:if test="${EditUserForm.inactivityMetric eq '0'}">
                                                <bean:message key="drac.security.userManagement.create.seconds"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.inactivityMetric eq '1'}">
                                                <bean:message key="drac.security.userManagement.create.minutes"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.inactivityMetric eq '2'}">
                                                <bean:message key="drac.security.userManagement.create.hours"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.inactivityMetric eq '3'}">
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </c:if>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.lockoutPeriod"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="lockoutPeriodTD">
                                            <logic:notEmpty name="EditUserForm" property="lockoutPeriodGP">
                                                <b>${fn:escapeXml(EditUserForm.lockoutPeriodGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="lockoutPeriod">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.lockoutPeriod)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <c:if test="${EditUserForm.lockoutMetric eq '0'}">
                                                <bean:message key="drac.security.userManagement.create.seconds"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.lockoutMetric eq '1'}">
                                                <bean:message key="drac.security.userManagement.create.minutes"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.lockoutMetric eq '2'}">
                                                <bean:message key="drac.security.userManagement.create.hours"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.lockoutMetric eq '3'}">
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </c:if>
                                        </td>
                                    </tr>
                                    <tr id="internalAccountRow2">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.maxInvalidLoginAttempts"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="maxInvalidLoginAttemptsTD">
                                            <logic:notEmpty name="EditUserForm" property="maxInvalidLoginAttemptsGP">
                                                <b>${fn:escapeXml(EditUserForm.maxInvalidLoginAttemptsGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="maxInvalidLoginAttempts">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.maxInvalidLoginAttempts)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            &nbsp;
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordHistorySize"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="passwordHistorySizeTD">
                                            <logic:notEmpty name="EditUserForm" property="passwordHistorySizeGP">
                                                <b>${fn:escapeXml(EditUserForm.passwordHistorySizeGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="passwordHistorySize">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.passwordHistorySize)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                    <tr id="internalAccountRow3">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordAging"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="passwordAgingTD">
                                            <logic:notEmpty name="EditUserForm" property="passwordAgingGP">
                                                <b>${fn:escapeXml(EditUserForm.passwordAgingGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="passwordAging">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.passwordAging)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <bean:message key="drac.security.userManagement.create.days"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordExpirationNotification"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="passwordExpirationNotificationTD">
                                            <logic:notEmpty name="EditUserForm" property="passwordExpirationNotificationGP">
                                                <b>${fn:escapeXml(EditUserForm.passwordExpirationNotificationGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="passwordExpirationNotification">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.passwordExpirationNotification)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <bean:message key="drac.security.userManagement.create.days"/>
                                        </td>
                                    </tr>
                                    <tr id="lockedIPsRow">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.lockedClientIPs"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="lockedClientIPsTD">
                                            <logic:notEmpty name="EditUserForm" property="lockedClientIPsGP">
                                            <logic:iterate id="lockedClientIPsGP" name="EditUserForm" property="lockedClientIPsGP">
                                                <b>${fn:escapeXml(lockedClientIPsGP)}</b><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="lockedClientIPs">
                                            <logic:iterate id="lockedClientIPs" name="EditUserForm" property="lockedClientIPs">
                                                <b><font color="red">${fn:escapeXml(lockedClientIPs)}</b></font><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                            <logic:empty name="EditUserForm" property="lockedClientIPsGP">
                                                <logic:empty name="EditUserForm" property="lockedClientIPs">
                                                    <b><bean:message key="drac.text.none"/></b>
                                                </logic:empty>
                                            </logic:empty>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="22">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <c:if test="${EditUserForm.authType eq 'A-Select'}">
                                    <tr>
                                        <td class="tbForm1" colspan="2" align="right">
                                            <img src="/images/spacer.gif" height="100">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <tr id="infoForLocalAndGlobalPolicy" style="display:none">
                                        <td class="tbForm1" nowrap colspan="4">
                                            <bean:message key="drac.security.userManagement.detail.footnoteLocalandGlobal0"/><br>
                                            <bean:message key="drac.security.userManagement.detail.footnoteLocalandGlobal1"/>
                                        </td>
                                    </tr>
                                    <tr id="infoForLocalPolicy" style="display:none">
                                        <td class="tbForm1" nowrap colspan="4">
                                            <bean:message key="drac.security.userManagement.detail.footnoteLocal"/>
                                        </td>
                                    </tr>
                                    <tr id="infoForGlobalPolicy">
                                        <td class="tbForm1" nowrap colspan="4">
                                            <bean:message key="drac.security.userManagement.detail.footnoteGlobal"/>
                                        </td>
                                    </tr>
                                    </c:if>
                                </logic:present>

                                <logic:notPresent name="editable">
                                    <!-- User sees read only -->
                                    <tr>
                                        <td class="tbForm1" nowrap colspan="4" id="dormantPeriodTD">
                                            <logic:notEmpty name="EditUserForm" property="dormantPeriodGP">
                                                <c:if test="${EditUserForm.dormantPeriodGP eq '0'}">
                                                <bean:message key="drac.security.userManagement.detail.noDormancy"/>
                                                </c:if>
                                                <c:if test="${EditUserForm.dormantPeriodGP gt '0'}">
                                                <bean:message key="drac.security.userManagement.detail.dormancy"/>
                                                <b>${fn:escapeXml(EditUserForm.dormantPeriodGP)}</b>
                                                <bean:message key="drac.security.globalPolicy.inactiveDays"/>
                                                </c:if>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="dormantPeriod">
                                                <c:if test="${EditUserForm.dormantPeriod eq '0'}">
                                                <bean:message key="drac.security.userManagement.detail.noDormancy"/>
                                                </c:if>
                                                <c:if test="${EditUserForm.dormantPeriod gt '0'}">
                                                <bean:message key="drac.security.userManagement.detail.dormancy"/>
                                                <b><font color="red">${fn:escapeXml(EditUserForm.dormantPeriod)}</font></b>
                                                <bean:message key="drac.security.globalPolicy.inactiveDays"/>
                                                </c:if>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                    <c:if test="${EditUserForm.authType eq 'Internal'}">
                                    <tr id="internalAccountRow1">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.inactivityPeriod"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="inactivityPeriodTD">
                                            <logic:notEmpty name="EditUserForm" property="inactivityPeriodGP">
                                                <b>${fn:escapeXml(EditUserForm.inactivityPeriodGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="inactivityPeriod">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.inactivityPeriod)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <c:if test="${EditUserForm.inactivityMetric eq '0'}">
                                                <bean:message key="drac.security.userManagement.create.seconds"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.inactivityMetric eq '1'}">
                                                <bean:message key="drac.security.userManagement.create.minutes"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.inactivityMetric eq '2'}">
                                                <bean:message key="drac.security.userManagement.create.hours"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.inactivityMetric eq '3'}">
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </c:if>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.lockoutPeriod"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="lockoutPeriodTD">
                                            <logic:notEmpty name="EditUserForm" property="lockoutPeriodGP">
                                                <b>${fn:escapeXml(EditUserForm.lockoutPeriodGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="lockoutPeriod">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.lockoutPeriod)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <c:if test="${EditUserForm.lockoutMetric eq '0'}">
                                                <bean:message key="drac.security.userManagement.create.seconds"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.lockoutMetric eq '1'}">
                                                <bean:message key="drac.security.userManagement.create.minutes"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.lockoutMetric eq '2'}">
                                                <bean:message key="drac.security.userManagement.create.hours"/>
                                            </c:if>
                                            <c:if test="${EditUserForm.lockoutMetric eq '3'}">
                                                <bean:message key="drac.security.userManagement.create.days"/>
                                            </c:if>
                                        </td>
                                    </tr>
                                    <tr id="internalAccountRow2">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.maxInvalidLoginAttempts"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="maxInvalidLoginAttemptsTD">
                                            <logic:notEmpty name="EditUserForm" property="maxInvalidLoginAttemptsGP">
                                                <b>${fn:escapeXml(EditUserForm.maxInvalidLoginAttemptsGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="maxInvalidLoginAttempts">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.maxInvalidLoginAttempts)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            &nbsp;
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordHistorySize"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="passwordHistorySizeTD">
                                            <logic:notEmpty name="EditUserForm" property="passwordHistorySizeGP">
                                                <b>${fn:escapeXml(EditUserForm.passwordHistorySizeGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="passwordHistorySize">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.passwordHistorySize)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                    </tr>
                                    <tr id="internalAccountRow3">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordAging"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="passwordAgingTD">
                                            <logic:notEmpty name="EditUserForm" property="passwordAgingGP">
                                                <b>${fn:escapeXml(EditUserForm.passwordAgingGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="passwordAging">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.passwordAging)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <bean:message key="drac.security.userManagement.create.days"/>
                                        </td>
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.passwordExpirationNotification"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="passwordExpirationNotificationTD">
                                            <logic:notEmpty name="EditUserForm" property="passwordExpirationNotificationGP">
                                                <b>${fn:escapeXml(EditUserForm.passwordExpirationNotificationGP)}</b>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="passwordExpirationNotification">
                                                <b><font color="red">${fn:escapeXml(EditUserForm.passwordExpirationNotification)}</font></b>
                                            </logic:notEmpty>
                                        </td>
                                        <td class="tbForm1" nowrap align="left">
                                            <bean:message key="drac.security.userManagement.create.days"/>
                                        </td>
                                    </tr>
                                    <tr id="lockedIPsRow">
                                        <td class="tbForm1" nowrap>
                                            <bean:message key="drac.security.userManagement.create.lockedClientIPs"/>
                                        </td>
                                        <td class="tbForm1" nowrap align="right" id="lockedClientIPsTD">
                                            <logic:notEmpty name="EditUserForm" property="lockedClientIPsGP">
                                            <logic:iterate id="lockedClientIPsGP" name="EditUserForm" property="lockedClientIPsGP">
                                                <b>${fn:escapeXml(lockedClientIPsGP)}</b><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                            <logic:notEmpty name="EditUserForm" property="lockedClientIPs">
                                            <logic:iterate id="lockedClientIPs" name="EditUserForm" property="lockedClientIPs">
                                                <b><font color="red">${fn:escapeXml(lockedClientIPs)}</b></font><br>
                                            </logic:iterate>
                                            </logic:notEmpty>
                                            <logic:empty name="EditUserForm" property="lockedClientIPsGP">
                                                <logic:empty name="EditUserForm" property="lockedClientIPs">
                                                    <b><bean:message key="drac.text.none"/></b>
                                                </logic:empty>
                                            </logic:empty>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tbForm1" colspan="2">
                                            <img src="/images/spacer.gif" height="22">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <c:if test="${EditUserForm.authType eq 'A-Select'}">
                                    <tr>
                                        <td class="tbForm1" colspan="2" align="right">
                                            <img src="/images/spacer.gif" height="100">
                                        </td>
                                    </tr>
                                    </c:if>
                                    <tr id="infoForLocalAndGlobalPolicy" style="display:none">
                                        <td class="tbForm1" nowrap colspan="4">
                                            <bean:message key="drac.security.userManagement.detail.footnoteLocalandGlobal0"/><br>
                                            <bean:message key="drac.security.userManagement.detail.footnoteLocalandGlobal1"/>
                                        </td>
                                    </tr>
                                    <tr id="infoForLocalPolicy" style="display:none">
                                        <td class="tbForm1" nowrap colspan="4">
                                            <bean:message key="drac.security.userManagement.detail.footnoteLocal"/>
                                        </td>
                                    </tr>
                                    <tr id="infoForGlobalPolicy">
                                        <td class="tbForm1" nowrap colspan="4">
                                            <bean:message key="drac.security.userManagement.detail.footnoteGlobal"/>
                                        </td>
                                    </tr>
                                </logic:notPresent>
                                </table>
                            </div>  <!-- end div account policy details -->

                        </div>
                    </td>
                </tr>
            </tbody>
            </table>
            </td>
        </tr>

        <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
        <tr>
            <td>
                <!-- Contents -->
                <table cellspacing="0" cellpadding="5" border="0" align="center">
                <tbody>
                    <tr>
                        <td>
                            <html:submit tabindex="97" property="Edit"  onclick="doSubmit()">
                                <bean:message key="drac.text.submit"/>
                            </html:submit>
                        </td>
                        <td>
                            <html:reset tabindex="98" property="Reset"  >
                            <bean:message key="drac.text.reset"/>
                            </html:reset>
                        </td>

                        <%-- permission has been checked and passed through via 'editable' bean (flag). --%>
                        <td>
                            <html:button tabindex="99" onclick="return confirmDelete('${EditUserForm.webSafeName}');" property="Delete"  >
                                <bean:message key="drac.text.delete"/>
                            </html:button>
                        </td>

                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
    </tbody>
    </table>
</html:form>

<script language="javascript">

function confirmDelete(text)
{
    var agree=confirm('<bean:message key="drac.security.userManagement.delete.confirm"/>' + "\n" + unescape(text));
    if (agree)
    {
        // delete user command is not posted via form submission, so token must be added to the URI here: 
        location.href="/management/userManagement/delete.do?uid="+text+"&CSRFToken="+"<c:out value='${sessionScope["CSRFToken"]}'/>";
    } 
    else 
    {
        return false;
    }
}

function doSubmit()
{
    setAllSelected(document.getElementById('userGroupsMembers'));
    setAllSelected(document.getElementById('lockedClientIPsList'));
}

function setAllSelected(elem)
{
    if (elem)
    {
        for (var i=0; i < elem.options.length; i++)
            elem.options[i].selected = true;
    }
}

function handleChangeAccountState() {
    obj = document.getElementById("accountStateSelect");

    if (obj != null) {
        if (obj.value == "enabled") {
            obj = document.getElementById("disabledLayer");
            obj.style.display = "none";
        } else {
            obj = document.getElementById("disabledLayer");
            obj.style.display = "";
        }
    }
}

function sortByValue(a, b) {
    var x = a.value.toLowerCase();
    var y = b.value.toLowerCase();
    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
}

function moveItems(fromId, toId) {
    var fromElem = document.getElementById(fromId);
    var toElem = document.getElementById(toId);

    // Working variables.
    var newDestList = new Array(toElem.options.length);
    var len = 0;

    if (fromElem && toElem) {
        // Populate the working list.
        for (len=0; len < toElem.options.length; len++) {
            if (toElem.options[len] != null) {
                newDestList[len] = new Option(toElem.options[len].text, toElem.options[len].value, toElem.options[len].defaultSelected, toElem.options[len].selected);
            }
        }

        // Decide what has been selected and incorporate into working list.
        for (var i=0; i < fromElem.options.length; i++) {
            if (fromElem.options[i].selected) {
                // Incorporate into working list.
                newDestList[len] = new Option(fromElem.options[i].text, fromElem.options[i].value, fromElem.options[i].defaultSelected, fromElem.options[i].selected);
                len++;
            }
        }

        // Sort the working list.
        newDestList.sort(sortByValue);

        // Populate the destination list with the items from the working list.
        for (var j=0; j < newDestList.length; j++) {
            if (newDestList[j] != null) {
                toElem.options[j] = newDestList[j];
            }
        }

        // Remove source list selected elements.
        for (var i=fromElem.options.length-1; i >= 0; i--) {
            if ( fromElem.options[i] != null && ( fromElem.options[i].selected == true ) ) {
//fromElem.options[i].selected = false;
                fromElem.options[i] = null;
            }
        }

        // maybe --> fromElem.options.selectedIndex = 0;

        // Make sure all options are selected, src and dest lists.
        // This is needed so that when swapping entries from one list to another and vice versa
        // the items need to be selected to get saved on the server side.
        for (var i=fromElem.options.length-1; i >= 0; i--) {
            fromElem.options[i].selected = true;
        }
        for (var i=toElem.options.length-1; i >= 0; i--) {
            toElem.options[i].selected = true;
        }
    }
}

function viewUserGroup(elemId)
{
    var elem = document.getElementById(elemId);
    if (elem && elem.selectedIndex >= 0) {
        window.location = "/management/userGroupManagement/queryUserGroup.do?ugName=" + escape(elem.options[elem.selectedIndex].value);
    }
    return false;
}

function removeIPItems(fromId) {
    var fromElem = document.getElementById(fromId);

    // Remove source list selected elements.
    for (var i=fromElem.options.length-1; i >= 0; i--) {
        if ( fromElem.options[i] != null && ( fromElem.options[i].selected == true ) ) {
            if (fromElem.options[i].selected) {
                fromElem.options[i] = null;
            }
        }
    }

//    fromElem.options.selectedIndex = 0;
}

function moveIPItems(fromId, toId) {
    var regexIP = /^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.){3}(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$/;
    var fromElem = document.getElementById(fromId);
    var toElem = document.getElementById(toId);

    // Working variables.
    var destList = document.getElementById(toId);
    var newDestList = new Array(destList.options.length);
    var len = 0;

    if (fromElem && toElem) {
        // Populate the working list.
        for (len=0; len < destList.options.length; len++) {
            if (destList.options[len] != null) {
                newDestList[len] = new Option(destList.options[len].text, destList.options[len].value, destList.options[len].defaultSelected, destList.options[len].selected);
            }
        }

        // Decide what has been selected and incorporate into working list.
        if (fromElem.value.match(regexIP)) {
            newDestList[len] = new Option(fromElem.value, fromElem.value);

            // Sort the working list.
            newDestList.sort(sortByValue);

            // Populate the destination list with the items from the working list.
            for (var j=0; j < newDestList.length; j++) {
                if (newDestList[j] != null) {
                    toElem.options[j] = newDestList[j];
                }
            }
            fromElem.value = "";
        } else {
            alert("That is not a valid IP address.");
            fromElem.focus();
        }
    }
}

// onload
handleChangeAccountState();

//
// Decide if the user is using the global policy.
// Look at all global policy elements.
//
function checkWhichPolicyBeingUsed(text) {
    var usingGlobalPolicy = false;
    var usingLocalPolicy = false;
    var usageIndicator = "";

    if (document.getElementById("inactivityPeriodTD") == null) {
           //document.UserForm.authenticationType.value == "A-Select") {
        if (document.getElementById("dormantPeriodTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
    } else {
        if (document.getElementById("dormantPeriodTD").innerHTML.indexOf(text) == -1) {
            // This element is not displaying the local account policy value;
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("inactivityPeriodTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("maxInvalidLoginAttemptsTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("lockoutPeriodTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("lockedClientIPsTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("passwordAgingTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("passwordExpirationNotificationTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
        if (document.getElementById("passwordHistorySizeTD").innerHTML.indexOf(text) == -1) {
            usingGlobalPolicy = true;
        } else usingLocalPolicy = true;
    }

    if (usingLocalPolicy && usingGlobalPolicy) {
        usageIndicator = "3";
    } else if (usingLocalPolicy) {
        usageIndicator = "2";
    } else {
        usageIndicator = "1";
    }

    return usageIndicator;
}

//
// Decide which footnote to display on the Account Policy tab.
// Will either be the one regarding mixed (local and global) policy or
// local policy only or global policy only.
//
function toggleDisplayFootnote() {
    var policyUsage;

    // Check if we have at least one policy details element on the page.
    // We may not have if the user is not able to view this users details.
    if (document.getElementById("dormantPeriodTD") != null) {
        // Browser check included here as I could not find how to use id tag in the notEmpty Struts tag.
        if (navigator.userAgent.indexOf("MSIE") != -1) {
            policyUsage = checkWhichPolicyBeingUsed("FONT color=red");
        } else if (navigator.userAgent.indexOf("Firefox") != -1) {
            policyUsage = checkWhichPolicyBeingUsed("font color=\"red\"");
        }
        
        if (policyUsage == "3") {
            // Account Policy elements are defined locally and globally.
            document.getElementById("infoForLocalAndGlobalPolicy").style.display = "";
            document.getElementById("infoForGlobalPolicy").style.display = "none";
            document.getElementById("infoForLocalPolicy").style.display = "none";
        } else if (policyUsage == "2") {
            // Account Policy elements are defined locally only.
            document.getElementById("infoForLocalAndGlobalPolicy").style.display = "none";
            document.getElementById("infoForGlobalPolicy").style.display = "none";
            document.getElementById("infoForLocalPolicy").style.display = "";
        } else if (policyUsage == "1") {
            // Account Policy elements are defined globally only.
            document.getElementById("infoForLocalAndGlobalPolicy").style.display = "none";
            document.getElementById("infoForGlobalPolicy").style.display = "";
            document.getElementById("infoForLocalPolicy").style.display = "none";
        }
    }
}

// onload
toggleDisplayFootnote();

</script>
<%@ include file="/common/footer.jsp" %>

