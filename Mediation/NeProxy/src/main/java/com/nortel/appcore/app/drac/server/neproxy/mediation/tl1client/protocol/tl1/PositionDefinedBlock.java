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

/**
 * PositionDefinedBlock encapsulates the construction, structure, and display of
 * a position-defined block of a TL1 input command message. It provides a
 * simple, high-level API to specify the parameters of a position-defined block.
 * The general format of a position-defined block:</p></p> <position-defined
 * block> ::= [<position-defined parameter>[,<position-defined parameter>]*]
 * </p> <position-defined parameter> ::= <value> | <empty> Some special rules
 * for omitting position-defined parameters apply. Consider a position- defined
 * block with 5 positions: </p></p> <parm0>,<parm1>,<parm2>,<parm3>,<parm4>
 * </p></p> Trailing parameters and their delimiting commas may be omitted. For
 * example, if parameters 2-4 are omitted, then "<parm0>,<parm1>" is a valid
 * position-defined block. </p></p> Non-trailing parameters may be omitted if
 * their delimiting comma is included. For example, if parameters 1, 3 and 4 are
 * omitted, then "<parm0>,,<parm2>" is a valid position-defined block. </p></p>
 * PositionDefinedBlock does not require specification of omitted parameters.
 * The message setParameter(4, "VALUE") is enough to completely specify a
 * position-defined block in which parameters 0-3 are omitted. Internally,
 * PDBlock null-pads its parameter vector to the correct length, and, when
 * printing, remembers that nulls represent omitted parameters.
 */
public class PositionDefinedBlock {
	/**
	 * The parameters for this position-defined block. </p> Internal note: Omitted
	 * parameters are internally represented by nulls.
	 */
	protected Vector<String> parameters = new Vector<String>();
	protected boolean trailingCommas = true;

	/**
	 * Insert the method's description here. Creation date: (11/28/01 1:22:29 PM)
	 * 
	 * @param args
	 *          java.lang.String[]
	 */
	// public static void main(String[] args)
	// {
	// PositionDefinedBlock block = new PositionDefinedBlock();
	// block.setParameter(0, null);
	// block.setParameter(1, null);
	// block.setParameter(2, "Vu");
	//
	// PositionDefinedBlock blockAID = new AccessIdentifierBlock();
	// blockAID.setParameter(0, null);
	// blockAID.setParameter(1, null);
	// blockAID.setParameter(2, null);
	//
	// }
	/**
	 * Return true if the block contains any parameters, false otherwise.
	 */
	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	/**
	 * Add a position-defined parameter to this parameter block. @param position
	 * The 0-based postion of the parameter. Parameter positions are numbered
	 * starting at 0. Note that negative positions are ignored.
	 * 
	 * @param parameter
	 *          The value for the positionth comma-separated parameter. Note that
	 *          null parameters are ignored.
	 */
	public void setParameter(int position, String parameter) {
		if (position < 0) {
			return;
		}

		// / support trailing commas
		if (parameter == null && !trailingCommas) {
			return;
		}

		// make sure the parameter can be inserted
		if (parameters.size() < position + 1) {
			parameters.setSize(position + 1);
		}

		// store the parameter
		parameters.setElementAt(parameter, position);
	}

	/**
	 * Return a string representation of this block. The string returned is also
	 * the correct TL1 format of the block.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		Enumeration<String> elementList = parameters.elements();
		Object parameter;
		while (elementList.hasMoreElements()) {
			parameter = elementList.nextElement();
			if (parameter != null) {
				buffer.append(parameter);
			}
			if (elementList.hasMoreElements()) {
				buffer.append(',');
			}
		}

		return buffer.toString();
	}
}
