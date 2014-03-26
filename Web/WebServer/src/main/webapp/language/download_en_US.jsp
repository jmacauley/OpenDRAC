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

<script src="/scripts/ext/deployJava.js"></script>
<h2>Downloads:</h2>
<br/>


<table width="80%" cellspacing="1" cellpadding="5" border="0" align="center" class="tbmain">
  <tr>
    <th align="center">
      <c:out value='${sessionScope["drac.help.download.header.item"]}'/>
    </th>
    <th align="center">
      <c:out value='${sessionScope["drac.help.download.header.description"]}'/>
    </th>
    <th align="center">
      <c:out value='${sessionScope["drac.help.download.header.link"]}'/>
    </th>
  </tr>
  <tr>
    <td nowrap valign="top" align="left" class="row2">
      <b>Web client</b>
    </td>
    <td valign="top" class="row2">
      Manual for this Web Client
    </td>
    <td valign="top" align=""left"" class="row2">
      <a href="/downloads/web_client_manual_opendrac.pdf" title="Download Manual for the Web Client">Download Manual</a> <br/>
      <br/> 
    </td>
  </tr>  
  <tr>
    <td nowrap valign="top" align="left" class="row1">
      <b>Administration Console</b>
    </td>
    <td valign="top" class="row1">
      Administration console for your local workstation.<br/><br/>
      Manual for the Administration Console
    </td>
    <td valign="top" align=""left"" class="row1" nowrap="nowrap">
      <a href="/downloads/AdminConsole-<%= System.getProperty("org.opendrac.version") %>.jar"  alt="Download AdminConsole-<%= System.getProperty("org.opendrac.version") %>.jar" title="Download AdminConsole-<%= System.getProperty("org.opendrac.version") %>.jar">Download Console</a><br/><br/>
      <a href="/downloads/adminconsole_manual_opendrac.pdf" title="Download Manual for the Admin Console">Download Manual</a> <br/>
    </td>
  </tr>
  <tr>
    <td nowrap valign="top" align="left" class="row2">
      <b>Automation Tool</b>
    </td>
    <td valign="top" class="row2"> 
      Automation Tool is a web service client application used to send request to the OpenDRAC 
      web service. These requests can reserve dynamic lightpath schedules, cancel schedules, or view the status 
      of schedules.
    </td>
    <td valign="top" align=""left"" class="row2">
      <a href="/downloads/OpenDRAC-AutomationTool.zip" alt="Download OpenDRAC-AutomationTool.zip" title="Download OpenDRAC-AutomationTool.zip">Download Tool</a>
    </td>
  </tr>
  <tr>
    <td nowrap valign="top" align="left" class="row1">
      <b>Web Services SDK</b>
    </td>
    <td valign="top" class="row1">
      Web Services SDK contains web services WSDL files and Java based web services client stubs.
    </td>
    <td valign="top" align="left" class="row1">
      <a href="/downloads/OpenDracWebServicesClientSDK_v3.0.0.tar.gz" alt="Download OpenDracWebServicesClientSDK_v3.0.0.tar.gz" title="Download OpenDracWebServicesClientSDK_v3.0.0.tar.gz">Download SDK</a>
    </td>
  </tr>
</table>
<br/>

