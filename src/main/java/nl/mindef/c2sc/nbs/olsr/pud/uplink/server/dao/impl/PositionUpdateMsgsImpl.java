package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PositionUpdateMsgsImpl implements PositionUpdateMsgs {
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
	public PositionUpdateMsg getPosition(InetAddress mainIp) {
		if (mainIp == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = sessionFactory.getCurrentSession()
				.createQuery("select pos from PositionUpdateMsg pos where pos.node.mainIp = :ip").setParameter("ip", mainIp)
				.list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional
	public List<PositionUpdateMsg> getPositionsForDistribution(long startTime, long endTime, Node clusterLeader) {
		if (startTime >= endTime) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select pos from PositionUpdateMsg pos where"
								+ " pos.receptionTime > :startTime and"
								+ " pos.receptionTime <= :endTime"
								+ ((clusterLeader == null) ? "" : " and pos.node is not null"
										+ " and pos.node.clusterLeaderMsg is not null"
										+ " and pos.node.clusterLeaderMsg.clusterLeader is not null"
										+ " and pos.node.clusterLeaderMsg.clusterLeader.id != " + clusterLeader.getId()))
				.setParameter("startTime", startTime).setParameter("endTime", endTime).list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional
	public void saveNodePosition(PositionUpdateMsg position, boolean newObject) {
		if (newObject) {
			sessionFactory.getCurrentSession().saveOrUpdate(position);
		} else {
			sessionFactory.getCurrentSession().merge(position);
		}
	}

	@Override
	@Transactional
	public boolean removeExpiredNodePosition(long utcTimestamp, double validityTimeMultiplier) {
		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select pu from PositionUpdateMsg pu where (receptionTime + (validityTime * " + validityTimeMultiplier
								+ ")) < " + utcTimestamp).list();

		if (result.size() == 0) {
			return false;
		}

		for (PositionUpdateMsg pu : result) {
			pu.getNode().setPositionUpdateMsg(null);
			sessionFactory.getCurrentSession().merge(pu.getNode());
			sessionFactory.getCurrentSession().delete(pu);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("  removed " + result.size() + " PositionUpdateMsg objects");
		}

		sessionFactory.getCurrentSession().flush();
		return true;
	}

	private String getPositionsDump() {
		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = sessionFactory.getCurrentSession().createQuery("from PositionUpdateMsg node")
				.list();

		StringBuilder s = new StringBuilder();
		s.append("[PositionUpdateMsgs]\n");
		for (PositionUpdateMsg node : result) {
			s.append(node.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getPositionsDump());
		}
	}

	@Override
	@Transactional
	public void print(OutputStream out) throws IOException {
		String s = getPositionsDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
