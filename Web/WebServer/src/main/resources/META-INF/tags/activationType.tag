<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ attribute name="value" required="true" %>
<c:choose>
    <c:when test="${value eq 'PRERESERVATION_MANUAL'}"><bean:message key="drac.reservation.type.PRERESERVATION_MANUAL"/></c:when>
    <c:when test="${value eq 'PRERESERVATION_AUTOMATIC'}"><bean:message key="drac.reservation.type.PRERESERVATION_AUTOMATIC"/></c:when>
    <c:when test="${value eq 'RESERVATION_MANUAL'}"><bean:message key="drac.reservation.type.RESERVATION_MANUAL"/></c:when>
    <c:when test="${value eq 'RESERVATION_AUTOMATIC'}"><bean:message key="drac.reservation.type.RESERVATION_AUTOMATIC"/></c:when>
    <c:otherwise>${value}</c:otherwise>
</c:choose>