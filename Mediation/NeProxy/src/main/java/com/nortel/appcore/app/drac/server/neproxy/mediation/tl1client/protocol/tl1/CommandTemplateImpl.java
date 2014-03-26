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

import java.util.Enumeration;
import java.util.Vector;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.CommandTemplate;

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
 * 
 * @see NameDefinedBlock @see PositionDefinedBlock
 */

final class CommandTemplateImpl implements CommandTemplate {

	/**
	 * BLOCK 0, command code.
	 * 
	 * @see #setCommand(String)
	 * @see #getCommand()
	 */

	private String commandCode;

	/**
	 * BLOCK 1, target identifier.
	 * 
	 * @see #setTid(String)
	 */

	private String targetIdentifier = "";

	/**
	 * BLOCK 2, access identifier.
	 * 
	 * @see #setAid(String)
	 * @see #setFromAid(String)
	 * @see #setToAid(String)
	 */

	private final PositionDefinedBlock accessIdentifier = new AccessIdentifierBlock();

	/**
	 * BLOCK 3, correlation tag. Note that this is overwritten by the TL1 Engine.
	 */

	private String correlationTag;

	/**
	 * BLOCKS 4+. Note that BLOCK 4, the general block, is a position defined
	 * block.
	 */
	// Name or position defined block
	private final Vector blocks = new Vector();

	/** flag to indicate if we should try quoting the item */

	private boolean quoteItemIfNecessary;

	/** flag to indicate if we should try validating the item */

	// private boolean validateItemIfNecessary = true;
	/** hold the NE uniqueIdentifier */

	private String neID;

	/**
	 * Empty template
	 */

	public CommandTemplateImpl() {
		quoteItemIfNecessary = true;
	}

	/**
	 * Create and return a new command message with the specified command code.
	 */

	// public CommandTemplateImpl(String command)
	// {
	// quoteItemIfNecessary = true;
	// setCommand(command);
	// }
	/**
	 * Create and return a new command message with the specified command code and
	 * target identifier.
	 */

	// private CommandTemplateImpl(String command, String tid)
	// throws Exception
	// {
	// quoteItemIfNecessary = true;
	// setCommand(command);
	// setTid(tid);
	// }
	/**
	 * Return a special copy of the command template. The new template will have
	 * the same command, tid, aid and ctag, but will be otherwise empty. This is
	 * used to safely log commands that contain passwords.
	 */

	// public CommandTemplate copyWithoutPayload()
	// throws Exception
	// {
	// CommandTemplateImpl clone = new CommandTemplateImpl(commandCode,
	// targetIdentifier);
	// clone.accessIdentifier = accessIdentifier;
	// clone.setCtag(getCtag());
	// return clone;
	// }
	/** get the aid */

	// public PositionDefinedBlock getAID()
	// {
	// return accessIdentifier;
	// }
	/** get the blocks */

	// public Vector getBlocks()
	// {
	// return blocks;
	// }
	/** return the command */

	@Override
	public String getCommand() {
		return commandCode;
	}

	/** return the ctag */

	@Override
	public String getCtag() {
		return correlationTag;
	}

	/**
	 * getNEId method comment.
	 */

	@Override
	public String getNEId() {
		return neID;
	}

	/**
	 * get the tid
	 */
	@Override
	public String getTID() {
		return targetIdentifier;
	}

	/**
	 * Return true if the template is ready for transmission. The TL1Engine will
	 * not send invalid messages. Applications and commands may want to double
	 * check that their command is valid before sending. @see
	 * com.nt.transport.equinox.engine.protocol.tl1.TL1Engine
	 */

	@Override
	public boolean isValid() {
		if (commandCode == null) {
			return false;
		}

		if (targetIdentifier == null) {
			return false;
		}

		return true;

	}

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

	@Override
	public void setAid(String aid) {
		accessIdentifier.setParameter(0, aid);
	}

	/** set the command item */

	@Override
	public void setCommand(String command) {
		commandCode = command;
	}

	/** set the ctag to the new value */

	@Override
	public void setCtag(String ctag) {
		correlationTag = ctag;
	}

	/**
	 * For a command with multiple access identifiers, set the "from" aid. This
	 * amounts to setting block 2, position 1 to the specified string.
	 */

	@Override
	public void setFromAid(String aid) {
		accessIdentifier.setParameter(0, aid);
	}

	/**
	 * setNEId method comment.
	 */

	@Override
	public void setNEId(java.lang.String neId) {
		this.neID = neId;
	}

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

	@Override
	public void setParameter(int blockNumber, int position, String parameter)
	    throws Exception {
		// translate the block number to a position in the blocks vector
		int blockIndex = getBlockIndex(blockNumber);
		PositionDefinedBlock block = (PositionDefinedBlock) blocks
		    .elementAt(blockIndex);
		if (block == null) {
			block = new PositionDefinedBlock();
			blocks.setElementAt(block, blockIndex);
		}

		// getNewValue will quote the string if necessary

		//
		block.setParameter(position, getNewValue(parameter));

		// String validation is done in mediation.controller.
		/*
		 * if (TL1Util.validateString(parameter, neID)) block.setParameter(position,
		 * getNewValue( parameter) ); else throw new
		 * Exception("parameter contains unsupported character(s): " + parameter);
		 */

	}

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

	@Override
	public void setParameter(int blockNumber, String name, String parameter)
	    throws Exception {
		// translate the block number to a position in the blocks vector

		int blockIndex = getBlockIndex(blockNumber);

		NameDefinedBlock block = (NameDefinedBlock) blocks.elementAt(blockIndex);

		if (block == null) {
			block = new NameDefinedBlock();
			blocks.setElementAt(block, blockIndex);

		}

		block.setParameter(name, getNewValue(parameter));

		// get new value will return a quoted String if appropriate

		// block.setParameter(name, getNewValue( parameter) );

		// String Validation is done at mediation.controller
		/*
		 * if (TL1Util.validateString(parameter, neID)) block.setParameter(name,
		 * getNewValue( parameter) ); else throw new
		 * Exception("parameter contains unsupported character(s): " + parameter);
		 */

	}

	/**
	 * set the boolean that controls if the tid and payload should be
	 * automatically quoted.
	 */

	@Override
	public void setQuoteItemIfNecessary(boolean shouldQuote) {
		quoteItemIfNecessary = shouldQuote;
	}

	/**
	 * set the tid to the value The tid will be quoted if appropriate.
	 */

	@Override
	public void setTid(String tid) {
		targetIdentifier = tid;
	}

	/**
	 * For a command with multiple access identifiers, set the "to" aid. This
	 * amounts to setting block 2, position 1 to the specified string.
	 */

	@Override
	public void setToAid(String aid) {
		accessIdentifier.setParameter(1, aid);
	}

	/*
	 * public void setValidateItemIfNecessary(boolean shouldValidate) {
	 * validateItemIfNecessary = shouldValidate; }
	 */

	/**
	 * Return a string representation of the command message. The returned string
	 * also happens to be the correct text format of the command, ready for
	 * transmission.
	 */

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(commandCode);
		buffer.append(':');
		buffer.append(targetIdentifier);
		buffer.append(':');
		buffer.append(accessIdentifier);
		buffer.append(':');
		if (correlationTag != null) {
			buffer.append(correlationTag);
		}

		if (blocks.isEmpty()) {
			buffer.append(';');
			return buffer.toString();
		}

		buffer.append(':');

		Enumeration elementList = blocks.elements();
		Object block;
		while (elementList.hasMoreElements()) {
			block = elementList.nextElement();

			if (block != null) {
				buffer.append(block);
			}

			if (elementList.hasMoreElements()) {
				buffer.append(':');
			}
			else {
				buffer.append(';');
			}

		}

		return buffer.toString();

	}

	/**
	 * Return the real index (in the blocks vector) of the specified message block
	 * number. The blocks vector stores parameter blocks 4+, and its numbering is
	 * also 0-based. Therefore, the index of the general block (4) in the blocks
	 * vector is really 0.</p>
	 */

	private int getBlockIndex(int blockNumber) {
		// translate the block number to a position in the blocks vector
		int blockIndex = blockNumber - 4;
		// make sure it will be safe to place a value in that position
		if (blocks.size() < blockIndex + 1) {
			blocks.setSize(blockIndex + 1);
		}
		return blockIndex;
	}

	/**
	 * do the quoting
	 */

	private String getNewValue(String input) {
		if (!quoteItemIfNecessary) {
			return input;
		}
		return TL1Util.getQuotedString(input);

	}
}
