/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.opendrac.nsi.util;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.opendrac.nsi.actors.CastingDirector;
import org.opendrac.nsi.actors.messages.NrmMessage;
import org.opendrac.nsi.domain.PendingOperationManager;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.domain.StateMachineManager;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.pathfinding.PathSegment.NsaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * StartTimeScheduler
 * This class manages and triggers startTime related scheduled events within
 * the system based on the NSI state machine.  Actors are used to process work
 * triggered by the scheduler, so this class can focus on management of timers
 * and work distribution.
 *
 * TODO: This is not scalable at the moment.  we need to take advantage of
 * more efficient database queries.
 *
 * @author hacksaw
 */
@Component("startTimeScheduler")
public class StartTimeScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("stateMachineManager")
    private StateMachineManager stateMachineManager;

    @Autowired
    @Qualifier("pendingOperationManager")
    private PendingOperationManager pendingOperationManager;

    @Autowired
    @Qualifier("castingDirector")
    private CastingDirector castingDirector;

    Timer timer;
    GregorianCalendar nextTime;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
       runAudit();
    }

    public static StartTimeScheduler getInstance() {
        StartTimeScheduler startTimeScheduler = SpringApplicationContext.getBean("startTimeScheduler", StartTimeScheduler.class);
        return startTimeScheduler;
    }

    public void cancelTimer() {
        timer.cancel();
    }

    public Timer getTimer() {
        return timer;
    }

    public void timerAudit(GregorianCalendar eventTime) {
        // TODO: optimize this to only update timer.
        if (nextTime == null || (eventTime != null && eventTime.before(nextTime))) {
            runAudit();
        }
    }

    public void timerAudit() {
        runAudit();
    }

    private synchronized GregorianCalendar auditStartTime() {
        /*
         * Look through each start time and determine when the next NRM event
         * should be kicked off.
         *
         * TODO: Optimize this to use the state machine and path segment
         * database.
         */
        Map<String, StateMachine> stateMachines = stateMachineManager.getStateMachines();
        Set<Entry<String, StateMachine>> list = stateMachines.entrySet();
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar next = null;
        StateMachine machine = null;
        GregorianCalendar startTime = null;
        GregorianCalendar endTime = null;

        for (Entry<String, StateMachine> entry : list) {

            // Machine we want to check.
            machine = entry.getValue();

            // We are only interested in the schedule startTime.
            startTime = machine.getStartTime();
            endTime = machine.getEndTime();

            /*
             * Do endtime first so we don't accidentially start something that
             * is already over.  This will be more for restart recovery than
             * during normal operation.
             *
             * If this state machine endTime is in the past then determine
             * if we need to take any of the following timer related actions:
             *
             * ANY_STATE -> TERMINATED
             *
             * We will handle schedule endTime events in a different timer.
             */
            if (endTime.before(now)) {
                if (machine.getCurrentState() != ConnectionStateType.TERMINATED) {
                    // Time to terminate the state machine and all associated path segments.
                    logger.info("auditStartTime: transitioning connectionId=" + entry.getKey() + " from " + machine.getCurrentState() + " to TERMINATED");
                    machine.transitionState(ConnectionStateType.TERMINATED);
                }
            }
            else {
                /*
                 * Keep track of the sortest time to the next event so that
                 * we can set this as our next timer event.
                 */
                if (next == null) {
                    next = endTime;
                }
                else if (next.after(endTime)) {
                    next = endTime;
                }
            }

            /*
             * If this state machine startTime is in the past then determine
             * if we need to take any of the following timer related actions:
             *
             * RESERVED -> SCHEDULED
             * AUTO_PROVISION -> PROVISIONING
             *
             * We will handle schedule endTime events in a different timer.
             */
            if (startTime.before(now)) {
                if (machine.getCurrentState() == ConnectionStateType.RESERVED) {
                    // We only set the state and wait for a provision message.
                    logger.info("auditStartTime: transitioning connectionId=" + entry.getKey() + " from RESERVED to SCHEDULED");
                    machine.transitionState(ConnectionStateType.RESERVED, ConnectionStateType.SCHEDULED);
                }
                else if (machine.getCurrentState() == ConnectionStateType.AUTO_PROVISION) {
                    logger.info("auditStartTime: transitioning connectionId=" + entry.getKey() + " from AUTO_PROVISION to PROVISIONING");

                    /*
                     * We already have received the provision message so now
                     * transition to the PROVISIONING state and wait for the
                     * NRM to return a provision complete.  This will transition
                     * all the child path segments as well.
                     */
                    machine.transitionState(ConnectionStateType.AUTO_PROVISION, ConnectionStateType.PROVISIONING);
                }

                if (machine.getCurrentState() == ConnectionStateType.PROVISIONING) {
                    /*
                     * Fire off a listener to wait until the local NRM has
                     * responsed with a successful provisioning operation.  All
                     * other NSA will handle sending us a provisionConfirmed
                     * message.
                     */
                    if (machine.getRoutePathList() != null) {
                        for (PathSegment segment : machine.getRoutePathList()) {
                            if (segment.getNsaType() == NsaType.LOCAL) {
                                // We have a local NRM we must provision for thi ssegment.
                                logger.info("auditStartTime: initiating local NRM provision monitoring for parentConnectionId=" + segment.getParentConnectionId() + ", childConnectionId=" + segment.getChildConnectionId() + ", nrmScheduleId=" + segment.getData());

                                NrmMessage nrmMessage = new NrmMessage();
                                nrmMessage.setNrmMessageType(NrmMessage.NrmMessageType.provisionMonitor);
                                nrmMessage.setParentConnectionId(segment.getParentConnectionId());
                                nrmMessage.setChildConnectionId(segment.getChildConnectionId());
                                nrmMessage.setNrmScheduleId(segment.getData());

                                // Now we need to route this message to the appropriate actor.
                                castingDirector.send(nrmMessage);
                            }
                        }
                    }
                }
            }
            else {
                /*
                 * Keep track of the sortest time to the next event so that
                 * we can set this as our next timer event.
                 */
                if (next == null) {
                    next = startTime;
                }
                else if (next.after(startTime)) {
                    next = startTime;
                }
            }
        }
        return next;
    }

    private void runAudit() {
        if (timer != null) {
            timer.cancel();
        }

        nextTime = auditStartTime();

        if (nextTime != null) {
            timer = new Timer();
            timer.schedule(new SchedulerTask(), nextTime.getTime());
        }
    }

    class SchedulerTask extends TimerTask {

        @Override
        public void run() {
            runAudit();
        }
    }

}
