package org.opendrac.redundancy;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("controllerWatchDog")
final class ControllerWatchDog extends WatchDog { // NO_UCD

	@Autowired(required = true)
	@Qualifier("controllerHostPortMapping")
	private ArrayList<String> controllerHostPortMapping;

	// @Scheduled(fixedDelay = frequency)
	public void heartbeat() {
		if (isFailedOver) {
			ControllerWatchDog.context.destroy(); // this will also shutdown this
			                                      // watchdog
		}
		else {
			try {
				checkHosts(controllerHostPortMapping);
			}
			catch (ConnectException e) {
				handleError(e);
			}
			catch (IOException e) {
				handleError(e);
			}
		}
	}

	private void handleError(Exception cause) {
		errorCounter++;
		log.error("Error (" + errorCounter + "/" + retries
		    + ") with retry frequency of " + frequency / 1000 + "s:", cause);

		if (errorCounter >= retries) {
			try {
				failover();
			}
			catch (IOException e) {
				log.error("Error: ", e);
			}
		}
	}

	private void failover() throws IOException {
		log.warn("Trying to activate OpenDRAC located at: " + opendracRoot.trim());
		executeCommand(startScript, opendracRoot);
		// TODO: Really check if this is true
		isFailedOver = true;
	}

}