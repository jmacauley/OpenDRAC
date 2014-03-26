package org.opendrac.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LauncherProcess {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String className;
  private final boolean isEnabled;
  private final boolean isForked;
  private String initCommand;
  private String destroyCommand;
  private Object process;

  private boolean isRunning = false;

  public LauncherProcess(String id, boolean isEnabled, String initCommand,
      String destroyCommand, final boolean isForked) {
    this.className = id;
    this.isEnabled = isEnabled;
    this.isForked = isForked;
    // Only do the rest if enabled, or we'll create a new instance of a disabled
    // class, which could lead to invoking lot's of constructor initialization
    // code
    if (isEnabled) {
      this.initCommand = initCommand;
      this.destroyCommand = destroyCommand;
      try {
        this.process = Class.forName(id).newInstance();
      }
      catch (Exception e) {
        log.error("Error: ", e);
      }
    }
  }

  public void init() {
    execute(className, initCommand);
    isRunning = true;
  }

  public void destroy() {
    execute(className, destroyCommand);
    isRunning = false;
  }

  public void execute(String className, String methodName) {
    log.debug(String.format("[+] %s %s's %s method", isForked ? "Forking"
        : "Executing", className, methodName));
    if (methodName != null && !methodName.isEmpty()) {
      try {
        process.getClass().getDeclaredMethod(methodName).invoke(process);
      }
      catch (Exception e) {
        log.error("Error: ", e);
      }
    }
  }

  public String getClassName() {
    return className;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public String getDestroyCommand() {
    return destroyCommand;
  }

  public boolean isForked() {
    return isForked;
  }

  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("LauncherProcess [id=");
    builder.append(className);
    builder.append(", isEnabled=");
    builder.append(isEnabled);
    builder.append(", initCommand=");
    builder.append(initCommand);
    builder.append(", destroyCommand=");
    builder.append(destroyCommand);
    builder.append(", processInstance=");
    builder.append(process);
    builder.append("]");
    return builder.toString();
  }

}
