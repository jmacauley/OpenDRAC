/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.domain;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.opendrac.nsi.util.ExceptionCodes;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author hacksaw
 */
@Component("stateMachineManager")
public class StateMachineManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // stateMachines - List of StateMachine objects representing reservations.
    private Map<String, StateMachine> stateMachines = new ConcurrentHashMap<String, StateMachine>();

    public static StateMachineManager getInstance() {
        StateMachineManager stateMachineManager = SpringApplicationContext.getBean("stateMachineManager", StateMachineManager.class);
        return stateMachineManager;
    }

    /**
     * @return the stateMachines
     */
    public Map<String, StateMachine> getStateMachines() {
        return stateMachines;
    }

    /**
     * @return the stateMachines
     */
    public Collection<StateMachine> getStateMachineList() {
        return stateMachines.values();
    }

    /**
     * @param stateMachines the stateMachines to set
     */
    public void setStateMachines(Map<String, StateMachine> stateMachines) {
        this.stateMachines = stateMachines;
    }

    /**
     * @return the stateMachine
     */
    public StateMachine getStateMachine(String key) {
        return stateMachines.get(key);
    }

    /**
     * @param key the index for the stateMachine to add
     * @param stateMachine the stateMachines to add
     */
    public void putStateMachine(String key, StateMachine stateMachine) {
        stateMachines.put(key, stateMachine);
    }

    /**
     * @param key the stateMachine to remove
     * @return stateMachine removed
     */
    public StateMachine removeStateMachine(String key) {
        return stateMachines.remove(key);
    }

    /**
     * @param key the stateMachine to query existence
     * @return true if stateMachine exists and false otherwise
     */
    public boolean containsStateMachine(String key) {
        return stateMachines.containsKey(key);
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder("StateMachines = {\n");

        for (Map.Entry<String, StateMachine> entry : stateMachines.entrySet()) {
            String key = entry.getKey();
            StateMachine machine = entry.getValue();
            tmp.append("    { key = \"");
            tmp.append(key);
            tmp.append("\", Machine = ");
            tmp.append(machine.toString());
            tmp.append(" }\n");
        }

        tmp.append("}");

        return tmp.toString();
    }

    /**
     * Return state machine if it already exists or throw an appropriate
     * exception if it does not.
     *
     * @param manager the manager holding the state machines.
     * @param connectionId the index key used to retrieve the state machine.
     * @throws ServiceException
     */
    public StateMachine failOnNoStateMachine(String connectionId) throws ServiceException {
        StateMachine machine = this.getStateMachine(connectionId);

        if (machine == null) {
            logger.info("StateMachineManager.failOnNoStateMachine: State machine does not exists for connectionId = " + connectionId);
            throw ExceptionCodes.buildProviderException(ExceptionCodes.DOES_NOT_EXIST, "connectionId", connectionId);
        }

        logger.info("StateMachineManager.failOnNoStateMachine: State machine found for connectionId = " +
                connectionId + ", with state = " +
                machine.getCurrentState().name());
        return machine;
    }


    /**
     * Determines if the state machine already exists and will return
     * appropriate exception if it does.
     *
     * @param manager the manager holding the state machines.
     * @param connectionId the index key used to retrieve the state machine.
     * @throws ServiceException
     */
    public void failOnStateMachine(String connectionId) throws ServiceException {
        if (this.containsStateMachine(connectionId) == true) {
            logger.info("StateMachineManager.failOnStateMachine: State machine exists for connectionId = " + connectionId);
            throw ExceptionCodes.buildProviderException(ExceptionCodes.ALREADY_EXISTS, "connectionId", connectionId);
        }

        logger.info("StateMachineManager.failOnStateMachine: State machine does not exist for connectionId = " + connectionId);
    }
}
