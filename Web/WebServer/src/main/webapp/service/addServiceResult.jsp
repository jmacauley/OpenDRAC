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
<%@ page session="true" errorPage="/common/dracError.jsp"%>


<%
String pageRef = "drac.service.addResult";
%>


<%@ include file="/common/header_struts.jsp"%>
<html:form action="/service/serviceDetails" method="POST">
<table width="450" cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <logic:messagesPresent message="true">
    <tr>
        <td align="center" class="gen"><b><font color="red"><bean:message key="drac.service.addResult.failure"/></font></b></td>
    </tr>
    <html:messages id="message" message="true">
    <tr>
        <td align="center" class="gen">
            <font color="red"><b><bean:write name="message"/></b></font>
        </td>
    </tr>
    </html:messages>
    </logic:messagesPresent>
    <logic:messagesNotPresent message="true">
    <c:if test='${ServiceForm.serviceID != null && ServiceForm.serviceID != ""}'>
    <tr>
        <td align="center">
            <font color="green"><b><bean:message key="drac.service.addResult.success"/></b></font><br>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot" align="center"><b><bean:message key="drac.service.details" /></b></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr align="center">
        <td>
            <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.serviceID" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.serviceID}" />
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.status" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap><bean:message key="drac.reservation.status.${ServiceForm.status}"/></td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.startdate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.startdate}" />
                    </td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.enddate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.enddate}" />
                    </td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.rate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.rate}" />
                        &nbsp;
                        <bean:message key="drac.text.Mbps" />
                    </td>
                </tr>
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
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot" align="center"><b><bean:message key="drac.service.details.call" /></b></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr align="center">
        <td>
            <logic:notEmpty name="ServiceForm" property="calls">
            <logic:iterate id="callType" name="ServiceForm" property="calls">
            <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.callId" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${callType.callID}" />
                    </td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.status" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <bean:message key="drac.reservation.status.${callType.callStatus}" />
                    </td>
                </tr>
            </table>
            </logic:iterate>
            </logic:notEmpty>
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
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot" align="center"><b><bean:message key="drac.service.details.scheduleDetails" /></b></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr align="center">
        <td>
            <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleID" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                    <a href="/schedule/querySchedule.do?sid=<c:out value="${ServiceForm.scheduleId}" />">
                        <c:out value="${ServiceForm.scheduleId}" /></a>
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.schedulename" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.scheduleName}" />
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.srcTNA" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.scheduleSrcTNA}" />
                    </td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.destTNA" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.scheduleDestTNA}" />
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleStartdate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.scheduleStartdate}" />
                    </td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleEnddate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.scheduleEnddate}" />
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleRate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:out value="${ServiceForm.scheduleRate}" />
                        &nbsp;
                        <bean:message key="drac.text.Mbps" />
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleStatus" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap><bean:message key="drac.reservation.status.${ServiceForm.scheduleStatus}"/></td>
                </tr>
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
    </c:if>

    </logic:messagesNotPresent>
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
</table>
</html:form>

<%@ include file="/common/footer.jsp"%>
