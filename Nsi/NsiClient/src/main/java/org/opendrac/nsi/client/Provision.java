/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import org.ogf.schemas.nsi._2011._10.connection._interface.ProvisionRequestType;

/**
 *
 * @author hacksaw
 */
public class Provision extends NsiOperation {

    public ProvisionRequestType getProvisionRequestType() {

        ProvisionRequestType provRequestType = new ProvisionRequestType();
        provRequestType.setReplyTo(getReplyTo());
        provRequestType.setCorrelationId(getCorrelationId());
        provRequestType.setProvision(getGenericRequestType());

        return provRequestType;
    }
}
