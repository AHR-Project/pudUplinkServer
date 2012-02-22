package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

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
	 *          the sessionFactory to set
	 */
	@Required
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Node> getAllNodes() {
		@SuppressWarnings("unchecked")
		List<Node> result = this.sessionFactory.getCurrentSession().createQuery("select node from Node node").list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	private void addToCluster(List<Node> allNodes, Set<Node> clusterLeaders, List<Node> cluster, Node node, boolean up) {
		assert (allNodes != null);
		assert (cluster != null);
		assert (node != null);
		assert (!cluster.contains(node));

		cluster.add(node);
		allNodes.remove(node);

		/* search down */
		Set<ClusterLeaderMsg> clusterNodeMsgs = node.getClusterNodes();
		if (clusterNodeMsgs.size() > 0) {
			clusterLeaders.add(node);

			for (ClusterLeaderMsg clusterNodeMsg : clusterNodeMsgs) {
				Node clusterNode = clusterNodeMsg.getNode();
				if ((node != clusterNode) && !cluster.contains(clusterNode)) {
					addToCluster(allNodes, clusterLeaders, cluster, clusterNode, false);
				}
			}
		}

		/* search up */
		if (up) {
			ClusterLeaderMsg clusterLeaderMsg = node.getClusterLeaderMsg();
			if (clusterLeaderMsg != null) {
				Node clusterLeaderNode = clusterLeaderMsg.getClusterLeaderNode();
				if ((node != clusterLeaderNode) && !clusterLeaders.contains(clusterLeaderNode)) {
					addToCluster(allNodes, clusterLeaders, cluster, clusterLeaderNode, true);
				}
			}
		}
	}

	protected class NodeComparatorOnClusterNodes_ReceptionTime_MainIP implements Comparator<Node> {
		private int compareInetAddresses(InetAddress ip1, InetAddress ip2) {
			byte[] ip1a = ip1.getAddress();
			byte[] ip2a = ip2.getAddress();

			/* IPv4 before IPv6 */
			if (ip1a.length < ip2a.length) {
				return -1;
			}
			if (ip1a.length > ip2a.length) {
				return 1;
			}

			/* 2 IPs of the same type, so we have to compare each byte */
			for (int i = 0; i < ip1a.length; i++) {
				int b1 = ip1a[i] & 0xff;
				int b2 = ip2a[i] & 0xff;
				if (b1 == b2) {
					continue;
				}

				if (b1 < b2) {
					return -1;
				}

				return 1;
			}

			return 0;
		}

		@Override
		public int compare(Node o1, Node o2) {
			/* first sort on the number of cluster nodes (cluster leaders first) */
			int o1ClusterNodesCount = o1.getClusterNodes().size();
			int o2ClusterNodesCount = o2.getClusterNodes().size();
			if (o1ClusterNodesCount > o2ClusterNodesCount) {
				return -1;
			}
			if (o1ClusterNodesCount < o2ClusterNodesCount) {
				return 1;
			}

			/* then sort on the reception time of the cluster leader message (recently seen nodes first) */
			ClusterLeaderMsg o1ClMsg = o1.getClusterLeaderMsg();
			ClusterLeaderMsg o2ClMsg = o2.getClusterLeaderMsg();
			long o1ReceptionTime = (o1ClMsg == null) ? 0 : o1ClMsg.getReceptionTime();
			long o2ReceptionTime = (o2ClMsg == null) ? 0 : o2ClMsg.getReceptionTime();
			if (o1ReceptionTime > o2ReceptionTime) {
				return -1;
			}
			if (o1ReceptionTime < o2ReceptionTime) {
				return 1;
			}

			/* as a final discriminator sort on the mainIP of the node */
			return compareInetAddresses(o1.getMainIp(), o2.getMainIp());
		}
	}

	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<List<Node>> getClusters(RelayServer relayServer) {
		List<Node> allNodes = null;
		if (relayServer == null) {
			allNodes = this.sessionFactory.getCurrentSession()
					.createQuery("select node from Node node order by size(node.clusterNodes) desc").list();
		} else {
			allNodes = this.sessionFactory
					.getCurrentSession()
					.createQuery(
							"select node from Node node where node.sender is not null and node.sender.relayServer.id = :rsId "
									+ "order by size(node.clusterNodes) desc").setLong("rsId", relayServer.getId().longValue()).list();
		}

		if (allNodes.size() == 0) {
			return null;
		}

		List<List<Node>> clusters = new LinkedList<List<Node>>();

		while (!allNodes.isEmpty()) {
			LinkedList<Node> cluster = new LinkedList<Node>();
			Set<Node> clusterLeaders = new HashSet<Node>();
			addToCluster(allNodes, clusterLeaders, cluster, allNodes.get(0), true);
			if (cluster.size() > 0) {
				Collections.sort(cluster, new NodeComparatorOnClusterNodes_ReceptionTime_MainIP());
				clusters.add(cluster);
			}
		}

		if (clusters.size() > 0) {
			return clusters;
		}

		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Node getNode(InetAddress mainIp) {
		if (mainIp == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Node> result = this.sessionFactory.getCurrentSession()
				.createQuery("select node from Node node where node.mainIp = :ip").setParameter("ip", mainIp).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Node> getClusterLeaders() {
		@SuppressWarnings("unchecked")
		List<Node> result = this.sessionFactory.getCurrentSession().createQuery(
		/* get nodes and their senders (eagerly fetched) */
		"select node from Node node left join node.sender where"
		/* node has cluster nodes */
		+ " node.clusterNodes is not empty" + " order by node.mainIp").list();
		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public Node getSubstituteClusterLeader(Node clusterLeader) {
		assert (clusterLeader != null);
		long clId = clusterLeader.getId().longValue();

		@SuppressWarnings("unchecked")
		List<Node> result = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						/* get nodes and their senders (eagerly fetched) */
						"select node from Node node left join node.sender where"
						/* node is not the cluster leader itself and node points to the cluster leader */
						+ " node.id != :clId"
								+ " and node.clusterLeaderMsg is not null and node.clusterLeaderMsg.clusterLeaderNode.id = :clId"
								/* node has a valid sender (a sender always has a valid IP address and a valid port) */
								+ " and node.sender is not null "
								/* keep the node with the most recently received cluster leader message on top of the list */
								+ "order by node.clusterLeaderMsg.receptionTime desc").setLong("clId", clId).list();

		if (result.size() == 0) {
			return null;
		}

		return result.get(0);
	}

	@Override
	@Transactional
	public void saveNode(Node node) {
		this.sessionFactory.getCurrentSession().saveOrUpdate(node);
	}

	@Override
	@Transactional
	public boolean removeExpiredNodes() {
		@SuppressWarnings("unchecked")
		List<Node> result = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"select node from Node node where positionUpdateMsg is null and clusterLeaderMsg is null and size(clusterNodes) = 0")
				.list();

		if (result.size() == 0) {
			return false;
		}

		for (Node node : result) {
			node.setSender(null);
			this.sessionFactory.getCurrentSession().delete(node);
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("removed " + result.size() + " Node objects");
		}

		return true;
	}

	private String getNodesDump() {
		@SuppressWarnings("unchecked")
		List<Node> result = this.sessionFactory.getCurrentSession().createQuery("from Node node").list();

		StringBuilder s = new StringBuilder();
		s.append("[Nodes]\n");
		for (Node node : result) {
			s.append(node.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional(readOnly = true)
	public void log(Logger log, Level level) {
		if (log.isEnabledFor(level)) {
			log.log(level, getNodesDump());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void print(OutputStream out) throws IOException {
		String s = getNodesDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
