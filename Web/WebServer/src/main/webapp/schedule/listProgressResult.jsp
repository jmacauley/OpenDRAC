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

<%@ page session="true" errorPage="/common/dracError.jsp"%>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<%
/****************************************************************************
* OpenDRAC Web GUI
*
* File: /schedule/listProgressResult.jsp
*
* Description:
*   This page diaplays the results of a list tasks query for schedule creation
*
****************************************************************************/

String pageRef = "drac.schedule.list.createProgress.result";
%>

<%@ include file="/common/header_struts.jsp"%>

<html:form action="/schedule/clearCreateProgress.do" method="POST">
<%@ include file="/common/csrf/setCSRFToken.jsp"%>
<table width="98%" cellspacing="5" cellpadding="0" border="0"
    align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align="center">
            <logic:empty name="taskList" scope="request">
                <tr>
                    <td colspan='9' align='center' class="gen">
                        <bean:message key="drac.schedule.list.createProgress.notavailable" />
                    </td>
                </tr>
            </logic:empty>
            <logic:notEmpty name="taskList" scope="request">
            <display:table id="task" name="taskList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" sort="list" requestURI="">
                <c:set var="tdclass" value="row1c" />
                <c:if test="${task_rowNum % 2 == 0}">
                    <c:set var="tdclass" value="row2c" />
                </c:if>
                <c:set var="tdclass_err" value="row1E" />
                <c:if test="${task_rowNum % 2 == 0}">
                    <c:set var="tdclass_err" value="row2E" />
                </c:if>
                <display:column title="<input type=\"checkbox\" name=\"headerCB\" onClick=\"javascript:checkAllBoxes(this);\"/>" class="${tdclass}">
                    <input type="checkbox" name="selectedItems" value="${task.taskId}" onclick="Javascript:uncheckHeaderBox();" />
                </display:column>
                <display:column title="Progress" sortable ="true" sortProperty="taskId" class="${tdclass}">
                    <c:choose>
                        <c:when test="${task.state eq 'drac.task.DONE' && !(task.result eq 'FAILED')}">
                            <html:link action="/schedule/querySchedule.do" paramId="sid" paramName="task" paramProperty="taskId"><c:out value="${task.taskName}"/></html:link>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${task.taskName}"/>
                        </c:otherwise>
                    </c:choose>
                </display:column>
                <display:column escapeXml="true" property="taskOwner" title="${sessionScope['drac.schedule.progress.owner']}" sortable ="true" class="${tdclass}"> </display:column>
                <display:column sortProperty="submittedTime" sortable="true" title="${sessionScope['drac.schedule.progress.submitdate']}" class="${tdclass}">
                    <fmt:formatDate value="${task.submittedTime}" type="both" timeStyle="short"/>
                </display:column>
                <display:column sortProperty="state" title="${sessionScope['drac.schedule.progress.state']}" sortable="true"  class="${tdclass}">
                    <bean:message key="${task.state}"/>
                </display:column>
                <display:column title="${sessionScope['drac.schedule.progress.progress']}" sortable ="true" sortProperty="percent" class="${tdclass}">
                    <c:out value="${task.numberOfCompletedActivity}"/> <bean:message key="drac.words.of"/>
                    <c:out value="${task.totalNumberOfActivity}" /> (<c:out value="${task.percent}"/>%)
                </display:column>
                <display:column title="${sessionScope['drac.schedule.create.cancel.button']}" class="${tdclass}">
                    <c:choose>
                        <c:when test="${task.state eq 'drac.task.SUBMITTED'}">
                            <a href="/schedule/cancelCreate.do?tid=${task.taskId}"
                                onClick="Javascript:return confirmCancel('current schedule');"><img src="/images/delete.gif"></a>
                        </c:when>
                        <c:otherwise><img src="/images/no-delete.gif"></c:otherwise>
                    </c:choose>
                </display:column>
                <display:column title="${sessionScope['drac.schedule.create.result.service.status']}" sortable ="true" class="${tdclass}">                    
                    <c:choose>
                        <c:when test="${task.result eq 'SUCCESS'}">
                        	<a href="/schedule/createScheduleResult.do?sid=${task.taskId}">
                            	<bean:message key="drac.schedule.progress.result.success"/>
                            </a>
                        </c:when>
                        <c:when test="${task.result eq 'PARTIAL_SUCCESS'}">
                        	<a href="/schedule/createScheduleResult.do?sid=${task.taskId}">
                            	<bean:message key="drac.schedule.progress.result.partial"/>
                            </a>
                        </c:when>
                        <c:when test="${task.result eq 'FAILED'}">
                            <bean:message key="drac.schedule.progress.result.failed"/>
                        </c:when>
                    </c:choose>                    
                </display:column>
            </display:table>
            <br>
            <html:submit><bean:message key='drac.schedule.progress.clear.button'/></html:submit>&nbsp;
            <html:submit><bean:message key='drac.schedule.progress.refresh.button'/></html:submit><br>
            </logic:notEmpty>
        </td>
    </tr>
</table>
</html:form>

<script LANGUAGE="JavaScript">
function confirmCancel(text)
{
    var agree=confirm('<bean:message key="drac.schedule.create.status.cancel.confirm"/>' + "\n" + unescape(text));
    if (agree) {
        return true;
    } else {
        return false;
    }
}

function checkAllBoxes(headerCheckBox)
{
    var checkBoxes = document.getElementsByName('selectedItems');
    for ( i = 0; i < checkBoxes.length; i++)
        checkBoxes[i].checked = headerCheckBox.checked;
}

function uncheckHeaderBox()
{
    document.getElementsByName("headerCB")[0].checked = false;
}
</script>

<%@ include file="/common/footer.jsp"%>
