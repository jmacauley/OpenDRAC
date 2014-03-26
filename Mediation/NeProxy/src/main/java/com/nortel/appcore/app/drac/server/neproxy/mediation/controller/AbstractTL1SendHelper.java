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

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.server.neproxy.mediation.sessioninfo.NetworkElementInfo;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1RequestMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1ResponseMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.CommandTemplate;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.AbstractCommand;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Response;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;

public abstract class AbstractTL1SendHelper extends AbstractCommand {
  
  protected final Logger log = LoggerFactory.getLogger(getClass());
	protected NetworkElementInfo neInfo;
	protected TL1RequestMessage inputMessage;
	protected TL1ResponseMessage responseMessage;
	protected TL1Composer composer;
	protected TL1LineParser parser;

	// this contains all the lines of data from all the linked responses.
	protected List<String> allBlocks;

	private static final String IICM = "IICM";
	// the command code. we need to hide the password form the commlog.
	protected String code;

	// completion code list
	private static final List<String> COMPLETION_CODES_LIST = Arrays
	    .asList(TL1Constants.completionCodes);

	public AbstractTL1SendHelper(NetworkElementInfo ne, TL1RequestMessage msg,
	    TL1Composer newComposer, TL1LineParser newParser) {
		super(ne.getNetworkElementName());
		neInfo = ne;
		inputMessage = msg;
		composer = newComposer;
		parser = newParser;
		allBlocks = new Vector<String>();
		responseMessage = new TL1ResponseMessage(ne.getNetworkElementName(),
		    inputMessage.getCommandCode());
		responseMessage.setNEInfo(ne);
		// responseMessage.setTL1Parser(parser);
		setTimeout(120);
	}

	public NetworkElementInfo getNeInfo() {
		return neInfo;
	}

	public TL1ResponseMessage getTL1ResponseMessage() {
		return responseMessage;
	}

	/**
	 * This method is called immediately after the message is sent.
	 */

	public boolean isCompleted() {
		if (responseMessage == null) {
			return false;
		}
		if (responseMessage.getCompletionCode() == null) {
			return false;
		}
		if (responseMessage.getCompletionCode().equals(TL1Constants.COMPLETED)) {
			return true;
		}
		return false;
	}

	public boolean isInProgress(Response response) {
		// 
		String completeCode = response.getCompletionCode();
		if (COMPLETION_CODES_LIST.contains(completeCode)) {
			return false;
		}
		if (TL1Constants.IN_PROGRESS.equals(response.getAcknowledgmentCode())) {
			return true;
		}
		return false;
	}

	public void setNeInfo(NetworkElementInfo newNeInfo) {
		neInfo = newNeInfo;
	}

	/**
	 * This method is called immediately before send
	 **/

	@Override
	protected boolean buildCommand(CommandTemplate msg) {
		try {
			// // the whole NEId added for SD support at engine level.
			msg.setNEId(neInfo.getNetworkElementID());
			msg.setTid(neInfo.getNetworkElementName());
			composer.composeTL1Message(msg, inputMessage.getCommandCode().toString(),
			    inputMessage.getParameters());
			// Printlog.println("TL1SendHelper done composing");
		}
		catch (NoSuchMethodError e) {
			String payload[] = new String[] { "SOUTH_BOUND",
			    "This version of tl1wrapper.jar is mismatch with this load." };
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.REQUEST_ERROR);
			responseMessage.setCompletionCode(TL1ResponseMessage.UNKNOWN);
			responseMessage.setTextBlocks(payload);
			log.debug(
			    "TL1 Request PreSend error for " + inputMessage.getCommandCode()
			        + "This tl1wrapper.jar have to be replaced with correct version.",
			    e);
			return false;
		}
		catch (ClassNotFoundException e) {
			log.error("Error: ", e);
			String payload[] = new String[] { IICM,
			    "Input, Invalid Command: Command is not supported" };
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.PROCESSING_ERROR);
			responseMessage.setCompletionCode(TL1ResponseMessage.UNKNOWN);
			responseMessage.setTextBlocks(payload);
			log.debug(
			    "TL1 Request PostSend warns for " + responseMessage.getCommand()
			        + " " + responseMessage.getTid() + " Ctag "
			        + responseMessage.getCtag() + " with completion code "
			        + responseMessage.getCompletionCode() + " at: "
			        + responseMessage.getDate() + " problem: " + e.getMessage(), e);
			return false;
		}
		catch (Exception e) {
			log.error("Error: ", e);
			String payload[] = new String[] { "SOUTH_BOUND",
			    "Failed to compose the command due to" + e.getMessage() };
			responseMessage
			    .setInternalCompletionCode(TL1ResponseMessage.COMPLETION_CODE.PROCESSING_ERROR);
			responseMessage.setCompletionCode(TL1ResponseMessage.UNKNOWN);
			responseMessage.setTextBlocks(payload);
			log.debug(
			    "TL1 Request PostSend warns for " + responseMessage.getCommand()
			        + " " + responseMessage.getTid() + " Ctag "
			        + responseMessage.getCtag() + " with completion code "
			        + responseMessage.getCompletionCode() + " at: "
			        + responseMessage.getDate() + " problem: " + e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Subclasses who need to hide passwords ( or other sensitive data) from the
	 * commlog shoud overload this method to replace any sensitive block items
	 * with a new String.
	 **/

	@Override
	protected void tweakCommandTemplateForLogging(CommandTemplate temp)
	    throws Exception {
		if ("ACT-USER".equals(code)) {
			temp.setParameter(5, 0, "XXX");
		}
	}
}
