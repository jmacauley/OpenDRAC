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

public class OpticalUtility {

	public static enum OpticalPortType {
		UNKNOWN("unknown", 0, 0), OC3(TYPE_OC3, PORTS_OC3, STS3RATE), OC12(
		    TYPE_OC12, PORTS_OC12, STS12RATE), OC48(TYPE_OC48, PORTS_OC48,
		    STS48RATE), OC192(TYPE_OC192, PORTS_OC192, STS192RATE), STM1(TYPE_STM1,
		    PORTS_STM1, STS3RATE), STM4(TYPE_STM4, PORTS_STM4, STS12RATE), STM16(
		    TYPE_STM16, PORTS_STM16, STS48RATE), STM64(TYPE_STM64, PORTS_STM64,
		    STS192RATE);

		private int channels = 0;
		private int rate = 0;
		private String type = "";

		OpticalPortType(String type, int channels, int rate) {
			this.channels = channels;
			this.type = type;
			this.rate = rate;
		}

		public int getChannels() {
			return channels;
		}

		public int getRate() {
			return rate;
		}

		public String getType() {
			return type;
		}
	}

	public static final String TYPE_OC3 = "OC3";
	public static final String TYPE_OC12 = "OC12";
	public static final String TYPE_OC48 = "OC48";
	public static final String TYPE_OC192 = "OC192";
	public static final String TYPE_STM1 = "STM1";
	public static final String TYPE_STM4 = "STM4";
	public static final String TYPE_STM16 = "STM16";
	public static final String TYPE_STM64 = "STM64";
	public static final int PORTS_OC3 = 3;
	public static final int PORTS_OC12 = 12;
	public static final int PORTS_OC48 = 48;
	public static final int PORTS_OC192 = 192;
	public static final int PORTS_STM1 = 1;
	public static final int PORTS_STM4 = 4;
	public static final int PORTS_STM16 = 16;
	public static final int PORTS_STM64 = 64;
	public static final int STS3RATE = 150;

	public static final int STS12RATE = 600;
	public static final int STS48RATE = 2400;
	public static final int STS192RATE = 9600;

	public static OpticalPortType lookupOptical(String type) {
		if (type != null) {
			if (type.equals(OpticalUtility.TYPE_OC3)) {
				return OpticalUtility.OpticalPortType.OC3;
			}
			else if (type.equals(TYPE_OC12)) {
				return OpticalUtility.OpticalPortType.OC12;
			}
			else if (type.equals(TYPE_OC48)) {
				return OpticalUtility.OpticalPortType.OC48;
			}
			else if (type.equals(TYPE_OC192)) {
				return OpticalUtility.OpticalPortType.OC192;
			}
			else if (type.equals(TYPE_STM1)) {
				return OpticalUtility.OpticalPortType.STM1;
			}
			else if (type.equals(TYPE_STM4)) {
				return OpticalUtility.OpticalPortType.STM4;
			}
			else if (type.equals(TYPE_STM16)) {
				return OpticalUtility.OpticalPortType.STM16;
			}
			else if (type.equals(TYPE_STM64)) {
				return OpticalUtility.OpticalPortType.STM64;
			}
		}
		return OpticalUtility.OpticalPortType.UNKNOWN;
	}
}
