/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.opendrac.nsi.pathfinding;

import static javax.persistence.GenerationType.*;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2011._10.connection.types.TechnologySpecificAttributesType;

/**
 * This {@link PathSegment} class models a child connection of a reservation
 * modeled in one of our {@link StateMachine} objects.  A child connection can
 * be a local NRM (LOCAL) path segment, or a remote NSA (REMOTE) path segment.
 * We maintain an individual {@link ConnectionStateType} here in the
 * {@link PathSegment} to shadow the state of the child reservation.  The parent
 * StateMachine can only transition when all {@link PathSegment} have
 * transitioned to the same state.
 *
 * @author hacksaw
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "NSI_PATH_SEGMENTS")
public class PathSegment implements Serializable {

    public enum NsaType {
        LOCAL,
        REMOTE
    }

    /*
     * ConnectionId of the original request and used as a primary key to index
     * into the state machine.
     */
    @Column(name = "PARENT_CONNECTION_ID", unique = false, nullable = true, length = 255)
    private String parentConnectionId;

    /*
     * New connectionId used to identify this path segment with child NSA.  We
     * can't reuse the parent connectionId since we may need to send multiple
     * connection requests to the same child NSA.  For this reason we generate
     * a new one for each path segment.
     */
    @Column(name = "CHILD_CONNECTION_ID", unique = false, nullable = true, length = 255)
    private String childConnectionId;

    // Field used to store segment specific data.
    @Column(name = "DATA", unique = false, nullable = true, length = 255)
    private String data;

    // Directionality this path segment.
    @Column(name = "DIRECTIONALITY", unique = false, nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DirectionalityType directionality = DirectionalityType.BIDIRECTIONAL;

    /*
     * There are our resolved STP topology entries.  Use URN only so we can
     * look them up when needed.
     */
    @Column(name = "SOURCE_STP_URN", unique = false, nullable = true, length = 255)
    private String sourceStpURN = null;

    @Column(name = "DEST_STP_URN", unique = false, nullable = true, length = 255)
    private String destStpURN = null;

    // The network containing the STP of this path segment.
    @Column(name = "NS_NETWORK_URN", unique = false, nullable = true, length = 255)
    private String nsNetworkURN = null;

    // NSA managing the network containing the STP of this path segment.
    @Column(name = "MANAGING_NSA_URN", unique = false, nullable = true, length = 255)
    private String managingNsaURN = null;

    /*
     * TODO: We did not maintain the tech attributes from the original STP but
     * may want them in the future.
     */

    // Type of NSA managing this domain.
    @Enumerated(EnumType.STRING)
    @Column(name = "NSA_TYPE", unique = false, nullable = false, length = 50)
    private NsaType nsaType = NsaType.LOCAL;

    // State of this connection segment.
    @Column(name = "CURRENT_STATE", unique = false, nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConnectionStateType currentState = ConnectionStateType.INITIAL;

    // TODO: Ignore serviceAttributes for now.
    @Column(name = "SERVICE_ATTRIBUTES", unique = false, nullable = true, length = 50)
    private TechnologySpecificAttributesType serviceAttributes = null;

    // Id column, needed for persistence
    @Id
  	@GeneratedValue(strategy = TABLE, generator = "nsi_sequences")
  	@TableGenerator(name = "nsi_sequences", table = "NSI_SEQUENCES", allocationSize = 1)
  	@Column(name = "ID", unique = true, nullable = false)
    private int id;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the parentConnectionId
     */
    public String getParentConnectionId() {
        return parentConnectionId;
    }

    /**
     * @param parentConnectionId the parentConnectionId to set
     */
    public void setParentConnectionId(String parentConnectionId) {
        this.parentConnectionId = parentConnectionId;
    }

    /**
     * @return the childConnectionId
     */
    public String getChildConnectionId() {
        return childConnectionId;
    }

    /**
     * @param childConnectionId the childConnectionId to set
     */
    public void setChildConnectionId(String childConnectionId) {
        this.childConnectionId = childConnectionId;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the currentState
     */
    public ConnectionStateType getCurrentState() {
        return currentState;
    }

    /**
     * @param currentState the currentState to set
     */
    public void setCurrentState(ConnectionStateType currentState) {
        this.currentState = currentState;
    }

    /**
     * @return the directionality
     */
    public DirectionalityType getDirectionality() {
        return directionality;
    }

    /**
     * @param directionality the directionality to set
     */
    public void setDirectionality(DirectionalityType directionality) {
        this.directionality = directionality;
    }

    /**
     * @return the sourceSTP
     */
    public String getSourceStpURN() {
        return sourceStpURN;
    }

    /**
     * @param sourceSTP the sourceSTP to set
     */
    public void setSourceStpURN(String sourceStpURN) {
        this.sourceStpURN = sourceStpURN;
    }

    /**
     * @return the destSTP
     */
    public String getDestStpURN() {
        return destStpURN;
    }

    /**
     * @param destSTP the destSTP to set
     */
    public void setDestStpURN(String destStpURN) {
        this.destStpURN = destStpURN;
    }

    /**
     * @return the managingNSA
     */
    public String getManagingNsaURN() {
        return managingNsaURN;
    }

    /**
     * @param managingNSA the managingNSA to set
     */
    public void setManagingNsaURN(String managingNsaURN) {
        this.managingNsaURN = managingNsaURN;
    }

    /**
     * @return the nsaType
     */
    public NsaType getNsaType() {
        return nsaType;
    }

    /**
     * @param nsaType the nsaType to set
     */
    public void setNsaType(NsaType nsaType) {
        this.nsaType = nsaType;
    }

    /**
     * @return the serviceAttributes
     */
    public TechnologySpecificAttributesType getServiceAttributes() {
        return serviceAttributes;
    }

    /**
     * @param serviceAttributes the serviceAttributes to set
     */
    public void setServiceAttributes(TechnologySpecificAttributesType serviceAttributes) {
        this.serviceAttributes = serviceAttributes;
    }

		@Override
    public String toString() {
        StringBuilder result = new StringBuilder("{ id=");
        result.append(getId());
        result.append(", parentConnectionId=");
        result.append(parentConnectionId);
        result.append(", childConnectionId=");
        result.append(childConnectionId);
        result.append(", data=");
        result.append(data);
        result.append(", directionality=");
        result.append(directionality.value());
        result.append(", sourceStpURN=");
        result.append(sourceStpURN);
        result.append(", destStpURN=");
        result.append(destStpURN);
        result.append(", nsNetworkURN=");
        result.append(nsNetworkURN);
        result.append(", managingNsaURN=");
        result.append(managingNsaURN);
        result.append(", nsaType=");
        result.append(nsaType);
        result.append(", currentState=");
        result.append(currentState.value());

        if (serviceAttributes != null) {
            result.append(", serviceAttributes=");
            result.append(serviceAttributes.toString());
        }

        result.append(" }");
        return result.toString();
    }
}
