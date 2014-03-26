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

package com.nortel.appcore.app.drac.server.lpcp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendrac.test.TestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.db.DbOpsHelper;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.DracVertex;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.SPF_KEYS;
import com.nortel.appcore.app.drac.common.utility.GenericJdomParser;
import com.nortel.appcore.app.drac.database.helper.test.DbTestPopulateDb;
import com.nortel.appcore.app.drac.server.lpcp.routing.HierarchicalModel;

@SuppressWarnings("unchecked")
public final class LpcpTest {
  private final Logger log = LoggerFactory.getLogger(getClass());
	/*
	 * The three node OME network
	 */
	private Lpcp lpcp;

	@Before
	public void setup() throws Exception {
		TestHelper.INSTANCE.initialize();
		DbTestPopulateDb.populateTestSystem(true);

		lpcp = new Lpcp(false);
		lpcp.initRouting();

		lpcp.startSchedulerService();

		HierarchicalModel modelMgr = lpcp.getDRACHierarchicalModel();

		// Build the facility model

		String OME0307 = "<nodeList><node id=\"00-21-E1-D6-D8-2C\" ip=\"47.134.3.228\" port=\"10001\"><layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-1\" type=\"OC12\" userLabel=\"label-OME0307_OC12-1-11-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"11\" neipForFac=\"47.134.3.228\" tna=\"OME0307_OC12-1-11-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D8-2C\" metric=\"1\" group=\"none\" primaryState=\"IS\" ps=\"N/A\" pk=\"00-21-E1-D6-D8-2C_OC12-1-11-1\" /><layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-12-1\" type=\"OC12\" userLabel=\"label-OME0307_OC12-1-12-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"12\" neipForFac=\"47.134.3.228\" tna=\"OME0307_OC12-1-12-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D8-2C\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D8-2C_OC12-1-12-1\" /></node></nodeList>";
		// modelMgr.parseAndAddToModel(OME0307);
		modelMgr.parseAndAddToModel(xmlToListFacility(OME0307));

		// VCAT enabled: WAN-1-1-4
		String OME0237 = "<nodeList>"
		    + "<node id=\"00-21-E1-D6-D5-DC\" ip=\"47.134.3.229\" port=\"10001\">"
		    + "<layer2 mtu=\"1600\" port=\"1\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-1\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-1\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"0016CA40C336\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" tna=\"OME0237_ETH-1-1-1\" cost=\"1\" autoNegotiationStatus=\"INPROGRESS\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AUMA\" group=\"none\" controlPauseTx=\"ENABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-1\" />"
		    + "<layer2 mtu=\"1600\" port=\"2\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-2\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-2\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" tna=\"OME0237_ETH-1-1-2\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AUMA\" group=\"none\" controlPauseTx=\"ENABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-2\" />"
		    + "<layer2 mtu=\"1600\" port=\"3\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-3\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-3\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" tna=\"OME0237_ETH-1-1-3\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AUMA\" group=\"none\" controlPauseTx=\"ENABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-3\" />"
		    + "<layer2 port=\"4\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-4\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0237_ETH-1-1-4\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.229\" tna=\"OME0237_ETH-1-1-4\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D5-DC_ETH-1-1-4\" ps=\"N/A\" />"
		    + "<layer2 port=\"4\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH-1-13-4\" type=\"ETH\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"unassigned\" slot=\"13\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.229\" tna=\"N/A\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" autoNegotiation=\"DISABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AU\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-21-E1-D6-D5-DC_ETH-1-13-4\" ps=\"N/A\" />"
		    + "<layer2 mtu=\"9216\" port=\"1\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-1\" type=\"ETH10G\" userLabel=\"label-OME0237_ETH10G-1-13-1\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.229\" tna=\"OME0237_ETH10G-1-13-1\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" autoNegotiation=\"DISABLE\" controlPauseRx=\"DISABLE\" group=\"none\" primaryState=\"OOS-AU\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_ETH10G-1-13-1\" />"
		    + "<layer2 mtu=\"9216\" port=\"2\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-2\" type=\"ETH10G\" userLabel=\"label-OME0237_ETH10G-1-13-2\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.229\" tna=\"OME0237_ETH10G-1-13-2\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" autoNegotiation=\"DISABLE\" controlPauseRx=\"DISABLE\" group=\"none\" primaryState=\"OOS-AU\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_ETH10G-1-13-2\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-1\" type=\"OC12\" userLabel=\"label-OME0237_OC12-1-11-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"11\" neipForFac=\"47.134.3.229\" tna=\"OME0237_OC12-1-11-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"IS-ANR\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC12-1-11-1\" />"
		    + "<layer1 port=\"2\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-2\" type=\"OC12\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"unassigned\" slot=\"11\" neipForFac=\"47.134.3.229\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" pk=\"00-21-E1-D6-D5-DC_OC12-1-11-2\" ps=\"N/A\" />"
		    + "<layer1 port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-3\" type=\"OC12\" userLabel=\"GREG: MY NEW OC12 TRIB!\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"11\" neipForFac=\"47.134.3.229\" tna=\"OME0237_OC12-1-11-3\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" pk=\"00-21-E1-D6-D5-DC_OC12-1-11-3\" ps=\"N/A\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-12-1\" type=\"OC12\" userLabel=\"label-OME0237_OC12-1-12-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"12\" neipForFac=\"47.134.3.229\" tna=\"OME0237_OC12-1-12-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"IS-ANR\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC12-1-12-1\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC192-1-9-1\" type=\"OC192\" userLabel=\"label-OME0237_OC192-1-9-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"9\" neipForFac=\"47.134.3.229\" tna=\"OME0237_OC192-1-9-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"IS-ANR\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC192-1-9-1\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC48-1-5-1\" type=\"OC48\" userLabel=\"label-OME0237_OC48-1-5-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"5\" neipForFac=\"47.134.3.229\" tna=\"OME0237_OC48-1-5-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_OC48-1-5-1\" />"
		    + "<layer1 port=\"2\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC48-1-5-2\" type=\"OC48\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"unassigned\" slot=\"5\" neipForFac=\"47.134.3.229\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" pk=\"00-21-E1-D6-D5-DC_OC48-1-5-2\" ps=\"N/A\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-1\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.229\" actualUnit=\"1\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" primaryState=\"OOS-MA\" group=\"none\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_WAN-1-1-1\" />"
		    + "<layer1 port=\"2\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-2\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.229\" actualUnit=\"1\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" primaryState=\"OOS-MA\" group=\"none\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_WAN-1-1-2\" />"
		    + "<layer1 port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-3\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.229\" actualUnit=\"UNKNOWN\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" primaryState=\"OOS-MA\" group=\"none\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_WAN-1-1-3\" />"
		    + "<layer1 port=\"4\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-4\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.229\" actualUnit=\"UNKNOWN\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" primaryState=\"OOS-MA\" group=\"none\" ps=\"N/A\" pk=\"00-21-E1-D6-D5-DC_WAN-1-1-4\" />"
		    + "<layer1 port=\"101\" valid=\"false\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-13-101\" type=\"WAN\" userLabel=\"N/A\" L2SS_FACILITY=\"true\" id=\"1\" manualProvision=\"false\" rate=\"NONE\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"13\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.229\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D5-DC\" metric=\"1\" primaryState=\"OOS-AU\" group=\"none\" pk=\"00-21-E1-D6-D5-DC_WAN-1-13-101\" ps=\"N/A\" />"
		    + "</node></nodeList>";
		// modelMgr.parseAndAddToModel(OME0237);
		modelMgr.parseAndAddToModel(xmlToListFacility(OME0237));

		// VCAT enabled: WAN-1-1-3, 1-1-4
		String OME0039 = "<nodeList><node id=\"00-21-E1-D6-D6-70\" ip=\"47.134.3.230\" port=\"10001\">"
		    + ""
		    + "<layer2 mtu=\"1600\" port=\"1\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-1\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-1\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"0016CA40C34D\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" tna=\"OME0039_ETH-1-1-1\" cost=\"1\" autoNegotiationStatus=\"INPROGRESS\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AUMA\" group=\"none\" controlPauseTx=\"ENABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-1\" />"
		    + "<layer2 mtu=\"1600\" port=\"2\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-2\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-2\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"0016CA40C34E\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" tna=\"OME0039_ETH-1-1-2\" cost=\"1\" autoNegotiationStatus=\"INPROGRESS\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AUMA\" group=\"none\" controlPauseTx=\"ENABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-2\" />"
		    + "<layer2 mtu=\"1600\" port=\"3\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-3\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-3\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"0016CA40C34F\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" tna=\"OME0039_ETH-1-1-3\" cost=\"1\" autoNegotiationStatus=\"INPROGRESS\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" autoNegotiation=\"ENABLE\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AUMA\" group=\"none\" controlPauseTx=\"ENABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-3\" />"
		    + "<layer2 port=\"4\" mtu=\"1600\" valid=\"true\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" txConditioning=\"ENABLE\" aid=\"ETH-1-1-4\" type=\"ETH\" passControlFrame=\"DISABLE\" userLabel=\"label-OME0039_ETH-1-1-4\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" slot=\"1\" signalingType=\"UNI\" speed=\"1000\" flowControl=\"ASYM\" neipForFac=\"47.134.3.230\" tna=\"OME0039_ETH-1-1-4\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"ENABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" group=\"none\" primaryState=\"OOS-AUMA\" controlPauseTx=\"ENABLE\" pk=\"00-21-E1-D6-D6-70_ETH-1-1-4\" ps=\"N/A\" />"
		    + "<layer2 port=\"4\" mtu=\"9216\" valid=\"true\" interfaceType=\"UNI\" constrain=\"0\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH-1-13-4\" type=\"ETH\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"unassigned\" slot=\"13\" speed=\"1000\" flowControl=\"NONE\" neipForFac=\"47.134.3.230\" tna=\"N/A\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" autoNegotiation=\"DISABLE\" metric=\"1\" controlPauseRx=\"UNKNOWN\" primaryState=\"OOS-AU\" group=\"none\" controlPauseTx=\"DISABLE\" pk=\"00-21-E1-D6-D6-70_ETH-1-13-4\" ps=\"N/A\" />"
		    + "<layer2 mtu=\"9216\" port=\"1\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-1\" type=\"ETH10G\" userLabel=\"label-OME0039_ETH10G-1-13-1\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.230\" tna=\"OME0039_ETH10G-1-13-1\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" autoNegotiation=\"DISABLE\" controlPauseRx=\"DISABLE\" group=\"none\" primaryState=\"OOS-AU\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_ETH10G-1-13-1\" />"
		    + "<layer2 mtu=\"9216\" port=\"2\" valid=\"true\" constrain=\"0\" interfaceType=\"UNI\" advertisedDuplex=\"UNKNOWN\" srlg=\"N/A\" aid=\"ETH10G-1-13-2\" type=\"ETH10G\" userLabel=\"label-OME0039_ETH10G-1-13-2\" L2SS_FACILITY=\"true\" priorityMode=\"PBITS\" id=\"2\" manualProvision=\"false\" neportForFac=\"10001\" anspeed=\"UNKNOWN\" encapsulationType=\"8100\" domain=\"N/A\" etherDuplex=\"FULL\" shelf=\"1\" physicalAddress=\"UNKNOWN\" signalingType=\"UNI\" slot=\"13\" speed=\"10000\" flowControl=\"NONE\" neipForFac=\"47.134.3.230\" tna=\"OME0039_ETH10G-1-13-2\" cost=\"1\" autoNegotiationStatus=\"UNKNOWN\" apsId=\"N/A\" policing=\"ENABLE\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" autoNegotiation=\"DISABLE\" controlPauseRx=\"DISABLE\" group=\"none\" primaryState=\"OOS-AU\" controlPauseTx=\"DISABLE\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_ETH10G-1-13-2\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-11-1\" type=\"OC12\" userLabel=\"label-OME0039_OC12-1-11-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"11\" neipForFac=\"47.134.3.230\" tna=\"OME0039_OC12-1-11-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_OC12-1-11-1\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC12-1-12-1\" type=\"OC12\" userLabel=\"label-OME0039_OC12-1-12-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"INNI\" slot=\"12\" neipForFac=\"47.134.3.230\" tna=\"OME0039_OC12-1-12-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"IS\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_OC12-1-12-1\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC48-1-5-1\" type=\"OC48\" userLabel=\"label-OME0039_OC48-1-5-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"5\" neipForFac=\"47.134.3.230\" tna=\"OME0039_OC48-1-5-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_OC48-1-5-1\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" aid=\"OC192-1-9-1\" type=\"OC192\" userLabel=\"label-OME0039_OC192-1-9-1\" id=\"1\" manualProvision=\"false\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" signalingType=\"UNI\" slot=\"9\" neipForFac=\"47.134.3.230\" tna=\"OME0039_OC192-1-9-1\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_OC192-1-9-1\" />"
		    + "<layer1 port=\"1\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-1\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"1\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" primaryState=\"OOS-MA\" group=\"none\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_WAN-1-1-1\" />"
		    + "<layer1 port=\"2\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-2\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"0\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AUMA\" pk=\"00-21-E1-D6-D6-70_WAN-1-1-2\" ps=\"N/A\" />"
		    + "<layer1 port=\"3\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-3\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"0\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AUMA\" pk=\"00-21-E1-D6-D6-70_WAN-1-1-3\" ps=\"N/A\" />"
		    + "<layer1 port=\"4\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-1-4\" type=\"WAN\" userLabel=\"N/A\" mode=\"SONET\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"1\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"0\" provUnit=\"1\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AUMA\" pk=\"00-21-E1-D6-D6-70_WAN-1-1-4\" ps=\"N/A\" />"
		    + "<layer1 port=\"103\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-13-103\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"13\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_WAN-1-13-103\" />"
		    + "<layer1 port=\"104\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-13-104\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"13\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_WAN-1-13-104\" />"
		    + "<layer1 port=\"101\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-13-101\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"13\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_WAN-1-13-101\" />"
		    + "<layer1 port=\"102\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-13-102\" type=\"WAN\" L2SS_FACILITY=\"true\" userLabel=\"N/A\" id=\"1\" manualProvision=\"false\" rate=\"STS3C\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"13\" signalingType=\"unassigned\" vcat=\"DISABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" group=\"none\" primaryState=\"OOS-AU\" ps=\"N/A\" pk=\"00-21-E1-D6-D6-70_WAN-1-13-102\" />"
		    + "<layer1 port=\"105\" valid=\"true\" constrain=\"0\" srlg=\"N/A\" mapping=\"GFP-F\" aid=\"WAN-1-13-105\" type=\"WAN\" userLabel=\"N/A\" L2SS_FACILITY=\"true\" id=\"1\" manualProvision=\"false\" rate=\"NONE\" neportForFac=\"10001\" domain=\"N/A\" shelf=\"1\" slot=\"13\" signalingType=\"unassigned\" vcat=\"ENABLE\" lcas=\"DISABLE\" neipForFac=\"47.134.3.230\" actualUnit=\"UNKNOWN\" tna=\"N/A\" cost=\"1\" apsId=\"N/A\" siteId=\"N/A\" neidForFac=\"00-21-E1-D6-D6-70\" metric=\"1\" primaryState=\"OOS-AU\" group=\"none\" pk=\"00-21-E1-D6-D6-70_WAN-1-13-105\" ps=\"N/A\" /></node>"
		    + "</nodeList>";
		// modelMgr.parseAndAddToModel(OME0039);
		modelMgr.parseAndAddToModel(xmlToListFacility(OME0039));

		// Build the topo/routing graph
		int vertexId = 0;
		DracVertex v1 = new DracVertex("OME0307", "00-21-E1-D6-D8-2C",
		    "47.134.3.228", "10001", "SONET", NeType.OME6, null, null,
		    NeStatus.NE_ALIGNED, Integer.toString(vertexId++), null, null);
		DracVertex v2 = new DracVertex("OME0237", "00-21-E1-D6-D5-DC",
		    "47.134.3.229", "10001", "SONET", NeType.OME6, null, null,
		    NeStatus.NE_ALIGNED, Integer.toString(vertexId++), null, null);
		DracVertex v3 = new DracVertex("OME0039", "00-21-E1-D6-D6-70",
		    "47.134.3.230", "10001", "SONET", NeType.OME6, null, null,
		    NeStatus.NE_ALIGNED, Integer.toString(vertexId++), null, null);

		lpcp.getTopologyMgr().addVertexToGraph(v1);
		lpcp.getTopologyMgr().addVertexToGraph(v2);
		lpcp.getTopologyMgr().addVertexToGraph(v3);

		// On a real installation, edge(link) events are received in both
		// orientations, but the opposite
		// orientation generates a duplicate link exception log in LPCP_PORT.
		// parms: source, dest, srcPort, tgtPort, cost, metric2, srlg

		// OME0237 - OME0307
		int nextEdgeId = 0;
		DracEdge edge1 = new DracEdge(v2, "OC12-1-11-1", v1, "OC12-1-12-1",
		    new Double(1.0), "1.0", "1.0", "N/A", Integer.toString(nextEdgeId++));
		DracEdge edge2 = new DracEdge(v3, "OC12-1-12-1", v1, "OC12-1-11-1",
		    new Double(1.0), "1.0", "1.0", "N/A", Integer.toString(nextEdgeId++));
		DracEdge edge3 = new DracEdge(v2, "OC12-1-12-1", v3, "OC12-1-11-1",
		    new Double(1.0), "1.0", "1.0", "N/A", Integer.toString(nextEdgeId++));

		// OME0237 - OME0307
		lpcp.getTopologyMgr().addEdgeToGraph(edge1);
		// OME0307 - OME0039
		lpcp.getTopologyMgr().addEdgeToGraph(edge2);
		// OME0039 - OME0237
		lpcp.getTopologyMgr().addEdgeToGraph(edge3);

		// Generates a lot of output, don't print it.
		// lpcp.displayTrackers();
	}

	@After
	public void teardown() throws Exception {
	}

	@Test
	public void test1() throws Exception {
		log.debug("===========================Test 1===========================");

		/*
		 * This test case demonstrates the current behaviour associated with routing
		 * a vcat service from a L2 ingress to a L1 handoff. The reqt from Surfnet
		 * was to enforce contiguous member provisioning on the L1 facility.
		 * Currently (without the SystemProperty override set), the routing engine
		 * will not route the vcat service if the L1 egress does not provision
		 * contiguously. As well, note that a L1 channel selection of -1 (default
		 * channel selection) can will result in the routing engine attempting to
		 * 'pack' the 150mbs vcat group members starting on timeslot 1 of the egress
		 * port. If contiguous b/w is available at higher channel boundaries, the
		 * user should specify the starting channel explicitly. This scenario has
		 * the short hop OC12 filled, the egress OC48 occupied on channels 7-9,
		 * 19-21, 31-33, 43-45. [1] Because the OME supports only 3C, 12C, 24C, 48C,
		 * and 192C, a request of 450 mbs (9C) CCAT rounds up to 12C and should fail
		 * to route. [2] 450 mbs VCAT with destination channel -1 should fail
		 * because contiguous b/w is not enforced on egress. [3] 450 mbs VCAT with
		 * destination channel 10 should succeed.
		 */

		// Prepare input parameter map
		Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();
		params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-1");
		params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
		params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");
		params.put(SPF_KEYS.SPF_USER, "admin");
		params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");
		params.put(SPF_KEYS.SPF_OFFSET, "30000");

		// --- Constraints ---
		params.put(SPF_KEYS.SPF_COST, "-1");
		params.put(SPF_KEYS.SPF_MBS, "450");
		params.put(SPF_KEYS.SPF_METRIC2, "-1");
		params.put(SPF_KEYS.SPF_HOP, "-1");

		// --- Routing options ---
		params.put(SPF_KEYS.SPF_PROTECTION, "UNPROTECTED");
		params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "false");

		params.put(SPF_KEYS.SPF_RATE, "450");

		params.put(SPF_KEYS.SPF_SRCVLAN, "7");
		params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_ETH10G-1-13-1");
		params.put(SPF_KEYS.SPF_SRCCHANNEL, "-1");

		params.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC48-1-5-1");
		params.put(SPF_KEYS.SPF_DSTCHANNEL, "-1");

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (30 * 60 + 1000);
		params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
		params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
		// Some casting required by the routing method
		Map<SPF_KEYS, Object> m = new HashMap<SPF_KEYS, Object>();
		for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}

		String srcNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_SRCTNA));
		String dstNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_DSTTNA));
		m.put(SPF_KEYS.SPF_SOURCEID, srcNeId);
		m.put(SPF_KEYS.SPF_TARGETID, dstNeId);

		// Pump in some used bandwidth
		m.put(SPF_KEYS.SPF_EXCLUDE, getOverlappingConnections());

		// [1]
		try {
			new LpcpScheduler(lpcp).schedule(m);
			fail("This is supposed to fail. ");
		}
		catch (RoutingException re) {
			// Expected result
		}

		// [2]
		try {
			m.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "true");
			new LpcpScheduler(lpcp).schedule(m);
			log.error("this failed!", new Exception("StackTrace"));
			fail("This is supposed to fail.");
		}
		catch (RoutingException re) {
			// Expected result
		}

		// [3]
		m.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "true");
		m.put(SPF_KEYS.SPF_DSTCHANNEL, "10");
		new LpcpScheduler(lpcp).schedule(m);
	}

	@Test
	// Simple
	public void test15() throws Exception {
		Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();

		params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-1");
		params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
		params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");
		params.put(SPF_KEYS.SPF_USER, "admin");
		params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");
		params.put(SPF_KEYS.SPF_OFFSET, "30000");
		// --- Constraints ---
		params.put(SPF_KEYS.SPF_COST, "-1");
		params.put(SPF_KEYS.SPF_MBS, "450");
		params.put(SPF_KEYS.SPF_METRIC2, "-1");
		params.put(SPF_KEYS.SPF_HOP, "-1");
		// --- Routing options ---
		params.put(SPF_KEYS.SPF_PROTECTION, "UNPROTECTED");
		params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "true");
		params.put(SPF_KEYS.SPF_RATE, "300");

		// EPL style card, with WAN 'parent' in the HierarchicalModel.
		// There were ETH/WAN translation errors with this going into the
		// tracker.
		// Ensure that takeBandwidth produces no error logs!
		params.put(SPF_KEYS.SPF_SRCTNA, "OME0237_ETH-1-1-4");
		params.put(SPF_KEYS.SPF_SRCCHANNEL, "1");

		params.put(SPF_KEYS.SPF_DSTTNA, "OME0039_ETH-1-1-4");
		params.put(SPF_KEYS.SPF_DSTCHANNEL, "1");

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (30 * 60 + 1000);
		params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
		params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
		Map<SPF_KEYS, Object> m = new HashMap<SPF_KEYS, Object>();
		for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}

		String srcNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_SRCTNA));
		String dstNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_DSTTNA));
		m.put(SPF_KEYS.SPF_SOURCEID, srcNeId);
		m.put(SPF_KEYS.SPF_TARGETID, dstNeId);
		new LpcpScheduler(lpcp).schedule(m);

	}

	@Test
	public void test2() throws Exception {
		log.debug("===========================Test 2===========================");

		// Prepare input parameter map
		Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();

		params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-2");
		params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
		params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");

		params.put(SPF_KEYS.SPF_USER, "admin");

		params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");

		params.put(SPF_KEYS.SPF_OFFSET, "30000");

		// --- Constraints ---
		params.put(SPF_KEYS.SPF_COST, "-1");
		params.put(SPF_KEYS.SPF_MBS, "150");
		params.put(SPF_KEYS.SPF_METRIC2, "-1");
		params.put(SPF_KEYS.SPF_HOP, "-1");

		// --- Routing options ---
		params.put(SPF_KEYS.SPF_PROTECTION, "UNPROTECTED");
		params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "false");

		params.put(SPF_KEYS.SPF_RATE, "150");
		params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_ETH-1-1-1");
		params.put(SPF_KEYS.SPF_SRCCHANNEL, "-1");

		params.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC48-1-5-1");
		params.put(SPF_KEYS.SPF_DSTCHANNEL, "-1");

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (30 * 60 + 1000);
		params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
		params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
		// Some casting required by the routing method
		Map<SPF_KEYS, Object> m = new HashMap<SPF_KEYS, Object>();
		for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}

		String srcNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_SRCTNA));
		String dstNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_DSTTNA));

		m.put(SPF_KEYS.SPF_SOURCEID, srcNeId);
		m.put(SPF_KEYS.SPF_TARGETID, dstNeId);

		// Pump in some used bandwidth
		m.put(SPF_KEYS.SPF_EXCLUDE, getOverlappingConnections());

		try {
			// OPTICAL FACILITY TRACKER
			// Verify that the 3C must be on a valid boundary
			m.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC48-1-5-1");
			m.put(SPF_KEYS.SPF_DSTCHANNEL, "2");

			new LpcpScheduler(lpcp).schedule(m);
			fail("This is supposed to fail.");
		}
		catch (RoutingException re) {
			// Expected result
		}

		try {
			// OPTICAL FACILITY TRACKER
			// Verify that if the requested channel is not available, the
			// routing request should fail.
			m.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC48-1-5-1");
			m.put(SPF_KEYS.SPF_DSTCHANNEL, "7");
			new LpcpScheduler(lpcp).schedule(m);
			fail("This is supposed to fail.");
		}
		catch (RoutingException re) {
			// Expected result
		}

		// Success path
		// OPTICAL FACILITY TRACKER
		m.put(SPF_KEYS.SPF_DSTTNA, "OME0237_OC48-1-5-1");
		m.put(SPF_KEYS.SPF_DSTCHANNEL, "4");
		new LpcpScheduler(lpcp).schedule(m);
	}

	@Test
	// VCAT
	public void test3() throws Exception {
		log.debug("===========================Test 3===========================");

		// Prepare input parameter map
		Map<SPF_KEYS, String> params = new HashMap<SPF_KEYS, String>();

		params.put(SPF_KEYS.SPF_SCHEDULE_NAME, "LpcpTest-3");
		params.put(SPF_KEYS.SPF_ACTIVATION_TYPE, "RESERVATION_AUTOMATIC");
		params.put(SPF_KEYS.SPF_SERVICE_STATUS, "EXECUTION_PENDING");

		params.put(SPF_KEYS.SPF_USER, "admin");

		params.put(SPF_KEYS.SPF_CONTROLLER_ID, "47.134.40.205:8001");

		params.put(SPF_KEYS.SPF_OFFSET, "30000");

		// --- Constraints ---
		params.put(SPF_KEYS.SPF_COST, "-1");
		params.put(SPF_KEYS.SPF_METRIC2, "-1");
		params.put(SPF_KEYS.SPF_HOP, "-1");

		// --- Routing options ---
		params.put(SPF_KEYS.SPF_PROTECTION, "UNPROTECTED");
		params.put(SPF_KEYS.SPF_VCATROUTING_OPTION, "true");

		params.put(SPF_KEYS.SPF_RATE, "600");
		params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_ETH-1-1-4");
		params.put(SPF_KEYS.SPF_SRCCHANNEL, "-1");

		params.put(SPF_KEYS.SPF_DSTTNA, "OME0237_ETH-1-1-4");
		params.put(SPF_KEYS.SPF_DSTCHANNEL, "-1");

		long startTime = System.currentTimeMillis();
		long endTime = startTime + (30 * 60 + 1000);
		params.put(SPF_KEYS.SPF_START_TIME, Long.toString(startTime));
		params.put(SPF_KEYS.SPF_END_TIME, Long.toString(endTime));
		// Some casting required by the routing method
		Map<SPF_KEYS, Object> m = new HashMap<SPF_KEYS, Object>();
		for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}

		String srcNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_SRCTNA));
		String dstNeId = lpcp.getModelMgr().getNeIdForTNA(
		    (String) m.get(SPF_KEYS.SPF_DSTTNA));

		m.put(SPF_KEYS.SPF_SOURCEID, srcNeId);
		m.put(SPF_KEYS.SPF_TARGETID, dstNeId);

		// Success path
		// ETHWAN TRACKER - VCAT
		// Four vcat members, each taking short path
		{
			new LpcpScheduler(lpcp).schedule(m);
			List<CrossConnection> cons = (List<CrossConnection>) m
			    .get(SPF_KEYS.SPF_RT_PATH_DATA);
			assertTrue(cons.size() == 8);
		}

		// Eight vcat members, four (2xXcon each) taking short path, four
		// (3xXcon each) taking long path with
		// the passthrough node
		{

			m = new HashMap<SPF_KEYS, Object>();
			for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
				m.put(e.getKey(), e.getValue());
			}
			srcNeId = lpcp.getModelMgr().getNeIdForTNA(
			    (String) m.get(SPF_KEYS.SPF_SRCTNA));
			dstNeId = lpcp.getModelMgr().getNeIdForTNA(
			    (String) m.get(SPF_KEYS.SPF_DSTTNA));
			m.put(SPF_KEYS.SPF_SOURCEID, srcNeId);
			m.put(SPF_KEYS.SPF_TARGETID, dstNeId);

			m.put(SPF_KEYS.SPF_RATE, "1050");
			new LpcpScheduler(lpcp).schedule(m);
			List<CrossConnection> cons = (List<CrossConnection>) m
			    .get(SPF_KEYS.SPF_RT_PATH_DATA);
			assertTrue(cons.size() == 17);
		}

		// same test, but drop one connection onto the passthrough - that blocks
		// one member and therefore the
		// whole vcat request
		{
			m = new HashMap<SPF_KEYS, Object>();
			for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
				m.put(e.getKey(), e.getValue());
			}
			srcNeId = lpcp.getModelMgr().getNeIdForTNA(
			    (String) m.get(SPF_KEYS.SPF_SRCTNA));
			dstNeId = lpcp.getModelMgr().getNeIdForTNA(
			    (String) m.get(SPF_KEYS.SPF_DSTTNA));
			m.put(SPF_KEYS.SPF_SOURCEID, srcNeId);
			m.put(SPF_KEYS.SPF_TARGETID, dstNeId);

			m.put(SPF_KEYS.SPF_RATE, "1200");

			List<CrossConnection> exclusions = new ArrayList<CrossConnection>();

			String sourceAid = "OC12-1-12-1";
			String targetAid = "OC12-1-11-1";
			String neId = "00-21-E1-D6-D8-2C";
			String rate = "STS1";
			String serviceId = "DRAC-cd28862f-1259097939999";

			Map<String, String> c = new HashMap<String, String>();
			c.put(CrossConnection.SOURCE_NEID, neId);
			c.put(CrossConnection.TARGET_NEID, neId);
			c.put(CrossConnection.SOURCE_PORT_AID, sourceAid);
			c.put(CrossConnection.TARGET_PORT_AID, targetAid);
			c.put(CrossConnection.RATE, rate);
			c.put(CrossConnection.SOURCE_CHANNEL, "10");
			c.put(CrossConnection.TARGET_CHANNEL, "10");
			c.put(CrossConnection.CKTID, serviceId);
			exclusions.add(new CrossConnection(c));
			m.put(SPF_KEYS.SPF_EXCLUDE, getOverlappingConnections());

			try {
				new LpcpScheduler(lpcp).schedule(m);
			}
			catch (RoutingException re) {
				// Expected result
				assert re.getErrorCode() == DracErrorConstants.LPCP_E3020_NO_PATH_FOR_SPECIFIED_PARAMETERS;
			}
		}

		// Nodal
		{
			params.put(SPF_KEYS.SPF_SRCTNA, "OME0039_ETH-1-1-3");
			params.put(SPF_KEYS.SPF_SRCCHANNEL, "1");

			params.put(SPF_KEYS.SPF_DSTTNA, "OME0039_ETH-1-1-4");
			params.put(SPF_KEYS.SPF_DSTCHANNEL, "1");

			m = new HashMap<SPF_KEYS, Object>();
			for (Map.Entry<SPF_KEYS, String> e : params.entrySet()) {
				m.put(e.getKey(), e.getValue());
			}
			srcNeId = lpcp.getModelMgr().getNeIdForTNA(
			    (String) m.get(SPF_KEYS.SPF_SRCTNA));
			dstNeId = srcNeId;
			m.put(SPF_KEYS.SPF_SOURCEID, srcNeId);
			m.put(SPF_KEYS.SPF_TARGETID, dstNeId);

			m.put(SPF_KEYS.SPF_RATE, "600");
			new LpcpScheduler(lpcp).schedule(m);
			List<CrossConnection> cons = (List<CrossConnection>) m
			    .get(SPF_KEYS.SPF_RT_PATH_DATA);
			assertTrue(cons.size() == 4);
		}

	}

	// These are the connections:

	// OC48-1-5-1-7
	// OC12-1-12-1-1
	// .
	// OC48-1-5-1-19
	// OC12-1-12-1-4
	// .
	// OC48-1-5-1-31
	// OC12-1-12-1-7
	// .
	// OC48-1-5-1-43
	// OC12-1-12-1-10
	private List<CrossConnection> getOverlappingConnections() {
		List<CrossConnection> exclusions = new ArrayList<CrossConnection>();

		String sourceAid = "OC12-1-12-1";
		String targetAid = "OC48-1-5-1";
		String neId = "00-21-E1-D6-D5-DC";
		String rate = "STS3C";
		// String serviceId = "DRAC-cd28862f-1259097930078";

		Map<String, String> c = new HashMap<String, String>();

		c.put(CrossConnection.SOURCE_NEID, neId);
		c.put(CrossConnection.TARGET_NEID, neId);
		c.put(CrossConnection.SOURCE_PORT_AID, sourceAid);
		c.put(CrossConnection.TARGET_PORT_AID, targetAid);
		c.put(CrossConnection.RATE, rate);
		c.put(CrossConnection.SOURCE_CHANNEL, "1");
		c.put(CrossConnection.TARGET_CHANNEL, "7");
		c.put(CrossConnection.CKTID, "DRAC-cd28862f-1259097930078");

		// result[ServiceXml.SOURCECHANNEL_IDX] = "1";
		// result[ServiceXml.TARGETCHANNEL_IDX] = "7";
		// result[ServiceXml.CCT_IDX] = "cd28862f-1259097930078";
		// ch = new CrossConnection(neId, neId, sourceAid, targetAid, rate, "1",
		// "7", serviceId, null, null,
		// null, null,
		// "cd28862f-1259097930078", null, null, null, null);
		exclusions.add(new CrossConnection(c));

		// result[ServiceXml.SOURCECHANNEL_IDX] = "4";
		// result[ServiceXml.TARGETCHANNEL_IDX] = "19";
		// result[ServiceXml.CCT_IDX] = "cd28862f-1259091649808";
		// ch = new CrossConnection(neId, neId, sourceAid, targetAid, rate, "4",
		// "19", serviceId, null, null,
		// null, null,
		// "cd28862f-1259091649808", null, null, null, null);
		c.put(CrossConnection.SOURCE_CHANNEL, "4");
		c.put(CrossConnection.TARGET_CHANNEL, "19");
		c.put(CrossConnection.CKTID, "DRAC-cd28862f-1259091649808");
		exclusions.add(new CrossConnection(c));

		// result[ServiceXml.SOURCECHANNEL_IDX] = "7";
		// result[ServiceXml.TARGETCHANNEL_IDX] = "31";
		// result[ServiceXml.CCT_IDX] = "cd28862f-1259097814196";
		// ch = new CrossConnection(neId, neId, sourceAid, targetAid, rate, "7",
		// "31", serviceId, null, null,
		// null, null,
		// "cd28862f-1259097814196", null, null, null, null);
		c.put(CrossConnection.SOURCE_CHANNEL, "7");
		c.put(CrossConnection.TARGET_CHANNEL, "31");
		c.put(CrossConnection.CKTID, "DRAC-cd28862f-1259097814196");
		exclusions.add(new CrossConnection(c));

		// result[ServiceXml.SOURCECHANNEL_IDX] = "10";
		// result[ServiceXml.TARGETCHANNEL_IDX] = "43";
		// result[ServiceXml.CCT_IDX] = "cd28862f-1259097872744";
		// ch = new CrossConnection(neId, neId, sourceAid, targetAid, rate,
		// "10", "43", serviceId, null, null,
		// null, null,
		// "cd28862f-1259097872744", null, null, null, null);

		c.put(CrossConnection.SOURCE_CHANNEL, "10");
		c.put(CrossConnection.TARGET_CHANNEL, "43");
		c.put(CrossConnection.CKTID, "DRAC-cd28862f-1259097872744");
		exclusions.add(new CrossConnection(c));
		return exclusions;
	}

	private List<Facility> xmlToListFacility(String xml) throws Exception {
		List<Facility> facList = new ArrayList<Facility>();

		GenericJdomParser jparser = new GenericJdomParser();
		jparser.parse(xml);
		Element root = jparser.getRoot();
		List<Element> nodes = root.getChildren("node");
		for (int i = 0; i < nodes.size(); i++) {
			Element node = nodes.get(i);
			List<Element> facListElements = node.getChildren();
			for (int j = 0; j < facListElements.size(); j++) {
				// Map<String, String> map = new HashMap<String, String>();
				// List<Attribute> attrList =
				// facListElements.get(j).getAttributes();
				// for (Attribute attr : attrList)
				// {
				// map.put(attr.getName(), attr.getValue());
				// }
				//
				// facList.add(new Facility(map));

				facList.add(new Facility(DbOpsHelper.elementToMap(facListElements
				    .get(j))));
			}
		}

		return facList;
	}
}
