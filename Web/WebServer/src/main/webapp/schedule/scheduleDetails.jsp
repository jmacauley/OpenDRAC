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

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
    /****************************************************************************
			 * OpenDRAC Web GUI
			 *
			 * File: /schedule/scheduleDetails.jsp
			 *
			 * Description:
			 *
			 ****************************************************************************/

			String pageRef = "drac.schedule.detail";
%>

<%@ include file="/common/header_struts.jsp"%>

<html:form action="/schedule/queryScheduleAction.do" method="POST">
	<%@ include file="/common/csrf/setCSRFToken.jsp"%>
	<table width="90%" cellspacing="5" cellpadding="6" border="0"
		align="center" style="position: static !important; position: relative">
		<tr>
			<td colspan="2"><img src="/images/spacer.gif" height="5" /></td>
		</tr>
		<logic:messagesPresent message="true">
			<tr>
				<td align="center" class="gen" colspan="2"><html:messages
					id="message" message="true" property="errors">
					<font color="red"><b><bean:write name="message" /></b></font>
					<br>
				</html:messages></td>
			</tr>
			<tr>
				<td align="center" class="gen" colspan="2"><html:messages
					id="message" message="true" property="success">
					<font color="green"><b><bean:write name="message" /></b></font>
					<br>
				</html:messages></td>
			</tr>
		</logic:messagesPresent>
		<logic:notEmpty name="ScheduleData" scope="request" property="id">
			<tr>
				<td valign="top" align="right" width="50%">
				<table cellpadding="0" cellspacing="0" border="0" align="right">
					<tr>
						<td>
						<table border="0" cellpadding="0" cellspacing="0" class="tbt">
							<tbody>
								<tr>
									<td><img src="/images/spacer.gif" alt="" height="5" /></td>
								</tr>
								<tr>
									<td class="tbtl"><img src="/images/spacer.gif" alt=""
										width="22" height="22" /></td>
									<td class="tbtbot">
									<center><b><bean:message
										key="drac.schedule.detail" /></b></center>
									</td>
									<td class="tbtrr"><img src="/images/spacer.gif" alt=""
										width="22" height="22" alt="" /></td>
								</tr>
							</tbody>
						</table>
						</td>
					</tr>
					<tr>
						<td>
						<table cellspacing="0" cellpadding="0" border="0" align="center"
							class="tbForm">
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.detail.name" /></b></td>
								<td class="tbForm1">${fn:escapeXml(ScheduleData.name)}</td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.id" /></b></td>
								<td class="tbForm1">${fn:escapeXml(ScheduleData.id)}</td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.detail.userID" /></b></td>
								<td class="tbForm1"><c:choose>
									<c:when
										test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
										<html:link href="/management/userManagement/editUser.do"
											paramId="uid" paramName="ScheduleData" paramProperty="userId">${fn:escapeXml(ScheduleData.userId)}</html:link>
									</c:when>
									<c:otherwise>
                                            ${fn:escapeXml(ScheduleData.userId)}
                                        </c:otherwise>
								</c:choose></td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.detail.billingGroup" /></b></td>
								<td class="tbForm1"><c:choose>
									<c:when
										test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
										<html:link
											href="/management/userGroupManagement/queryUserGroup.do"
											paramId="ugName" paramName="ScheduleData"
											paramProperty="userGroup">${fn:escapeXml(ScheduleData.userGroup)}</html:link>
									</c:when>
									
									<c:otherwise>
                                            ${fn:escapeXml(ScheduleData.userGroup)}
                                        </c:otherwise>
								</c:choose></td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.startdate" /></b></td>
								<td class="tbForm1">${fn:escapeXml(ScheduleData.startDate)}</td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.enddate" /></b></td>
								<td class="tbForm1">${fn:escapeXml(ScheduleData.endDate)}</td>
							</tr>
							<tr align='left'>
								<td class="tbForm1" align="left"><b><bean:message
									key="drac.schedule.activationType" /></b></td>
								<td class="tbForm1" align="left"><bean:message
									key="drac.reservation.type.${ScheduleData.activationType}" /></td>
							</tr>
							<tr>
								<td align='left' class="tbForm1"><b><bean:message
									key="drac.schedule.status" /> </b></td>
								<td align="left" class="tbForm1" nowrap><bean:message
									key="drac.reservation.status.${ScheduleData.status}" /></td>
							</tr>
							<c:if
								test="${ScheduleData.recurrence eq true && ! empty ScheduleData.recurrenceType}">
								<tr>
									<td class="tbForm1" colspan="2">&nbsp;</td>
								</tr>
								<tr>
									<td class="tbForm1" colspan="2">&nbsp;</td>
								</tr>
								<tr>
									<td class="tbForm1" colspan="2">&nbsp;</td>
								</tr>
							</c:if>
						</table>
						</td>
					</tr>
					<tr>
						<td><!-- Drop shadow. -->
						<table border="0" cellpadding="0" cellspacing="0" class="tbl">
							<tbody>
								<tr>
									<td class="tbll"><img src="/images/spacer.gif" alt=""
										width="8" height="4" /></td>
									<td class="tblbot"><img src="/images/spacer.gif" alt=""
										width="8" height="4" /></td>
									<td class="tblr"><img src="/images/spacer.gif" alt=""
										width="8" height="4" /></td>
								</tr>
							</tbody>
						</table>
						</td>
					</tr>
				</table>
				</td>
				<td valign="top" align="left" width="50%">
				<table cellpadding="0" cellspacing="0" border="0" align="left">
					<tr>
						<td>
						<table border="0" cellpadding="0" cellspacing="0" class="tbt">
							<tbody>
								<tr>
									<td><img src="/images/spacer.gif" alt="" height="5" /></td>
								</tr>
								<tr>
									<td class="tbtl"><img src="/images/spacer.gif" alt=""
										width="22" height="22" /></td>
									<td class="tbtbot">
									<center><b><bean:message
										key="drac.schedule.detail.path" /></b></center>
									</td>
									<td class="tbtrr"><img src="/images/spacer.gif" alt=""
										width="22" height="22" alt="" /></td>
								</tr>
							</tbody>
						</table>
						</td>
					</tr>
					<tr>
						<td>
						<table cellspacing="0" cellpadding="0" border="0" align="center"
							class="tbForm">
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.sourcePort" /></b></td>
								<td class="tbForm1" nowrap>${fn:escapeXml(ScheduleData.srcTNA)}</td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.sourcePortLabel" /></b></td>
								<td class="tbForm1" nowrap>${fn:escapeXml(ScheduleData.srcFacLabel)}</td>
							</tr>

							<c:if test="${ScheduleData.srcVlanId ne ''}">
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.sourceVlanId" /></b></td>
									<td class="tbForm1" nowrap>${fn:escapeXml(ScheduleData.srcVlanId)}</td>
								</tr>
							</c:if>

							<c:if test="${ScheduleData.srcCh ne ''}">
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.detail.src.channel" /><b></td>
									<td class="tbForm1"><c:choose>
										<c:when
											test="${ScheduleData.srcCh eq 'drac.schedule.detail.channel.auto'}">
											<bean:message key="drac.schedule.detail.channel.auto" />
										</c:when>
										<c:otherwise>${fn:escapeXml(ScheduleData.srcCh)}</c:otherwise>
									</c:choose></td>
								</tr>
							</c:if>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.destPort" /></b></td>
								<td class="tbForm1" nowrap>${fn:escapeXml(ScheduleData.destTNA)}</td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.destPortLabel" /></b></td>
								<td class="tbForm1" nowrap>${fn:escapeXml(ScheduleData.destFacLabel)}</td>
							</tr>

							<c:if test="${ScheduleData.destVlanId ne '' }">
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.destVlanId" /></b></td>
									<td class="tbForm1" nowrap>${fn:escapeXml(ScheduleData.destVlanId)}</td>
								</tr>
							</c:if>

							<c:if test="${ScheduleData.destCh ne ''}">
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.detail.dest.channel" /><b></td>
									<td class="tbForm1"><c:choose>
										<c:when
											test="${ScheduleData.destCh eq 'drac.schedule.detail.channel.auto'}">
											<bean:message key="drac.schedule.detail.channel.auto" />
										</c:when>
										<c:otherwise>${fn:escapeXml(ScheduleData.destCh)}</c:otherwise>
									</c:choose></td>
								</tr>
							</c:if>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.rate" /></b></td>
								<td class="tbForm1">${fn:escapeXml(ScheduleData.rate)}&nbsp;<bean:message
									key="drac.text.Mbps" /></td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.detail.protection" /></b></td>
								<td class="tbForm1"><bean:message
									key="drac.schedule.protection.${ScheduleData.protectionType}" /></td>
							</tr>
							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.detail.srlg" /><b></td>
								<td class="tbForm1"><c:choose>
									<c:when test="${ScheduleData.srlg eq ''}">
										<bean:message key="drac.text.none" />
									</c:when>
									<c:otherwise>${fn:escapeXml(ScheduleData.srlg)}</c:otherwise>
								</c:choose></td>
							</tr>
							<tr>
								<td align='left' valign="top" class="tbForm1"><b><bean:message
									key="drac.service.details.srsg" /> </b></td>
								<td align="left" class="tbForm1" nowrap><c:if
									test="${empty ScheduleData.srsg}">
									<bean:message key="drac.text.none" />
								</c:if> <c:if test="${not empty ScheduleData.srsg}">
									<logic:iterate id="aSrsg" name="ScheduleData" property="srsg">
										<html:link href="/service/serviceDetails.do" paramId="sid"
											paramName="aSrsg">${fn:escapeXml(aSrsg)}</html:link>
										<br>
									</logic:iterate>
								</c:if></td>
							</tr>

							<tr>
								<td class="tbForm1"><b><bean:message
									key="drac.schedule.isVcat" /></b></td>
								<td class="tbForm1" nowrap>${fn:escapeXml(ScheduleData.vcat)}</td>
							</tr>

							<c:if test="${!ScheduleData.recurrence}">
								<tr>
									<td class="tbForm1" colspan="2">&nbsp;</td>
								</tr>
								<tr>
									<td class="tbForm1" colspan="2">&nbsp;</td>
								</tr>
							</c:if>
						</table>
						</td>
					</tr>
					<tr>
						<td><!-- Drop shadow. -->
						<table border="0" cellpadding="0" cellspacing="0" class="tbl">
							<tbody>
								<tr>
									<td class="tbll"><img src="/images/spacer.gif" alt=""
										width="8" height="4" /></td>
									<td class="tblbot"><img src="/images/spacer.gif" alt=""
										width="8" height="4" /></td>
									<td class="tblr"><img src="/images/spacer.gif" alt=""
										width="8" height="4" /></td>
								</tr>
							</tbody>
						</table>
						</td>
					</tr>
					<c:if
						test="${ScheduleData.recurrence eq true && ! empty ScheduleData.recurrenceType}">
						<tr>
							<td>
							<table border="0" cellpadding="0" cellspacing="0" class="tbt">
								<tbody>
									<tr>
										<td><img src="/images/spacer.gif" alt="" height="5" /></td>
									</tr>
									<tr>
										<td class="tbtl"><img src="/images/spacer.gif" alt=""
											width="22" height="22" /></td>
										<td class="tbtbot">
										<center><b><bean:message
											key="drac.schedule.detail.recurrence" /></b></center>
										</td>
										<td class="tbtrr"><img src="/images/spacer.gif" alt=""
											width="22" height="22" alt="" /></td>
									</tr>
								</tbody>
							</table>
							</td>
						</tr>
						<tr>
							<td>
							<table cellspacing="0" cellpadding="0" border="0" align="center"
								class="tbForm">
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.detail.type" /></b></td>
									<td class="tbForm1">${fn:escapeXml(ScheduleData.recurrenceType)}</td>
								</tr>
								<c:if test='${ScheduleData.recurrenceType eq "Monthly"}'>
									<tr>
										<td class="tbForm1"><b><bean:message
											key="drac.schedule.detail.day" /></b></td>
										<td class="tbForm1">${fn:escapeXml(ScheduleData.recDay)}</td>
									</tr>
								</c:if>

								<c:if test='${ScheduleData.recurrenceType eq "Yearly"}'>
									<tr>
										<td class="tbForm1"><b><bean:message
											key="drac.schedule.every" /></b></td>
										<td class="tbForm1"><c:choose>
											<c:when test="${ScheduleData.recMonth eq 0}">
												<bean:message key="drac.schedule.jan" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 1}">
												<bean:message key="drac.schedule.feb" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 2}">
												<bean:message key="drac.schedule.mar" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 3}">
												<bean:message key="drac.schedule.apr" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 4}">
												<bean:message key="drac.schedule.may" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 5}">
												<bean:message key="drac.schedule.jun" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 6}">
												<bean:message key="drac.schedule.jul" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 7}">
												<bean:message key="drac.schedule.aug" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 8}">
												<bean:message key="drac.schedule.sep" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 9}">
												<bean:message key="drac.schedule.oct" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 10}">
												<bean:message key="drac.schedule.nov" />
											</c:when>
											<c:when test="${ScheduleData.recMonth eq 11}">
												<bean:message key="drac.schedule.dec" />
											</c:when>
										</c:choose> &nbsp;${fn:escapeXml(ScheduleData.recDay)}</td>
									</tr>
								</c:if>

								<c:if
									test='${ScheduleData.recurrenceType eq "Weekly" && ScheduleData.weekDay != null}'>
									<tr>
										<td class="tbForm1"><b><bean:message
											key="drac.schedule.detail.weekdays" /></b></td>
										<td class="tbForm1"><logic:iterate id="day"
											name="ScheduleData" property="weekDay">
											<c:choose>
												<c:when test='${day eq 1}'>
													<bean:message key="drac.schedule.sun" />
												</c:when>
												<c:when test='${day eq 2}'>
													<bean:message key="drac.schedule.mon" />
												</c:when>
												<c:when test='${day eq 3}'>
													<bean:message key="drac.schedule.tue" />
												</c:when>
												<c:when test='${day eq 4}'>
													<bean:message key="drac.schedule.wed" />
												</c:when>
												<c:when test='${day eq 5}'>
													<bean:message key="drac.schedule.thu" />
												</c:when>
												<c:when test='${day eq 6}'>
													<bean:message key="drac.schedule.fri" />
												</c:when>
												<c:when test='${day eq 7}'>
													<bean:message key="drac.schedule.sat" />
												</c:when>
												<c:otherwise>
                                                    &nbsp;
                                                </c:otherwise>
											</c:choose>
                                            &nbsp;
                                        </logic:iterate></td>
									</tr>
								</c:if>
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.detail.occurrences" /></b></td>
									<td class="tbForm1">${fn:length(ScheduleData.services)}</td>
								</tr>
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.detail.firstOccur" /></b></td>
									<td class="tbForm1">${fn:escapeXml(ScheduleData.firstOccurrence)}</td>
								</tr>
								<tr>
									<td class="tbForm1"><b><bean:message
										key="drac.schedule.detail.lastOccur" /></b></td>
									<td class="tbForm1">${fn:escapeXml(ScheduleData.lastOccurrence)}</td>
								</tr>

							</table>
							</td>
						</tr>
						<tr>
							<td><!-- Drop shadow. -->
							<table border="0" cellpadding="0" cellspacing="0" class="tbl">
								<tbody>
									<tr>
										<td class="tbll"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
										<td class="tblbot"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
										<td class="tblr"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
									</tr>
								</tbody>
							</table>
							</td>
						</tr>
					</c:if>
				</table>
				</td>
			</tr>
			<c:if
				test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER' || ScheduleData.userId eq sessionScope['userID']}">
				<tr>
					<td colspan="2">
					<table cellpadding="0" cellspacing="0" border="0" align="center">
						<tr>
							<td>
							<table border="0" cellpadding="0" cellspacing="0" class="tbt">
								<tbody>
									<tr>
										<td><img src="/images/spacer.gif" alt="" height="5" /></td>
									</tr>
									<tr>
										<td class="tbtl"><img src="/images/spacer.gif" alt=""
											width="22" height="22" /></td>
										<td class="tbtbot">
										<center><b><bean:message
											key="drac.schedule.detail.actions" /></b></center>
										</td>
										<td class="tbtrr"><img src="/images/spacer.gif" alt=""
											width="22" height="22" alt="" /></td>
									</tr>
								</tbody>
							</table>
							</td>
						</tr>
						<tr>
							<td>
							<table border="0" cellpadding="0" cellspacing="0" class="tbForm">
								<tbody>
									<tr>
										<td class="tbForm1"><c:if
											test="${ScheduleData.confirmable}">
											<html:submit styleClass="gen"
												onclick="return confirmSchedule();">
												<bean:message key='drac.schedule.confirm' />
											</html:submit>
										</c:if> <c:if test="${!ScheduleData.confirmable}">
											<html:submit styleClass="gen" disabled="true">
												<bean:message key='drac.schedule.confirm' />
											</html:submit>
										</c:if> <c:if test="${ScheduleData.cancellable}">
											<html:submit styleClass="gen"
												onclick="return checkCancelSchedule();">
												<bean:message key='drac.menu.schedule.delete' />
											</html:submit>
										</c:if> <c:if test="${!ScheduleData.cancellable}">
											<html:submit styleClass="gen" disabled="true">
												<bean:message key='drac.menu.schedule.delete' />
											</html:submit>
										</c:if> <c:if test="${ScheduleData.expandable}">
											<html:button styleClass="gen" property="command"
												onclick="window.location.href='/service/addService.do?sid=${ScheduleData.id}'">
												<bean:message key="drac.service.button.add" />
											</html:button>
										</c:if> <c:if test="${!ScheduleData.expandable}">
											<html:button styleClass="gen" property="command"
												disabled="true">
												<bean:message key="drac.service.button.add" />
											</html:button>
										</c:if>
									  <!--TODO: Add change/extend schedule -->
                    <html:button styleClass="gen" property="command" disabled="true"
                       onclick="window.location.href='/service/addService.do?sid=${ScheduleData.id}'">
                      <bean:message key="drac.menu.schedule.edit" />
                    </html:button>
										</td>
									</tr>
								</tbody>
							</table>
							</td>
						</tr>
						<tr>
							<td><!-- Drop shadow. -->
							<table border="0" cellpadding="0" cellspacing="0" class="tbl">
								<tbody>
									<tr>
										<td class="tbll"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
										<td class="tblbot"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
										<td class="tblr"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
									</tr>
								</tbody>
							</table>
							</td>
						</tr>
					</table>
					</td>
				</tr>
			</c:if>
			<logic:notEmpty name="ScheduleData" scope="request"
				property="services">
				<tr>
					<td colspan="2">
					<table cellpadding="0" cellspacing="0" border="0" align="center">
						<tr>
							<td>
							<table border="0" cellpadding="0" cellspacing="0" class="tbt">
								<tbody>
									<tr>
										<td><img src="/images/spacer.gif" alt="" height="5" /></td>
									</tr>
									<tr>
										<td class="tbtl"><img src="/images/spacer.gif" alt=""
											width="22" height="22" /></td>
										<td class="tbtbot" align="center"><b><bean:message
											key="drac.schedule.detail.servicedetails" /></b>&nbsp;&nbsp;&nbsp;

										</td>
										<td class="tbtrr"><img src="/images/spacer.gif" alt=""
											width="22" height="22" alt="" /></td>
									</tr>
								</tbody>
							</table>
							</td>
						</tr>
						<tr>
							<td>
							<table cellspacing="1" cellpadding="5" border="0" align="center"
								class="tbForm">
								<tr>
									<th><input class="smallbox" type="checkbox" size="1px"
										name="headerCB" onClick="checkAllBoxes(this);" /></th>
									<th><bean:message
										key="drac.schedule.create.result.serviceNo" /></th>
									<th><bean:message key="drac.text.details" /></th>
									<th><bean:message key="drac.schedule.detail.serviceid" />
									</th>
									<th><bean:message key="drac.service.details.startdate" />
									</th>
									<th><bean:message key="drac.service.details.enddate" /></th>
									<th><bean:message key="drac.schedule.detail.status" /></th>
								</tr>

								<logic:iterate id="service" indexId="count" name="ScheduleData"
									scope="request" property="services">
									<tr>
										<c:set var="tdclass" value="row2" />
										<c:if test="${count % 2 == 0}">
											<c:set var="tdclass" value="row1" />
										</c:if>
										<td align="center" class="${tdclass}" nowrap><input
											type="checkbox" class="smallbox" name="selectedItems"
											value="${service.serviceID}"
											onclick="Javascript:uncheckHeaderBox();" /></td>
										<td align="center" class="${tdclass}" nowrap><c:out
											value="${count+1}" /></td>
										<td class="${tdclass}" align="center" nowrap><a
											href="javascript:handleClick('user<c:out value="${count}"/>');">
										<img align="absmiddle" id="select<c:out value="${count}"/>"
											name="select<c:out value="${count}"/>" src="/images/plus.gif"
											onclick="swapImage('select<c:out value="${count}"/>');">
										</a></td>
										<td class="${tdclass}" align="center" nowrap><a
											href="/service/serviceDetails.do?sid=<c:out value="${service.serviceID}" />">
										${service.serviceID}</a></td>
										<td class="${tdclass}" align="center" nowrap>
										${fn:escapeXml(service.startdate)}</td>
										<td class="${tdclass}" align="center" nowrap>
										${fn:escapeXml(service.enddate)}</td>
										<td class="${tdclass}" align="center" nowrap><bean:message
											key="drac.reservation.status.${service.status}" /></td>
									</tr>
									<tr id="user<c:out value="${count}"/>" style="display: none"
										border="0">
										<td class="tbForm1" colspan="2"><img
											src="/images/spacer.gif" height="5" /></td>
										<td colspan="4" align="left" class="tbForm1">
										<table width="100%" cellspacing="1" cellpadding="5" border="0"
											align="center" class="tbmain">
											<tr>
												<td class="rowth"><bean:message key="drac.text.entry" />
												</td>
												<td class="rowth"><bean:message
													key="drac.schedule.detail.callid" /></td>
												<td class="rowth"><bean:message
													key="drac.schedule.detail.status" /></td>
											</tr>
											<logic:notEmpty name="service" property="calls">
												<logic:iterate id="call" indexId="callCnt" name="service"
													property="calls">
													<tr>
														<c:set var="tdclass2" value="tbForm1" />
														<c:if test="${callCnt % 2 == 0}">
															<c:set var="tdclass2" value="tbForm1" />
														</c:if>
														<td class="<c:out value="${tdclass2}"/>" align="center">
														<c:out value="${callCnt+1}" /></td>
														<td class="<c:out value="${tdclass2}"/>" align="center">
														<c:out value="${call.callID}" /></td>
														<td class="<c:out value="${tdclass2}"/>" align="center">
														<bean:message
															key="drac.reservation.status.${call.callStatus}" /></td>
													</tr>
												</logic:iterate>
											</logic:notEmpty>
										</table>
										</td>
										<td class="tbForm1"><img src="/images/spacer.gif"
											height="5" /></td>
									</tr>
								</logic:iterate>
								<tr>
									<th colspan="7" align="left"><c:if
										test="${ScheduleData.activateableService}">
										<html:submit styleClass="gen"
											onclick="return activateServices();">
											<bean:message key="drac.schedule.detail.activate.selected" />
										</html:submit>
									</c:if> <c:if test="${!ScheduleData.activateableService}">
										<html:submit styleClass="gen" disabled="true">
											<bean:message key="drac.schedule.detail.activate.selected" />
										</html:submit>
									</c:if> <c:if test="${ScheduleData.cancellableService}">
										<html:submit styleClass="gen"
											onclick="return checkCancelServices()">
											<bean:message key='drac.schedule.detail.cancel.selected' />
										</html:submit>
									</c:if> <c:if test="${!ScheduleData.cancellableService}">
										<html:submit styleClass="gen" disabled="true">
											<bean:message key='drac.schedule.detail.cancel.selected' />
										</html:submit>
									</c:if></th>
								</tr>

							</table>
							</td>
						</tr>
						<tr>
							<td><!-- Drop shadow. -->
							<table border="0" cellpadding="0" cellspacing="0" class="tbl">
								<tbody>
									<tr>
										<td class="tbll"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
										<td class="tblbot"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
										<td class="tblr"><img src="/images/spacer.gif" alt=""
											width="8" height="4" /></td>
									</tr>
								</tbody>
							</table>
							</td>
						</tr>
					</table>
					</td>
				</tr>
			</logic:notEmpty>
		</logic:notEmpty>
		<%@ include file="/common/loading.jsp"%>
	</table>
	<html:select property="command" styleId="hiddenCommandSel"
		style="display:none;">
		<html:option value="confirm">confirm</html:option>
		<html:option value="cancel">cancel</html:option>
		<html:option value="cancelServices">cancelServices</html:option>
		<html:option value="activate">activate</html:option>
	</html:select>
	<html:hidden property="id" value="${ScheduleData.id}" />
	<html:hidden property="name" value="${ScheduleData.name}" />


	<script type="text/javascript">

function checkAllBoxes(headerCheckBox)
{
    var checkBoxes = document.getElementsByName('selectedItems');
    for ( i = 0; i < checkBoxes.length; i++)
        if (checkBoxes[i].disabled == false) {
            checkBoxes[i].checked = headerCheckBox.checked;
        }
}

function isAnySelected()
{
    var checkBoxes = document.getElementsByName('selectedItems');
    for ( i = 0; i < checkBoxes.length; i++) {
        if (checkBoxes[i].checked) {
            return true;
        }
    }
    return false;

}

function uncheckHeaderBox()
{
    document.getElementsByName("headerCB")[0].checked = false;
}


function checkCancelSchedule() {
    var agree=confirm('<bean:message key="drac.schedule.delete.confirm"/>');
    if (agree) {
        document.getElementById("hiddenCommandSel").value = "cancel";
        showLoading(true);
    } else {
        return false;
    }
}

function confirmSchedule() {
    document.getElementById("hiddenCommandSel").value = "confirm";
    showLoading(true);
    return true;
}

function activateServices() {
    if (!isAnySelected()) {
        return false;
    }
    document.getElementById("hiddenCommandSel").value = "activate";
    showLoading(true);
    return true;
}

function checkCancelServices() {
    if (!isAnySelected()) {
        return false;
    }
    var agree=confirm('<bean:message key="drac.service.delete.confirm.multiple"/>');
    if (agree) {
        document.getElementById("hiddenCommandSel").value = "cancelServices";
        showLoading(true);
        return true;
    } else {
        return false;
    }
}
</script>

</html:form>

<script type="text/javascript" src="/scripts/handleExpand.js"></script>
<%@ include file="/common/footer.jsp"%>
