/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.actors;

import static akka.actor.Actors.*;

import org.apache.commons.lang.builder.HashCodeBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.routing.UntypedDispatcher;

import org.opendrac.nsi.actors.provision.ProvisionFailedRequesterActor;
import org.opendrac.nsi.actors.reserve.ReserveFailedRequesterActor;
import org.opendrac.nsi.actors.messages.NsaMessage;
import org.opendrac.nsi.actors.provision.ProvisionConfirmedProviderActor;
import org.opendrac.nsi.actors.provision.ProvisionConfirmedRequesterActor;
import org.opendrac.nsi.actors.provision.ProvisionFailedProviderActor;
import org.opendrac.nsi.actors.provision.ProvisionProviderActor;
import org.opendrac.nsi.actors.provision.ProvisionRequesterLocalActor;
import org.opendrac.nsi.actors.provision.ProvisionRequesterRemoteActor;
import org.opendrac.nsi.actors.reserve.ReserveConfirmedProviderActor;
import org.opendrac.nsi.actors.reserve.ReserveConfirmedRequesterActor;
import org.opendrac.nsi.actors.reserve.ReserveFailedProviderActor;
import org.opendrac.nsi.actors.reserve.ReserveProviderActor;
import org.opendrac.nsi.actors.reserve.ReserveRequesterLocalActor;
import org.opendrac.nsi.actors.reserve.ReserveRequesterRemoteActor;
import org.opendrac.nsi.actors.terminate.TerminateConfirmedProviderActor;
import org.opendrac.nsi.actors.terminate.TerminateConfirmedRequesterActor;
import org.opendrac.nsi.actors.terminate.TerminateFailedProviderActor;
import org.opendrac.nsi.actors.terminate.TerminateFailedRequesterActor;
import org.opendrac.nsi.actors.terminate.TerminateProviderActor;
import org.opendrac.nsi.actors.terminate.TerminateRequesterLocalActor;
import org.opendrac.nsi.actors.terminate.TerminateRequesterRemoteActor;
import org.opendrac.nsi.actors.messages.NrmMessage;
import org.opendrac.nsi.actors.query.QueryConfirmedProviderActor;
import org.opendrac.nsi.actors.query.QueryFailedProviderActor;
import org.opendrac.nsi.actors.query.QuerySummaryProviderActor;
import org.opendrac.nsi.actors.query.QueryRequesterLocalActor;
import org.opendrac.nsi.nrm.NrmProvisionActor;

/**
 *
 * @author hacksaw
 */
public class ActorLoadBalancer extends UntypedDispatcher {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Registry registry;
    private int workerAmount;

    /* Reserve Actor set. */
    private ActorRef[] reserveProviderActors = null;
    private ActorRef[] reserveFailedProviderActors = null;
    private ActorRef[] reserveConfirmedProviderActors = null;

    private ActorRef[] reserveRequesterRemoteActors = null;
    private ActorRef[] reserveRequesterLocalActors = null;
    private ActorRef[] reserveConfirmedRequesterActors = null;
    private ActorRef[] reserveFailedRequesterActors = null;

    /* Provision Actor set. */
    private ActorRef[] provisionProviderActors = null;
    private ActorRef[] provisionFailedProviderActors = null;
    private ActorRef[] provisionConfirmedProviderActors = null;

    private ActorRef[] provisionRequesterRemoteActors = null;
    private ActorRef[] provisionRequesterLocalActors = null;
    private ActorRef[] provisionConfirmedRequesterActors = null;
    private ActorRef[] provisionFailedRequesterActors = null;

    /* Terminate Actor set. */
    private ActorRef[] terminateProviderActors = null;
    private ActorRef[] terminateFailedProviderActors = null;
    private ActorRef[] terminateConfirmedProviderActors = null;

    private ActorRef[] terminateRequesterRemoteActors = null;
    private ActorRef[] terminateRequesterLocalActors = null;
    private ActorRef[] terminateConfirmedRequesterActors = null;
    private ActorRef[] terminateFailedRequesterActors = null;

    /* Query Actor set. */
    private ActorRef[] querySummaryProviderActors = null;
    private ActorRef[] queryFailedProviderActors = null;
    private ActorRef[] queryConfirmedProviderActors = null;

    private ActorRef[] queryRequesterRemoteActors = null;
    private ActorRef[] queryRequesterLocalActors = null;
    private ActorRef[] queryConfirmedRequesterActors = null;
    private ActorRef[] queryFailedRequesterActors = null;

    /* NRM monitoring message set. */
    private ActorRef[] nrmProvisionActors = null;

	public ActorLoadBalancer(Registry registry, int workerAmount) {
        super();

        this.registry = registry;
        this.workerAmount = workerAmount;

        /*
         * Build individual sets of actor pools so we can route to specialized
         * actors per message type.
         */
        log.info("Starting with " + workerAmount + " workers.");

        /* Reserve Actor set. */
        reserveProviderActors = new ActorRef[workerAmount];
        reserveFailedProviderActors = new ActorRef[workerAmount];
        reserveConfirmedProviderActors = new ActorRef[workerAmount];

        reserveRequesterRemoteActors = new ActorRef[workerAmount];
        reserveRequesterLocalActors = new ActorRef[workerAmount];
        reserveConfirmedRequesterActors = new ActorRef[workerAmount];
        reserveFailedRequesterActors = new ActorRef[workerAmount];

        /* Provision Actor set. */
        provisionProviderActors = new ActorRef[workerAmount];
        provisionFailedProviderActors = new ActorRef[workerAmount];
        provisionConfirmedProviderActors = new ActorRef[workerAmount];

        provisionRequesterRemoteActors = new ActorRef[workerAmount];
        provisionRequesterLocalActors = new ActorRef[workerAmount];
        provisionConfirmedRequesterActors = new ActorRef[workerAmount];
        provisionFailedRequesterActors = new ActorRef[workerAmount];

        /* Terminate Actor set. */
        terminateProviderActors = new ActorRef[workerAmount];
        terminateFailedProviderActors = new ActorRef[workerAmount];
        terminateConfirmedProviderActors = new ActorRef[workerAmount];

        terminateRequesterRemoteActors = new ActorRef[workerAmount];
        terminateRequesterLocalActors = new ActorRef[workerAmount];
        terminateConfirmedRequesterActors = new ActorRef[workerAmount];
        terminateFailedRequesterActors = new ActorRef[workerAmount];

        /* Query Actor set. */
        querySummaryProviderActors = new ActorRef[workerAmount];
        queryFailedProviderActors = new ActorRef[workerAmount];
        queryConfirmedProviderActors = new ActorRef[workerAmount];

        queryRequesterRemoteActors = new ActorRef[workerAmount];
        queryRequesterLocalActors = new ActorRef[workerAmount];
        queryConfirmedRequesterActors = new ActorRef[workerAmount];
        queryFailedRequesterActors = new ActorRef[workerAmount];

        /* NRM monitoring message set. */
        nrmProvisionActors = new ActorRef[workerAmount];


        /* Start remote actors. */
        for (int i = 0; i < workerAmount; i++) {
            /* Reserve Actor set. */
            reserveProviderActors[i] = remote().actorFor(ReserveProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            reserveFailedProviderActors[i] = remote().actorFor(ReserveFailedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            reserveConfirmedProviderActors[i] = remote().actorFor(ReserveConfirmedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

            reserveRequesterRemoteActors[i] = remote().actorFor(ReserveRequesterRemoteActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            reserveRequesterLocalActors[i] = remote().actorFor(ReserveRequesterLocalActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            reserveConfirmedRequesterActors[i] = remote().actorFor(ReserveConfirmedRequesterActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            reserveFailedRequesterActors[i] = remote().actorFor(ReserveFailedRequesterActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

            /* Provision Actor set. */
            provisionProviderActors[i] = remote().actorFor(ProvisionProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            provisionFailedProviderActors[i] = remote().actorFor(ProvisionFailedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            provisionConfirmedProviderActors[i] = remote().actorFor(ProvisionConfirmedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

            provisionRequesterRemoteActors[i] = remote().actorFor(ProvisionRequesterRemoteActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            provisionRequesterLocalActors[i] = remote().actorFor(ProvisionRequesterLocalActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            provisionConfirmedRequesterActors[i] = remote().actorFor(ProvisionConfirmedRequesterActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            provisionFailedRequesterActors[i] = remote().actorFor(ProvisionFailedRequesterActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

            /* Terminate Actor set. */
            terminateProviderActors[i] = remote().actorFor(TerminateProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            terminateFailedProviderActors[i] = remote().actorFor(TerminateFailedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            terminateConfirmedProviderActors[i] = remote().actorFor(TerminateConfirmedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

            terminateRequesterRemoteActors[i] = remote().actorFor(TerminateRequesterRemoteActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            terminateRequesterLocalActors[i] = remote().actorFor(TerminateRequesterLocalActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            terminateConfirmedRequesterActors[i] = remote().actorFor(TerminateConfirmedRequesterActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            terminateFailedRequesterActors[i] = remote().actorFor(TerminateFailedRequesterActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

            /* Query Actor set. */
            querySummaryProviderActors[i] = remote().actorFor(QuerySummaryProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            queryFailedProviderActors[i] = remote().actorFor(QueryFailedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            queryConfirmedProviderActors[i] = remote().actorFor(QueryConfirmedProviderActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

            queryRequesterRemoteActors[i] = null;
            queryRequesterLocalActors[i] = remote().actorFor(QueryRequesterLocalActor.class.getName(), Registry.serverHost, Registry.serverPort).start();
            queryConfirmedRequesterActors[i] = null;
            queryFailedRequesterActors[i] = null;

            /* NRM monitoring message set. */
            nrmProvisionActors[i] = remote().actorFor(NrmProvisionActor.class.getName(), Registry.serverHost, Registry.serverPort).start();

        }
	}

    private ActorRef getActorForMessage(NsaMessage message) throws IllegalArgumentException {

        /**
         * We need to build a hash based on the connectionId so we serialize
         * processing of messages associated with a single connection.
         */
        HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(message.getConnectionId());
        int index = Math.abs(hash.toHashCode() % getWorkers());

        // We now grab a dedicated actor based on request type and hash value.
        switch (message.getMessageType()) {
            /* Reserve Actor set. */
            case reserveProvider:
                return reserveProviderActors[index];

            case reserveFailedProvider:
                return reserveFailedProviderActors[index];

            case reserveConfirmedProvider:
                return reserveConfirmedProviderActors[index];

            case reserveRequesterRemote:
                return reserveRequesterRemoteActors[index];

            case reserveRequesterLocal:
                return reserveRequesterLocalActors[index];

            case reserveConfirmedRequester:
                return reserveConfirmedRequesterActors[index];

            case reserveFailedRequester:
                return reserveFailedRequesterActors[index];

            /* Provision Actor set. */
            case provisionProvider:
                return provisionProviderActors[index];

            case provisionFailedProvider:
                return provisionFailedProviderActors[index];

            case provisionConfirmedProvider:
                return provisionConfirmedProviderActors[index];

            case provisionRequesterRemote:
                return provisionRequesterRemoteActors[index];

            case provisionRequesterLocal:
                return provisionRequesterLocalActors[index];

            case provisionConfirmedRequester:
                return provisionConfirmedRequesterActors[index];

            case provisionFailedRequester:
                return provisionFailedRequesterActors[index];

            /* Terminate Actor set. */
            case terminateProvider:
                return terminateProviderActors[index];

            case terminateFailedProvider:
                return terminateFailedProviderActors[index];

            case terminateConfirmedProvider:
                return terminateConfirmedProviderActors[index];

            case terminateRequesterRemote:
                return terminateRequesterRemoteActors[index];

            case terminateRequesterLocal:
                return terminateRequesterLocalActors[index];

            case terminateConfirmedRequester:
                return terminateConfirmedRequesterActors[index];

            case terminateFailedRequester:
                return terminateFailedRequesterActors[index];

            /* Query Actor set. */
            case querySummaryProvider:
                return querySummaryProviderActors[index];

            case queryFailedProvider:
                return queryFailedProviderActors[index];

            case queryConfirmedProvider:
                return queryConfirmedProviderActors[index];

            case queryRequesterRemote:
                return queryRequesterRemoteActors[index];

            case queryRequesterLocal:
                return queryRequesterLocalActors[index];

            case queryConfirmedRequester:
                return queryConfirmedRequesterActors[index];

            case queryFailedRequester:
                return queryFailedRequesterActors[index];
        }

        throw new IllegalArgumentException("No route available for message: " + message.getMessageType().name());
    }

    private ActorRef getNrmActorForMessage(NrmMessage message) throws IllegalArgumentException {

        /**
         * We need to build a hash based on the connectionId so we serialize
         * processing of messages associated with a single connection.
         */
        HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(message.getChildConnectionId());
        int index = Math.abs(hash.toHashCode() % getWorkers());

        // We now grab a dedicated actor based on request type and hash value.
        switch (message.getNrmMessageType()) {
            /* Reserve Actor set. */
            case provisionMonitor:
                return nrmProvisionActors[index];
        }

        throw new IllegalArgumentException("No route available for message: " + message.getNrmMessageType().name());
    }

	@Override
	public ActorRef route(Object message) {

        // Did we get the NsaMessage object?  That is all we know how to use.
		if (NsaMessage.class.isAssignableFrom(message.getClass())) {
            NsaMessage nsaMessage = (NsaMessage) message;
			log.info("Routing message type: " + nsaMessage.getMessageType().name());

            // Get the associated actor from the pool.
            return this.getActorForMessage(nsaMessage);
		}
        else if (NrmMessage.class.isAssignableFrom(message.getClass())) {
            NrmMessage nrmMessage = (NrmMessage) message;
			log.info("Routing message type: " + nrmMessage.getNrmMessageType().name());

            // Get the associated actor from the pool.
            return this.getNrmActorForMessage(nrmMessage);
		}
        else {
            // We received a message we do not understand.
			throw new IllegalArgumentException(getClass().getSimpleName()
			    + ": No route available for message: " + message);
		}
	}

    public void stop() {
        log.info("ActorLoadBalancer.stop()");
        if (reserveProviderActors != null) {
            for (int i = 0; i < workerAmount; i++) {
                reserveProviderActors[i].stop();
                reserveFailedProviderActors[i].stop();
                reserveConfirmedProviderActors[i].stop();
                reserveRequesterRemoteActors[i].stop();
                reserveRequesterLocalActors[i].stop();
                reserveConfirmedRequesterActors[i].stop();
                reserveFailedRequesterActors[i].stop();

                provisionProviderActors[i].stop();
                provisionFailedProviderActors[i].stop();
                provisionConfirmedProviderActors[i].stop();
                provisionRequesterRemoteActors[i].stop();
                provisionRequesterLocalActors[i].stop();
                provisionConfirmedRequesterActors[i].stop();
                provisionFailedRequesterActors[i].stop();

                terminateProviderActors[i].stop();
                terminateFailedProviderActors[i].stop();
                terminateConfirmedProviderActors[i].stop();
                terminateRequesterRemoteActors[i].stop();
                terminateRequesterLocalActors[i].stop();
                terminateConfirmedRequesterActors[i].stop();
                terminateFailedRequesterActors[i].stop();

                querySummaryProviderActors[i].stop();
                queryFailedProviderActors[i].stop();
                queryConfirmedProviderActors[i].stop();
                //queryRequesterRemoteActors[i].stop();
                queryRequesterLocalActors[i].stop();
                //queryConfirmedRequesterActors[i].stop();
                //queryFailedRequesterActors[i].stop();

                nrmProvisionActors[i].stop();
            }
        }
    }

    public void start() {
        log.info("ActorLoadBalancer.start()");
        if (reserveProviderActors != null) {
            for (int i = 0; i < workerAmount; i++) {
                reserveProviderActors[i].start();
                reserveFailedProviderActors[i].start();
                reserveConfirmedProviderActors[i].start();
                reserveRequesterRemoteActors[i].start();
                reserveRequesterLocalActors[i].start();
                reserveConfirmedRequesterActors[i].start();
                reserveFailedRequesterActors[i].start();

                provisionProviderActors[i].start();
                provisionFailedProviderActors[i].start();
                provisionConfirmedProviderActors[i].start();
                provisionRequesterRemoteActors[i].start();
                provisionRequesterLocalActors[i].start();
                provisionConfirmedRequesterActors[i].start();
                provisionFailedRequesterActors[i].start();

                terminateProviderActors[i].start();
                terminateFailedProviderActors[i].start();
                terminateConfirmedProviderActors[i].start();
                terminateRequesterRemoteActors[i].start();
                terminateRequesterLocalActors[i].start();
                terminateConfirmedRequesterActors[i].start();
                terminateFailedRequesterActors[i].start();

                querySummaryProviderActors[i].start();
                queryFailedProviderActors[i].start();
                queryConfirmedProviderActors[i].start();
                //queryRequesterRemoteActors[i].start();
                queryRequesterLocalActors[i].start();
                //queryConfirmedRequesterActors[i].start();
                //queryFailedRequesterActors[i].start();

                nrmProvisionActors[i].start();
            }
        }
    }

    /**
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * @return the workers
     */
    public int getWorkers() {
        return workerAmount;
    }

}