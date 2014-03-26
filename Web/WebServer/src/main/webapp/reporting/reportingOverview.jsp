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
<%@ taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@ page session="true" errorPage="/common/dracError.jsp"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<%
    String pageRef = "drac.reporting.list";
%>

<%@ include file="/common/header_struts.jsp"%>
<%@ include file="/common/calendar.jsp"%>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/reporting.js"></SCRIPT>
<script type="text/javascript" src="/scripts/handleExpand.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>

<html:form action="/downloadReport" method="POST">
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
			<td align="center" class="gen">&nbsp;
			<p><bean:message key="drac.reporting.list.text" />
			</td>
		</tr>
		<tr>
			<td align="center" class="gen"><!-- Header. -->
			<table border="0" cellpadding="0" cellspacing="0" class="tbt">
				<tbody>
					<tr>
						<td><img src="/images/spacer.gif" alt="" height="5" /></td>
					</tr>
					<tr>
						<td class="tbtl"><img src="/images/spacer.gif" alt=""
							width="22" height="22" /></td>
						<td class="tbtbot">
						<center><b><bean:message
							key="drac.reporting.reportings.list.title" /></b></center>
						</td>
						<td class="tbtrr"><img src="/images/spacer.gif" alt=""
							width="22" height="22" alt="" /></td>
					</tr>
				</tbody>
			</table>
			</td>
		</tr>

		<jsp:useBean id="dracHelper"
			class="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper"
			scope="application" />
		<tr>
			<td>
			<table cellspacing="0" cellpadding="5" border="0" align="center"
				class="tbForm">
				<tbody>
					<tr align='left'>
						<td class="tbForm1" nowrap><b><bean:message
							key="drac.schedule.create.starttime" /></b></td>
						<td class="tbForm1" nowrap><!-- Avoid spaces and new lines between input and </a> -->
						<input id="startdate" name="startdate" size="30" class="gen"
							type="text"
							onfocus="dCal.dateFocus(this, document.getElementById('starttime')));"
							onblur="dCal.dateBlur(this);" onchange="dCal.dateChange(this);"
							value="" onkeydown="dCal.hideCalendar();" tabindex="1" /><a
							href="Javascript:void(0);" tabindex="-1"
							onclick="dCal.dateFocus(document.getElementById('startdate'),document.getElementById('starttime'));"><img
							src="/images/calendar.gif" border="0" align="top" vspace="0"
							hspace="0"></a> &nbsp;&nbsp; <input id="starttime"
							name="startTime" size="11" class="gen" type="text"
							onchange="timeChangeListener(this);" tabindex="2" /><a
							href="Javascript:void(0);" tabindex="-1"
							onclick="startTimeMenu.activate();"><img
							src="/images/clock.gif" border="0" align="top" vspace="0"
							hspace="0"></a>
						<div id="startTimeLayer"
							style="position: absolute; z-index: 10; overflow: auto; display: none;"
							onmouseover="startTimeMenu.overMenu(true);"
							onmouseout="startTimeMenu.overMenu(false);"><select
							id="starttimes" size="10" class="gen"
							style="width: 100px; border-style: none"
							onclick="startTimeMenu.textSet(this.value);"
							onkeypress="startTimeMenu.comboKey();">
							<logic:iterate id="timeStrChoice" name="dracHelper"
								property="timeStringList" scope="application">
								<option
									value="<bean:write name="timeStrChoice" property="value" />">
								<bean:write name="timeStrChoice" property="label" /></option>
							</logic:iterate>
						</select>
						</td>
					</tr>
					<tr>
						<td class="tbForm1" nowrap><b><bean:message
							key="drac.schedule.create.endtime" /></b></td>
						<td class="tbForm1" nowrap><input id="enddate" name="enddate"
							size="30" class="gen" type="text"
							onfocus="dCal.dateFocus(this,document.getElementById('endtime'));"
							onblur="dCal.dateBlur(this);" onchange="dCal.dateChange(this);"
							value="" onkeydown="dCal.hideCalendar();" tabindex="3" /><a
							href="Javascript:void(0)" tabindex="-1"
							onclick="dCal.dateFocus(document.getElementById('enddate'),document.getElementById('endtime'));"><img
							src="/images/calendar.gif" border="0" align="top" vspace="0"
							hspace="0"></a> &nbsp;&nbsp; <input id="endtime" name="endTime"
							size="11" class="gen" type="text"
							onchange="timeChangeListener(this);" tabindex="4" /><a
							href="Javascript:void(0);" tabindex="-1"
							onclick="endTimeMenu.activate();"><img
							src="/images/clock.gif" border="0" align="top" vspace="0"
							hspace="0"></a>
						<div id="endTimeLayer"
							style="position: absolute; z-index: 10; overflow: auto; display: none;"
							onmouseover="endTimeMenu.overMenu(true);"
							onmouseout="endTimeMenu.overMenu(false);"><select
							id="endtimes" size="10" class="gen"
							style="width: 100px; border-style: none"
							onclick="endTimeMenu.textSet(this.value);"
							onkeypress="endTimeMenu.comboKey();">
							<logic:iterate id="timeStrChoice" name="dracHelper"
								property="timeStringList" scope="application">
								<option
									value="<bean:write name="timeStrChoice" property="value" />">
								<bean:write name="timeStrChoice" property="label" /></option>
							</logic:iterate>
						</select>
						</td>
					</tr>
				</tbody>
			</table>
			
			</td>
		</tr>
		<tr>
			<td><!-- Drop shadow. -->
			<table border="0" cellpadding="0" cellspacing="0" class="tbl">
				<tbody>
					<tr>
						<td class="tbll"><img src="/images/spacer.gif" alt=""
							width="8" height="4" /></td>
						<td class="tblbot"><img src="/images/spacer.gif" alt=""
							width="8" height="4" /></td>
						<td class="tblr"><img src="/images/spacer.gif" alt=""
							width="8" height="4" /></td>
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
	            <html:submit tabindex="98" styleId="retrieve" property="Retrieve">
	                <bean:message key="drac.text.download"/>
	            </html:submit>
	            &nbsp;&nbsp;
 				<html:button tabindex="98" styleId="retrieve" property="Retrieve" onclick="toReportingOverviewPage();">
	                <bean:message key="drac.text.reportAsTable"/>
	            </html:button>
	            &nbsp;&nbsp;	            
	            <html:button tabindex="99" property="Reset" onclick="setDatesToday();">
	                <bean:message key="drac.text.reset" />
	            </html:button>
	        </td>
	    </tr>
	    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
	</table>

	<logic:notEmpty name="no_logs" scope="request">
		<div align="center"><font color="red"><b><bean:message
			key="drac.auditlogs.empty" /></b></div>
	</logic:notEmpty>

	<logic:notEmpty name="auditLogList" scope="request">
		<%
		    String timeTitle = (String) session
									.getAttribute("drac.auditlogs.list.time")
									+ (String) request.getAttribute("TZString");
		%>

		<table width="98%" cellspacing="5" cellpadding="0" border="0"
			align="center">
			<tr>
				<td><img src="/images/spacer.gif" height="5" /></td>
			</tr>
			<tr>
				<td align="center"><display:table id="log" name="auditLogList"
					cellspacing="1" cellpadding="5" class="forumline" pagesize="20"
					sort="list" requestURI="">
					<c:set var="tdclass" value="row1c" />
					<c:set var="tdclassL" value="row1Left" />
					<c:if test="${log_rowNum % 2 == 0}">
						<c:set var="tdclass" value="row2c" />
						<c:set var="tdclassL" value="row2Left" />
					</c:if>
					<display:column property="time" sortable="true"
						sortProperty="occurTime" title='<c:out value="${timeTitle}" />' class="${tdclass}">
					</display:column>
					<display:column escapeXml="true" sortable="true"
						title="${sessionScope['drac.auditlogs.list.userID']}"
						class="${tdclass}">
						<c:choose>
							<c:when test="${log.userid eq 'DRAC_SYSTEM'}">DRAC</c:when>
							<c:otherwise>${log.userid}</c:otherwise>
						</c:choose>
					</display:column>
					<display:column property="address" sortable="true"
						title="${sessionScope['drac.auditlogs.list.address']}"
						class="${tdclass}">
					</display:column>
					<display:column escapeXml="true" property="billingGroup"
						sortable="true"
						title="${sessionScope['drac.auditlogs.list.billinggroup']}"
						class="${tdclass}">
					</display:column>
					<display:column sortable="true"
						title="${sessionScope['drac.auditlogs.list.severity']}"
						class="${tdclass}">
						<c:if test="${log.severity eq 'INFO'}">
							<b><bean:message key="drac.auditlogs.severity.INFO" /></b>
						</c:if>
						<c:if test="${log.severity eq 'WARNING'}">
							<b><bean:message key="drac.auditlogs.severity.WARNING" /></b>
						</c:if>
						<c:if test="${log.severity eq 'MINOR'}">
							<font color="orange"><b><bean:message
								key="drac.auditlogs.severity.MINOR" /></b></font>
						</c:if>
						<c:if test="${log.severity eq 'MAJOR'}">
							<font color="orange"><b><bean:message
								key="drac.auditlogs.severity.MAJOR" /></b></font>
						</c:if>
						<c:if test="${log.severity eq 'CRITICAL'}">
							<font color="red"><b><bean:message
								key="drac.auditlogs.severity.CRITICAL" /></b></font>
						</c:if>
					</display:column>
					<display:column sortable="true"
						title="${sessionScope['drac.auditlogs.list.category']}"
						class="${tdclass}">
						<bean:message key="drac.auditlogs.category.${log.category}" />
					</display:column>
					<display:column sortable="true"
						title="${sessionScope['drac.auditlogs.list.type']}"
						class="${tdclass}">
						<bean:message key="drac.auditlogs.logType.${log.type}" />
					</display:column>
					<display:column sortable="true"
						title="${sessionScope['drac.auditlogs.list.result']}"
						class="${tdclass}">
						<c:if test="${log.result eq 'SUCCESS'}">
							<b><bean:message key="drac.auditlogs.result.SUCCESS" /></b>
						</c:if>
						<c:if test="${log.result eq 'FAILED'}">
							<font color="red"><b><bean:message
								key="drac.auditlogs.result.FAILED" /></b></font>
						</c:if>
						<c:if test="${log.result eq 'UNKNOWN'}">
							<bean:message key="drac.auditlogs.result.UNKNOWN" />
						</c:if>
						<c:if test="${log.result eq 'NA'}">
							<bean:message key="drac.auditlogs.result.NA" />
						</c:if>
					</display:column>
					<display:column sortable="true"
						title="${sessionScope['drac.auditlogs.list.resource']}"
						class="${tdclass}">
						<c:choose>
							<c:when test="${log.category eq 'RESERVATIONGROUP'}">
								<html:link href="/schedule/querySchedule.do?sid=${log.resource}">${fn:escapeXml(log.resource)}</html:link>
							</c:when>
							<c:when test="${log.category eq 'RESERVATION'}">
								<html:link href="/service/serviceDetails.do?sid=${log.resource}">${fn:escapeXml(log.resource)}</html:link>
							</c:when>
							<c:otherwise>${fn:escapeXml(log.resource)}</c:otherwise>
						</c:choose>
					</display:column>
					<display:column property="description"
						title="${sessionScope['drac.auditlogs.list.desc']}"
						class="${tdclassL}">
					</display:column>
				</display:table></td>
			</tr>
		</table>
		 

	</logic:notEmpty>
	
</html:form>

<script language="javascript">


function initializeDate() {

  <c:if test="${not empty requestScope['QueryAuditLogForm'].startdate}" >
        setStartDate("${fn:escapeXml(QueryAuditLogForm.startdate)}");
  </c:if>
  <c:if test="${empty requestScope['QueryAuditLogForm'].startdate}">
        setStartDateToday();
  </c:if>

  <c:if test="${not empty requestScope['QueryAuditLogForm'].enddate}">
        setEndDate("${fn:escapeXml(QueryAuditLogForm.enddate)}");
  </c:if>
  <c:if test="${empty requestScope['QueryAuditLogForm'].enddate}">
        setEndDateToday();
  </c:if>

  <c:if test="${not empty requestScope['QueryAuditLogForm'].startTime}">
        setStartTime("${fn:escapeXml(QueryAuditLogForm.startTime)}");
  </c:if>
  <c:if test="${empty requestScope['QueryAuditLogForm'].startTime}">
        setStartTimePast();
  </c:if>

  <c:if test="${not empty requestScope['QueryAuditLogForm'].endTime}">
        setEndTime("${fn:escapeXml(QueryAuditLogForm.endTime)}");
  </c:if>
  <c:if test="${empty requestScope['QueryAuditLogForm'].endTime}">
        setEndTimeFuture();
  </c:if>

}
dCal = new DRACCalendar("<%=myLanguage%>", "long", serverDigitalTime);

startDateId = "startdate";
endDateId = "enddate";
startTimeId = "starttime";
endTimeId = "endtime";
dCal.addListener(dateChange);
setDatesToday();

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
