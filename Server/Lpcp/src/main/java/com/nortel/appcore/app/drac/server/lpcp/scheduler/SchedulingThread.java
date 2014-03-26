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

package com.nortel.appcore.app.drac.server.lpcp.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.types.State.SERVICE;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.database.helper.DbUtilityLpcpScheduler;
import com.nortel.appcore.app.drac.server.lpcp.trackers.LpcpFacility;

/**
 * SchedulingThread Superclass for StartScheduleThread and DeleteScheduleThread
 * 
 * @since 2005-11-08
 * @author adlee
 */
public class SchedulingThread extends Thread {
  private final Logger log = LoggerFactory.getLogger(getClass());
	// one of 4 commands supported.
	public enum Command {
		ADDXCON_CMD("createXConnection"), // "createXConnection";
		DELXCON_CMD("deleteXConnection"), // "deleteXConnection";
		PREP_ADDXCON_CMD("PREPcreateXConnection"), //
		POST_DELXCON_CMD("POSTdeleteXConnection");//

		private final String asString;

		Command(String s) {
			asString = s;
		}

		@Override
		public String toString() {
			return asString;
		}
	}

	protected LightPathScheduler hScheduler;

	public SchedulingThread(String threadName, LightPathScheduler lpScheduler) {
		super(threadName);
		hScheduler = lpScheduler;
	}

	protected boolean sendCommandToNe(Command command, ServiceXml aService,
	    SERVICE successState, SERVICE failureState) {
		String callId = aService.getCallId();
		boolean returnStatus = true;

		try {
			DbUtilityLpcpScheduler.INSTANCE.updateServiceStatusByCallId(callId,
			    successState);
		}
		catch (Exception e) {
			log.error(
			    "SchedulingThread::sendCommandToNe failed to update service status",
			    e);
			returnStatus = false;
		}

		
		returnStatus = sendCommand(command, aService);

		if (!returnStatus) {
			

			try {
				DbUtilityLpcpScheduler.INSTANCE.updateServiceStatusByCallId(
				    callId, failureState);
			}
			catch (Exception e2) {
				log.error("Failed to update service status: " + callId, e2);
			}
		}
		else {
			log.debug("SchedulingThread: Successfully sent service request: "
			    + callId);
		}

		hScheduler.getDracServerReference().updateScheduleAndLogServiceChange(
		    aService, command.toString(),
		    returnStatus ? successState : failureState);
		return returnStatus;
	}

	private boolean sendCommand(Command command, ServiceXml aService) {
		boolean returnStatus = true;
		String numberOfPathConnections = Integer.toString(aService
		    .numberOfConnectionsInPath());

		Map<String, List<CrossConnection>> neXcListMap = aService.getNeXcListMap();

		// Housekeeping stage
		// ===========================
		for (Map.Entry<String, List<CrossConnection>> m : neXcListMap.entrySet()) {
			List<CrossConnection> neXcons = m.getValue();
			for (CrossConnection xcon : neXcons) {
				xcon.setNumberOfConnectionsInPath(numberOfPathConnections);
				setL2SSFlags(xcon);
			}
		}

		// Prep stage
		// ===========================
		if (returnStatus) {
			List<FutureTask<Integer>> connectionTasks = new ArrayList<FutureTask<Integer>>();
			Map<FutureTask<Integer>, NeConnectionTask> mapFutureTaskToCommandTask = new HashMap<FutureTask<Integer>, NeConnectionTask>();
			Map<FutureTask<Integer>, List<CrossConnection>> mapFutureTaskToConnectionsList = new HashMap<FutureTask<Integer>, List<CrossConnection>>();

			if (command == Command.ADDXCON_CMD) {
				for (Map.Entry<String, List<CrossConnection>> m : neXcListMap
				    .entrySet()) {
					List<CrossConnection> neXcons = m.getValue();

					NeConnectionTask connectionTask = new NeConnectionTask(neXcons,
					    Command.PREP_ADDXCON_CMD);
					FutureTask<Integer> aTask = new FutureTask<Integer>(connectionTask);
					connectionTasks.add(aTask);
					mapFutureTaskToCommandTask.put(aTask, connectionTask);
					mapFutureTaskToConnectionsList.put(aTask, neXcons);
					LightPathScheduler.executorPool.submit(aTask);
				}

				try {
					for (FutureTask<Integer> futureTask : connectionTasks) {
						// Wait (block) until all tasks complete
						if (futureTask.get().intValue() < 0) {
							returnStatus = false;
							break;
						}
					}

					// Collect and store mediation data generated during prep stage
					// (one record element per NE)
					Element serviceMediationData = null;
					Iterator<FutureTask<Integer>> it = mapFutureTaskToCommandTask
					    .keySet().iterator();
					while (it.hasNext()) {
						FutureTask<Integer> futureTask = it.next();
						NeConnectionTask connectionTask = mapFutureTaskToCommandTask
						    .get(futureTask);
						Element neMediationDataRecord = connectionTask.getOutputData();

						if (neMediationDataRecord != null) {
							// We got mediation data from the prep stage ... used for tracking
							// L2 entities and aid translations ...

							if (serviceMediationData == null) {
								serviceMediationData = new Element(ServiceXml.MEDIATION_DATA_ID);
							}

							// apply it to the serviceMediationData (which will get applied
							// to the service...for recall later)
							serviceMediationData.addContent(neMediationDataRecord.detach());

							// apply it to the xcons of this NE (to invoke necessary aid
							// translations)
							List<CrossConnection> neXcons = mapFutureTaskToConnectionsList
							    .get(futureTask);
							for (CrossConnection xcon : neXcons) {
								xcon.setMediationData(XmlUtility
								    .rootNodeToString(neMediationDataRecord));
							}
						}
					}

					// Set the additional data on the service record
					if (serviceMediationData != null) {
						aService.setXMLUserData(XmlUtility
						    .rootNodeToString(serviceMediationData));
					}

					// Persist the mediation data
					if (serviceMediationData != null) {
						Map<String, String> data = new HashMap<String, String>();
						data.put(DbKeys.LightPathCols.LP_XML,
						    XmlUtility.rootNodeToString(serviceMediationData));
						Map<String, String> idMap = new HashMap<String, String>();
						idMap.put(DbKeys.LightPathCols.LP_SERVICEID,
						    aService.getServiceId());
						DbLightPath.INSTANCE.update(idMap, data);
					}
				}
				catch (Exception e) {
					log.error(
					    "SchedulingThread::sendCommand - exception during prep stage.", e);
					returnStatus = false;
				}

			}
		}

		// Add/Delete XC implementation stage
		// ===========================
		if (returnStatus) {
			List<FutureTask<Integer>> connectionTasks = new ArrayList<FutureTask<Integer>>();

			for (Map.Entry<String, List<CrossConnection>> m : neXcListMap.entrySet()) {
				List<CrossConnection> neXcons = m.getValue();
				for (CrossConnection xcon : neXcons) {
					NeConnectionTask connectionTask = new NeConnectionTask(xcon, command);
					FutureTask<Integer> aTask = new FutureTask<Integer>(connectionTask);
					connectionTasks.add(aTask);
					LightPathScheduler.executorPool.submit(aTask);
				}
			}

			try {
				for (int i = 0; i < connectionTasks.size(); i++) {
					Integer result = connectionTasks.get(i).get();
					if (result.intValue() < 0) {
						returnStatus = false;
						break;
					}
				}
			}
			catch (Exception e) {
				log.error(
				    "SchedulingThread::sendCommand - exception during xc impl stage.",
				    e);
				returnStatus = false;
			}
		}

		// Post stage
		// ===========================
		if (returnStatus) {
			List<FutureTask<Integer>> connectionTasks = new ArrayList<FutureTask<Integer>>();
			if (command == Command.DELXCON_CMD) {
				for (Map.Entry<String, List<CrossConnection>> m : neXcListMap
				    .entrySet()) {
					List<CrossConnection> neXcons = m.getValue();

					NeConnectionTask connectionTask = new NeConnectionTask(neXcons,
					    Command.POST_DELXCON_CMD);
					FutureTask<Integer> aTask = new FutureTask<Integer>(connectionTask);
					connectionTasks.add(aTask);
					LightPathScheduler.executorPool.submit(aTask);
				}

				try {
					for (int i = 0; i < connectionTasks.size(); i++) {
						Integer result = connectionTasks.get(i).get();
						if (result.intValue() < 0) {
							returnStatus = false;
							break;
						}
					}
				}
				catch (Exception e) {
					log.error(
					    "SchedulingThread::sendCommand - exception during post stage.", e);
					returnStatus = false;
				}

			}
		}

		if (!returnStatus) {
			if (command == Command.ADDXCON_CMD) {
				
				// ROLLBACK ...
				sendCommand(Command.DELXCON_CMD, aService);
			}
		}

		return returnStatus;
	}

	private void setL2SSFlags(CrossConnection xcon) {
		String neid = xcon.getSourceNeId();

		String fromPortAid = xcon.getSourcePortAid();
		LpcpFacility fromPort = hScheduler.getDracServerReference()
		    .getDRACHierarchicalModel().getFacility(neid, fromPortAid);
		if (fromPort.isL2SS()) {
			xcon.setFromSideL2SS();
		}

		String toPortAid = xcon.getTargetPortAid();
		LpcpFacility toPort = hScheduler.getDracServerReference()
		    .getDRACHierarchicalModel().getFacility(neid, toPortAid);
		if (toPort.isL2SS()) {
			xcon.setToSideL2SS();
		}
	}

}
