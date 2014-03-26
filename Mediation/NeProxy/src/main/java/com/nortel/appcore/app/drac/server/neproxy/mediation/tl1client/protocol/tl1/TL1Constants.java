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

/**
 * TL1Constants defines several commonly used TL1 constants. For more details on
 * the meaning and usage of these values, refer to [Bellcore 96] GR-831-CORE,
 * OTGR Section 12.1: Operations Application Messages - Language for Operations
 * Application Messages, Bellcore, 1996.
 */
public final class TL1Constants {
	/**
	 * ACKNOWLEDGMENT CODES identify the reason for an acknowledgment message. A
	 * brief description of each code is provided below. For more details, refer
	 * to [Bellcore 96] </p></p> The command has been initiated. Output response
	 * messages should follow.
	 */
	public static final String IN_PROGRESS = "IP";
	/**
	 * The command has been initiated. Output response messages should follow.
	 */
	public static final String PRINTOUT_FOLLOWS = "PF";

	/**
	 * The command was received and the requested action was initiated and
	 * completed.
	 */
	public static final String OK = "OK";

	/**
	 * The command was accepted, but something has gone wrong. Initiation and
	 * execution of the command are uncertain.
	 */
	public static final String NO_ACKNOWLEDGEMENT = "NA";

	/**
	 * The command is valid, but the request conflicts with current system or
	 * equipment status.
	 */
	public static final String NO_GOOD = "NG";

	/**
	 * The system is busy and cannot process the command. The command may be
	 * resent later.
	 */
	public static final String REPEAT_LATER = "RL";

	/** All expected values for acknowledgement codes. */
	public static final String[] acknowledgementCodes = { IN_PROGRESS,
	    PRINTOUT_FOLLOWS, OK, NO_ACKNOWLEDGEMENT, NO_GOOD, REPEAT_LATER };

	/**
	 * OUTPUT RESPONSE MESSAGE COMPLETION CODES identify the success of a command.
	 * A brief description of each code is provided below. For more details, refer
	 * to [Bellcore 96] </p></p> The command was successfully completed.
	 */
	public static final String COMPLETED = "COMPLD";

	/** The command was totally denied. */
	public static final String DENY = "DENY";

	/**
	 * For commands which specify multiple AIDs, a subset have been successfully
	 * executed. If all AIDs failed, a DENY is expected.
	 */
	public static final String PARTIAL = "PRTL";

	/** The command has been queued for delayed execution. */
	public static final String DELAY = "DELAY";

	/**
	 * The response is part of a lengthy retrieve command. The same command may
	 * return several responses with RTRV, but the final response must be a
	 * COMPLD.
	 */
	public static final String RETRIEVE = "RTRV";

	/** All expected values for output response message completion codes. */
	public static final String[] completionCodes = { COMPLETED, DENY, PARTIAL,
	    DELAY, RETRIEVE };

	/**
	 * AUTONOMOUS MESSAGE ALARM CODES identify the severity of autonomous
	 * messages. A brief description of each code is provided below. For more
	 * details, refer to [Bellcore 96] </p></p> The most severe alarms.
	 * Unprotected optical line or circuit pack alarms.
	 */
	public static final String CRITICAL = "*C";

	/** Less severe alarms. Unprotected tributary line or circuit pack alarms. */
	public static final String MAJOR = "**";

	/** Protected or non-traffic affecting alarms. */
	public static final String MINOR = "* ";

	/** Non-alarm messages. */
	public static final String ALERT = "A ";

	/** All expected values for autonomous message alarm codes. */
	public static final String[] alarmCodes = { CRITICAL, MAJOR, MINOR, ALERT };

	/**
	 * MESSAGE TERMINATOR CHARACTERS </p></p> Terminates an acknowledgment.
	 */
	public static final char ACK_TERMINATOR = '<';

	/**
	 * Terminates an individual output response message and indicates that NO more
	 * messages will follow.
	 */
	public static final char COMPLETE = ';';
	/**
	 * Terminates an individual output response message, but indicates that more
	 * messages will follow.
	 */
	public static final char INCOMPLETE = '>';
	/** Indicates that the terminator character was not read. */
	public static final char UNKNOWN = '?';
	/** All expected values for message terminator characters. */
	public static final char[] messageTerminators = { ACK_TERMINATOR, COMPLETE,
	    INCOMPLETE };

	private TL1Constants() {
	}
}
