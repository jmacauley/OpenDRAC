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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.widgets.calendarcombobox;

import java.util.List;

import javax.swing.SpinnerListModel;

public final class RolloverSpinnerListModel extends SpinnerListModel {
	private static final long serialVersionUID = 1L;

	public RolloverSpinnerListModel(List<?> values) {
		super(values);
	}

	public RolloverSpinnerListModel(Object values[]) {
		super(values);
	}

	@Override
	public Object getNextValue() {
		return super.getNextValue() == null ? getList().get(0) : super
		    .getNextValue();
	}

	@Override
	public Object getPreviousValue() {
		return super.getPreviousValue() == null ? getList().get(
		    getList().size() - 1) : super.getPreviousValue();
	}
}
