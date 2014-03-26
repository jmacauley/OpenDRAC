/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import java.util.List;
import org.ogf.schemas.nsi._2011._10.connection._interface.QueryRequestType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryFilterType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryType;

/**
 *
 * @author hacksaw
 */
public class Query extends NsiOperation {

    public QueryRequestType getQueryRequestType(List<String> connectionIdList, List<String> globalReservationIdList) {

        QueryFilterType filter = new QueryFilterType();
        if (connectionIdList != null) {
            filter.getConnectionId().addAll(connectionIdList);
        }
        
        if (globalReservationIdList != null) {
            filter.getGlobalReservationId().addAll(globalReservationIdList);
        }

        QueryType queryType = new QueryType();
        queryType.setOperation(QueryOperationType.SUMMARY);
        queryType.setProviderNSA(getProviderNSA());
        queryType.setRequesterNSA(getRequesterNSA());
        queryType.setSessionSecurityAttr(SessionSecurityAttr.getAttributeStatementType(getGlobalUserName(), getUserRole()));
        queryType.setQueryFilter(filter);

        QueryRequestType queryRequestType = new QueryRequestType();
        queryRequestType.setReplyTo(getReplyTo());
        queryRequestType.setCorrelationId(getCorrelationId());
        queryRequestType.setQuery(queryType);

        return queryRequestType;
    }
}