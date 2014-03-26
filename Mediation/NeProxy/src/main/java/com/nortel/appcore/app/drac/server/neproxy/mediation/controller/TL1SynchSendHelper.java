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

package com.nortel.appcore.app.drac.server.neproxy.mediation.controller;

import java.util.Date;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Response;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;

public final class TL1SynchSendHelper extends AbstractTL1SendHelper {

	/**
	 * TL1SynchSendHelper constructor comment.
	 * 
	 * @param ne
	 * @param msg
	 * @param newComposer
	 * @param newParser
	 */

	public TL1SynchSendHelper(NetworkElementInfo ne, TL1RequestMessage msg,
	    TL1Composer newComposer, TL1LineParser newParser) {
		super(ne, msg, newComposer, newParser);
	}

	/**
	 * This method is called in repsonse to errors like a DENY repsonse from the
	 * NE.
	 **/

	@Override
	protected void handleError(Response response, String errorCode) {
		try {
			responseMessage.setCtag(response.getCorrelationTag());
			responseMessage.setCompletionCode(response.getCompletionCode());
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.REQUEST_ERROR);
			responseMessage.setErrorCode(errorCode);
			responseMessage.setTextBlocks(response.getTextBlock());
			responseMessage.setDate(response.getDate());
			responseMessage.setTid(response.getSid());

		}
		catch (Exception e) {
			log.error("handleError", e);
		}
		finally {
			wakeup();
		}
	}

	/**
	 * when the NE is overloaded, it returns RL. retry later.
	 **/

	@Override
	protected void handleFlowControl() {
		try {
			String payload[] = new String[] { "SOUTH_BOUND", "Flow control" };
			responseMessage.setCompletionCode(TL1ResponseMessage.UNKNOWN);
			responseMessage.setTextBlocks(payload);
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.PROCESSING_ERROR);
			responseMessage.setDate(new Date());

		}
		catch (Exception e) {
			log.error("handleFlowControl", e);
		}
		finally {
			wakeup();
		}
	}

	/**
	 * This method is called immediately after the message is sent.
	 **/

	@Override
	protected void handlePostSend() {

		synchronized (responseMessage) {
			try {
				if (responseMessage.getCompletionCode() == null
				    || responseMessage.getTextBlocks() == null) {
					responseMessage.wait();
				}
				// 
				if (responseMessage.getTextBlocks() != null
				    && hasData(responseMessage.getCompletionCode())) {
					parser.parse(responseMessage);
				}

			}
			catch (Exception e) {
				log.error("In handling postMessage", e);
			}
		}
	}

	/**
	 * This message gets called every time a piece of a completed message gets
	 * called. This method is not called if an error happened. To check if this is
	 * the last message check response.isComplete(). Note that getParsedPayload()
	 * will return the payload data for all the messages received.
	 **/

	@Override
	protected void handleResponseMessage(Response response) {
		/*
		 * applications don't handle IP curently. Therefore, don't wakeup
		 * application. In the future, we need to allow applications (eg. RTRV-HDR)
		 * to receive this IP and handle it themselves.
		 */
		if (isInProgress(response)) {
			// 
			return;
		}
		// need to store the data from linked message
		try {
			String currentBlocks[] = response.getTextBlock();
			for (String currentBlock : currentBlocks) {
				allBlocks.add(currentBlock);
			}

			// if not complete, do nothing
			if (!response.isComplete()) {
				return;
			}

			// we have received the last block
			responseMessage.setCtag(response.getCorrelationTag());
			responseMessage.setCompletionCode(response.getCompletionCode());
			responseMessage.setDate(response.getDate());
			responseMessage.setTid(response.getSid());

			// get all the data form all linked messages
			// String allData[] = new String[allBlocks.size()];
			// allBlocks.copyInto(allData);
			String allData[] = allBlocks.toArray(new String[allBlocks.size()]);
			responseMessage.setTextBlocks(allData);
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.SUCCESS);

			wakeup();

		}
		catch (Exception e) {
			log.error("Error: ", e);
			String payload[] = new String[] { "NORTH_BOUND", e.getMessage() };
			responseMessage.setCompletionCode(TL1ResponseMessage.UNKNOWN);
			responseMessage.setTextBlocks(payload);
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.PROCESSING_ERROR);
			responseMessage.setDate(new Date());
			wakeup();
			// String ctag = responseMessage.getCtag();
			// String error = "Command failed: " + responseMessage.getCommand()
			// + (ctag == null ? "" : " with ctag " + responseMessage.getCtag())
			// + " with description: " + e.getMessage();

		}
	}

	/**
	 * if a command never returns then this method is called after the timeout
	 **/

	@Override
	protected void handleTimeout() {
		try {
			String payload[] = new String[] { "N/A", "Timeout on receiving message" };
			responseMessage.setCompletionCode(TL1ResponseMessage.UNKNOWN);
			responseMessage.setTextBlocks(payload);
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.TIME_OUT);
			responseMessage.setDate(new Date());
		}
		catch (Exception e) {
			log.error("handleTimeout", e);
		}
		finally {
			wakeup();
		}
	}

	/**
	 * check for data from response message
	 */
	private boolean hasData(String code) {
		return TL1Constants.COMPLETED.equals(code)
		    || TL1Constants.PARTIAL.equals(code);
	}

	private void wakeup() {

		// notify the sleeping thread waiting for this message
		synchronized (responseMessage) {
			responseMessage.notify();
		}
	}
}
