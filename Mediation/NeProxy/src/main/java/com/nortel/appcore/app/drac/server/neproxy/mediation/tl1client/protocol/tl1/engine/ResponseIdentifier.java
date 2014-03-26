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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.engine;

import java.io.IOException;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.OutputMessage;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.Response;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.TL1Constants;

/**
 * ResponseIdentifier parses an output response message identifier line. The
 * rough format of the response message identifier: </p></p> <response id> ::=
 * <cr><lf>M^^<ctag>^<completion code> ResponseIdentifier's nextState() method
 * reads the expected ctag and completion code, and returns a MessageTerminator.
 * The terminator will parse the autonomous message's text block and terminator.
 * Note that response identifier expects the leading <cr><lf> to be stripped.
 */
final class ResponseIdentifier extends AbstractMessageIdentifier {
	/**
	 * Read the expected ctag and completion code. Return a MessageTerminator
	 * which will parse the autonomous message's text block and terminator.
	 */
	@Override
	public AbstractParseState nextState() throws IOException,
	    InterruptedException {
		Response response = (Response) message;

		try {
			getReader().read("M");
			getReader().skip(TL1Reader.spaces);
		}
		catch (SyntaxException exception) {
			return logError("Invalid response reply code: " + exception.getMessage(),
			    message);
		}

		try {
			String ctag = getReader().readCorrelationTag();
			response.setCorrelationTag(ctag);
			getReader().skip(TL1Reader.spaces);
		}
		catch (SyntaxException exception) {
			return logError(exception.getMessage(), message);
		}

		try {
			String code = getReader().readCode(TL1Constants.completionCodes);
			response.setCompletionCode(code);
		}
		catch (SyntaxException exception) {
			return logErrorAndForward("Invalid response completion code: "
			    + exception.getMessage(), message);
		}

		return super.nextState();
	}

	/**
	 * Intended for the use of MessageHeader, so that it can pass the data it
	 * parsed on to this state. Copy the data from output into a Response.
	 */
	@Override
	public void setMessage(OutputMessage output) {
		message = new Response(output);
	}

	/**
	 * Return a very short string that uniquely identifies the parsing state
	 * subclass, used in parsing machine debug messages.
	 */
	@Override
	protected String getDebugId() {
		return "ri";
	}
}
