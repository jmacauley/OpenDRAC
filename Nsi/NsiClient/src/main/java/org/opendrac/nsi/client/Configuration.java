/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

/**
 *
 * @author hacksaw
 */
public class Configuration {
    // Local web server configuration information.
    private String localWebServerAddress = "localhost";
    private int    localWebServerPort    = 9080;

    // Remote NSA configuration information.
    private String providerEndpoint   = "http://localhost:8080/nsi-v1/ConnectionServiceProvider";
    private String providerUserId     = "nsidemo";
    private String providerPassword   = "RioPlug-Fest2011!";

    // Requesting user information.
    //private String providerNsa        = "urn:ogf:network:nsa:netherlight";
    // private String providerNsa        = "urn:ogf:network:nsa:czechlight";
    private String providerNsa        = "urn:ogf:network:surfnet.nl:2012:drac.surfnet.nl";

    private String globalUserName     = "jrv@internet2.edu";
    private String globalRole         = "AuthorizedUser";

    // Reservation information.
    //private String reservationDescription       = "This is a test schedule connecting cph-80 to pra-80";
    //private String reservationSourceStp         = "urn:ogf:network:stp:netherlight.ets:cph-80";
    //private String reservationDestinationStp    = "urn:ogf:network:stp:netherlight.ets:pra-80";

    private String reservationDescription       = "This is an NsiClient test schedule.";
    private String reservationSourceStp         = "urn:ogf:network:surfnet.nl:2012:Asd001A_OME12_ETH-1-36-4";
    private String reservationDestinationStp    = "urn:ogf:network:surfnet.nl:2012:Asd001A_OME1T_ETH-1-3-1";

    private int    reservationBandwidth         = 200;
    private String reservationProtection        = "Shit"; // One of Unprotected, Redundant, or Protected.

    // My local NSA identity information.
    private String requesterEndpoint   = "http://localhost:9080/nsi-v1/ConnectionServiceRequester";
    private String requesterNSA        = "urn:ogf:network:nsa:ferb.surfnet.nl";

    /**
     * @return the localWebServerAddress
     */
    public String getLocalWebServerAddress() {
        return localWebServerAddress;
    }

    /**
     * @param localWebServerAddress the localWebServerAddress to set
     */
    public void setLocalWebServerAddress(String localWebServerAddress) {
        this.localWebServerAddress = localWebServerAddress;
    }

    /**
     * @return the localWebServerPort
     */
    public int getLocalWebServerPort() {
        return localWebServerPort;
    }

    /**
     * @param localWebServerPort the localWebServerPort to set
     */
    public void setLocalWebServerPort(int localWebServerPort) {
        this.localWebServerPort = localWebServerPort;
    }

    /**
     * @return the providerEndpoint
     */
    public String getProviderEndpoint() {
        return providerEndpoint;
    }

    /**
     * @param providerEndpoint the providerEndpoint to set
     */
    public void setProviderEndpoint(String providerEndpoint) {
        this.providerEndpoint = providerEndpoint;
    }

    /**
     * @return the providerUserId
     */
    public String getProviderUserId() {
        return providerUserId;
    }

    /**
     * @param providerUserId the providerUserId to set
     */
    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    /**
     * @return the providerPassword
     */
    public String getProviderPassword() {
        return providerPassword;
    }

    /**
     * @param providerPassword the providerPassword to set
     */
    public void setProviderPassword(String providerPassword) {
        this.providerPassword = providerPassword;
    }

    /**
     * @return the globalRole
     */
    public String getGlobalRole() {
        return globalRole;
    }

    /**
     * @param globalRole the globalRole to set
     */
    public void setGlobalRole(String globalRole) {
        this.globalRole = globalRole;
    }

    /**
     * @return the providerNsa
     */
    public String getProviderNsa() {
        return providerNsa;
    }

    /**
     * @param providerNsa the providerNsa to set
     */
    public void setProviderNsa(String providerNsa) {
        this.providerNsa = providerNsa;
    }

    /**
     * @return the globalUserName
     */
    public String getGlobalUserName() {
        return globalUserName;
    }

    /**
     * @param globalUserName the globalUserName to set
     */
    public void setGlobalUserName(String globalUserName) {
        this.globalUserName = globalUserName;
    }

    /**
     * @return the reservationDescription
     */
    public String getReservationDescription() {
        return reservationDescription;
    }

    /**
     * @param reservationDescription the reservationDescription to set
     */
    public void setReservationDescription(String reservationDescription) {
        this.reservationDescription = reservationDescription;
    }

    /**
     * @return the reservationSourceStp
     */
    public String getReservationSourceStp() {
        return reservationSourceStp;
    }

    /**
     * @param reservationSourceStp the reservationSourceStp to set
     */
    public void setReservationSourceStp(String reservationSourceStp) {
        this.reservationSourceStp = reservationSourceStp;
    }

    /**
     * @return the reservationDestinationStp
     */
    public String getReservationDestinationStp() {
        return reservationDestinationStp;
    }

    /**
     * @param reservationDestinationStp the reservationDestinationStp to set
     */
    public void setReservationDestinationStp(String reservationDestinationStp) {
        this.reservationDestinationStp = reservationDestinationStp;
    }

    /**
     * @return the reservationBandwidth
     */
    public int getReservationBandwidth() {
        return reservationBandwidth;
    }

    /**
     * @param reservationBandwidth the reservationBandwidth to set
     */
    public void setReservationBandwidth(int reservationBandwidth) {
        this.reservationBandwidth = reservationBandwidth;
    }

    /**
     * @return the requesterEndpoint
     */
    public String getRequesterEndpoint() {
        return requesterEndpoint;
    }

    /**
     * @param requesterEndpoint the requesterEndpoint to set
     */
    public void setRequesterEndpoint(String requesterEndpoint) {
        this.requesterEndpoint = requesterEndpoint;
    }

    /**
     * @return the requesterNSA
     */
    public String getRequesterNSA() {
        return requesterNSA;
    }

    /**
     * @param requesterNSA the requesterNSA to set
     */
    public void setRequesterNSA(String requesterNSA) {
        this.requesterNSA = requesterNSA;
    }

    /**
     * @return the reservationProtection
     */
    public String getReservationProtection() {
        return reservationProtection;
    }

    /**
     * @param reservationProtection the reservationProtection to set
     */
    public void setReservationProtection(String reservationProtection) {
        this.reservationProtection = reservationProtection;
    }


}
