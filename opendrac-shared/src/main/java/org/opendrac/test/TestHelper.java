package org.opendrac.test;

import java.rmi.RemoteException;

import org.opendrac.ioc.IocContainer;
import org.opendrac.launcher.RmiLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TestHelper {
  INSTANCE;
  private boolean initialized;
  private Logger log = LoggerFactory.getLogger(getClass());
  private final RmiLauncher rmiLauncher = new RmiLauncher();

  public void initialize() {
    if (initialized) {
      return;
    }
    System.setProperty("org.opendrac.rmi.port",
        System.getProperty("org.opendrac.rmi.port", "1099"));
    
    System.setProperty("org.opendrac.db.max.resultset",
        Integer.getInteger("org.opendrac.db.max.resultset", 100).toString());
    
    try {
      rmiLauncher.start();
    }
    catch (RemoteException e) {
      log.error("Error: ", e);
    }
    IocContainer.setConfigs("/spring/opendrac-database.xml",
        "/spring/opendrac-common.xml");
    IocContainer.start();
    initialized = true;
  }

  public void destroy() {
    IocContainer.close();
    try {
      rmiLauncher.stop();
    }
    catch (RemoteException e) {
      log.error("Error: ", e);
    }
  }

}
