/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 *
 * @author hacksaw
 */
public class ConnectionServiceServer {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionServiceServer.class);

    private final String LOCAL_WEB_SERVICES_CONTEXT  = "/nsi-v1/ConnectionServiceRequester";

    private HttpServer server = null;
    private HttpContext context = null;
    private Endpoint endpoint = null;
    private ExecutorService threads = null;

    public ConnectionServiceServer() throws IOException {

        endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new ConnectionServiceRequester());

    }

	public void server(String ipAddress, int port) throws IOException {

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(ipAddress);
        }
        catch (UnknownHostException ex) {
            throw (ex);
        }

        logger.info("Starting server...");
        server = HttpServer.create(new InetSocketAddress(inetAddress, port), 5);
        threads = Executors.newFixedThreadPool(5);
        server.setExecutor(threads);
        server.start();

        Binding binding = endpoint.getBinding();
		List<Handler> handlerChain = new LinkedList<Handler>();
		handlerChain.add(new EnvelopeLoggingSOAPHandler());
		binding.setHandlerChain(handlerChain);

		context = server.createContext(LOCAL_WEB_SERVICES_CONTEXT);
		context.setAuthenticator(new TestBasicAuthenticator("test"));
		endpoint.publish(context);
	}

	public void stop() {
		endpoint.stop();
	}

	public void shutdown() {
        if (endpoint != null) {
            endpoint.stop();
        }

        if (server != null) {
            server.stop(1);
        }

        if (threads != null) {
            threads.shutdown();
        }
	}

}
