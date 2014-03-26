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

package com.nortel.appcore.app.drac.server.lpcp.common;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;

public final class Utility {

  private static final Logger log = LoggerFactory.getLogger(Utility.class);
	public static final int STS1 = 0;
	public static final int STS3C = 1;
	// Note on 300mbs:
	// The OME does not currently support the STS6C concatenation rate.
	// public static final int STS6C = 2;
	public static final int STS12C = 3;
	public static final int STS24C = 4;
	public static final int STS48C = 5;
	public static final int STS192C = 6;

	public static final String NONE_STR_RATE = "NONE";

	public static final String STS1_STR_RATE = "STS1";
	public static final String STS3C_STR_RATE = "STS3C";
	// public static final String STS6C_STR_RATE = "STS6C";
	public static final String STS12C_STR_RATE = "STS12C";
	public static final String STS24C_STR_RATE = "STS24C";
	public static final String STS48C_STR_RATE = "STS48C";
	public static final String STS192C_STR_RATE = "STS192C";
	public static final String VC3_STR_RATE = "VC3";
	public static final String VC4_STR_RATE = "VC4";
	public static final String VC4_2C_STR_RATE = "VC4-2C";
	public static final String VC4_4C_STR_RATE = "VC4-4C";
	public static final String VC4_8C_STR_RATE = "VC4-8C";
	public static final String VC4_16C_STR_RATE = "VC4-16C";
	public static final String VC4_64C_STR_RATE = "VC4-64C";

	private static final int MAX_BYTES_TO_PROCESS = 4;
	private static final int MIN_IP_BYTES = 4;

	private Utility() {
		super();
	}

	public static String convertMB2STS(String rate) {
		String convertedRate = null;
		int intRate = -1;
		int baseRate = 50; // STS1
		int iConvertedRate = -1;

		try {
			intRate = Integer.parseInt(rate);
			if (intRate % baseRate == 0) {
				
				iConvertedRate = intRate / baseRate;
			}
			else {
				iConvertedRate = (int) Math.floor(intRate / baseRate) + 1;
			}
			
			if (iConvertedRate <= 1) {
				convertedRate = "STS1";
			}
			else if (iConvertedRate <= 3) {
				convertedRate = "STS3C";
			}
			// else if (iConvertedRate <= 6)
			// {
			// convertedRate = "STS6C";
			// }
			else if (iConvertedRate <= 12) {
				convertedRate = "STS12C";
			}
			else if (iConvertedRate <= 24) {
				convertedRate = "STS24C";
			}
			else if (iConvertedRate <= 48) {
				convertedRate = "STS48C";
			}
			else if (iConvertedRate <= 192) {
				convertedRate = "STS192C";
			}
			
//			// 10GB
//			else if (iConvertedRate <= 200) {
//				convertedRate = "?";
//			}
			
			// 40GB
			else if (iConvertedRate <= 800) {
				convertedRate = "STS768C";
			}
			
			else {
				log.error("Utility::convertMB2STS::FATAL ERROR: Rate: " + rate
				    + " is greater than max. supported rate of 38,486,016, ("+iConvertedRate+")");
			}
		}
		catch (Exception e) {
			log.error(
			    "convertMB2STS::FATAL ERROR: Could not convert input rate: "
			        + rate, e);
		}

		log.debug("convertMB2STS::convertMB2STS::rate: " + rate
		    + " converted to: " + convertedRate);

		return convertedRate;
	}

	// public static String convertETH2WAN(String aid)
	// {
	// String wan = aid;
	// if (aid != null)
	// {
	// if (aid.startsWith("ETH"))
	// {
	// wan = "WAN" + aid.substring(3, aid.length());
	// }
	// }
	//
	// return wan;
	// }

	// Convert the input rate into a suitable STS rate
	// Suitable means the smallest STS rate that will fit the required number of
	// Mb/s
	// The incoming rate MUST be in Mb/s
	/*
	 * Mapping table: Mb/s SDH SONET 1.5 VC11 VT1.5 2 VC12 VT2 50 VC3 STS1 150 VC4
	 * STS3c 600 VC4-4c STS12c 1200 VC4-8c STS24c 2400 VC4-16c STS48c 9600 VC4-64c
	 * STS192c
	 */
	public static int convertStringRateToInt(String rate) {

		int trackerRateInt = -1;
		int inputRate = -1;

		try {
			inputRate = Integer.parseInt(rate);
			
		}
		catch (Exception e) {
			log.error("converting to int " + rate, e);
		}

		// Note: Sub-optimal use of bandwidth is possible using this mapping...
		if (inputRate > 50) {
			if (inputRate > 150) {
				// if (inputRate > 300)
				// {
				if (inputRate > 600) {
					if (inputRate > 1200) {
						if (inputRate > 2400) {
							if (inputRate <= 9600) {
								trackerRateInt = STS192C;
							}
							else {
								log.error("Error: Rate is greater than maximum supported. (STS192C/STM64)");
							}
						}
						else {
							trackerRateInt = STS48C;
						}
					}
					else {
						trackerRateInt = STS24C;
					}
				}
				else {
					trackerRateInt = STS12C;
				}
				// }
				// else
				// {
				// trackerRateInt = STS6C;
				// }
			}
			else {
				trackerRateInt = STS3C;
			}
		}
		else {
			trackerRateInt = STS1;
		}

		/*
		 * if ( mode == SONET ) { if ( inputRate > 1 ) { if ( inputRate > 3 ) { if (
		 * inputRate > 6 ) { if ( inputRate > 12 ) { if ( inputRate > 48 ) { if (
		 * inputRate <= 192 ) { trackerRateInt = STS192C; } } else { trackerRateInt
		 * = STS48C; } } else { trackerRateInt = STS12C; } } else { trackerRateInt =
		 * STS6C; } } else { trackerRateInt = STS3C; } } else { trackerRateInt =
		 * STS1; } }
		 */

		/*
		 * if ( "STS1".equalsIgnoreCase( rate ) ) { trackerRateInt = STS1; } else if
		 * ( "STS3C".equalsIgnoreCase( rate ) ) { trackerRateInt = STS3C; } else if
		 * ( "STS6C".equalsIgnoreCase( rate ) ) { trackerRateInt = STS6C; } else if
		 * ( "STS12C".equalsIgnoreCase( rate ) ) { trackerRateInt = STS12C; } else
		 * if ( "STS48C".equalsIgnoreCase( rate ) ) { trackerRateInt = STS48C; }
		 * else if ( "STS192C".equalsIgnoreCase( rate ) ) { trackerRateInt =
		 * STS192C; } else if ( "STM1".equalsIgnoreCase( rate ) ) { trackerRateInt =
		 * STM1; } else if ( "STM2".equalsIgnoreCase( rate ) ) { trackerRateInt =
		 * STM2; } else if ( "STM4".equalsIgnoreCase( rate ) ) { trackerRateInt =
		 * STM4; } else if ( "STM8".equalsIgnoreCase( rate ) ) { trackerRateInt =
		 * STM8; } else if ( "STM16".equalsIgnoreCase( rate ) ) { trackerRateInt =
		 * STM16; } else if ( "STM64".equalsIgnoreCase( rate ) ) { trackerRateInt =
		 * STM64; }
		 */

		return trackerRateInt;

	}

	public static int convertStringRateToRateIdxInt(String inputRate) {

		int trackerRateInt = -1;

		if (STS1_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.STS1;
		}
		if (STS3C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.STS3C;
		}
		// if (STS6C_STR_RATE.equals(inputRate))
		// {
		// trackerRateInt = BitBandwidthTrackerI.STS6C;
		// }
		if (STS12C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.STS12C;
		}
		if (STS24C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.STS24C;
		}
		if (STS48C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.STS48C;
		}
		if (STS192C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.STS192C;
		}

		if (VC3_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.VC3;
		}
		if (VC4_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.VC4;
		}
		if (VC4_2C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.VC4_2C;
		}
		if (VC4_4C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.VC4_4C;
		}
		if (VC4_8C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.VC4_8C;
		}
		if (VC4_16C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.VC4_16C;
		}
		if (VC4_64C_STR_RATE.equals(inputRate)) {
			trackerRateInt = LpcpFacility.VC4_64C;
		}

		if (trackerRateInt == -1) {
			log.error("Unable to convert tracker rate '" + inputRate
			    + "' into a rate, returning -1!");
		}

		return trackerRateInt;

	}

	public static String convertSTS2Mb(String stsRate) {
		String mbRate = stsRate;
		if (STS1_STR_RATE.equalsIgnoreCase(stsRate)) {
			mbRate = "50";
		}
		else if (STS3C_STR_RATE.equalsIgnoreCase(stsRate)) {
			mbRate = "150";
		}
		// else if (STS6C_STR_RATE.equalsIgnoreCase(stsRate))
		// {
		// mbRate = "300";
		// }
		else if (STS12C_STR_RATE.equalsIgnoreCase(stsRate)) {
			mbRate = "600";
		}
		else if (STS24C_STR_RATE.equalsIgnoreCase(stsRate)) {
			mbRate = "1200";
		}
		else if (STS48C_STR_RATE.equalsIgnoreCase(stsRate)) {
			mbRate = "2400";
		}
		else if (STS192C_STR_RATE.equalsIgnoreCase(stsRate)) {
			mbRate = "9600";
		}
		else if (NONE_STR_RATE.equalsIgnoreCase(stsRate)) {
			mbRate = "150";
		}
		return mbRate;
	}

	/*
	 * deZeroAid
	 * 
	 * @param <code>String</code> aid - An AID taken from an AD tag that contains
	 * zeroes ie OC192-001-005-001
	 * 
	 * @return <code>String</code> - the input AID with zeroes removed. Ex:
	 * OC192-001-005-001 -> OC192-1-5-1
	 */
	public static String deZeroAID(String aid) {
		String dezeroedAID = aid;
		String components[];

		if (aid == null || "".equals(aid)) {
			return aid;
		}

		components = aid.split("-");
		// Note: components[0] will contain the entity type ex OC192 so it can be
		// ignored
		for (int i = 1; i < components.length; i++) {
			try {
				components[i] = Integer.toString(Integer.parseInt(components[i]));
			}
			catch (Exception e) {
				log.debug("Failed to parse aid portion to integer, can ignore? " + aid
				    + " component " + components[i]);
			}
		}

		if (components.length == 4) {
			dezeroedAID = components[0] + "-" + components[1] + "-" + components[2]
			    + "-" + components[3];
		}
		else {
			log.error(
			    "Utility::deZeroAID::AID does not contain 4 components.  Ignored aid <"
			        + aid + ">", new Exception("StackTrace back"));
		}
		return dezeroedAID;
	}

	// public static String convertWAN2ETH(String aid)
	// {
	// String eth = aid;
	// if (aid != null)
	// {
	// if (aid.startsWith("WAN"))
	// {
	// try
	// {
	// eth = "ETH" + aid.substring(3, aid.length());
	// }
	// catch (Exception e)
	// {
	// }
	// }
	// }
	// return eth;
	// }

	public static String getCombinedSRLG(String srlgSource, String srlgDest) {
		StringBuilder combinedSRLG = new StringBuilder();
		String[] srcDestArray = { srlgSource, srlgDest };
		String[] srlgArray;
		String curSRLG = null;

		try {
			for (String element : srcDestArray) {
				curSRLG = element;
				if (curSRLG != null) {
					srlgArray = curSRLG.split(",");
					for (String element2 : srlgArray) {
						combinedSRLG.append(element2 + ",");
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Exception caught in Utility::getCombinedSRLG: ", e);
		}

		return combinedSRLG.toString();
	}

	

	public static String getNormalisedAID(String aid, NeType neType) {
		// AIDs for OME: TYPE-SHELF-SLOT-PORT,
		// non-normalised AIDs for connections include channel number ex:
		// TYPE-SHELF-SLOT-PORT-CHANNEL
		// AIDs for HDX: TYPE-SHELF-SLOT-SUBSLOT-PORT-SIGNAL,
		// non-normalised AIDs for connections include channel number ex:
		// TYPE-SHELF-SLOT-SUBSLOT-PORT-SIGNAL-CHANNEL
		// AIDs for CPL: TYPE-SHELF-SLOT-PORT-CHANNEL
		String normalisedAID = aid;

		if (NeType.OME.equals(neType) || NeType.OME5.equals(neType)
		    || NeType.OME6.equals(neType) || NeType.OME7.equals(neType) || NeType.OME8.equals(neType) || NeType.OME9.equals(neType)) {
			if (aid.split("-").length > 4) { // Non-normalised AID including channel
				log.debug("Detected non-normalised OME AID: " + aid
				    + ", stripping off channel number");
				// Strip off channel number
				normalisedAID = aid.substring(0, aid.lastIndexOf('-'));
				
			}
			else {
				normalisedAID = aid;
				
			}
		}
		else if (NeType.HDX.equals(neType)) {
			if (aid.split("-").length > 6) { // Non-normalised AID including channel
				log.debug("Detected non-normalised HDX AID: " + aid
				    + ", stripping off channel number");
				// Strip off channel number
				normalisedAID = aid.substring(0, aid.lastIndexOf('-'));
				
			}
			else {
				normalisedAID = aid;
				
			}
		}
		else if (NeType.CPL.equals(neType)) {
			
			if (aid.split("-").length > 4) { // Non-normalised AID including channel
				log.debug("Detected non-normalised CPL AID: " + aid
				    + ", stripping off channel number");
				// Strip off channel number
				normalisedAID = aid.substring(0, aid.lastIndexOf('-'));
				
			}
			else {
				normalisedAID = aid;
				
			}

		}
		else if (NeType.PP8600.equals(neType)) {
			
		}
		else {
			log.error("UNRECOGNISED NETYPE: " + neType
			    + " AID processing skipped for: " + aid);
		}

		return normalisedAID;
	}

	/*
	 * This method will hash the last 4 octets of an IPv4/IPv6 address in dotted
	 * decimal notation in reverse order.
	 */
	public static String getReverseIpHashFromIp(String ip) {
		String[] ipParts;
		StringBuilder result = new StringBuilder();
		BigInteger converter = null;
		

		if (ip != null) {

			ipParts = ip.split("\\.");
			
			if (ipParts.length >= MIN_IP_BYTES) {
				try {
					for (int i = ipParts.length - 1; i >= ipParts.length
					    - MAX_BYTES_TO_PROCESS; i--) {
						converter = new BigInteger(ipParts[i]);
						if (converter.toString(16).length() < 2) {
							result.append("0");
						}
						result.append(converter.toString(16));
					}
				}
				catch (Exception e) {
					log.error("Exception decoding hash: ", e);
				}
			}
		}
		
		if(result.length() < 7){
			try {
				result.append(getReverseIpHashFromIp(InetAddress.getLocalHost().getHostAddress()));
			} 
			catch (UnknownHostException e) {
				log.error("Error: ", e);
			}
		}
		return result.toString();
	}

	public static int getVCATIterations(String wanRate, String requestedRate) {
		int iterations = -1;
		int wanRateInt = getVCATRateInt(wanRate);
		int requestedRateInt = -1;

		try {
			requestedRateInt = Integer.parseInt(requestedRate);
			if (requestedRateInt < wanRateInt) {
				iterations = 1;
			}
			else {
				iterations = requestedRateInt / wanRateInt;
				// Add 1 to the iterations if the wanRate does not divide
				// evenly into the requestedRate
				if (requestedRateInt % wanRateInt != 0) {
					iterations += 1;
				}
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}

		return iterations;
	}

	public static String printBitSet(BitSet bitset, String aid, String name,
	    int size, BigInteger constraints) {
		StringBuilder sb = new StringBuilder();
		int adjustedSize = size + 1; // Increase the size by one to print bits from
		                             // 1 to n instead of 0 to
		// n-1.

		sb.append("\r\nAID: " + aid + " " + name + " constraints: " + constraints
		    + "\r\n");

		// Print a nice header
		if (size < 24) {
			for (int i = 1; i < adjustedSize; i++) {
				sb.append(i % 10);
				sb.append(" ");
			}
			sb.append("\r\n");
		}
		else {
			sb.append("1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4\r\n");
		}

		for (int i = 1; i < adjustedSize; i++) {

			if (constraints == null) {
				sb.append((bitset.get(i) ? 1 : 0) + " ");
			}
			else {
				if (bitset.get(i)) {
					if (constraints.testBit(i)) {
						sb.append("C ");
					}
					else {
						sb.append("1 ");
					}
				}
				else {
					sb.append("0 ");
				}
			}

			if (i % 24 == 0) {
				sb.append("\r\n");

			}
		}

		sb.append("\r\n");
		return sb.toString();
	}

	/**
	 * The goal is to keep this in one place. We allow the user to provide 0,1 or
	 * 2 VLAN IDs to use when provisioning. The src vlan id is best used on the
	 * src node, the dst vlan id is best used on the destination node. What about
	 * nodes in the middle? Here we examine and select the vlan to use.
	 */

	public static String scanForVlanIdFromNeId(Map<SPF_KEYS, Object> parms,
	    String neId) throws Exception {
		String vid = null;

		String srcVlan = (String) parms.get(SPF_KEYS.SPF_SRCVLAN);
		String dstVlan = (String) parms.get(SPF_KEYS.SPF_DSTVLAN);

		if (neId.equals(parms.get(SPF_KEYS.SPF_SOURCEID))) {
			// We are the head end
			if (srcVlan != null) {
				vid = srcVlan;
				
				return vid;
			}
		}

		if (neId.equals(parms.get(SPF_KEYS.SPF_TARGETID))) {
			// we are the tail end
			if (dstVlan != null) {
				vid = srcVlan;
				
				return vid;
			}
		}

		if (dstVlan == null && srcVlan != null) {
			// if only one vlan is specified use it.
			vid = srcVlan;
		}

		if (dstVlan != null && srcVlan == null) {
			// if only one vlan is specified use it.
			vid = dstVlan;
		}

		if (srcVlan != null && srcVlan.equals(dstVlan)) {
			// source and dest vlan are the same
			vid = srcVlan;
		}

		
		return vid;
	}

	/**
	 * The goal is to keep this in one place. We allow the user to provide 0,1 or
	 * 2 VLAN IDs to use when provisioning. The src vlan id is best used on the
	 * src node, the dst vlan id is best used on the destination node. What about
	 * nodes in the middle? Here we examine and select the vlan to use.
	 */

	public static String scanForVlanIdFromTna(Map<SPF_KEYS, Object> parms,
	    String tna) throws Exception {
		String vid = null;
		String srcVlan = (String) parms.get(SPF_KEYS.SPF_SRCVLAN);
		String dstVlan = (String) parms.get(SPF_KEYS.SPF_DSTVLAN);

		if (tna.equals(parms.get(SPF_KEYS.SPF_SRCTNA))) {
			// We are the head end
			if (srcVlan != null) {
				vid = srcVlan;
				
				return vid;
			}
		}

		if (tna.equals(parms.get(SPF_KEYS.SPF_DSTTNA))) {
			// we are the tail end
			if (dstVlan != null) {
				vid = srcVlan;
				
				return vid;
			}
		}

		if (dstVlan == null && srcVlan != null) {
			// if only one vlan is specified use it.
			vid = srcVlan;
		}

		if (dstVlan != null && srcVlan == null) {
			// if only one vlan is specified use it.
			vid = dstVlan;
		}

		if (srcVlan != null && srcVlan.equals(dstVlan)) {
			// source and dest vlan are the same
			vid = srcVlan;
		}

		
		return vid;
	}

	private static int getVCATRateInt(String wanRate) {
		int vcatRateInt = -1;

		if (NONE_STR_RATE.equals(wanRate)) {
			vcatRateInt = -1;
		}
		else if (STS1_STR_RATE.equals(wanRate)) {
			vcatRateInt = 50;
		}
		else if (STS3C_STR_RATE.equals(wanRate)) {
			vcatRateInt = 150;
		}

		return vcatRateInt;
	}
}
