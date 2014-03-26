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

public final class SentMessageType extends AbstractMessageType {
	private static final long serialVersionUID = 3984875507672819471L;
	private final String bcc;

	public SentMessageType(int messageId, String userId, String sender,
	    String receiver, String cc, long dateSent, String subject, String text,
	    boolean deleted, String bccUser) {
		super(messageId, userId, sender, receiver, cc, dateSent, subject, text,
		    deleted);
		bcc = bccUser;
	}

	public SentMessageType(String sender, String receiver, String cc,
	    String subject, String text, String bccUser) {
		super(sender, receiver, cc, subject, text);
		bcc = bccUser;
	}

	public String getBcc() {
		return bcc;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SentMessageType [bcc=");
		builder.append(bcc);
		builder.append(", toString()=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

}
