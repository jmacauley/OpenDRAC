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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
/****************************************************************************
* OpenDRAC Web GUI
*
* File: /network/listEndpointsResult.jsp
*
* Description:
*   This page diaplays the results of a list endpoints query.
*
****************************************************************************/

String pageRef = "drac.network.list.results";
%>

<%@ include file="/common/header_struts.jsp"%>

<html:form action="/network/editEndpoint.do" method="POST">
<script type="text/javascript" src="/scripts/utilities.js"></script>

    <table width="98%" cellspacing="5" cellpadding="0" border="0"
        align="center">
        <tr>
            <td>
                <img src="/images/spacer.gif" height="5" />
            </td>
        </tr>
        <tr>
            <td>
                <logic:empty name="endPointList" scope="request">
                    <tr>
                        <td colspan='9' align='center' class="gen">
                            <bean:message key="drac.network.list.results.empty" />
                        </td>
                    </tr>
                </logic:empty>
                <logic:notEmpty name="endPointList" scope="request">
                    <html:hidden property="groupFilter" value="{$endPointList[0].group}"/>
                    <html:hidden property="layerFilter" value="{$endPointList[0].layerFilter}"/>
                    <table cellspacing="1" cellpadding="5" border="0" align="center"
                        class="forumline">
                        <tr>
                            <th colspan="12" align='center' nowrap>
                                <c:if test="${requestScope['layerFilter'] eq 'layer1'}">
                                    <b><bean:message key="drac.network.list.filter.layer1"/>
                                </c:if>
                                <c:if test="${requestScope['layerFilter'] eq 'layer2'}">
                                    <b><bean:message key="drac.network.list.filter.layer2"/>
                                </c:if>
                                <c:if test="${requestScope['groupFilter'] ne 'all'}">
                                    <bean:message key="drac.network.list.results.title"/>
                                    <c:choose>
                                        <c:when test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                            <html:link href="/management/userGroupManagement/queryUserGroup.do" paramId="ugName" paramName="groupFilter">${fn:escapeXml(groupFilter)}</html:link>
                                        </c:when>
                                        <c:otherwise>
                                            ${fn:escapeXml(groupFilter)}
                                        </c:otherwise>
                                    </c:choose>
                                    </b>
                                </c:if>

                                <c:if test="${requestScope['groupFilter'] eq 'all'}">
                                    <bean:message key="drac.network.list.results.title2"/></b>
                                </c:if>
                            </th>
                        </tr>
                        <tr>
                            <td class="rowth" nowrap>
                                <bean:message key="drac.text.entry" />
                            </td>
                            <!-- <td class="rowth" nowrap>
                                <bean:message key="drac.text.action" />
                            </td> -->
                            <td class="rowth" nowrap>
                                <bean:message key="drac.text.details" />
                            </td>
                            <td class="rowth" nowrap>
                                <bean:message key="drac.network.list.results.name" />
                            </td>
                            <td class="rowth" nowrap>
                                <bean:message key="drac.network.list.results.label" />
                            </td>
                            <td class="rowth" nowrap>
                                <bean:message key="drac.network.list.results.type" />
                            </td>
                            <td class="rowth" nowrap>
                                <bean:message key="drac.network.list.results.role" />
                            </td>
                            <td class="rowth" nowrap>
                                <bean:message key="drac.network.list.results.state" />
                            </td>
                            <logic:equal parameter="layerFilter" value="layer1">
                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.cost" />
                                </td>
                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.metric" />
                                </td>
                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.srlg" />
                                </td>
                            </logic:equal>
                            <logic:equal parameter="layerFilter" value="layer2">
                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.speed" />
                                </td>
                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.mtu" />
                                </td>

                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.vcat" />
                                </td>
                            </logic:equal>
                            <logic:equal parameter="layerFilter" value="layer0">
                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.wavelength"/>
                                </td>
                            </logic:equal>
                            <td class="rowth" nowrap>
                                <bean:message key="drac.network.list.results.utilization"/>
                            </td>
                            <c:if test="${requestScope['groupFilter'] eq 'all'}">
                                <td class="rowth" nowrap>
                                    <bean:message key="drac.network.list.results.group"/>
                                </td>
                            </c:if>
                        </tr>
                        <logic:iterate id="endpoint" indexId="count" name="endPointList" scope="request">
                            <tr>
                                <c:set var="tdclass" value="row2" />
                                <c:if test="${count % 2 == 0}">
                                    <c:set var="tdclass" value="row1" />
                                </c:if>
                                <td align="center" class="${tdclass}">
                                    ${count+1}
                                </td>
                                <!-- <td align="center" class="${tdclass}">
                                    <c:choose>
                                    <c:when test='${endpoint.portType eq "ETH"}'>
                                    <a class="action"
                                        href="/network/editEndpoint.do?id=${endpoint.id}&name=${endpoint.name}">
                                        <bean:message key="drac.text.edit" /> </a>
                                    </c:when>
                                    <c:otherwise><i>Edit</i></c:otherwise>
                                    </c:choose>
                                </td> -->
                                <td align="center" class="${tdclass}">
                                    <a
                                        href="javascript:handleClick('endpoint${count}');">
                                        <img align="absmiddle" id="select${count}"
                                            name="select${count}"
                                            src="/images/plus.gif"
                                            onclick="swapImage('select${count}');">
                                    </a>
                                </td>
                                <td align="center" nowrap class="${tdclass}">
                                    <script type="text/javascript"> document.write(getMaxStringDisplay('${fn:escapeXml(endpoint.name)}', 60)); </script>
                                </td>
                                <td align="center" nowrap class="${tdclass}">
                                    <script type="text/javascript"> document.write(getMaxStringDisplay('${fn:escapeXml(endpoint.label)}')); </script>
                                </td>
                                <td align="center" nowrap class="${tdclass}">
                                    ${endpoint.portType}
                                </td>
                                <td align="center" class="${tdclass}">
                                    ${endpoint.signalingType}
                                </td>
                                <td align="center" class="${tdclass}">
                                    <c:choose>
                                        <c:when test='${endpoint.state != "IS"}'>
                                            <font color="red"><b><c:out
                                                        value="${endpoint.state}" />
                                            </b>
                                            </font>
                                        </c:when>
                                        <c:otherwise>
                                            ${endpoint.state}
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <logic:equal parameter="layerFilter" value="layer1">
                                    <td align="center" class="${tdclass}">
                                        ${endpoint.cost}
                                    </td>
                                    <td align="center" class="${tdclass}">
                                        ${endpoint.metric}
                                    </td>

                                    <td align="center" class="${tdclass}">
                                        ${endpoint.srlg}
                                    </td>
                                </logic:equal>
                                <logic:equal parameter="layerFilter" value="layer2">
                                    <td align="center" nowrap class="${tdclass}">
                                        ${endpoint.dataRate}&nbsp;<bean:message key="drac.text.Mbps" />
                                    </td>
                                    <td align="center" class="${tdclass}">
                                        ${endpoint.mtu}
                                    </td>
                                    <td align="center" class="${tdclass}">
                                        ${endpoint.vcat}
                                    </td>
                                </logic:equal>
                                <logic:equal parameter="layerFilter" value="layer0">
                                    <td align="center" class="${tdclass}">
                                        bla -> ${endpoint.wavelength}&nbsp;<bean:message key="drac.network.list.results.nanometers"/>
                                    </td>
                                </logic:equal>
                                <td align="center" class="${tdclass}">
                                    <table width="100px" border="1" bgcolor="#CCCCCC" cellpadding="0" cellspacing="0">
                                        <td align="left" id="${endpoint.name}"><img src="/images/spacer.gif" height="15" width="100"></td>
                                    </table>
                                    <div style="position:relative;top:-16px;z-index:10" align="center"><font color="#006699"><b><div id="${endpoint.name}_util"><bean:message key="drac.text.retrieving"/></div></b><font></div>
                                </td>
                                <c:if test="${requestScope['groupFilter'] eq 'all'}">
                                <td align="center" class="${tdclass}">
                                    <logic:iterate id="group" name="endpoint" property="groupList">
                                        <c:choose>
                                            <c:when test="${sessionScope['authObj'].userPolicyProfile.userGroupType ne 'USER'}">
                                                <html:link href="/management/userGroupManagement/queryUserGroup.do" paramId="ugName" paramName="group">${fn:escapeXml(group)}</html:link>&nbsp;
                                            </c:when>
                                            <c:otherwise>
                                                ${fn:escapeXml(group)}&nbsp;
                                            </c:otherwise>
                                        </c:choose>
                                    </logic:iterate>
                                </td>
                                </c:if>

                            </tr>
                            <tr id="endpoint${count}" style="display: none" border="0">
                                <td colspan="2" align="center"
                                    class="${tdclass}">
                                    &nbsp;
                                </td>
                                <td colspan="9" align="center"
                                    class="${tdclass}">
                                    <table align="left" cellspacing="1" cellpadding="1" border="0">
                                        <logic:iterate id="attribute" indexId="i" name="endpoint" property="attributes">
                                            <c:if test="${i % 2 == 0}">
                                            <tr>
                                            </c:if>
                                                <td>
                                                    <img src="/images/spacer.gif" width="10" />
                                                </td>
                                                <td class="gen" align="right">
                                                    <b>${attribute.key}
                                                    </b>
                                                </td>
                                                <td class="gen" align="center">
                                                    &nbsp;:&nbsp;
                                                </td>
                                                <td class="gen" align="left">
                                                    ${fn:escapeXml(attribute.value)}
                                                </td>
                                            <c:if test="${i % 2 != 0}">
                                            </tr>
                                            </c:if>
                                        </logic:iterate>
                                    </table>
                                </td>
                            </tr>
                        </logic:iterate>
                    </table>
                </logic:notEmpty>
            </td>
        </tr>
    </table>
</html:form>

<script type="text/javascript" src="/scripts/handleExpand.js"></script>
<script type="text/javascript" src="/scripts/ajax.js"></script>

<script language="javascript">

function processQueryUtilResponse(responseXML) {
    var utilTag = responseXML.getElementsByTagName("util")[0];
    var util = utilTag.firstChild.nodeValue;
    var tna = utilTag.attributes.getNamedItem("id").firstChild.nodeValue;
    var barTd = document.getElementById(tna);
    var utilDiv = document.getElementById(tna+"_util");
    if (util == -1) {
        utilDiv.innerHTML = "<c:out value='${drac.text.unknown}'/>";
    } else {
        utilDiv.innerHTML = util + "%";
    }

    if ((util >= 0.0) && (util <= 33.0)) {
        barTd.innerHTML = "<img src=\"/images/spacer_green.gif\" height=\"15\" width=\"" + util + "\"/>";
    } else if (util <= 50.0) {
        barTd.innerHTML = "<img src=\"/images/spacer_yellow.gif\" height=\"15\" width=\"" + util + "\"/>";
    } else if (util <= 75.0) {
        barTd.innerHTML = "<img src=\"/images/spacer_orange.gif\" height=\"15\" width=\"" + util + "\"/>";
    } else {
        barTd.innerHTML = "<img src=\"/images/spacer_red.gif\" height=\"15\" width=\"" + util + "\"/>";
    }
}

<logic:iterate id="endpoint" indexId="count" name="endPointList" scope="request">
new AJAXInteraction("/drac?action=queryUtil&tna=${endpoint.name}", processQueryUtilResponse).doGet();
</logic:iterate>


</script>
<%@ include file="/common/footer.jsp"%>
