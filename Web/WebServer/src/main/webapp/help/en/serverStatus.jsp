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

<%@ page errorPage="/common/dracError.jsp" %>

<%
/* OpenDRAC Web GUI */
%>

<span class="gen">
  <p>"<i>OpenDRAC Server Status</i>" provides a summary status of the current OpenDRAC system.</p>
  <ul>
    <li>
    "<i>OpenDRAC Status</i>" - an indication of the status of the OpenDRAC Network Resource Broker.  The normal state for this attribute is "<b>Running</b>."
    </li>
    <br />  
    <li>
    "<i>OpenDRAC Version</i>" - current OpenDRAC software version running on server.
    </li>
    <br />  
    <li>
    "<i>OpenDRAC Build Date</i>" - Build date of the OpenDRAC server software.
    </li>
    <br />
    <li>
    "<i>Web Server</i>" - The version of web server being used to provide client access to OpenDRAC.
    </li>
    <br />
    <li>
    "<i>Web Server Address</i>" - The web server name and port number being used by the client to access OpenDRAC.
    </li>

  </ul>
  </p>
</span>
