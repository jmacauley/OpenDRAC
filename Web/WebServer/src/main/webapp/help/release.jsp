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

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /help/release.jsp
 *
 * Description:
 *   This file contains browser compatibility information.
 *
 ****************************************************************************/

String pageRef = "drac.help.release";

String releaseFile = (String) session.getAttribute("drac.help.release.file");

%>

<%@ include file="/common/header_struts.jsp" %>

<table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
  <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
  <tr>
    <td>
      <jsp:include page="<%= releaseFile %>" />
    </td>
  </tr>
</table>

<br />

<%@ include file="/common/footer.jsp" %>
