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

import java.util.LinkedList;
import java.util.List;

import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1.OutputMessage;

/**
 * Originally MessageDispatcher was using a Vector for it's Queue. This class is
 * much more efficient since this uses a linked list. Vectors are array based
 * and are notoriously expensive for removing 1st element and growing.
 * <p>
 * We make things synchronized to avoid any nasty surprises from the legacy
 * code. ( Vector is synchronized )
 */
final class MessageQueue {

	/**
	 * we use a linked list so that we can have a nicely performing queue. ie O(c)
	 * adds and removes
	 */
	private final List<OutputMessage> queue = new LinkedList<OutputMessage>();

	/** empty the queue */
	public synchronized void clear() {
		queue.clear();
	}

	/**
	 * destroy the event queue.
	 */
	public synchronized void dispose() {
		queue.clear();
	}

	/**
	 * return true is linked list is empty
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * return a string representation of this object
	 * 
	 * @return identity of this object
	 */
	@Override
	public String toString() {
		return "TL1 Engine Message Queue";
	}

	/**
	 * remove the first element from the queue
	 */
	synchronized OutputMessage dequeue() {
		if (queue.isEmpty()) {
			return null;
		}

		return queue.remove(0);
	}

	/**
	 * add an element to the queue
	 */
	synchronized void enqueue(OutputMessage message) {
		// add event to end
		//
		queue.add(message);
	}

	/**
	 * return true is linked list is empty
	 */
	synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
}
