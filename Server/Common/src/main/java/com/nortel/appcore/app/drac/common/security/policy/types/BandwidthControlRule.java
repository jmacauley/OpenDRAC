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

public final class BandwidthControlRule extends AbstractRule {
	private static final long serialVersionUID = 1L;
	// BandwidthControlRule XML Element and Attribute Constants
	public static final String BANDWIDTHCONTROLRULE_ELEMENT = "bandwidthControlRule";
	public static final String MAXSERVICESIZE_ELEMENT = "maximumServiceSize";
	public static final String MAXSERVICEDURATION_ELEMENT = "maximumServiceDuration";
	public static final String MAXSERVICEBANDWIDTH_ELEMENT = "maximumServiceBandwidth";
	public static final String MAXAGGSERVICESIZE_ELEMENT = "maximumAggregateServiceSize";

	// BandwidthControlRule fields
	private Integer maximumServiceSize;
	private Integer maximumServiceDuration;
	private Integer maximumServiceBandwidth;
	private Integer maximumAggregateServiceSize;

	@Override
	public void fromXML(Element node) throws Exception {
		/*********************************************************************/
		/* Constructs BandwidthControl element. */
		/*********************************************************************/
		// Element bwControlRule = root.getChild(BANDWIDTHCONTROLRULE_ELEMENT);
		Element bwControlRule = node;

		String temp;
		if (bwControlRule != null) {

			super.fromXML(node);

			temp = bwControlRule.getChildText(MAXSERVICESIZE_ELEMENT);
			if (temp != null && !temp.equals("")) {
				maximumServiceSize = Integer.valueOf(temp);
			}

			temp = bwControlRule.getChildText(MAXSERVICEDURATION_ELEMENT);
			if (temp != null && !temp.equals("")) {
				maximumServiceDuration = Integer.valueOf(temp);
			}

			temp = bwControlRule.getChildText(MAXSERVICEBANDWIDTH_ELEMENT);
			if (temp != null && !temp.equals("")) {
				maximumServiceBandwidth = Integer.valueOf(temp);
			}

			temp = bwControlRule.getChildText(MAXAGGSERVICESIZE_ELEMENT);
			if (temp != null && !temp.equals("")) {
				maximumAggregateServiceSize = Integer.valueOf(temp);
			}
		}

	}

	public Integer getMaximumAggregateServiceSize() {
		return maximumAggregateServiceSize;
	}

	public Integer getMaximumServiceBandwidth() {
		return maximumServiceBandwidth;
	}

	public Integer getMaximumServiceDuration() {
		return maximumServiceDuration;
	}

	public Integer getMaximumServiceSize() {
		return maximumServiceSize;
	}

	public boolean isEmpty() {
		if (maximumServiceSize != null) {
			return false;
		}
		if (maximumServiceDuration != null) {
			return false;
		}
		if (maximumServiceBandwidth != null) {
			return false;
		}
		if (maximumAggregateServiceSize != null) {
			return false;
		}

		return true;
	}

	public void setMaximumAggregateServiceSize(Integer maximumAggregateServiceSize) {
		this.maximumAggregateServiceSize = maximumAggregateServiceSize;
	}

	public void setMaximumServiceBandwidth(Integer maximumServiceBandwidth) {
		this.maximumServiceBandwidth = maximumServiceBandwidth;
	}

	public void setMaximumServiceDuration(Integer maximumServiceDuration) {
		this.maximumServiceDuration = maximumServiceDuration;
	}

	public void setMaximumServiceSize(Integer maximumServiceSize) {
		this.maximumServiceSize = maximumServiceSize;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append('\n');
		buf.append(XmlUtility.toXMLString(MAXSERVICESIZE_ELEMENT,
		    maximumServiceSize));

		buf.append('\n');
		buf.append(XmlUtility.toXMLString(MAXSERVICEDURATION_ELEMENT,
		    maximumServiceDuration));

		buf.append('\n');
		buf.append(XmlUtility.toXMLString(MAXSERVICEBANDWIDTH_ELEMENT,
		    maximumServiceBandwidth));

		buf.append('\n');
		buf.append(XmlUtility.toXMLString(MAXAGGSERVICESIZE_ELEMENT,
		    maximumAggregateServiceSize));

		return buf.toString();
	}

	public String toXMLBodyString() {
		return toString();
	}

	@Override
	public String toXMLString() {
		StringBuilder buf = new StringBuilder();
		buf.append('<');
		buf.append(BANDWIDTHCONTROLRULE_ELEMENT);
		buf.append(super.toXMLString());
		buf.append('>');

		buf.append(toXMLBodyString());

		buf.append('\n');
		buf.append("</");
		buf.append(BANDWIDTHCONTROLRULE_ELEMENT);
		buf.append('>');
		return buf.toString();
	}

}
