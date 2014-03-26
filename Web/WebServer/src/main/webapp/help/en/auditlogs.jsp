<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page errorPage="/common/dracError.jsp"%>
<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /help/en/download.jsp
 *
 * Description:
 *   This page displays US English help data for the download functionality.
 *
 ****************************************************************************/
%>
<span class="gen">
	<p>
		<i><b>"<c:out value='${sessionScope["drac.help.auditlogs"]}'/>"</b></i> Retrieve an overview of logged events.<br/>
		These events can be filtered according to the following options:
		<ul>
			<li><b>Start Time: </b>Start time for the requested log history</li>
			<li><b>End Time: </b>End time for the requested log history</li>
			<li><b>Category: </b>Subject type of logged event</li>
			<li><b>Log Type: </b>Event type of the logged event</li>
			<li><b>Result: </b>Result of  the action for which the logging was done</li>
			<li><b>Severity: </b>The severity of the logged event, particularly for errors.</li>
			<li><b>IP Address: </b>IP -address of the user that cause the logged event, as seen by openDrac</li>
			<li><b>Billing Group: </b>Billing group of the user that cause the logged event</li>
			<li><b>Resource: </b>User nameof the user that cause the logged event</li>
		</ul>
	</p> 
</span>