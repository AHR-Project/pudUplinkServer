package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Positions;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.NodePosition;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PositionsImpl implements Positions {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *            the sessionFactory to set
	 */
	@Required
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional
	public NodePosition getPosition(InetAddress mainIp) {
		if (mainIp == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<NodePosition> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select node from NodePosition node where node.mainIp = :par1")
				.setParameter("par1", mainIp).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional
	public List<NodePosition> getPositionsForDistribution(long startTime,
			long endTime, Node clusterLeader) {
		if (startTime >= endTime) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<NodePosition> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select pos from NodePosition pos where"
								+ " pos.receptionTime > :startTime and"
								+ " pos.receptionTime <= :endTime"
								+ ((clusterLeader == null) ? ""
										: " and pos.node != null"
												+ " and pos.node.clusterLeader != null"
												+ " and pos.node.clusterLeader.id != "
												+ clusterLeader.getId()
												+ " and pos.node.downlinkPort != "
												+ Node.DOWNLINK_PORT_INVALID))
				.setParameter("startTime", startTime)
				.setParameter("endTime", endTime).list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional
	public void saveNodePosition(NodePosition position, boolean newObject) {
		if (newObject) {
			sessionFactory.getCurrentSession().saveOrUpdate(position);
		} else {
			sessionFactory.getCurrentSession().merge(position);
		}
	}

	@Override
	@Transactional
	public void removeExpiredNodePosition(double validityTimeMultiplier) {
		long utcTimestamp = System.currentTimeMillis();

		int cnt = sessionFactory
				.getCurrentSession()
				.createQuery(
						"delete NodePosition position where"
								+ " (receptionTime + (validityTime * "
								+ validityTimeMultiplier + ")) < "
								+ utcTimestamp).executeUpdate();
		if (cnt != 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("  removed " + cnt + " positions");
			}
		}
		return;
	}

	@Override
	@Transactional
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			@SuppressWarnings("unchecked")
			List<NodePosition> result = sessionFactory.getCurrentSession()
					.createQuery("from NodePosition node").list();

			if (result.size() == 0) {
				return;
			}

			StringBuilder s = new StringBuilder();
			s.append("*** Positions ***\n");
			for (NodePosition node : result) {
				s.append(node.toString());
			}
			logger.log(level, s.toString());
		}
	}
}
