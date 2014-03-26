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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * CommAdapterWriter is part of a bridge pattern with CommAdapter. All
 * CommAdapters may or may not need to implement a pause between writing
 * characters; CommAdapterWriter decouples that behavior from the CommAdapter
 * hierarchy.</p> Future considerations: this hierarchy is really a fix for the
 * OC-48 buffer. If IP data adds overhead to data sent anyway (does this apply
 * only to datagram sockets? are IP stream connections affected?), then this is
 * really only a fix for OC-48 serial port, and if that's true, this hierarchy
 * should be removed and the delay functionality set in serial port adapter.
 */
abstract class AbstractCommAdapterWriter {
	/**
	 * The writer field is a java.io.Writer. Whether it is normal output stream
	 * writer, a buffered writer or even a string writer is up to concrete
	 * subclasses.
	 */
	protected Writer writer;

	/**
	 * The listener to notify of what data has been written.
	 */
	protected CommAdapterWriterListener writeListener;

	/**
	 * Concrete subclasses should wrap the specified stream with the appropriate
	 * stream wrapper.
	 */
	public abstract void connect(OutputStream stream);

	/**
	 * Set the adapter's write listener; the listener will be notified when
	 * characters are written. This method should only be used by the comm log,
	 * since it only allows one listener.
	 */
	public void setCommAdapterWriterListener(CommAdapterWriterListener listener) {
		writeListener = listener;
	}

	/**
	 * Write the specified string to the receiver's Writer. Concrete
	 * implementations should be synchronized because we want to make sure that
	 * two threads do not write at the same time, causing commands to interleave.
	 * 
	 * @throws java.io.IOException
	 *           The CommAdapter catches these exceptions and wraps them with
	 *           CommAdapterException
	 */
	public abstract void write(String command) throws IOException;

	/**
	 * Notify the write adapter's CommAdapterWriterListener that data was written.
	 */
	protected void fireWritten(String data) {
		if (writeListener != null) {
			writeListener.written(data);
		}
	}
}
