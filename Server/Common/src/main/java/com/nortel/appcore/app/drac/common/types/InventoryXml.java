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

public final class InventoryXml {
	private final String shelf;
	private final String slot;
	private final String subSlot;
	private final String pecCode;
	private final String componentType;
	private final String componentAid;

	public InventoryXml(String aid, String type, String pec, String shelfStr,
	    String slotStr) {
		subSlot = "0";
		componentAid = aid;
		componentType = type;
		shelf = shelfStr;
		slot = slotStr;
		pecCode = pec;
	}

	/**
	 * @return the componentAid
	 */
	public String getComponentAid() {
		return componentAid;
	}

	/**
	 * @return the componentType
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * @return the pecCode
	 */
	public String getPecCode() {
		return pecCode;
	}

	/**
	 * @return the shelf
	 */
	public String getShelf() {
		return shelf;
	}

	/**
	 * @return the slot
	 */
	public String getSlot() {
		return slot;
	}

	/**
	 * @return the subSlot
	 */
	public String getSubSlot() {
		return subSlot;
	}

	@Override
	public String toString() {
		return "<inventoryInstance " + "componentAid=\"" + componentAid + "\" "
		    + "componentType=\"" + componentType + "\" " + "pecCode=\"" + pecCode
		    + "\" " + "shelf=\"" + shelf + "\" " + "slot=\"" + slot + "\"/>";
	}
}
