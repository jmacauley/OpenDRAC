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
     * File: /management/userManagement/listUsersResult.jsp
     *
     * Description:
     *   This page displays the results of a list users query.
     *
     ****************************************************************************/

    String pageRef = "drac.security.userManagement.list.results";
%>

<%@ include file="/common/header_struts.jsp"%>

<html>


<head>
<link rel="StyleSheet" href="/scripts/dtree/dtree.css" type="text/css" />
</head>

<body>
<script type="text/javascript" src="/scripts/dtree/dtree.js"></script>
<script type="text/javascript" src="/scripts/wz_tooltip.js"></script>

<table width="350" cellspacing="0" cellpadding="0" border="0"
	align="center">
	<tbody>
		<tr>
			<td><!-- Header -->
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
							key="drac.security.userManagement.list.results.title" /></b></center>
						</td>
						<td class="tbtrr"><img src="/images/spacer.gif" alt=""
							width="22" height="22" /></td>
					</tr>
				</tbody>
			</table>
			</td>
		</tr>
		<tr>
			<td>

<!-- Contents -->

<div class="dtree">
<table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
<tbody>

		<logic:empty name="indexedTreeNodesOfUsersAndUserGroups"
			scope="request">
			<tr>
				<td colspan="9" align="center" class="gen"><bean:message
					key="drac.security.userManagement.list.results.notavailable" />
				</td>
			</tr>
		</logic:empty>


		<logic:notEmpty name="indexedTreeNodesOfUsersAndUserGroups"
			scope="request">

			<tr>
				<td align="left" valign="top" class="tbForm1">
				<p><a href="javascript: d1.openAll();"><i>open all</a> |
				<a href="javascript: d1.closeAll();">close all</i></a></p>
				</td>
			</tr>


			<tr>
				<td align="left" valign="top" class="tbForm1">
				<table class="tbForm" align="left" border="0" cellpadding="5"
					cellspacing="0">
					<tbody>
						<tr>
							<td class="tbForm1" nowrap="nowrap">Find:</td>
							<td class="tbForm1" nowrap="nowrap"><input
								name="searchForD1" value="" id="searchForD1" type="text"
								onkeypress="checkKey(d1, 'searchForD1', event);"> <a
								href="JavaScript:void(0);" tabindex="-1"
								onclick="JavaScript:d1.find('decr', document.getElementById('searchForD1').value);">
							<img src="/images/arrow_beak_up.gif" style="height: 13px"
								border="0" align="top" vspace="0" hspace="0"></a> <a
								href="JavaScript:void(0);" tabindex="-1"
								onclick="JavaScript:d1.find('incr', document.getElementById('searchForD1').value);">
							<img src="/images/arrow_beak_down.gif" style="height: 13px"
								border="0" align="top" vspace="0" hspace="0"></a></td>
						</tr>
					</tbody>
				</table>
				</td>
			</tr>


<tr>
<td align="left" valign="top" class="tbForm1"><script type="text/javascript">

	    d1 = new dTree('d1', '/scripts/dtree/img');
	    <!-- configuration -->
	    d1.config.useIcons = true;
	    d1.config.useStatusText = true;
	    d1.add(0,-1,'User Membership');
	    <logic:iterate name="indexedTreeNodesOfUsersAndUserGroups" id="treeNode" scope="request">
	    <logic:present name="treeNode">
	     <c:choose>
		<c:when test='${treeNode.isGroup}'>
		    d1.add ( '${treeNode.idx}',
			      '${treeNode.parentIdx}',
			      '${treeNode.clientSafeName}',
			      '/management/userGroupManagement/queryUserGroup.do?ugName=${treeNode.webSafeName}',
			      '<b>User Group name: </b>' + '${treeNode.clientSafeName}',
			      '',
			      '',
			      ''
		     );

		</c:when>
		<c:otherwise>

                    var tooltip="";
                    <c:forEach var="hdr" items="${treeNode.clientDataMap}">
                      tooltip=tooltip+'${hdr.key}'+'${hdr.value}'+'<br>';
                    </c:forEach>

		    d1.add ( '${treeNode.idx}',
			      '${treeNode.parentIdx}',
			      '${treeNode.clientSafeName}',
			      '/management/userManagement/editUser.do?uid=${treeNode.webSafeName}',

			      tooltip,

			      '',
			      '/images/User.gif',
			      '/images/User.gif'
		     );
		</c:otherwise>
	     </c:choose>
	    </logic:present>
	    </logic:iterate>
	    document.write(d1);
	    d1.addToolTips();
       </script>
</td>
</tr>

		</logic:notEmpty>

</tbody>
</table>
</div>

<!-- Contents -->



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
	</tbody>
</table>

<script>
    function checkKey(tree, str, e)
    {
        var characterCode;

        if (e && e.which)
        {
            e = e;
            characterCode = e.which;
        }
        else
        {
            e = event;
            characterCode = e.keyCode;
        }

        if (characterCode == 13)
        {
            tree.find('incr', document.getElementById(str).value);
        }
}
</script>


</body>
</html>

<%@ include file="/common/footer.jsp"%>
