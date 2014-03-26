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

<%@ page session="true" errorPage="/common/dracError.jsp"
    import="com.nortel.appcore.app.drac.server.webserver.struts.common.DracHelper"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /schedule/createProgress.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page updates user on create schedule progress
 *
 ****************************************************************************/


String pageRef = "drac.schedule.create.progress";
String schName = (String) request.getParameter("name");
String schId = (String) request.getParameter("sid");
String websafename = request.getParameter("webSafeName");

request.setAttribute("pageRef", pageRef);
request.setAttribute("schName", schName);
request.setAttribute("schId", schId);
request.setAttribute("websafename", websafename);
 %>


<%@ include file="/common/header_struts.jsp" %>

<form method="post" name="ProgressForm" action="/schedule/cancelCreate.do?tid<c:out value='${schId}'/>&name=<c:out value='${websafename}'/>">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
<table width="90%" cellspacing="5" cellpadding="0" border="0" align="center">
    <tr>
      <td><img src="/images/spacer.gif" height="5" /></td>
    </tr>
    <tr />
    <tr>
      <td align="center">
        <bean:message key="drac.schedule.name"/>: ${fn:escapeXml(param.name)} <br>
        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td><img src="/images/progress-bar-top-left.gif"></td>
            <td background="/images/progress-bar-top.gif"></td>
            <td><img src="/images/progress-bar-top-right.gif"></td>
          </tr>
          <tr>
            <td><img src="/images/progress-bar-left.gif" width="4" height="20"></td>
            <td>
              <div id="progress"></div>
            </td>
            <td align="right"><img src="/images/progress-bar-right.gif" width="4" height="20"></td>
          </tr>
          <tr>
            <td><img src="/images/progress-bar-bottom-left.gif"></td>
            <td background="/images/progress-bar-bottom.gif"></td>
            <td><img src="/images/progress-bar-bottom-right.gif"></td>
          </tr>
        </table>
     </tr>
    <tr>
      <td><img src="/images/spacer.gif" height="5" /></td>
    </tr>
    <tr>
        <td align="center">
            <input type="submit" id="cancel" value='<bean:message key="drac.schedule.create.cancel.button"/>' onclick="Javascript:return confirmCancel(${fn:escapeXml(param.name)});"/>
        </td>
    </tr>
    <tr>
      <td><img src="/images/spacer.gif" height="5" /></td>
    </tr>
    <tr>
        <td align="center">
        <div id="status"></div><p>
        <a href="/schedule/listCreateProgress.do"><bean:message key="drac.menu.schedule.createProgress.list"/></a>
      </td>
    </tr>
</table>
</form>

<script language="javascript">
var isIE = false;
var req;
var prevPercent = 0;
var centerCell;
var numBlocks=50;
var blockSize = 100/numBlocks;
var scheduleId;
var scheduleName;

function initialize() {
    prevPercent = 0;
    createProgressBar();
    updateProgressBar(0);
    document.body.style.cursor = "wait";
    scheduleId = "<c:out value='${schId}'/>";
    scheduleName = "<c:out value='${websafename}'/>";
    setTimeout("pollStatus()", 5000);
}

function finished() {
    var statusDiv = document.getElementById("status");
    statusDiv.innerHTML = "<c:out value='${sessionScope["drac.schedule.create.status.completed"]}'/>";
    document.body.style.cursor = "default";
}

function pollStatus() {
    var url = "/drac?action=pollStatus&sid=" + scheduleId;
    if (window.XMLHttpRequest) {
         req = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
         req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    req.open("GET", url, true);
    req.onreadystatechange = processStatusResponse;
    req.send(null);
}

function processStatusResponse() {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var item = req.responseXML.getElementsByTagName("state")[0];
            var state = item.firstChild.nodeValue;
            //alert("state [" + state + "]");
            if (state != 'SUBMITTED') {
                document.ProgressForm.cancel.disabled = true;
            }

            if (state == 'DONE') {
                updateProgressBar(100);
                item = req.responseXML.getElementsByTagName("error")[0];
                if (item) {
                    var error = item.firstChild.nodeValue;
                    //alert("error [" + error + "]");
                    doError(error);
                } else {
                    setTimeout("finished()", 5000);
                    setTimeout("doForward()", 5000);
                }
            } else {
                item = req.responseXML.getElementsByTagName("percent")[0];
                var percent = item.firstChild.nodeValue;
                //alert("percent " + percent);
                updateProgressBar(percent);
                item = req.responseXML.getElementsByTagName("total")[0];
                var total = item.firstChild.nodeValue;
                //alert("total " + total);
                item = req.responseXML.getElementsByTagName("completed")[0];
                var completed = item.firstChild.nodeValue;
                //alert("completed " + completed);
                updateStatus(total, completed, state);
                prevPercent = percent;
                //alert("prevPercent" + prevPercent);
                if (prevPercent < 100) {
                    setTimeout("pollStatus()", 5000);
                } else if (prevPercent == 100) {
                    //document.ProgressForm.cancel.disabled = true;
                    setTimeout("finished()", 5000);
                    setTimeout("doForward()", 5000);
                }
            }
        }
    }
}

function createProgressBar() {
    var centerCellName;
    var tableText = "";
    for (x = 0; x < numBlocks; x++) {
      tableText += "<td id=\"block" + x + "\" width=\"10\" height=\"10\" bgcolor=\"#003399\"/>";
      if (x == (numBlocks/2)) {
          centerCellName = "block" + x;
      }
    }
    var progressDiv = document.getElementById("progress");
    progressDiv.innerHTML = "<table class=\"forumline\" with=\"100\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>" + tableText + "</tr></table>";
    centerCell = document.getElementById(centerCellName);
    var statusDiv = document.getElementById("status");
    statusDiv.innerHTML = "<c:out value='${sessionScope["drac.schedule.create.status"]}'/>";
}

function updateStatus(total, completed, state) {
    var statusDiv = document.getElementById("status");
    if (state == "SUBMITED")
    	state = "<c:out value='${sessionScope["drac.task.SUBMITED"]}'/>";
    else if (state == "DONE")
    	state = "<c:out value='${sessionScope["drac.task.DONE"]}'/>";
    else if (state == "ABORTED")
    	state = "<c:out value='${sessionScope["drac.task.ABORTED"]}'/>";
    else if (state =="IN_PROGRESS")
    	state = "<c:out value='${sessionScope["drac.task.IN_PROGRESS"]}'/>";

    message = "<c:out value='${sessionScope["drac.schedule.create.status"]}'/> " + state + "<br><b>" +
    	completed + "</b> <c:out value='${sessionScope["drac.words.of"]}'/> <b>" + total +
    	"</b> <c:out value='${sessionScope["drac.schedule.create.status.complete"]}'/>";
    statusDiv.innerHTML = message;
}

function updateProgressBar(percentage) {
    var percentageText = "";
    if (percentage < 10) {
        percentageText = "&nbsp;" + percentage;
    } else {
        percentageText = percentage;
    }
    centerCell.innerHTML = "<font color=\"orange\"><b>" + percentageText + "%</b></font>";
    var tableText = "";

    // How many cells are done?
    doneBlock = (percentage/100) * numBlocks;

    // Loop through cells, update background color
    for (x = 0; x < numBlocks; x++) {
      var cell = document.getElementById("block" + x);
      if ((cell) && (x < doneBlock)) {
        cell.style.backgroundColor = "#003399";
      } else {
        cell.style.backgroundColor = "#ECECEC";
      }
    }
}

function doError(message) {
    req = null;
    var statusDiv = document.getElementById("status");
    if(message.indexOf("A VlanID must be specified") >0){
    	message +="<br>Create a schedule with this port using 'Create advanced schedule'"
    }
    
    statusDiv.innerHTML = "<font color=\"red\"><b>" + message + "</b></font>";
    document.body.style.cursor = "default";
}

function doForward() {
    req = null;
    location.href="/schedule/createScheduleResult.do?sid=" + scheduleId + "&name=" + scheduleName;
}

function confirmCancel(text)
{
    var agree=confirm('<bean:message key="drac.schedule.create.status.cancel.confirm"/>' + "\n" + unescape(text));
    if (agree) {
        return true;
    } else {
        return false;
    }
}
initialize();
</script>


<%@ include file="/common/footer.jsp" %>

