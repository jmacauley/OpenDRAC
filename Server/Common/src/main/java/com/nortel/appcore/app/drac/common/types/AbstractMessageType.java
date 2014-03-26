/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;

public abstract class AbstractMessageType implements Serializable, Comparable {

	public static enum MessageBox {
		INBOX, OUTBOX
	}

	private static final long serialVersionUID = -8489697900039772920L;
	private final int messageId;
	private final String userId;
	private String sender;
	private final String receiver;
	private final String cc;
	private final long dateSent;
	private final String subject;
	private final String text;
	private final boolean deleted;

	AbstractMessageType(int theMessageId, String theUserId, String theSender,
	    String theReceiver, String ccUsers, long theDateSent, String theSubject,
	    String theText, boolean isDeleted) {
		super();
		messageId = theMessageId;
		userId = theUserId;
		sender = theSender;
		receiver = theReceiver;
		cc = ccUsers;
		dateSent = theDateSent;
		subject = theSubject;
		text = theText;
		deleted = isDeleted;
	}

	AbstractMessageType(String theSender, String theReceiver, String ccUsers,
	    String theSubject, String theText) {
		this(0, "", theSender, theReceiver, ccUsers, 0, theSubject, theText, false);
	}

	@Override
	public int compareTo(Object o) { // NO_UCD
		if (o instanceof AbstractMessageType) {
			AbstractMessageType m = (AbstractMessageType) o;
			if (this.getDateSent() == m.getDateSent()) {
				return 0;
			}
			else if (this.getDateSent() > m.getDateSent()) {
				return 1;
			}
			else if (this.getDateSent() < m.getDateSent()) {
				return -1;
			}
		}
		return 0;
	}

	public String getCc() {
		return cc;
	}

	public long getDateSent() {
		return dateSent;
	}

	public int getMessageId() {
		return messageId;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getSender() {
		return sender;
	}

	public String getSubject() {
		return subject;
	}

	public String getText() {
		return text;
	}

	public String getUserId() {
		return userId;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	// public boolean isDeleted()
	// {
	// return deleted;
	// }

	// public void setCc(String cc)
	// {
	// this.cc = cc;
	// }

	// public void setDateSent(long dateSent)
	// {
	// this.dateSent = dateSent;
	// }

	// public void setDeleted(boolean deleted)
	// {
	// this.deleted = deleted;
	// }

	// public void setMessageId(String messageId)
	// {
	// this.messageId = messageId;
	// }

	// public void setReceiver(String receiver)
	// {
	// this.receiver = receiver;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractMessageType [messageId=");
		builder.append(messageId);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", receiver=");
		builder.append(receiver);
		builder.append(", dateSent=");
		builder.append(dateSent);
		builder.append(", cc=");
		builder.append(cc);
		builder.append(", deleted=");
		builder.append(deleted);
		builder.append(", sender=");
		builder.append(sender);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", text=");
		builder.append(text);
		builder.append("]");
		return builder.toString();
	}

	// public void setText(String text)
	// {
	// this.text = text;
	// }

	// public void setUserId(String userId)
	// {
	// this.userId = userId;
	// }

}
