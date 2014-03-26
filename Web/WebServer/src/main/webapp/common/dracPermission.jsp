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

<%@ page isErrorPage="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /common/dracPermission.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page displayed when user has no permission to perform the previous action
 *
 ****************************************************************************/

String pageRef = "drac.error.security.title";

%>

<%@ include file="/common/header_struts.jsp" %>

<script type="text/javascript" src="/scripts/handleExpand.js"></script>
    <!-- This table encompasses the contents of the page header. -->
<table border="0" cellpadding="0" cellspacing="0" width="90%" align="center">

<table width="100%" cellspacing="10" cellpadding="0" border="0">
    <tr>
        <td>
            <span class="gen"><bean:message key="drac.error.security"/></span><P>
             <h4><font color="red">${fn:escapeXml(requestScope['errorMessage'])}</font></h4>
             <logic:notEmpty name="errorCode" scope="request">
                <br>
                <b><bean:message key="drac.error.problem.code"/>:</b> ${requestScope['errorCode']}
             </logic:notEmpty>
        </td>
    </tr>
    <tr>
        <td>
            <span class="gen"><a href="/welcome.do"><bean:message key="drac.error.return"/></a></span>
        </td>
    </tr>
</table>
<%@ include file="/common/footer.jsp"%>

