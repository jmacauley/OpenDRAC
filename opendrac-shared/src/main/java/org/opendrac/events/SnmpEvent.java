package org.opendrac.events;

import java.util.Date;

public class SnmpEvent {

  private String address, id, name, status;
  private Date date;

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
    builder.append("SnmpEvent [address=");
    builder.append(address);
    builder.append(", id=");
    builder.append(id);
    builder.append(", name=");
    builder.append(name);
    builder.append(", status=");
    builder.append(status);
    builder.append(", date=");
    builder.append(date);
    builder.append("]");
    return builder.toString();
  }

}
