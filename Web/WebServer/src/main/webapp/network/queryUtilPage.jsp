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
<%@ page session="true" errorPage="/common/dracError.jsp" %>

<%String pageRef = "drac.network.QueryUtil";%>

<%@ include file="/common/header_struts.jsp"%>
<%@ include file="/common/calendar.jsp" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>

<html:form action="/network/queryUtilList" method="POST">
<table cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align="center" class="gen">
            <bean:message key="drac.menu.network.title" />
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
                <td class="tbtbot"><center><b><bean:message key="drac.network.utilization.list.title"/></b></center></td>
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
                    <logic:empty name="QueryUtilForm" property="endPointList" scope="request">
                    <tr>
                        <td colspan='8' align='center'>
                            <bean:message key="drac.menu.network.norecords" />
                        </td>
                    </tr>
                    </logic:empty>
                    <logic:notEmpty name="QueryUtilForm" property="endPointList" scope="request">
                    <tr align='left'>
                        <td class="row1" align="left" class="gen">
                            <b><bean:message key="drac.menu.network.endpoint" /></b>
                        </td>
                        <td  colspan='3' align="left" class="row1">
                            <html:select size="1" property="endpoint" style="width: 180px">
                                <html:options property="endPointList" />
                            </html:select>
                        </td>
                    </tr>
                    <tr align='left'>
                        <!-- <td class="row2" align="left">
                            <b><bean:message key="drac.schedule.create.starttime" /> (EST)</b>
                        </td>
                        <td class="row2">
                            <html:text property="startTime" size="18" style="width:100" />
                            <input type="button" value="&#9660;" style="height:23; width:22; font-family: helvetica;" onclick="JavaScript:menuActivate('startTime', 'startTimeLayer', 'startTimeSel')">
                        </td> -->
                        <td class="row1">
                            <b><bean:message key="drac.schedule.startdate" /></b>
                        </td>
                        <td class="row1" colspan="3">
                            <input id="startdate" name="startdate" size="30" class="gen"
                                type="text" onfocus="JavaScript:dCal.dateFocus(this, document.getElementById('dayButton'));"
                                onblur="JavaScript:dCal.dateBlur(this);"
                                onchange="JavaScript:dCal.dateChange(this);"
                                value="" onkeydown="JavaScript:dCal.hideCalendar();"
                                /><a href="JavaScript:void(0);" tabindex="-1" onclick="JavaScript:dCal.dateFocus(document.getElementById('startdate'), document.getElementById('dayButton'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                        </td>
                    </tr>
                    <!-- TODO unhide the enddate when we figure out how to show graph between multiple day ranges -->
                    <tr align='left' style="display:none;">
                        <td class="row1">
                            <b><bean:message key="drac.schedule.enddate"/></b>
                        </td>
                        <td class="row1">
                            <input id="enddate" name="enddate" size="30" class="gen"
                                type="text" onfocus="JavaScript:dateFocus(this, document.getElementById('dayButton'));"
                                onblur="JavaScript:dateBlur(this);"
                                onchange="JavaScript:dateChange(this);"
                                value="" onkeydown="JavaScript:hideCalendar();"
                                /><a href="JavaScript:void(0)" tabindex="-1" onclick="JavaScript:dateFocus(document.getElementById('enddate'), document.getElementById('dayButton'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                        </td>
                    </tr>
                    <tr align="left">
                        <td class="row1">
                            <b><bean:message key="drac.network.utilization.range"/></b>
                        </td>
                        <td class="row1">
                            <html:radio property="range" value="oneDay" styleId="dayButton"><bean:message key="drac.network.utilization.range.oneDay"/></html:radio><br>
                            <html:radio property="range" value="oneWeek" styleId="dayButton"><bean:message key="drac.network.utilization.range.oneWeek"/></html:radio><BR>
                            <html:radio property="range" value="oneMonth"><bean:message key="drac.network.utilization.range.oneMonth"/></html:radio><br>
                            <html:radio property="range" value="threeMonth"><bean:message key="drac.network.utilization.range.threeMonth"/></html:radio>
                        </td>
                    </logic:notEmpty>
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
    <logic:notEmpty name="QueryUtilForm" property="endPointList" scope="request">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr align='left'>
        <td colspan='4' align="center">
            <html:submit property="Query" value="Query" />
            <input type="button" name="button2" value='<c:out value='${sessionScope["drac.text.reset"]}'/>' onclick="setStartDateNow()">
        <td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    </logic:notEmpty>
</table>
</html:form>

<script language="javascript">
dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
startDateId = "startdate";
//dCal.addListener(dateChange);
setStartDateToday();
document.getElementById("dayButton").checked = true;

var mB = new HoldButton("", monthBack);
var mF = new HoldButton("", monthForward);
var yB = new HoldButton("", yearBack);
var yF = new HoldButton("", yearForward);
</script>
<%@ include file="/common/footer.jsp"%>
