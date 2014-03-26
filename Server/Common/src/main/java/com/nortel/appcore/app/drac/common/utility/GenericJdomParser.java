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

import java.io.StringReader;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public final class GenericJdomParser {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private Element rootElement;

	public Element getRoot() {
		return rootElement;
	}

	public void parse(InputSource source) throws Exception {
		// Build the document with SAX and Xerces, no validation
		SAXBuilder builder = new SAXBuilder();
		// Create the document
		Document doc = builder.build(source);
		rootElement = doc.getRootElement();
	}

	public void parse(String xml) {
		try {
			parse(new InputSource(new StringReader(xml)));
		}
		catch (Exception e) {
			log.error("Error parsing XML: " + xml, e);
		}
	}
}
