/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import org.ogf.schemas.nsi._2011._10.connection._interface.ReleaseRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericRequestType;

/**
 *
 * @author hacksaw
 */
public class Release {
    private String replyTo = null;
    private String correlationId = null;
    private String providerNSA = null;
    private String requesterNSA = null;
    private String globalUserName = null;
    private String userRole = null;
    private String connectionId = null;

    public ReleaseRequestType getReleaseRequestType() {
        GenericRequestType requestType = new GenericRequestType();
        requestType.setConnectionId(getConnectionId());
        requestType.setProviderNSA(getProviderNSA());
        requestType.setRequesterNSA(getRequesterNSA());
        requestType.setSessionSecurityAttr(SessionSecurityAttr.getAttributeStatementType(globalUserName, userRole));

        ReleaseRequestType relRequestType = new ReleaseRequestType();
        relRequestType.setReplyTo(getReplyTo());
        relRequestType.setCorrelationId(getCorrelationId());
        relRequestType.setRelease(requestType);

        return relRequestType;
    }

    /**
     * @return the replyTo
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * @param replyTo the replyTo to set
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
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
     * @return the providerNSA
     */
    public String getProviderNSA() {
        return providerNSA;
    }

    /**
     * @param providerNSA the providerNSA to set
     */
    public void setProviderNSA(String providerNSA) {
        this.providerNSA = providerNSA;
    }

    /**
     * @return the requesterNSA
     */
    public String getRequesterNSA() {
        return requesterNSA;
    }

    /**
     * @param requesterNSA the requesterNSA to set
     */
    public void setRequesterNSA(String requesterNSA) {
        this.requesterNSA = requesterNSA;
    }

    /**
     * @return the globalUserName
     */
    public String getGlobalUserName() {
        return globalUserName;
    }

    /**
     * @param globalUserName the globalUserName to set
     */
    public void setGlobalUserName(String globalUserName) {
        this.globalUserName = globalUserName;
    }

    /**
     * @return the userRole
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * @param userRole the userRole to set
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    /**
     * @return the connectionId
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * @param connectionId the connectionId to set
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
