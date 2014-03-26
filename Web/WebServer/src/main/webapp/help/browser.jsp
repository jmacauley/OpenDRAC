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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="true" errorPage="/common/dracError.jsp" %>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /help/browser.jsp
 *
 * Description:
 *   This file contains browser compatibility information.
 *
 ****************************************************************************/

String pageRef = "drac.help.browser";

%>

<%@ include file="/common/header_struts.jsp" %>

<table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
  <tr><td><img src="/images/spacer.gif" height="5" /></td></tr>
  <tr>
    <td align="center" valign="middle">
      <span class="gen">
	<c:out value='${sessionScope["drac.help.browser.text"]}'/> <br /><br />
      </span>
      <table width="500" cellspacing="1" cellpadding="5" border="0" align="center" class="tbmain">
        <tr>
          	<th align="center">
	    		<c:out value='${sessionScope["drac.help.browser.header.browser"]}'/>
	  		</th>
            <th align="center">
	   			<c:out value='${sessionScope["drac.help.browser.header.version"]}'/>
	  		</th>
          	<th align="center">
	    		<c:out value='${sessionScope["drac.help.browser.header.link"]}'/>
	 		</th>
	    </tr>
	    <tr>
		  <td class="row1">Chrome</td>
		  <td class="row1" align="center">11.0+</td>
		  <td class="row1" align="center"><a href="http://www.google.com/chrome"><img src="/images/browser-chrome.png" alt="Chrome" ></a></td>
	    </tr>	 
  	    <tr>
		  <td class="row2">Internet Explorer</td>
		  <td class="row2" align="center">8 +</td>
		  <td class="row2" align="center"><a href="http://www.microsoft.com/windows/ie/default.mspx"><img src="/images/browser-ie.png" alt="Internet Explorer"></a></td>
	    </tr>
	    <tr>
		  <td class="row1">Firefox</td>
		  <td class="row1" align="center">4.0.1+</td>
		  <td class="row1" align="center"><a href="http://www.mozilla.com/firefox/"><img src="/images/browser-firefox.png" alt="Firefox"></a></td>
	    </tr>
	    <tr>
		  <td class="row2">Opera</td>
		  <td class="row2" align="center">11.11+</td>
		  <td class="row2" align="center"><a href="http://www.opera.com/"><img src="/images/browser-opera.png" alt="Opera"></a></td>
	    </tr>
	    <tr>
	      <td class="row1">Safari</td>
	   	  <td class="row1" align="center">5.0.5+</td>
	      <td class="row1" align="center"><a href="http://www.apple.com/safari"><img src="/images/browser-safari.png" alt="Safari"  height="42"></a></td>
	    </tr>
      </table>      
    </td>
  </tr>
</table>

<br />

<%@ include file="/common/footer.jsp" %>
