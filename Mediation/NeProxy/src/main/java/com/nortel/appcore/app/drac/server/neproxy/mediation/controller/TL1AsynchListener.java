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

package com.nortel.appcore.app.drac.server.neproxy.mediation.controller;

import java.util.List;
import java.util.Map;

/**
 * Created on Sep 2, 2005
 * 
 * @author nguyentd
 */
public interface TL1AsynchListener {
	// int UNKNOWN = 0;
	// int SUCCESS = TL1ResponseMessage.SUCCESS;
	// int TIME_OUT = TL1ResponseMessage.TIME_OUT;
	// int RETRY_LATER = TL1ResponseMessage.RETRY_LATER;
	// int PROCESSING_ERROR = TL1ResponseMessage.PROCESSING_ERROR;
	// int REQUEST_ERROR = TL1ResponseMessage.REQUEST_ERROR;
	// int IN_PROGRESS = TL1ResponseMessage.IN_PROGRESS;
	// int PURE_ASYNCH = 0;
	// int BOUNDED_ASYNCH = 0;

	int getType();

	void handleComplete();

	void receiveMessage(List<Map<String, String>> msg);

	// public void receiveAlarmMessage(List msg);
	void setCompletionCode(int value);
}