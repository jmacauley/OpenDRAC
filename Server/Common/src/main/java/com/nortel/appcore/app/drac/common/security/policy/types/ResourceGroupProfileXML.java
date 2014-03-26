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

package com.nortel.appcore.app.drac.common.security.policy.types;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class ResourceGroupProfileXML {
	public static enum ResourceAccessStateRule {
		OPEN(0, "open"), CLOSED(1, "closed"), DISABLED(3, "disabled"), EMPTY(4, "");

		private static final long serialVersionUID = 1;
		public static final String RESOURCE_STATE_ELEMENT = "resourceStateRule";
		private final int code;
		private final String description;

		ResourceAccessStateRule(int stateCode, String sateDescription) {
			code = stateCode;
			description = sateDescription;
		}

		public static ResourceAccessStateRule fromString(String ruleStr)
		    throws Exception {
			ResourceAccessStateRule rule = null;
			if (ruleStr != null) {
				if (ruleStr.equals(OPEN.description)) {
					rule = OPEN;
				}
				else if (ruleStr.equals(DISABLED.description)) {
					rule = DISABLED;
				}
				else if (ruleStr.equals(CLOSED.description)) {
					rule = CLOSED;
				}
				else if (ruleStr.equals(EMPTY.description)) {
					rule = EMPTY;
				}
				else {
					throw new Exception("Invalid value");
				}
			}
			else {
				throw new Exception("Missing value");
			}
			return rule;
		}

		public static ResourceAccessStateRule fromXMLToObject(Element root)
		    throws Exception {
			return fromString(root.getValue());
		}

		public boolean isClosed() {
			return code == CLOSED.code || description.equals(CLOSED.description);
		}

		public boolean isDisabled() {
			return code == DISABLED.code || description.equals(DISABLED.description);
		}

		public boolean isOpen() {
			return code == OPEN.code || description.equals(OPEN.description);
		}

		@Override
		public String toString() {
			return description;
		}

		public String toXMLString() {
			return XmlUtility.toXMLString(RESOURCE_STATE_ELEMENT, description);
		}
	}

	public static enum ResourceType

	{
		ENDPOINT("endpoint");

		private static final long serialVersionUID = 1;
		public static final String RESOURCE_TYPE = "resourceType";

		private final String description;

		ResourceType(String typeDescription) {
			description = typeDescription;
		}

		public static ResourceType fromString(String typeStr) throws Exception {
			if (typeStr != null && !typeStr.equals("")) {
				if (typeStr.equals(ENDPOINT.description)) {
					return ENDPOINT;
				}
				throw new Exception("Invalid value '" + typeStr + "'");
			}
			return null;
		}

		@Override
		public String toString() {
			return description;
		}
		
	}

	private ResourceGroupProfileXML() {
		super();
	}
}
