/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.util;

import java.util.UUID;

/**
 *
 * @author hacksaw
 */
public class UUIDUtilities {
	
    private final static String URN_UUID = "urn:uuid:";
    private final static String URN_SERVICE = "urn:ogf:network:service:netherlight.net:";

    public static String getUrnUuid() {
        return URN_UUID + UUID.randomUUID().toString();
    }
    
    public static String getGlobalReservationId() {
        return URN_SERVICE + UUID.randomUUID().toString();
    }
}
