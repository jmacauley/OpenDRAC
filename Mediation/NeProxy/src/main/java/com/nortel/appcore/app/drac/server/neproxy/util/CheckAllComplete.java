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

package com.nortel.appcore.app.drac.server.neproxy.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CheckAllComplete {
  
  INSTANCE;
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	static class SingleElement {
		public String id;
		public int totalCount;
		public int currentCount;

		public SingleElement(String nid, int total) {
			id = nid;
			totalCount = total;
		}
	}

	private final Map<String, SingleElement> groupList = new HashMap<String, SingleElement>();

	public void addAndDone(String id) {
		SingleElement aGroup = groupList.get(id);
		if (aGroup != null) {
			synchronized (aGroup) {
				aGroup.currentCount++;
				if (aGroup.currentCount >= aGroup.totalCount) {
					aGroup.notifyAll();
				}
			}
		}
	}

	public void createElement(String id, int total) {
		synchronized (groupList) {
			SingleElement result = groupList.get(id);
			if (result == null) {
				result = new SingleElement(id, total);
				groupList.put(id, result);
				log.debug("New Group: " + result);
			}
			// return result;
		}
	}

	public void iAmDone(String id) {
		
		SingleElement aGroup = groupList.get(id);
		if (aGroup != null) {
			synchronized (aGroup) {
				aGroup.currentCount++;
				if (aGroup.currentCount >= aGroup.totalCount) {
					log.debug("All done for " + aGroup.id);
					aGroup.notifyAll();
				}
			}
		}
	}

	public void waitFor(String id) {
		/**
		 * @TODO WP June 2009: This seams to have some bugs. We fetch a
		 *       SingleElement from the groupList (but cast it to an Object and
		 *       later try groupList.remove(aGroup) which should never work as we
		 *       want a string not an object for a key.
		 */

		Object aGroup = groupList.get(id);
		if (aGroup != null) {
			synchronized (aGroup) {
				SingleElement temp = (SingleElement) aGroup;
				if (temp.currentCount < temp.totalCount) {
					try {
						
						long timeToWait = Integer.parseInt(System.getProperty(
						    "maxTimeForCallCreate", "30000"));
						temp.wait(timeToWait);
						groupList.remove(aGroup);
					}
					catch (Exception e) {
						log.error("Unexpected error", e);
					}
				}
				else {
					log.debug("no need to wait for: " + temp.id);
				}
			}
		}
	}
}