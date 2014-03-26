/**
service * <pre>
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

import java.math.BigInteger;
import java.net.InetAddress;
import java.rmi.Naming;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.opendrac.security.InternalLoginHelper;
import org.opendrac.security.InternalLoginHelper.InternalLoginTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.auditlogs.LogKeyEnum;
import com.nortel.appcore.app.drac.common.auditlogs.LogRecord;
import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.errorhandling.DracErrorConstants;
import com.nortel.appcore.app.drac.common.errorhandling.RoutingException;
import com.nortel.appcore.app.drac.common.graph.DracEdge;
import com.nortel.appcore.app.drac.common.graph.NeStatus;
import com.nortel.appcore.app.drac.common.graph.NeType;
import com.nortel.appcore.app.drac.common.info.RmiServerInfo;
import com.nortel.appcore.app.drac.common.types.AuditResult;
import com.nortel.appcore.app.drac.common.types.BandwidthRecord;
import com.nortel.appcore.app.drac.common.types.ControllerType;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.JvmType;
import com.nortel.appcore.app.drac.common.types.LpcpStatus;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.OsType;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.ScheduleResult;
import com.nortel.appcore.app.drac.common.types.ServerInfoType;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.dracdb.DbLog;
import com.nortel.appcore.app.drac.database.helper.DbUtilityCommonUtility;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;
import com.nortel.appcore.app.drac.server.lpcp.common.Utility;
import com.nortel.appcore.app.drac.server.lpcp.rmi.LpcpInterface;
import com.nortel.appcore.app.drac.server.lpcp.rmi.LpcpInterfaceImpl;
import com.nortel.appcore.app.drac.server.lpcp.rmi.NeProxyRmiMediator;
import com.nortel.appcore.app.drac.server.lpcp.routing.Engine;
import com.nortel.appcore.app.drac.server.lpcp.routing.HierarchicalModel;
import com.nortel.appcore.app.drac.server.lpcp.routing.TopologyManager;
import com.nortel.appcore.app.drac.server.lpcp.scheduler.LightPathScheduler;
import com.nortel.appcore.app.drac.server.lpcp.trackers.BasicTracker;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;
import com.nortel.appcore.app.drac.server.lpcp.trackers.SonetTrackerI;

/**
 * DRAC Light Path Control Plane - handles connection requests and starts
 * routing engine and scheduler.
 *
 * @author adlee
 */

public class Lpcp {
	private final Logger log = LoggerFactory.getLogger(getClass());

	public enum ServerState {
		DISC_PRE_INIT, DISC_GOT_NES, DISC_GOT_FACS, DISC_GOT_ADJS, DISC_GOT_CRS, RESTART_COMPLETE, SVRSTATE_INACTIVE;

		public int asInt() {
			switch (this) {
			case DISC_PRE_INIT:
				return -1;
			case DISC_GOT_NES:
				return 1;
			case DISC_GOT_FACS:
				return 2;
			case DISC_GOT_ADJS:
				return 3;
			case DISC_GOT_CRS:
				return 4;
			case RESTART_COMPLETE:
				return 5;
			case SVRSTATE_INACTIVE:
				// admin console checks for this value!
				return -100;
			default:
				return 0;
			}
		}

		public String asString() {
			String stateStr = null;
			switch (this) {
			case DISC_PRE_INIT:
				stateStr = "Server starting, stage 1 of 5";
				break;
			case DISC_GOT_NES:
				stateStr = "Server restarting, stage 2 of 5";
				break;
			case DISC_GOT_FACS:
				stateStr = "Server restarting, stage 3 of 5";
				break;
			case DISC_GOT_ADJS:
				stateStr = "Server restarting, stage 4 of 5";
				break;
			case DISC_GOT_CRS:
				stateStr = "Server restarting, stage 5 of 5";
				break;
			case RESTART_COMPLETE:
				// DRACDesktop hard codes this string
				stateStr = "Server running";
				break;
			case SVRSTATE_INACTIVE:
				// DRACDesktop hard codes this string
				stateStr = "Server inactive";
				break;
			default:
				stateStr = "Unknown";
				break;
			}
			return stateStr;
		}
	}

	public static final String REQ_CANCEL_SERVICE = "deleteService";
	public static final String PRT_PATH1PLUS1 = "PATH1PLUS1";
	private ServerState serverState = ServerState.DISC_PRE_INIT;
	private boolean isRestarting;
	private static String id = "unknown";
	private String globalOffset;
	private DiscoveryManager discoveryMgr;
	private LpcpInterfaceImpl lpcpInterfaceImpl;

	private final DbUtilityLpcpScheduler dbutility;
	private Engine routingEngine;
	private LightPathScheduler scheduler;
	private static String lpcpUid = "";

	public Lpcp(boolean fullInit) {
		dbutility = DbUtilityLpcpScheduler.INSTANCE;
		initModels(fullInit);
	}

	public String getLpcpUid() {
		return lpcpUid;
	}

	public void activateService(String serviceId) throws Exception {


		SERVICE curServiceStatus = DbUtilityLpcpScheduler.INSTANCE
		    .getServiceStatus(serviceId);

		if (SERVICE.ACTIVATION_PENDING == curServiceStatus) {
			// int newStatus = SERVICE.EXECUTION_PENDING.ordinal();
			// Notify listeners of new service state
			updateScheduleAndLogServiceChange(
			    DbUtilityLpcpScheduler.INSTANCE.getServiceFromServiceId(serviceId),
			    "activateService", SERVICE.EXECUTION_PENDING);
			DbUtilityLpcpScheduler.INSTANCE.updateServiceByServiceId(serviceId,
			    SERVICE.EXECUTION_PENDING);
			scheduler.notifyThreads();
		}
		else {
			log.error("Service activation failed.  Service '" + serviceId
			    + "' in INVALID state.  Service state: " + curServiceStatus);
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3046_SERVICE_IN_INVALID_STATE);
		}
	}

	/**
	 * auditCorrectModel: Set the bit trackers according to the connections found
	 * in the network.
	 */
	public void auditCorrectModel() throws Exception {
		log.debug("auditCorrectModel invoked");

		/*
		 * Sequentially walk through the entire hierarchical model and compare each
		 * layer 1 port's usage with the actual connections in the db. Currently we
		 * only audit layer 1!
		 */

		synchronized (HierarchicalModel.INSTANCE) {
			for (String neId : HierarchicalModel.INSTANCE.getModel().keySet()) {
				for (LpcpFacility fac : HierarchicalModel.INSTANCE.getModel().get(neId)
				    .values()) {
					BasicTracker tracker = fac.getTracker();
					if (!(tracker instanceof SonetTrackerI)) {
						continue; // skip layer 0 and 2
					}

					SonetTrackerI stracker = (SonetTrackerI) tracker;
					BigInteger conBigInt = getConnectionsBigInt(neId, fac.getAid());
					BigInteger bigInt = convertBitsetToBigInteger(stracker
					    .getInternalTracker());

					if (!bigInt.toString().equalsIgnoreCase(conBigInt.toString())) {
						log.debug("Correcting model for " + fac.toString());
						// First, clear all the bits in the tracker
						stracker.getInternalTracker().clear();
						/*
						 * Next, apply the bits that the connections are using in the db to
						 * our internal tracker
						 */

						for (int i = 1; i < conBigInt.bitLength(); i++) {
							if (conBigInt.testBit(i)) {
								stracker.getInternalTracker().set(i);
							}
						}
					}
				}
			}
		}
		log.debug("auditCorrectModel done");
	}

	/**
	 * Compare the bit trackers according to the connections found in the network.
	 * Generally reflects the orphaned connections. The audit is only going to
	 * compare the trackers in memory against the OSS connections in DRAC's
	 * database. If DRAC's database is out of sync then the audit will not know.
	 */
	public List<AuditResult> auditModel() throws Exception {
        /* Store our audit results here. */
		List<AuditResult> results = new ArrayList<AuditResult>();

        /* Track mismatches here. */
		List<AuditResult> mismatches = new ArrayList<AuditResult>();

        /*
		 * Sequentially walk through the entire hierarchical model and compare each
		 * layer 1 port's usage with the actual connections in the db. Currently we
		 * only audit layer 1!
		 */
		synchronized (HierarchicalModel.INSTANCE) {
			for (String neId : HierarchicalModel.INSTANCE.getModel().keySet()) {
				for (LpcpFacility fac : HierarchicalModel.INSTANCE.getModel().get(neId)
				    .values()) {
					BasicTracker tracker = fac.getTracker();
					if (!(tracker instanceof SonetTrackerI)) {
						continue; // skip layer 0 and 2
					}

                    /* The SONET payload tracker for this facility. */
					SonetTrackerI stracker = (SonetTrackerI) tracker;

                    /* Convert NE connection list for facility to a bit map. */
					BigInteger conBigInt = getConnectionsBigInt(neId, fac.getAid());

                    /* Add the bits which are constraints and not for use by
                     * OpenDRAC.  I think this is what we are trying to achieve
                     * but am not 100% sure.  Cross your fingers. -- John
                     */
					conBigInt = conBigInt.or(stracker.getConstraints().getConstraintsInteger());

                    /* Get the internal OpenDRAC version of the bandwidth usage. */
					BigInteger bigInt = convertBitsetToBigInteger(stracker.getInternalTracker());

                    /* Compare the internal version of the bitmap to the network version with constraints. */
					boolean matched = false;
                    if (bigInt.compareTo(conBigInt) == 0) {
                        matched = true;
                    }

                    /* Create a new audit result. */
					AuditResult r = new AuditResult(neId, HierarchicalModel.INSTANCE.getTidForNeId(neId), fac.getAid(), fac.getRate(), !matched, bigInt, conBigInt);

					if (matched) {
						results.add(r);

                        if (log.isDebugEnabled()) {
                            log.debug("auditModel: NE " + HierarchicalModel.INSTANCE.getTidForNeId(neId) + " " + neId + " facility " + fac.getAid() + " matched!");
                        }
					}
					else {
                        mismatches.add(r);

                        if (log.isDebugEnabled()) {
                            StringBuilder sb = new StringBuilder("auditModel: NE ");
                            sb.append(HierarchicalModel.INSTANCE.getTidForNeId(neId));
                            sb.append(" ");
                            sb.append(neId);
                            sb.append(" facility ");
                            sb.append(fac.getAid());

                            sb.append(" mismatched! Network view = ");
                            sb.append(conBigInt.toString());
                            sb.append(", OpenDRAC view = ");
                            sb.append(bigInt.toString());
                            sb.append(", mismatched slots = ( ");

                            BigInteger mismatched = conBigInt.xor(bigInt);
                            int timeslot = mismatched.getLowestSetBit();
                            while (timeslot > 0) {
                                sb.append(timeslot);
                                sb.append(" ");
                                mismatched = mismatched.clearBit(timeslot);
                                timeslot = mismatched.getLowestSetBit();
                            }

                            sb.append(" )");
                            log.debug(sb.toString());
                        }
                    }
				}
			}

			if (mismatches.size() > 0) {
				/* Group mismatches at the front. */
				results.addAll(0, mismatches);
			}
		}

		return results;
	}

	public void cancelService(String[] serviceIds, SERVICE state)
	    throws Exception {
		if (serviceIds == null) {
			log.error("Cannot cancel service. serviceIds is null ");
			throw new Exception("Cannot cancel service, service id is null");
		}

		log.debug("Cancelling services " + Arrays.toString(serviceIds));

		for (String element : serviceIds) {
			// Ask the light path scheduler to cancel the service
			try {
				scheduler.cancelSchedule(element, state);
				scheduler.notifyThreads();
			}
			catch (Exception e) {
				log.error("Exception cancelling service with id: " + element, e);
				throw new Exception("Failed to cancel service '" + id + "'", e);
			}
		}
	}

	public void confirmService(String serviceId) throws Exception {
		SERVICE curServiceStatus = DbUtilityLpcpScheduler.INSTANCE
		    .getServiceStatus(serviceId);
		if (SERVICE.CONFIRMATION_PENDING != curServiceStatus) {
			throw new RoutingException(
			    DracErrorConstants.LPCP_E3046_SERVICE_IN_INVALID_STATE);
		}

		String curScheduleActType = DbUtilityLpcpScheduler.INSTANCE
		    .getActivationTypeForService(serviceId);
		log.debug("confirmService: Schedule activation type for service: "
		    + serviceId + " is: " + curScheduleActType);
		if (Schedule.ACTIVATION_TYPE.valueOf(curScheduleActType).ordinal() == Schedule.ACTIVATION_TYPE.PRERESERVATION_MANUAL
		    .ordinal()) {
			DbUtilityLpcpScheduler.INSTANCE.updateServiceByServiceId(serviceId,
			    SERVICE.ACTIVATION_PENDING);
		}
		else {
			DbUtilityLpcpScheduler.INSTANCE.updateServiceByServiceId(serviceId,
			    SERVICE.EXECUTION_PENDING);
		}
		// Wake up the scheduling threads
		scheduler.notifyThreads();
	}

	public void editFacility(String neid, String aid, String tna,
	    String faclabel, String mtu, String srlg, String grp, String cost,
	    String metric2, String sigType, String constraints, String domainId,
	    String siteId) throws Exception {
		NeProxyRmiMediator.INSTANCE.editFacility(neid, aid, tna, faclabel, mtu,
		    srlg, grp, cost, metric2, sigType, constraints, domainId, siteId);

		synchronized (HierarchicalModel.INSTANCE) {
			HierarchicalModel.INSTANCE.editFacility(neid, aid, tna, faclabel, mtu,
			    srlg, grp, cost, metric2, sigType, constraints, domainId, siteId);
		}

		DracEdge e = TopologyManager.INSTANCE.getEdge(neid, aid);
		if (e == null) {
			log.debug("Could not find edge for " + neid + " " + aid
			    + " while editing facility ");
		}
		else {
			if (srlg != null) {
				e.setSrlg(srlg);
			}
			if (cost != null) {
				e.setCost(cost);
			}
			if (metric2 != null) {
				e.setMetric(metric2);
			}
		}
		// Could have changed something, reconsolidate the graph.
		TopologyManager.INSTANCE.requestConsolidation();
	}

	// public String displayTracker(String neid, String facid)
	// {
	// List<ServiceXml> overlappingSchedules = null;
	// ArrayList<CrossConnection> connections = new ArrayList<CrossConnection>();
	// String sTime = Long.toString(System.currentTimeMillis());
	// String eTime = Long.toString(System.currentTimeMillis());
	// String result = null;
	// ServiceXml schedule = null;
	//
	// overlappingSchedules = queryInterval(sTime, eTime);
	//
	// if (overlappingSchedules != null)
	// {
	// for (int i = 0; i < overlappingSchedules.size(); i++)
	// {
	// schedule = overlappingSchedules.get(i);
	// for (int j = 0; j < schedule.numberOfConnectionsInPath(); j++)
	// {
	// connections.add(schedule.getCrossConnectionInfo(j));
	// }
	// }
	// }
	// synchronized (HierarchicalModel.INSTANCE)
	// {
	// try
	// {
	// // Mark in-use bandwidth in preparation for bandwidth utilisation query
	// HierarchicalModel.INSTANCE.markBandwidth(connections);
	// result = HierarchicalModel.INSTANCE.displayTracker(neid, facid);
	// }
	// finally
	// {
	// HierarchicalModel.INSTANCE.unmarkBandwidth(connections);
	// }
	// }
	// return result;
	// }

	// public String displayTrackers()
	// {
	// return HierarchicalModel.INSTANCE.displayTrackers();
	// }

	public double getCurrentBandwidthUsage(String tna) throws Exception {
		List<CrossConnection> overLapconnections = new ArrayList<CrossConnection>();

		// query with a start and end time of now for current usage.
		String now = Long.toString(System.currentTimeMillis());
		List<ServiceXml> overlappingSchedules = queryInterval(now, now);

		if (overlappingSchedules != null) {
			for (ServiceXml schedule : overlappingSchedules) {
				for (int j = 0; j < schedule.numberOfConnectionsInPath(); j++) {
					overLapconnections.add(schedule.getCrossConnectionInfo(j));
				}
			}
		}

		synchronized (HierarchicalModel.INSTANCE) {
			log.debug("getCurrentBandWidthUsage Overlapping schedules: "
			    + overlappingSchedules + " Overlapping connections: "
			    + overLapconnections);

			try {
				// Mark in-use bandwidth in preparation for bandwidth utilisation query
				HierarchicalModel.INSTANCE.markBandwidth(overLapconnections);

				LpcpFacility facility = HierarchicalModel.INSTANCE
				    .getFacilityForTNA(tna);
				log.debug("getCurrentBandWidthUsage tna " + tna
				    + " resolves to facility: " + facility);
				
        // RH: Added so that retrieving all NE's will not fail if one of them is broken.
				if (facility != null) {
					try {
            return HierarchicalModel.INSTANCE.getFacUtilisation(
                facility.getNeId(), facility.getAid());
          }
          catch (Exception e) {
            log.error("Unable to compute bandwidth for tna: "+facility.getNeId());
          }
				}
				
//				throw new Exception("Could not compute bandwith usage for '" + tna
//				    + "' no facility in model!");
				log.error("Could not compute bandwith usage for '" + tna
            + "' no facility in model!");
				return -1l;
			}
			catch (Exception e) {
				log.error("Could not compute bandwith usage for " + tna, e);
//				throw new Exception("Could not compute bandwith usage for " + tna, e);
				return -1l;
			}
			finally {
				HierarchicalModel.INSTANCE.unmarkBandwidth(overLapconnections);
			}
		}
	}

	//
	// public boolean isStop()
	// {
	// return stop;
	// }

	/**
	 * @return the discoveryMgr
	 */
	public DiscoveryManager getDiscoveryMgr() {
		return discoveryMgr;
	}

	public String getDiscStatus() {
		return "<discStatus activeCount=\"" + discoveryMgr.getActiveJobCount()
		    + "\"/>";
	}

	public HierarchicalModel getDRACHierarchicalModel() {
		return HierarchicalModel.INSTANCE;
	}

	public ServerInfoType getInfo() throws Exception {
		final String buildId = "org.opendrac "
		    + System.getProperty("org.opendrac.version");

		final ServerInfoType serverInfoType = new ServerInfoType();

		ControllerType controllerInfo = new ControllerType();
		controllerInfo.setConnectionActive(0);
		controllerInfo.setConnectionHandled(0);
		controllerInfo.setFaults(0);
		controllerInfo.setId(id);
		controllerInfo.setIpAddress(InetAddress.getLocalHost().getHostAddress());
		controllerInfo.setPort("0");
		controllerInfo.setStatus(isRestarting ? "Restarting" : "Running");
		controllerInfo.setVersion(buildId);
		controllerInfo.setTimeZoneId(TimeZone.getDefault().getID());

		serverInfoType.setControllerInfo(controllerInfo);

		OsType osInfo = new OsType(System.getProperty("os.name"),
		    System.getProperty("os.version"), System.getProperty("os.arch"));
		serverInfoType.setOsInfo(osInfo);
		// s.setSystemOverhead(systemOverhead);

		JvmType vmInfo = new JvmType(System.getProperty("java.vm.name"),
		    System.getProperty("java.vm.vendor"),
		    System.getProperty("java.vm.version"));
		serverInfoType.setVmInfo(vmInfo);
		return serverInfoType;
	}

	/**
	 * Return bandwidth utilization of all ports with topological links on them,
	 * used for the admin console to give a view of the internal state of the
	 * network usage
	 */
	public List<BandwidthRecord> getInternalBandwithUsage(long startTime,
	    long endTime) throws Exception {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM yyyy HH:mm:ss.SSS");
			sdf.setTimeZone(TimeZone.getDefault());
			log.debug("getInternalBandwithUsage Start time: " + startTime + " ("
			    + sdf.format(new Date(startTime)) + ") End time: " + endTime + " ("
			    + sdf.format(new Date(endTime)) + ")");

			ServiceXml schedule = null;
			List<CrossConnection> overlappingConnections = new ArrayList<CrossConnection>();
			List<ServiceXml> overlappingSchedules = queryInterval(
			    Long.toString(startTime), Long.toString(endTime));
			if (overlappingSchedules != null) {
				for (int i = 0; i < overlappingSchedules.size(); i++) {
					schedule = overlappingSchedules.get(i);
					for (int j = 0; j < schedule.numberOfConnectionsInPath(); j++) {
						overlappingConnections.add(schedule.getCrossConnectionInfo(j));
					}
				}
			}
			log.debug("Overlapping schedules: " + overlappingSchedules
			    + " Overlapping connections: " + overlappingConnections);

			synchronized (HierarchicalModel.INSTANCE) {
				try {
					// Mark in-use bandwidth in preparation for bandwidth utilisation
					// query
					HierarchicalModel.INSTANCE.markBandwidth(overlappingConnections);
					return TopologyManager.INSTANCE
					    .QueryBWUsage(HierarchicalModel.INSTANCE.getModel());
				}
				finally {
					HierarchicalModel.INSTANCE.unmarkBandwidth(overlappingConnections);
				}
			}
		}
		catch (Exception e) {
			log.error("Exception querying utilisation: ", e);
			throw e;
		}
	}

	public String getPeerIp() {
		return System.getProperty("org.opendrac.controller.secondary");
	}

	public Engine getRoutingEngine() {
		return routingEngine;
	}

	public LightPathScheduler getScheduler() {
		return scheduler;
	}

	/**
	 * Returns a comma delimited list of SRLG or "" in none apply.
	 */
	public String getSRLGListForServiceId(String serviceId) throws Exception {
		StringBuilder result = new StringBuilder();

		List<String> neidList = new ArrayList<String>();
		List<String> portList = new ArrayList<String>();
		Map<String, String> uniqueMap = new HashMap<String, String>();

		ServiceXml service = retrieveService(serviceId);

		if (service != null) {

			for (CrossConnection edge : service.getCrossConnections()) {

				String srcNeid = edge.getSourceNeId();
				String tgtNeid = edge.getTargetNeId();
				String srcPortAid = edge.getSourcePortAid();
				String tgtPortAid = edge.getTargetPortAid();
				neidList.add(srcNeid);
				neidList.add(tgtNeid);
				portList.add(srcPortAid);
				portList.add(tgtPortAid);

				for (int j = 0; j < neidList.size(); j++) {
					LpcpFacility facility = HierarchicalModel.INSTANCE.getFacility(
					    neidList.get(j), portList.get(j));
					if (facility != null) {
						if (facility.getSRLG() != null
						    && !"N/A".equalsIgnoreCase(facility.getSRLG())) {

							log.debug("uniqueMap.get(" + facility.getSRLG() + ") is: "
							    + uniqueMap.get(facility.getSRLG()));
							if (uniqueMap.get(facility.getSRLG()) == null) {
								log.debug("unique map does not contain: " + facility.getSRLG()
								    + " adding to uniqueMap");
								uniqueMap.put(facility.getSRLG(), facility.getSRLG());
							}
						}
						log.debug("SRLG value: |" + facility.getSRLG() + "| assigned to: "
						    + neidList.get(j) + " port: " + portList.get(j));

					}
					else {
						log.error("Cannot find facility for neid: " + neidList.get(j)
						    + " aid: " + portList.get(j));
					}
				}

			}


			for (String srlg : uniqueMap.keySet()) {
				result.append(srlg);
				result.append(",");
			}

			if (result.length() > 0) {
				// remove the last comma
				result.setLength(result.length() - 1);
			}

		}


		return result.toString();
	}

	public LpcpStatus getStatus() {
		return new LpcpStatus(isRestarting, serverState.asInt(), serverState.asString());
	}

	public TopologyManager getTopologyMgr() {
		return TopologyManager.INSTANCE;
	}

	public HierarchicalModel getModelMgr() {
		return HierarchicalModel.INSTANCE;
	}

	public void handleDelNodeRequest(NetworkElementHolder oldNe) throws Exception {
		String ip = oldNe.getIp();
		String port = oldNe.getPort();
		String ieee = oldNe.getId();
		log.debug("handleDelNodeRequest: ip:" + ip + " port:" + port + " ieee:"
		    + ieee);

		NeProxyRmiMediator.INSTANCE.deleteNetworkElement(oldNe);
		/*
		 * Remove the discover thread from the DiscoveryManager if the NE unmanaged
		 * was in the process of being discovered
		 */
		discoveryMgr.removeFromDiscoveryQ(ip);

	}

	/**
	 * The lpcp routing graph has changed, tell the admin client to refresh
	 * itself. No extra data required.
	 */
	public void notifyClientsGraphRefreshRequired() {
		LpcpEventHandler.getInstance(this).notifyClientsGraphRefreshRequired();
	}

	public ServiceXml retrieveService(String serviceId) {

		Map<String, Object> serviceFilter = new HashMap<String, Object>();
		serviceFilter.put(DbKeys.LightPathCols.LP_SERVICEID, serviceId);
		try {
			List<ServiceXml> results = DbLightPath.INSTANCE.retrieve(serviceFilter);
			if (results != null && results.size() == 1) {
				return results.get(0);
			}
		}
		catch (Exception e) {
			log.error("retrieveService:: failed to retrieve service record for id: "
			    + serviceId, e);
		}

		return null;
	}

	public void start() { // NO_UCD
		try {
			String dracIp = System.getProperty("org.opendrac.controller.primary",
			    "localhost");
			lpcpUid = Utility.getReverseIpHashFromIp(dracIp);
			log.debug("Unique id set to: " + lpcpUid);
			init();
		}
		catch (Exception t) {
			log.error("Exception starting LPCP_PORT component of DRAC: ", t);
		}
	}

	/**
	 * called when a service changes it state. We used to bundle up this as an
	 * event and ship it to all registered clients, including the NRB_PORT which
	 * would use the information in the event to update the schedule and write an
	 * audit log. We now do that here and don't forward the events outside of
	 * LPCP_PORT.
	 */
	public synchronized void updateScheduleAndLogServiceChange(
	    ServiceXml service, String command, SERVICE status) {


	  log.info("Updating schedule and service called for service {}, with command {} and service status {}", new Object[]{service, command, status});

		try {
			String serviceId = service.getServiceId();
			log.debug("updateScheduleAndLogServiceChange: serviceId:" + serviceId
			    + " operation:" + command + " status:" + status);

			DracService aService = DbUtilityCommonUtility.INSTANCE
			    .queryServiceSummaryFromServiceId(serviceId);
			Schedule sched = DbUtilityCommonUtility.INSTANCE
			    .queryScheduleFromServiceId(aService.getId());
			// State.SERVICE newServerState = aService.getStatus();
			LogKeyEnum auditKeyEnum = null;
			// only care about the expired, failed to delete, ... for now

			switch (status) {
			case EXECUTION_INPROGRESS:
				auditKeyEnum = LogKeyEnum.KEY_SERVICE_STARTED;
				break;
			case EXECUTION_TIMED_OUT:
				auditKeyEnum = LogKeyEnum.KEY_SERVICE_FINISHED;
				break;
			case CREATE_FAILED:
				auditKeyEnum = LogKeyEnum.KEY_SERVICE_START_FAILED;
				break;
			case DELETE_FAILED:
				auditKeyEnum = LogKeyEnum.KEY_SERVICE_FINISHED_FAILED;
				break;
			case ACTIVATION_TIMED_OUT:
				auditKeyEnum = LogKeyEnum.KEY_SERVICE_ACTIVATION_TIMEOUT;
				break;
			/*
			 * @TODO Wayne Nov 25,2010 We are not logging these states, perhaps we
			 * should
			 */
			case ACTIVATION_CANCELLED:
				break;
			case ACTIVATION_PENDING:
				break;
			case CONFIRMATION_CANCELLED:
				break;
			case CONFIRMATION_PENDING:
				break;
			case CONFIRMATION_TIMED_OUT:
				break;
			case EXECUTION_CANCELLED:
				break;
			case EXECUTION_FAILED:
				break;
			case EXECUTION_PARTIALLY_CANCELLED:
				break;
			case EXECUTION_PENDING:
				break;
			case EXECUTION_SUCCEEDED:
				break;
			default:
				log.error("updateScheduleAndLogServiceChange Unexpected SERVICE value "
				    + status);
				break;
			}

			if (auditKeyEnum != null) {
				DbLog.INSTANCE.generateLog(new LogRecord(null, null, sched
				    .getUserInfo().getBillingGroup(), serviceId, auditKeyEnum));
			}

			DbUtilityCommonUtility.INSTANCE.updateScheduleStatus(aService
			    .getScheduleId());
		}
		catch (Exception e) {
			log.error(
			    "updateScheduleAndLogServiceChange Failed to update service/schedule states "
			        + service + " " + command + " " + status, e);
		}
	}

    /**
     * Initialize the lightpath control plane's routing engine using the
     * current topology graph.
     *
     * @return true if the routing engine was successfully created and
     * initialized.
     */
	protected boolean initRouting() {
		boolean rc = true;
		try {
            // Create the routing engine instance using our current tolology.
			routingEngine = new Engine(TopologyManager.INSTANCE);
		}
		catch (Exception e) {
			log.error("Exception initialsing routing", e);
		}
		return rc;
	}

	protected boolean startSchedulerService() {
		boolean started = false;
		try {
			if (scheduler == null) {
				scheduler = LightPathScheduler.getInstance(globalOffset);
				scheduler.setLpcp(this);
				started = true;
			}
			else {
				started = true;
			}
		}
		catch (Exception e) {
			log.error("Exception starting scheduler service: ", e);

		}
		return started;
	}

	private void exportLpcp() throws Exception {
		if (lpcpInterfaceImpl == null) {
			lpcpInterfaceImpl = new LpcpInterfaceImpl(this);
		}

		String bindName = RmiServerInfo.LPCP_RMI_BINDING_NAME;
		log.debug("Binding LpcpRemote to: " + bindName + ", "
		    + lpcpInterfaceImpl.getRemote());
		Naming.rebind(bindName, lpcpInterfaceImpl.getRemote());
		LpcpInterface lpcpInterface = (LpcpInterface) Naming.lookup(bindName);
		log.debug("Lookup got: " + lpcpInterface);
		log.debug("Is Lpcp alive? "
		    + lpcpInterface.isAlive(InternalLoginHelper.INSTANCE
		        .getToken(InternalLoginTokenType.LPCP)));
	}

	private BigInteger convertBitsetToBigInteger(BitSet bitset) {
		BigInteger bigInt = BigInteger.ZERO;
		if (bitset != null) {
			for (int i = 1; i < bitset.length(); i++) {
				i = bitset.nextSetBit(i);
				bigInt = bigInt.setBit(i);
			}
		}
		return bigInt;
	}

	private void discoverNetwork() throws Exception {
		List<NetworkElementHolder> nes = null;

		DiscoverThread dt = null;
		// Discover network First, retrieve NEs
		while (nes == null) {
			try {
				nes = NeProxyRmiMediator.INSTANCE.getNEs(null);

				/**
				 * WP: This code is strange/wrong. I think they wanted to loop until all
				 * NEs are aligned (but not loop for ever incase something never aligns)
				 * but instead we fetch the NE list (once) and if all are aligned set
				 * allDiscoveded and break out of the loop and continue on right away.
				 * Otherwise we sleep once but since nes!=null we fall of the loop and
				 * continue on. Very strange way to write this at best.
				 */
				boolean allDiscovered = true;

				if (nes != null) {
					/*
					 * Give the NEs up to 20 seconds to all get discovered. This prevents
					 * the client from trying to obtain topology from the LPCP_PORT too
					 * soon...
					 */

					for (NetworkElementHolder ne : nes) {
						log.debug("Rediscovering NEs... the status of: " + ne.getTid()
						    + " " + ne.getIp() + ":" + ne.getPort() + " is: "
						    + ne.getNeStatus());
						NeStatus status = ne.getNeStatus();
						if (status != null && !NeStatus.NE_NOT_CONNECT.equals(status)
						    && !NeStatus.NE_ALIGNED.equals(status)) {
							log.debug("Found ne in state: " + status + " (" + ne.getTid()
							    + ")");
							allDiscovered = false;
						}
					}
					if (allDiscovered) {
						break;
					}
				}
				Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
			}
			catch (Exception e) {
				log.error("Failed to get ne list.  Retrying...", e);
			}
		}

		/*
		 * We have now slept and spun for a bit, clear the model, register for
		 * events and re-fetch the NE list (which could have changed) before
		 * proceeding. Hopefully we'll get events to advise us of further changes
		 */
		// Initialise the hierarchical model with an empty string to zero it
		HierarchicalModel.INSTANCE.parseAndAddToModel(new ArrayList<Facility>());

		// Allow message broadcast to receive all NE-related events
		NeProxyRmiMediator.INSTANCE.listenForEvents(this);
		nes = NeProxyRmiMediator.INSTANCE.getNEs(null);

		for (NetworkElementHolder ne : nes) {
			if (ne == null) {
				log.error("Null NE found in NE list, strange man! " + nes);
				continue;
			}
			NeStatus status = ne.getNeStatus();



			if (NeStatus.NE_ALIGNED.equals(status)) {
				log.debug("Found aligned NE... starting discovery thread for: "
				    + ne.getTid() + "....");
				dt = new DiscoverThread(ne.getId(), ne.getTid(), ne.getIp(),
				    ne.getPort(), TopologyManager.INSTANCE, HierarchicalModel.INSTANCE);
				discoveryMgr.addToDiscoveryQ(dt);
			}
		}

		serverState = ServerState.RESTART_COMPLETE;
		isRestarting = false;
	}

	private int getChannelsForRate(String rate) {

		int channels;

		if (Utility.STS1_STR_RATE.equalsIgnoreCase(rate)
		    || Utility.VC3_STR_RATE.equalsIgnoreCase(rate)) {
			channels = 1;
		}
		else if (Utility.STS3C_STR_RATE.equalsIgnoreCase(rate)
		    || Utility.VC4_STR_RATE.equalsIgnoreCase(rate)) {
			channels = 3;
		}
		// See "NOTE on 300Mbs" in Utility.java
		// else if (Utility.STS6C_STR_RATE.equalsIgnoreCase(rate) ||
		// Utility.VC4_4C_STR_RATE.equalsIgnoreCase(rate))
		// {
		// channels = 6;
		// }
		else if (Utility.STS12C_STR_RATE.equalsIgnoreCase(rate)
		    || Utility.VC4_8C_STR_RATE.equalsIgnoreCase(rate)) {
			channels = 12;
		}
		else if (Utility.STS24C_STR_RATE.equalsIgnoreCase(rate)) {
			channels = 24;
		}
		else if (Utility.STS48C_STR_RATE.equalsIgnoreCase(rate)
		    || Utility.VC4_16C_STR_RATE.equalsIgnoreCase(rate)) {
			channels = 48;
		}
		else if (Utility.STS192C_STR_RATE.equalsIgnoreCase(rate)
		    || Utility.VC4_64C_STR_RATE.equalsIgnoreCase(rate)) {
			channels = 192;
		}
		else {
			log.error("Unable to determine number of channles for rate " + rate
			    + " returning -1");
			return -1;
		}

		return channels;
	}

	private BigInteger getConnectionsBigInt(String neid, String aid)
	    throws Exception {
		BigInteger result = BigInteger.ZERO;
		List<CrossConnection> conList = NeProxyRmiMediator.INSTANCE
		    .getCrossConnections(neid);

		log.debug("getConnectionsBigInt: getConnections() for: " + neid
		    + " returned: " + conList.size() + " connections");
		for (CrossConnection c : conList) {
			String conID = c.getId();
			if (conID == null) {
				log.error("getConnectionsBigInt: Skipped connection with null ID: " + c);
				continue;
			}

			if (c.isDracConnection()) {
				log.debug("getConnectionsBigInt: Skipped OpenDRAC connection: " + conID
				    + " " + c);
				continue;
			}

			String rate = c.getRate();
			String srcNeid = c.getSourceNeId();
			String dstNeid = c.getTargetNeId();
			// String srcFacAID = Utility.convertWAN2ETH(c.getSourcePortAid());
			String srcFacAID = c.getSourcePortAid();
			// String dstFacAID = Utility.convertWAN2ETH(c.getTargetPortAid());
			String dstFacAID = c.getTargetPortAid();
			int srcChannel = Integer.parseInt(c.getSourceChannel());
			int dstChannel = Integer.parseInt(c.getTargetChannel());

			/*
			 * HACKS >>>>> src and dst fac AIDs of WAN will all be converted to ETH
			 * for the purposes of bandwidth tracker population since it is the ETH
			 * port that keeps track of the BW used. OC AIDs should be untouched.
			 */
			// TEMP... until AIDs are normalised
			NeType neType = TopologyManager.INSTANCE.getNeType(srcNeid);
			srcFacAID = Utility.getNormalisedAID(srcFacAID, neType);
			dstFacAID = Utility.getNormalisedAID(dstFacAID, neType);

			log.debug("getConnectionsBigInt: ### Examining conID: " + conID
			    + "\n neid: " + neid + " aid: " + aid + "\n srcNeid: " + srcNeid
			    + " srcFacAID: " + srcFacAID + " dstNeid: " + dstNeid
			    + " dstFacAID: " + dstFacAID + " rate: " + rate + " srcChannel: "
			    + srcChannel + " dstChannel: " + dstChannel);

			int channels = getChannelsForRate(rate);


			if (neid.equalsIgnoreCase(srcNeid) && aid.equalsIgnoreCase(srcFacAID)) {

				for (int i = srcChannel; i < srcChannel + channels; i++) {
					result = result.setBit(i);
				}
			}
			else if (neid.equalsIgnoreCase(dstNeid)
			    && aid.equalsIgnoreCase(dstFacAID)) {

				for (int i = dstChannel; i < dstChannel + channels; i++) {
					result = result.setBit(i);
				}
			}
		}
		return result;
	}

	private void init() throws Exception {
		isRestarting = true;

		// Create the Discovery Manager
		discoveryMgr = new DiscoveryManager();
		if (initRouting() && initNeProxy()) {
			if (startSchedulerService()) {
				Thread discoveryManagerThread = new Thread(discoveryMgr,
				    "DiscoveryManager");
				discoveryManagerThread.setDaemon(true);
				discoveryManagerThread.start();
				exportLpcp();


				/*
				 * GG ADJ In the test integration environment, with recent RMI overhaul,
				 * NE discovery completes before lpcp is prepared to receive events
				 * (which drive topo consolidation). So, on lpcp init, make a one time
				 * call to consolidate:
				 */
				TopologyManager.INSTANCE.requestConsolidation();
			}
		}
		else {
			log.error("Routing initialisation failed");
		}
		isRestarting = false;
	}

	private void initModels(boolean fullInit) {
		TopologyManager.INSTANCE.setLpcp(this);
		if (fullInit) {
			globalOffset = retrieveScheduleOffset();

		}
	}

	private boolean initNeProxy() {
		if (NeProxyRmiMediator.INSTANCE.isAlive()) {
			try {
				discoverNetwork();
				return true;
			}
			catch (Exception e) {
				log.error("Error: ", e);
				return false;
			}
		}
		return true;
	}

	private List<ServiceXml> queryInterval(String startTime, String endTime) {
		List<ServiceXml> schedules = null;

		if (startTime != null && endTime != null) {
			try {
				schedules = dbutility.getLiveServicesWithinTimeInterval(
				    Long.parseLong(startTime), Long.parseLong(endTime));
				if (schedules != null) {
					log.debug("Schedules: " + schedules);
					for (int i = 0; i < schedules.size(); i++) {
						log.debug("schedule(" + i + "): " + schedules.get(i));
					}
				}
			}
			catch (Exception e) {
				log.debug("Exception: ", e);
			}
		}
		else {
			log.error("Exception: Start or end time is invalid.  startTime: "
			    + startTime + " endTime: " + endTime);
		}

		return schedules;
	}

	private String retrieveScheduleOffset() {
		return Integer.toString(DbUtilityCommonUtility.INSTANCE
		    .queryScheduleOffset());
	}

	public ScheduleResult extendServiceTime(DracService service,
	    Integer minutesToExtendService) throws Exception {
		long newEndTime = service.getEndTime() + minutesToExtendService.intValue()
		    * 60 * 1000;
		service.setEndTime(newEndTime);
		ServiceXml serviceXml = retrieveService(service.getId());
		serviceXml.setEndTime(newEndTime);
		String xmlString = serviceXml.getXMLUserData();

		Map<String, String> idMap = new HashMap<String, String>();
		idMap.put(DbKeys.LightPathCols.LP_SERVICEID, service.getId());

		Map<String, String> dataToUpdate = new HashMap<String, String>();
		dataToUpdate.put(DbKeys.LightPathCols.LP_ENDTIME, "" + newEndTime);
		dataToUpdate.put(DbKeys.LightPathCols.LP_XML, xmlString);

		DbLightPath dbLightPath = DbLightPath.INSTANCE;

		dbLightPath.update(idMap, dataToUpdate);

		scheduler.notifyThreads();
		return new ScheduleResult(getLpcpUid() + "-" + System.currentTimeMillis(),
		    xmlString);
	}

	public void stop() { // NO_UCD
	}
}