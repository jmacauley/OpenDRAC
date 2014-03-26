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

package com.nortel.appcore.app.drac.client.lpcpadminconsole.topology;

import java.awt.Color;
import java.awt.EventQueue;

import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Record things about the graph we can set or save/restore.
 * 
 * @author pitman
 */
public final class JungGraphPreferences {
  private final Logger log = LoggerFactory.getLogger(getClass());

	public enum LineStyles {
		LINE_LINE, LINE_CUBIC_CURVE, LINE_QUAD_CURVE;
	}

	public enum NeLabels {
		OFF_NORMAL, OFF_SMALL, INSIDE, OUTSIDE_NORMAL, OUTSIDE_SMALL;
	}

	private NeLabels neLabel = NeLabels.INSIDE;
	private Color edgeColorSelected = Color.YELLOW;
	private Color edgeColorNetworkDiscovered = Color.GRAY;
	private Color edgeColorManual = Color.GRAY;
	private Color edgeColorNetworkDiscoveredAndManual = Color.GRAY;
	private LineStyles edgeLineStyle = LineStyles.LINE_QUAD_CURVE;

	private final JungTopologyPanel jungPanel;

	public JungGraphPreferences(JungTopologyPanel jp) {
		jungPanel = jp;
	}

	public Element asXml() {
		Element e = new Element("pref");
		e.setAttribute("neLabel", neLabel.name());
		e.setAttribute("edgeLineStyle", edgeLineStyle.name());
		e.setAttribute("edgeColorSelected", "" + edgeColorSelected.getRGB());
		e.setAttribute("edgeColorNetworkDiscovered", ""
		    + edgeColorNetworkDiscovered.getRGB());
		e.setAttribute("edgeColorManual", "" + edgeColorManual.getRGB());
		e.setAttribute("edgeColorNetworkDiscoveredAndManual", ""
		    + edgeColorNetworkDiscoveredAndManual.getRGB());
		return e;
	}

	/**
	 * Take a xml string and set preferences, return true if preferences were
	 * updated, false if not. Need to handle preferences from different releases!
	 */
	public boolean fromXml(Element xml) {
		/**
		 * <user id="admin"><preferencesList><pref neLabel="INSIDE"
		 * edgeLineStyle="LINE_QUAD_CURVE" edgeColorSelected="ffffff00"
		 * edgeColorNetworkDiscovered="ff808080" /></preferencesList></user>
		 */
		String input = new XMLOutputter().outputString(xml);
		log.debug("WP fromXml given " + input);

		Element prefList = xml.getChild("preferencesList");
		if (prefList == null) {
			log.debug("Failed to find preferencesList when parsing preferences "
			    + input);
			return false;
		}
		Element pref = prefList.getChild("pref");
		if (pref == null) {
			log.debug("Failed to find pref when parsing preferences " + input);
			return false;
		}

		boolean rc = false;

		String l = pref.getAttributeValue("neLabel");
		if (l != null) {
			try {
				neLabel = NeLabels.valueOf(l);
				rc = true;
			}
			catch (Exception e) {
				log.error("failed to update perference neLabel from " + input, e);
			}
		}

		l = pref.getAttributeValue("edgeLineStyle");
		if (l != null) {
			try {
				edgeLineStyle = LineStyles.valueOf(l);
				rc = true;
			}
			catch (Exception e) {
				log.error("failed to update perference edgeLineStyle from " + input, e);
			}
		}

		l = pref.getAttributeValue("edgeColorSelected");
		if (l != null) {
			try {
				edgeColorSelected = Color.decode(l);
				rc = true;
			}
			catch (Exception e) {
				log.error(
				    "failed to update perference edgeColorSelected from " + input, e);
			}
		}

		l = pref.getAttributeValue("edgeColorNetworkDiscovered");
		if (l != null) {
			try {
				edgeColorNetworkDiscovered = Color.decode(l);
				rc = true;
			}
			catch (Exception e) {
				log.error(
				    "failed to update perference edgeColorNetworkDiscovered from "
				        + input, e);
			}
		}

		l = pref.getAttributeValue("edgeColorManual");
		if (l != null) {
			try {
				edgeColorManual = Color.decode(l);
				rc = true;
			}
			catch (Exception e) {
				log.error("failed to update perference edgeColorManual from " + input,
				    e);
			}
		}

		l = pref.getAttributeValue("edgeColorNetworkDiscoveredAndManual");
		if (l != null) {
			try {
				edgeColorNetworkDiscoveredAndManual = Color.decode(l);
				rc = true;
			}
			catch (Exception e) {
				log.error(
				    "failed to update perference edgeColorNetworkDiscoveredAndManual from "
				        + input, e);
			}
		}

		// return true if something was updated.
		if (rc) {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						jungPanel.updateGraphPreferences();
					}
					catch (Exception e) {
						log.error("updateGraphPreferences ", e);
					}
				}
			});
		}
		return rc;
	}

	/**
	 * @return the edgeColorManual
	 */
	public Color getEdgeColorManual() {
		return edgeColorManual;
	}

	/**
	 * @return the edgeColorNetworkDiscovered
	 */
	public Color getEdgeColorNetworkDiscovered() {
		return edgeColorNetworkDiscovered;
	}

	/**
	 * @return the edgeColorManual
	 */
	public Color getEdgeColorNetworkDiscoveredAndManual() {
		return edgeColorNetworkDiscoveredAndManual;
	}

	/**
	 * @return the edgeColorSelected
	 */
	public Color getEdgeColorSelected() {
		return edgeColorSelected;
	}

	/**
	 * @return the edgeLineStyle
	 */
	public LineStyles getEdgeLineStyle() {
		return edgeLineStyle;
	}

	/**
	 * @return the neLabel
	 */
	public NeLabels getNeLabel() {
		return neLabel;
	}

	/**
	 * Go back to default values.
	 */
	public void resetToDefaults() {
		neLabel = NeLabels.INSIDE;
		edgeColorSelected = Color.YELLOW;
		edgeColorNetworkDiscovered = Color.GRAY;
		edgeColorManual = Color.GRAY;
		edgeColorNetworkDiscoveredAndManual = Color.GRAY;
		edgeLineStyle = LineStyles.LINE_QUAD_CURVE;
	}

	/**
	 * @param edgeColorNetworkDiscovered
	 *          the edgeColorNetworkDiscovered to set
	 */
	public void setEdgeColorManual(Color edgeColorManual) {
		this.edgeColorManual = edgeColorManual;
	}

	/**
	 * @param edgeColorNetworkDiscovered
	 *          the edgeColorNetworkDiscovered to set
	 */
	public void setEdgeColorNetworkDiscovered(Color edgeColorNetworkDiscovered) {
		this.edgeColorNetworkDiscovered = edgeColorNetworkDiscovered;
	}

	/**
	 * @param edgeColorNetworkDiscovered
	 *          the edgeColorNetworkDiscovered to set
	 */
	public void setEdgeColorNetworkDiscoveredAndManual(
	    Color edgeColorNetworkDiscoveredAndManual) {
		this.edgeColorNetworkDiscoveredAndManual = edgeColorNetworkDiscoveredAndManual;
	}

	/**
	 * @param edgeColorSelected
	 *          the edgeColorSelected to set
	 */
	public void setEdgeColorSelected(Color edgeColorSelected) {
		this.edgeColorSelected = edgeColorSelected;
	}

	/**
	 * @param edgeLineStyle
	 *          the edgeLineStyle to set
	 */
	public void setEdgeLineStyle(LineStyles edgeLineStyle) {
		this.edgeLineStyle = edgeLineStyle;
	}

	/**
	 * @param neLabel
	 *          the neLabel to set
	 */
	public void setNeLabel(NeLabels neLabel) {
		this.neLabel = neLabel;
	}
}
