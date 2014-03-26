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

<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /management/changePassword.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page allows user change their own password
 *
 ****************************************************************************/


String pageRef = "drac.security.changePassword"; %>
<%@ include file="/common/header_struts.jsp" %>

<html:form method="POST" action="/management/changePasswordAction.do" >
<%@ include file="/common/csrf/setCSRFToken.jsp"%>

        <table cellspacing="0" cellpadding="0" border="0" width="650" align="center">
            <tbody>
                <tr>
                    <td><img src="/images/spacer.gif" height="5" /></td>
                </tr>
                <logic:messagesPresent message="true">
                <tr>
                  <td>
                    <table cellspacing="0" cellpadding="5" border="0" align="center">
                    <tbody>
                        <html:messages id="message" message="true">
                        <tr>
                            <td align="center" class="tbForm1" nowrap><font color="red"><b><bean:write name="message"/></b></font></td>
                        </tr>
                        </html:messages>
                        <logic:present name="pwRulesFailed" scope="request">
                        <tr>
                            <td align="center" class="tbForm1" nowrap>
                                <bean:message key="drac.security.changePassword.rules.header0"/>
                                <br><bean:message key="drac.security.changePassword.rules.header1"/>
                                <logic:present name="minPwLength" scope="request">
                                    <br><bean:message key="drac.security.changePassword.rules.minChars" arg0="${requestScope['minPwLength']}"/>
                                </logic:present>
                                <logic:present name="minAlphaChars" scope="request">
                                    <br><bean:message key="drac.security.changePassword.rules.minAlpha" arg0="${requestScope['minAlphaChars']}"/>
                                    <c:if test="${minAlphaChars == 1}">
                                        <bean:message key="drac.security.changePassword.rules.char"/>
                                    </c:if>
                                    <c:if test="${minAlphaChars > 1}">
                                        <bean:message key="drac.security.changePassword.rules.chars"/>
                                    </c:if>
                                </logic:present>
                                <logic:present name="minNumericChars" scope="request">
                                    <br><bean:message key="drac.security.changePassword.rules.minNumeric" arg0="${requestScope['minNumericChars']}"/>
                                    <c:if test="${minNumericChars == 1}">
                                        <bean:message key="drac.security.changePassword.rules.char"/>
                                    </c:if>
                                    <c:if test="${minNumericChars > 1}">
                                        <bean:message key="drac.security.changePassword.rules.chars"/>
                                    </c:if>
                                </logic:present>
                                <logic:present name="minDifferentChars" scope="request">
                                    <br><bean:message key="drac.security.changePassword.rules.minDiffer" arg0="${requestScope['minDifferentChars']}"/>
                                    <c:if test="${minDifferentChars == 1}">
                                        <bean:message key="drac.security.changePassword.rules.char"/>
                                    </c:if>
                                    <c:if test="${minDifferentChars > 1}">
                                        <bean:message key="drac.security.changePassword.rules.chars"/>
                                    </c:if>
                                </logic:present>
                                <logic:present name="mixedCase" scope="request">
                                    <br><bean:message key="drac.security.changePassword.rules.mixedAlpha"/>
                                </logic:present>
                                <logic:present name="minSpecialChars" scope="request">
                                    <br><bean:message key="drac.security.changePassword.rules.minSpecial" arg0="${requestScope['minSpecialChars']}"/>
                                    <c:if test="${minSpecialChars == 1}">
                                        <bean:message key="drac.security.changePassword.rules.char"/>
                                    </c:if>
                                    <c:if test="${minSpecialChars > 1}">
                                        <bean:message key="drac.security.changePassword.rules.chars"/>
                                    </c:if>
                                    <br><bean:message key="drac.security.changePassword.rules.specialChars" arg0="${requestScope['specialChars']}"/>
                                </logic:present>
                                </b>
                            </td>
                        </tr>
                        </logic:present>
                    </tbody>
                    </table>
                  </td>
                </tr>
                </logic:messagesPresent>
                <logic:present name="success" scope="request">
                <tr>
                  <td>
                    <!-- Schedule information contents. -->
                    <table cellspacing="0" cellpadding="5" border="0" align="center">
                    <tbody>
                      <tr>
                        <td align="center" class="tbForm1" nowrap>
                           <font color="green"><b><bean:message key="drac.security.changePassword.success"/></b></font>
                        </td>
                    </tr>
                    </tbody>
                    </table>
                  </td>
                </tr>
                </logic:present>
            </tbody>
        </table>
        <table cellspacing="0" cellpadding="5" border="0" align="center">
            <tr>
                <td class="tbForm1" nowrap align="right">
                    <b><bean:message key="drac.security.changePassword.old"/></b>
                </td>
                <td class="tbForm1" nowrap align="left">
                    <html:password style = "width: 150px" property="oldPassword" tabindex="1" errorStyleClass="invalid"/>
                </td>
            </tr>
            <tr>
                <td class="tbForm1" nowrap align="right">
                    <b><bean:message key="drac.security.changePassword.new1"/></b>
                </td>
                <td class="tbForm1" nowrap align="left">
                    <html:password style = "width: 150px" property="newPassword1" tabindex="2" errorStyleClass="invalid"/>
                </td>
            </tr>
            <tr>
                <td class="tbForm1" nowrap align="right">
                    <b><bean:message key="drac.security.changePassword.new2"/></b>
                </td>
                <td class="tbForm1" nowrap align="left">
                    <html:password style = "width: 150px" property="newPassword2" tabindex="3" errorStyleClass="invalid"/>
                </td>
            </tr>
        </table>
        <table align="center">
            <tr><td><img src="/images/spacer.gif" height="10"/></td></tr>
            <tr>
                <td>
                    <html:submit tabindex="98" property="Create">
                        <bean:message key="drac.text.submit"/>
                    </html:submit>
                </td>
                <td>
                    <html:reset tabindex="99" >
                        <bean:message key="drac.text.reset"/>
                    </html:reset>
                </td>
            </tr>
        </table>

</html:form>

<script language="javascript">

</script>




<%@ include file="/common/footer.jsp" %>

