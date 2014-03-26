/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.topology;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author hacksaw
 */
public class Nsa {

    // networkURN - URN identifying the NSnetwork.
    private String nsaURN = null;

    // label - display name for the NSA.
    private String label = null;

    // managing - indicates the network being managed by this NSA.
    private String managing = null;

    // hostName - DNS name of the NSA.
    private String hostName = null;

    // csProviderEndpoint - the SOAP endpoint for this NSA's CS provider interface.
    private String csProviderEndpoint = null;

    // connectedTo - represents an NSA to which this NSA has a peering relationship.
    private Set<String> connectedTo = Collections.synchronizedSet(new HashSet<String>());

    // adminContact - administrative contact information for this NSA.
    private String adminContact = null;

    /**
     * Converts the NSA object to a structured string.
     * @return A string representation of the NSA object.
     */
    @Override
    public String toString() {
        if (nsaURN == null) {
            return "<null>";
        }

        StringBuilder tmp = new StringBuilder("NSA = { nsaURN = \"");
        tmp.append(nsaURN);
        tmp.append("\", label = \"");
        tmp.append(label);
        tmp.append("\", managing = \"");
        tmp.append(managing);
        tmp.append("\", hostName = \"");
        tmp.append(hostName);
        tmp.append("\", csProviderEndpoint = \"");
        tmp.append(csProviderEndpoint);
        tmp.append("\", adminContact = \"");
        tmp.append(adminContact);
        tmp.append("\", connectedTo = {");
        synchronized(connectedTo) {
            for (String uri : connectedTo) {
                tmp.append(" \"");
                tmp.append(uri);
                tmp.append("\",");
            }
        }
        tmp.append("} }");

        return tmp.toString();
    }

    /**
     * @return the nsaURN
     */
    public String getNsaURN() {
        return nsaURN;
    }

    /**
     * @param nsaURN the nsaURN to set
     */
    public void setNsaURN(String nsaURN) {
        this.nsaURN = nsaURN;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the managing
     */
    public String getManaging() {
        return managing;
    }

    /**
     * @param managing the managing to set
     */
    public void setManaging(String managing) {
        this.managing = managing;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the csProviderEndpoint
     */
    public String getCsProviderEndpoint() {
        return csProviderEndpoint;
    }

    /**
     * @param csProviderEndpoint the csProviderEndpoint to set
     */
    public void setCsProviderEndpoint(String csProviderEndpoint) {
        this.csProviderEndpoint = csProviderEndpoint;
    }

    /**
     * @return the connectedTo
     */
    public Set<String> getConnectedTo() {
        return connectedTo;
    }

    /**
     * @param connectedTo the connectedTo to set
     */
    public void setConnectedTo(Set<String> connectedTo) {
        synchronized(this.connectedTo) {
            this.connectedTo = connectedTo;
        }
    }

    /**
     * @return the adminContact
     */
    public String getAdminContact() {
        return adminContact;
    }

    /**
     * @param adminContact the adminContact to set
     */
    public void setAdminContact(String adminContact) {
        this.adminContact = adminContact;
    }

}
