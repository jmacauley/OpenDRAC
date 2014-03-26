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
  <p>"Endpoints" identify network access points that can be dynamically interconnected to provide bandwidth between two points within the network.  The "<i>List Endpoints</i>" functionality provides a mechanism to view all endpoints accessible within your user profile.</p>
  <p>The "<i>Endpoint Filter</i>" provides a mechanism to restrict the set of endpoints returned during this query.
  <ul>
    <li>
    "<i>Member Group</i>" provides the ability to filter based on a specific user group in your user profile.  Specifying the "All Member Groups" option will return a list of all endpoints accessible from your profile.
    </li>
    <br />  
    <li>
    "<i>Endpoint Layer</i>" provides the ability to filter based on the layer of service provided by the endpoint.  "<i>Layer 2</i>" can be specified to view endpoints providing Ethernet services.  "<i>Layer 1</i>" can be specified to view endpoints providing SONET/SDH services.
    </li>
  </ul>
  </p>
</span>
