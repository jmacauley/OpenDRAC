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

package com.nortel.appcore.app.drac.server.neproxy.command.hdx;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.Holder;
import com.nortel.appcore.app.drac.database.dracdb.DbNetworkElementAdjacency;
import com.nortel.appcore.app.drac.server.neproxy.AbstractNe;
import com.nortel.appcore.app.drac.server.neproxy.command.AbstractInitializeNe;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

/**
 * Created on Aug 19, 2005
 * 
 * @author nguyentd
 */
public final class AlignNe extends AbstractInitializeNe {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final String NeID_FILE = "NEData.xml";
	private static final String DEFAULT_HTTPPORT = "80";
	private int neHttpPort;

	public AlignNe(AbstractNe ne) {
		super(ne);
	}

	@Override
	public boolean start() throws Exception {
		String ipPortCombined = "http_" + getNe().getIpAddress() + ":"
		    + getNe().getPortNumber();
		neHttpPort = Integer.parseInt(System.getProperty(ipPortCombined,
		    DEFAULT_HTTPPORT));
		log.debug("Initializing " + getNe().getNeName() + " at "
		    + getNe().getIpAddress());
		try {
			try {
				
				getNeIdFile();
			}
			catch (Exception e) {
				log.error("Failed to retrieve NEID for " + getNe().getIpAddress() + ":"
				    + getNe().getPortNumber(), e);
				return false;
			}
			try {
				DbNetworkElementAdjacency.INSTANCE.delete(getNe().getNeId());
			}
			catch (Exception e) {
				log.error("Failed to delete Adjacency data for " + getNe().getNeId(), e);
			}
			try {
				DbUtility.INSTANCE.invalidateFacility(getNe());
			}
			catch (Exception e) {
				log.error(
				    "Failed to invalidate Facility data for " + getNe().getNeId(), e);
			}

			NeType initType = getNe().getNeType();
			// if (getNe().initializeNeType != null)
			// {
			// initType = getNe().initializeNeType;
			// }
			List<Holder> initCommandList = getNe().getNeInitDefinition()
			    .get(initType);

			for (int i = 0; i < initCommandList.size(); i++) {
				Holder aCommandG = initCommandList.get(i);
				if (aCommandG == null) {
					continue;
				}
				String aCommand = aCommandG.getKeyAttr();
				if (aCommand == null) {
					continue;
				}
				Map<String, Object> parameters = new HashMap<String, Object>(
				    aCommandG.getCommandParam());
				String className = (String) parameters.get(NePoxyDefinitionsParser.CLASS_ATTR);
				parameters.remove(NePoxyDefinitionsParser.CLASS_ATTR);
				parameters.put(NePoxyDefinitionsParser.COMMANDNAME_KEY, aCommand);
				parameters.put(NePoxyDefinitionsParser.NETWORKELEMENT_KEY, getNe());
				

				AbstractCommandlet command = AbstractCommandlet.getCommandlet(
				    className, parameters);
				if (!command.start()) {
					log.error(aCommand + " failed");
					return false;
				}
			}

			

		}
		catch (Exception e) {
			log.error("Failed to initialize NE " + getNe().getIpAddress() + ":"
			    + getNe().getPortNumber(), e);
			return false;
		}
		return true;
	}

	private void getNeIdFile() throws Exception {
		String ipAddress = getNe().getIpAddress();
		// String queryString = "?tid=" + ne.neInfo.getNeName();
		// URL sourceURL = new URL("http", ipAddress, NeHttpPort, "/" + NeID_FILE +
		// queryString);
		URL sourceURL = new URL("http", ipAddress, neHttpPort, "/" + NeID_FILE);
		URLConnection connection = sourceURL.openConnection();
		byte[] data = new byte[connection.getContentLength()];
		InputStream locationToRead = connection.getInputStream();
		locationToRead.read(data, 0, data.length);

		SAXBuilder builder = new SAXBuilder();
		Document aDoc = builder.build(new ByteArrayInputStream(data));
		Element root = aDoc.getRootElement();
		String ieeeAddress = root.getChild("ne_identification")
		    .getChildText("ieee_address").trim();
		String version = root.getChild("ne_identification").getChildText("version");
		log.debug("Got this ieeAddress: " + ieeeAddress + " for Ne: "
		    + getNe().getNeName() + " version: " + version);
		getNe().setNeId(ieeeAddress);
	}
}
