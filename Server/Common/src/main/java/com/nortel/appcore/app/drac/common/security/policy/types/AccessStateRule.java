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

import com.nortel.appcore.app.drac.common.security.policy.types.ResourceGroupProfileXML.ResourceAccessStateRule;

public final class AccessStateRule extends AbstractRule {
	private static final long serialVersionUID = 1L;
	private ResourceAccessStateRule rule;

	public AccessStateRule() throws Exception {
		super();
		this.rule = ResourceAccessStateRule.OPEN;
	}

	public AccessStateRule(ResourceAccessStateRule rule) {
		super();

		if (rule == null) {
			this.rule = ResourceAccessStateRule.OPEN;
		}
		else {
			this.rule = rule;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AccessStateRule) {
			AccessStateRule r = (AccessStateRule) o;
			if (r.isClosed() && this.isClosed()) {
				return true;
			}
			else if (r.isDisabled() && this.isDisabled()) {
				return true;
			}
			else if (r.isOpen() && this.isOpen()) {
				return true;
			}
			else {
				return false;
			}
		}

		return false;

	}

	@Override
	public void fromXML(Element root) throws Exception {
		this.rule = ResourceAccessStateRule.fromXMLToObject(root);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return rule.toString();
	}

	@Override
	public String toXMLString() {
		return rule.toXMLString();
	}

	private boolean isClosed() {
		return this.rule.isClosed();
	}

	private boolean isDisabled() {
		return this.rule.isDisabled();
	}

	private boolean isOpen() {
		return this.rule.isOpen();
	}
}
