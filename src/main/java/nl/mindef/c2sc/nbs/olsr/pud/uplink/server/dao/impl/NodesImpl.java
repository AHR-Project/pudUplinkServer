package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NodesImpl implements Nodes {
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

	private boolean clusterLeadersIncludesTransitionalNodes = false;

	/**
	 * @param clusterLeadersIncludesTransitionalNodes
	 *            the clusterLeadersIncludesTransitionalNodes to set
	 */
	@Required
	public final void setClusterLeadersIncludesTransitionalNodes(
			boolean clusterLeadersIncludesTransitionalNodes) {
		this.clusterLeadersIncludesTransitionalNodes = clusterLeadersIncludesTransitionalNodes;
	}

	@Override
	@Transactional
	public Node getNode(InetAddress mainIp) {
		if (mainIp == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Node> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select node from Node node where node.mainIp = :par1")
				.setParameter("par1", mainIp).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional
	public List<Node> getClusterLeaders() {
		@SuppressWarnings("unchecked")
		List<Node> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select node from Node node "
								+ "where clusterLeader.id = id"
								+ (clusterLeadersIncludesTransitionalNodes ? " or clusterLeader.clusterLeader.id != clusterLeader.id"
										: "")).list();

		return result;
	}

	@Override
	@Transactional
	public void saveNode(Node node, boolean newObject) {
		if (newObject) {
			sessionFactory.getCurrentSession().saveOrUpdate(node);
		} else {
			sessionFactory.getCurrentSession().merge(node);
		}
	}

	@Override
	@Transactional
	public void removeExpiredNodes(double validityTimeMultiplier) {
		long utcTimestamp = System.currentTimeMillis();

		int cnt = sessionFactory
				.getCurrentSession()
				.createQuery(
						"delete Node node"
								+ " where (receptionTime + (validityTime * "
								+ validityTimeMultiplier + ")) < "
								+ utcTimestamp).executeUpdate();
		if (cnt != 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("  removed " + cnt + " nodes");
			}
		}
		return;
	}

	private String getNodesDump() {
		@SuppressWarnings("unchecked")
		List<Node> result = sessionFactory.getCurrentSession()
				.createQuery("from Node node").list();

		StringBuilder s = new StringBuilder();
		s.append("[Nodes]\n");
		for (Node node : result) {
			s.append(node.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getNodesDump());
		}
	}

	@Override
	@Transactional
	public void print(OutputStream out) throws IOException {
		String s = getNodesDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
