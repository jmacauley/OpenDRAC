<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ attribute name="value" required="true" %>
<c:choose>
    <c:when test="${value eq 'CONFIRMATION_PENDING'}"><bean:message key="drac.reservation.status.CONFIRMATION_PENDING"/></c:when>
    <c:when test="${value eq 'CONFIRMATION_TIMED_OUT'}"><bean:message key="drac.reservation.status.CONFIRMATION_TIMED_OUT"/></c:when>
    <c:when test="${value eq 'CONFIRMATION_CANCELLED'}"><bean:message key="drac.reservation.status.CONFIRMATION_CANCELLED"/></c:when>
    <c:when test="${value eq 'ACTIVATION_PENDING'}"><bean:message key="drac.reservation.status.ACTIVATION_PENDING"/></c:when>
    <c:when test="${value eq 'ACTIVATION_TIMED_OUT'}"><bean:message key="drac.reservation.status.ACTIVATION_TIMED_OUT"/></c:when>
    <c:when test="${value eq 'ACTIVATION_CANCELLED'}"><bean:message key="drac.reservation.status.ACTIVATION_CANCELLED"/></c:when>
    <c:when test="${value eq 'EXECUTION_PENDING'}"><bean:message key="drac.reservation.status.EXECUTION_PENDING"/></c:when>
    <c:when test="${value eq 'EXECUTION_ACTIVE'}"><bean:message key="drac.reservation.status.EXECUTION_ACTIVE"/></c:when>
    <c:when test="${value eq 'EXECUTION_INPROGRESS'}"><bean:message key="drac.reservation.status.EXECUTION_INPROGRESS"/></c:when>
    <c:when test="${value eq 'EXECUTION_SUCCEEDED'}"><bean:message key="drac.reservation.status.EXECUTION_SUCCEEDED"/></c:when>
    <c:when test="${value eq 'EXECUTION_PARTIALLY_SUCCEEDED'}"><bean:message key="drac.reservation.status.EXECUTION_PARTIALLY_SUCCEEDED"/></c:when>
    <c:when test="${value eq 'EXECUTION_TIME_OUT'}"><bean:message key="drac.reservation.status.EXECUTION_TIME_OUT"/></c:when>
    <c:when test="${value eq 'EXECUTION_FAILED'}"><bean:message key="drac.reservation.status.EXECUTION_FAILED"/></c:when>
    <c:when test="${value eq 'EXECUTION_PARTIALLY_CANCELLED'}"><bean:message key="drac.reservation.status.EXECUTION_PARTIALLY_CANCELLED"/></c:when>
    <c:when test="${value eq 'EXECUTION_CANCELLED'}"><bean:message key="drac.reservation.status.EXECUTION_CANCELLED"/></c:when>
    <c:when test="${value eq 'CREATE_FAILED'}"><bean:message key="drac.reservation.status.CREATE_FAILED"/></c:when>
    <c:when test="${value eq 'DELETE_FAILED'}"><bean:message key="drac.reservation.status.DELETE_FAILED"/></c:when>
    <c:when test="${value eq 'Active'}"><bean:message key="drac.schedule.status.Active"/></c:when>
    <c:when test="${value eq 'InActive'}"><bean:message key="drac.schedule.status.InActive"/></c:when>
    <c:otherwise>${value}</c:otherwise>
</c:choose>