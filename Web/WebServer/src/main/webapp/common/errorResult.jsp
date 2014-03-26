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

<table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
  <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
  <tr>
    <td>
          <table width="90%" cellspacing="1" cellpadding="5" border="0" align="center" class="tbmain">
        <tr>
          <th colspan="2">
          	<c:out value='${sessionScope["drac.error.title"]}'/>
          </th>
        </tr>
        <tr>
          <td align="left" class="row1" nowrap>
          	<c:out value='${sessionScope["drac.error.message"]}'/>
          </td>
          <td align="left" class="row1">
            <c:out value="${errorMessage }" />
          </td>	     
        </tr>
        <tr>
          <td align="left" class="row2" nowrap>
          <c:out value='${sessionScope["drac.error.details"]}'/>
          </td>
          <td align="left" class="row2">
            <c:out value="${errorDetails}" />
          </td>	     
        </tr>
      </table>
    </td>
  </tr>
</table>
<br />
