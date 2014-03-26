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
import org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionServiceProvider;
import org.ogf.schemas.nsi._2011._10.connection.provider.ConnectionProviderPort;

/**
 *
 * @author hacksaw
 */
public class RequesterClientProxy {
    private static final Logger logger = LoggerFactory.getLogger(RequesterClientProxy.class);

    private ConnectionServiceProvider service = null;
    private ConnectionProviderPort proxy = null;

    private String providerEndpoint = null;

    public void setProviderEndpoint(String endpoint, String userID, String password) {
        setService(new ConnectionServiceProvider());
        setProxy(getService().getConnectionServiceProviderPort());

        logger.info("Service name: " + getService().getServiceName().toString());
        logger.info("WSDL location: " + getService().getWSDLDocumentLocation().toString());

        BindingProvider bp = (BindingProvider) getProxy();

        Map<String, Object> context = bp.getRequestContext();

        // Update the new endpoint if one was provided.
        if (endpoint != null && !endpoint.isEmpty()) {
            setProviderEndpoint(endpoint);
            context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            logger.info("New service endoint: " + endpoint);
        }

        // Set the HTTP basic authentication parameters if provided.
        if (userID != null && !userID.isEmpty()) {
            context.put(BindingProvider.USERNAME_PROPERTY, userID);
            context.put(BindingProvider.PASSWORD_PROPERTY, password);
            context.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        }
    }

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
    public ConnectionServiceProvider getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(ConnectionServiceProvider service) {
        this.service = service;
    }

    /**
     * @return the proxy
     */
    public ConnectionProviderPort getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy to set
     */
    public void setProxy(ConnectionProviderPort proxy) {
        this.proxy = proxy;
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

}
