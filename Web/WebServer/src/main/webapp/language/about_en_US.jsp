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

<h2>Overview</h2>
<span class="gen">
<p>The Open Dynamic Resource Allocation Controller (OpenDRAC) is an implementation of a grid-like resource broker providing end application control of network resources.  The primary goal of OpenDRAC is to expose network bandwidth to end user/application control while preventing unauthorized access and resource theft.  Through simplified GUI and programmatic interfaces, OpenDRAC will accelerate the demand for application integration of bandwidth control within the network.  In release 1.0, OpenDRAC provides user and application control of point-to-point gigabit Ethernet "light-paths" at varying bandwidth granularities for Nortel OME and HDX-C network elements.  The following diagram provides a high-level view of the OpenDRAC architecture.</p>
</span>
<center><img src="/language/images/DRAC_architecture_v1_en_US.jpg"></center>
