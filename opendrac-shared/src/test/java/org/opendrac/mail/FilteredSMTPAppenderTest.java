package org.opendrac.mail;
import static org.junit.Assert.*;

import org.junit.Test;

public class FilteredSMTPAppenderTest {

	
	@Test
	public void testHasFilter() {		
		FilteredSMTPAppender appender = new FilteredSMTPAppender();
		assertNull(appender.getFilter());
	}
}
