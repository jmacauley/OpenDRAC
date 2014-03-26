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
		<i><b>"<c:out value='${sessionScope["drac.help.message"]}'/>"</b></i> Show a selected sent message.<br/>
		The message can be altered and sent again.
	</p> 
</span>