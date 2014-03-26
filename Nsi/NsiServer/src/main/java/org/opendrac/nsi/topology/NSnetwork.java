/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.topology;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;


/**
 *
 * @author hacksaw
 */
public class NSnetwork {

    // networkURN - URN identifying the NSnetwork.
    private String networkURN = null;

    // label - display name for the network.
    private String label = null;

    // canSwap - indicates VLAN Id interchange is supported in the network domain.
    private boolean canSwap = false;

    // managedBy - the Network Services Agent managing this network.
    private String managedBy = null;

    // stpList - identifies the inter-domain Service Termination Points that are within this network.
    private Set<String> stpList = Collections.synchronizedSet(new HashSet<String>());

    // nodeList - List of DTOX Nodes within this network.
    private Set<String> nodeList = Collections.synchronizedSet(new HashSet<String>());

    /**
     * Converts the NSnetwork object to a structured string.
     * @return A string representation of the NSnetwork object.
     */
    @Override
    public String toString() {
        if (networkURN == null) {
            return "<null>";
        }

        StringBuilder tmp = new StringBuilder("NSnetwork = { networkURN = \"");
        tmp.append(networkURN);
        tmp.append("\", label = \"");
        tmp.append(label);
        tmp.append("\", canSwap = \"");
        tmp.append(canSwap);
        tmp.append("\", managedBy = \"");
        tmp.append(managedBy);
        tmp.append("\", stpList = { ");
        synchronized(this) {
            for (String uri : stpList) {
                tmp.append("\"");
                tmp.append(uri);
                tmp.append("\", ");
            }
        }
        tmp.append("}, nodeList = { ");
        synchronized(this) {
            for (String uri : nodeList) {
               tmp.append("\"");
               tmp.append(uri);
               tmp.append("\", ");
            }
        }
        tmp.append(" } }");

        return tmp.toString();
    }

    /**
     * @return the networkURN
     */
    public String getNetworkURN() {
        return networkURN;
    }

    /**
     * @param networkURN the networkURN to set
     */
    public void setNetworkURN(String networkURN) {
        this.networkURN = networkURN;
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
     * @return the canSwap
     */
    public boolean isCanSwap() {
        return canSwap;
    }

    /**
     * @param canSwap the canSwap to set
     */
    public void setCanSwap(boolean canSwap) {
        this.canSwap = canSwap;
    }

    /**
     * @return the managedBy
     */
    public String getManagedBy() {
        return managedBy;
    }

    /**
     * @param managedBy the managedBy to set
     */
    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }

    /**
     * @return the stpList
     */
    public Set<String> getStpList() {
        return stpList;
    }

    /**
     * @param stpList The stpList to set.
     */
    public void setStpList(Set<String> stpList) {
        synchronized(this) {
            this.stpList = stpList;
        }
    }

    /**
     * Add the provided STP URN to list.
     * @param urn Unique identifier for the STP.
     * @return the added STP.
     */
    public boolean addStp(String urn) {
        boolean result = false;
        synchronized(this) {
            if (this.stpList == null) {
                this.stpList = new HashSet<String>();
            }
            result = this.stpList.add(urn);
        }
        return result;
    }

    /**
     * Remove the STP urn from list.
     * @param urn Unique identifier for the STP.
     * @return The deleted STP.
     */
    public boolean removeStp(String urn) {
        boolean result = false;
        synchronized(this) {
            result = this.stpList.remove(urn);
        }
        return result;
    }

    public boolean containsStp(String urn) {
        return this.stpList.contains(urn);
    }

    /**
     * @return The nodeList.
     */
    public Set<String> getNodeList() {
        return nodeList;
    }

    /**
     * @param nodeList the nodeList to set
     */
    public void setNodeList(Set<String> nodeList) {
        synchronized(this) {
            this.nodeList = nodeList;
        }
    }

    /**
     * Add the DTOX node urn to list.
     * @param urn Unique identifier for the node.
     * @return The added node.
     */
    public boolean addNode(String urn) {
        boolean result = false;
        synchronized(this) {
            if (this.nodeList == null) {
                this.nodeList = new HashSet<String>();
            }
            result = this.nodeList.add(urn);
        }
        return result;
    }

    /**
     * Delete the DTOX node urn from list.
     * @param urn Unique identifier for the node.
     * @return The deleted node.
     */
    public boolean removeNode(String urn) {
        boolean result = false;
        synchronized(this) {
            result = this.nodeList.remove(urn);
        }
        return result;
    }
}
