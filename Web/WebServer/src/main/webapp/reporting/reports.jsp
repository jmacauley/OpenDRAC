<%@ page language="java" contentType="text/html ; charset=ISO-8859-1" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ page session="true" errorPage="/common/dracError.jsp"%>
<%@ page import="org.opendrac.server.nrb.reporting.*" %>
<%
String pageRef = "drac.reporting.list.results";
%>

<%@ include file="/common/header_struts.jsp"%>
<%@ include file="/common/csrf/setCSRFToken.jsp"%>

<%@ include file="/common/calendar.jsp"%>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/calendar.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/reporting.js"></SCRIPT>
<script type="text/javascript" src="/scripts/handleExpand.js"></script>
<script type="text/javascript" src="/scripts/EditableList.js"></script>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>

<table cellspacing="5" cellpadding="0" border="0" align="center">

<tr><td>
<div class="reportFilterMetaDataLetf">
<table>
    <tr>
        <td class="gen">
        <bean:message key="drac.result.filter" arg0="${fn:escapeXml(requestScope['startdate'])}" arg1="${fn:escapeXml(requestScope['enddate'])}"/><br>
        <bean:message key="drac.schedule.list.filter.group.title"/>&nbsp;
            <c:if test="${requestScope['filterGroup'] eq 'all'}">
                <i><bean:message key="drac.schedule.list.filter.group.all"/></i>
            </c:if>
            <c:if test="${requestScope['filterGroup'] ne 'all'}"><i>${fn:escapeXml(requestScope['filterGroup'])}</i></c:if>

        </td>
    </tr>
    <logic:messagesPresent message="true">
        <html:messages id="message" message="true" property="errors">
        <tr>
            <td align="center" class="gen">
                <font color="red"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
        <html:messages id="message" message="true" property="success">
        <tr>
            <td align="center" class="gen">
                <font color="green"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
    </logic:messagesPresent>
</table>
</div>
<div class="reportFilterMetaData">
<html:form action="/downloadReport" method="POST">
	<table cellspacing="0" cellpadding="0" border="0" align="center">
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

</html:form>
</div>
<div class="clear"></</div>
</td></tr>
<script language="javascript">


function initializeDate() {

  <c:if test="${not empty requestScope['GenerateReportForm'].startdate}" >
        setStartDate("${fn:escapeXml(GenerateReportForm.startdate)}");
  </c:if>
  <c:if test="${empty requestScope['GenerateReportForm'].startdate}">
        setStartDateToday();
  </c:if>

  <c:if test="${not empty requestScope['GenerateReportForm'].enddate}">
        setEndDate("${fn:escapeXml(GenerateReportForm.enddate)}");
  </c:if>
  <c:if test="${empty requestScope['GenerateReportForm'].enddate}">
        setEndDateToday();
  </c:if>

  <c:if test="${not empty requestScope['GenerateReportForm'].startTime}">
        setStartTime("${fn:escapeXml(GenerateReportForm.startTime)}");
  </c:if>
  <c:if test="${empty requestScope['GenerateReportForm'].startTime}">
        setStartTimePast();
  </c:if>

  <c:if test="${not empty requestScope['GenerateReportForm'].endTime}">
        setEndTime("${fn:escapeXml(GenerateReportForm.endTime)}");
  </c:if>
  <c:if test="${empty requestScope['GenerateReportForm'].endTime}">
        setEndTimeFuture();
  </c:if>

}

dCal = new DRACCalendar('<c:out value="${myLanguage}" />', "long", serverDigitalTime);
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


  <tr><td>
  <logic:empty name="reports" scope="request">
  	<div class="emptyreports"><bean:message key="drac.reporting.list.results.notavailable" /></div>
  </logic:empty>
  <logic:notEmpty name="reports" scope="request">
		<div class="reports">
			<c:forEach var="report" items="${reports}">	  
			<div class="reportMetaData"><c:out value="${report.getReportData().getMetadata()}"/></div>      
             <table class="forumline reportForm" cellspacing="1" cellpadding="5">
            	<thead>
            		<tr>
            			<c:forEach var="tableKey" items="${report.getTableKeys()}">
            				<c:forEach var="collName" items="${report.getReportData().getCollumnNames()}">  
								<c:if test="${collName.key eq tableKey}">         				
            						<th><c:out value="${collName.value} "/></th>
            					</c:if>
            				</c:forEach>
            			</c:forEach>   
            		</tr>
            	</thead>
            	<tfoot>
					<tr>
						<td colspan="<c:out value="${fn:length(report.getTableKeys())}"/>"></td>						
					</tr>
				</tfoot>
           		<c:forEach items="${report.getReportData().getData()}" var="item" varStatus="rowCounter">
					<c:choose>
			          <c:when test="${rowCounter.count % 2 == 0}"><c:set var="rowStyle" scope="page" value="odd"/><c:set var="tdclass" value="row1" /></c:when>
			          <c:otherwise> <c:set var="rowStyle" scope="page" value="even"/><c:set var="tdclass" value="row2" /></c:otherwise>
			        </c:choose>           		
           			<tr class="<c:out value="${rowStyle}"/>">
           			
           				<c:choose>
	           				<c:when test="${item.isSeparator()}">
		       					<td class="<c:out value="${tdclass}"/>"><c:out value='${item.get("value")}'/></td>
		       					<td class="<c:out value="${tdclass}"/>" colspan="${fn:length(report.getTableKeys())-1}">&#160;</td>
		    				</c:when>
		    				 
		    				<c:otherwise>
		    					<c:forEach var="key" items="${report.getTableKeys()}">
		    						<c:forEach var="mapEntry" items="${item.getData()}">
		    							<c:if test="${mapEntry.key eq key}"> 
		    								<td class="<c:out value="${tdclass}"/>"><c:out value='${item.getData().get(key)}'/></td>
		    							</c:if> 
		    						</c:forEach> 
		    					</c:forEach>	
		    				</c:otherwise>   
	    				</c:choose>		
	    				
           			</tr>
           		</c:forEach>
             </table>       
    	</c:forEach>
    </div>
     </logic:notEmpty>
    </td></tr>
    <%@ include file="/common/loading.jsp"%>
    <tr>
        <td>
            <img src="/images/spacer.gif" height="10" />
        </td>
    </tr>
</table>


<%@ include file="/common/footer.jsp"%>


