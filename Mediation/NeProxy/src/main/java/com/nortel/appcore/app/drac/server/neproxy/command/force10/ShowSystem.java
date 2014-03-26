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

import com.nortel.appcore.app.drac.server.neproxy.Force10NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.database.DbUtility;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class ShowSystem extends AbstractCommandlet {

	public ShowSystem(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() throws Exception {
		Force10NetworkElement ne = (Force10NetworkElement) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);

        /**
         * Depending on the type of force 10 box, we can obtain the system
         * mac/IEEE address from either the show system or show chassis command.
         * Rather than try to figure out which command is supported on which box,
         * we'll send one, and if it fails, try the other.
         *
         * The E600 running version 5.3.1.6 does not have a chassis MAC
         * available through the CLI.  We can use the MAC of the management
         * interface card "ManagementEthernet 0/0" using the show interface
         * command.
         *
         * Send the "show system" command to see if we get an error (which
         * would mean the command is not supported and we try the "show
         * chassis" command.  If an exception occurs this is for a timeout
         * waiting for the error message, and therefore, we must have had
         * a valid "show system" command.
         */
        
        ne.sendCommandWaitForCommandPrompt("show system\r\n", 30 * 1000);

        // Look for the error message
        try {
            String mac = ne.getExpectReader()
                    .expect(ne.patternCache("(?m)^\\s*Stack MAC :(.*)$"), 10 * 1000,
                    true, 1).trim().toUpperCase().replace(':', '-');

            
            ne.setNeId(mac);
            DbUtility.INSTANCE.upDateNe(ne);
            return true;
        }
        catch (Exception e) {
            
        }

        /**
         * Test to see if we can get the MAC address from the "show chassis"
         * command.  If we can't we will need to fallback onto the "show
         * interfaces" command.
         */
        
        ne.sendCommandWaitForCommandPrompt("show chassis\r\n", 30 * 1000);

        try {
            String mac = ne.getExpectReader()
                .expect(ne.patternCache("(?m)^\\s*Chassis MAC   : (.*)$"),
                10 * 1000, true, 1).trim().toUpperCase().replace(':', '-');
            
            ne.setNeId(mac);
            DbUtility.INSTANCE.upDateNe(ne);
            return true;
        }
        catch (Exception e) {
            

        }

        
        ne.sendCommandWaitForCommandPrompt("show interfaces ManagementEthernet 0\r\n", 30 * 1000);

		try {
            /**
             * Last chance!  Now we send the "show interfaces" command.
             */
			ne.getExpectReader()
			    .expect(ne.patternCache("(?m)^\\s*ManagementEthernet 0/0 (.*)$"), 10 * 1000, true);

            String mac = ne
                    .getExpectReader()
                    .expect(ne.patternCache("(?m)^\\s*Hardware is Force10Eth, address is (.*)$"),
                    10 * 1000, true, 1).trim().toUpperCase().replace(':', '-');

			
			ne.setNeId(mac);
			DbUtility.INSTANCE.upDateNe(ne);
			return true;
		}
		catch (Exception e) {
			log.error("ShowSystem: show system failed to determine devices MAC address "
			    + ne.toDebugString(), e);
			return false;
		}
	}
}
