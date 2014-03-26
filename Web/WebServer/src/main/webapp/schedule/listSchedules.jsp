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
<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
 /****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /schedule/listSchedules.jsp
 *
 * Description:
 *  This page provides the user with a list schedules filter for generating
 *  a schedule query.
 *
 ****************************************************************************/

String pageRef = "drac.schedule.list";
%>

<%@ include file="/common/header_struts.jsp"%>

<%@ include file="/common/calendar.jsp" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>



<html:form method="POST" action="/schedule/listSchedulesResult.do">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
<table cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
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
        <td align="center" class="gen">
            <bean:message key="drac.schedule.list.text"/>
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
                <td class="tbtbot"><center><b><bean:message key="drac.schedule.list.title"/></b></center></td>
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
                    <td class="row1">
                        <b><bean:message key="drac.schedule.list.filter.group" />
                        </b>
                    </td>
                    <td class="row1">
                        <html:select tabindex="1" styleClass="gen" size="1" property="group" style = "width: 180px">
                            <html:option value="all"><bean:message key="drac.schedule.list.filter.group.all" /></html:option>
                            <html:options property="groupList"/>
                        </html:select>
                    </td>
                </tr>
                <tr>
                    <td class="row2">
                        <b><bean:message key="drac.schedule.startdate" />
                        </b>
                    </td>
                    <td class="row2">
                        <input tabindex="2" id="startdate" name="startdate" size="30" class="gen"
                            type="text" onfocus="dCal.dateFocus(this,document.getElementById('enddate'));"
                            onblur="dCal.dateBlur(this);"
                            onchange="dCal.dateChange(this);"
                            value="" onkeydown="dCal.hideCalendar();"
                             /><a href="Javascript:void(0);" tabindex="-1" onclick="dCal.dateFocus(document.getElementById('startdate'),document.getElementById('enddate'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                    </td>
                <tr>
                    <td class="row1">
                        <b><bean:message key="drac.schedule.enddate" /> </b>
                    </td>
                    <td class="row1">
                        <input tabindex="3" id="enddate" name="enddate" size="30" class="gen"
                            type="text" onfocus="dCal.dateFocus(this, document.getElementById('Retrieve'));"
                            onblur="dCal.dateBlur(this);"
                            onchange="dCal.dateChange(this);"
                            value="" onkeydown="dCal.hideCalendar();"
                             /><a href="Javascript:void(0)" tabindex="-1" onclick="dCal.dateFocus(document.getElementById('enddate'),document.getElementById('Retrieve'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
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
            <html:submit tabindex="98" property="Retrieve">
                <bean:message key="drac.text.submit"/>
            </html:submit>
            &nbsp;&nbsp;
            <html:button tabindex="99" property="Reset" onclick="setDatesToday();">
                <bean:message key="drac.text.reset" />
            </html:button>
        </td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
</table>


<script language="javascript">
dCal = new DRACCalendar("<c:out value='${myLanguage}'/>", "long", serverDigitalTime);
startDateId = "startdate";
endDateId = "enddate";
dCal.addListener(dateChange);
setDatesToday();

var mB = new HoldButton("", monthBack);
var mF = new HoldButton("", monthForward);
var yB = new HoldButton("", yearBack);
var yF = new HoldButton("", yearForward);
</script>
</html:form>

<%@ include file="/common/footer.jsp"%>
