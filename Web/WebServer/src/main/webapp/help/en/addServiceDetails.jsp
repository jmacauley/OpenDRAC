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
		<i><b>"<c:out value='${sessionScope["drac.service.add"]}'/>"</b></i> Derive a new schedule from a selected schedule.<br/>
		In addition to the settings from the schedule the following items should be added in the presented form:
		<ol>
			<li><b>Start Time: </b>Start time for the service</li>
			<li><b>End Time: </b>End time for the service</li>
			<li><b>Diversely Route By Shared Risk Link Groups: </b> a comma separated list of group id's'</li>
			<li><b>Diversely Route By Existing Services (ID): </b> a comma separated list of service id's </li>
		</ol>
	</p> 
</span>