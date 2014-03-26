/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.domain;

import java.text.DateFormat;
import java.util.Date;
import org.opendrac.nsi.pathfinding.PathSegment;

/**
 *
 * @author hacksaw
 */
public class PendingOperation {
    private String correlationId = null;
    private String parentCorrelationId = null;
    private String parentReplyTo = null;
    private PathSegment segment = null;
    private Date timeSent = null;
    private int numberOfRetries = 0;
    private OperationType operation = OperationType.Unknown;

    public enum OperationType {
        Reserve,
        Provision,
        Release,
        Terminate,
        Unknown
    }

    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * @return the parentCorrelationId
     */
    public String getParentCorrelationId() {
        return parentCorrelationId;
    }

    /**
     * @param parentCorrelationId the parentCorrelationId to set
     */
    public void setParentCorrelationId(String parentCorrelationId) {
        this.parentCorrelationId = parentCorrelationId;
    }

    /**
     * @return the parentReplyTo
     */
    public String getParentReplyTo() {
        return parentReplyTo;
    }

    /**
     * @param parentReplyTo the parentReplyTo to set
     */
    public void setParentReplyTo(String parentReplyTo) {
        this.parentReplyTo = parentReplyTo;
    }

    /**
     * @return the segment
     */
    public PathSegment getSegment() {
        return segment;
    }

    /**
     * @param segment the segment to set
     */
    public void setSegment(PathSegment segment) {
        this.segment = segment;
    }

    /**
     * @return the timeSent
     */
    public Date getTimeSent() {
        return timeSent;
    }

    /**
     * @param timeSent the timeSent to set
     */
    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

     /**
     * @param timeSent the timeSent to set
     */
    public void setTimeSentNow() {
        this.timeSent = new Date();
    }

    /**
     * @return the numberOfRetries
     */
    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    /**
     * @param numberOfRetries the numberOfRetries to set
     */
    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    /**
     * @return the operation
     */
    public OperationType getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

        StringBuilder result = new StringBuilder("{ correlationId=");
        result.append(correlationId);
        result.append(", operation=");
        result.append(operation);
        result.append(", parentCorrelationId=");
        result.append(parentCorrelationId);
        result.append(", parentReplyTo=");
        result.append(parentReplyTo);

        if (timeSent != null) {
            result.append(", timeSent=");
            result.append(df.format(timeSent));
        }

        result.append(", numberOfRetries=");
        result.append(numberOfRetries);

        if (segment != null) {
            result.append(", segment=");
            result.append(segment.toString());
        }

        result.append(" }");
        return result.toString();
    }
}
