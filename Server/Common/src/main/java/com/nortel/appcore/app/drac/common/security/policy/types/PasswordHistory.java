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
import java.util.Calendar;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.utility.DateFormatter;

public final class PasswordHistory implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	public static final String PASSWORD_HISTORY_ELEMENT = "passwordHistory";
	public static final String PASSWORD_ATT = "oldPassword";
	public static final String DATE_CHANGED_ATT = "dateChanged";
	private String password;
	private Calendar date;

	public PasswordHistory(String pw, Calendar date) {
		this.password = pw;
		this.date = date;
	}

	public void fromXML(Element root) throws Exception {

		if (root == null) {
			return;
		}

		String password = root.getAttributeValue(PasswordHistory.PASSWORD_ATT);

		Calendar changedDate = null;
		try {
			String changedDateString = root
			    .getAttributeValue(PasswordHistory.DATE_CHANGED_ATT);
			changedDate = DateFormatter.getDateFromString(changedDateString);

		}
		catch (Exception e) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { PasswordHistory.DATE_CHANGED_ATT }, e);
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		this.password = password;
		this.date = changedDate;
	}

	// public Calendar getDate()
	// {
	// return date;
	// }
	//
	// public String getPassword()
	// {
	// return password;
	// }
	//
	// public void setDate(Calendar date)
	// {
	// this.date = date;
	// }
	//
	// public void setPassword(String password)
	// {
	// this.password = password;
	// }

	public String toXMLString() {
		StringBuilder buf = new StringBuilder(20);
		buf.append("<");
		buf.append(PasswordHistory.PASSWORD_HISTORY_ELEMENT);
		buf.append(" " + PasswordHistory.PASSWORD_ATT + "=\"" + this.password
		    + "\"");
		buf.append(" " + PasswordHistory.DATE_CHANGED_ATT + "=\""
		    + DateFormatter.dateToString(this.date) + "\"");
		buf.append(">");
		buf.append("</" + PasswordHistory.PASSWORD_HISTORY_ELEMENT + ">");
		return buf.toString();

	}
}
