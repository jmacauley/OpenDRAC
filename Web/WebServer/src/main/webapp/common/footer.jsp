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

<%
/*
 * OpenDRAC Web GUI
 * 
 * Filename: footer.jsp
 *
 */

    String localDateLabel = (String) session.getAttribute("drac.header.localDate");
    if (localDateLabel == null)
    {
        localDateLabel = "";
    }

    String localTimeLabel = (String) session.getAttribute("drac.header.localTime");
    if (localTimeLabel == null)
    {
        localTimeLabel = "";
    }


    String serverDateLabel = (String) session.getAttribute("drac.header.serverDate");
    if (serverDateLabel == null)
    {
        serverDateLabel = "";
    }

    String serverTimeLabel = (String) session.getAttribute("drac.header.serverTime");
    if (serverTimeLabel == null)
    {
        serverTimeLabel = "";
    }

    String clientDateLabel = (String) session.getAttribute("drac.header.clientDate");
    if (clientDateLabel == null)
    {
        clientDateLabel = "";
    }

    String clientTimeLabel = (String) session.getAttribute("drac.header.clientTime");
    if (clientTimeLabel == null)
    {
        clientTimeLabel = "";
    }

%>

          <!-- Close the page content table. -->
          </td>
        </tr>
       </table>
    </td>

<c:if test="${!empty sessionScope['userPreferredTimeZoneId']}">
  <tr>
   <td>
   <table border="0" cellpadding="0" cellspacing="0" class="tbb">

        <tr>
            <td class="tbbl">
                <img src="/images/spacer.gif" alt="" height="19" />
            </td>

            <td class="tbbbot" align="left">
                <b>Local: </b><span id="local_date"></span>  <span id="local_time"></span>
            </td>

            <td class="tbbbot" align="center">
                <b>Server: </b><span id="server_date"></span>    <span id="server_time"></span>
            </td>

            <td class="tbbbot" align="right">
                <b>Display: </b><span id="client_date"></span>    <span id="client_time"></span>
            </td>

            <td class="tbbbot">
                <img src="/images/spacer.gif" alt="" height="19" />
            </td>

        </tr>

    </table>

    </td>

  </tr>

</c:if>

  </tr>
</table>

<!-- Create the menu and set behavior parameters. This must be outside the table for menus to function in IE.  -->
<script type="text/javascript">
  var ddmx = new DropDownMenuX('mainMenu');
  ddmx.delay.show = 0;
  ddmx.delay.hide = 400;
  ddmx.position.levelX.left = 2;
  ddmx.init();
</script>


</body>
</html>

