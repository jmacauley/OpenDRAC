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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.jdom2.Element;

import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class UserPreferences implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String PREFERENCES_ELEMENT = "preferences";
	public static final String TIMEZONEID_ELEMENT = "timeZoneId";
	private final Map<String, String> prefList;

	public UserPreferences() {
		prefList = new HashMap<String, String>();
	}

	public void fromXML(Element root) throws Exception {
		/*********************************************************************/
		/* Constructs UserGroupType element. */
		/*********************************************************************/
		Element preferencesElement = root.getChild(PREFERENCES_ELEMENT);

		if (preferencesElement != null) {
			List<Element> elements = preferencesElement.getChildren();

			if (elements == null) {
				return;
			}

			for (Element element : elements) {
				this.prefList.put(element.getName(), element.getValue());
			}
		}

		if (getTimeZoneId() == null) {
			String timeZone = TimeZone.getDefault().getID();
			this.prefList.put(UserPreferences.TIMEZONEID_ELEMENT, timeZone);
		}

		// 
	}

	public String getTimeZoneId() {
		return prefList.get(TIMEZONEID_ELEMENT);
	}

	public void setTimeZoneId(String timeZone) {
		if (timeZone != null) {
			this.prefList.put(UserPreferences.TIMEZONEID_ELEMENT, timeZone);
		}
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder();

		buf.append('<');
		buf.append(UserPreferences.PREFERENCES_ELEMENT);
		buf.append('>');

		for (Map.Entry<String, String> entry : this.prefList.entrySet()) {
			buf.append("\n");
			buf.append(XmlUtility.toXMLString(entry.getKey(), entry.getValue()));
		}

		buf.append('\n');
		buf.append("</");
		buf.append(UserPreferences.PREFERENCES_ELEMENT);
		buf.append('>');

		return buf.toString();
	}

}
