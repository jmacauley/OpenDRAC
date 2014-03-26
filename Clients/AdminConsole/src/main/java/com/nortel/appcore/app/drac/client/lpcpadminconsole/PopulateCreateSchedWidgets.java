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

package com.nortel.appcore.app.drac.client.lpcpadminconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.client.lpcpadminconsole.common.Tna;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.tabs.CreateScheduleTab;
import com.nortel.appcore.app.drac.client.lpcpadminconsole.util.ServerOperation;
import com.nortel.appcore.app.drac.common.FacilityConstants;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.Layer;

/**
 * 
 */
public final class PopulateCreateSchedWidgets implements Runnable {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
	private final OpenDracDesktop desktop;
	private final JComboBox srcTNAbox;
	private final JComboBox dstTNAbox;
	private final Map<Layer, List<Tna>> srcTNAMap;
	private final Map<Layer, List<Tna>> dstTNAMap;
	private final Layer layer;
	private final JFrame parent;
	private final String srcIeee;
	private final String dstIeee;
	private static final String SRCTNABOX_STR = "SRCTNABOX";
	private static final String DSTTNABOX_STR = "DSTTNABOX";
	private static final String SRCTNAMAP_STR = "SRCTNAMAP";
	private static final String DSTTNAMAP_STR = "DSTTNAMAP";

	/*
	 * this.desktop.getCurrentView().getAddLinkSource("IEEE")
	 * this.desktop.getCurrentView().getAddLinkDest("IEEE")
	 */
	public PopulateCreateSchedWidgets(OpenDracDesktop desk, JFrame jFrame,
	    JComboBox srcTNA, JComboBox dstTNA, Map<Layer, List<Tna>> srcMap,
	    Map<Layer, List<Tna>> dstMap, Layer l, String sourceIeee, String destIeee) {
		desktop = desk;
		srcTNAbox = srcTNA;
		dstTNAbox = dstTNA;
		srcTNAMap = srcMap;
		dstTNAMap = dstMap;
		layer = l;
		parent = jFrame;
		srcIeee = sourceIeee;
		dstIeee = destIeee;
	}

	@Override
	public void run() {
		try {

			String boxIdxs[] = { SRCTNABOX_STR, DSTTNABOX_STR };

			String tnaIdxs[] = { SRCTNAMAP_STR, DSTTNAMAP_STR };

			Map<String, JComboBox> boxMap = new HashMap<String, JComboBox>();
			Map<String, Map<Layer, List<Tna>>> tnaMap = new HashMap<String, Map<Layer, List<Tna>>>();

			parent.setCursor(OpenDracDesktop.WAIT_CURSOR);

			boxMap.put(SRCTNABOX_STR, srcTNAbox);
			boxMap.put(DSTTNABOX_STR, dstTNAbox);
			tnaMap.put(SRCTNAMAP_STR, srcTNAMap);
			tnaMap.put(DSTTNAMAP_STR, dstTNAMap);

			List<Facility> srcTNAs = new ServerOperation().listFacalities(srcIeee);
			List<Facility> dstTNAs = new ServerOperation().listFacalities(dstIeee);

			List<List<Facility>> tnaXML = new ArrayList<List<Facility>>();
			tnaXML.add(srcTNAs);
			tnaXML.add(dstTNAs);

			
			

			for (int i = 0; i < boxIdxs.length; i++) {
				try {
					populateTNAMap(tnaXML.get(i), tnaMap.get(tnaIdxs[i]));
					CreateScheduleTab.getInstance(desktop).populateBoxForLayer(layer,
					    boxMap.get(boxIdxs[i]), tnaMap.get(tnaIdxs[i]));
				}
				catch (Exception e) {
					log.error(
					    "Exception reading facilities for " + tnaIdxs[i] + " TNA: ", e);
				}
			}

			CreateScheduleTab.getInstance(desktop).populateSrcSiteBox(srcTNAMap);
			CreateScheduleTab.getInstance(desktop).populateDstSiteBox(dstTNAMap);
			parent.setCursor(OpenDracDesktop.DEFAULT_CURSOR);
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
	}

	private void populateTNAMap(List<Facility> children,
	    Map<Layer, List<Tna>> tnaMap) {
		Tna tna;
		List<Tna> list = null;

		tnaMap.clear();
		for (Facility child : children) {
			// skip endpoints that are not UNI or ENNI.
			if (!FacilityConstants.SIGNAL_TYPE.UNI.toString().equalsIgnoreCase(
			    child.getSigType())
			    && !FacilityConstants.SIGNAL_TYPE.ENNI.toString().equalsIgnoreCase(
			        child.getSigType())) {
				continue;
			}

			// Filter out endpoints with invalid TNAs.
			if ("N/A".equalsIgnoreCase(child.getTna())) {
				continue;
			}

			

			tna = new Tna(child.getNeId(), child.getAid(), child.getTna(),
			    child.getLayer(), child.get(OpenDracDesktop.TYPE_ATTR),
			    child.getWavelength(), child.getSite());

			if (Layer.LAYER0.equals(child.getLayer())) {
				if (tnaMap.get(Layer.LAYER0) == null) {
					list = new ArrayList<Tna>();
					list.add(tna);
					tnaMap.put(Layer.LAYER0, list);
				}
				else {
					list = tnaMap.get(Layer.LAYER0);
					list.add(tna);
				}
			}
			else if (Layer.LAYER1.equals(child.getLayer())) {
				if (tnaMap.get(Layer.LAYER1) == null) {
					list = new ArrayList<Tna>();
					list.add(tna);
					tnaMap.put(Layer.LAYER1, list);
				}
				else {
					list = tnaMap.get(Layer.LAYER1);
					list.add(tna);
				}
			}
			else if (Layer.LAYER2.equals(child.getLayer())) {
				if (tnaMap.get(Layer.LAYER2) == null) {
					list = new ArrayList<Tna>();
					list.add(tna);
					tnaMap.put(Layer.LAYER2, list);
				}
				else {
					list = tnaMap.get(Layer.LAYER2);
					list.add(tna);
				}
			}

		}
	}

}
