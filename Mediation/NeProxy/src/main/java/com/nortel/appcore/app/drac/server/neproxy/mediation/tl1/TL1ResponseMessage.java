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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;

public final class TL1ResponseMessage implements Serializable {
	public enum COMPLETION_CODE {
		SUCCESS, TIME_OUT, RETRY_LATER, PROCESSING_ERROR, REQUEST_ERROR, IN_PROGRESS;
	}

	private static final long serialVersionUID = -8490651246312313028L;
	// public static final int SUCCESS = 1;
	// public static final int TIME_OUT = 2;
	// public static final int RETRY_LATER = 3;
	// public static final int PROCESSING_ERROR = 4;
	// public static final int REQUEST_ERROR = 5;
	// public static final int IN_PROGRESS = 6;
	public static final String UNKNOWN = "Unknown";
	private transient COMPLETION_CODE internalCompletionCode;
	private Tl1CommandCode commandCode;
	private String tid;
	private String correlationTag;
	private String completionCode;
	private String errorCode;
	private List<Map<String, String>> payload;
	private transient String[] textBlocks;
	private boolean Embedded;
	private transient List<TL1AlarmEvent> allMessages;
	private Date date;
	private NetworkElementInfo neInfo;
	// private
	// com.nortel.appcore.app.drac.server.neproxy.mediation.controller.TL1LineParser
	// tl1Parser;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
	    "yy-MM-dd HH:mm:ss");

	public TL1ResponseMessage(String id, Tl1CommandCode code) {
		commandCode = code;
		tid = id;
	}

	public List<TL1AlarmEvent> getAllMessages() {
		return allMessages;
	}

	public Tl1CommandCode getCommand() {
		return commandCode;
	}

	public String getCompletionCode() {
		return completionCode;
	}

	public String getCtag() {
		return correlationTag;
	}

	public Date getDate() {
		return date;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public COMPLETION_CODE getInternalCompletionCode() {
		return internalCompletionCode;
	}

	public List<Map<String, String>> getPayload() {
		return payload;
	}

	public String[] getTextBlocks() {
		return textBlocks;
	}

	// This is only used by Mediation

	public String getTid() {
		return tid;
	}

	// This is usd by TL1 wrapper to get the raw TL1 payload

	public boolean isEmbedded() {
		return Embedded;
	}

	public void resetPayload() {
		payload = null;
	}

	public void setAllMessages(List<TL1AlarmEvent> newAllMessages) {
		allMessages = newAllMessages;
	}

	// This is set by the wrapper after it parsed the payload and sets the List
	public void setCommand(Tl1CommandCode command) {
		commandCode = command;
	}

	public void setCompletionCode(String code) {
		completionCode = code;
	}

	public void setCtag(String ctag) {
		correlationTag = ctag;
	}

	/**
	 * Insert the method's description here. Creation date: (9/27/01 4:15:42 PM)
	 * 
	 * @param newDate
	 *          java.util.Date
	 */
	public void setDate(Date newDate) {
		date = newDate;
	}

	public void setEmbedded(boolean newEmbedded) {
		Embedded = newEmbedded;
	}

	public void setErrorCode(String error) {
		errorCode = error;
	}

	public void setInternalCompletionCode(COMPLETION_CODE code) {
		internalCompletionCode = code;
	}

	public void setNEInfo(NetworkElementInfo neInfo) {
		this.neInfo = neInfo;
	}

	public void setPayload(List<Map<String, String>> aList) {
		payload = aList;
		parsePayloads();
	}

	public void setTextBlocks(String[] blocks) {
		textBlocks = blocks;
	}

	public void setTid(String newTid) {
		this.tid = newTid;
	}

	// This is set by the wrapper after it parsed the payload and sets the List

	/*
	 * public void
	 * setTL1Parser(com.nortel.appcore.app.drac.server.neproxy.mediation
	 * .controller.TL1LineParser tl1Parser) { this.tl1Parser = tl1Parser; }
	 */

	// TestBlocks is the an array of Strings that contains raw TL1 payload. This
	// is only used by TL1 Engine
	/**
	 * Return a brief string representation of the response.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(50);

		/*
		 * //if this response represents an ack, it will have an ack //code but no
		 * sid` if(acknowledgementCode != null && getSid() == null) {
		 * buffer.append("Acknowledgment(Ack=");
		 * buffer.append(getAcknowledgmentCode()); buffer.append(", Ctag=");
		 * buffer.append(getCorrelationTag()); buffer.append(")"); return
		 * buffer.toString(); } if(isTimeout()) { buffer.append("Timeout(Ctag=");
		 * buffer.append(getCorrelationTag()); buffer.append(")"); return
		 * buffer.toString(); }
		 */

		// /// Header
		buffer.append("TL1 Response of: ");
		buffer.append(getCommand());
		buffer.append("\n");

		// // Header line 1
		buffer.append("   " + getTid());
		buffer.append(" " + dateFormat.format(getDate()));
		buffer.append("\n");

		// // Header line 2
		buffer.append("M  ");
		buffer.append(getCtag() + " ");
		buffer.append(getCompletionCode());

		buffer.append("\n");
		int index = textBlocks.length;
		for (int i = 0; i < index; i++) {
			buffer.append("   " + textBlocks[i]);
			buffer.append("\n");
		}
		buffer.append(";\n");

		return buffer.toString();
	}

	private void parsePayloads() {
		if (payload != null) {
			for (Map<String, String> data : payload) {
				data.put(ObjectElement.OPERATION_KEY, commandCode.toString());
				data.put("neId", neInfo.getNeID());
			}
		}
	}
}
