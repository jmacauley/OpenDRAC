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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.graph.NeType;

/**
 * @author pitman
 */
public class NePoxyDefinitionsParserTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Test method for
	 * {@link com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser#getEvenHandlerList(NeType)}
	 * .
	 */
	@Test
	public void testGetEvenHandlerList() {
		log.debug("getEvenHandlerList: "
		    + NePoxyDefinitionsParser.INSTANCE.getEvenHandlerList(NeType.CPL).toString());
	}

	/**
	 * Test method for
	 * {@link com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser#getInitializationList(NeType)}
	 * .
	 */
	@Test
	public void testGetInitializationList() {
		log.debug("getInitializationList:"
		    + NePoxyDefinitionsParser.INSTANCE.getInitializationList(NeType.CPL).toString());
	}

	/**
	 * Test method for
	 * {@link com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser#getNeEventHandlingDefinition()}
	 * .
	 */
	@Test
	public void testGetNeEventHandlingDefinition() {
		log.debug("getNeEventHandlingDefinition:"
		    + NePoxyDefinitionsParser.INSTANCE.getNeEventHandlingDefinition().toString());
	}

	/**
	 * Test method for
	 * {@link com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser#getNeInitDefinition()}
	 * .
	 */
	@Test
	public void testGetNeInitDefinition() {
		log.debug("getNeInitDefinition:"
		    + NePoxyDefinitionsParser.INSTANCE.getNeInitDefinition().toString());
		log.debug("getNeInitDefinition:"
		    + NePoxyDefinitionsParser.INSTANCE.getNeInitDefinition().get(NeType.OME6)
		        .toString());
		log.debug("getNeInitDefinition:"
		    + NePoxyDefinitionsParser.INSTANCE.getNeInitDefinition().get(NeType.OME7)
		        .toString());
		log.debug("getNeInitDefinition:"
		    + NePoxyDefinitionsParser.INSTANCE.getNeInitDefinition().get(NeType.OME8)
		        .toString());
		log.debug("getNeInitDefinition:"
        + NePoxyDefinitionsParser.INSTANCE.getNeInitDefinition().get(NeType.OME9)
            .toString());
	}

	/**
	 * Test method for
	 * {@link com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser#getNeTypeMapping()}
	 * .
	 */
	@Test
	public void testGetNeTypeMapping() {
		log.debug("getNeTypeMapping:"
		    + NePoxyDefinitionsParser.INSTANCE.getNeTypeMapping().toString());
	}

	@Test
	public void testParsingErrors() {
		if (!NePoxyDefinitionsParser.INSTANCE.errors.isEmpty()) {
			log.error("parsing errors " + NePoxyDefinitionsParser.INSTANCE.errors.toString());
			fail("Parsing errors parsing data file "
			    + NePoxyDefinitionsParser.INSTANCE.errors.toString());
		}
	}

}
