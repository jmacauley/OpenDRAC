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

<%@ page session="true" errorPage="/common/dracError.jsp" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /schedule/HandleCreateScheduleSuccess.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page returns result of creating a schedule
 *
 ****************************************************************************/

String pageRef = "drac.schedule.create.result";

%>


<%@ include file="/common/header_struts.jsp" %>

<html:form action="/schedule/createScheduleResult" >
<table width="90%" cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <logic:messagesPresent message="true">
        <html:messages id="message" message="true">
        <tr>
            <td align="center" class="gen">
                <font color="red"><b><bean:write name="message"/></b></font>
            </td>
        </tr>
        </html:messages>
    </logic:messagesPresent>
    <logic:messagesNotPresent message="true">
    <logic:notEmpty name="ScheduleForm" scope="request">
    <tr>
        <td align="center" class="gen">
            <c:choose>
                <c:when test="${ScheduleForm.createResult eq 'SUCCESS'}">
                    <font color="green"><b><bean:message key="drac.schedule.create.result.success"/></b></font>
                </c:when>
                <c:when test="${ScheduleForm.createResult eq 'PARTIAL_SUCCESS'}">
                    <font color="orange"><b><bean:message key="drac.schedule.create.result.partial"/></b></font>
                </c:when>
                <c:when test="${ScheduleForm.createResult eq 'FAILED'}">
                    <font color="red"><b><bean:message key="drac.schedule.create.result.failed"/></b></font>
                </c:when>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>
        <table cellpadding="0" cellspacing="0" border="0" align="center">
            <tr>
                <td>
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                    <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.schedule.detail"/></b></center></td>
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
                        <tr align='left'>
                            <td class="tbForm1" align="left" class="gen">
                                <b><bean:message key="drac.schedule.name" /></b>
                            </td>
                            <td class="tbForm1" align="left" class="gen">${fn:escapeXml(ScheduleForm.name)}</td>
                        </tr>
                        <tr align='left'>
                            <td class="tbForm1" align="left" class="gen">
                                <b><bean:message key="drac.schedule.create.starttime" /></b>
                            </td>
                            <td class="tbForm1" align="left" class="gen">${fn:escapeXml(ScheduleForm.startDate)}</td>
                        </tr>
                        <tr align='left'>
                            <td class="tbForm1" align="left" class="gen">

                                <b><bean:message key="drac.schedule.create.endtime" /></b>
                            </td>
                            <td class="tbForm1" align="left" class="gen">${fn:escapeXml(ScheduleForm.endDate)}</td>
                        </tr>

                        <logic:notEmpty name="ScheduleForm" property="recurrenceType">
                        <tr align='left'>
                            <td class="tbForm1" align="left" class="gen">
                                <b><bean:message key="drac.schedule.detail.type" /></b>
                            </td>
                            <td class="tbForm1" align="left" class="gen">${fn:escapeXml(ScheduleForm.recurrenceType)}</td>
                        </tr>
                        </logic:notEmpty>
                        <tr align='left'>
                            <td class="tbForm1" align="left" class="gen">
                                <b><bean:message key="drac.schedule.activationType" /></b>
                            </td>
                            <td class="tbForm1" align="left" class="gen"><bean:message key="drac.reservation.type.${ScheduleForm.activationType}"/></td>
                        </tr>
                        <tr align='left'>
                            <td class="tbForm1" align="left" class="gen">
                                <b><bean:message key="drac.schedule.create.success.recinfo.status" /></b>
                            </td>
                            <td class="tbForm1" align="left" class="gen">
                                <c:if test="${ScheduleForm.createResult ne 'FAILED'}">
                                    <bean:message key="drac.reservation.status.${ScheduleForm.status}"/>
                                </c:if>
                                <c:if test="${ScheduleForm.createResult eq 'FAILED'}">
                                    --
                                </c:if>
                            </td>
                        </tr>
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
            <tr>
                <td>
                    <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                        <tbody>
                        <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                        <tr>
                            <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                            <td class="tbtbot"><center><b><bean:message key="drac.schedule.detail.path"/></b></center></td>
                            <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" alt="" /></td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <table cellspacing="0" cellpadding="0" border="0" align="center" class="tbForm">
                        <tr>
                            <td class="tbForm1">
                                <b><bean:message key="drac.schedule.sourcePort" /></b>
                            </td>
                            <td class="tbForm1">
                                <c:out value="${ScheduleForm.srcTNA}" />
                            </td>
                        </tr>

			 <tr>
			     <td class="tbForm1">
				 <b><bean:message key="drac.schedule.sourcePortLabel" /></b>
			     </td>
			     <td class="tbForm1" nowrap>${fn:escapeXml(ScheduleForm.srcFacLabel)}</td>
			 </tr>

			 <c:if test="${ScheduleForm.srcVlanId ne ''}">
					 <tr>
				     <td class="tbForm1">
					 <b><bean:message key="drac.schedule.sourceVlanId" /></b>
				     </td>
				     <td class="tbForm1" nowrap>${fn:escapeXml(ScheduleForm.srcVlanId)}</td>
				 </tr>
			 </c:if>


                        <c:if test="${ScheduleForm.srcCh ne ''}">
                        <tr>
                            <td class="tbForm1">
                                <b><bean:message key="drac.schedule.detail.src.channel"/><b>
                            </td>
                            <td class="tbForm1">
                                <c:choose>
                                    <c:when test="${ScheduleForm.srcCh eq 'drac.schedule.detail.channel.auto'}">
                                        <bean:message key="drac.schedule.detail.channel.auto"/>
                                    </c:when>
                                    <c:otherwise>${fn:escapeXml(ScheduleForm.srcCh)}</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        </c:if>
                        <tr>
                            <td class="tbForm1">
                                <b><bean:message key="drac.schedule.destPort" /></b>
                            </td>
                            <td class="tbForm1">
                                <c:out value="${ScheduleForm.destTNA}" />
                            </td>
                        </tr>
                            <tr>
                                <td class="tbForm1">
                                    <b><bean:message key="drac.schedule.destPortLabel" /></b>
                                </td>
                                <td class="tbForm1" nowrap>${fn:escapeXml(ScheduleForm.destFacLabel)}</td>
                            </tr>

                            <c:if test="${ScheduleForm.destVlanId ne '' }">
                            <tr>
                                <td class="tbForm1">
                                    <b><bean:message key="drac.schedule.destVlanId" /></b>
                                </td>
                                <td class="tbForm1" nowrap>${fn:escapeXml(ScheduleForm.destVlanId)}</td>
                            </tr>
                            </c:if>




                        <c:if test="${ScheduleForm.destCh ne ''}">
                        <tr>
                            <td class="tbForm1">
                                <b><bean:message key="drac.schedule.detail.dest.channel"/><b>
                            </td>
                            <td class="tbForm1">
                                <c:choose>
                                    <c:when test="${ScheduleForm.destCh eq 'drac.schedule.detail.channel.auto'}">
                                        <bean:message key="drac.schedule.detail.channel.auto"/>
                                    </c:when>
                                    <c:otherwise>${fn:escapeXml(ScheduleForm.destCh)}</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        </c:if>
                        <tr>
                            <td class="tbForm1">
                                <b><bean:message key="drac.schedule.rate" /></b>
                            </td>
                            <td class="tbForm1">
                                <c:out value="${ScheduleForm.rate}" />
                                &nbsp;
                                <bean:message key="drac.text.Mbps" />
                            </td>
                        </tr>
                        <tr>
                            <td class="tbForm1">
                                <b><bean:message key="drac.schedule.detail.protection" /></b>
                            </td>
                            <td class="tbForm1"><bean:message key="drac.schedule.protection.${ScheduleForm.protectionType}"/></td>
                        </tr>
                        <tr>
                            <td class="tbForm1">
                                <b><bean:message key="drac.schedule.detail.srlg"/><b>
                            </td>
                            <td class="tbForm1">
                                <c:choose>
                                    <c:when test="${ScheduleForm.srlg eq ''}">
                                        <bean:message key="drac.text.none"/>
                                    </c:when>
                                    <c:otherwise>${fn:escapeXml(ScheduleForm.srlg)}</c:otherwise>
                                </c:choose>
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
        </table>

        <table cellpadding="0" cellspacing="0" border="0" align="center">
            <logic:notEmpty name="ScheduleForm" property="services">
            <tr>
                <td>
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                    <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.schedule.create.result.service.summary"/></b></center></td>
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
                            <th class="tbForm1" align="center">
                                <b><bean:message key="drac.schedule.create.result.serviceNo" /></b>
                            </th>
                            <th class="tbForm1" align="left">
                                <b><bean:message key="drac.schedule.create.starttime" /></b>
                            </th>
                            <th class="tbForm1" align="left">
                                <b><bean:message key="drac.schedule.create.endtime" /></b>
                            </th>
                            <th class="tbForm1" align="left">
                                <b><bean:message key="drac.schedule.create.result.service.message" /></b>
                            </th>
                        <logic:iterate id="service" indexId="cnt" name="ScheduleForm" property="services">
                            <tr align='left'>
                                <c:set var="tdclass" value="row2" />
                                <c:if test="${cnt % 2 == 0}">
                                    <c:set var="tdclass" value="row1" />
                                </c:if>
                                <td class="${tdclass}" align="left" nowrap>
                                    ${cnt + 1}
                                </td>
                                <td class="${tdclass}" align="left" nowrap>
                                    ${fn:escapeXml(service.startdate)}
                                </td>
                                <td class="${tdclass}" align="left" nowrap>
                                    ${fn:escapeXml(service.enddate)}
                                </td>
                                <td class="${tdclass}" align="left">
                                    <c:choose>
                                        <c:when test="${service.status eq 'INFO'}">
                                            ${fn:escapeXml(service.message)}
                                        </c:when>
                                        <c:when test="${service.status eq 'WARNING'}">
                                            <font color="orange"><b>${fn:escapeXml(service.message)}</b></font>
                                        </c:when>
                                        <c:when test="${service.status eq 'ERROR'}">
                                            <font color="red"><b>${fn:escapeXml(service.message)}</b></font>
                                        </c:when>
                                        <c:otherwise>
                                            &nbsp;
                                        </c:otherwise>
                                    </c:choose>
                                </td>
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
        </table>
        </td>
    </tr>
    </logic:notEmpty>
    </logic:messagesNotPresent>
</table>
</html:form>
<%@ include file="/common/footer.jsp"%>

