package org.opendrac.monitoring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.icegreen.greenmail.util.GreenMail;

/**
 * This class is here so OpenDRAC can startup during development without bailing
 * out if there is no SMTP server available.
 * 
 * @author robert
 * 
 */
@Service("mockMailServer")
public class MockMailServer {

  private final GreenMail greenMail = new GreenMail();
  
  @PostConstruct
  private void init(){
    greenMail.start();
  }

  @PreDestroy
  private void destroy() {
    greenMail.stop();
  }

}
