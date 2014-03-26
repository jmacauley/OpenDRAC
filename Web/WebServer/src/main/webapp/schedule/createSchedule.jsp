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
<%@ page session="true" errorPage="/common/dracError.jsp"
             import="com.nortel.appcore.app.drac.server.webserver.struts.schedule.form.*"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /schedule/CreateSchedulePage.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page allows user to create a new schedule
 *
 ****************************************************************************/


String pageRef = "drac.schedule.create"; %>


<%@ include file="/common/header_struts.jsp" %>

<%@ include file="/common/calendar.jsp" %>
<script type="text/javascript" src="/scripts/handleExpand.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/scheduleCreate.js"></SCRIPT>
<script type="text/javascript" src="/scripts/tabpane.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<script type="text/javascript" src="/scripts/ajax.js"></script>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>
<script type="text/javascript" src="/scripts/dhtmlwindow.js">

/***********************************************
* DHTML Window Widget- © Dynamic Drive (www.dynamicdrive.com)
* This notice must stay intact for legal use.
* Visit http://www.dynamicdrive.com/ for full source code
***********************************************/

</script>

<%
    // Set the form-bean as an object we can use in any scriplets
    CreateScheduleForm createForm = (CreateScheduleForm) request.getAttribute("CreateScheduleForm");
%>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<html:form method="POST" action="/schedule/handleCreateSchedule.do" onsubmit="return doSubmit()">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>

<table cellspacing="0" cellpadding="0" border="0" width="600" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5" /></td>
        </tr>
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
        <logic:messagesPresent message="true">
        <html:messages id="message" message="true">
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
                    <img valign="middle" src="/images/warning.png">&nbsp;<font color="red"><b><bean:write name="message"/></b></font>
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
        </html:messages>
        </logic:messagesPresent>
        <tr>
            <td align="center"><bean:message key="drac.schedule.create.title"/></td>
        </tr>
        <tr>
            <td>
            <!-- Header. -->
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot"><center><b><bean:message key="drac.schedule.create.title.info"/></b></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>
        <tr>
          <td>
            <!-- Schedule information contents. -->
            <table cellspacing="0" cellpadding="0" border="1" align="center" class="tbForm">
            <tbody>
              <tr>
                <td  class="tbForm1" nowrap>
                  <b><bean:message key="drac.schedule.create.starttime"/></b>
                </td>
                <td  class="tbForm1" valign="middle" nowrap>
                  <!-- Avoid spaces and new lines between input and </a> -->
                  <html:text tabindex="4" styleId="startdate" property="startdate" size="30" styleClass="gen"
                    onfocus="dCal.dateFocus(this,document.getElementById('starttime'));"
                    onblur="dCal.dateBlur(this);"
                    onchange="dCal.dateChange(this);"
                    value=""
                    onkeydown="dCal.hideCalendar();"
                    errorStyleClass="invalid"
                    /><a href="JavaScript:void(0);"
                  tabindex="-1" onclick="dCal.dateFocus(document.getElementById('startdate'), document.getElementById('starttime'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                  &nbsp;&nbsp;
                  <html:text tabindex="5" styleId="starttime" errorStyleClass="invalid" property="startTime" size="11" styleClass="gen"
                    onchange="createSchTimeChange(this);"
                    /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:startTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                  <div id="startTimeLayer" style="position:absolute; z-index:10; overflow:auto; display:none;" onmouseover="javascript:startTimeMenu.overMenu(true);" onmouseout="JavaScript:startTimeMenu.overMenu(false);">
                    <select id="starttimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:startTimeMenu.textSet(this.value);" onkeypress="JavaScript:startTimeMenu.comboKey();">
                        <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                            <option value="<bean:write name="timeStrChoice" property="value" />">
                                <bean:write name="timeStrChoice" property="label" />
                            </option>
                        </logic:iterate>
                    </select>
                  </div>&nbsp;&nbsp;
                  <b><input tabindex="6" id="startNow" name="startNow" type="checkbox" onclick="JavaScript:startNowChange('startNow', startTimeMenu);">&nbsp;<bean:message key="drac.schedule.create.startNow"/></b>
                </td>
              </tr>
                <tr>
                  <td  class="tbForm1" nowrap>
                    <b><bean:message key="drac.schedule.create.endtime"/></b>
                  </td>
                  <td  class="tbForm1" valign="middle" nowrap>
                    <html:text tabindex="7" styleId="enddate" property="enddate" size="30" styleClass="gen"
                        onfocus="dCal.dateFocus(this, document.getElementById('endtime'));"
                        onblur="dCal.dateBlur(this);"
                        onchange="dCal.dateChange(this);"
                        onkeydown="dCal.hideCalendar();"
                        errorStyleClass="invalid"
                        /><a href="JavaScript:void(0)"
                    tabindex="-1" onclick="dCal.dateFocus(document.getElementById('enddate'),document.getElementById('endtime'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                    &nbsp;&nbsp;
                    <html:text tabindex="8" styleId="endtime" property="endTime" errorStyleClass="invalid" size="11" styleClass="gen"
                        onchange="createSchTimeChange(this);"
                        /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:endTimeMenu.activate();"><img src="/images/clock.gif" border="0" align="top" vspace="0" hspace="0"></a>
                    <div id="endTimeLayer" style="position:absolute; z-index:10; overflow:auto; display:none;" onmouseover="javascript:endTimeMenu.overMenu(true);" onmouseout="JavaScript:endTimeMenu.overMenu(false);">
                    <select id="endtimes" size="10" class="gen" style="width: 100px; border-style: none" onclick="JavaScript:endTimeMenu.textSet(this.value);" onkeypress="JavaScript:endTimeMenu.comboKey();">
                        <logic:iterate id="timeStrChoice" name="dracHelper" property="timeStringList" scope="application">
                            <option value="<bean:write name="timeStrChoice" property="value" />">
                                <bean:write name="timeStrChoice" property="label" />
                            </option>
                        </logic:iterate>
                    </select>
                    </div>&nbsp;&nbsp;
                    <input tabindex="9" id ="endNever" name="endNever" type="checkbox" onclick="JavaScript:endNeverChange('endNever', endTimeMenu, 'recurrence');" />&nbsp;<img src="/images/infinity.gif" valign="middle" alt="" />
                    <b><bean:message key="drac.schedule.create.endNever"/></b>
                  </td>
                </tr>
                <tr>
                    <td class="tbForm1" nowrap><b><bean:message key="drac.schedule.create.duration"/></b></td>
                    <td class="tbForm1" align="left">
                    <a href="javascript:void(0)" tabindex="-1" onclick="durationSub()" onmouseout="durSub.handleMouseOut()" onmousedown="durSub.handleMouseDown()" onmouseup="durSub.handleMouseUp()"><img src="/images/arrow_beak_down.gif"></a>
                    <html:text tabindex="10" styleId="duration" property="duration" errorStyleClass="invalid" size="8" styleClass="gen" onchange="modifyEndTime(this);" />
                    <a href="javascript:void(0)" tabindex="-1" onclick="durationAdd()" onmouseout="durAdd.handleMouseOut()" onmousedown="durAdd.handleMouseDown()" onmouseup="durAdd.handleMouseUp()"><img src="/images/arrow_beak_up.gif"></a>
                    <bean:message key="drac.schedule.create.minutes"/>
                    &nbsp;&nbsp;

                    <!-- Find a Time button: not working for L2 vcat, not adjusted for timezone display on response -->
                    <%--
                    <html:button tabindex="11" property="" styleId="findButton"  onclick="showFindTime()">
                        <bean:message key="drac.schedule.create.queryTime.button"/>
                    </html:button>
                    --%>

                    </td>
                </tr>
                <logic:notEmpty name="CreateScheduleForm" property="systemOffsetTime">
                <tr>
                  <td class="tbForm1">&nbsp;</td>
                  <td class="tbForm1" align="left">
                    <bean:message key="drac.schedule.create.offsetMsg" arg0="${CreateScheduleForm.systemOffsetTime}"/>
                  </td>
                </tr>
                </logic:notEmpty>

	      <tr>
                <td class="tbForm1" nowrap><b><bean:message key="drac.schedule.create.srcEndpoint"/></b></td>
                <td class="tbForm1" align="left">
                    <html:select tabindex="17" size ="1" property="srcTna" style="width:300px" styleId="srcTna" styleClass="gen" errorStyleClass="invalid">
                         <html:options property="tnas"/>
                    </html:select>
                </td>
              </tr>

	      <tr>
                <td class="tbForm1" nowrap><b><bean:message key="drac.schedule.create.destEndpoint"/></b></td>
                <td class="tbForm1" align="left">
                    <html:select tabindex="17" size ="1" property="destTna" style="width:300px" styleId="destTna" styleClass="gen" errorStyleClass="invalid">
                         <html:options property="tnas"/>
                    </html:select>
                </td>
              </tr>

	     <tr>
                <td class="tbForm1" align="left"><b><bean:message key="drac.schedule.rate" /></b></td>
                <td align="left" class="tbForm1" nowrap><html:text tabindex="28" maxlength="9" style="width:112px" property="rate" styleId="rate" styleClass="gen" errorStyleClass="invalid" /><b><bean:message key="drac.text.Mbps" /></b></td>
	     </tr>
			<input type="hidden" value="${CreateScheduleForm.srcResGroup}" id="srcGroup" name="srcGroup" />			
			<input type="hidden" value="${CreateScheduleForm.destResGroup}" id="destGroup" name="destGroup" />
			<input type="hidden" value="${CreateScheduleForm.srcResGroup}" id="srcResGroup" name="srcResGroup" />			
			<input type="hidden" value="${CreateScheduleForm.destResGroup}" id="destResGroup" name="destResGroup" />		
								
			<input type="hidden" value="" id="srcSiteBox" name="srcSiteBox" />		
			<input type="hidden" value="" id="destSiteBox" name="destSiteBox" />	
			<input type="hidden" value="" id="fixedSrcLayer" name="layer2" />
			<input type="hidden" value="" id="fixedDestLayer" name="layer2" />
              <tr>
                <td class="tbForm1" nowrap><b><bean:message key="drac.schedule.create.billingGroup"/></b></td>
                <td class="tbForm1" align="left">
                    ${CreateScheduleForm.billingGroup}
                </td>
              </tr>

                <tr>
                    <td class="tbForm1" colspan="2"><img src="/images/spacer.gif" height="3"></td>
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



   
   </tbody>
</table>
<table align="center">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr>
        <td>
            <html:submit tabindex="98" property="Create" styleId="Create">
                <bean:message key="drac.schedule.create.button.create" />
            </html:submit>
        </td>

        <td>
            <html:button tabindex="99" onclick="Javascript: resetFields();" property="">
                <bean:message key="drac.schedule.create.button.reset" />
            </html:button>
        </td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
</table>

    <html:hidden property="billingGroup" styleId="billingGroup" value="${CreateScheduleForm.billingGroup}" />
    <html:hidden property="dur_hr" />
    <html:hidden property="dur_min" />
    <html:hidden property="locale" value="<c:out value='${myLanguage}'/>" />
    <html:hidden property="vcatRoutingOption" styleId="vcatRoutingOption" value="true" />

<div id="loadingDiv" style="display:none">
</div>



<!-- These scripts make use of JSP tags -->

<script language="javascript">

function initializeDate() {
  mydate=new Date();

  <% if (!createForm.getStartdate().equals("")) { %>
  		setStartDate('<c:out value="${CreateScheduleForm.getStartdate()}"/>');
  <% } else { %>
        setStartDateToday();
  <% } %>

  <% if (!createForm.getEnddate().equals("")) { %>
        setEndDate("<c:out value='${CreateScheduleForm.getEnddate()}'/>");
  <% } else { %>
        setEndDateToday();
  <% } %>

  <% if (!createForm.getStartTime().equals("")) { %>
        setStartTime("<c:out value='${CreateScheduleForm.getStartTime()}'/>");
  <% } else { %>
       setStartTimeDefault();
  <% } %>

  <% if (!createForm.getEndTime().equals("")) { %>
       setEndTime("<c:out value='${CreateScheduleForm.getEndTime()}'/>");
  <% } else { %>
       setEndTimeDefault();
  <% } %>


    updateDuration();

  /* var dayOfWeek = mydate.getDay();
  if (dayOfWeek == 0) document.CreateScheduleForm.weeklySun.checked = true;
  else if (dayOfWeek == 1) document.CreateScheduleForm.weeklyMon.checked = true;
  else if (dayOfWeek == 2) document.CreateScheduleForm.weeklyTue.checked = true;
  else if (dayOfWeek == 3) document.CreateScheduleForm.weeklyWed.checked = true;
  else if (dayOfWeek == 4) document.CreateScheduleForm.weeklyThu.checked = true;
  else if (dayOfWeek == 5) document.CreateScheduleForm.weeklyFri.checked = true;
  else if (dayOfWeek == 6) document.CreateScheduleForm.weeklySat.checked = true;
 */
}


function resetFields()
{
    document.CreateScheduleForm.reset();

    initializeDate();
}

dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
startDateId = "startdate";
endDateId = "enddate";
startTimeId = "starttime";
endTimeId = "endtime";
endByDateId = "endByDate";
dCal.addListener(createSchDateChange);
//initRecurrence("monthlyDay", "yearlyDay", "yearlyMonth");
// Registered html select elements that may overlap with the calendar popup.
if (navigator.appVersion.indexOf("MSIE") != -1) {
    dCal.registerSelect('enddate', 'srcGroup', null);

}

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

startTimeMenu.setChangeListener(createSchTimeChange);
endTimeMenu.setChangeListener(createSchTimeChange);
resetFields();
adjustSrcResGroups(document.getElementById('billingGroup'));
adjustDestResGroups(document.getElementById('billingGroup'));

var durSub = new HoldButton("", durationSub);
var durAdd = new HoldButton("", durationAdd);
var mB = new HoldButton("", monthBack);
var mF = new HoldButton("", monthForward);
var yB = new HoldButton("", yearBack);
var yF = new HoldButton("", yearForward);
var occurUp = new HoldButton("", addOccurrence);
var occurDown = new HoldButton("", subtractOccurrence);
var simplePage = "true";
</script>

</html:form>

<%@ include file="/common/footer.jsp" %>

