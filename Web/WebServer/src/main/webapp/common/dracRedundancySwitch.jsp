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

<%@ page session="true" errorPage="/common/dracError.jsp" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /common/dracRedundancySwitch.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page displayed when the server is in progress of performing a
 *  redundancy switch over.
 *
 ****************************************************************************/

String pageRef = "drac.redundancy.switchInProgress";

%>


<%@ include file="/common/header_struts.jsp" %>

<table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align="center" class="gen">
            <table width="50%" align="center">
                <tr><td align="left">
                    <font color="red"><b><h4><bean:message key="drac.error.redundacy.switchInProgress.1"/></h4><p><p>
                    <bean:message key="drac.error.redundacy.switchInProgress.2"/></b></font>
                </tr></td>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
</table>
<%@ include file="/common/footer.jsp"%>

