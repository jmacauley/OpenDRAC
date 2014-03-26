package org.opendrac.nsi.dao;

import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.SessionFactory;
import org.opendrac.nsi.domain.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("stateMachineDao")
@Transactional(readOnly = true)
public class StateMachineDao {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("nsiSessionFactory")
	private SessionFactory sessionFactory;

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		log.info("Loaded with template: " + sessionFactory);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void update(StateMachine stateMachine) {
		sessionFactory.getCurrentSession().update(stateMachine);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public int create(StateMachine stateMachine) {
		return (Integer) sessionFactory.getCurrentSession().save(stateMachine);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void delete(StateMachine stateMachine) {
		sessionFactory.getCurrentSession().delete(stateMachine);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void deleteById(int id) {
		delete(findById(id));
	}

	@SuppressWarnings("unchecked")
	public List<StateMachine> findAll() {
		return this.sessionFactory.getCurrentSession()
		    .createQuery("from StateMachine state_machines").list();
	}

	public StateMachine findById(int id) {
		return (StateMachine) this.sessionFactory.getCurrentSession()
		    .createQuery("from StateMachine state_machines where id = ? ")
		    .setParameter(0, id).uniqueResult();
	}
}
