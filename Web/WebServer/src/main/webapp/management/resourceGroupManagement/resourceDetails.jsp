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

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%
             /****************************************************************************
             * OpenDRAC Web GUI
             *
             * File: /management/resourceGroupManagement/resourceDetails.jsp
             *
             * Description:
             *
             ****************************************************************************/

String pageRef = "drac.security.resourceGroupManagement.resource.detail";
%>

<%@ include file="/common/header_struts.jsp"%>

<html:form action="/management/resourceGroupManagement/queryResource.do">
    <table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
    <tbody>
        <tr>
            <td>
                <!-- Header -->
                <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                    <tr><td><img src="/images/spacer.gif" alt="" height="5" /></td></tr>
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
                    <tr>
                        <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                        <td class="tbtbot"><center><b><bean:message key="drac.security.resourceManagement.resource" /></b></center></td>
                        <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22" /></td>
                    </tr>
                    </logic:messagesNotPresent>
                </tbody>
                </table>
            </td>
        </tr>

        <logic:messagesNotPresent message="true">
        <tr>
            <td>
                <!-- Contents -->
                <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                    <logic:notEmpty name="ResourceForm" property="resourceID" scope="request">
                    <tr>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.resource.detail.name" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceForm.name)}</b>
                        </td>
                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.resource.detail.resourceType" />
                        </td>
                        <td class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceForm.resourceType)}</b>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" class="tbForm1" nowrap>
                            <bean:message key="drac.network.list.results.label" />
                        </td>
                        <td valign="top" class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceForm.label)}</b>
                        </td>

                        <td class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.resource.detail.referencingResourceGroupNames" />
                        </td>
                        <td class="tbForm1" nowrap>
                        <logic:empty name="ResourceForm" property="referencingResourceGroupNames">
                            &nbsp;
                        </logic:empty>
                        <logic:notEmpty name="ResourceForm" property="referencingResourceGroupNames">
                        <logic:iterate id="refRgName" name="ResourceForm" property="referencingResourceGroupNames">
                            <b>${fn:escapeXml(refRgName)}</b>
                        </logic:iterate>
                        </logic:notEmpty>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" class="tbForm1" nowrap>
                            <bean:message key="drac.security.resourceGroupManagement.resource.detail.resourceID" />
                        </td>
                        <td valign="top" class="tbForm1" nowrap>
                            <b>${fn:escapeXml(ResourceForm.resourceID)}</b>
                        </td>
                        <td valign="top" class="tbForm1" nowrap>
                            &nbsp                            
                        </td>
                        <td valign="top" class="tbForm1" nowrap>
                            &nbsp                            
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
                        <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                        <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                    </tr>
                </tbody>
                </table>
            </td>
        </tr>
        </logic:messagesNotPresent>
    </tbody>
    </table>
</html:form>

<script type="text/javascript" src="/scripts/handleExpand.js"></script>

<%@ include file="/common/footer.jsp"%>
