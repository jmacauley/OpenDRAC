package org.opendrac.nsi.dao;

import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opendrac.nsi.pathfinding.PathSegment;
import org.opendrac.nsi.util.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("pathSegmentDao")
@Transactional(readOnly = true)
public class PathSegmentDao {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("nsiSessionFactory")
	private SessionFactory sessionFactory;

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		log.info("Loaded with template: " + sessionFactory);
	}

    public static PathSegmentDao getInstance() {
        PathSegmentDao pathSegmentDao = SpringApplicationContext.getBean("pathSegmentDao", PathSegmentDao.class);
        return pathSegmentDao;
    }

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void update(PathSegment pathSegment) {
		sessionFactory.getCurrentSession().update(pathSegment);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public int create(PathSegment pathSegment) {
		return (Integer) sessionFactory.getCurrentSession().save(pathSegment);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void delete(PathSegment pathSegment) {
		sessionFactory.getCurrentSession().delete(pathSegment);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void deleteById(int id) {
		delete(findById(id));
	}

	@SuppressWarnings("unchecked")
	public List<PathSegment> findAll() {
		return this.sessionFactory.getCurrentSession()
		    .createQuery("from PathSegment path_segments").list();
	}

	public PathSegment findById(int id) {
        log.info("PathSegment.findById: id=" + id);

        if (sessionFactory == null) {
            log.info("PathSegment.findById: sessionFactory == null");
            return null;
        }

        Session session = sessionFactory.getCurrentSession();
        if (session == null) {
            log.info("PathSegment.findById: session == null");
        }

        org.hibernate.Query query = session.createQuery("from PathSegment path_segments where id = :id");
        query.setInteger("id", id);

        log.info("PathSegment.findById: query=" + query.getQueryString());

        PathSegment result = (PathSegment) query.uniqueResult();

        if (result == null) {
            log.info("PathSegment.findById: result=null");
        }
        else {
            log.info("PathSegment.findById: pathSegment=" + result.toString());
        }

		return result;
	}
}
