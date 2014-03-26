package org.opendrac.mail;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class LogsForMailFilter extends Filter {

	private final Set<String> acceptedSubStrings = new HashSet<String>();
	private final boolean isActive = Boolean.getBoolean("org.opendrac.logging.smtp.mailing.active");

	public LogsForMailFilter() {
		super();
		initSubStrings();
	}

	@Override
	public int decide(LoggingEvent event) {
		int decision = Filter.NEUTRAL;
		if (isActive) {
			String loggedMessage = event.getRenderedMessage();
			for (String subString : acceptedSubStrings) {				
				if (loggedMessage.indexOf(subString) >= 0) {
					decision = Filter.ACCEPT;
					break;
				}
			}			
		}
		return decision;
	}

	private void initSubStrings() {
		acceptedSubStrings.add("Login failed This user does not have policy");
		acceptedSubStrings.add("Shutdown hook invoked");
		acceptedSubStrings.add("Trying to activate OpenDRAC located at:");
		acceptedSubStrings.add("Performing switch to");
		acceptedSubStrings.add("Error looking up SRLG for");
		acceptedSubStrings
				.add("Possible mismatch between NE and DRAC's database. The entry in the database was deleted");
   		acceptedSubStrings.add("Lockout is under effect");
   		acceptedSubStrings.add("Locked client ip address");
		acceptedSubStrings.add("Locked client http session");
		acceptedSubStrings.add("populateTrackers: >>>>>FATAL ERROR, srcTracker:");
		
	}

}
