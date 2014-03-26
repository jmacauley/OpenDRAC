package org.opendrac.mail;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogsForMailFilterTest {

	private LogsForMailFilter activeFilter = new LogsForMailFilter();
	private LogsForMailFilter passiveFilter = new LogsForMailFilter();
	private static LoggingEvent positiveEvent;
	private static LoggingEvent negativeEvent;
	private static LoggingEvent emptyEvent;
	private final static Logger log = Logger
			.getLogger(LogsForMailFilterTest.class);

	private static final String MATCHING_STRING = "Login failed This user does not have policy";
	private static final String NON_MATCHING_STRING = "Login failed This user does have policy";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		positiveEvent = new LoggingEvent(LogsForMailFilterTest.class.getName(),
				(Category) log, Level.ERROR, MATCHING_STRING, new Exception(
						MATCHING_STRING));
		negativeEvent = new LoggingEvent(LogsForMailFilterTest.class.getName(),
				(Category) log, Level.ERROR, NON_MATCHING_STRING,
				new Exception(NON_MATCHING_STRING));
		emptyEvent = new LoggingEvent(LogsForMailFilterTest.class.getName(),
				(Category) log, Level.ERROR, "", new Exception(""));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//
	}

	@Before
	public void setUp() throws Exception {
		System.setProperty("org.opendrac.logging.smtp.mailing.active", "true");
		activeFilter = new LogsForMailFilter();
		System.setProperty("org.opendrac.logging.smtp.mailing.active", "false");
		passiveFilter = new LogsForMailFilter();
	}

	@After
	public void tearDown() throws Exception {
		//
	}

	@Test
	public void testDecidePositive() {
		assertTrue(activeFilter.decide(positiveEvent) == Filter.ACCEPT);
	}

	@Test
	public void testSkipDecidePositiveBySystemProperty() {
		assertFalse(passiveFilter.decide(positiveEvent) == Filter.ACCEPT);
	}

	@Test
	public void testDecideNegative() {
		assertTrue(activeFilter.decide(negativeEvent) == Filter.NEUTRAL);
	}

	@Test
	public void testEmptyNegative() {
		assertTrue(activeFilter.decide(emptyEvent) == Filter.NEUTRAL);
	}
}
