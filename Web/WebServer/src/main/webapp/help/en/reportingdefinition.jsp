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
<p/>
<span class="gen">

	<i><b>"<c:out value='${sessionScope["drac.reporting.definition"]}'/>"</b></i> Define the period for which the reports are to be generated.<br/>
	This period is defined by a start time and an end time.<br/>
	The reports can be generated in two formats:
</span>
<ol>
	<li><b>Document: </b>Generates a CSV-file: to be read by a spreadsheet program</li>
	<li><b>HTML: </b>Displayed in a new window of this application</li>
</ol>

