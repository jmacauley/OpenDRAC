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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=iso-8859-1"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri='/WEB-INF/tld/cewolf.tld' prefix='cewolf' %>


<%@ page session="true" errorPage="/common/dracError.jsp"%>
<%String pageRef = "drac.network.viewUtil"; %>
<%@ include file="/common/header_struts.jsp"%>

<SCRIPT LANGUAGE="JavaScript" SRC="/scripts/overlib.js"></SCRIPT>

<html:form action="/network/queryUtilList" method="POST">

    <table width="90%" cellspacing="0" cellpadding="0" border="0" align="center">
        <tr>
            <td>
                <img src="/images/spacer.gif" height="5" />
            </td>
        </tr>
        <c:if test="${requestScope['range'] eq 'oneDay'}">
           <c:set var="rangeTitle" value="${sessionScope['drac.network.utilization.range.oneDay']}"/>
        </c:if>
        <c:if test="${requestScope['range'] eq 'oneWeek'}">
           <c:set var="rangeTitle" value="${sessionScope['drac.network.utilization.range.oneWeek']}"/>
        </c:if>
        <c:if test="${requestScope['range'] eq 'oneMonth'}">
           <c:set var="rangeTitle" value="${sessionScope['drac.network.utilization.range.oneMonth']}"/>
        </c:if>
        <c:if test="${requestScope['range'] eq 'threeMonth'}">
           <c:set var="rangeTitle" value="${sessionScope['drac.network.utilization.range.threeMonth']}"/>
        </c:if>

        <logic:empty name="utilList" scope="request">
            <tr>
                <td colspan='8' align='center' class="gen">
                    <bean:message key="drac.network.utilization.noservices" arg0="${requestScope['date']}"/> (${rangeTitle})
                </td>
            </tr>
        </logic:empty>
        <logic:notEmpty name="utilList" scope="request">
        <tr>
            <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                    <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    <td class="tbtbot" align="center"><b><bean:write name="tna" scope="request"/> : <bean:write name="date" scope="request"/> (${rangeTitle}) </b></td>
                    <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>
        <tr>
            <td>
                <table cellspacing="0" cellpadding="5" border="1" align="center" class="tbForm">
                <tbody>
                    <tr>
                        <th class="rowth" align='center' nowrap>
                            <b><bean:message key="drac.schedule.detail.sno" /></b>
                        </th>
                        <th class="rowth" align='left' nowrap>
                            <b><bean:message key="drac.schedule.detail.name" /></b>
                        </th>
                        <th class="rowth" align='left' nowrap>
                            <b><bean:message key="drac.schedule.detail.serviceid" /></b>
                        </th>
                        <th class="rowth" align='left' nowrap>
                            <b><bean:message key="drac.schedule.rate" />(Mbps)</b>
                        </th>
                        <th class="rowth" align='left' nowrap>
                            <b><bean:message key="drac.schedule.create.starttime" /><c:out value="${param.TZString}" /></b>
                        </th>
                        <th class="rowth" align='left' nowrap>
                            <b><bean:message key="drac.schedule.create.endtime" /><c:out value="${param.TZString}" /></b>
                        </th>
                        <th class="rowth" align='left' nowrap>
                            <b><bean:message key="drac.service.details.status" /></b>
                        </th>

                    </tr>
                    <logic:iterate id="service" indexId="count" name="utilList" scope="request">
                        <tr>
                            <c:set var="tdclass" value="row2" />
                            <c:if test="${count % 2 == 0}">
                                <c:set var="tdclass" value="row1" />
                            </c:if>
                            <td nowrap align="center" class="<c:out value="${tdclass}"/>">
                                <c:out value="${count+1}" />
                            </td>
                            <td nowrap align="center" class="<c:out value="${tdclass}Left"/>">
                                <a href="/schedule/querySchedule.do?sid=<c:out value="${service.scheduleId}" />">${service.scheduleName}</a>
                            </td>
                            <td nowrap align="center" class="<c:out value="${tdclass}Left"/>">
                                <a
                                    href="/service/serviceDetails.do?sid=<c:out value="${service.serviceID}"/>">
                                    <c:out value="${service.serviceID}" />
                                </a>
                            </td>
                            <td nowrap align="center" class="<c:out value="${tdclass}Left"/>">${service.rate}</td>
                            <td nowrap align="center" class="<c:out value="${tdclass}Left"/>">${service.startDateForList}</td>
                            <td nowrap align="center" class="<c:out value="${tdclass}Left"/>">${service.endDateForList}</td>
                            <td nowrap align="center" class="<c:out value="${tdclass}Left"/>"><bean:message key="drac.reservation.status.${service.status}"/></td>
                        </tr>
                    </logic:iterate>
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
        </logic:notEmpty>
        <tr>
            <td><img src="/images/spacer.gif" alt="" height="14"></td>
        </tr>
        <tr>
           <td align="center" border='1'>
                <bean:define id="utilizationData" name="utilCount" scope="request" />
                <jsp:useBean id="axisRange" scope="page" class="com.nortel.appcore.app.drac.server.webserver.struts.network.graphicalview.DracChartPostProcessor"/>
                <cewolf:chart
                    id="bar"
                    title="${sessionScope['drac.network.utilization.graph.title']}"
                    type="verticalxybar"
                    xaxislabel="${sessionScope['drac.network.utilization.graph.xaxis']}"
                    yaxislabel="${sessionScope['drac.network.utilization.graph.yaxis']}">
                    <cewolf:data>
                        <cewolf:producer id="utilizationData"/>
                    </cewolf:data>
                    <cewolf:chartpostprocessor id="axisRange"/>


                </cewolf:chart>
                <cewolf:img chartid="bar" renderer="cewolf" width="800" height="600">
                    <cewolf:map tooltipgeneratorid="utilizationData"/>
                </cewolf:img>
            </td>
        </tr>
    </table>
</html:form>

<%@ include file="/common/footer.jsp"%>
