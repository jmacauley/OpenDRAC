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

<%@ page language="java" contentType="text/html; charset=iso-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page session="true" errorPage="/common/dracError.jsp"
    import="com.nortel.appcore.app.drac.server.webserver.struts.service.form.*"%>


<%String pageRef = "drac.service.add.details";%>

<%@ include file="/common/header_struts.jsp"%>

<%@ include file="/common/calendar.jsp" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>
<script type="text/javascript" src="/scripts/handleExpand.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/scheduleCreate.js"></SCRIPT>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>

<%
    // Set the form-bean as an object we can use in any scriplets
    AddServiceForm createForm = (AddServiceForm) request.getAttribute("AddServiceForm");
%>

<jsp:useBean id="dracHelper" class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper" scope="application" />

<html:form action="/service/performAddService" method="POST" onsubmit="return addServiceDoSubmit()">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
    <html:hidden property="schId" value="${AddServiceForm.schId}"/>
    <html:hidden property="schName" value="${AddServiceForm.schName}"/>
    <html:hidden property="srcGroup" value="${AddServiceForm.srcGroup}"/>
    <html:hidden property="srcLayer" value="${AddServiceForm.srcLayer}"/>
    <html:hidden property="destGroup" value="${AddServiceForm.destGroup}"/>
    <html:hidden property="destLayer" value="${AddServiceForm.destLayer}"/>
    <html:hidden property="desttna" value="${AddServiceForm.desttna}"/>
    <html:hidden property="srctna" value="${AddServiceForm.srctna}"/>
    <html:hidden property="locale" value="<c:out value='${myLanguage}'/>" />

    <logic:messagesPresent>
    <table cellspacing="0" cellpadding="0" border="0" width="550" align="center">
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
    </table>
    </logic:messagesPresent>

    <logic:messagesPresent message="true">
    <table cellspacing="0" cellpadding="0" border="0" align="center">
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
        <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
        <tbody>
        <html:messages id="message" message="true">
        <tr>
            <td align="center" class="tbForm1">
                <img valign="middle" src="/images/warning.png">&nbsp;<font color="red"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
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

    </table>
    </logic:messagesPresent>

<table cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.service.details.scheduleDetails"/></th></b></center></td>
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
                    <tr>
                        <td align="right" class="row1"><b><bean:message key="drac.schedule.name"/>:</b></td>
                        <td align="left" class="row1"><a href="/schedule/querySchedule.do?sid=${AddServiceForm.schId}">${fn:escapeXml(AddServiceForm.schName)}</a></td>
                    </tr>
                    <tr>
                        <td align="right" class="row2"><b><bean:message key="drac.schedule.rate"/>:</b></td>
                        <td align="left" class="row2">${fn:escapeXml(AddServiceForm.rate)} <bean:message key="drac.text.Mbps"/></td>
                    </tr>
                    <tr>
                        <td align="right" class="row1"><b><bean:message key="drac.schedule.sourceTNA"/>:</b></td>
                        <td align="left" class="row1">${fn:escapeXml(AddServiceForm.srctna)}</td>
                    </tr>
                    <tr>
                        <td align="right" class="row2"><b><bean:message key="drac.schedule.sourceLayer"/>:</b></td>
                        <td align="left" class="row2">${fn:escapeXml(AddServiceForm.srcLayer)}</td>
                    </tr>
                    <tr>
                        <td align="right" class="row1"><b><bean:message key="drac.schedule.destinationTNA"/>:</b></td>
                        <td align="left" class="row1">${fn:escapeXml(AddServiceForm.desttna)}</td>
                    </tr>
                    <tr>
                        <td align="right" class="row2"><b><bean:message key="drac.schedule.destinationLayer"/>:</b></td>
                        <td align="left" class="row2">${fn:escapeXml(AddServiceForm.destLayer)}</td>
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

<table cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td align="center" class="gen">&nbsp;<p><bean:message key="drac.service.add.title" /></td>
    </tr>
    <tr>
        <td align="center" class="gen">
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
          <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
            <tbody>
                <tr align='left'>
                    <td class="tbForm1" nowrap>
                      <b><bean:message key="drac.schedule.create.starttime"/></b>
                    </td>
                    <td class="tbForm1" nowrap>
                        <html:text styleId="startdate" property="startdate" size="30" styleClass="gen"
                            onfocus="dCal.dateFocus(this, document.getElementById('starttime'));"
                            onblur="dCal.dateBlur(this);"
                            onchange="dCal.dateChange(this);"
                            value=""
                            onkeydown="dCal.hideCalendar();"
                            errorStyleClass="invalid"
                            /><a href="JavaScript:void(0);"
                            tabindex="-1" onclick="dCal.dateFocus(document.getElementById('startdate'),document.getElementById('starttime'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                        &nbsp;&nbsp;
                        <html:text styleId="starttime" errorStyleClass="invalid" property="startTime" size="11" styleClass="gen"
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
                        </div>
                    </td>
                    <td class="tbForm1" align="right">
                        <b><input id ="startNow" name="startNow" type="checkbox" onclick="JavaScript:startNowChange('startNow', startTimeMenu);"></b>
                    </td>
                    <td class="tbForm1" nowrap align="left">
                        <b><bean:message key="drac.schedule.create.startNow"/></b>
                    </td>
                </tr>
                <tr>
                  <td class="tbForm1" nowrap>
                    <b><bean:message key="drac.schedule.create.endtime"/></b>
                  </td>
                  <td class="tbForm1" nowrap>
                    <html:text styleId="enddate" property="enddate" size="30" styleClass="gen"
                        onfocus="dCal.dateFocus(this, document.getElementById('endtime'));"
                        onblur="dCal.dateBlur(this);"
                        onchange="dCal.dateChange(this);"
                        onkeydown="dCal.hideCalendar();"
                        errorStyleClass="invalid"
                        /><a href="JavaScript:void(0)"
                        tabindex="-1" onclick="dCal.dateFocus(document.getElementById('enddate'),document.getElementById('endtime'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                    &nbsp;&nbsp;
                    <html:text styleId="endtime" property="endTime" errorStyleClass="invalid" size="11" styleClass="gen"
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
                    </div>
                  </td>
                  <td  class="tbForm1" align="right">
                    <input id ="endNever" name="endNever" type="checkbox" onclick="JavaScript:endNeverChange('endNever', endTimeMenu);" />
                  </td>
                  <td  class="tbForm1" nowrap align="left">
                    <img src="/images/infinity.gif" align="top" alt="" /><img src="/images/spacer.gif" align="top" alt="" width="2" /><b><bean:message key="drac.schedule.create.endNever"/></b>
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
    <tr>
        <td class="gen"><img src="/images/spacer.gif" height="10"></td>
    </tr>
    <tr>
        <td align="center" class="gen">
            <!-- Header. -->
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.service.add.diverseRouting"/></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                    </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table cellpadding="5" cellspacing="0" border="0" align="center" class="tbForm">
                <tr>
                    <td class="tbForm1" nowrap>
                        <b><bean:message key="drac.service.add.diverse.routeBy"/><br>
                        <bean:message key="drac.service.add.srlg"/></b>
                    </td>
                    <td class="tbForm1" nowrap>
                        <html:text style="width: 240px;" styleId="srlg" styleClass="gen" property="srlg" errorStyleClass="invalid"/>
                    </td>
                 </tr>
                 <tr>
                    <td class="tbForm1" nowrap>
                        <b><bean:message key="drac.service.add.diverse.routeBy"/><br>
                        <bean:message key="drac.service.add.srsg"/></b>
                    </td>
                    <td class="tbForm1" nowrap>
                        <html:text style="width: 240px;" styleId="srsg" styleClass="gen" property="srsg" errorStyleClass="invalid"/>
                    </td>
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
<table align="center">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr>
        <td>
            <html:submit property="Add" styleId="Add">
                <bean:message key="drac.service.add.button"/>
            </html:submit>
        </td>
        <td>
            <input type="button" name="button2" value="<bean:message key='drac.text.reset'/>"
                onclick="resetFields()">
        </td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
</table>


<script language="javascript">
function initializeDate() {
  <% if (!createForm.getStartdate().equals("")) { %>
        setStartDate("<c:out value='${AddServiceForm.getStartdate()}'/>");
    <% } else { %>
        setStartDateToday();
    <% } %>

  <% if (!createForm.getEnddate().equals("")) { %>
        setEndDate("<c:out value='${AddServiceForm.getEnddate()}'/>");
    <% } else { %>
        setEndDateToday();
    <% } %>

  <% if (!createForm.getStartTime().equals("")) { %>
        setStartTime("<c:out value='${AddServiceForm.getStartTime()}'/>");
  <% } else { %>
       setStartTimeDefault();
  <% } %>

  <% if (!createForm.getEndTime().equals("")) { %>
       setEndTime("<c:out value='${AddServiceForm.getEndTime()}'/>");
  <% } else { %>
       setEndTimeDefault();
  <% } %>
}

//
// startNowChange() - handles logic for fixing startDate to the current
//   server time (i.e. start the schedule now).
//
// External Functions:
//   unlockStartDate() - calendar.js
//   lockStartDate() - calendar.js
//
function startNowChange(checkbox, startTimeMenu)
{
  var elem = document.getElementById(checkbox);

  if (elem == null)
  {
    return;
    }

  // Check to see if the startNow element is checked.
  if (elem.checked == false)
  {
    unlockStartDate();
    startTimeMenu.unlock();
  }
  else
  {
    lockStartDate();
    startTimeMenu.lock();
  }

} /* startNowChange */

//
// endNeverChange() - handles logic for fixing endDate to infinity (i.e. a
//   schedule that never ends).
//
// External Functions:
//   unlockEndDate() - calendar.js
//   lockEndDate() - calendar.js
//
function endNeverChange(checkbox, endTimeMenu)
{
  var elem = document.getElementById(checkbox);

  if (elem == null)
  {
    return;
  }
  // Check to see if the endNever element is checked.
  if (elem.checked == false)
  {
    unlockEndDate();
    endTimeMenu.unlock();
  }
  else
  {
    lockEndDate();
    endTimeMenu.lock();
  }
} /* endNeverChange */

function resetFields()
{
    document.getElementById('startNow').checked = false;
    unlockStartDate();
    startTimeMenu.unlock();

    document.getElementById('endNever').checked = false;
    unlockEndDate();
    endTimeMenu.unlock();
}


dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
startDateId = "startdate";
endDateId = "enddate";
startTimeId = "starttime";
endTimeId = "endtime";
dCal.addListener(dateChange);
// Initialize the pulldown start/end time menues.
var startTimeMenu = new EditableList('starttime', 'startTimeLayer', 'starttimes');

var endTimeMenu = new EditableList('endtime', 'endTimeLayer', 'endtimes');

// I think this is needed for the pulldown menues to function correctly.
function timeMouseSelect(e)
{
startTimeMenu.mouseSelect(0);
endTimeMenu.mouseSelect(0);
}

function addServiceDoSubmit()
{
        var add = document.getElementById("Add");
        if (add != null)
        {
            add.disabled = true;
        }
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

</html:form>
<%@ include file="/common/footer.jsp"%>
