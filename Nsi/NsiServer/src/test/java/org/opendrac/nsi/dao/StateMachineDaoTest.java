package org.opendrac.nsi.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.opendrac.nsi.domain.StateMachine;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.security.NsaSecurityContext;
import org.opendrac.nsi.security.SessionSecurity;
import org.opendrac.nsi.util.UUIDUtilities;
import org.opendrac.test.TestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/opendrac-nsi.xml",
    "/spring/opendrac-nsi-database.xml" })
public class StateMachineDaoTest {

	@Autowired
	@Qualifier("stateMachineDao")
	private StateMachineDao stateMachineDao;

	private final String globalUserName = "global_user_name_"
	    + UUIDUtilities.getUrnUuid();

	private List<Integer> ids = new ArrayList<Integer>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestHelper.INSTANCE.initialize();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		addTestStateMachine(100);
	}

	@After
	public void tearDown() {
		for (final Integer id : ids) {
			if (stateMachineDao.findById(id) != null) {
				stateMachineDao.deleteById(id);
			}
		}
	}

	@Test
	public void testFindById() throws Exception {
		final StateMachine stateMachine = stateMachineDao.findById(ids.get(0));
		assertNotNull(stateMachine);
		assertEquals("global_user_name: 100", stateMachine.getSessionSecurity()
		    .getGlobalUserName());
		assertEquals(2, stateMachine.getRoutePathList().size());
	}

	@Test
	public void testUpdate() throws Exception {
		final StateMachine stateMachine = stateMachineDao.findById(ids.get(0));
		final String connectionId = "NEW_ID";
		stateMachine.setConnectionId(connectionId);
		stateMachine.getSessionSecurity().setGlobalUserName(
		    globalUserName + ": " + 100 + 1);
		stateMachineDao.update(stateMachine);
		assertEquals(connectionId, stateMachineDao.findById(ids.get(0))
		    .getConnectionId());
		assertEquals(globalUserName + ": " + 100 + 1,
		    stateMachineDao.findById(ids.get(0)).getSessionSecurity()
		        .getGlobalUserName());
	}

	@Test
	public void testDelete() {
		final StateMachine stateMachine = stateMachineDao.findById(ids.get(0));
		assertEquals(stateMachine.getId(), (int) ids.get(0));
		stateMachineDao.delete(stateMachine);
		assertNull(stateMachineDao.findById(ids.get(0)));
	}

	@Test
	public void testDeleteById() {
		final StateMachine stateMachine = stateMachineDao.findById(ids.get(0));
		assertEquals(stateMachine.getId(), (int) ids.get(0));
		stateMachineDao.deleteById(ids.get(0));
		assertNull(stateMachineDao.findById(ids.get(0)));
	}

	@Test
	public void testFindAll() throws Exception {
		final int amount = 10;
		for (int i = 0; i < amount; i++) {
			addTestStateMachine(i);
		}
		// +1 because one entry was already created during setup
		assertEquals(amount + 1, stateMachineDao.findAll().size());
	}

	private void addTestStateMachine(int id) throws ServiceException {
		StateMachine stateMachine = new StateMachine();
		stateMachine.setConnectionId("connection_id " + id);
		stateMachine.setCurrentState(ConnectionStateType.SCHEDULED);
		stateMachine.setDescription("description " + id);
		stateMachine.setDesiredBandwidth(100);
		stateMachine.setEndTime(new GregorianCalendar());
		stateMachine.setGlobalReservationId(UUIDUtilities.getUrnUuid());
		stateMachine.setMaximumBandwidth(Integer.MAX_VALUE);
		stateMachine.setMinimumBandwidth(Integer.MIN_VALUE);
		stateMachine.setNsaSecurityContext(new NsaSecurityContext());
		stateMachine.setPath(new PathType());
		stateMachine.setProviderNSA("providerNSA " + id);
		stateMachine.setReplyTo("replyto:" + id);
		stateMachine.setRequesterNSA("requesterNSA " + id);
		final List<PathSegment> pathSegments = new ArrayList<PathSegment>();
		final PathSegment pathSegment = new PathSegment();
		pathSegment.setData("some data 1: " + id);
		pathSegments.add(pathSegment);
		final PathSegment pathSegment2 = new PathSegment();
		pathSegment.setData("some data 2: " + id);
		pathSegments.add(pathSegment2);
		stateMachine.setRoutePathList(pathSegments);
		stateMachine.setServiceParameters(new ServiceParametersType());
		final SessionSecurity sessionSecurity = new SessionSecurity();
		sessionSecurity.setGlobalUserName("global_user_name: " + id);
		stateMachine.setSessionSecurity(sessionSecurity);
		stateMachine.setStartTime(new GregorianCalendar());
		ids.add(stateMachineDao.create(stateMachine));
	}

}
