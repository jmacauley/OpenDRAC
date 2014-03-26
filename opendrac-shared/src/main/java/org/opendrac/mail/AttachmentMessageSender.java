package org.opendrac.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;

public class AttachmentMessageSender extends AbstactMessageSender {

  public void sendMessage(List<String> addressessTo, List<String> addressessCC,
      List<String> addressessBCC, String from, String subject,
      String messageBody, List<File> files, boolean isHtml)
      throws MessagingException {
		
		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		for (String addressee : addressessTo) {
			helper.addTo(addressee);
		}
		for (String addressee : addressessCC) {
			helper.addCc(addressee);
		}
		for (String addressee : addressessBCC) {
			helper.addBcc(addressee);
		}
		helper.setFrom(from);
		helper.setSubject(subject);
		helper.setText(messageBody, isHtml);

		for (File file : files) {
			FileSystemResource fileSystemResource = new FileSystemResource(file);
			helper.addAttachment(file.getName(), fileSystemResource);
		}
		sender.send(msg);
	}
	
  public void sendMessage(List<String> addressessTo, String from,
      String subject, String messageBody, List<File> files)
      throws MessagingException {
    sendMessage(addressessTo, new ArrayList<String>(), new ArrayList<String>(),
        from, subject, messageBody, files, false);
	}
}
