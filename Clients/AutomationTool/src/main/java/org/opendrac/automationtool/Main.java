package org.opendrac.automationtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Erno
 */
public class Main {

  private static PrintStream originalPrintStream = System.out;
  private static Level originalRootLogLevel = LogManager.getRootLogger().getLevel();
  private static final Level LOG_LEVEL_OFF = Level.toLevel("OFF");
  private static Map<Logger, Level> logeLevels = new HashMap<Logger, Level>();

  static {
    @SuppressWarnings("unchecked")
    Enumeration<Logger> loggers = LogManager.getCurrentLoggers();
    while (loggers.hasMoreElements()) {
      Logger logger = loggers.nextElement();
      logeLevels.put(logger, logger.getLevel());
    }
  }

  /**
   * @param args
   *          the command line arguments
   */
  public static void main(String[] args) throws Exception {

    if (args.length == 0) {
      System.out.println("\nNo arguments provided, exiting now!\nAdd -h for list of arguments\n");
      return;
    }

    CmdParser commandParser = new CmdParser();
    commandParser.parse(args);
    Main main = new Main();
    main.handleCommand(commandParser);
  }

  private void handleCommand(CmdParser commandParser) throws Exception {
    try {

      final Reservation reservation = new Reservation(commandParser.getConfigFile());

      if (commandParser.isRunNow()) {
        reservation.makeReservationNow();
      }

      else if (commandParser.isMakeSched()) {
        reservation.makeReservation();
      }

      else if (commandParser.getSchedIdStatus() != null) {
        reservation.scheduleStatus(commandParser.getSchedIdStatus());
      }

      else if (commandParser.getSchedIdCancel() != null) {
        reservation.cancelSchedule(commandParser.getSchedIdCancel());
      }

      else if (commandParser.getListScheds() != null) {
        try {
          if ((commandParser.getListvals()[0]).equals("propsfile")) {
            reservation.listSchedules();
          }
          else {
            reservation.listSchedules(commandParser.getListvals()[0], commandParser.getListvals()[1]);
          }
        }
        catch (Exception e) {
          System.out.println("Error: Check Time format and select 1 startTime and 1 endTime\n");
          return;
        }
      }

      else if (commandParser.isResumeService()) {
        reservation.resumeCurrentService();
      }

      else if (commandParser.isTerminateService()) {
        reservation.terminateCurrentSchedule();
      }

      else if (commandParser.isPathInfo()) {
        reservation.showStatusInfoCurrentService();
      }
      else if (commandParser.isExtendService()) {
        reservation.extendCurrentServiceForSchedule(commandParser.getExtensionMinutes());
      }
      else if (commandParser.isQueryPathAvailability()) {
        reservation.queryPathAvailability();
      }
    }
    catch (RemoteException re) {
      if (re instanceof RemoteException
          && re.getLocalizedMessage().indexOf("sun.security.validator.ValidatorException") >= 0) {
        if (getMustLoadNewCertificate()) {
          CertificateLoader loader = new CertificateLoader();
          loader.updateKeyStore(commandParser.getConfigFile());
          System.out.println("\nResume executing original task...");
          handleCommand(commandParser);
        }
        else {
          System.out.println("Program will exit now.");
        }
      }
      else {
        throw re;
      }
    }
  }

  private boolean getMustLoadNewCertificate() throws IOException {
    restoreSTDOut();
    System.out.println("You do not have the right certificate in  your keystore.");
    System.out.println("This tool can automatically retrieve it.");
    System.out.println("If you want to retrieve it automatically type \"yes\".");
    System.out.println("If you want to do this by hand after ending this program type \"no\".");
    System.out.print("Retrieve it? ");
    return getAnswerForCertificateRetrieval().equals("yes");
  }

  private String getAnswerForCertificateRetrieval() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String answer = reader.readLine().trim().toLowerCase();
    if (!(answer.equals("yes") || answer.equals("no"))) {
      System.out.print("Answer with \"yes\" or \"no\" ");
      answer = getAnswerForCertificateRetrieval();
    }
    return answer;
  }

  protected static PrintStream disableSTDOut() {
    LogManager.getRootLogger().setLevel(LOG_LEVEL_OFF);
    Set<Logger> loggers = logeLevels.keySet();
    for (Logger logger : loggers) {
      logger.setLevel(LOG_LEVEL_OFF);
    }
    System.setOut(new PrintStream(new OutputStream() {
      public void write(int b) {
      }
    }));
    return originalPrintStream;
  }

  protected static void restoreSTDOut() {
    LogManager.getRootLogger().setLevel(originalRootLogLevel);
    Set<Logger> loggers = logeLevels.keySet();
    for (Logger logger : loggers) {
      logger.setLevel(logeLevels.get(logger));
    }
    System.setOut(originalPrintStream);
  }
}
