package org.opendrac.events;

import java.util.Date;

public class MailEvent {

  public enum EventType {
    NE_ERROR;
  }

  private String address, id, name, status;
  private Date date;
  private EventType eventType;

  public String getName() {
    return name;
  }

  public void setName(String tid) {
    this.name = tid;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String pk) {
    this.address = pk;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MailEvent [pk=");
    builder.append(address);
    builder.append(", id=");
    builder.append(id);
    builder.append(", tid=");
    builder.append(name);
    builder.append(", status=");
    builder.append(status);
    builder.append(", date=");
    builder.append(date);
    builder.append(", eventType=");
    builder.append(eventType);
    builder.append("]");
    return builder.toString();
  }

}
