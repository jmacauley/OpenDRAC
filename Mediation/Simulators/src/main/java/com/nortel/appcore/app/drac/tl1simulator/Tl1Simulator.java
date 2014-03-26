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

package com.nortel.appcore.app.drac.tl1simulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.utility.StringParser;

/**
 * We open a socket accept commands and occasionally respond to them. The NE
 * simulator is not a complete or complex TL1 simulator, it is quick and dirty
 * framework that works well most of the time. The simulator generates mostly
 * valid TL1 (but does take short cuts). The simulator responds to queries with
 * canned responses stored in local files that are located via the classpath
 * search mechanism in Java. If no file exists, the simulator responds with an
 * empty "ok" response. Multiple NEs can be simulated at once and each NE can
 * either share or use separate canned response files.
 *
 * @author pitman
 */
public final class Tl1Simulator {

  private static final Logger log = LoggerFactory.getLogger(Tl1Simulator.class);

  /**
   * An AcceptorThread is a thread that manages a single simulated NE on a
   * single port number. It opens a socket and listens for requests and passes
   * them off to a Tl1ClientThread.
   */
  static class AcceptorThread extends Thread {
    private volatile boolean stop;
    private final int port;
    private ServerSocket s;
    // needed to get rid of our 'every now and then'
    // ConcurrentModificationException
    private final LinkedBlockingDeque<Tl1ClientThread> clients = new LinkedBlockingDeque<Tl1ClientThread>();
    private final String neType;
    private int actualPort = -1;

    /**
     * Create a simulator thread running on the given port of the given NE type.
     * If port is zero a random free port will be chosen and getAcutalPort() can
     * be used to determine the real port number.
     */
    public AcceptorThread(int serverPort, String type) {
      port = serverPort;
      neType = type;
      setName("Tl1 Ne simulator on: " + port + " of type: " + type);
    }

    /**
     * Return the port number this AcceptorThread is bound to or -1 if not bound
     * to anything. Useful when the initial port number passed in is zero and
     * the OS is permitted to choose a port number to use.
     */
    public int getActualPort() {
      return actualPort;
    }

    @Override
    public void run() {
      try {
        log.info("Starting, binding on port " + port);

        s = new ServerSocket(port);
        actualPort = port;
        if (port == 0) {
          actualPort = s.getLocalPort();
        }
        try {
          while (!stop) {
            if (!s.isClosed()) {
              Tl1ClientThread sim = new Tl1ClientThread(s.accept(), neType);
              clients.add(sim);
              sim.setDaemon(true);
              sim.start();
            }
          }
        }
        finally {
          s.close();
        }
      }
      catch (SocketException e) {
        if (e.toString().contains("Socket closed")) {
          log.warn("Expected \"Socket closed\" exception");
        }
        else {
          log.error("Error: ", e);
        }
      }
      catch (Exception e) {
        log.error("Error: ", e);
      }
      // just in case.
      terminate();
    }

    /**
     * Stop the simulator on this socket from running
     */
    public synchronized void terminate() {
      stop = true;

      try {
        if (s != null) {
          s.close();
        }
      }
      catch (IOException e) {

      }

      this.interrupt();

      for (Tl1ClientThread sim : clients) {
        sim.terminate();
      }
      log.info("Shutting down");
    }
  }

  private final List<AcceptorThread> accept = new ArrayList<AcceptorThread>();

  /**
   * Default Simulator configuration. We simulate a single NE on a fixed port as
   * its faster.
   */
  protected static final String DEFAULT_SETUP = "OME7#OMED2196:10001";

  /**
   * Simulate a network on random port numbers (later we ask the simulator for
   * the actual port numbers) - 3 node connected OME network - 6 node connected
   * CPL network - 1 longTid node (soon to be an HDX?) - the first and last OME
   * NEs generating AO events.
   * <p>
   * Note we cannot simulate a HDXC here, as much as I'd like to. We talk TL1
   * and HTTP to a HDXC and would need to simulate the HTTP communications so
   * the we can obtain the IEEE address via HTTP get. Yeck....
   * HDXC#TDM3-AMSTERDAM1:0
   */

  public static final String OME6_CPL3_LONGTID_OME7L2_NETWORK_RANDOM_PORTS = "!OME6#OME0039:0, OME6#OME0237:0, OME6#OME0307:0, OME6#LONGTID8901234567890:0, CPL3#ASD001A-CPL01:0, CPL3#ASD001A-CPL2P:0, CPL3#ASD001A-CPL14:0, CPL3#ASD002A-CPL02:0, CPL3#ASD002A-CPL2P:0, CPL3#ASD003A-CPL01:0, OME7#OME0171-SONMP:0";
  public static final String OME6_NOAO_NETWORK_RANDOM_PORTS = "OME6#OME0039:0, OME6#OME0237:0, OME6#OME0307:0";

  /**
   * Simulate 3 NEs on fixed port numbers with the first NE generating AO
   * events. May not work if these ports are already allocated!
   */
  public static final String THREE_NODE_NETWORK_FIXED_PORTS = "!OME6#OME0039:10001,OME6#OME0237:10002,OME6#OME0307:10003";
  public static final String SIX_NODE_NETWORK_FIXED_PORTS = "!OME6#OME0039:10001,OME6#OME0237:10002,OME6#OME0307:10003,!OME6#Asd001A_OME1T:10004,!OME8#Asd001A_OME3T:10005,!OME6#Asd001A_OME4T:10006";
  public static final String SEVEN_NODE_NETWORK_FIXED_PORTS = "!OME6#OME0039:10001,OME6#OME0237:10002,OME6#OME0307:10003,!OME6#Asd001A_OME1T:10004,!OME9#Asd001A_OME3T:10005,!OME6#Asd001A_OME4T:10006,!OME8#OME001:10007";

    public static final String LAB_PORTS = "!Lab#Asd001A_OME1T:10001,Lab#Asd001A_OME3T:10002,Lab#Asd001A_OME4T:10003";

  /*
   * The SURFnet production network as of January 1 2013.
   */
  public static final String PRODUCTION_NETWORK_FIXED_PORTS =
          "!Production#Gn001A_OME01:10001," +
          "!Production#Ut001A_OME01:10002," +
          "!Production#Dgl001A_OME01:10003," +
          "!Production#Asd001A_OME4T:10004," +
          "!Production#Asd001A_OME1T:10005," +
          "!Production#Asd001A_OME3T:10006," +
          "!Production#Asd003A_OME01:10007," +
          "!Production#Ut002A_OME01:10008," +
          "!Production#Ut001A_OME02:10009," +
          "!Production#Ut015A_OME01:10010," +
          "!Production#Asd001A_OME12:10011," +
          "!Production#Asd001A_OME11:10012," +
          "!Production#Asd002A_OME11:10013," +
          "!Production#Asd002A_OME12:10014," +
          "!Production#Asd006A_OME01:10015," +
          "!Production#Asd001A_OME09:10016," +
          "!Production#Asd009A_OME01:10017," +
          "!Production#Asd001A_OME13:10018," +
          "!Production#Dt001B_OME01:10019," +
          "!Production#Nm001A_OME03:10020," +
          "!Production#Hrl001A_OME01:10021," +
          "!Production#Hrl002A_OME01:10022," +
          "!Production#Ledn001A_OME01:10023," +
          "!Production#Mt001A_OME01:10024," +
          "!Production#Wg001A_OME01:10025," +
          "!Production#Ehv001A_OME03:10026," +
          "!Production#Flightcase:10027," +
          "!Production#Ehv017A_OME01:10028," +
          "!Production#Ehv017B_OME01:10029";

  /**
   * Create a TL1 simulator using the default NE(s) and port numbers.
   */
  public Tl1Simulator() {
    this(DEFAULT_SETUP);
  }

  /**
   * Create a TL1 simulator using the provided configuration string. The
   * configuration string looks something like "TYPE#TID:PORT" where TYPE and
   * TID are actually directory names under which TL1 response files exist for
   * the simulator to use to respond to queries. Port number (can be zero) is
   * the port to bind the simulator to. Multiple simulated NEs can be created by
   * passing a list of TYPE#TID:PORT separated by a comma. For example
   * "OME6#OME0039:10001,OME6#OME0237:10002,OME6#OME0307:10003" simulates 3 NEs
   * on ports 10001, 10002 and 10003.
   * <p>
   * If one or more "!" are present in the TYPE_TID string the simulated NE will
   * generate AO events when running. The more the "!" the faster they will be
   * generated. The simulator will look for "AO-1.tl1", "AO-2.tl1", etc. until
   * no more are found and use the contents of these files when generating
   * events, the first event coming from the first file, etc and wrapping around
   * when no more files exist. The "!"'s will be stripped from string before
   * extracting the TYPE and TID fields.
   * <p>
   * This simulator is not super efficient, it uses lots of threads (2 per NE, 3
   * if generating events) and resources, and is not recommended for large
   * simulations.
   * </p>
   *
   * @param input
   */
  public Tl1Simulator(String input) {
    try {
      List<String> toks = StringParser.split(input, ',', false);

      for (String t : toks) {
        // expect a string that looks like type:port
        List<String> parsed = StringParser.split(t.trim(), ':', false);
        accept.add(new AcceptorThread(Integer.parseInt(parsed.get(1)), parsed
            .get(0).trim()));
      }
    }
    catch (RuntimeException re) {
      throw new RuntimeException(
          "Error creating simulator with configuration '" + input + "'", re);
    }
  }

  /**
   * Run the TL1 simulator using either a default configuration or a
   * configuration supplied by the user.
   */
  public static void main(String[] args) {
    try {
      Tl1Simulator cs = null;

      if (args == null || args.length == 0) {
        log.info("Starting TL1 simulator with 7 node fixed port setup '"
            + SEVEN_NODE_NETWORK_FIXED_PORTS + "'");
        cs = new Tl1Simulator(SEVEN_NODE_NETWORK_FIXED_PORTS);
      }
      else if (args.length == 1 && "production".equalsIgnoreCase(args[0])) {
        log.info("Starting TL1 simulator for production network.");
        cs = new Tl1Simulator(PRODUCTION_NETWORK_FIXED_PORTS);
      }
      else if (args.length == 1 && "lab".equalsIgnoreCase(args[0])) {
        log.info("Starting TL1 simulator for lab network.");
        cs = new Tl1Simulator(LAB_PORTS);
      }
      else {
        log.info("Starting TL1 simulator with user supplied argument '"
            + args[0] + "'");
        cs = new Tl1Simulator(args[0]);
      }

      cs.startSimulator();

      /* Simulator is up and running, wait for ever or until terminated */
      Object monitor = new Object();
      synchronized (monitor) {
        // wait for ever.
        monitor.wait();
      }

    }
    catch (Exception t) {
      log.error("Error running simulator", t);
    }

    log.info("done");
  }

  /**
   * Can only be called after the simulator has been started, returns the actual
   * port numbers the simulator is running on.
   */
  public List<Integer> getActualPortNumbers() {
    List<Integer> result = new ArrayList<Integer>(accept.size());

    int errorCount = 1;
    int maxErrorCount = 5;

    while (true) {
      result.clear();
      boolean good = true;
      for (AcceptorThread a : accept) {
        int p = a.getActualPort();
        if (p == -1) {
          // The thread is not running and set up yet, need to spin some more
          good = false;
          break;
        }
        result.add(Integer.valueOf(p));
      }
      if (good) {
        return result;
      }
      try {
        Thread.sleep(500);
      }
      catch (InterruptedException e) {
        log.error("Error: ", e);
      }
      log.error("AceptorThreads are not up will retry "
          + (maxErrorCount - errorCount) + ".");
      errorCount++;
      if (errorCount >= maxErrorCount) {
        throw new RuntimeException("Unable to start AceptorThreads after "
            + maxErrorCount + " tries, bailing out");
      }
    }
  }

  /**
   * Starts the simulator running
   *
   * @throws Exception
   */
  public void startSimulator() throws Exception {
    for (AcceptorThread a : accept) {
      a.setDaemon(true);
      a.start();
    }
    /*
     * Give it a chance to start running, our junit test cases want to connect
     * right away
     */
    Thread.sleep(500);
  }

  /**
   * Stops the simulator
   *
   * @throws Exception
   */
  public void stopSimulator() throws Exception {
    for (AcceptorThread a : accept) {
      a.terminate();
    }
    Thread.sleep(500L);
  }
}
