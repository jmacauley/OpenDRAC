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

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />
    <tr>
        <td>
          <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
            <tbody>
                <tr align='left'>
                    <td  class="tbForm1" nowrap>
                      <b><bean:message key="drac.schedule.create.starttime"/></b>
                    </td>
                    <td  class="tbForm1" nowrap>
                      <!-- Avoid spaces and new lines between input and </a> -->
                      <input id="startdate" name="startdate" size="30" class="gen"
                        type="text" onfocus="JavaScript:dateFocus(this);"
                        onblur="JavaScript:dateBlur(this);"
                        onchange="JavaScript:dateChange(this);"
                        value="" onkeydown="JavaScript:hideCalendar();"
                        tabindex="2" /><a href="JavaScript:void(0);" onclick="JavaScript:dateFocus(document.getElementById('startdate'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                        &nbsp;&nbsp;
                        <input id="starttime" name="startTime" size="11" class="gen" type="text" onchange="JavaScript:onTimeChange(this);" tabindex="3" /><a href="JavaScript:void(0);" onclick="JavaScript:startTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                        <div id="startTimeLayer" style="position:absolute; z-index:10; overflow:auto; display:none;" onmouseover="javascript:startTimeMenu.overMenu(true);" onmouseout="JavaScript:startTimeMenu.overMenu(false);">
                        <select id="starttimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:startTimeMenu.textSet(this.value);" onkeypress="JavaScript:startTimeMenu.comboKey();">
                            <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                                <option value="<bean:write name="timeStrChoice" property="value" />">
                                    <bean:write name="timeStrChoice" property="label" />
                                </option>
                            </logic:iterate>
                        </select>
                    </td>
                </tr>
                <tr>
                  <td  class="tbForm1" nowrap>
                    <b><bean:message key="drac.schedule.create.endtime"/></b>
                  </td>
                  <td  class="tbForm1" nowrap>
                    <input id="enddate" name="enddate" size="30" class="gen"
                        type="text" onfocus="JavaScript:dateFocus(this);"
                        onblur="JavaScript:dateBlur(this);"
                        onchange="JavaScript:dateChange(this);"
                        value="" onkeydown="JavaScript:hideCalendar();"
                        tabindex="4"/><a href="JavaScript:void(0)" onclick="JavaScript:dateFocus(document.getElementById('enddate'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                    &nbsp;&nbsp;
                    <input id="endtime" name="endTime" size="11" class="gen" type="text" onchange="JavaScript:onTimeChange(this);" tabindex="3" /><a href="JavaScript:void(0);" onclick="JavaScript:endTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                    <div id="endTimeLayer" style="position:absolute; z-index:10; overflow:auto; display:none;" onmouseover="javascript:endTimeMenu.overMenu(true);" onmouseout="JavaScript:endTimeMenu.overMenu(false);">
                    <select id="endtimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:endTimeMenu.textSet(this.value);" onkeypress="JavaScript:endTimeMenu.comboKey();">
                        <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                            <option value="<bean:write name="timeStrChoice" property="value" />">
                                <bean:write name="timeStrChoice" property="label" />
                            </option>
                        </logic:iterate>
                    </select>
                  </td>
                </tr>
            </tbody>
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

<table align="center">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr>
        <td>
            <html:submit property="Query" value="Query"  />
         </td>
        <td>
            <input type="button" name="button2" value='<c:out value="${session.getValue("drac.text.reset")}" />' onclick="initializeDate()">
        </td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
</table>

<script language="javascript">


function initializeDate() {
    setStartDateNow();
    setEndDateNow();

    setStartTimeObj(new Date(0, 0, 0, 0, 0, 0, 0));
    setEndTimeNow();
}

initCalendar("startdate", "starttime", "enddate", "endtime", "duration", '<c:out value="${myLanguage}" />', "long", Digital);
// Initialize the pulldown start/end time menues.
var startTimeMenu = new EditableList('starttime', 'startTimeLayer', 'starttimes');

var endTimeMenu = new EditableList('endtime', 'endTimeLayer', 'endtimes');

// I think this is needed for the pulldown menues to function correctly.
function timeMouseSelect(e)
{
startTimeMenu.mouseSelect(0);
endTimeMenu.mouseSelect(0);
}

document.onmousedown = timeMouseSelect;

startTimeMenu.setChangeListener(timeChangeListener);
endTimeMenu.setChangeListener(timeChangeListener);

// refresh the dates
initializeDate();

</script>