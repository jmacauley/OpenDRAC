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
		<i><b>"<c:out value='${sessionScope["drac.reporting.list.results"]}'/>"</b></i> Display of generated reports.<br/>
		A new series of reports can be defined for a new period.
		The reports can be generated in two formats:
		<ol>
			<li><b>Document: </b>Generates a CSV-file: to be read by a spreadsheet program</li>
			<li><b>HTML: </b>Displayed in a new window of this application</li>
		</ol>		
	</p> 
</span>