package org.opendrac.mail;

import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * SMTPAppender with filters in place.
 * 
 * @author andre
 * 
 */
public class FilteredSMTPAppender extends SMTPAppender {


	/**
	 * Perform SMTPAppender specific appending actions. If the filters decide
	 * the event is to be logged, it is appended to the cyclic buffer. If the
	 * TriggeringEventEvaluator triggers to the event the buffer is sent away.
	 * 
	 * @param event
	 *            LoggingEvent the event
	 */
	@Override
	public void append(LoggingEvent event) {

		if (!checkEntryConditions()) {
			return;
		}
		event.getThreadName();
		event.getNDC();
		event.getMDCCopy();
		if (getLocationInfo()) {
			event.getLocationInformation();
		}
		event.getRenderedMessage();
		event.getThrowableStrRep();

		// add event to buffer if it matches filter criteria
		if (matchesFilterCriteria(event)) {
			cb.add(event);
		}
		// mail buffered events after trigger
		if (evaluator.isTriggeringEvent(event)) {
			sendBuffer();
		}
	}

	/**
	 * Check if the event matches the criteria of the installed filters
	 * 
	 * @param event
	 *            LoggingEvent the event
	 * @return boolean true if event matches filter criteria.
	 */
	private boolean matchesFilterCriteria(LoggingEvent event) {
		boolean matches = false;
		Filter filter = getFilter();
		if (filter != null) {
			matches = (filter.decide(event) == Filter.ACCEPT);
		}
		return matches;
	}
}
