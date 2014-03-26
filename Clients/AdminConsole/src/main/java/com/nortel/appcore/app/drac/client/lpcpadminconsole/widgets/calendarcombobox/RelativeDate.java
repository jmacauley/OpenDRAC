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

import java.util.Date;

public final class RelativeDate extends Date {
	private static final long serialVersionUID = 1L;
	private int offset;
	private int units;
	private boolean ahead;

	public RelativeDate() {
		offset = 0;
		units = 0;
		ahead = false;
		evaluate();
	}

	public RelativeDate(int theOffset, int theUnits, boolean theAhead) {
		checkUnits(theUnits);
		offset = theOffset;
		units = theUnits;
		ahead = theAhead;
		evaluate();
	}

	@Override
	public Object clone() {
		RelativeDate d = null;
		d = (RelativeDate) super.clone();
		return d;
	}

	public void evaluate() {
		evaluate(new Date());
	}

	public void evaluate(Date date) {
		long milliOffset = 1000L;
		milliOffset *= 60L;
		if (units != 0) {
			milliOffset *= 60L;
			if (units != 1) {
				milliOffset *= 24L;
				if (units != 2) {
					if (units == 3) {
						milliOffset *= 31L;
					}
					else {
						milliOffset *= 365L;
					}
				}
			}
		}
		milliOffset *= offset;
		if (!ahead) {
			milliOffset = -milliOffset;
		}
		setTime(date.getTime() + milliOffset);
	}

	public String formatRelativeDate() {
		if (offset == 0) {
			return "Now";
		}
		boolean plural = true;
		if (offset == 1) {
			plural = false;
		}
		String result = offset + " ";
		if (units == 0) {
			result = result + "Minute";
		}
		else if (units == 1) {
			result = result + "Hour";
		}
		else if (units == 2) {
			result = result + "Day";
		}
		else if (units == 3) {
			result = result + "Month";
		}
		else {
			result = result + "Year";
		}
		if (plural) {
			result = result + "s";
		}
		if (ahead) {
			result = result + " Ahead";
		}
		else {
			result = result + " Ago";
		}
		return result;
	}

	public String formatRelativeDateForTooltip() {
		String result = formatRelativeDate();
		if (offset == 0) {
			return result;
		}
		if (units == 2) {
			result = result + " (1 Day = 24 Hours)";
		}
		else if (units == 3) {
			result = result + " (1 Month = 31 Days)";
		}
		else if (units == 4) {
			result = result + " (1 Year = 365 Days)";
		}
		return result;
	}

	public boolean getAhead() {
		return ahead;
	}

	public int getOffset() {
		return offset;
	}

	public int getUnits() {
		return units;
	}

	public void setAhead(boolean ahead) {
		this.ahead = ahead;
		evaluate();
	}

	public void setOffset(int offset) {
		this.offset = offset;
		evaluate();
	}

	public void setUnits(int units) {
		checkUnits(units);
		this.units = units;
		evaluate();
	}

	@Override
	public String toString() {
		return super.toString() + " (" + formatRelativeDate() + ")";
	}

	private void checkUnits(int theUnit) {
		if (theUnit != 0 && theUnit != 1 && theUnit != 2 && theUnit != 3
		    && theUnit != 4) {
			throw new IllegalArgumentException("Unsupported unit for relative date "
			    + theUnit);
		}
	}
}
