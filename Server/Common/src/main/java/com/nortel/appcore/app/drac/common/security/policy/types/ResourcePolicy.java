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
import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckablePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;

public final class ResourcePolicy implements Serializable,
    PolicyCheckablePolicy {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	public static final String RESOURCEPOLICY_ELEMENT = "resourcePolicy";

	private List<AbstractRule> accessRule;
	private AbstractRule stateRule;
	private AbstractRule bandwidthControlRule;

	public ResourcePolicy() {
		accessRule = new AccessRuleList();
		bandwidthControlRule = new BandwidthControlRule();
	}

	public void fromXML(Element root) throws Exception {
		/*********************************************************************/
		/* Constructs UserGroupType element. */
		/*********************************************************************/
		Element resourcePolicy = root.getChild(RESOURCEPOLICY_ELEMENT);

		if (resourcePolicy != null) {
			List<Element> elements = resourcePolicy.getChildren();

			if (elements == null) {
				// this.empty = true;
				return;
			}

			Element policy = null;
			for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
				policy = it.next();
				AbstractRule rule;
				if (policy.getName().equals(AccessRule.RESOURCEACCESSRULE_ELEMENT)) {
					rule = new AccessRule(AccessPermission.DENY);
					rule.fromXML(policy);
					// just meticuluous here
					((AccessRule) rule)
					    .setAccessRuleElementString(AccessRule.RESOURCEACCESSRULE_ELEMENT);
					this.accessRule.add(rule);

				}
				else if (policy
				    .getName()
				    .equals(
				        ResourceGroupProfileXML.ResourceAccessStateRule.RESOURCE_STATE_ELEMENT)) {
					rule = new AccessStateRule();
					rule.fromXML(policy);
					this.stateRule = rule;

				}
				else if (policy.getName().equals(
				    BandwidthControlRule.BANDWIDTHCONTROLRULE_ELEMENT)) {
					rule = new BandwidthControlRule();
					rule.fromXML(policy);
					this.bandwidthControlRule = rule;

				}
				else {
					// @TODO: Are we going to throw it?
					DracException e = new DracException(
					    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
					    new Object[] { new String(policy.getName()) });
					log.error("Error: ", e);
				}
			}
		}
		else {
			
		}

	}

	public List<AbstractRule> getAccessRule() {
		return accessRule;
	}

	public AbstractRule getBandwidthControlRule() {
		return bandwidthControlRule;
	}

	public AbstractRule getStateRule() {
		return stateRule;
	}

	public boolean isEmpty() {

		if (this.bandwidthControlRule != null
		    && !((BandwidthControlRule) this.bandwidthControlRule).isEmpty()) {
			return false;
		}

		if (this.accessRule != null && !this.accessRule.isEmpty()) {
			return false;
		}

		return true;
	}

	public void setAccessRule(List<AbstractRule> accessControlRule) {
		this.accessRule = accessControlRule;
	}

	public void setBandwidthControlRule(AbstractRule bandwidthControlRule) {
		this.bandwidthControlRule = bandwidthControlRule;
	}

	public void setStateRule(AbstractRule stateRule) {
		this.stateRule = stateRule;
	}

	@Override
	public String toString() {
		return toXMLString();
	}

	public String toXMLString() {
		StringBuilder buf = new StringBuilder();

		buf.append("<");
		buf.append(RESOURCEPOLICY_ELEMENT);
		buf.append(">");

		if (this.accessRule != null) {
			for (AbstractRule rule2 : this.accessRule) {
				AccessRule rule = (AccessRule) rule2;
				rule.setAccessRuleElementString(AccessRule.RESOURCEACCESSRULE_ELEMENT);
				buf.append(rule.toXMLString());
			}
		}

		if (this.stateRule != null) {
			buf.append(stateRule.toXMLString());
		}

		if (this.bandwidthControlRule != null) {
			buf.append(this.bandwidthControlRule.toXMLString());

		}

		buf.append("\n");
		buf.append("</");
		buf.append(RESOURCEPOLICY_ELEMENT);
		buf.append(">");

		return buf.toString();
	}
}
