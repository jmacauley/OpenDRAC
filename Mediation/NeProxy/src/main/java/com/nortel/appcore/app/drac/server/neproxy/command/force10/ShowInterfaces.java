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

package com.nortel.appcore.app.drac.server.neproxy.command.force10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.EndPointType;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementFacility;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * Fetch interfaces (facilities) from the switch and populate the drac model.
 * The show interface command is very verbose and can be slow to run.
 *
 * @author pitman
 */
public final class ShowInterfaces extends AbstractCommandlet {
	private final Force10NetworkElement ne;
	private static final Pattern INTERFACE_FIRST_LINE_PATTERN = Pattern
	    .compile("(.*) is (.*), line protocol is (.*)");

	public ShowInterfaces(Map<String, Object> param) {
		super(param);
		ne = (Force10NetworkElement) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
	}

	@Override
	public boolean start() throws Exception {
		boolean rc = false;
		try {


			/**
			 * The initial delay is used to reduce the time we spend scanning the
			 * buffer for the command prompt before the command has finished running.
			 * Scanning for the command prompt uses a regular expression that is very
			 * cpu intensive.
			 * <p>
			 * The show interfaces command is slow, wait for it to come back to us.
			 */
			ne.sendCommandWaitForCommandPrompt("show interfaces\r\n", 30 * 1000,
			    10 * 60 * 1000);

			/*
			 * Now grab the entire xml response from the buffer using another
			 * expensive regular expression match.
			 */
			String block = ne.getExpectReader().expect(
			    ne.patternCache("(?s)(.*)" + ne.getCommandLinePrompt()),
			    10 * 60 * 1000, true);
			//

			/**
			 * The command show interfaces returns us a series of interfaces separated
			 * by 2 blank lines. Each block or interface looks like the following.
			 *
			 * <pre>
			 * GigabitEthernet 0/1 is down, line protocol is down
			 * Hardware is Force10Eth, address is 00:01:e8:d6:37:aa
			 *     Current address is 00:01:e8:d6:37:aa
			 * Pluggable media present, SFP type is 1000BASE-LX
			 *     Wavelength is 1310nm
			 *     SFP receive power reading is -30.7641dBm
			 * Interface index is 34128387
			 * Internet address is not set
			 * MTU 1554 bytes, IP MTU 1500 bytes
			 * LineSpeed auto
			 * Flowcontrol rx off tx off
			 * ARP type: ARPA, ARP Timeout 04:00:00
			 * Last clearing of "show interface" counters 3d23h46m
			 * Queueing strategy: fifo
			 * Input Statistics:
			 *      0 packets, 0 bytes
			 *      0 64-byte pkts, 0 over 64-byte pkts, 0 over 127-byte pkts
			 *      0 over 255-byte pkts, 0 over 511-byte pkts, 0 over 1023-byte pkts
			 *      0 Multicasts, 0 Broadcasts
			 *      0 runts, 0 giants, 0 throttles
			 *      0 CRC, 0 overrun, 0 discarded
			 * Output Statistics:
			 *      0 packets, 0 bytes, 0 underruns
			 *      0 64-byte pkts, 0 over 64-byte pkts, 0 over 127-byte pkts
			 *      0 over 255-byte pkts, 0 over 511-byte pkts, 0 over 1023-byte pkts
			 *      0 Multicasts, 0 Broadcasts, 0 Unicasts
			 *      0 throttles, 0 discarded, 0 collisions
			 * Rate info (interval 299 seconds):
			 *      Input 00.00 Mbits/sec,          0 packets/sec, 0.00% of line-rate
			 *      Output 00.00 Mbits/sec,          0 packets/sec, 0.00% of line-rate
			 * Time since last interface status change: 3d23h45m
			 * </pre>
			 */

			// Separate the interface data into a list of interfaces
			List<List<String>> blocks = new ArrayList<List<String>>();
			List<String> b = new ArrayList<String>();

			StringTokenizer st = new StringTokenizer(block, "\n\r", false);
			boolean firstblock = true;
			while (st.hasMoreTokens()) {
				String line = st.nextToken();
				if (firstblock) {
					if (line.contains("show interface")) {
						// swallow this line its not part of the output

						firstblock = false;
						continue;
					}

				}
				b.add(line);
				if (line.startsWith("Time since last interface status change")) {
					blocks.add(new ArrayList<String>(b));
					b.clear();
				}
			}

			/* Debug, print out what we've got */

			// for (List<String> bl : blocks)
			// {
			//
			// for (String l : bl)
			// {
			//
			// }
			// }

			Set<Integer> inuseVlans = new TreeSet<Integer>();

			Map<String, Map<String, String>> networkFacs = new TreeMap<String, Map<String, String>>();

			for (List<String> bl : blocks) {
				/**
				 * <pre>
				 * <ifName>TenGigabitEthernet 0/0</ifName>
				 * <ifAdminStatus>down</ifAdminStatus>
				 * <ifOperStatus>down</ifOperStatus>
				 * <ifDescr>----> [DRAC RESERVED]</ifDescr>
				 * <ifHardwareType>Force10Eth</ifHardwareType>
				 * <ifIndex>33882171</ifIndex>
				 * <ifLineSpeed>10000 Mbit</ifLineSpeed>
				 * </pre>
				 */
				/**
				 * <pre>
				 * TenGigabitEthernet 0/28 is down, line protocol is down
				 * Description: to dlp01.surfnet.nl eth1
				 * Hardware is Force10Eth, address is 00:01:e8:d6:37:aa
				 *      Current address is 00:01:e8:d6:37:aa
				 * Pluggable media present, XFP type is 10GBASE-LR
				 *     Medium is MultiRate, Wavelength is 1310.00nm
				 *     XFP receive power reading is -30.4576dBm
				 * Interface index is 41222659
				 * Internet address is not set
				 * MTU 1554 bytes, IP MTU 1500 bytes
				 * LineSpeed 10000 Mbit
				 * </pre>
				 */
				try {
                    // Strip starting white space lines.
                    String line0 = null;
                    for (int i = 0; i < bl.size(); i++) {
                        line0 = bl.get(i);
                        if (!line0.trim().isEmpty()) {
                            break;
                        }
                    }

					String ifName = "";
					String ifAdminStatus = "down";
					String ifOperStatus = "down";
					String ifDescr = "";
					String ifHardwareType = "";
					String ifIndex = "";
					String ifLineSpeed = "";

					Matcher m = INTERFACE_FIRST_LINE_PATTERN.matcher(line0);
					if (m.matches()) {
						ifName = m.group(1);
						ifAdminStatus = m.group(2);
						ifOperStatus = m.group(3);
					}
					else {
						log.error("Expected the first line of the interface to match the regular expression '"
						    + INTERFACE_FIRST_LINE_PATTERN.toString()
						    + "' parsing the interface line "
						    + line0
						    + " from "
						    + bl
						    + " from " + ne.toDebugString());
					}

					String tmp;

					for (String line : bl) {
						if (line.startsWith("Description:")) {
							ifDescr = line.substring(13).trim();
						}
						else if (line.startsWith("Hardware is ")) {
							/**
							 * Handle entries such as:
							 *
							 * <pre>
							 * Hardware is Force10Eth, address is 00:01:e8:d6:37:aa
							 * </pre>
							 *
							 * <pre>
							 * Hardware is Loopback.
							 * </pre>
							 */
							tmp = line.substring(12);
							if (tmp.indexOf(',') >= 0) {
								ifHardwareType = tmp.substring(0, tmp.indexOf(',')).trim();
							}
							else {
								if (tmp.indexOf('.') >= 0) {
									ifHardwareType = tmp.substring(0, tmp.indexOf('.')).trim();
								}
								else {
									log.error("Expected a comma or period while parsing the Hardware entry "
									    + line + " from " + bl + " from " + ne.toDebugString());
								}
							}

						}
						else if (line.startsWith("Interface index is ")) {
							ifIndex = line.substring(19).trim();
						}
						else if (line.startsWith("LineSpeed ")) {
							tmp = line.substring(10);
							if (tmp.indexOf(',') >= 0) {
								// LineSpeed 1000 Mbit, Mode full duplex
								ifLineSpeed = tmp.substring(0, tmp.indexOf(',')).trim();
							}
							else {
								// LineSpeed auto
								ifLineSpeed = tmp.trim();
							}
						}
					}

					log.debug("Got ifName '" + ifName + "' ifAdminStatus '"
					    + ifAdminStatus + "' ifOperStatus '" + ifOperStatus
					    + "' ifDescr '" + ifDescr + "' ifHardwareType '" + ifHardwareType
					    + "' ifIndex '" + ifIndex + "' ifLineSpeed '" + ifLineSpeed + "'");

					if (ifName.startsWith("TenGigabitEthernet")
					    || ifName.startsWith("GigabitEthernet")
					    || ifName.startsWith("FortyGigE")) {
						// convert an ifName like "GigabitEthernet 7/46" to slot/port and
						// aid...

						st = new StringTokenizer(ifName);
						st.nextToken();
						String shelf = "1";
						String slot = st.nextToken("/").trim();
						String port = st.nextToken().trim();

						String sigType = "?";
						String speed = "";
						if (ifName.startsWith("TenGigabitEthernet")) {
							sigType = "TenGigabitEthernet";
							speed = "10000";
						}
						else if (ifName.startsWith("GigabitEthernet")) {
							sigType = "GigabitEthernet";
							speed = "1000";

						}
						else if (ifName.startsWith("FortyGigE")) {
							sigType = "FortyGigE";
							speed = "40000";
						}

						String aid = sigType + "-" + shelf + "-" + slot + "-" + port;

						String primaryState = ifAdminStatus + "/" + ifOperStatus;

						String pk = EndPointType.encodeFacilityEndpointResourceId(
						    ne.getNeId(), aid);
						Map<String, String> facilityMap = new TreeMap<String, String>();
						facilityMap.put(DbKeys.NetworkElementFacilityCols.AID, aid);
						facilityMap.put(DbKeys.NetworkElementFacilityCols.FAC_NEID,
						    ne.getNeId());
						facilityMap.put(DbKeys.NetworkElementFacilityCols.NEPORT_FOR_FAC,
						    Integer.toString(ne.getPortNumber()));
						facilityMap.put(DbKeys.NetworkElementFacilityCols.NEIP_FOR_FAC,
						    ne.getIpAddress());
						facilityMap.put(DbKeys.NetworkElementFacilityCols.LAYER,
						    Layer.LAYER2.toString());

						facilityMap.put(DbKeys.NetworkElementFacilityCols.SHELF, shelf);
						facilityMap.put(DbKeys.NetworkElementFacilityCols.SLOT, slot);
						facilityMap.put(DbKeys.NetworkElementFacilityCols.PORT, port);
						facilityMap.put(DbKeys.NetworkElementFacilityCols.PRIMARYSTATE,
						    primaryState);
						facilityMap.put(DbKeys.NetworkElementFacilityCols.TYPE, sigType);
						facilityMap.put(DbKeys.NetworkElementFacilityCols.PK, pk);
						// vcat not supported!
						facilityMap.put(DbKeys.NetworkElementFacilityCols.VCAT, "DISABLE");
						facilityMap.put("speed", speed);

						/*
						 * non standard attributes we want to add to the facility record,
						 * some for debug some because we think they are useful.
						 */
						facilityMap.put("ifAdminStatus", ifAdminStatus);
						facilityMap.put("ifOperStatus", ifOperStatus);
						facilityMap.put("ifDescr", ifDescr);
						facilityMap.put("ifHardwareType", ifHardwareType);
						facilityMap.put("ifIndex", ifIndex);
						facilityMap.put("ifLineSpeed", ifLineSpeed);

						log.debug("Force10 show interfaces: interface as facility map "
						    + facilityMap.toString());
						networkFacs.put(pk, facilityMap);
					}
					else if (ifName.startsWith("Vlan")) {
						st = new StringTokenizer(ifName);
						st.nextToken();
						String vid = st.nextToken();

						log.debug("Force10 show interfaces: Found Vlan '" + vid
						    + "' description '" + ifDescr + "'");
						if (!ifDescr.startsWith("DRAC-")) {
							try {
								Integer id = Integer.valueOf(vid);
								inuseVlans.add(id);
							}
							catch (Exception e) {
								log.error(
								    "unable to parse existing vlan record, inuse vlan record will be inaccurate!",
								    e);
							}
						}
					}
				}
				catch (Exception e) {
					log.error(
					    "Force10: Skipping individual interface block that we failed to parse '"
					        + bl + "' from " + ne.toDebugString(), e);
				}
			}

			/*
			 * Store the list of inuse vlans in each and every facility object we
			 * create... this is overkill but simplifies the logic on the other end.
			 */
			StringBuilder inuse = new StringBuilder();
			for (Integer i : inuseVlans) {
				inuse.append(i);
				inuse.append(',');
			}
			if (inuse.length() > 0) {
				// remove the final ','
				inuse.setLength(inuse.length() - 1);
			}

			String inuseVlanString = inuse.toString();
			log.debug("Adding the InUseVlans attribute to facilities.  Value ='"
			    + inuseVlanString + "'");

			for (Map.Entry<String, Map<String, String>> e : networkFacs.entrySet()) {
				/*
				 * According to Map.entrySet modifying the map while iterating through
				 * it can be dangerous, we are only modifying the value and are fine.
				 */
				e.getValue().put("InUseVlans", inuseVlanString);
			}

			updateDb(networkFacs);
			rc = true;
		}
		catch (Exception e) {
			log.error("Force10: failed to show interfaces " + ne.toDebugString(), e);
			rc = false;
		}


		return rc;
	}

	/**
	 * In the case of a newly discovered facility or as a safety check for
	 * existing, make sure that certain mandatory attributes are included with
	 * default values.
	 */
	private Map<String, String> populateMandatoryAttributes(Map<String, String> m) {
		if (m.get(DbKeys.NetworkElementFacilityCols.GROUP) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.GROUP, "none");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.TNA) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.TNA, "N/A");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.COST) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.COST, "1");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.METRIC) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.METRIC, "1");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.SRLG) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.SRLG, "N/A");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.CONSTRAIN) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.CONSTRAIN, "0");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.SITE) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.SITE, "N/A");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.DOMAIN) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.DOMAIN, "N/A");
		}
		if (m.get(DbKeys.NetworkElementFacilityCols.SIGTYPE) == null) {
			m.put(DbKeys.NetworkElementFacilityCols.SIGTYPE,
			    FacilityConstants.SIGNAL_TYPE.unassigned.toString());
		}

		// Always mark a facility as valid
		m.put(DbKeys.NetworkElementFacilityCols.VALID, "true");
		return m;
	}

	/**
	 * Update NE facility. Given a map of maps (keyed by primary key) and
	 * containing facility attributes from the network, merge with existing info
	 * in the database and apply if different.. but every record will be different
	 * because we marked all the existing records as invalid before we started,
	 * and all these will be marked as valid.
	 */
	private void updateDb(Map<String, Map<String, String>> networkFacs)
	    throws Exception {
		Map<String, String> filter = new HashMap<String, String>();
		filter.put(DbKeys.NetworkElementFacilityCols.NEID_FOR_FAC, ne.getNeId());

		// Create a map of existing facilities for the NE keyed by the facility
		// primary key (neid,aid).
		Map<String, Facility> existingRecords = new HashMap<String, Facility>();
		for (Facility f : DbNetworkElementFacility.INSTANCE
		    .retrieveFacilities(filter)) {
			existingRecords.put(f.getPrimaryKey(), f);
		}

		List<Facility> facilitiesToAdd = new ArrayList<Facility>();
		List<Facility> facilitiesToAlter = new ArrayList<Facility>();

		for (Map.Entry<String, Map<String, String>> m : networkFacs.entrySet()) {
			String key = m.getKey();
			Map<String, String> f = m.getValue();

			if (existingRecords.get(key) == null) {
				// brand new facility
				facilitiesToAdd.add(new Facility(ne, populateMandatoryAttributes(f)));
			}
			else {
				/*
				 * existing facility. Take the record from the database overwrite with
				 * the new stuff, sanitize and save.
				 */
				Map<String, String> fac = new TreeMap<String, String>(existingRecords
				    .get(key).asUnmodifiableMap());
				fac.putAll(f);
				facilitiesToAlter
				    .add(new Facility(ne, populateMandatoryAttributes(fac)));
			}
		}

		// Look for new facilities to add and existing once that need to be updated.

		if (!facilitiesToAdd.isEmpty()) {
			log.debug("Adding " + facilitiesToAdd.size() + " new facilities for ne "
			    + ne.getNeId() + " " + ne.getIpAddress() + ":" + ne.getPortNumber()
			    + " " + existingRecords.size()
			    + " records were already in the database.");
			DbNetworkElementFacility.INSTANCE.addFacility(ne.getNeId(),
			    ne.getIpAddress(), ne.getPortNumber(), facilitiesToAdd);
		}

		if (!facilitiesToAlter.isEmpty()) {
			log.debug("Updating " + facilitiesToAlter.size() + " facilities for ne "
			    + ne.getNeId() + " " + ne.getIpAddress() + ":" + ne.getPortNumber()
			    + " " + existingRecords.size()
			    + " records were already in the database.");
			for (Facility f : facilitiesToAlter) {
				DbNetworkElementFacility.INSTANCE.update(ne, f.getAid(),
				    f.asUnmodifiableMap(), true);
			}
		}

		/*
		 * Ignore facilities that have been deleted, they will already have been
		 * marked invalid when the alignment started.
		 */
	}
}
