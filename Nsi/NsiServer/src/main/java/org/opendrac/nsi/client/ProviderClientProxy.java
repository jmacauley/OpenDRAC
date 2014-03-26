/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import java.io.Closeable;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionServiceRequester;
import org.ogf.schemas.nsi._2011._10.connection.requester.ConnectionRequesterPort;
import org.opendrac.nsi.config.NsaConfigurationManager;
import org.opendrac.nsi.config.xml.AuthenticationInfoType;
import org.opendrac.nsi.domain.DataManager;

/**
 * This is a web services client proxy for the NSA provider role.
 *
 * @author hacksaw
 */
public class ProviderClientProxy {
    private static final Logger logger = LoggerFactory.getLogger(RequesterClientProxy.class);

    private ConnectionServiceRequester service = null;
    private ConnectionRequesterPort proxy = null;

    private String requesterEndpoint = null;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (getProxy() != null) {
            try {
                ((Closeable) getProxy()).close();
            } catch (Exception ex) {
                logger.info("Exception closing proxy:" + ex.toString());
            }
        }
    }

    /**
     * @return the service
     */
    public ConnectionServiceRequester getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(ConnectionServiceRequester service) {
        this.service = service;
    }

    /**
     * @return the proxy
     */
    public ConnectionRequesterPort getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy to set
     */
    public void setProxy(ConnectionRequesterPort proxy) {
        this.proxy = proxy;
    }

    /**
     * @return the providerEndpoint
     */
    public String getRequesterEndpoint() {
        return requesterEndpoint;
    }

    /**
     * @param providerEndpoint the providerEndpoint to set
     */
    public void setRequesterEndpoint(String requesterEndpoint, String requesterNSA) throws IllegalArgumentException {

        // Get a reference to the realted data managers.
        NsaConfigurationManager nsaConfiguration = NsaConfigurationManager.getInstance();

        setService(new ConnectionServiceRequester());
        setProxy(getService().getConnectionServiceRequesterPort());

        BindingProvider bp = (BindingProvider) getProxy();

        Map<String, Object> context = bp.getRequestContext();

        // Update the new endpoint if one was provided.
        if (requesterEndpoint != null && !requesterEndpoint.isEmpty()) {
            context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requesterEndpoint);
            logger.info("setRequesterEndpoint: requesterEndpoint=" + requesterEndpoint);
        }
        else {
            throw new IllegalArgumentException("setRequesterEndpoint: requesterEndpoint not provided");
        }

        // Look for authentication information.
        AuthenticationInfoType auth = nsaConfiguration.getNsaAuthenticationInfo(requesterNSA);

        // Was there associated authentication information?  If not we can try without.
        if (auth != null && auth.getUserId() != null && !auth.getUserId().isEmpty()) {
            // We have credentials so set HTTP basic authentication.
            context.put(BindingProvider.USERNAME_PROPERTY, auth.getUserId());
            context.put(BindingProvider.PASSWORD_PROPERTY, auth.getPassword());
            context.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
            logger.info("setRequesterEndpoint: found credentials for requesterEndpoint=" + requesterEndpoint);
        }

        this.requesterEndpoint = requesterEndpoint;
    }
}

