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

public enum Layer {
	LAYER0("layer0"), //
	LAYER1("layer1"), //
	LAYER2("layer2"), //

	// Compound layers for retrieval filters
	LAYER_ALL("layer_all"), //
	LAYER1_LAYER2("layer1_2");

	private final String layerKey;

	private Layer(String layerKey) {
		this.layerKey = layerKey;
	}

	public static Layer toEnum(String s) {
		if (LAYER0.toString().equals(s)) {
			return LAYER0;
		}
		else if (LAYER1.toString().equals(s)) {
			return LAYER1;
		}
		else if (LAYER2.toString().equals(s)) {
			return LAYER2;
		}
		else if (LAYER_ALL.toString().equals(s)) {
			return LAYER_ALL;
		}
		else if (LAYER1_LAYER2.toString().equals(s)) {
			return LAYER1_LAYER2;
		}

		return null;
	}

	@Override
	public String toString() {
		return layerKey;
	}
}
