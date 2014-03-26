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

public final class ReceivedMessageType extends AbstractMessageType {
	private static final long serialVersionUID = 2724953741565830008L;
	private final boolean replied;
	private final boolean unread;

	public ReceivedMessageType(int messageId, String userId, String sender,
	    String receiver, String cc, long dateSent, String subject, String text,
	    boolean deleted, boolean isReplied, boolean isUnread) {
		super(messageId, userId, sender, receiver, cc, dateSent, subject, text,
		    deleted);
		replied = isReplied;
		unread = isUnread;
	}

	public boolean isReplied() {
		return replied;
	}

	public boolean isUnread() {
		return unread;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReceivedMessageType [replied=");
		builder.append(replied);
		builder.append(", unread=");
		builder.append(unread);
		builder.append(", toString()=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}
}
