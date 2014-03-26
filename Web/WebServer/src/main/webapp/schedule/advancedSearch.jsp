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

<%@ page language="java" contentType="text/html; charset=iso-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ page session="true" errorPage="/common/dracError.jsp" %>

<%String pageRef = "drac.menu.schedule.advanced.results";%>

<%@ include file="/common/header_struts.jsp"%>
<%@ include file="/common/calendar.jsp" %>

<html:form action="/schedule/advancedSearchResults.do" method="POST">
<table cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <logic:messagesPresent>
        <tr>
            <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.schedule.create.title.errors"/></b></font></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>
        <tr>
          <td>
            <!-- Schedule information contents. -->
            <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
            <tbody>
              <tr>
                <td align="center" class="tbForm1" nowrap>
                    <html:errors/>
                </td>
            </tr>
            </tbody>
            </table>
          </td>
        </tr>
        <tr>
        <tr>
          <td>
            <!-- Drop shadow. -->
            <table border="0" cellpadding="0" cellspacing="0" class="tbl">
            <tbody>
              <tr>
                <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
              </tr>
            </tbody>
            </table>
          </td>
        </tr>
    </logic:messagesPresent>
    <logic:messagesPresent message="true">
        <html:messages id="message" message="true">
        <tr>
            <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot"><center><font color="red"><b><bean:message key="drac.schedule.create.title.errors"/></b></font></center></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>
        <tr>
          <td>
            <!-- Schedule information contents. -->
            <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
            <tbody>
              <tr>
                <td align="center" class="tbForm1" nowrap>
                    <img valign="middle" src="/images/warning.png">&nbsp;<font color="red"><b><bean:write name="message"/></b></font>
                </td>
            </tr>
            </tbody>
            </table>
          </td>
        </tr>
        <tr>
        <tr>
          <td>
            <!-- Drop shadow. -->
            <table border="0" cellpadding="0" cellspacing="0" class="tbl">
            <tbody>
              <tr>
                <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
                <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
              </tr>
            </tbody>
            </table>
          </td>
        </tr>
        </html:messages>
    </logic:messagesPresent>
    <tr>
        <td align="center" class="gen">
            <bean:message key="drac.schedule.advancedSearch.title" />
        </td>
    </tr>
    <tr>
        <td>
        <!-- Header. -->
        <table border="0" cellpadding="0" cellspacing="0" class="tbt">
            <tbody>
            <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
            <tr>
                <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                <td class="tbtbot"><center><b><bean:message key="drac.schedule.advancedSearch.title2"/></b></center></td>
                <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
            </tr>
            </tbody>
        </table>
        </td>
    </tr>
   <tr>
        <td>
            <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <logic:empty name="FindTimeForm" property="endPoints" scope="request">
                    <tr>
                        <td colspan='8' align='center' class="gen">
                            <bean:message key="drac.schedule.advancedSearch.msg.noTna" />
                        </td>
                    </tr>
                    </logic:empty>
                    <logic:notEmpty name="FindTimeForm" property="endPoints" scope="request">
                    <tr align='left'>
                        <td nowrap class="row1" align="left" class="gen">
                            <b><bean:message key="drac.schedule.advancedSearch.srcTna" /></b>
                        </td>
                        <td nowrap colspan='3' align="left" class="row1">
                            <html:select size="1" property="srcTna">
                         	  	<c:forEach var="tna" items="${endPoints}">
                          			<option value="${tna.name}" <c:if test="${querySrc eq tna.name}"> selected="selected" </c:if>><c:out value="${tna.label}"/></option>                               		
                                </c:forEach>
                            </html:select>
                        </td>
                        <td nowrap class="row1" align="left" class="gen">
                            <b><bean:message key="drac.schedule.advancedSearch.duration" /></b>
                        </td>
                        <td nowrap colspan='3' align="left" class="row1">
                            <html:text size="1" property="duration" style="width: 80px"/>&nbsp;<bean:message key="drac.text.minutes"/>
                        </td>
                    </tr>
                    <tr align='left'>
                        <td nowrap class="row1" align="left" class="gen">
                            <b><bean:message key="drac.schedule.advancedSearch.destTna" /></b>
                        </td>
                        <!-- querySrc -->
                        <td nowrap colspan='3' align="left" class="row1">
                            <html:select size="1" property="destTna">                            
                         	  	<c:forEach var="tna" items="${endPoints}">  					
                         			<option value="${tna.name}" <c:if test="${queryDest eq tna.name}">selected="selected"</c:if>><c:out value="${tna.label}"/></option> 
                                </c:forEach>
                            </html:select> 
                        </td>
                        <td nowrap class="row1" align="left" class="gen">
                            <b><bean:message key="drac.schedule.advancedSearch.bandwidth" /></b>
                        </td>
                        <td nowrap colspan='3' align="left" class="row1">
                            <html:text size="1" property="rate" style="width: 80px"/>&nbsp;<bean:message key="drac.text.Mbps"/>
                        </td>
                    </tr>
                    </logic:notEmpty>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
      <td>
        <!-- Drop shadow. -->
        <table border="0" cellpadding="0" cellspacing="0" class="tbl">
        <tbody>
          <tr>
            <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
            <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
            <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4" /></td>
          </tr>
        </tbody>
        </table>
      </td>
    </tr>
    <logic:notEmpty name="FindTimeForm" property="endPoints" scope="request">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr align='left'>
        <td colspan='4' align="center">
            <html:submit property="Query" value="Query" />
            <input type="button" name="button2" value='<c:out value='${sessionScope["drac.text.reset"]}'/>' onclick="setStartDateNow()">
        <td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    </logic:notEmpty>
</table>
<P>
<div align="center">
<logic:notEmpty name="scheduleList" scope="request">
    <% String fromDateTitle = (String) session.getAttribute("drac.schedule.advancedSearch.fromDate") + (String) request.getAttribute("TZString");
       String toDateTitle = (String) session.getAttribute("drac.schedule.advancedSearch.toDate") + (String) request.getAttribute("TZString"); %>
    <display:table id="sched" name="scheduleList" cellspacing="1" cellpadding="5" class="forumline" pagesize="20" requestURI="" sort="list">
        <c:set var="tdclass" value="row1c" />
        <c:if test="${sched_rowNum % 2 == 0}">
            <c:set var="tdclass" value="row2c" />
        </c:if>
        <display:column title="No." class="${tdclass}">${sched_rowNum}</display:column>
        <display:column title="<c:out value='${fromDateTitle}'/>" property="startDate" sortable="true" sortProperty="startTimeMillis" class="${tdclass}"/>
        <display:column title="<c:out value='${toDateTitle}'/>" property="endDate" sortProperty="endTimeMillis" sortable="true" class="${tdclass}"/>
    </display:table>
</logic:notEmpty>
</div>
</html:form>
<%@ include file="/common/footer.jsp"%>
