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

<%
             /****************************************************************************
             * OpenDRAC Web GUI
             *
             * File: /network/editEndPoint.jsp
             *
             * Description:
             *  This page allows the user to edit endpoint MTU (if applicable)
             *
             ****************************************************************************/

            String pageRef = "drac.network.edit";
%>

<%@ include file="/common/header_struts.jsp"%>

<logic:messagesPresent message="true">
	<% String failed = (String) request.getAttribute("failed"); %>
	<table width="90%" cellspacing="5" cellpadding="0" border="0"
		align="center">
		<tr>
			<td>
				<img src="/images/spacer.gif" height="5" />
			</td>
		</tr>
		<tr>
		<td align="center" class="gen">
			<b><bean:message key="drac.network.edit.error" arg0="<c:out value='${failed}'/>"/></b>
		</td>
		</tr>
		<html:messages id="message" message="true">
		<tr>
			<td align="center" class="gen">
				<font color="red"><b><bean:write name="message"/></b></font>
			</td>
		</tr>
		</html:messages>
		<tr>
			<td>
				<img src="/images/spacer.gif" height="10" />
			</td>
		</tr>
	</table>
</logic:messagesPresent>

<logic:present name="status" scope="request">
	<% String status = (String) request.getAttribute("status"); %>
	<table width="90%" cellspacing="5" cellpadding="0" border="0"
		align="center">
		<tr>
			<td>
				<img src="/images/spacer.gif" height="5" />
			</td>
		</tr>
		<tr>
			<td align="center" class="gen">
				<b><bean:message key="drac.network.edit.editSuccess" arg0="<c:out value='${status}'/>" /></b>
			</td>
		</tr>
		<tr>
			<td>
				<img src="/images/spacer.gif" height="10" />
			</td>
		</tr>
	</table>
</logic:present>

<logic:messagesNotPresent message="true">
<logic:notPresent name="status" scope="request">
	<bean:parameter id="id" name="id" />
	<bean:parameter id="name" name="name" />

	<html:form action="/network/editEndpointAction.do">
		<input type="hidden" name="id" value="<c:out value="${id}"/>">
		<input type="hidden" name="name" value="<c:out value="${name}"/>">
		<table cellspacing="5" cellpadding="0" border="0" align="center">
			<tr>
				<td>
					<img src="/images/spacer.gif" height="5" />
				</td>
			</tr>
			<tr />
			<tr>
				<td>
					<table cellspacing="1" cellpadding="5" border="0" align="center"
						class="forumline">
						<tr>
							<th align="center" colspan=2>
								<span class="gen"><c:out value="${name}" /> </span>
							</th>
						<tr>
						<TR>
							<TD class="row2">
								<bean:message key="drac.network.edit.id" />
							</TD>
							<td class="row2">
								<input type="text" name="id" value="<c:out value="${id}"/>"
									style="width: 190px" disabled></input>
							</td>
						</TR>

						<TR>
							<TD class="row1">
								<bean:message key="drac.network.edit.mtu" />
							</td>
							<td class="row1">
								<html:select size="1" property="newMtu" style="width: 180px">
									<html:option value="">
										<bean:message key="drac.text.selectOption" />
									</html:option>
									<html:option value="1600">1600</html:option>
									<html:option value="9600">9600</html:option>
								</html:select>
							</td>
						</tr>
						<tr>
							<td colspan=2 class="row2" align="center">
								<html:submit property="Edit" onclick="return check('newMtu')">
									<bean:message key="drac.text.edit" />
								</html:submit>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<img src="/images/spacer.gif" height="5" />
				</td>
			</tr>
		</table>
	</html:form>
</logic:notPresent>
</logic:messagesNotPresent>

<script language="JavaScript">
function check(option) {
	var element = (document.getElementsByName(option))[0];
	if (element.value == '') {
		alert('<bean:message key="drac.network.edit.alert"/>');
		return false;
	}
	return true;
}
</script>


<%@ include file="/common/footer.jsp"%>
