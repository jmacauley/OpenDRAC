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
* File: /network/listEndpoints.jsp
*
* Description:
*  This page provides the user with a list endpoints filter for generating
*  an endpoint query.
*
****************************************************************************/

String pageRef = "drac.network.list";
%>

<%@ include file="/common/header_struts.jsp"%>

<html:form action="/network/listEndpointsAction.do">
<table cellspacing="0" cellpadding="0" border="0" align="center">
    <tr>
        <td>
            <img src="/images/spacer.gif" height="5" />
        </td>
    </tr>
    <tr>
        <td align="center" class="gen">
            <bean:message key="drac.network.list.text"/>
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
                <td class="tbtbot"><center><b><bean:message key="drac.network.list.filter.title"/></b></center></td>
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
                    <tr>
                        <td class="tbform1" nowrap>
                            <b><bean:message key="drac.network.list.filter.group" />
                            </b>
                        </td>
                        <td class="tbform1">
                            <html:select size="1" property="group" style="width: 180px">
                                <html:option value="all">
                                    <bean:message key="drac.schedule.list.filter.group.all" />
                                </html:option>
                                <html:options property="groupList"/>
                            </html:select>
                        </td>
                    </tr>
                    <tr>
                        <td class="tbform1" nowrap>
                            <b><bean:message key="drac.network.list.filter.layer" />
                            </b>
                        </td>
                        <td class="tbform1" align="right">
                            <html:select size="1" property="layerFilter" style="width: 180px">
                                <html:option value="layer0">
                                    <bean:message key="drac.network.list.filter.layer0"/>
                                </html:option>          
                                <html:option value="layer1">
                                    <bean:message key="drac.network.list.filter.layer1" />
                                </html:option>
                                <html:option value="layer2" >
                                    <bean:message key="drac.network.list.filter.layer2" />
                                </html:option>


                            </html:select>
                            </select>
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
</table>
<table align="center">
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
    <tr>
        <td>
            <html:submit property="Retrieve">
                <bean:message key="drac.text.submit"/>
            </html:submit>
        </td>
    </tr>
    <tr><td><img src="/images/spacer.gif" height="10" /></td></tr>
</table>
</html:form>

<%@ include file="/common/footer.jsp"%>
