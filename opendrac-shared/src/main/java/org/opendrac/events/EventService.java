package org.opendrac.events;

import java.io.IOException;

public interface EventService<T> {

  void sendAlarm(T... events) throws IOException;
}
