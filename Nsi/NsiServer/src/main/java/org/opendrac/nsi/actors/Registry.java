package org.opendrac.nsi.actors;

import static akka.actor.Actors.*;

import org.opendrac.nsi.actors.reserve.ReserveConfirmedProviderActor;
import org.opendrac.nsi.actors.reserve.ReserveFailedProviderActor;
import org.opendrac.nsi.actors.reserve.ReserveProviderActor;
import org.opendrac.nsi.actors.terminate.TerminateProviderActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.Actor;
import akka.config.Config;
import akka.remoteinterface.RemoteServerModule;

public class Registry {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private RemoteServerModule remote;

	public static int serverPort = (Integer) Config.config()
	    .getInt("akka.remote.server.port").get();

	public static String serverHost = Config.config()
	    .getString("akka.remote.server.hostname").get();

	public enum Service {

    // TODO: Finish adding these registrations.
		ReserveProviderService("reserve-provider-service", ReserveProviderActor.class),
    ReserveConfirmedProviderService("reserveConfirmed-provider-service", ReserveConfirmedProviderActor.class),
    ReserveFailedProviderService("reserveFailed-provider-service", ReserveFailedProviderActor.class),
    TerminateProviderService("terminate-provider-service", TerminateProviderActor.class);

		final String name;
		final Class<? extends Actor> actor;

		Service(final String name, final Class<? extends Actor> actor) {
			this.name = name;
			this.actor = actor;
		}
	}

	/**
	 * Will register all defined services
	 */
	public void registerAll() {
		for (final Service service : Service.values()) {
			remote.register(service.name, actorOf(service.actor));
			log.info(String.format("Registering actor as service '%s', of type '%s'",
			    service.name, service.actor));
		}
	}

	public void startRemoteServiceServer() {
		if (remote == null || !remote.isRunning()) {
			log.info("Starting remote service: " + serverHost + ":"
			    + serverPort);
			remote = remote().start(serverHost, serverPort);
		}
	}

	public void stopRemoteServiceServer() {
		if (remote != null && remote.isRunning()) {
			log.info("Shutting down remote service");
			remote().shutdown();
		}
	}

}
