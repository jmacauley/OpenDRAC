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

package com.nortel.appcore.app.drac.server.webserver.struts.general.form;

import org.apache.struts.validator.ValidatorActionForm;

public final class ServerSettingsForm extends ValidatorActionForm {
	private static final long serialVersionUID = 1L;
	private int confirmationTimeout;
	private int oldConfirmationTimeout;
	private int scheduleOffset;
	private int oldScheduleOffset;

	public int getConfirmationTimeout() {
		return confirmationTimeout;
	}

	public int getOldConfirmationTimeout() {
		return oldConfirmationTimeout;
	}

	public int getOldScheduleOffset() {
		return oldScheduleOffset;
	}

	public int getScheduleOffset() {
		return scheduleOffset;
	}

	public void setConfirmationTimeout(int confirmationTimeout) {
		this.confirmationTimeout = confirmationTimeout;
	}

	public void setOldConfirmationTimeout(int oldConfirmationTimeout) {
		this.oldConfirmationTimeout = oldConfirmationTimeout;
	}

	public void setOldScheduleOffset(int oldScheduleOffset) {
		this.oldScheduleOffset = oldScheduleOffset;
	}

	public void setScheduleOffset(int scheduleOffset) {
		this.scheduleOffset = scheduleOffset;
	}

}
