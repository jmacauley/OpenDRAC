/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.server.neproxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;

/**
 * Created on Aug 17, 2005
 * 
 * @author nguyentd
 */
public enum DiscoverNePool implements Runnable {
  INSTANCE;
  private final Logger log = LoggerFactory.getLogger(DiscoverNePool.class);
  private static final int MAX_WORKER = 10;
  private static final int[] RETRY_PERIOD = { 1000, 1000, 1000, 1000, 1000,
      2000, 2000, 2000, 2000, 2000, 5000, 5000, 5000, 5000, 5000 };
  private static final int LONGWAIT = 300000;
  private final Random randomObject = new Random(System.currentTimeMillis());
  private final Map<String, AbstractNe> neList = new ConcurrentHashMap<String, AbstractNe>();
  private final List<AbstractNe> messageQueue = new ArrayList<AbstractNe>();
  private boolean shouldTerminate;
  private boolean readyFlag;
  private DbUtility dbUtility;
  private final String terminalId = System.getProperty(
      "org.opendrac.proxy.terminal.id", "DEFAULT_PROXY");

  private DiscoverNePool() {
    dbUtility = DbUtility.INSTANCE;
    readNetworkElementsFromDatabase();
    final Thread discoverNePoolthread = new Thread(this);
    discoverNePoolthread.setDaemon(true);
    discoverNePoolthread.start();
  }

  public void addNe(String name, AbstractNe ne) throws Exception {
    if (dbUtility.addNewNe(ne, NeProxy.INSTANCE.getTid())) {
      neList.put(name, ne);
      if (ne.isAutoReDiscover()) {
        enqueueTask(ne);
      }
    }
  }

  public void enqueueTask(final AbstractNe ne) {
    if (ne.getTotalRetry() == 0) {
      ne.incTotalRetry();
      messageQueue.add(ne);
      wakeup();
    }
    else {
      int period = 1000;
      if (ne.getState() == NeStatus.NE_NOT_PROVISION) {
        return;
      }
      if (ne.getState() == ne.getPreviousNeState()) {
        if (ne.getTotalRetry() >= RETRY_PERIOD.length) {
          period = LONGWAIT;
        }
        else {
          period = randomObject.nextInt(RETRY_PERIOD[ne.getTotalRetry()]) + 1000;
          if (ne.getTotalRetry() < RETRY_PERIOD.length) {
            ne.incTotalRetry();
          }
        }
      }
      else {
        ne.setTotalRetry(0);
        messageQueue.add(ne);
        wakeup();
        return;
      }
      log.debug("Adding " + ne.getIpAddress() + " delay " + period + " with "
          + ne.getTotalRetry() + " retries");
      new Timer("DiscoverNePool Delay Task Thread: " + ne.getIpAddress(), false)
          .schedule(new TimerTask() {
            @Override
            public void run() {
              messageQueue.add(ne);
              wakeup();
            }
          }, period);
    }

  }

  /**
   * NE is referenced by ipAddress:port string
   */
  public AbstractNe getNe(String neName) {
    return neList.get(neName);
  }

  /**
   * Can't say we don't try. Look for the NE by TID(name), IEEE or ip:port
   * return null if not found
   */
  public AbstractNe getNeByTidOrIdOrIpandPort(String neName) {
    for (AbstractNe ne : neList.values()) {
      if (neName.equalsIgnoreCase(ne.getNeName())) {
        return ne;
      }
      else if (neName.equalsIgnoreCase(ne.getNeId())) {
        return ne;
      }
    }
    return neList.get(neName);
  }

  public Map<String, AbstractNe> getNeList() {
    return new HashMap<String, AbstractNe>(neList);
  }

  public boolean isReady() {
    return readyFlag;
  }

  public void removeNe(AbstractNe ne) {
    if (ne != null) {
      neList.remove(ne.getIpAddress() + ":" + ne.getPortNumber());
    }
  }

  @Override
  public void run() {
    log.debug("Starting DiscoverNePool");
    final Executor executor = Executors.newFixedThreadPool(MAX_WORKER,
        new ThreadFactory() {
          @Override
          public Thread newThread(Runnable runnable) {
            return new Thread(runnable);
          }
        });
    shouldTerminate = false;

    while (!shouldTerminate) {
      synchronized (messageQueue) {
        try {
          if (messageQueue.isEmpty()) {
            messageQueue.wait();
          }
          else {
            final AbstractNe ne = messageQueue.remove(0);
            if (ne != null) {
              executor.execute(new Runnable() {
                @Override
                public void run() {
                  log.debug("Changing " + ne.getIpAddress() + " state to "
                      + ne.getState());
                  try {
                    ne.nextState();
                  }
                  catch (Exception e) {
                    log.error("Failed to change NE state", e);
                    INSTANCE.enqueueTask(ne);
                  }
                }
              });
            }
          }
        }
        catch (Exception e) {
          log.error("Failed to process NE state", e);
        }
      }
    }
    log.debug("Terminating the NE Discovery");
    for (AbstractNe ne : neList.values()) {
      ne.terminate();
    }
    neList.clear();
  }

  public void terminateThread() {
    shouldTerminate = true;
    wakeup();
    while (!messageQueue.isEmpty()) {
      try {
        Thread.sleep(200L);
      }
      catch (InterruptedException e) {
        log.error("Error: ", e);
      }
    }
  }

  public void wakeup() {
    synchronized (messageQueue) {
      messageQueue.notifyAll();
    }
  }

  /**
   * This method is used to recover the NEs from the database at the
   * initialization time as well as during schedule audit cycle.
   * 
   * RH: Also used when changing ip/port of an existing ne to re-read the
   * topology
   * 
   */
  public void readNetworkElementsFromDatabase() {
    try {
      log.debug("Retrieving NE data from database...");
      if (dbUtility == null) {
        dbUtility = DbUtility.INSTANCE;
      }
      List<AbstractNetworkElement> neListToAdd = dbUtility
          .retrieveNetworkElement(terminalId);
      for (AbstractNetworkElement ane : neListToAdd) {
        AbstractNe ne = (AbstractNe) ane;
        String id = ne.getIpAddress() + ":" + ne.getPortNumber();
        // Just to make sure that it's not in the internal cache
        if (neList.containsKey(id)) {
          
        }
        else {
          neList.put(id, ne);
          dbUtility.upDateNe(ne);
          if (ne.isAutoReDiscover()) {
            this.enqueueTask(ne);
            
          }
          else {
            
          }
        }
      }
      readyFlag = true;
      log.debug("...done with " + neListToAdd.size() + " NE");
    }
    catch (Exception e) {
      log.error("Failed to get NE from Database", e);
    }
  }
}
