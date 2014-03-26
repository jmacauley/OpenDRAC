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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=iso-8859-1"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%String pageRef = "drac.service.alarms.list";%>

<%@ include file="/common/header_struts.jsp" %>

<%@ include file="/common/calendar.jsp" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>
<script type="text/javascript" src="/scripts/handleExpand.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />
<html:form action="/service/listAlarmsAction" method="POST">
<table cellspacing="0" cellpadding="0" border="0" align="center">
       <logic:messagesPresent>
        <tr>
            <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.schedule.create.title.errors"/></b></font></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>
        <tr>
          <td>
            <!-- Schedule information contents. -->
            <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
            <tbody>
              <tr>
                <td align="center" class="tbForm1" nowrap>
                    <html:errors/>
                </td>
            </tr>
            </tbody>
            </table>
          </td>
        </tr>
        <tr>
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
    </logic:messagesPresent>
    <logic:present name="success" scope="request">
          <tr>
             <td>
               <!-- Schedule information contents. -->
               <table cellspacing="0" cellpadding="5" border="0" align="center">
               <tbody>
                 <tr>
                   <td align="center" class="tbForm1" nowrap>
                      <font color="green"><b><bean:message key="drac.general.serverSettings.success"/></b></font>
                   </td>
               </tr>
               </tbody>
               </table>
             </td>
           </tr>
    </logic:present>      

    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align="center" class="gen">
            <bean:message key="drac.service.query.title"/>
        </td>
    </tr>
    <tr>
        <td>
        <!-- Header. -->
        <table border="0" cellpadding="0" cellspacing="0" class="tbt">
            <tbody>
            <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
            <tr>
                <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                <td class="tbtbot"><center><b><bean:message key="drac.service.alarms.query.filter"/></b></center></td>
                <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
            </tr>
            </tbody>
        </table>
        </td>
    </tr>

    <tr>
        <td>
            <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
            <tbody>
                <tr align='left'>
                    <td class="row1" align="left" class="gen">
                        <b><bean:message key="drac.service.query.membergroup" />
                        </b>
                    </td>
                    <td class="row1">
                        <html:select tabindex="1" size="1" property="groupFilter" style="width: 180px">
                            <html:option value="all"><bean:message key="drac.schedule.list.filter.group.all" /></html:option>
                            <html:options property="memberGroupList"/>
                        </html:select>
                    </td>
                </tr>
                <tr align='left'>
                    <td  class="tbForm1" nowrap>
                      <b><bean:message key="drac.schedule.create.starttime"/></b>
                    </td>
                    <td  class="tbForm1" nowrap>
                        <!-- Avoid spaces and new lines between input and </a> -->
                      <html:text tabindex="2" styleId="startdate" property="startDate" size="30" styleClass="gen"
                        onfocus="dCal.dateFocus(this,document.getElementById('starttime'));"
                        onblur="dCal.dateBlur(this);"
                        onchange="dCal.dateChange(this);"
                        value="" onkeydown="dCal.hideCalendar();"
                        /><a href="JavaScript:void(0);" tabindex="-1" onclick="dCal.dateFocus(document.getElementById('startdate'),document.getElementById('starttime'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                        &nbsp;&nbsp;
                        <html:text tabindex="3" styleId="starttime" property="startTime" size="11" styleClass="gen" onchange="JavaScript:onTimeChange(this);" /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:startTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
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
                    <html:text tabindex="4" styleId="enddate" property="endDate" size="30" styleClass="gen"
                        onfocus="dCal.dateFocus(this,document.getElementById('endtime'));"
                        onblur="dCal.dateBlur(this);"
                        onchange="dCal.dateChange(this);"
                        onkeydown="dCal.hideCalendar();"
                        /><a href="JavaScript:void(0)" tabindex="-1" onclick="dCal.dateFocus(document.getElementById('enddate'),document.getElementById('endtime'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                    &nbsp;&nbsp;
                    <html:text tabindex="5" styleId="endtime" property="endTime" size="11" styleClass="gen" onchange="JavaScript:onTimeChange(this);" /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:endTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
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
<div align="center"><bean:message key="drac.service.alarms.query.warning"/></div>
<table align="center">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr>
        <td>
            <html:submit tabindex="98" property="Query" value="Query"  />
         </td>
        <td>
            <input tabindex="99" type="button" name="button2" value='<c:out value='${sessionScope["drac.text.reset"]}'/>' onclick="initializeDate()">
        </td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
</table>
</html:form>


<script language="javascript">


function initializeDate() {
    setStartDateToday();
    setEndDateToday();

    setStartTimePast();
    setEndTimeFuture();
}

dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
startDateId = "startdate";
endDateId = "enddate";
startTimeId = "starttime";
endTimeId = "endtime";
dCal.addListener(dateChange);
//setDatesToday();
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

var mB = new HoldButton("", monthBack);
var mF = new HoldButton("", monthForward);
var yB = new HoldButton("", yearBack);
var yF = new HoldButton("", yearForward);
</script>

<%@ include file="/common/footer.jsp"%>
