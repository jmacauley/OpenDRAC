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

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /general/serverStatus.jsp
 *
 * Description:
 *   This page display OpenDRAC status information to the user.
 *
 ****************************************************************************/

String pageRef = "drac.general.serverStatus";

%>


<%@ include file="/common/header_struts.jsp" %>

<logic:notEmpty name="drac.general.serverStatus.error" scope="request">
<!-- Display error if needed. -->


<% String errorDetails = (String) request.getAttribute("drac.general.serverStatus.error");
String errorMessage = errorDetails; %>
<%@ include file="/common/errorResult.jsp" %>
</logic:notEmpty>
<logic:empty name="drac.general.serverStatus.error" scope="request">
<br />
<logic:messagesPresent message="true">
<table cellspacing="0" cellpadding="0" border="0" align="center">
    <html:messages id="message" message="true">
    <tr>
        <td align="center" class="gen">
            <img valign="middle" src="/images/warning.png">&nbsp;<font color="red"><b><bean:write name="message"/></b></font>
        </td>
    </tr>
    </html:messages>
</table>
<p>
</logic:messagesPresent>
<table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
    <tbody>
      <tr>
        <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                  <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                  <td class="tbtbot"><center><b><bean:message key="drac.general.serverStatus"/></b></center></td>
                  <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td>
          <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
            <tr>
              <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.status"/></b></td>
              <td class="row1" nowrap>${requestScope['status']}</td>
            </tr>
            <tr>
              <td class="row2" nowrap><b><bean:message key="drac.general.serverStatus.version"/></b></td>
              <td class="row2" nowrap>${requestScope['version']}</td>
            </tr>
            <tr>
                <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.coreServerAddress"/></b></td>
                <td class="row1" nowrap>
                    <c:choose>
                        <c:when test="${ServerInfoForm.redundancyMode eq 'STANDALONE' || ServerInfoForm.redundancyMode eq 'SERVER1'}">
                            ${ServerInfoForm.server1Ip}&nbsp;
                        </c:when>
                        <c:when test="${ServerInfoForm.redundancyMode eq 'SERVER2'}">
                            ${ServerInfoForm.server2Ip}&nbsp;
                        </c:when>
                        <c:otherwise><b><font color="red"><bean:message key="drac.general.serverStatus.serverState.unknown"/></font></b></c:otherwise>
                    </c:choose>
                </td>
                </tr>
            <tr>
              <td class="row2" nowrap><b><bean:message key="drac.general.serverStatus.webServerAddress"/></b></td>
              <td class="row2" nowrap><%= request.getLocalName() %>:<%= request.getServerPort() %></td>
            </tr>
            <tr>
              <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.webServer"/></b></td>
              <td class="row1" nowrap><%= getServletContext().getServerInfo() %></td>
            </tr>
            
            <tr>
              <td class="row2" nowrap><b>Built Info</b></td>
              <td class="row2" nowrap>
                <jsp:include page="/META-INF/maven/org.opendrac/WebServer/pom.properties"/> 
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
    </tbody>
</table>

<html:form action="/serverStatus.do" method="POST">
<c:if test="${ServerInfoForm.redundancyMode ne 'STANDALONE'}">
<table cellspacing="15" cellpadding="0" border="0" align="center">
    <tbody>
        <tr><td>
      <table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
        <tr>
            <td>
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                <tr>
                  <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                  <td class="tbtbot"><center><b><bean:message key="drac.general.serverStatus.server1.title"/></b></center></td>
                  <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                </tr>
                </tbody>
            </table>
        </td>
        </tr>
        <tr>
            <td>
            <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tr>
                  <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.ipAddress"/></b></td>
                  <td class="row1" nowrap>${ServerInfoForm.server1Ip}&nbsp;</td>
                </tr>
                <tr>
                  <td class="row2" nowrap><b><bean:message key="drac.general.serverStatus.serverVersion"/></b></td>
                  <td class="row2" nowrap>${ServerInfoForm.server1Version}&nbsp;</td>
                </tr>
                <tr>
                  <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.redundancyConfig"/></b></td>
                  <td class="row1" nowrap>
                    <c:choose>
                        <c:when test="${ServerInfoForm.server1Config eq 'REDUNDANT'}">
                            <bean:message key="drac.general.serverStatus.redundancyMode.redundant"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1Config eq 'STANDALONE'}">
                            <bean:message key="drac.general.serverStatus.redundancyMode.standalone"/>
                        </c:when>
                        <c:otherwise>
                            ${ServerInfoForm.server1Config}&nbsp;
                        </c:otherwise>
                    </c:choose>
                  </td>
                </tr>
                <tr>
                  <td class="row2" nowrap><b><bean:message key="drac.general.serverStatus.serverState"/></b></td>
                  <td class="row2" nowrap>
                    <c:choose>
                        <c:when test="${ServerInfoForm.server1State eq 'NOT_REACHABLE'}">
                            <bean:message key="drac.general.serverStatus.serverState.notReachable"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'REACHABLE'}">
                            <bean:message key="drac.general.serverStatus.serverState.reachable"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'ACTIVE'}">
                            <bean:message key="drac.general.serverStatus.serverState.active"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'INACTIVE'}">
                            <bean:message key="drac.general.serverStatus.serverState.inactive"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'ACTIVE_INPROGRESS'}">
                            <bean:message key="drac.general.serverStatus.serverState.active.inProgress"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'INACTIVE_INPROGRESS'}">
                            <bean:message key="drac.general.serverStatus.serverState.inactive.inProgress"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'SWITCHING_OVER'}">
                            <bean:message key="drac.general.serverStatus.serverState.switching"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'INITIALIZING'}">
                            <bean:message key="drac.general.serverStatus.serverState.init"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1State eq 'UNKNOWN'}">
                            <bean:message key="drac.general.serverStatus.serverState.unknown"/>
                        </c:when>
                        <c:otherwise>
                            ${ServerInfoForm.server1State}&nbsp;
                        </c:otherwise>
                    </c:choose>
                  </td>
                </tr>
                <tr>
                  <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.serverMode"/></b></td>
                  <td class="row1" nowrap>
                    <c:choose>
                        <c:when test="${ServerInfoForm.server1Mode eq 'PRIMARY'}">
                            <bean:message key="drac.general.serverStatus.serverMode.primary"/>
                        </c:when>
                        <c:when test="${ServerInfoForm.server1Mode eq 'SECONDARY'}">
                            <bean:message key="drac.general.serverStatus.serverMode.secondary"/>
                        </c:when>
                        <c:otherwise>
                            ${ServerInfoForm.server1Mode}&nbsp;
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
      </table>
      </td>
      <td>
        <table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
              <tr>
                <td>
                    <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                        <tbody>
                        <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
                        <tr>
                          <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                          <td class="tbtbot"><center><b><bean:message key="drac.general.serverStatus.server2.title"/></b></center></td>
                          <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                  <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                    <tr>
                      <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.ipAddress"/></b></td>
                      <td class="row1" nowrap>${ServerInfoForm.server2Ip}&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="row2" nowrap><b><bean:message key="drac.general.serverStatus.serverVersion"/></b></td>
                      <td class="row2" nowrap>${ServerInfoForm.server2Version}&nbsp;</td>
                    </tr>
                    <tr>
                      <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.redundancyConfig"/></b></td>
                      <td class="row1" nowrap>
                        <c:choose>
                            <c:when test="${ServerInfoForm.server2Config eq 'REDUNDANT'}">
                                <bean:message key="drac.general.serverStatus.redundancyMode.redundant"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2Config eq 'STANDALONE'}">
                                <bean:message key="drac.general.serverStatus.redundancyMode.standalone"/>
                            </c:when>
                            <c:otherwise>
                                ${ServerInfoForm.server2Config}&nbsp;
                            </c:otherwise>
                        </c:choose>
                      </td>
                    </tr>
                    <tr>
                      <td class="row2" nowrap><b><bean:message key="drac.general.serverStatus.serverState"/></b></td>
                      <td class="row2" nowrap>
                        <c:choose>
                            <c:when test="${ServerInfoForm.server2State eq 'NOT_REACHABLE'}">
                                <bean:message key="drac.general.serverStatus.serverState.notReachable"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'REACHABLE'}">
                                <bean:message key="drac.general.serverStatus.serverState.reachable"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'ACTIVE'}">
                                <bean:message key="drac.general.serverStatus.serverState.active"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'INACTIVE'}">
                                <bean:message key="drac.general.serverStatus.serverState.inactive"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'ACTIVE_INPROGRESS'}">
                                <bean:message key="drac.general.serverStatus.serverState.active.inProgress"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'INACTIVE_INPROGRESS'}">
                                <bean:message key="drac.general.serverStatus.serverState.inactive.inProgress"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'SWITCHING_OVER'}">
                                <bean:message key="drac.general.serverStatus.serverState.switching"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'INITIALIZING'}">
                                <bean:message key="drac.general.serverStatus.serverState.init"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2State eq 'UNKNOWN'}">
                                <bean:message key="drac.general.serverStatus.serverState.unknown"/>
                            </c:when>
                            <c:otherwise>
                                ${ServerInfoForm.server2State}&nbsp;
                            </c:otherwise>
                        </c:choose>
                      </td>
                    </tr>
                    <tr>
                      <td class="row1" nowrap><b><bean:message key="drac.general.serverStatus.serverMode"/></b></td>
                      <td class="row1" nowrap>
                        <c:choose>
                            <c:when test="${ServerInfoForm.server2Mode eq 'PRIMARY'}">
                                <bean:message key="drac.general.serverStatus.serverMode.primary"/>
                            </c:when>
                            <c:when test="${ServerInfoForm.server2Mode eq 'SECONDARY'}">
                                <bean:message key="drac.general.serverStatus.serverMode.secondary"/>
                            </c:when>
                            <c:otherwise>
                                ${ServerInfoForm.server2Mode}&nbsp;
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
        </table>
      </td>
      </tr>
      <c:if test="${sessionScope['authObj'].userPolicyProfile.userGroupType eq 'SYSTEM_ADMIN'}">
          <tr>
            <td colspan="2" align="center" class="gen">
                <b><bean:message key="drac.general.serverStatus.switch.title"/></b>
                    <html:select styleId="switchTo" property="forceSwitchTo" styleClass="gen">
                        <html:option value="server1">Server 1 (${ServerInfoForm.server1Ip})</html:option>
                        <html:option value="server2">Server 2 (${ServerInfoForm.server2Ip})</html:option>
                    </html:select> &nbsp;<html:submit styleClass="gen" onclick="return confirmSwitch()"><bean:message key="drac.text.submit"/></html:submit>

            </td>
          </tr>
      </c:if>
    </tbody>
</table>
</c:if>
</html:form>

</logic:empty>

  <br />

<script LANGUAGE="JavaScript">
function confirmSwitch()
{
var agree=confirm('<bean:message key="drac.general.serverStatus.switch.confirm"/>');
if (agree) {
    return true;
} else {
    return false;
}
}
</script>

<%@ include file="/common/footer.jsp" %>
