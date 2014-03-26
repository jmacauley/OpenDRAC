/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 *
 * @author hacksaw
 */
@Component("pendingOperationManager")
public class PendingOperationManager {

    private final Map<String, PendingOperation> pendingOperations = new ConcurrentHashMap<String, PendingOperation>();

    public PendingOperation add(String correlationId, PendingOperation operation) {
        return pendingOperations.put(correlationId, operation);
    }

    public PendingOperation add(PendingOperation operation) {
        return pendingOperations.put(operation.getCorrelationId(), operation);
    }

    public PendingOperation get(String correlationId) {
        return pendingOperations.get(correlationId);
    }

    public PendingOperation remove(String correlationId) {
        return pendingOperations.remove(correlationId);
    }

    public Map<String, PendingOperation> getPendingOperations() {
        return pendingOperations;
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder("PendingOperation = {\n");

        for (Map.Entry<String, PendingOperation> entry : pendingOperations.entrySet()) {
            String key = entry.getKey();
            PendingOperation operation = entry.getValue();
            tmp.append("    { key = \"");
            tmp.append(key);
            tmp.append("\", Operation = ");
            tmp.append(operation.toString());
            tmp.append(" }\n");
        }

        tmp.append("}");

        return tmp.toString();
    }
}
