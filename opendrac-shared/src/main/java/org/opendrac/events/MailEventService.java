package org.opendrac.events;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service("mailEventService")
class MailEventService implements EventService<MailEvent> {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource(name = "mailSender")
  private MailSender mailSender;

  @Resource(name = "templateMessage")
  private SimpleMailMessage templateMessage;

  public void sendAlarm(final MailEvent... events) {
    if (events == null || events.length == 0) {
      return;
    }
    else {
      final StringBuilder message = new StringBuilder();
      for (final MailEvent mailEvent : events) {
        log.debug("Sending alarm for {} with status {}", mailEvent.getAddress(), mailEvent.getStatus());
        message.append("Address: ");
        message.append(mailEvent.getAddress()).append("\n");
        message.append("Status: ").append(mailEvent.getStatus()).append("\n");
        message.append("\n");
      }
      templateMessage.setText(message.toString());
      mailSender.send(templateMessage);
    }
  }

}
