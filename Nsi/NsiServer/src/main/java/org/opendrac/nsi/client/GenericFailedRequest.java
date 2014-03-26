/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import org.opendrac.nsi.actors.messages.NsaMessage;
import org.ogf.schemas.nsi._2011._10.connection.types.GenericFailedType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceExceptionType;

/**
 *
 * @author hacksaw
 */
public class GenericFailedRequest {

    public static GenericFailedType generate(NsaMessage message) {
        GenericFailedType generic = new GenericFailedType();
        generic.setConnectionId(message.getConnectionId());
        generic.setConnectionState(message.getConnectionState());
        generic.setGlobalReservationId(message.getGlobalReservationId());
        generic.setProviderNSA(message.getProviderNSA());
        generic.setRequesterNSA(message.getRequesterNSA());

        ServiceExceptionType nsi = (ServiceExceptionType) message.getPayload();
        generic.setServiceException(nsi);

        return generic;
    }
}