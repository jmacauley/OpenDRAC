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

package com.nortel.appcore.app.drac.common.utility;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RmiOperationHelper {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * The Operation class holds data we use for logging the start/finish/error of
	 * a RMI method call. We store an instance of Operation on a ThreadLocal when
	 * the method is invoked (via start) and fetch it back via the ThreadLocal on
	 * the error() and finish() calls.
	 * 
	 * @author pitman
	 */
	private static final class Operation {
		private final String remoteIP;
		private final String operationName;
		private final long opNum;
		private final long start;
		private final String id;
		private final String address;

		Operation(String remote, String name, long seqNum) {
			remoteIP = remote;
			operationName = name;
			opNum = seqNum;
			start = System.currentTimeMillis();
			id = null;
			address = null;
		}

		public String finished() {
			long time = System.currentTimeMillis() - start;

			StringBuilder sb = new StringBuilder();
			sb.append("operation ");
			sb.append(opNum);
			sb.append(" ");
			sb.append(operationName);
			sb.append(" from ");
			sb.append(remoteIP);
			if (id != null) {
				sb.append(" by ");
				sb.append(id);
			}

			if (address != null) {
				sb.append(" from ");
				sb.append(address);
			}
			sb.append(" completed in ");
			sb.append(time);
			sb.append(" ms");
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("operation ");
			sb.append(opNum);
			sb.append(" ");
			sb.append(operationName);
			sb.append(" from ");
			sb.append(remoteIP);
			if (id != null) {
				sb.append(" by ");
				sb.append(id);
			}

			if (address != null) {
				sb.append(" from ");
				sb.append(address);
			}
			return sb.toString();
		}
	}

	private final String ifName;
	private final AtomicLong SEQ = new AtomicLong();
	private final ThreadLocal<Operation> THREAD_LOCAL = new ThreadLocal<Operation>();

	public RmiOperationHelper(String interfaceName) {
		ifName = interfaceName;
	}

	public Exception error(Exception t) {
		Operation op = THREAD_LOCAL.get();
		THREAD_LOCAL.remove();
		log.error(ifName + ": Failed " + op.finished(), t);
		return t;
	}

	public void finish() {
		Operation op = THREAD_LOCAL.get();
		THREAD_LOCAL.remove();
		
	}

	public void start(String name, String host, Object... args) throws Exception {
		Operation op = new Operation(host, name, SEQ.getAndIncrement());
		THREAD_LOCAL.set(op);
		if (args != null && args.length > 0) {
			
		}
		else {
			
		}
	}
	
}
