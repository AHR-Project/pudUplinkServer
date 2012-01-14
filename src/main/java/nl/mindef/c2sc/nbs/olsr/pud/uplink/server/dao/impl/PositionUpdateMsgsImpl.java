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
	@Transactional(readOnly = true)
	public PositionUpdateMsg getPositionUpdateMsg(InetAddress mainIp) {
		if (mainIp == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = this.sessionFactory.getCurrentSession()
				.createQuery("select pu from PositionUpdateMsg pu where pu.node.mainIp = :ip").setParameter("ip", mainIp)
				.list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PositionUpdateMsg> getPositionUpdateMsgForDistribution(long startTime, long endTime, Node clusterLeader) {
		if (startTime >= endTime) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"select pu from PositionUpdateMsg pu where"
								/* receptionTime in <startTime, endTime] */
								+ " pu.receptionTime > "
								+ startTime
								+ " and pu.receptionTime <= "
								+ endTime
								/* the cluster leader of the node is not the specified cluster leader */
								+ ((clusterLeader == null) ? "" : " and pu.node.clusterLeaderMsg is not null"
										+ " and pu.node.clusterLeaderMsg.clusterLeaderNode.id != " + clusterLeader.getId())).list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional
	public void savePositionUpdateMsg(PositionUpdateMsg positionUpdateMsg) {
		this.sessionFactory.getCurrentSession().saveOrUpdate(positionUpdateMsg);
	}

	@Override
	@Transactional
	public boolean removeExpiredPositionUpdateMsg(long utcTimestamp, double validityTimeMultiplier) {
		@SuppressWarnings("unchecked")
		List<PositionUpdateMsg> result = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"select pu from PositionUpdateMsg pu where (receptionTime + (validityTime * " + validityTimeMultiplier
								+ ")) < " + utcTimestamp).list();

		if (result.size() == 0) {
			return false;
		}

		for (PositionUpdateMsg pu : result) {
			pu.getNode().setPositionUpdateMsg(null);
			this.sessionFactory.getCurrentSession().merge(pu.getNode());
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
		if (log.isEnabledFor(level)) {
			log.log(level, getPositionsDump());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void print(OutputStream out) throws IOException {
		String s = getPositionsDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
