package org.opendrac.monitoring;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.opendrac.events.EventService;
import org.opendrac.events.MailEvent;
import org.opendrac.events.MailEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component("networkElementMonitor")
public class NetworkElementMonitor {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource(name = "mailEventService")
  private EventService<MailEvent> mailEventService;

  @Resource(name = "dataSource")
  private DataSource dataSource;

  @Resource(name = "neExcludesByPk")
  private List<String> neExcludesByPk;

  private JdbcTemplate jdbcTemplate;

  private static final String selectQuery = "select * from NetworkElement where status not like 'aligned%'";
  
  private boolean isFirstRun = true;

  @PostConstruct
  private void init() {
    jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public void checkNetworkElements() {
    
    // Added so NOC will not receive alarms the moment opendrac starts and
    // NE's are not aligned yet
    if (isFirstRun) {
      isFirstRun = false;
      return;
    }

    final List<NetworkElement> networkElements = jdbcTemplate.query(selectQuery, new RowMapper<NetworkElement>() {
      public NetworkElement mapRow(ResultSet rs, int rowNum) throws SQLException {
        NetworkElement networkElement = new NetworkElement();
        networkElement.setPk(rs.getString("pk"));
        networkElement.setId(rs.getString("id"));
        networkElement.setTid(rs.getString("tid"));
        networkElement.setStatus(rs.getString("status"));
        return networkElement;
      }
    });

    final List<MailEvent> mailEvents = new ArrayList<MailEvent>();
    for (final NetworkElement ne : networkElements) {
      if (neExcludesByPk.contains(ne.getPk())) {
        log.debug("Excluding: " + ne.getPk());
      }
      else {
        final MailEvent mailEvent = new MailEvent();
        mailEvent.setDate(new Date());
        mailEvent.setEventType(EventType.NE_ERROR);
        mailEvent.setId(ne.getId());
        mailEvent.setName(ne.getTid());
        mailEvent.setStatus(ne.getStatus());
        mailEvent.setAddress(ne.getPk());
        mailEvents.add(mailEvent);
      }
    }
    try {
      mailEventService.sendAlarm(mailEvents.toArray(new MailEvent[] {}));
    }
    catch (IOException e) {
      log.error("Error: unable to send event!", e);
    }
  }

  private static class NetworkElement {

    private String pk, id, tid, status;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getTid() {
      return tid;
    }

    public void setTid(String tid) {
      this.tid = tid;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public String getPk() {
      return pk;
    }

    public void setPk(String pk) {
      this.pk = pk;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("NetworkElement [pk=");
      builder.append(pk);
      builder.append(", id=");
      builder.append(id);
      builder.append(", tid=");
      builder.append(tid);
      builder.append(", status=");
      builder.append(status);
      builder.append("]");
      return builder.toString();
    }

  }

}
