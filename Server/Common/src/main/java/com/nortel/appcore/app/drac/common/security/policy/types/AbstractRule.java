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

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;

public abstract class AbstractRule implements Serializable {
  protected final Logger log = LoggerFactory.getLogger(getClass());
  
	private static final long serialVersionUID = 5612098582847839205L;
	public static final String RULE_ID_ATTR = "ruleID";
	private String ruleID;

	protected AbstractRule() {
		ruleID = String.valueOf(System.currentTimeMillis());
	}

	public void fromXML(Element root) throws Exception {
		this.ruleID = root.getAttributeValue(RULE_ID_ATTR);
		if (this.ruleID == null) {
			DracException ex = new DracException(
			    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
			    new Object[] { RULE_ID_ATTR });
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String id) {
		this.ruleID = id;
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder();
		buf.append(" ");
		buf.append(RULE_ID_ATTR);
		buf.append("=\"");
		buf.append(this.ruleID + "\"");
		return buf.toString();
	}
}
