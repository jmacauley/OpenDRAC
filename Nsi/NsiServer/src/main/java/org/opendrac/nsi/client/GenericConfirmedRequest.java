/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import org.ogf.schemas.nsi._2011._10.connection.types.GenericConfirmedType;
import org.opendrac.nsi.actors.messages.NsaMessage;

/**
 *
 * @author hacksaw
 */
public class GenericConfirmedRequest {
    public static GenericConfirmedType generate(NsaMessage message) {

        GenericConfirmedType generic = new GenericConfirmedType();
        generic.setProviderNSA(message.getProviderNSA());
        generic.setRequesterNSA(message.getRequesterNSA());
        generic.setConnectionId(message.getConnectionId());
        generic.setGlobalReservationId(message.getGlobalReservationId());
        return generic;
    }
}
