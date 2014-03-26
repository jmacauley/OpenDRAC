<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
		<i><b>"<c:out value='${sessionScope["drac.service.alarms.query"]}'/>"</b></i> Retrieve a list of service alarms for a given period.<br/>
		For the query the following parameters are available:
		<ol>
			<li><b>Member Group: </b> group for which alarms are available</li>
			<li><b>Start Time: </b>Start time for query</li>
			<li><b>End Time: </b>End time for the query</li>			
		</ol>
	</p> 
</span>