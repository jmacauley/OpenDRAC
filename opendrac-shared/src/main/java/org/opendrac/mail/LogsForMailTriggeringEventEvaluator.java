package org.opendrac.mail;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

public class LogsForMailTriggeringEventEvaluator implements
		TriggeringEventEvaluator {
	
	private Filter filter = new LogsForMailFilter();

	@Override
	public boolean isTriggeringEvent(LoggingEvent event) {
		return filter.decide(event) == Filter.ACCEPT;
	}
}
