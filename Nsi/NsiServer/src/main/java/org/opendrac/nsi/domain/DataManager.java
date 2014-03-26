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
package org.opendrac.nsi.domain;

import org.opendrac.nsi.config.NsaConfigurationManager;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.topology.TopologyFactory;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides basic topology services for NSI.  Loaded by Spring on start-up and
 * has topology file injected.  Creates new TopologyFactory using the supplied
 * topology file that represents both the inter-domain and intra-domain
 * topology of NSI.
 *
 * TODO: Would like this class to be a bridge between topology and routing
 * graphs.
 *
 * @author hacksaw
 */
@Component("dataManager")
public class DataManager {
    private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

    @Value("#{nsiProperties.topologyFile}")
    private String topologyFile;

    @Autowired
    @Qualifier("nsaConfigurationManager")
    private NsaConfigurationManager nsaConfigurationManager;

    @Autowired
    @Qualifier("stateMachineManager")
    private StateMachineManager stateMachineManager;

    @Autowired
    @Qualifier("pendingOperationManager")
    private PendingOperationManager pendingOperationManager;

    @Autowired
    @Qualifier("castingDirector")
    private CastingDirector castingDirector;


    private TopologyFactory topologyFactory = null;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() throws IOException{
        // Load the NSI topology file.
    	this.setTopologyFile(topologyFile);
    }

    public static DataManager getInstance() {
        DataManager dataManager = SpringApplicationContext.getBean("dataManager", DataManager.class);
        return dataManager;
    }

    /**
     * @return the topologyFile
     */
    public String getTopologyFile() {
        return topologyFile;
    }

    /**
     * This method may be called through IoC or by an audit that has detected
     * a topology file change.  We want to only replace the existing topology
     * if we can successfully parse the new one.
     *
     * @param topologyFile the topologyFile to set
     */
    public void setTopologyFile(String topologyFile) {
        logger.debug("Configuring topology using:: " + topologyFile);
        try {
            this.topologyFactory = new TopologyFactory(topologyFile);
            logger.info("New topology: " + topologyFile);
            this.topologyFile = topologyFile;
        }
        catch (IOException ex) {
            logger.error("Could not load file: " + topologyFile);
        }
    }

    public TopologyFactory getTopologyFactory() {
        return topologyFactory;
    }

    /**
     * @return the nsaConfigurationManager
     */
    public NsaConfigurationManager getNsaConfigurationManager() {
        return nsaConfigurationManager;
    }

    /**
     * @return the stateMachineManager
     */
    public StateMachineManager getStateMachineManager() {
        return stateMachineManager;
    }

    /**
     * @return the pendingOperationManager
     */
    public PendingOperationManager getPendingOperationManager() {
        return pendingOperationManager;
    }

    /**
     * @return the castingDirector
     */
    public CastingDirector getCastingDirector() {
        return castingDirector;
    }
}
