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

package com.nortel.appcore.app.drac.server.neproxy.command.cpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nortel.appcore.app.drac.common.db.DbKeys;
import com.nortel.appcore.app.drac.common.types.AbstractNetworkElement;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.types.ServiceXml;
import com.nortel.appcore.app.drac.common.utility.ClientMessageXml;
import com.nortel.appcore.app.drac.common.utility.event.Tl1XmlDbChangeEvent;
import com.nortel.appcore.app.drac.database.dracdb.DbLightPath;
import com.nortel.appcore.app.drac.server.neproxy.DiscoverNePool;
import com.nortel.appcore.app.drac.server.neproxy.NetworkElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.staticinfo.ObjectElement;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.TL1AlarmEvent;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1.Tl1CommandCode;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.AbstractCommandlet;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NeProxy;
import com.nortel.appcore.app.drac.server.neproxy.mediation.tl1server.NePoxyDefinitionsParser;

public final class ReptAlarm extends AbstractCommandlet {
	public static final class QueryDocTask extends Thread {
		private final NetworkElement ne;
		private boolean shouldTerminate;
		// private final String aid;
		private final Map<String, String> paramList = new HashMap<String, String>();

		public QueryDocTask(NetworkElement networkElement, String aid) {
			ne = networkElement;
			setName("ReptAlarm.QueryDocTask thread for NE " + ne.getNeName());

			// this.aid = aid;
			paramList.put(ObjectElement.OPERATION_KEY,
			    Tl1CommandCode.RTRV_DOC.toString());
			String[] temp = aid.split("-");
			paramList.put("AID", "SHELF-" + temp[1] + "-ALL");
		}

		@Override
		public void run() {
			try {
				log.debug("CPL ReptAlarm: Querying DOC status for " + ne.getNeName()
				    + " in 1 second");
				Thread.sleep(1000);
				String docCmdStatus = "NOTREADY";
				while (!shouldTerminate && !docCmdStatus.equalsIgnoreCase("READY")) {
					List<Map<String, String>> result = ne.getTl1Session().sendToNE(
					    paramList);
					if (result != null && result.size() > 0) {
						Map<String, String> aResult = result.get(0);
						if (aResult != null) {
							Map<String, String> eventData = new HashMap<String, String>();
							String docStatus = aResult.get("DOCPROGRESSSTATUS");
							String docPercentage = aResult.get("DOCPERCENT");
							docCmdStatus = aResult.get("DOCCMDSTAT");
							eventData.put("docProgressStatus", docStatus);
							eventData.put("docPercent", docPercentage);
							log.debug("CPL ReptAlarm: got DOC status " + aResult.toString()
							    + " docCmdStatus is equal to " + docCmdStatus);
							NeProxy.generateEvent(buildEvent(eventData),
							    ClientMessageXml.DBCHG_EVENT_VALUE);
						}
					}
					Thread.sleep(20 * 1000);
				}
				HashMap<String, String> eventData = new HashMap<String, String>();
				eventData.put("docProgressStatus", "Ready");
				eventData.put("docPercent", "100");

				NeProxy.generateEvent(buildEvent(eventData),
				    ClientMessageXml.DBCHG_EVENT_VALUE);

			}
			catch (Exception e) {
				log.error("CPL ReptAlarm: Failed to query DOC status", e);
			}
		}

		public void terminate() {
			shouldTerminate = true;
		}

		private Tl1XmlDbChangeEvent buildEvent(Map<String, String> data) {
			Tl1XmlDbChangeEvent dbchgEvent = new Tl1XmlDbChangeEvent(ne);
			dbchgEvent.updateNeInfo(ne.getNeStatus().getStateString());
			String timeDate = new java.sql.Timestamp(System.currentTimeMillis())
			    .toString();
			dbchgEvent.setOccurrentDate(timeDate.substring(2, 10));
			dbchgEvent.setOccurrentTime(timeDate.substring(11, 19));
			dbchgEvent.setEventId(ne.getNeId() + "_" + "1111");
			dbchgEvent.setReportType("docStatus");
			dbchgEvent.addDataElement(data);
			return dbchgEvent;
		}
	}

	private static final String DELETECONDTYPE = "CHDEL_IP";
	private static final String ADDCONDTYPE = "CHADD_IP";

	private static final int CHANNEL_STATUS_NUM_RETRIES = Integer.parseInt(System
	    .getProperty("Channel_Status_Num_Retries", "60"));

	// private Timer queryDocTimer;
	// private QueryDocTask queryTask;
	public ReptAlarm(Map<String, Object> param) {
		super(param);
	}

	@Override
	public boolean start() {
		NetworkElement ne = (NetworkElement) getParameters().get(
		    NePoxyDefinitionsParser.NETWORKELEMENT_KEY);
		ne.upDateLocalInfo();
		TL1AlarmEvent anEvent = (TL1AlarmEvent) getParameters().get(
		    AbstractNetworkElement.EVENTRECV_KEY);
		Map<String, String> values = anEvent.getPayloads().get(0);

		log.debug("CPL ReptAlarm: Process Alarm event from " + ne.getNeName() + ": "
		    + values);
		String conditionType = values.get("CONDTYPE");

		// The aid is the DOC ID: DOC-shelf-slot where slot is always 4!
		String docAid = values.get("AID");

		// Only interest in Add and Delete events

		if (!conditionType.equalsIgnoreCase(DELETECONDTYPE)
		    && !conditionType.equalsIgnoreCase(ADDCONDTYPE)) {
			
			return false;
		}

		/**
		 * Start or cancel the query to update the DOC status
		 */
		String alarmSeverity = values.get("NTFNCDE");
		String dgnType = values.get("DGNTYPE");
		if (!alarmSeverity.equalsIgnoreCase("CL")) {
			// Timer queryDocTimer = new Timer();
			QueryDocTask queryTask = new QueryDocTask(ne, docAid);
			queryTask.setDaemon(true);
			queryTask.start();
			// queryDocTimer.schedule(queryTask, 1000, 20000);
			ne.enQueueTimer(dgnType, queryTask);
			return false;
		}

		// Cancel the timer if it's running
		QueryDocTask aTimer = ne.deQueueTimer(dgnType);
		if (aTimer != null) {
			log.debug("CPL ReptAlarm: Cancel the Query process in " + ne.getNeName());
			aTimer.terminate();
		}

		// Check to see that there is any pending command that needs to execute.
		Map<String, DeleteConnectionTask> commandList = ne.getAllCommand();
		log.debug("CPL ReptAlarm: There are " + commandList.size()
		    + " commands in queue for " + ne.getNeName() + " list:" + commandList);
		Iterator<String> ir = commandList.keySet().iterator();
		while (ir.hasNext()) {
			String waveLength = ir.next();
			String channelAid = this.getNCAid(ne, docAid, waveLength);
			if (channelAid != null) {
				DeleteConnectionTask aTask = commandList.get(waveLength);
				log.debug("CPL ReptAlarm: Processing task " + aTask.toString()
				    + " trying up to 60 times, 30 second sleep");

				try {
					// GG 2009-02-03
					// As the channel in each domain transitions into 'Active' or
					// 'Inactive', checked upon
					// receipt of
					// a 'clear' on the CHDEL_IP or CHADD_IP DOC alarm, all branch node
					// NEs will now check
					// that
					// both the Tx and Rx domains are finished with their channel
					// activities BEFORE any OCH
					// connection adds/deletes are issued. It used to be that the branch
					// node would check only
					// the channel
					// status on each side of its WSS ... but that's not good enough. You
					// have to wait until
					// the Tx and Rx
					// domains (at each end of the path) have finished their channel
					// activities.
					NetworkElement TxDomainNE = null;
					String TxDomainSHELF = null;
					NetworkElement RxDomainNE = null;
					String RxDomainSHELF = null;

					try {
						ServiceXml aService = null;
						Map<String, Object> serviceFilter = new HashMap<String, Object>();
						serviceFilter
						    .put(DbKeys.LightPathCols.LP_CALLID, aTask.getCallId());
						List<ServiceXml> results = DbLightPath.INSTANCE.retrieve(
						    serviceFilter);
						if (results != null && results.size() == 1) {
							aService = results.get(0);
						}

						TxDomainNE = (NetworkElement) DiscoverNePool.INSTANCE
						    .getNeByTidOrIdOrIpandPort(aService.getAend());
						RxDomainNE = (NetworkElement) DiscoverNePool.INSTANCE
						    .getNeByTidOrIdOrIpandPort(aService.getZend());

						// Is this the best way to find the far end connections? The
						// ServiceXml
						// getConnectionInfo(idx)
						// cannot assumed to be ordered.
						boolean foundAEnd = false;
						boolean foundZEnd = false;
						for (CrossConnection edge : aService.getCrossConnections()) {
							if (aService.getAend().equals(edge.getSourceNeId())
							    || aService.getAend().equals(edge.getTargetNeId())) {
								String sourceAid = edge.getSourcePortAid();
								String targetAid = edge.getTargetPortAid();

								if (sourceAid.startsWith("LIM")) {
									TxDomainSHELF = sourceAid.split("-")[1];
									foundAEnd = true;
								}
								else if (targetAid.startsWith("LIM")) {
									TxDomainSHELF = targetAid.split("-")[1];
									foundAEnd = true;
								}
							}

							if (aService.getZend().equals(edge.getSourceNeId())
							    || aService.getZend().equals(edge.getTargetNeId())) {
								String sourceAid = edge.getSourcePortAid();
								String targetAid = edge.getTargetPortAid();

								if (sourceAid.startsWith("LIM")) {
									RxDomainSHELF = sourceAid.split("-")[1];
									foundZEnd = true;
								}
								else if (targetAid.startsWith("LIM")) {
									RxDomainSHELF = targetAid.split("-")[1];
									foundZEnd = true;
								}
							}

							if (foundAEnd && foundZEnd) {
								break;
							}
						}

						if (foundAEnd && foundZEnd) {
							StringBuilder sb = new StringBuilder();
							sb.append(" For WSS node: " + ne);
							sb.append(" callId: " + aTask.getCallId());
							sb.append(" serviceId: " + aService.getServiceId());
							sb.append(" TxDomainNE: " + aService.getAend());
							sb.append(" TxDomainSHELF: " + TxDomainSHELF);
							sb.append(" RxDomainNE: " + aService.getZend());
							sb.append(" RxDomainSHELF: " + RxDomainSHELF);
							sb.append(" wavelength: " + waveLength);
							log.debug("Derived parameters for checking channel status: "
							    + sb.toString());
						}
					}

					// In the event of failure in obtaining the end info, default to old
					// behaviour?
					catch (Exception ex) {
						// Default below
						;
						;
					}

					if (TxDomainNE == null || RxDomainNE == null || TxDomainSHELF == null
					    || RxDomainSHELF == null) {
						log.debug("Could not correctly derive Tx/Rx Domain parameters for checking channel status; using defaults.");
						TxDomainNE = ne;
						RxDomainNE = ne;
						TxDomainSHELF = aTask.getFromShelf();
						RxDomainSHELF = aTask.getToShelf();
					}

					for (int i = 0; i < CHANNEL_STATUS_NUM_RETRIES; i++) {
						Thread.sleep(30 * 1000);
						log.debug("CPL ReptAlarm: Checking channel status " + waveLength
						    + " for iteration: " + i + " " + aTask);
						if (!isChannelActive(TxDomainNE, TxDomainSHELF, waveLength)
						    && !isChannelActive(RxDomainNE, RxDomainSHELF, waveLength)) {
							log.debug("CPL ReptAlarm: Removing the task for wavelength (DELAYED)"
							    + waveLength + " in " + ne.getNeName() + " " + aTask);

							// One final wait period of 1 min. Tests with the recent
							// three-domain service
							// teardown were
							// generally successful, but there were still occasional DOC
							// lockups. David
							// Bownass had
							// a look at the CPL logs. It appears that the DOCCARE alarm is
							// cleared prior to
							// the
							// actual channel conditions being ready for branch connection
							// deletions.
							Thread.sleep(60 * 1000);

							if (aTask.sendCommand()) {
								ne.deQueueCommand(waveLength);
								log.debug("CPL ReptAlarm: Finished " + waveLength + " in "
								    + ne.getNeName());
							}
							else {
								log.debug("CPL ReptAlarm: send failed, skiping this wavelength "
								    + waveLength + " in " + ne.getNeName() + " " + aTask);
							}
							// Not sure why we return false, but thats what the end returns as
							// well...
							return false;
						}
						log.debug("CPL ReptAlarm: channel(s) still active, spinning 30 seconds for "
						    + waveLength + " in " + ne.getNeName() + " " + aTask);
					}
					log.error("CPL ReptAlarm: Failed to delete, timed out.  Tried 60 times for  "
					    + waveLength + " in " + ne.getNeName() + " " + aTask);
				}
				catch (Exception te) {
					log.error(
					    "CPL ReptAlarm: Unexpected error processing task "
					        + aTask.toString(), te);
				}
			}
		}

		// Not generating alarm yet
		return false;
	}

	private String getNCAid(NetworkElement ne, String docAid, String waveLength) {
		Map<String, String> paramList = new HashMap<String, String>();
		paramList.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_DOC_CH.toString());
		String[] temp = docAid.split("-");
		paramList.put("AID", "SHELF-" + temp[1] + "-ALL");
		try {
			List<Map<String, String>> result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					String ncAid = aResult.get("NCAID");
					if (ncAid.indexOf(waveLength) > 0) {
						return ncAid;
					}
				}
			}
		}
		catch (Exception e) {
			log.error("CPL ReptAlarm: Failed to query DOC channel", e);
		}
		return null;
	}

	// private boolean isChannelActive(NetworkElement ne, String ncAid) {
	private boolean isChannelActive(NetworkElement ne, String shelf,
	    String waveLength) {
		List<Map<String, String>> result;
		Map<String, String> paramList = new HashMap<String, String>();
		// paramList.put(ObjectElement.operationKey, "RTRV-DOC-CH");
		// paramList.put("AID", ncAid);
		// String[] temp = docAid.split("-");
		log.debug("CPL ReptAlarm: isChannelActive for " + ne.getNeName() + " "
		    + shelf + " " + waveLength);
		paramList.put(ObjectElement.OPERATION_KEY,
		    Tl1CommandCode.RTRV_TOPO_SWT.toString());
		paramList.put("AID", "SWT-" + shelf + "-" + waveLength);
		// paramList.put("AID", "ALL");
		try {
			result = ne.getTl1Session().sendToNE(paramList);
			if (result != null) {
				Iterator<Map<String, String>> resultIr = result.iterator();
				while (resultIr.hasNext()) {
					Map<String, String> aResult = resultIr.next();
					String[] swAid = aResult.get("SWTAID").split("-");
					if (waveLength.equals(swAid[2])) {
						String docCareFlag = aResult.get("DOCCARE");
						log.debug("CPL ReptAlarm: isChannelActive found waveLength "
						    + swAid[2] + " with " + docCareFlag);
						if (docCareFlag.equalsIgnoreCase("TRUE")) {
							log.debug("CPL ReptAlarm: isChannelActive for " + ne.getNeName()
							    + " " + shelf + " " + waveLength + " returning true");
							return true;
						}
					}
				}
				log.debug("CPL ReptAlarm: isChannelActive for " + ne.getNeName() + " "
				    + shelf + " " + waveLength + " returning false");
				return false;
			}
		}
		catch (Exception e) {
			log.error("CPL ReptAlarm: isChannelActive Failed to query DOC channel", e);
		}
		/*
		 * try { result = ne.tl1Session.sendToNE(paramList); if (result != null) {
		 * SimpleHashtable aResult = (SimpleHashtable) result.get(0); String eec =
		 * (String) aResult.get("EEC"); String activeFlag = (String)
		 * aResult.get("INGRESSACTIVEFLAG"); String channelCond = (String)
		 * aResult.get("COS"); if (activeFlag.equalsIgnoreCase("False") &&
		 * channelCond.equalsIgnoreCase("Inactive") &&
		 * eec.equalsIgnoreCase("INACTIVE")) return false; } } catch (Exception e) {
		 * log.error("Failed to query DOC channel", e); return true; }
		 */
		log.error("CPL ReptAlarm: isChannelActive returning true after failure getting channel status for "
		    + ne.getNeName() + " " + shelf + " " + waveLength);
		return true;
	}
}
