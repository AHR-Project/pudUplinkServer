package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util.TxChecker;

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

	private TxChecker txChecker;

	/**
	 * @param txChecker
	 *          the txChecker to set
	 */
	@Required
	public final void setTxChecker(TxChecker txChecker) {
		this.txChecker = txChecker;
	}

	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<PositionUpdateMsg> getPositionUpdateMsgForDistribution(long startTime, long endTime, List<Node> cluster) {
		try {
			this.txChecker.checkInTx("PositionUpdateMsgs::getPositionUpdateMsgForDistribution");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (startTime >= endTime) {
			return null;
		}

		List<PositionUpdateMsg> result;
		if (cluster == null) {
			result = this.sessionFactory.getCurrentSession().createQuery("select pu from PositionUpdateMsg pu where"
			/* receptionTime in <startTime, endTime] */
			+ " pu.receptionTime > :startTime and pu.receptionTime <= :endTime").setLong("startTime", startTime)
					.setLong("endTime", endTime).list();
		} else {
			List<Long> clusterLeaderIds = new LinkedList<Long>();
			boolean first = true;
			for (Node clusterNode : cluster) {
				if (!first && (clusterNode.getClusterNodes().size() == 0)) {
					break;
				}
				clusterLeaderIds.add(clusterNode.getId());
				first = false;
			}
			assert (clusterLeaderIds.size() > 0);

			result = this.sessionFactory
					.getCurrentSession()
					.createQuery(
							"select pu from PositionUpdateMsg pu where"
							/* receptionTime in <startTime, endTime] */
							+ " pu.receptionTime > :startTime and pu.receptionTime <= :endTime"
							/* the cluster leader of the position update node is not one of the cluster leaders */
							+ " and pu.node.clusterLeaderMsg is not null"
									+ " and pu.node.clusterLeaderMsg.clusterLeaderNode.id not in (:cluster)")
					.setLong("startTime", startTime).setLong("endTime", endTime).setParameterList("cluster", clusterLeaderIds)
					.list();
		}

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional
	public void savePositionUpdateMsg(PositionUpdateMsg positionUpdateMsg) {
		try {
			this.txChecker.checkInTx("PositionUpdateMsgs::savePositionUpdateMsg");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		this.sessionFactory.getCurrentSession().saveOrUpdate(positionUpdateMsg);
	}

	@Override
	@Transactional
	public boolean removeExpiredPositionUpdateMsg(long utcTimestamp, double validityTimeMultiplier) {
		try {
			this.txChecker.checkInTx("PositionUpdateMsgs::removeExpiredPositionUpdateMsg");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"select pu from PositionUpdateMsg pu where"
								+ " (receptionTime + (validityTime * :validityTimeMultiplier)) < :utcTimestamp")
				.setDouble("validityTimeMultiplier", validityTimeMultiplier).setLong("utcTimestamp", utcTimestamp).list();

		if (result.size() == 0) {
			return false;
		}

		for (PositionUpdateMsg pu : result) {
			pu.getNode().setPositionUpdateMsg(null);
			this.sessionFactory.getCurrentSession().delete(pu);
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("removed " + result.size() + " PositionUpdateMsg objects");
		}

		return true;
	}

	private String getPositionsDump() {
		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = this.sessionFactory.getCurrentSession().createQuery("from PositionUpdateMsg pu")
				.list();

		StringBuilder s = new StringBuilder();
		s.append("[PositionUpdateMsgs]\n");
		for (PositionUpdateMsg node : result) {
			s.append(node.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional(readOnly = true)
	public void log(Logger log, Level level) {
		try {
			this.txChecker.checkInTx("PositionUpdateMsgs::log");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (log.isEnabledFor(level)) {
			log.log(level, getPositionsDump());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void print(OutputStream out) throws IOException {
		try {
			this.txChecker.checkInTx("PositionUpdateMsgs::print");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		String s = getPositionsDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
