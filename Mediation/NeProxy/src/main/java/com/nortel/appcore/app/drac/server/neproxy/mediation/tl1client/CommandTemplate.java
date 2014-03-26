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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client;

/**
 * CommandTemplate encapsulates the construction, structure, and display of a
 * TL1 input command message. It provides a simple, high level API to specify
 * message parameters. Until Site Manager products and applications are updated
 * to the new API, CommandTemplate also supports a translation from the alpha
 * message construction API.</p></p> The general structure of a TL1 input
 * command message is: </p> <command>:<tid>:<aid(s)>:<ctag>:<general>:<payload>;
 * </p></p> CommandTemplate provides a high level API to set the contents of all
 * the blocks mentioned above. Specific methods are provided for blocks 0-3,
 * whose formats are predefined. For example, a message's command code may be
 * set with #setCommand(). </p></p> CommandTemplate provides more generic
 * methods for blocks 4+. Parameters are added one at a time. Position-defined
 * parameters may be added with #setParameter(int, int, String). Name-defined
 * parameters may be added with #setParameter(int, String, String). </p></p>
 * Note that blocks 4+ are currently populated one parameter at a time.
 * NameDefinedBlock and PositionDefined block are already defined, but hidden
 * from command message's clients. If block at a time parameter specification is
 * useful, it can be added easily.
 * <p>
 * This class will attempt to quote the payload data items and the TID if they
 * contain special characters and if the quoteItemIfNecessary boolean is true.
 * By default it will NOT try to quote things if necessary. Set the boolean to
 * on to allow automatic quoting. We don't quote the aid, the command or the
 * ctag.
 */
public interface CommandTemplate {
	/** return the command */
	String getCommand();

	/** return the ctag */
	String getCtag();

	/** return the ctag */
	String getNEId();

	/** get the tid */
	String getTID();

	/**
	 * Return true if the template is ready for transmission. The TL1Engine will
	 * not send invalid messages. Applications and commands may want to double
	 * check that their command is valid before sending. @see
	 * com.nt.transport.equinox.engine.protocol.tl1.TL1Engine
	 */
	boolean isValid();

	/**
	 * Set the contents of the access identifier block to the specified string.
	 * 
	 * @param aid
	 *          A string whose syntax is valid for the aid block. It can be a
	 *          single access identifer, two access identifers separated by a
	 *          comma, or any other strange syntax expected by the network
	 *          element. If your command has 2 aids, from and to, consider using
	 *          the more self-documenting methods #setFromAid() and #setToAid(). @see
	 *          #setFromAid(String)
	 * @see #setToAid(String)
	 */
	void setAid(String aid);

	/** set the command item */
	void setCommand(String command);

	/** set the ctag to the new value */
	void setCtag(String ctag);

	/**
	 * For a command with multiple access identifiers, set the "from" aid. This
	 * amounts to setting block 2, position 1 to the specified string.
	 */
	void setFromAid(String aid);

	/**
	 * Set the contents of the access identifier block to the specified string.
	 * 
	 * @param aid
	 *          A string whose syntax is valid for the aid block. It can be a
	 *          single access identifer, two access identifers separated by a
	 *          comma, or any other strange syntax expected by the network
	 *          element. If your command has 2 aids, from and to, consider using
	 *          the more self-documenting methods #setFromAid() and #setToAid(). @see
	 *          #setFromAid(String)
	 * @see #setToAid(String)
	 */
	void setNEId(String neId);

	/**
	 * Store the specified position-defined parameter. </p>
	 * 
	 * @param blockNumber
	 *          The parameter's 0-BASED block position. Parameter blocks are
	 *          numbered starting from 0. NOTE: parameters in blocks 0-3 cannot be
	 *          set with this method - the specific methods (ie.) setCommand()
	 *          must be used instead.
	 * @param position
	 *          The parameter's 0-BASED position in its block. Positions are
	 *          numbered starting from 0. @param parameter The value that should
	 *          appear in the specified position of the specified position-defined
	 *          block. @throws ClassCastException if the specified block is a
	 *          name-defined block.
	 */
	void setParameter(int blockNumber, int position, String parameter)
	    throws Exception;

	/**
	 * Store the specified name-defined parameter. </p> @param blockNumber The
	 * parameter's 0-BASED block position. Parameter blocks are numbered starting
	 * from 0. NOTE: parameters in blocks 0-3 cannot be set with this method - the
	 * specific methods (ie.) setCommand() must be used instead.
	 * 
	 * @param name
	 *          The name of the specified name-defined parameter, ie. "NAME" in
	 *          "NAME=VALUE". @param parameter The value of the specified
	 *          name-defined parameter, ie. "VALUE" in "NAME=VALUE". @throws
	 *          ClassCastException if the specified block is a position-defined
	 *          block.
	 */
	void setParameter(int blockNumber, String name, String parameter)
	    throws Exception;

	/**
	 * set the boolean that controls if the tid and payload should be
	 * automatically quoted.
	 */
	void setQuoteItemIfNecessary(boolean shouldQuote);

	/**
	 * set the tid to the value The tid will be quoted if appropriate.
	 */
	void setTid(String tid);

	/**
	 * For a command with multiple access identifiers, set the "to" aid. This
	 * amounts to setting block 2, position 1 to the specified string.
	 */
	void setToAid(String aid);

	/**
	 * Return a string representation of the command message. The returned string
	 * also happens to be the correct text format of the command, ready for
	 * transmission.
	 */
	String toString();
}
