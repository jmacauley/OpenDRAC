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

import com.nortel.appcore.app.drac.common.types.NetworkElementHolder.NETWORK_ELEMENT_MODE;
import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class ShowVersion extends AbstractCommandlet {

	public ShowVersion(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() throws Exception {
		Force10NetworkElement ne = (Force10NetworkElement) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

        
        ne.sendCommandWaitForCommandPrompt("show version\r\n", 30 * 1000);

		try {
			// Grab the software version from the buffer.
			String rel = ne.getExpectReader().expect(
			        ne.patternCache("(?m)^\\s*Force10 Application Software Version: (.*)$"),
			        10 * 1000, true, 1).trim();

			// rel = rel.replace("Force10 Application Software Version:", "").trim();
			

			ne.setNeRelease(rel);
			DbUtility.INSTANCE.upDateNeRelease(ne);

            /**
             * Now we need to get the Force10 chassis type which may also be
             * system type on certain network elements.  We can find this
             * information using the "show version" command.
             *
             * UvA
             *   Force10 Application Software Version: 5.3.1.6
             *   show version -> Chassis Type: E600
             * CERN
             *   Force10 Application Software Version: 8.3.1.1
             *   show chassis -> Chassis Type  : E1200i
             *   show version -> Chassis Type: E1200i
             * SURFnet
             *   Force10 Application Software Version: 8.3.2.0
             *   show chassis -> Chassis Type  : E1200i
             *   show version -> System Type: S25P
             */

            String neSubType = null;

            try {
                
                neSubType = ne.getExpectReader().expect(
                      ne.patternCache("(?m)^\\s*System Type: (.*)$"),
                      10 * 1000, true, 1).trim();
            }
            catch (Exception e) {
                
            }

            if (neSubType == null) {
                try {
                    
                    neSubType = ne.getExpectReader().expect(
                          ne.patternCache("(?m)^\\s*Chassis Type: (.*)$"),
                          10 * 1000, true, 1).trim();
                }
                catch (Exception e) {
                    
                    neSubType = "Unknown";
                }
            }

            

            ne.setSubType(neSubType);
            DbUtility.INSTANCE.upDateSubType(ne);

			ne.setNeMode(NETWORK_ELEMENT_MODE.Unknown);

			// use the command line prompt for the NE's name
			ne.setNeName(ne.getCommandLinePrompt().pattern());
			DbUtility.INSTANCE.upDateNe(ne);
			return true;
		}
		catch (Exception e) {
			log.error("ShowVersion: failed to set release, subType, and terninal for " + ne.getIpAddress() + ":" + ne.getPortNumber(), e);
			return false;
		}
	}
}
