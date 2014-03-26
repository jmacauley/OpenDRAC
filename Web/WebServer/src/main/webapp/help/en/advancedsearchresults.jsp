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
		<i><b>"<c:out value='${sessionScope["drac.menu.service.advanced.results"]}'/>"</b></i> Search results with available time slots to create a schedule.<br/>
		From this page a new sreach can be started. To do the search the following criteria must be entered:
		<ol>
			<li><b>Source Port: </b>Source port for which the availabilty of free time slots should be calculated</li>
			<li><b>Destination Port: </b>Displayed in a new window of this application</li>
			<li><b>Duration: </b>Duration of the schedule</li>			
			<li><b>Rate: </b>Rate in Mb of the schedule</li>			
		</ol>		
	</p> 
</span>