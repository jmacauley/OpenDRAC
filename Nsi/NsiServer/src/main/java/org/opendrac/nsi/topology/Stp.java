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
public class Stp {
    // stpURN - URN identifying the STP.
    private String stpURN = null;

    // label - display name for the STP.
    private String label = null;

    // partOf - indicates the member network of this STP.
    private String partOf = null;

    // connectedTo - a topological link identifying the peer STP to which this STP is connected.
    private Set<String> connectedTo = Collections.synchronizedSet(new HashSet<String>());

    // mapsTo - the logical STP represented by this object instance maps through to this physical entity (port, vlan, etx.) in DTOX.
    private String mapsTo = null;

    /**
     * Converts the STP object to a structured string.
     * @return A string representation of the STP object.
     */
    @Override
    public String toString() {
        if (stpURN == null) {
            return "<null>";
        }

        StringBuilder tmp = new StringBuilder("STP = { stpURN = \"");
        tmp.append(stpURN);
        tmp.append("\", label = \"");
        tmp.append(label);
        tmp.append("\", partOf = \"");
        tmp.append(partOf);
        tmp.append("\", mapsTo = \"");
        tmp.append(mapsTo);

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
     * @return the stpURN
     */
    public String getStpURN() {
        return stpURN;
    }

    /**
     * @param stpURN the stpURN to set
     */
    public void setStpURN(String stpURN) {
        this.stpURN = stpURN;
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
     * @return the partOf
     */
    public String getPartOf() {
        return partOf;
    }

    /**
     * @param partOf the partOf to set
     */
    public void setPartOf(String partOf) {
        this.partOf = partOf;
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
     * @return the mapsTo
     */
    public String getMapsTo() {
        return mapsTo;
    }

    /**
     * @param mapsTo the mapsTo to set
     */
    public void setMapsTo(String mapsTo) {
        this.mapsTo = mapsTo;
    }

}
