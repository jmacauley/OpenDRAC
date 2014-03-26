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

    <tr>
        <td colspan="2">
        <div id="loadingArea" align="center" style="display:none;position:absolute;left:0px;top:175px!important;top:0px;width:100%">
          <table cellspacing="0" cellpadding="0" border="0" align="center" width="150">
            <tr>
                <td>
                <!-- Header. -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                    <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                    </tr>
                    </tbody>
                </table>
                </td>
            </tr>

            <tr>
                <td>
                    <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm" bgcolor="#EFEFEF">
                    <tbody>
                        <tr>
                            <td class="cat" align="center">&nbsp;<br>
                            <img src="/images/loading_small.gif" align="left"><b><bean:message key="drac.processing.action"/><br>
                            <bean:message key="drac.general.login.loading.msg2"/><br>&nbsp;</b></td>
                        </tr>
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
          </table>
        </div>
        </td>
    </tr>

<script LANGUAGE="JavaScript">
function showLoading(show) {
    if (show) {
        document.getElementById("loadingArea").style.display = "";
    } else {
        document.getElementById("loadingArea").style.display = "none";
    }
}

showLoading(false);

</script>
