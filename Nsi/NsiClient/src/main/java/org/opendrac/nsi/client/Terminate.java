/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import org.ogf.schemas.nsi._2011._10.connection._interface.TerminateRequestType;

/**
 *
 * @author hacksaw
 */
public class Terminate extends NsiOperation {

    public TerminateRequestType getTerminateRequestType() {


        TerminateRequestType termRequestType = new TerminateRequestType();
        termRequestType.setReplyTo(getReplyTo());
        termRequestType.setCorrelationId(getCorrelationId());
        termRequestType.setTerminate(getGenericRequestType());

        return termRequestType;
    }
}
