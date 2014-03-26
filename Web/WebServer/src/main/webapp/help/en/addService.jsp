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
		<i><b>"<c:out value='${sessionScope["drac.service.add"]}'/>"</b></i> Create a new service that is derived from one of the listed schedules.<br/>
		To create a new service click on the big plus in the collumn "Add" in the row with the schedule.
		This schedule will be a template for the new service. All the services created with that schedule will be grouped together.
	</p> 
</span>