package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ClusterLeaderMsgsImpl implements ClusterLeaderMsgs {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *          the sessionFactory to set
	 */
	@Required
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional
	public ClusterLeaderMsg getClusterLeaderMsg(InetAddress mainIp) {
		if (mainIp == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<ClusterLeaderMsg> result = sessionFactory.getCurrentSession()
				.createQuery("select cl from ClusterLeaderMsg cl where cl.node.mainIp = :ip").setParameter("ip", mainIp).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional
	public void saveClusterLeaderMsg(ClusterLeaderMsg clusterLeaderMsg, boolean newObject) {
		if (newObject) {
			sessionFactory.getCurrentSession().saveOrUpdate(clusterLeaderMsg);
		} else {
			sessionFactory.getCurrentSession().merge(clusterLeaderMsg);
		}
	}

	// FIXME set readonly attributes on relevant queries
	@Override
	@Transactional
	public boolean removeExpiredClusterLeaderMsg(long utcTimestamp, double validityTimeMultiplier) {
		@SuppressWarnings("unchecked")
		List<ClusterLeaderMsg> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select cl from ClusterLeaderMsg cl where"
								+ " (receptionTime + (validityTime * :validityTimeMultiplier)) < :utcTimestamp")
				.setParameter("validityTimeMultiplier", validityTimeMultiplier).setParameter("utcTimestamp", utcTimestamp)
				.list();

		if (result.size() == 0) {
			return false;
		}

		for (ClusterLeaderMsg cl : result) {
			cl.getNode().setClusterLeaderMsg(null);
			cl.setClusterLeaderNode(null);
			sessionFactory.getCurrentSession().merge(cl.getNode());
			sessionFactory.getCurrentSession().delete(cl);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("removed " + result.size() + " clusterLeaderMsg objects");
		}

		sessionFactory.getCurrentSession().flush();
		return true;
	}

	private String getClusterLeaderMsgsDump() {
		@SuppressWarnings("unchecked")
		List<ClusterLeaderMsg> result = sessionFactory.getCurrentSession()
				.createQuery("from ClusterLeaderMsg clusterLeaderMsg").list();

		StringBuilder s = new StringBuilder();
		s.append("[ClusterLeaderMsgs]\n");
		for (ClusterLeaderMsg clusterLeaderMsg : result) {
			s.append(clusterLeaderMsg.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getClusterLeaderMsgsDump());
		}
	}

	@Override
	@Transactional
	public void print(OutputStream out) throws IOException {
		String s = getClusterLeaderMsgsDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
