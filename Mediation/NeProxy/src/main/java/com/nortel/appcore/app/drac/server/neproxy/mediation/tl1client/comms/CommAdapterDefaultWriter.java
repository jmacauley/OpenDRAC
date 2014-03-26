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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * CommAdapterDefaultWriter is a concrete implementation of the CommAdapter-
 * CommAdapterWriter bridge pattern. It provides concrete writing behaviour for
 * its CommAdapter. CommAdapterDefaultWriter does not implement a pause between
 * writing characters. It therefore uses a buffered writer and writes entire
 * strings at once. @see #CommDelayedWriter
 */
final class CommAdapterDefaultWriter extends AbstractCommAdapterWriter {
	/**
	 * Wrap the specified stream. Since CommAdapterDefaultWriter can buffer data,
	 * it uses a BufferedWriter.
	 */
	@Override
	public void connect(OutputStream stream) {
		writer = new BufferedWriter(new OutputStreamWriter(stream));
	}

	/**
	 * Write the specified string to the receiver's Writer. Concrete *
	 * implementations should be synchronized because we want to make sure that
	 * two threads do not write at the same time, causing commands to interleave. @throws
	 * java.io.IOException The CommAdapter catches these exceptions and wraps them
	 * with CommAdapterException
	 */
	@Override
	public synchronized void write(String command) throws IOException {
		writer.write(command);
		writer.flush();
		fireWritten(command);
	}
}
