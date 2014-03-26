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

<html:form method="POST" action="/schedule/handleCreateAdvancedSchedule.do" onsubmit="return doSubmit()">
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
                <td class="tbForm1" nowrap>
                  <b><bean:message key="drac.schedule.create.name"/></b>
                </td>
                <td class="tbForm1" nowrap>
                    <html:text tabindex="1" style = "width: 250px" styleClass="gen" property="schName" errorStyleClass="invalid"/>
                </td>
              </tr>
              <!-- remove -->
              <tr>
                <td class="tbForm1" nowrap>
                    <b>Activation Type</b>
                </td>
                <td class="tbForm1" nowrap>
                    <html:select tabindex="2" property="scheduleType" styleClass="gen">
                        <html:option value="PRERESERVATION_AUTOMATIC"><bean:message key="drac.reservation.type.PRERESERVATION_AUTOMATIC"/></html:option>
                        <html:option value="PRERESERVATION_MANUAL"><bean:message key="drac.reservation.type.PRERESERVATION_MANUAL"/></html:option>                    
                        <html:option value="RESERVATION_AUTOMATIC"><bean:message key="drac.reservation.type.RESERVATION_AUTOMATIC"/></html:option>
                        <html:option value="RESERVATION_MANUAL"><bean:message key="drac.reservation.type.RESERVATION_MANUAL"/></html:option> 
                    </html:select>
                 </td>
              </tr>
              <!-- end remove -->
              <tr>
                <td class="tbForm1" nowrap><b><bean:message key="drac.schedule.create.billingGroup"/></b></td>
                <td class="tbForm1" align="left">
                    <html:select tabindex="3" styleId="billingGroup" styleClass="gen" property="billingGroup" errorStyleClass="invalid">
                        <html:options property="groups"/>
                    </html:select>
                </td>
              </tr>
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
                    <html:button tabindex="11" property="" styleId="findButton"  onclick="showFindTime()">
                        <bean:message key="drac.schedule.create.queryTime.button"/>
                    </html:button>

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

    <tr>
        <td>
        <!-- Header. -->
        <table border="0" cellpadding="0" cellspacing="0" class="tbt">
            <tbody>
            <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
            <tr>
                <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                <td class="tbtbot"><center><b><bean:message key="drac.schedule.create.title.connectivity"/></b></center></td>
                <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
            </tr>
            </tbody>
        </table>
        </td>
    </tr>
    <tr>
        <td>
        <!-- Schedule Connectivity contents. -->

        <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
        <tbody>

            <tr>
                <td class="tbForm1" colspan="4" align="left" nowrap>
                    <fieldset style="-moz-border-radius: 4pt;">
                    <legend><bean:message key="drac.schedule.create.srcEndpoint"/></legend>

                    <table cellpadding="1" cellspacing="4" border="0">

                        <tr>
                            <td colspan="2"><b><bean:message key="drac.schedule.create.userGroup"/></b></td>
                            <td colspan="4" align="left" nowrap>
                                <html:select tabindex="12" styleId="srcGroup" style="width:200px" styleClass="gen" property="srcGroup" onchange="adjustSrcResGroups(this);">
                                    <html:options property="groups"/>
                                </html:select>
                            </td>
                        </tr>


                        <tr>
                            <td colspan="2" align="left" nowrap>
                                <b><bean:message key="drac.schedule.create.resGroup"/></b>
                            </td>
                            <td colspan="4" align="left" nowrap>
                                <html:select tabindex="13" styleId="srcResGroup" style="width:200px" styleClass="gen" property="srcResGroup" onchange="adjustSrcTNAList('srcResGroup','srcLayer','srcSiteBox');">
                                </html:select>
                            </td>
                        </tr>


                        <tr>
                            <td colspan="2" align="left" nowrap>
                                <b><bean:message key="drac.schedule.create.site"/></b>
                            </td>
                            <td colspan="4" align="left" nowrap>
                                <select tabindex="14" id="srcSiteBox" style="width:200px" class="gen" onchange="adjustSrcTNAList('srcResGroup','srcLayer','srcSiteBox');">
                                    <option value=""><bean:message key="drac.schedule.create.site.all"/></option>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td  colspan="1" align="left" nowrap>
                                <b><bean:message key="drac.schedule.create.endpoint"/></b>
                            </td>
                            <td  colspan="1" align="left" nowrap>
                            <html:select tabindex="15" size="1" property="srcLayer" styleId="srcLayer" styleClass="gen" errorStyleClass="invalid" onchange="adjustSrcTNAList('srcResGroup','srcLayer','srcSiteBox');">
                                <html:option value="layer0"><bean:message key="drac.schedule.create.layer0"/></html:option>
                                <html:option value="layer1"><bean:message key="drac.schedule.create.layer1"/></html:option>
                                <html:option value="layer2"><bean:message key="drac.schedule.create.layer2"/></html:option>
                            </html:select>
                            </td>
                            <td nowrap id="srcWavelengthTd1" style="display:none">
                                <bean:message key="drac.schedule.create.wavelength"/>
                            </td>
                            <td nowrap id="srcWavelengthTd2" style="display:none">
                                <html:select tabindex="16" property="srcWavelength" styleClass="gen" styleId="srcWavelengthBox" onchange="adjustSrcTNAListForWavelength('srcResGroup','srcWavelengthBox','srcSiteBox');">
                                </html:select>
                            </td>
                            <td  colspan="1" align="left" nowrap>
                            <html:select tabindex="17" size ="1" property="srcTna" style="width:200px" styleId="srcTna" styleClass="gen" errorStyleClass="invalid" onchange="adjustSrcChannelList('srcLayer','srcTna');">
                                <html:option value=""> <bean:message key="drac.schedule.create.option.default"/></html:option>
                            </html:select>
                            </td>
                            <td id="srcChannelTd1" colspan="1" align="right" nowrap>
                                <bean:message key="drac.schedule.create.channel"/>
                            </td>
                            <td id="srcChannelTd2" colspan="1" align="right" nowrap title="<bean:message key='drac.schedule.create.channel.help'/>">
                            <html:select tabindex="18" styleId="srcChannel" styleClass="gen" property="sourceChannel" disabled="true">
                                <html:option value="-1"><bean:message key="drac.schedule.create.channel.auto"/></html:option>
                            </html:select>
                            </td>
                            
                            <td id="srcVlanTd1" colspan="1" align="right" nowrap>
                                <bean:message key="drac.schedule.vlan.vid"/>
                            </td>
                             <td id="srcVlanTd2" colspan="1" align="right" nowrap >
                            <!-- html:text styleId="srcVlan" styleClass="gen" property="srcVlan"/-->
                            <html:select tabindex="19" size="1" property="srcVlan" styleId="srcVlan" styleClass="gen" errorStyleClass="invalid">
                                <html:options collection="VlanIdSelections" property="value" labelProperty="label" />
                            </html:select>
                            </td>
                        </tr>

                        <tr>
                            <td colspan="2" align="right" nowrap>
                                <b>Phsyical name:</b>
                            </td>

                             <td colspan="6" align="left" nowrap >
                               <input id='srcFacLabel' type="text" size="75" disabled="true">
                            </td>
                        </tr>

                    </table>
                    </fieldset>
                </td>
            </tr>


           <tr>
                <td class="tbForm1" colspan="4" align="left" nowrap>
                    <fieldset style="-moz-border-radius: 4pt;">
                    <legend><bean:message key="drac.schedule.create.destEndpoint"/></legend>
                    <table cellpadding="1" cellspacing="4" border="0">
                        <tr>
                            <td colspan="2"><b><bean:message key="drac.schedule.create.userGroup"/></b></td>
                            <td colspan="4" align="left" nowrap>
                                <html:select tabindex="20" styleId="destGroup" style="width:200px" styleClass="gen" property="destGroup" onchange="adjustDestResGroups(this);">
                                    <html:options property="groups"/>
                                </html:select>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" align="left" nowrap>
                                <b><bean:message key="drac.schedule.create.resGroup"/></b>
                            </td>
                            <td colspan="4" align="left" nowrap>
                                <html:select tabindex="21" styleId="destResGroup" style="width:200px" styleClass="gen" property="destResGroup" onchange="adjustDestTNAList('destResGroup','destLayer','destSiteBox');">
                                </html:select>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" align="left" nowrap>
                                <b><bean:message key="drac.schedule.create.site"/></b>
                            </td>
                            <td colspan="4" align="left" nowrap>
                                <select tabindex="22" id="destSiteBox" style="width:200px" class="gen" onchange="adjustDestTNAList('destResGroup','destLayer','destSiteBox');">
                                    <option value=""><bean:message key="drac.schedule.create.site.all"/></option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td  colspan="1" align="left" nowrap>
                                <b><bean:message key="drac.schedule.create.endpoint"/></b>
                            </td>
                            <td  colspan="1" align="left" nowrap>
                            <html:select tabindex="23" size="1" property="destLayer" styleId="destLayer" styleClass="gen" errorStyleClass="invalid" onchange="adjustDestTNAList('destResGroup','destLayer','destSiteBox');">
                                <html:option value="layer0"><bean:message key="drac.schedule.create.layer0"/></html:option>
                                <html:option value="layer1"><bean:message key="drac.schedule.create.layer1"/></html:option>
                                <html:option value="layer2"><bean:message key="drac.schedule.create.layer2"/></html:option>
                            </html:select>
                            </td>
                            <td nowrap id="destWavelengthTd1" style="display:none">
                                <bean:message key="drac.schedule.create.wavelength"/>
                            </td>
                            <td nowrap id="destWavelengthTd2" style="display:none">
                                <html:select tabindex="24" property="destWavelength" size ="1" styleClass="gen" styleId="destWavelengthBox" onchange="adjustDestTNAListForWavelength('destResGroup','destWavelengthBox','destSiteBox')">
                                </html:select>
                            </td>
                            <td  colspan="1" align="left" nowrap>
                                <html:select tabindex="25" size ="1" property="destTna" style="width:200px" styleId="destTna" styleClass="gen" errorStyleClass="invalid" onchange="adjustDestChannelList('destLayer','destTna');">
                                    <html:option value=""> <bean:message key="drac.schedule.create.option.default"/> </html:option>
                                </html:select>
                            </td>
                            <td id="destChannelTd1" colspan="1" align="right" nowrap>
                                <bean:message key="drac.schedule.create.channel"/>
                            </td>
                            <td id="destChannelTd2" colspan="1" align="right" nowrap title="<bean:message key='drac.schedule.create.channel.help'/>">
                            <html:select tabindex="26" styleId="destChannel" styleClass="gen" property="destChannel" disabled="true">
                                <html:option value="-1"><bean:message key="drac.schedule.create.channel.auto"/></html:option>
                            </html:select>
                            </td>
                            
                            <td id="destVlanTd1" colspan="1" align="right" nowrap>
                                <bean:message key="drac.schedule.vlan.vid"/>
                            </td>
                             <td id="destVlanTd2" colspan="1" align="right" nowrap >
                            <!--html:text styleId="dstVlan" styleClass="gen" property="dstVlan"/-->
                            <html:select tabindex="27" size="1" property="dstVlan" styleId="dstVlan" styleClass="gen" errorStyleClass="invalid">
                                <html:options collection="VlanIdSelections" property="value" labelProperty="label" />
                            </html:select>
                            </td>
                            
                        </tr>

                        <tr>
                            <td colspan="2" align="right" nowrap>
                                <b>Physical name:</b>
                            </td>
                             <td colspan="6" align="left" nowrap >                                
                                <input id='destFacLabel' type="text" size="75" disabled="true">
                            </td>
                        </tr>


                    </table>
                    </fieldset>
                </td>
            </tr>


   <tr>
	<td class="tbForm1" colspan="4" align="left" nowrap>
	    <table cellpadding="1" cellspacing="4" border="0">
		<tr>

                <td class="tbForm1" align="left"><b><bean:message key="drac.schedule.rate" /></b></td>

                <td align="left" class="tbForm1" nowrap><html:text tabindex="28" maxlength="9" style="width:112px" property="rate" styleId="rate" styleClass="gen" errorStyleClass="invalid" /><b><bean:message key="drac.text.Mbps" /></b></td>

                <td class="tbForm1" align="left"><b><bean:message key="drac.schedule.create.protectionType" /></b></td>

                <td align="left" class="tbForm1">
                    <html:select tabindex="29" style="width:112px" property="protectionType" styleId="protectionType" styleClass="gen" errorStyleClass="invalid">
                        <html:option value="UNPROTECTED"><bean:message key="drac.schedule.create.unprotected"/></html:option>
                        <html:option value="PATH1PLUS1"><bean:message key="drac.schedule.create.1plus1"/></html:option>
                    </html:select>

                  &nbsp;&nbsp;
                  <input type="radio" NAME="concatType" value="CCAT" onclick="updateConcatType()" id="concatTypeCCAT"><bean:message key="drac.schedule.create.ccat"/>
                  <input type="radio" NAME="concatType" value="VCAT" checked onclick="updateConcatType()" id="concatTypeVCAT"><bean:message key="drac.schedule.create.vcat"/>

                </td>

		</tr>
            </table>
        </td>
    </tr>






            <tr>
                <td class="tbForm1" colspan="5"><img src="/images/spacer.gif" height="3"></td>
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
        <td>
        <!-- Header. -->
        <table border="0" cellpadding="0" cellspacing="0" class="tbt">
            <tbody>
            <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
            <tr>
                <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                <td class="tbtbot"><center><b><bean:message key="drac.schedule.create.title.extra"/></b></center></td>
                <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                    </tr>
            </tbody>
        </table>
        </td>
    </tr>
    <tr>
        <td>
        <!-- Schedule extra contents. -->
        <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
        <tbody>
            <tr>
                <td colspan='4' align='left' valign="top" class="row2">
                    <div class="tab-pane" id="optionsPane">
                        <script type="text/javascript">
                        tp1 = new WebFXTabPane( document.getElementById( "optionsPane" ) );
                        </script>

                        <div class="tab-page" id="recurrenceTab" align="center">
                            <h2 class="tab"><bean:message key="drac.schedule.rec"/></h2>
                            <script type="text/javascript">tp1.addTabPage( document.getElementById( "recurrenceTab" ) );</script>
                            <table width="95%" cellspacing="1" cellpadding="5" border="0" id="recurrencePattern">
                            <tr>
                                <td colspan="2">
                                    <table width="100%" border="0" cellspacing="0" cellborder="0">
                                        <tr>
                                            <td><input tabindex="35" type="checkbox" id="recurrence" name="recurrence" onclick="javascript:handleRecurrence();"><img src="/images/recurrence.gif">&nbsp;<b><bean:message key="drac.schedule.rec.message"/></b></td>
                                            <td><input type="radio" name="range" value="byOccurrences" checked onclick="updateRange();applyOccurrences();"><bean:message key="drac.schedule.rec.endafter"/>
                                                <a href="javascript:void(0)" onmousedown="occurDown.handleMouseDown()" onmouseup="occurDown.handleMouseUp()" onmouseout="occurDown.handleMouseOut()" onclick="modifyOccurrences(false)"><img src="/images/arrow_beak_down.gif"></a>
                                                <html:text property="numOccur" styleClass="gen" styleId="occurrences" size='1' maxlength='3' onchange="applyOccurrences();"/>
                                                <a href="javascript:void(0)" onmousedown="occurUp.handleMouseDown()" onmouseup="occurUp.handleMouseUp()" onmouseout="occurUp.handleMouseOut()" onclick="modifyOccurrences(true)"><img src="/images/arrow_beak_up.gif"></a>&nbsp;<bean:message key="drac.schedule.rec.occurrences"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>&nbsp;</td>
                                            <td><input type="radio" name="range" value="byDate" onclick="updateRange()"><bean:message key="drac.schedule.rec.endafter"/>
                                                <html:text styleId="endByDate" property="recEndDate" size="30" styleClass="gen"
                                                    onfocus="dCal.dateFocus(this);"
                                                    onblur="dCal.dateBlur(this);"
                                                    onchange="dCal.dateChange(this);"
                                                    onkeydown="dCal.hideCalendar();"
                                                    errorStyleClass="invalid"/><a href="JavaScript:void(0)" onclick="dCal.dateFocus(document.getElementById('endByDate'));"><img src="/images/calendar.gif" border="0" align="top" vspace="0" hspace="0"></a>
                                            </td>
                                        </tr>
                                    </table>

                                </td>
                            </tr>
                            <TR>
                                <td colspan="2">
                                    <fieldset style="-moz-border-radius: 4pt;"><legend><b><bean:message key="drac.schedule.recurrence_pattern"/></b></legend>
                                        <table border="0" cellspacing="1" cellpadding="5">

                                            <TR>
                                                <TD>
                                                    <INPUT TYPE="radio" NAME="frequency" value="Daily" onClick="handleClickWeekly();" checked><bean:message key="drac.schedule.daily"/><br>
                                                    <INPUT TYPE="radio" NAME="frequency" value="Weekly" onClick="handleClickWeekly();"><bean:message key="drac.schedule.weekly"/><br>
                                                    <INPUT TYPE="radio" NAME="frequency" value="Monthly" onClick="handleClickMonthly();"><bean:message key="drac.schedule.monthly"/><br>
                                                    <INPUT TYPE="radio" NAME="frequency" value="Yearly" onFocus="alert(focused);" onClick="handleClickYearly();"><bean:message key="drac.schedule.yearly"/><br>
                                                </TD>
                                                <td>
                                                    <table id="dailyLayer" style="display:none">
                                                        <tr><td>&nbsp;</td></tr>
                                                    </table>
                                                    <table id="weeklyLayer" style="display:none">
                                                        <TR>
                                                            <TD><INPUT TYPE="checkbox" NAME="weeklySun" onclick="applyOccurrences();"><B><bean:message key="drac.schedule.sun"/></B></TD>
                                                            <TD><INPUT TYPE="checkbox" NAME="weeklyMon" onclick="applyOccurrences();"><B><bean:message key="drac.schedule.mon"/></B></TD>
                                                            <TD><INPUT TYPE="checkbox" NAME="weeklyTue" onclick="applyOccurrences();"><B><bean:message key="drac.schedule.tue"/></B></TD>
                                                            <TD><INPUT TYPE="checkbox" NAME="weeklyWed" onclick="applyOccurrences();"><B><bean:message key="drac.schedule.wed"/></B></TD>
                                                        </TR>
                                                        <TR>
                                                            <TD><INPUT TYPE="checkbox" NAME="weeklyThu" onclick="applyOccurrences();"><B><bean:message key="drac.schedule.thu"/></B></TD>
                                                            <TD><INPUT TYPE="checkbox" NAME="weeklyFri" onclick="applyOccurrences();"><B><bean:message key="drac.schedule.fri"/></B></TD>
                                                            <TD><INPUT TYPE="checkbox" NAME="weeklySat" onclick="applyOccurrences();"><B><bean:message key="drac.schedule.sat"/></B></TD>
                                                        </TR>
                                                    </TABLE>
                                                    <TABLE id="monthlyLayer" style="display:none">
                                                        <TR>
                                                            <TD><INPUT TYPE="radio" NAME="meetinmonth" checked><B><bean:message key="drac.schedule.day"/></B>
                                                            <html:text styleId='monthlyDay' onchange="monthlyDayChange(this);" styleClass="gen" property='monthlyDay' size='1' maxlength='2' value='' errorStyleClass="invalid"/> <b><bean:message key="drac.schedule.of_every_month"/></b>
                                                            </TD>
                                                        </TR>
                                                    </TABLE>
                                                    <table id="yearlyLayer" style="display:none">
                                                        <TR>
                                                            <TD><INPUT TYPE="radio" NAME="year" checked><B><bean:message key="drac.schedule.every"/></B>
                                                            <html:select size="1" onchange="yearlyChange()" styleId="yearlyMonth" styleClass="gen" property="yearlyMonth" style="width:120px">
                                                                <html:option value="0"> <bean:message key="drac.schedule.jan"/></html:option>
                                                                <html:option value="1"> <bean:message key="drac.schedule.feb"/></html:option>
                                                                <html:option value="2"> <bean:message key="drac.schedule.mar"/></html:option>
                                                                <html:option value="3"> <bean:message key="drac.schedule.apr"/></html:option>
                                                                <html:option value="4"> <bean:message key="drac.schedule.may"/></html:option>
                                                                <html:option value="5"> <bean:message key="drac.schedule.jun"/></html:option>
                                                                <html:option value="6"> <bean:message key="drac.schedule.jul"/></html:option>
                                                                <html:option value="7"> <bean:message key="drac.schedule.aug"/></html:option>
                                                                <html:option value="8"> <bean:message key="drac.schedule.sep"/></html:option>
                                                                <html:option value="9"> <bean:message key="drac.schedule.oct"/></html:option>
                                                                <html:option value="10"> <bean:message key="drac.schedule.nov"/></html:option>
                                                                <html:option value="11"> <bean:message key="drac.schedule.dec"/></html:option>
                                                            </html:select>
                                                            <html:text styleId="yearlyDay" onchange="yearlyChange()" styleClass="gen" property='yearlyDay' size='1' maxlength='2' value='' errorStyleClass="invalid"/>
                                                            </TD>
                                                        </TR>
                                                    </table>
                                                </td>
                                            </TR>
                                        </table>
                                    </fieldset>
                                </td>
                            </TR>
                        </table>
                    </div>
                    <div class="tab-page" id="advancedTab" align="center">
                        <h2 class="tab"><bean:message key='drac.schedule.create.button.advanced'/></h2>
                        <script type="text/javascript">tp1.addTabPage( document.getElementById( "advancedTab" ) );</script>
                        <table width="95%" cellspacing="1" cellpadding="0" border="0" id="advancedOptions">
                            <tr align ="left">
                                <TD class="tbForm1"><b><bean:message key="drac.schedule.create.advanced.email" /></b></TD>
                                <TD class="tbForm1" colspan="2">
                                 <html:text tabindex="31" property="email" styleId="email" styleClass="gen" errorStyleClass="invalid" style="width:200px;"/>
                                </TD>
                            </TR>
                            <!-- Source endpoint layer selection. -->
                            <tr>
                                <td class="tbForm1" nowrap>
                                  <b><bean:message key="drac.schedule.create.advanced.algorithm"/></b>
                                </td>
                                <td class="tbForm1" colspan="2">
                                  <html:select tabindex="32" styleId="algorithm" property="algorithm" size="1" styleClass="gen" onchange="JavaScript:routingAlgorithmChange('algorithm');">
                                      <html:option value="default"><bean:message key="drac.schedule.create.advanced.algorithm.default"/></html:option>
                                       <html:option value="cspf"><bean:message key="drac.schedule.create.advanced.algorithm.cspf"/></html:option>
                                      <html:option value="spf"><bean:message key="drac.schedule.create.advanced.algorithm.spf"/></html:option>                                     
                                  </html:select>
                                </td>
                            </tr>
                            <tr>
                                <td class="tbForm1" nowrap>
                                  <b><bean:message key="drac.schedule.create.advanced.routing.cspf.metric"/></b>
                                </td>
                                <td class="tbForm1" nowrap>
                                <b><bean:message key="drac.schedule.create.advanced.routing.cspf.metricType"/></b> &nbsp
                                  <html:select tabindex="33" styleId="routingMetric" property="routingMetric" size="1" styleClass="gen" onchange="JavaScript:routingMetricChange('routingMetric', 'metricValue');" disabled="true">
                                      <html:option value="default"><bean:message key="drac.schedule.create.advanced.routing.cspf.default"/></html:option>
                                      <html:option value="cost"><bean:message key="drac.schedule.create.advanced.routing.cspf.cost"/></html:option>
                                      <html:option value="hop"><bean:message key="drac.schedule.create.advanced.routing.cspf.hop"/></html:option>                                      
                                      <html:option value="metric2"><bean:message key="drac.schedule.create.advanced.routing.cspf.metric2"/></html:option>
                                  </html:select>
                                </td>
                                <td nowrap class="tbForm1">
                                <b><bean:message key="drac.schedule.create.advanced.routing.cspf.metricValue"/></b>&nbsp;
                                <html:text tabindex="34" styleId="metricValue" property="metricValue" styleClass="gen" size="10" errorStyleClass="invalid" disabled="true"/>
                                </td>
                            </tr>
                            <!-- <tr>
                                <td class="tbForm1" nowrap>
                                    <b><bean:message key="drac.schedule.create.advanced.routing.cspf.srlg"/></b>
                                </td>
                                <td class="tbForm1" colspan="2">
                                    <html:text property="srlg" style="width:200px;" styleClass="gen" styleId="srlg" errorStyleClass="invalid"/>
                                </td>
                            </tr> -->
                             <tr align='left'>
                                <td class="tbForm1" nowrap colspan="3">
                                    <fieldset>
                                        <legend><b><bean:message key="drac.schedule.create.advanced.diverseRouting"/></b></legend>
                                        <table cellpadding="1" cellspacing="4" border="0">
                                            <tr>
                                                <td class="tbForm1" nowrap>
                                                    <b><bean:message key="drac.schedule.create.advanced.diverse.routeBy"/><br>
                                                    <bean:message key="drac.schedule.create.advanced.srlg"/></b>
                                                </td>
                                                <td class="tbForm1" nowrap>
                                                    <html:text tabindex="35" style="width: 240px;" styleId="srlg" styleClass="gen" property="srlg" errorStyleClass="invalid"/>
                                                </td>
                                             </tr>
                                             <tr>
                                                <td class="tbForm1" nowrap>
                                                    <b><bean:message key="drac.schedule.create.advanced.diverse.routeBy"/><br>
                                                    <bean:message key="drac.schedule.create.advanced.srsg"/></b>
                                                </td>
                                                <td class="tbForm1" nowrap>
                                                    <html:text tabindex="36" style="width: 240px;" styleId="srsg" styleClass="gen" property="srsg" errorStyleClass="invalid"/>
                                                </td>
                                             </tr>
                                        </table>
                                   </fieldset>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2"><img src="/images/spacer.gif" alt="" height="95"/></td>
                            </tr>
                        </table>
                    </div>
                    <c:if test="${!empty param.debug}">
                    <div class="tab-page" id="debugTab" align="center">
                        <h2 class="tab">Debug Mode</h2>
                        <script type="text/javascript">tp1.addTabPage( document.getElementById( "debugTab" ) );</script>
                        <table width="95%" cellspacing="1" cellpadding="0" border="0" id="debugOptions">
                            <tr>
                                <td><html:select multiple="true" styleId="debugOptionsBox" property="debugOptions" size="5" style="width:200px">
                                    </html:select><p>
                                    <input type="text" size="10" id="debugInput"/> = <input type="text" size="20" id="debugInput2"/><p>
                                    <input type="button" value="Add" id="addDebugButton" onclick="addDebugOption()"/>
                                    <input type="button" value="Remove" id="removeDebugButton" onclick="removeDebugOption()"/>
                                    <input type="button" value="Clear" id="clearDebugButton" onclick="clearDebugOption()"/>
                                </td>
                            </tr>
                            <tr>
                                <td><img src="/images/spacer.gif" alt="" height="95"/></td>
                            </tr>
                        </table>
                    </div>
                    </c:if>
                    <!-- <div class="tab-page" id="endpointTab">
                        <h2 class="tab">Endpoint Templates</h2>
                        <script type="text/javascript">tp1.addTabPage( document.getElementById( "endpointTab" ) );</script>
                    </div> -->
                </div>
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
   </tbody>
</table>
<table align="center">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr>
        <td>
            <html:button tabindex="97" property="" styleId="queryButton"  onclick="if (checkOccurrences()) showDiv()">
                <bean:message key="drac.schedule.create.button.query"/>
            </html:button>
        </td>
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

    <html:hidden property="dur_hr" />
    <html:hidden property="dur_min" />
    <html:hidden property="locale" value="<c:out value='${myLanguage}'/>" />
    <html:hidden property="vcatRoutingOption" styleId="vcatRoutingOption" value="true" />

<div id="loadingDiv" style="display:none">
</div>
<%@ include file="/schedule/createScheduleScripts.jsp" %>


</html:form>

<%@ include file="/common/footer.jsp" %>

