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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Uninterruptibles;
import com.nortel.appcore.app.drac.common.types.CrossConnection;
import com.nortel.appcore.app.drac.common.utility.ClientSocketException;
import com.nortel.appcore.app.drac.common.utility.ProvisioningResultHolder;
import com.nortel.appcore.app.drac.common.utility.XmlUtility;
import com.nortel.appcore.app.drac.server.lpcp.rmi.NeProxyRmiMediator;
import com.nortel.appcore.app.drac.server.lpcp.scheduler.SchedulingThread.Command;

/**
 * Created on Apr 12, 2006
 * 
 * @author nguyentd
 */
public final class NeConnectionTask implements Callable<Integer> {
  private final Logger log = LoggerFactory.getLogger(getClass());
	private static final int MAX_RETRIES = 30;
	private static final String NE_ERR_IUID = "data, Unknown NE ID";
	private static final String NE_ERR_INRY = "Status, NE not ready";
	public static final String ERROR_CODE_IEAE = "IEAE";

	private SchedulingThread.Command command;
	private String errorText;
	private String errorCode;
	private int retryCounter;

	private CrossConnection xcon = null;
	private List<CrossConnection> xconList = null;
	private Element outputData;

	// Called for xc impl stage
	public NeConnectionTask(CrossConnection xcon,
	    SchedulingThread.Command commandStr) {
		this.command = commandStr;
		this.xcon = xcon;
	}

	// Called for prep and post stages
	public NeConnectionTask(List<CrossConnection> xconList,
	    SchedulingThread.Command commandStr) {
		this.command = commandStr;
		this.xconList = xconList;
	}

	@Override
	public Integer call() {
		int rc = 0;
		boolean shouldRetry = true;

		log.debug("NeConnectionTaskV2, command: " + command + " CrossConnection: "
		    + xcon);

		ProvisioningResultHolder resp = null;
		while (shouldRetry && retryCounter < MAX_RETRIES) {
			try {
				if (command == Command.PREP_ADDXCON_CMD) {
					resp = NeProxyRmiMediator.INSTANCE.prepCreateConnections(
					    xconList);

					if (resp.getExceptionText() != null) {
						log.error("Failed to create CrossConnection: " + xcon + " resp:"
						    + resp);
						throw new ClientSocketException(resp.getExceptionText());
					}
				}
				else if (command == Command.ADDXCON_CMD) {
					resp = NeProxyRmiMediator.INSTANCE.createConnection(xcon);

					if (resp.getExceptionText() != null) {
						log.error("Failed to create CrossConnection: " + xcon + " resp:"
						    + resp);
						throw new ClientSocketException(resp.getExceptionText());
					}
				}

				else if (command == Command.DELXCON_CMD) {
					resp = NeProxyRmiMediator.INSTANCE.deleteConnection(xcon);

					if (resp.getExceptionText() != null) {
						log.error("Failed to delete CrossConnection: " + xcon + " resp:"
						    + resp);
						throw new ClientSocketException(resp.getExceptionText());
					}
				}
				else if (command == Command.POST_DELXCON_CMD) {
					resp = NeProxyRmiMediator.INSTANCE.postDeleteConnections(
					    xconList);

					if (resp.getExceptionText() != null) {
						log.error("Failed to delete CrossConnection: " + xcon + " resp:"
						    + resp);
						throw new ClientSocketException(resp.getExceptionText());
					}
				}
				else {
					log.error("Unexpected command!!! " + command);
				}

				shouldRetry = false;
			}

			catch (ClientSocketException e) {
				errorText = e.toString();
				errorCode = resp.getErrorCode();
				log.error("Error code is: " + errorCode, e);
				if (!ERROR_CODE_IEAE.equalsIgnoreCase(errorCode)) {
					if (NE_ERR_IUID.equalsIgnoreCase(e.getMessage())
					    || NE_ERR_INRY.equalsIgnoreCase(e.getMessage())) {
						shouldRetry = true;
						sleep(1000);
						retryCounter++;
					}
					else {
						shouldRetry = false;
						log.error("Exception from NE: " + errorText, e);
						rc = -1;
					}
				}
				else {
					// The connection already exists!
					shouldRetry = false;
					log.error(
					    "Exception from NE, errorCode indicates connection already exists: "
					        + errorCode, e);
				}
			}
			catch (Exception e) {
				rc = -1;
				log.error("Unexpected exception, won't retry! " + command + " " + xcon,
				    e);
				shouldRetry = false;
			}

			/*
			 * Try to capture response data even in the presence of errors...in case
			 * data can be obtained for cleanup
			 */
			if (resp != null && resp.getOutputData() != null) {
				try {
					outputData = XmlUtility.createDocumentRoot(resp.getOutputData());
				}
				catch (Exception e) {
					log.error("Unable to convert response data to XML element from "
					    + resp);
				}
			}
		}

		return Integer.valueOf(rc);
	}

	public String getErrorCodeString() {
		return errorCode;
	}

	public Element getOutputData() {
		return outputData;
	}

	public void setCommand(SchedulingThread.Command command) {
		this.command = command;
	}

	private void sleep(int milliseconds) {
	  Uninterruptibles.sleepUninterruptibly(milliseconds, TimeUnit.MICROSECONDS);
	}

}
