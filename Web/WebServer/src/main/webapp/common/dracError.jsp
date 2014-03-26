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

<%@ page isErrorPage="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<c:choose>
  <c:when test="${not empty pageContext.exception}">
     <c:set var="problemType"><bean:message key="drac.error.type.jsp"/></c:set>
     <c:set var="appException" value="${pageContext.exception}"/>
     <c:set var="causeException" value="${appException.cause}"/>
  </c:when>
  <c:when test="${not empty requestScope['javax.servlet.
                                             error.exception']}">
     <c:set var="problemType"><bean:message key="drac.error.type.servlet"/></c:set>
     <c:set var="appException" value="${requestScope['javax.
                                           servlet.error.exception']}"/>
     <c:set var="causeException" value="${appException.rootCause}"/>
  </c:when>
  <c:when test="${not empty requestScope['dracError']}">
     <c:set var="problemType"><bean:message key="drac.error.type.app"/></c:set>
     <c:set var="appException" value="${requestScope['dracError']}"/>
     <c:set var="causeException" value="${appException.cause}"/>
  </c:when>
  <c:otherwise>
     <c:set var="problemType"><bean:message key="drac.error.type.unidentified"/></c:set>
  </c:otherwise>
</c:choose>

<%
/****************************************************************************
 * OpenDRAC Web GUI
 *
 * File: /common/dracPermission.jsp
 * Author: Darryl Cheung
 *
 * Description:
 *  This page displayed when user has no permission to perform the previous action
 *
 ****************************************************************************/

String pageRef = "drac.error.title";

%>

<%@ include file="/common/header_struts.jsp" %>

<script type="text/javascript" src="/scripts/handleExpand.js"></script>
    <!-- This table encompasses the contents of the page header. -->
<table border="0" cellpadding="0" cellspacing="0" width="90%" align="center">

<table width="100%" cellspacing="10" cellpadding="0" border="0">
    <tr>
        <td >
            <span class="gen"><bean:message key="drac.error.generic"/></span>
        </td>
    </tr>
    <tr>
        <td >
            <span class="gen"><a href="/welcome.do"><bean:message key="drac.error.return"/></a></span>
        </td>
    </tr>

                    <!-- Error message contents. -->
    <tr>
        <td class="row1">
             <html:errors/>
        </td>
    </tr>
    <tr valign="top">
      <td class="row1">
         <b><bean:message key="drac.error.problem.details"/>:</b>
         <font color="red">
         <c:if test="${not empty requestScope['javax.servlet.error.message']}">
                <c:out value="${requestScope['javax.servlet.error.message']}"/>
         </c:if>
         <c:if test="${not empty appException}">

            <c:if test="${requestScope['appDRACException'] eq 'true'}">
                <c:choose>
                    <c:when test="${not empty appException.key}">
                        <bean:message key="${appException.keyAsString}"/>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${appException.message}"/>
                    </c:otherwise>
                </c:choose>
            </c:if>
            <c:if test="${requestScope['appDRACException'] ne 'true'}">
                <c:out value="${appException.message}"/>
            </c:if>
         </c:if><br>
         </font>
         <b><bean:message key="drac.error.problem.type"/>:</b> <c:out value="${problemType}"/><br>
         <br>
         <c:if test="${not empty causeException}">
                <b><bean:message key="drac.error.cause"/>:</b> <c:out value="${causeException.class.canonicalName}"/>
              <br>
              <c:if test="${requestScope['causeDRACException'] eq 'true'}">
                <b><bean:message key="drac.error.cause.details"/>:</b> <bean:message key="${causeException.keyAsString}"
                    arg0="${causeException.args[0]}" arg1="${causeException.args[1]}" arg2="${causeException.args[2]}"
                    arg3="${causeException.args[3]}" arg4="${causeException.args[4]}" bundle="ERROR_RESOURCE_KEY"/>
              </c:if>
              <c:if test="${requestScope['causeDRACException'] ne 'true'}">
                <b><bean:message key="drac.error.cause.details"/>:</b> <c:out value="${causeException.message}" />
              </c:if>
         </c:if>
         <P>

<c:if test="${not empty appException || not empty causeException}">
        <a href="javascript:handleClick('details');"><bean:message key="drac.error.details"/></a><br>
</c:if>
        <!-- details table start -->
        <table width="100%" cellspacing="1" cellpadding="5" border="0" id="details" style="display: none" >
            <tr>
                <td>
                <c:if test="${not empty appException}">
                    <p></p>
                     <table cellpadding="4" cellspacing="0"
                            border="0" width="100%">
                        <tr>
                           <td>
                                 <h3><bean:message key="drac.error.exception.stacktrace"/></h3>
                           </td>
                        </tr>
                     </table>
                     <b><c:out value="${appException}"/></b>
                     <br/>
                     <table align="center" cellpadding="0" cellspacing="0" border="0" width="90%" class="pod">
                        <c:forEach var="stackItem" items="${appException.stackTrace}">
                            <tr><td><c:out value="${stackItem}"/></td></tr>
                        </c:forEach>
                     </table>
                </c:if>
                <c:if test="${not empty causeException}">
                    <p></p>
                    <table cellpadding="4" cellspacing="0" border="0" width="100%">
                        <tr>
                           <td>
                                 <h3><bean:message key="drac.error.cause.stacktrace"/></h3>
                           </td>
                        </tr>
                    </table>
                    <b><c:out value="${causeException}"/></b>
                    <br/>
                    <table align="center" cellpadding="0" cellspacing="0" border="0" width="90%" class="pod">
                        <c:forEach var="stackItem" items="${causeException.stackTrace}">
                            <tr><td><c:out value="${stackItem}"/></td></tr>
                        </c:forEach>
                    </table>
                </c:if>
                </td>
            </tr>
          </table>
          <!-- end details -->
       </td>
   </tr>
  <!-- End Error message contents. -->
</table>

<%@ include file="/common/footer.jsp"%>
