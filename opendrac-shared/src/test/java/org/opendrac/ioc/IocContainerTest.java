package org.opendrac.ioc;

import static org.junit.Assert.*;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IocContainerTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//
	}

	@Before
	public void setUp() throws Exception {
		//
	}

	@After
	public void tearDown() throws Exception {
		IocContainer.close();
	}

	@Test
	public void testSetupNoConfigs() {
		// should fail because configs are not set
		try {
			IocContainer.start();
			fail("Previous call should have triggerd an exception");
		}
		catch (Exception e) {
			assertEquals(IllegalStateException.class, e.getClass());
		}

	}

	@Test
	public void testSetupCorrectly() {
		// should not fail because configs are set
		try {
			IocContainer.setConfigs("spring/opendrac-ioc-1-test.xml");
			IocContainer.start();
		}
		catch (Exception e) {
			log.error("Error: ", e);
			fail("Should not throw an exception");
		}

	}

	@Test
	public void testSetupOverridingConfigs() {

		try {
			IocContainer.setConfigs("spring/opendrac-ioc-1-test.xml");
			IocContainer.start();
		}
		catch (Exception e) {
			log.error("Error: ", e);
			fail("Should not throw an exception");
		}

		// should refresh context set up in the previous step
		try {
			IocContainer.setConfigs("spring/opendrac-ioc-2-test.xml");
			IocContainer.start();
		}
		catch (Exception e) {
			log.error("Error: ", e);
			fail("Should not throw an exception");
		}
	}

	@Test
	public void testGetBeanByClass() {
		IocContainer.setConfigs("spring/opendrac-ioc-1-test.xml");
		IocContainer.start();
		final DataSource datasource = IocContainer.getBean(DataSource.class);
		assertNotNull(datasource);
	}

	@Test
	public void testGetBeanByClassAndName() {
		IocContainer.setConfigs("spring/opendrac-ioc-1-test.xml");
		IocContainer.start();
		final DataSource datasource = IocContainer.getBean("dataSource",
		    DataSource.class);
		assertNotNull(datasource);
	}

	@Test
	@Ignore
	public void testEncryptablePropertyPlaceholderConfigurer() {
		IocContainer.startWithDefaultConfig();
		try {
			final Connection connection = IocContainer.getBean(DataSource.class)
			    .getConnection();
			assertNotNull(connection);
		}
		catch (Exception e) {
			log.error("Error: ");
			fail();
		}

	}

	@Test
	@Ignore
	public void testSnmp() {
		log.error("smtp / snmp!");
	}
	
	@Test
	@Ignore
	public void testDatasourceService() throws Exception {
		IocContainer.setConfigs("spring/opendrac-common.xml", "spring/opendrac-ioc-2-test.xml");
		IocContainer.start();
		DataSource rmiDataSource = IocContainer.getBean("rmiDataSource", DataSource.class);
		assertNotNull(rmiDataSource);
		final Connection connection = rmiDataSource.getConnection();
		assertNotNull(connection);
		assertEquals("H2", connection.getMetaData().getDatabaseProductName());
	}

}
