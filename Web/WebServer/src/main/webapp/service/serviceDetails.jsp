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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page session="true" errorPage="/common/dracError.jsp"%>


<%
String pageRef = "drac.service.details";
%>

<%@ include file="/schedule/extendServiceScripts.jsp" %>
<%@ include file="/common/header_struts.jsp"%>

<script type="text/javascript" src="/scripts/ajax.js"></script>
<script type="text/javascript" src="/scripts/HoldButton.js"></script>
<script type="text/javascript" src="/scripts/dhtmlwindow.js"></script>

<div id="loadingDiv" style="display:none">
</div>
<html:form action="/service/serviceDetails" method="POST">
<input type="hidden" value="<c:out value="${ServiceForm.serviceID}" />" id="serviceId" name="serviceId"/>
<table width="450" cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <c:if test='${ServiceForm.serviceID != null && ServiceForm.serviceID != ""}'>
    <tr>
        <td>
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
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.startdate)}</td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.enddate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap="nowrap" id="endDate" name="endDate">${fn:escapeXml(ServiceForm.enddate)}</td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.rate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.rate)}&nbsp;<bean:message key="drac.text.Mbps" /></td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.extension" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                    <c:choose>
	                    <c:when test="${ServiceForm.isCancellable()}">
	                    	<input type="text" name="extensionMinutes" id="extensionMinutes" size="8">
				            <html:button tabindex="97" property="" styleId="queryButton"  onclick="showDiv()">
				                <bean:message key="drac.schedule.create.button.extendService"/>
				            </html:button>  
				        </c:when>
				        <c:otherwise>
				         	Service cannot be extended
				        </c:otherwise>
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
                    <td class="tbtbot" align="center"><b><bean:message key="drac.service.details.routeDiverse" /></b></td>
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
                    <td align='left' valign="top" class="tbForm1">
                        <b><bean:message key="drac.service.details.srsg" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:if test="${empty ServiceForm.srsg}"><bean:message key="drac.text.none"/></c:if>
                        <c:if test="${not empty ServiceForm.srsg}">
                            <logic:iterate id="aSrsg" name="ServiceForm" property="srsg">
                                <html:link href="/service/serviceDetails.do" paramId="sid" paramName="aSrsg">${aSrsg}</html:link><br>
                            </logic:iterate>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.srlg" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:if test="${empty ServiceForm.srlg}"><bean:message key="drac.text.none"/></c:if>
                        ${fn:escapeXml(ServiceForm.srlg)}
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.srlgInclusions" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>
                        <c:if test="${empty ServiceForm.srlgInclusions}"><bean:message key="drac.text.none"/></c:if>
                        ${fn:escapeXml(ServiceForm.srlgInclusions)}
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
                        ${fn:escapeXml(callType.callID)}
                    </td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.status" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap><bean:message key="drac.reservation.status.${callType.callStatus}" /></td>
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
                        ${fn:escapeXml(ServiceForm.scheduleId)}</a>
                    </td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.schedulename" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleName)}</td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.schedule.sourcePort" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleSrcTNA)}</td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.schedule.sourcePortLabel" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleSrcFacLabel)}</td>
                </tr>
                <c:if test="${ServiceForm.scheduleSrcVlanId ne ''}">
                            <tr>
                                <td class="tbForm1">
                                    <b><bean:message key="drac.schedule.sourceVlanId" /></b>
                                </td>
                                <td class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleSrcVlanId)}</td>
                            </tr>
                </c:if>


                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.schedule.destPort" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleDestTNA)}</td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.schedule.destPortLabel" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleDestFacLabel)}</td>
                </tr>
                <c:if test="${ServiceForm.scheduleDestVlanId ne ''}">
                            <tr>
                                <td class="tbForm1">
                                    <b><bean:message key="drac.schedule.destVlanId" /></b>
                                </td>
                                <td class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleDestVlanId)}</td>
                            </tr>
                </c:if>


                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleStartdate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleStartdate)}</td>
                </tr>

                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleEnddate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleEnddate)}</td>
                </tr>
                <tr>
                    <td align='left' class="tbForm1">
                        <b><bean:message key="drac.service.details.scheduleRate" />
                        </b>
                    </td>
                    <td align="left" class="tbForm1" nowrap>${fn:escapeXml(ServiceForm.scheduleRate)}&nbsp;<bean:message key="drac.text.Mbps" /></td>
                </tr>
                <tr align='left'>
                    <td class="tbForm1" align="left">
                        <b><bean:message key="drac.schedule.activationType" /></b>
                    </td>
                    <td class="tbForm1" align="left"><bean:message key="drac.reservation.type.${ServiceForm.scheduleActivationType}"/></td>
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
    <logic:messagesPresent message="true">
    <tr>
        <td>
            <html:messages id="message" message="true">
            <font color="red"><b><bean:write name="message"/></b></font>
            </html:messages>
        </td>
    </tr>
    </logic:messagesPresent>
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
</table>
</html:form>

<%@ include file="/common/footer.jsp"%>
