package org.opendrac.mail;

import org.springframework.mail.javamail.JavaMailSender;

public abstract class AbstactMessageSender {

	protected JavaMailSender sender;

	public void setSender(JavaMailSender sender) {
		this.sender = sender;
	}
}