package com.nortel.appcore.app.drac.server.lpcp;

public final class LpcpLauncher {

  private Lpcp lpcp;

  public void startup() {
    lpcp = new Lpcp(true);
    lpcp.start();
  }

  public void shutdown() {
    if (lpcp != null) {
      lpcp.stop();
    }
  }

}
