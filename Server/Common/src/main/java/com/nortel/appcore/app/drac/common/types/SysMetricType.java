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

package com.nortel.appcore.app.drac.common.types;

import java.io.Serializable;

public final class SysMetricType implements Serializable {
	private static final long serialVersionUID = 1;

	/**
	 * configureOffset: is the initial value when DRAC is first provisioned. Its
	 * value can be changed by the system admin at runtime.
	 */
	private int configureOffset;

	/**
	 * averageOffset: this value is calculated based on the actual number of
	 * schedule occurences had been processed by DRAC.
	 */
	// private int averageOffset;
	/**
	 * actualMaxOffset: this value represents the largest amount of time that DRAC
	 * took to process one occurrence over the number of "processedOccurrence"
	 */
	// private int actualMaxOffset;
	/**
	 * actualMinOffset: this value represents the smallest amount of time that
	 * DRAC took to process one occurrence over the number of
	 * "processedOccurrence"
	 */
	// private int actualMinOffset;
	/**
	 * processedOccurrence: this value represents the total number of occurrences
	 * that DRAC had processed since it has been in service.
	 */
	// private int processedOccurrence;
	public SysMetricType(int offset) {
		configureOffset = offset;
	}

	/**
	 * The following variables are used to present the system overhead associated
	 * with each occurrence (i.e. service) of the schedule. It affects the actual
	 * startTime (add to) and endTime (subtract from) of the schedule. All values
	 * are in milliseconds.
	 */

	// public int getActualMaxOffset()
	// {
	// return actualMaxOffset;
	// }
	//
	// public int getActualMinOffset()
	// {
	// return actualMinOffset;
	// }
	//
	// public int getAverageOffset()
	// {
	// return averageOffset;
	// }
	public int getConfigureOffset() {
		return configureOffset;
	}

	// public int getProcessedOccurrence()
	// {
	// return processedOccurrence;
	// }
	//
	// public void setActualMaxOffset(int actualMaxOffset)
	// {
	// this.actualMaxOffset = actualMaxOffset;
	// }
	//
	// public void setActualMinOffset(int actualMinOffset)
	// {
	// this.actualMinOffset = actualMinOffset;
	// }
	//
	// public void setAverageOffset(int averageOffset)
	// {
	// this.averageOffset = averageOffset;
	// }
	//
	// public void setConfigureOffset(int configureOffset)
	// {
	// this.configureOffset = configureOffset;
	// }
	//
	// public void setProcessedOccurrence(int processedOccurrence)
	// {
	// this.processedOccurrence = processedOccurrence;
	// }

}
