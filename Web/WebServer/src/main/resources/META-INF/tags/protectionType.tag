<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ attribute name="value" required="true" %>
<c:choose>
    <c:when test="${value eq 'PATH1PLUS1'}">
        <bean:message key="drac.schedule.create.1plus1"/>
    </c:when>
    <c:when test="${value eq 'UNPROTECTED'}">
        <bean:message key="drac.schedule.create.unprotected"/>
    </c:when>
    <c:otherwise>${value}</c:otherwise>
</c:choose>