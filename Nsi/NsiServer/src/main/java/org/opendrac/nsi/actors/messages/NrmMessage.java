/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.actors.messages;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public class NrmMessage implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static enum NrmMessageType {
        provisionMonitor,
        unknown
    }

    private NrmMessageType nrmMessageType = NrmMessageType.unknown;
    private String childConnectionId;
    private String parentConnectionId;
    private String nrmScheduleId;

    /**
     * @return the messageType
     */
    public NrmMessageType getNrmMessageType() {
        return nrmMessageType;
    }

    /**
     * @param messageType the messageType to set
     */
    public void setNrmMessageType(NrmMessageType nrmMessageType) {
        this.nrmMessageType = nrmMessageType;
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
     * @return the nrmScheduleId
     */
    public String getNrmScheduleId() {
        return nrmScheduleId;
    }

    /**
     * @param nrmScheduleId the nrmScheduleId to set
     */
    public void setNrmScheduleId(String nrmScheduleId) {
        this.nrmScheduleId = nrmScheduleId;
    }


}
