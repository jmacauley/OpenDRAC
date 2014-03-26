package org.opendrac.launcher;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public final class RmiLauncher {

  private final int port = Integer.getInteger("org.opendrac.rmi.port", 1099);

  private Registry registry;

  public void start() throws RemoteException {
    registry = LocateRegistry.createRegistry(port);
  }

  public void stop() throws RemoteException {
    if (registry == null) {
      return;
    }
    UnicastRemoteObject.unexportObject(registry, true);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RmiLauncher [port=");
    builder.append(port);
    builder.append(", registry=");
    builder.append(registry);
    builder.append("]");
    return builder.toString();
  }

}
