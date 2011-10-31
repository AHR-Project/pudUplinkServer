package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;

import org.apache.log4j.Logger;
import org.olsr.plugin.pud.ClusterLeader;
import org.olsr.plugin.pud.WireFormatConstants;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ClusterLeaderHandlerImpl implements ClusterLeaderHandler {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/** the Node handler */
	private Nodes nodes;

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	@Required
	public final void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	@Override
	@Transactional
	public boolean handleClusterLeaderMessage(long utcTimestamp,
			ClusterLeader clUpMsg, RelayServer relayServer) {
		assert (relayServer != null);

		if (clUpMsg.getClusterLeaderVersion() != WireFormatConstants.VERSION) {
			logger.warn("Received wrong version of cluster leader"
					+ " message, expected version "
					+ WireFormatConstants.VERSION + ", received version "
					+ clUpMsg.getClusterLeaderVersion() + ": ignored");
			return false;
		}

		InetAddress originator = clUpMsg.getClusterLeaderOriginator();
		InetAddress clusterLeader = clUpMsg.getClusterLeaderClusterLeader();
		int downlinkPort = clUpMsg.getClusterLeaderDownlinkPort();

		Node originatorNode = nodes.getNode(originator);
		boolean originatorNodeIsNew = false;
		if (originatorNode == null) {
			originatorNode = new Node();
			originatorNodeIsNew = true;
		}

		Node clusterleaderNode;
		if (originator.equals(clusterLeader)) {
			clusterleaderNode = originatorNode;
		} else {
			clusterleaderNode = nodes.getNode(clusterLeader);
			if (clusterleaderNode == null) {
				logger.warn("Cluster leader message from "
						+ originator.getHostAddress()
						+ " mentions a non-existent cluster leader" + " ("
						+ clusterLeader.getHostAddress() + "): creating a stub");
				clusterleaderNode = new Node();
				clusterleaderNode.setMainIp(clusterLeader);
				clusterleaderNode.setReceptionTime(utcTimestamp);
				clusterleaderNode.setValidityTime(clUpMsg
						.getClusterLeaderValidityTime() * 1000);
				clusterleaderNode.setRelayServer(relayServer);
				nodes.saveNode(clusterleaderNode, true);
			}
		}

		originatorNode.setMainIp(originator);
		originatorNode.setDownlinkPort(downlinkPort);
		originatorNode.setReceptionTime(utcTimestamp);
		originatorNode
				.setValidityTime(clUpMsg.getClusterLeaderValidityTime() * 1000);
		originatorNode.setRelayServer(relayServer);

		originatorNode.setClusterLeader(clusterleaderNode);

		nodes.saveNode(originatorNode, originatorNodeIsNew);

		return true;
	}
}
