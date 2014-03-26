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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class enumerates all the supported TL1 command codes we use/require to
 * function. This is mostly for documentation and understanding what commands
 * are being used within DRAC. This enumeration should cover all releases/NE
 * types we support but is intended to narrow down the vast sea of TL1 commands
 * to those we know we are interested in. Previously we would build the TL1
 * command code as a string and hope the corresponding TL1 parser/stub was
 * present to avoid run time errors.
 *
 * @author pitman
 */
public final class Tl1CommandCode {
  private static final Logger log = LoggerFactory.getLogger(Tl1CommandCode.class);
	private static final Map<String, Tl1CommandCode> SUPPORTED = new HashMap<String, Tl1CommandCode>();
	private final String code;

	public static final Tl1CommandCode ACT_USER = new Tl1CommandCode("ACT_USER");
	public static final Tl1CommandCode RTRV_HDR = new Tl1CommandCode("RTRV_HDR");
	public static final Tl1CommandCode CANC_USER = new Tl1CommandCode("CANC_USER");
	public static final Tl1CommandCode RTRV_NETYPE = new Tl1CommandCode("RTRV_NETYPE");
	public static final Tl1CommandCode RTRV_SW_VER = new Tl1CommandCode("RTRV_SW_VER");
	public static final Tl1CommandCode RTRV_NE = new Tl1CommandCode("RTRV_NE");

	//
	public static final Tl1CommandCode RMV_ETH = new Tl1CommandCode("RMV_ETH");
	public static final Tl1CommandCode ED_ETH = new Tl1CommandCode("ED_ETH");
	public static final Tl1CommandCode RST_ETH = new Tl1CommandCode("RST_ETH");
	//
	public static final Tl1CommandCode ED_ADJ_TX = new Tl1CommandCode("ED_ADJ_TX");
	public static final Tl1CommandCode ED_FAC = new Tl1CommandCode("ED_FAC");
	//
	public static final Tl1CommandCode DLT_CRS = new Tl1CommandCode("DLT_CRS");
	// command!
	public static final Tl1CommandCode DLT_CRS_OCH = new Tl1CommandCode("DLT_CRS_OCH");
	public static final Tl1CommandCode DLT_CRS_STS1 = new Tl1CommandCode("DLT_CRS_STS1");
	public static final Tl1CommandCode DLT_CRS_STS3C = new Tl1CommandCode("DLT_CRS_STS3C");
	public static final Tl1CommandCode DLT_CRS_STS6C = new Tl1CommandCode("DLT_CRS_STS6C");
	public static final Tl1CommandCode DLT_CRS_STS12C = new Tl1CommandCode("DLT_CRS_STS12C");
	public static final Tl1CommandCode DLT_CRS_STS24C = new Tl1CommandCode("DLT_CRS_STS24C");
	public static final Tl1CommandCode DLT_CRS_STS48C = new Tl1CommandCode("DLT_CRS_STS48C");
	public static final Tl1CommandCode DLT_CRS_STS192C = new Tl1CommandCode("DLT_CRS_STS192C");

	// GGL2
	public static final Tl1CommandCode RMV_VCS = new Tl1CommandCode("RMV_VCS");
	public static final Tl1CommandCode DLT_VCS = new Tl1CommandCode("DLT_VCS");
	public static final Tl1CommandCode RMV_VCE = new Tl1CommandCode("RMV_VCE");
	public static final Tl1CommandCode DLT_VCE = new Tl1CommandCode("DLT_VCE");
	public static final Tl1CommandCode DLT_VCEMAP = new Tl1CommandCode("DLT_VCEMAP");
	public static final Tl1CommandCode RMV_WAN = new Tl1CommandCode("RMV_WAN");
	public static final Tl1CommandCode DLT_WAN = new Tl1CommandCode("DLT_WAN");
	public static final Tl1CommandCode PREP_ADD_CONNECTIONS = new Tl1CommandCode("PREP-ADD-CONNECTIONS");
	public static final Tl1CommandCode POST_DLT_CONNECTIONS = new Tl1CommandCode("POST-DLT-CONNECTIONS");
	public static final Tl1CommandCode RTRV_ALL_ETH_WAN = new Tl1CommandCode("RTRV-ALL-ETH-WAN");

	// command!
	public static final Tl1CommandCode ENT_CRS = new Tl1CommandCode("ENT_CRS");
	public static final Tl1CommandCode ENT_CRS_OCH = new Tl1CommandCode("ENT_CRS_OCH");
	public static final Tl1CommandCode ENT_CRS_STS1 = new Tl1CommandCode("ENT_CRS_STS1");
	public static final Tl1CommandCode ENT_CRS_STS3C = new Tl1CommandCode("ENT_CRS_STS3C");
	public static final Tl1CommandCode ENT_CRS_STS6C = new Tl1CommandCode("ENT_CRS_STS6C");
	public static final Tl1CommandCode ENT_CRS_STS12C = new Tl1CommandCode("ENT_CRS_STS12C");
	public static final Tl1CommandCode ENT_CRS_STS24C = new Tl1CommandCode("ENT_CRS_STS24C");
	public static final Tl1CommandCode ENT_CRS_STS48C = new Tl1CommandCode("ENT_CRS_STS48C");
	public static final Tl1CommandCode ENT_CRS_STS192C = new Tl1CommandCode("ENT_CRS_STS192C");

	// GGL2
	public static final Tl1CommandCode ENT_VCS = new Tl1CommandCode("ENT_VCS");
	public static final Tl1CommandCode ENT_VCE = new Tl1CommandCode("ENT_VCE");
	public static final Tl1CommandCode ENT_VCEMAP = new Tl1CommandCode("ENT_VCEMAP");
	public static final Tl1CommandCode ENT_WAN = new Tl1CommandCode("ENT_WAN");

	//
	public static final Tl1CommandCode RTRV_CRS_ALL = new Tl1CommandCode("RTRV_CRS_ALL");
	//
	public static final Tl1CommandCode ENT_CALL = new Tl1CommandCode("ENT_CALL");
	public static final Tl1CommandCode DLT_CALL = new Tl1CommandCode("DLT_CALL");
	//
	public static final Tl1CommandCode ALW_MSG_BROADCAST = new Tl1CommandCode("ALW_MSG_BROADCAST");
	//
	public static final Tl1CommandCode RTRV_TOPO_SWT = new Tl1CommandCode("RTRV_TOPO_SWT");
	public static final Tl1CommandCode RTRV_ADJ_LINE = new Tl1CommandCode("RTRV_ADJ_LINE");
	public static final Tl1CommandCode RTRV_LINE = new Tl1CommandCode("RTRV_LINE");
	public static final Tl1CommandCode RTRV_ADJ_TX = new Tl1CommandCode("RTRV_ADJ_TX");
	public static final Tl1CommandCode RTRV_INVENTORY = new Tl1CommandCode("RTRV_INVENTORY");
	public static final Tl1CommandCode RTRV_FFP_OC192 = new Tl1CommandCode("RTRV_FFP_OC192");
	public static final Tl1CommandCode RTRV_AD_ALL = new Tl1CommandCode("RTRV_AD_ALL");
	// hdx?
	public static final Tl1CommandCode RTRV_AD = new Tl1CommandCode("RTRV_AD");

	public static final Tl1CommandCode RTRV_FFP_ALL = new Tl1CommandCode("RTRV_FFP_ALL");
	// hdx?
	public static final Tl1CommandCode RTRV_FFP = new Tl1CommandCode("RTRV_FFP");
	//
	public static final Tl1CommandCode RTRV_OC3 = new Tl1CommandCode("RTRV_OC3");
	public static final Tl1CommandCode RTRV_OC12 = new Tl1CommandCode("RTRV_OC12");
	public static final Tl1CommandCode RTRV_OC48 = new Tl1CommandCode("RTRV_OC48");
	public static final Tl1CommandCode RTRV_OC192 = new Tl1CommandCode("RTRV_OC192");
	//
	public static final Tl1CommandCode RTRV_CRS = new Tl1CommandCode("RTRV_CRS");
	public static final Tl1CommandCode RTRV_ALM = new Tl1CommandCode("RTRV_ALM");
	public static final Tl1CommandCode RTRV_ALM_ALL = new Tl1CommandCode("RTRV_ALM_ALL");
	public static final Tl1CommandCode RTRV_FAC = new Tl1CommandCode("RTRV_FAC");
	public static final Tl1CommandCode RTRV_WAN = new Tl1CommandCode("RTRV_WAN");
	public static final Tl1CommandCode RTRV_RTG_INFO = new Tl1CommandCode("RTRV_RTG_INFO");
	public static final Tl1CommandCode RTRV_SHELF = new Tl1CommandCode("RTRV_SHELF");
	public static final Tl1CommandCode RTRV_ADJ = new Tl1CommandCode("RTRV_ADJ");
	public static final Tl1CommandCode RTRV_DOC_CH = new Tl1CommandCode("RTRV_DOC_CH");
	public static final Tl1CommandCode RTRV_DOC = new Tl1CommandCode("RTRV_DOC");
	public static final Tl1CommandCode RTRV_SYS = new Tl1CommandCode("RTRV_SYS");
	public static final Tl1CommandCode RTRV_BLSRMAP = new Tl1CommandCode("RTRV_BLSRMAP");
	// GGL2
	public static final Tl1CommandCode RTRV_ETH = new Tl1CommandCode("RTRV_ETH");
	public static final Tl1CommandCode RTRV_ETH10G = new Tl1CommandCode("RTRV_ETH10G");
	public static final Tl1CommandCode RTRV_ETH100 = new Tl1CommandCode("RTRV_ETH100");
	public static final Tl1CommandCode RTRV_VCS = new Tl1CommandCode("RTRV_VCS");
	public static final Tl1CommandCode RTRV_VCE = new Tl1CommandCode("RTRV_VCE");
	public static final Tl1CommandCode RTRV_VCEMAP = new Tl1CommandCode("RTRV_VCEMAP");
	public static final Tl1CommandCode RTRV_EQPT = new Tl1CommandCode("RTRV_EQPT");
	//
	public static final Tl1CommandCode REPT_EVT = new Tl1CommandCode("REPT_EVT");
	public static final Tl1CommandCode REPT_EVT_WAN = new Tl1CommandCode("REPT_EVT_WAN");
	public static final Tl1CommandCode REPT_EVT_OC48 = new Tl1CommandCode("REPT_EVT_OC48");
	public static final Tl1CommandCode REPT_EVT_OC12 = new Tl1CommandCode("REPT_EVT_OC12");
	public static final Tl1CommandCode REPT_EVT_LOG = new Tl1CommandCode("REPT_EVT_LOG");
	public static final Tl1CommandCode REPT_EVT_ETH = new Tl1CommandCode("REPT_EVT_ETH");
	public static final Tl1CommandCode REPT_CRS_OCH = new Tl1CommandCode("REPT_CRS_OCH");
	public static final Tl1CommandCode REPT_DBCHG = new Tl1CommandCode("REPT_DBCHG");
	public static final Tl1CommandCode REPT_CALL = new Tl1CommandCode("REPT_CALL");
	public static final Tl1CommandCode REPT_AD = new Tl1CommandCode("REPT_AD");
	public static final Tl1CommandCode REPT_CRS_PLD = new Tl1CommandCode("REPT_CRS_PLD");
	public static final Tl1CommandCode REPT_LINE = new Tl1CommandCode("REPT_LINE");
	public static final Tl1CommandCode RTRV_CALL_ALL = new Tl1CommandCode("RTRV_CALL_ALL");
	public static final Tl1CommandCode RTRV_OTM2 = new Tl1CommandCode("RTRV_OTM2");
	public static final Tl1CommandCode REPT_ALM = new Tl1CommandCode("REPT_ALM");
	public static final Tl1CommandCode REPT_ALM_SECU = new Tl1CommandCode("REPT_ALM_SECU");
    public static final Tl1CommandCode REPT_PROTNSW = new Tl1CommandCode("REPT_PROTNSW");

	private Tl1CommandCode(String key) {
		synchronized (SUPPORTED) {
			SUPPORTED.put(key, this);
			code = key.replace('_', '-');
			SUPPORTED.put(code, this);
		}
	}

	public static Tl1CommandCode fromString(String c)
	    throws IllegalArgumentException {
		synchronized (SUPPORTED) {
			Tl1CommandCode r = SUPPORTED.get(c);
			if (r == null) {
				if (c.startsWith("REPT-")) {
					/*
					 * We really want all the TL1 command codes we use to be define here,
					 * but when it comes to the report side,their are too too many to deal
					 * with, so we tolerate unknown rept events.
					 */

					return new Tl1CommandCode(c);
				}
				log.error("Cannot map '" + c + "' to Tl1CommandCode ");
				throw new IllegalArgumentException("Failed to map '" + c
				    + "' to Tl1CommandCode");
			}

			return r;
		}
	}

	@Override
	public String toString() {
		return code;
	}
}
