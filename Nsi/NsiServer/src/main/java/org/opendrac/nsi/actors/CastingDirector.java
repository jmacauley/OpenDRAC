/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.actors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import static akka.actor.Actors.actorOf;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author hacksaw
 */
@Component("castingDirector")
public class CastingDirector {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("#{nsiProperties.actorPoolSize}")
    private int actorPoolSize;

    private Registry registry = null;
    private ActorRef actorLoadBalancer = null;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        log.info("ActorPoolSize: " + actorPoolSize);
        registry = new Registry();
        registry.startRemoteServiceServer();
        registry.registerAll();

        actorLoadBalancer = actorOf(new UntypedActorFactory() {
            @Override
            public UntypedActor create() {
                return new ActorLoadBalancer(registry, actorPoolSize);
            }
        }).start();

    }

    public static CastingDirector getInstance() {
        CastingDirector castingDirector = SpringApplicationContext.getBean("castingDirector", CastingDirector.class);
        return castingDirector;
    }

    @PreDestroy
    public void stop() throws Exception {
    		registry.stopRemoteServiceServer();
        actorLoadBalancer.stop();
	}

    public void start() throws Exception {
        registry.startRemoteServiceServer();
        registry.registerAll();
        actorLoadBalancer.start();
	}

    public void send(Object message) {
        actorLoadBalancer.sendOneWay(message);
    }

}
