package com.nortel.appcore.app.drac.server.lpcp.common;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UtilityTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConvertMB2STS() {
		assertEquals("STS768C", Utility.convertMB2STS("10000")); // 200
		assertEquals("STS768C", Utility.convertMB2STS("40000")); // 800
	}

}
