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

import java.util.Map;
import java.util.StringTokenizer;

import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;

public final class CreateConnection extends AbstractCommandlet {
	private Force10NetworkElement ne;
	private final CrossConnection xcon;

	public CreateConnection(Map<String, Object> param) {
		super(param);
		xcon = (CrossConnection) getParameters().get(
		    ClientMessageXml.CROSSCONNECTION_KEY);
	}

	@Override
	public boolean start() throws Exception {
		try {
			log.debug("Force10 CreateConnection Processing "
			    + getParameters().toString());

			String neid = xcon.getSourceNeId();
			ne = (Force10NetworkElement) DiscoverNePool.INSTANCE
			    .getNeByTidOrIdOrIpandPort(neid);
			if (ne == null) {
				log.error("Force10 CreateConnection cannot find NE " + neid);
				getCandidate().setErrorCode("ERR_IUID");
				return false;
			}

			try {

				ne.lockCommunicationsWithNe();
				String tag = "tagged ";
				String vlanId = xcon.getVlanId().toLowerCase();
				// @TODO Rethink this and add if it makes sence. ONly support tagged for
				// now.
				/*
				 * Vlan ID can just be a number "100" or it can can include the word
				 * untagged and a number in which case we want to take untagged input
				 * from the port and associate it with the given vlan id ("untagged-100"
				 * "100-UnTagged"). To be complete, we'll also permit "100-tagged", and
				 * "tagged-100" to mean the same as "100".
				 */
				//
				// if (vlanId.contains("untagged"))
				// {
				// tag = "untagged";
				// vlanId = vlanId.replace("untagged", "").replace("-", "");
				// }
				// else if (vlanId.contains("tagged"))
				// {
				// vlanId = vlanId.replace("tagged", "").replace("-", "");
				// }

				// extract the id (and validate it in the process, though its a bit late
				// for that now)
				int vid = -1;
				try {
					vid = Integer.parseInt(vlanId);
				}
				catch (NumberFormatException nfe) {
					throw new Exception(
					    "Force10 CreateConnection Could not extract VLAN id from the vlan field '"
					        + xcon.getVlanId() + "' when creating the connection " + xcon,
					    nfe);
				}

				String desc = xcon.getId();

				// "GigabitEthernet" sourcePortAid=10G-1-0-0, targetAid=10G-1-0-3-0
				StringTokenizer st = new StringTokenizer(xcon.getSourcePortAid(), "-");
				StringBuilder src = new StringBuilder();
				src.append(st.nextToken());
				src.append(" ");
				st.nextToken(); // skip shelf
				src.append(st.nextToken());
				src.append("/");
				src.append(st.nextToken());

				st = new StringTokenizer(xcon.getTargetPortAid(), "-");
				StringBuilder dst = new StringBuilder();
				dst.append(st.nextToken());
				dst.append(" ");
				st.nextToken(); // skip shelf
				dst.append(st.nextToken());
				dst.append("/");
				dst.append(st.nextToken());

				StringBuilder sb = new StringBuilder("configure\r\nno interface vlan "
				    + vid + "\r\ninterface Vlan " + vid + "\r\n");
				sb.append(" description " + desc + "\r\n");
				sb.append(" no shutdown\r\n");
				sb.append(" " + tag + src + "\r\n");
				sb.append(" " + tag + dst + "\r\n");
				sb.append("end\r\n");

				log.debug("Force10 CreateConnection: Proposed configuration commands "
				    + sb.toString());

				try {
					ne.sendCommandWaitForCommandPrompt("configure\r\n", 60 * 1000);

					/**
					 * Remove the vlan before creating it, just in case... Will get
					 *
					 * <pre>
					 *  % Error: No such interface Vl 201.
					 * </pre>
					 *
					 * if the vlan does not exist, thats ok.
					 */
					ne.sendCommandWaitForCommandPrompt("no interface vlan " + vid
					    + "\r\n", 60 * 1000);
					ne.sendCommandWaitForCommandPrompt("interface vlan " + vid + "\r\n",
					    60 * 1000);
					ne.sendCommandWaitForCommandPrompt(" description " + desc + "\r\n",
					    60 * 1000);
					ne.sendCommandWaitForCommandPrompt(" no shutdown\r\n", 60 * 1000);
					ne.sendCommandWaitForCommandPrompt(" " + tag + src + "\r\n",
					    60 * 1000);
					ne.sendCommandWaitForCommandPrompt(" " + tag + dst + "\r\n",
					    60 * 1000);
				}
				finally {
					/*
					 * if we got into the config menu we want to make sure we get out of
					 * it... send the "end" command even if we fail to configure something
					 * so that we'll get out of the config menu. If we didn't get into the
					 * config menu, the end command will not harm anything
					 */
					ne.sendCommandWaitForCommandPrompt("end\r\n", 60 * 1000);
				}

				// Save the running config
                // JHM - commented out
                //ne.saveRunningConfig();

			}
			finally {
				ne.unlockCommunicationsWithNe();
			}
			log.debug("Force10 CreateConnection: Created connection");
			return true;
		}
		catch (Exception e) {
			log.error("Force10 CreateConnection: Create connection failed "
			    + ne.toDebugString());

			throw e;
		}

	}
}
