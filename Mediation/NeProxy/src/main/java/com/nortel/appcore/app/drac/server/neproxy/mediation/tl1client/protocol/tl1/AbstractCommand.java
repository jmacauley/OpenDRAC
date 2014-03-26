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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1;

import java.util.Vector;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.CommandTemplate;

/**
 * This is a utility class which builds upon the raw TL1Engine. This class is
 * used to send commands to the NE. Subclasses will be notified of : timeouts,
 * error messages ( ie return code is not CMPLD ), COMPLD messages and
 * flowcontrol messages ( ie RL). Each should be handled via the appropriate
 * abstract method.
 * <p>
 * Note that #handleResponseMessage(ResponseMessage) is called for each linked
 * TL1 message that is recieved. Do not assume that this method is only called
 * once. You can check if this is the last message of a series via
 * myResponse.isComplete().
 * <p>
 * It is also possible to get all the parsed application data via the
 * getParsedPayload(), but obviously it will not contain all the payload data
 * unless the command has completed, or at least one reply has been received.
 */
public abstract class AbstractCommand {

	/** command template for building */
	private final CommandTemplate commandDesc;

	/** the parsed reply. contains parsed payload data */
	private final TL1Parser parsedReplies;

	/**
	 * a boolean to indicate if we should automatically parse the reply messages.
	 * If this boolean is false, then parsedReplies will be non-null, but will not
	 * contain any data
	 */
	private boolean autoParseReplies;

	/** the timeout interval in seconds */
	private int timeout;

	/**
	 * the listener to the engine. We hide this from users so they don't start
	 * overloading things we don't want them to.
	 */
	private final ResponseListener listener;

	/**
	 * Create a new command
	 */
	public AbstractCommand(String tid) {
		timeout = 120;
		commandDesc = new CommandTemplateImpl();
		commandDesc.setTid(tid);
		autoParseReplies = false;
		parsedReplies = new TL1Parser();

		// shh! secret listener
		//
		listener = new ResponseListener() {
			@Override
			public void received(Response response) {
				handleReceived(response);
			}

			@Override
			public void timedOut(Response response) {
				handleTimeout();
			}

		};

	}

	/**
	 * return the application payload data received to date. Note that after the
	 * last Repsonse message has been processed by the subclass, this object is
	 * cleared. note that in order for the returned object to contain data the
	 * autoParseReplies flag must be true.
	 */
	public String getCtag() {
		if (commandDesc == null) {
			return null;
		}

		return commandDesc.getCtag();
	}

	/**
	 * return the application payload data received to date. Note that after the
	 * last Repsonse message has been processed by the subclass, this object is
	 * cleared. note that in order for the returned object to contain data the
	 * autoParseReplies flag must be true.
	 */
	public TL1Parser getParsedPayload() {
		return parsedReplies;
	}

	/**
	 * After the command is sent, this method is called to obtain the string to
	 * log. This method allows the subclass a chance to hide and sensitive info (
	 * ie passwords) before returning the String to log.
	 */
	public String getStringToLog() throws Exception {
		tweakCommandTemplateForLogging(commandDesc);
		return commandDesc.toString();
	}

	/**
	 * Set the command timeout in seconds
	 */
	public void setTimeout(int timeoutInSeconds) {
		if (timeoutInSeconds < 0) {
			throw new RuntimeException("Timeout must be greater than zero. "
			    + "Here we obey the laws of Thermodynamic");
		}

		timeout = timeoutInSeconds;
	}

	/**
	 * internal method called by listener. Don't touch
	 */
	void handleReceived(Response response) {
		String code = response.getCompletionCode();
		String ack = response.getAcknowledgmentCode();

		if (TL1Constants.COMPLETED.equals(code)
		    || TL1Constants.PARTIAL.equals(code)
		    || TL1Constants.RETRIEVE.equals(code)) {
			// only add the response if the flag is set
			//
			if (autoParseReplies) {
				parsedReplies.addData(response);
			}

			handleResponseMessage(response);

		}
		else if (TL1Constants.REPEAT_LATER.equals(ack)) {
			handleFlowControl();
		}
		else if (TL1Constants.IN_PROGRESS.equals(ack) && code == null) {
			handleResponseMessage(response);
		}
		else {
			Vector<String> data = response.getData();
			String errorCode = "";

			if (!data.isEmpty()) {
				errorCode = data.firstElement();
			}

			// In the case of an error, we do not append the reply event
			// to the parser. We overwite any previous data with the error
			// response if the flag is set.
			//
			if (autoParseReplies) {
				parsedReplies.setData(response);
			}

			handleError(response, errorCode);

		}

		// If this response is complete, then this means that
		// by here the user has been notified of the message,
		// and that they have done whatever they need to do.
		// We do not want to keep a handle to all the parsed
		// data, since by this point it is not needed.

		if (response.isComplete()) {
			parsedReplies.clear();
			// Dead store of null to local variable, let the JVM do handle this
			// response = null;
		}

	}

	/**
	 * Send this command to the engine. This method should not be called directly.
	 * It will be called by the TL1LanguageEngine.
	 */
	boolean send(TL1Engine engine) {
		if (buildCommand(commandDesc)) {
			engine.send(commandDesc, listener, timeout);
			handlePostSend();
			return true;
		}
		return false;
	}

	/**
	 * subclasses use this to build up the outgoing tl1 message
	 */
	protected abstract boolean buildCommand(CommandTemplate temp);

	/**
	 * This method is called in repsonse to errors like a DENY repsonse from the
	 * NE.
	 */
	protected abstract void handleError(Response response, String errorCode);

	/** this method is called if the command was flow controlled */
	protected abstract void handleFlowControl();

	/**
	 * this method is called right after the message is sent
	 */
	protected void handlePostSend() {

	}

	/**
	 * deal with the response message. This method is called once for every linked
	 * TL1 message we receive. <b> Do not assume this method will be called only
	 * once </b>
	 * <p>
	 * You can check if the message is complete via a call to
	 * repsonse.isComplete()
	 * <p>
	 * get the application payload data by calling #getParsedPayload()
	 */
	protected abstract void handleResponseMessage(Response respose);

	/**
	 * if a command never returns then this method is called after the timeout
	 */
	protected abstract void handleTimeout();

	/**
	 * In order to automatically add response messages to the parsed replies, this
	 * boolean must be true. Otherwise getParsedPayload() will return non-null but
	 * be empty.
	 */
	protected void setAutoParseReplies(boolean flag) {
		autoParseReplies = flag;
	}

	/**
	 * This method does nothing. Subclasses who need to hide passwords ( or other
	 * sensitive data) from the commlog shoud overload this method to replace any
	 * sensitive block items with a new String.
	 */
	protected void tweakCommandTemplateForLogging(CommandTemplate temp)
	    throws Exception {

	}
}
