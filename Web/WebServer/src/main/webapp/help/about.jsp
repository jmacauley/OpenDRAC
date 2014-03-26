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
 * File: /help/browser.jsp
 *
 * Description:
 *   This file contains browser compatibility information.
 *
 ****************************************************************************/

String pageRef = "drac.help.about";

String aboutFile = (String) session.getAttribute("drac.help.about.file");

%>

<%@ include file="/common/header_struts.jsp" %>

<% Map releaseInfo = (Map)request.getAttribute("releaseInfo");
   if (releaseInfo != null && releaseInfo.size() > 0)
   {
%>
<table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
    <tbody>
      <tr>
        <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                  <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                  <td class="tbtbot"><center><b>Release Information</b></center></td>
                  <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td>
          <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">

<% 
  out.println(createTable(releaseInfo));
%>


          </table>
      </td>
  </tr>
  <tr>
    <td>
        <!-- Drop shadow. -->
        <table border="0" cellpadding="0" cellspacing="0" class="tbl">
            <tbody>
            <tr>
              <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
              <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
              <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
            </tr>
            </tbody>
        </table>
        </td>
      </tr>
    </tbody>
</table>
<%}%>

<table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
  <tr> 
    <td align="left" valign="middle">
      <jsp:include page="<%= aboutFile %>" />
      <p></p>
      <jsp:include page="/META-INF/NOTICE.html" />
    </td>
  </tr>
</table> 

<br />


<%!
   private static String createTable(Map map)
   {
      StringBuffer sb = new StringBuffer();

      // Generate the table rows

      Iterator imap = map.entrySet().iterator();
      String rowType = "row1";
      while (imap.hasNext()) {
         Map.Entry entry = (Map.Entry) imap.next();
         String key = (String) entry.getKey();
         String value = (String) entry.getValue();
         sb.append("<tr>");
         sb.append("<td class=\"" + rowType + "\" nowrap><b>" + key + "</b></td>");
         sb.append("<td class=\"" + rowType + "\" nowrap>" + value + "</td>");
         sb.append("</tr>");

         if ("row1".equals(rowType) )
         {
             rowType = "row2";
         }
         else
         {
             rowType = "row1";
         }
      }

      // Return the generated HTML
      return sb.toString();
   }
%>


<%@ include file="/common/footer.jsp" %>
