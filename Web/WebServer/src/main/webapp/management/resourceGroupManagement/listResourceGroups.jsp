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
 * File: /management/resourceGroupManagement/listResourceGroups.jsp
 *
 * Description:
 *
 ****************************************************************************/

String pageRef = "drac.security.resourceGroupManagement.list";
%>

<%@ include file="/common/header_struts.jsp"%>

<!-- Start of listResourceGroups.jsp -->
<script language="javascript">

//
// Disable the searchFor element.
//
function doReset()
{
    var obj = document.getElementById("searchFor");
    obj.disabled = true;
}

//
// Enable the searchFor element.
// Reset the searchFor element if searchBy is default value.
//
function doChangeSearchFor()
{
    var searchForLabel = document.getElementById("searchForLabel");
    var searchForField = document.getElementById("searchFor");
    var wildcard = document.getElementById("wildcardNote");

    if (document.getElementById("searchBy").value == "allResourceGroups")
    {
        searchForLabel.style.display = "none";

        searchForField.value = "";
        searchForField.disabled = true;
        searchForField.style.display = "none";

        wildcard.style.display = "none";
    }
    else
    {
        searchForLabel.style.display = "";

        searchForField.disabled = false;
        searchForField.style.display = "";

        wildcard.style.display = "";
    }
}


function checkForData()
{
    var obj = document.getElementById("searchBy");

    if (obj.value == "allResourceGroups" ) {
    } else {
        // Ensure search for field has some value.
        obj = document.getElementById("searchFor");
        if (obj.value == "") {
            alert("Please enter a value to search for.");
            return false;
        }
    }
    ListResourceGroupsForm.submit();
}

</script>

<html:form method="POST" action="/management/resourceGroupManagement/listResourceGroupsResult.do">
    <table width="350" cellspacing="0" cellpadding="0" border="0" align="center">
    <tbody>
        <tr>
            <td><img src="/images/spacer.gif" height="5"/></td>
        </tr>
        <tr>
            <td align="center"><bean:message key="drac.security.resourceGroupManagement.list.text"/></td>
        </tr>

        <tr>
            <td>
            <!-- Header -->
            <table border="0" cellpadding="0" cellspacing="0" class="tbt">
                <tbody>
                <tr><td><img src="/images/spacer.gif" alt="" height="5"/></td></tr>
                <tr>
                  <td class="tbtl"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                  <td class="tbtbot"><center><b><bean:message key="drac.security.list.searchCriteria"/></b></center></td>
                  <td class="tbtrr"><img src="/images/spacer.gif" alt="" width="22" height="22"/></td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>
        <tr>
            <td>
            <!-- Contents -->
            <table cellspacing="0" cellpadding="5" border="0" align="center" class="tbForm">
                <tbody>
                <tr>
                    <td class="tbForm1" nowrap>
                        <bean:message key="drac.security.list.searchBy" />
                    </td>
                    <td class="tbForm1">
                        <select name="searchBy" id="searchBy" size="1" style="width: 170px" onchange="doChangeSearchFor();">
                        <option value="allResourceGroups" selected><bean:message key="drac.security.resourceGroupManagement.option.all" /></option>
                        <option value="name"><bean:message key="drac.security.resourceGroupManagement.option.name" /></option>
                        </select>
                    </td>
                    <td class="tbForm1" id="searchForLabel" style="display:none" nowrap>
                        <bean:message key="drac.security.list.searchFor" />
                    </td>
                    <td class="tbForm1" nowrap>
                        <html:text property="searchFor" styleId="searchFor" disabled="true" style="display:none"/>
                    </td>
                </tr>
                <tr id="wildcardNote" style="display:none">
                    <td class="tbForm1" nowrap colspan="4" align="right">
                        <bean:message key="drac.security.list.wildcard" />
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
                  <td class="tbll"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                  <td class="tblbot"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                  <td class="tblr"><img src="/images/spacer.gif" alt="" width="8" height="4"/></td>
                </tr>
                </tbody>
            </table>
            </td>
        </tr>

    </tbody>
    </table>

    <table align="center">
        <tr><td><img src="/images/spacer.gif" height="10"/></td></tr>
        <tr>
            <td>
                <html:button property="Retrieve" onclick="checkForData();">
                <bean:message key="drac.text.submit"/>
                </html:button>
            </td>
            <td>
                <html:reset property="Reset" onclick="doReset();">
                <bean:message key="drac.text.reset"/>
                </html:reset>
            </td>
        </tr>

    </table>
</html:form>

<%@ include file="/common/footer.jsp"%>
