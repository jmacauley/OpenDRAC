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
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.DracException;
import com.nortel.appcore.app.drac.common.security.policy.PolicyCheckablePolicy;
import com.nortel.appcore.app.drac.common.security.policy.types.UserGroupProfileXML.AccessPermission;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;

public final class GroupPolicy implements Serializable, PolicyCheckablePolicy {
	private static final long serialVersionUID = 1;
	private final Logger log = LoggerFactory.getLogger(getClass());
	// UserGroupPolicy XML Element and attribute constants.
	public static final String USERGOURPPOLICY_ELEMENT = "groupPolicy";

	// UserGroupPolicy fields
	private List<AbstractRule> accessControlRule;
	private List<AbstractRule> systemAccessRule;
	private AbstractRule bandwidthControlRule;

	// private boolean empty = true;

	// public GroupPolicy(List<AbstractRule> accessControlRule, List<AbstractRule>
	// systemAccessRule,
	// AbstractRule bandwidthControlRule)
	// {
	// super();
	// this.accessControlRule = accessControlRule;
	// this.systemAccessRule = systemAccessRule;
	// this.bandwidthControlRule = bandwidthControlRule;
	// }

	public void fromXML(Element root) throws Exception {
		/*********************************************************************/
		/* Constructs UserGroupType element. */
		/*********************************************************************/
		Element userGroupType = root.getChild(USERGOURPPOLICY_ELEMENT);

		if (userGroupType != null) {
			List<Element> elements = userGroupType.getChildren();

			if (elements == null) {
				// this.empty = true;
				return;
			}

			Element policy = null;
			for (Element element : elements) {
				policy = element;
				AbstractRule rule;
				if (policy.getName().equals(AccessRule.SYSTEMACCESSRULE_ELEMENT)) {
					rule = new AccessRule(AccessPermission.DENY);
					rule.fromXML(policy);
					if (this.systemAccessRule == null) {
						this.systemAccessRule = new AccessRuleList();
					}
					this.systemAccessRule.add(rule);
					// this.empty = false;
				}
				else if (policy.getName().equals(
				    AccessControlRule.ACCESSCONTROLRULE_ELEMENT)) {
					rule = new AccessControlRule();
					rule.fromXML(policy);
					if (this.accessControlRule == null) {
						this.accessControlRule = new ArrayList<AbstractRule>();
					}
					this.accessControlRule.add(rule);
					// this.empty = false;
				}
				else if (policy.getName().equals(
				    BandwidthControlRule.BANDWIDTHCONTROLRULE_ELEMENT)) {
					rule = new BandwidthControlRule();
					rule.fromXML(policy);
					this.bandwidthControlRule = rule;
					// this.empty = false;
				}
				else {
					DracException ex = new DracException(
					    DracErrorConstants.SECURITY_ERROR_XML_ATTR_MISSING,
					    new Object[] { policy.getName() });
					log.error(ex.getMessage() + " " + XmlUtility.rootNodeToString(root),
					    ex);
					throw ex;
				}
			}
		}
		else {
			
		}

	}

	public List<AbstractRule> getAccessControlRule() {
		return accessControlRule;
	}

	public List<AbstractRule> getAccessRule() {
		return systemAccessRule;
	}

	public AbstractRule getBandwidthControlRule() {
		return bandwidthControlRule;
	}

	public boolean isEmpty() {

		if (this.bandwidthControlRule != null
		    && !((BandwidthControlRule) this.bandwidthControlRule).isEmpty()) {
			return false;
		}

		if (this.systemAccessRule != null && !this.systemAccessRule.isEmpty()) {
			return false;
		}

		if (this.accessControlRule != null && !this.accessControlRule.isEmpty()) {
			return false;
		}

		return true;
	}

	public void setAccessControlRule(List<AbstractRule> accessControlRule) {
		this.accessControlRule = accessControlRule;
	}

	public void setBandwidthControlRule(AbstractRule bandwidthControlRule) {
		this.bandwidthControlRule = bandwidthControlRule;
	}

	public void setSystemAccessRule(List<AbstractRule> systemAccessRule) {
		this.systemAccessRule = systemAccessRule;
	}

	public String toXMLString() {

		StringBuilder buf = new StringBuilder();

		buf.append("<");
		buf.append(USERGOURPPOLICY_ELEMENT);
		buf.append(">");

		if (this.systemAccessRule != null) {
			for (AbstractRule rule : this.systemAccessRule) {
				buf.append("\n");
				buf.append(rule.toXMLString());
			}
		}

		if (this.accessControlRule != null) {
			for (AbstractRule rule : this.accessControlRule) {
				buf.append("\n");
				buf.append(rule.toXMLString());
			}
		}

		if (this.bandwidthControlRule != null) {
			buf.append("\n");
			buf.append(this.bandwidthControlRule.toXMLString());
		}

		buf.append("\n");
		buf.append("</");
		buf.append(USERGOURPPOLICY_ELEMENT);
		buf.append(">");

		return buf.toString();
	}

}
