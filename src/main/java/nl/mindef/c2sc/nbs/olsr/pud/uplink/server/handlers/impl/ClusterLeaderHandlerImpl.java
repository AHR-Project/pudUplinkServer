package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
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

	/** the PositionUpdateMsgs handler */
	private ClusterLeaderMsgs clusterLeaderMsgs;

	/**
	 * @param clusterLeaderMsgs
	 *          the clusterLeaderMsgs to set
	 */
	@Required
	public final void setClusterLeaderMsgs(ClusterLeaderMsgs clusterLeaderMsgs) {
		this.clusterLeaderMsgs = clusterLeaderMsgs;
	}

	/** the Node handler */
	private Nodes nodes;

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	@Required
	public final void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	@Override
	@Transactional
	public boolean handleClusterLeaderMessage(Gateway gateway, long utcTimestamp, ClusterLeader clMsg) {
		assert (clMsg != null);

		if (clMsg.getClusterLeaderVersion() != WireFormatConstants.VERSION) {
			logger.error("Received wrong version of cluster leader message from " + gateway.getIp().getHostAddress() + ":"
					+ gateway.getPort() + ", expected version " + WireFormatConstants.VERSION + ", received version "
					+ clMsg.getClusterLeaderVersion() + ": ignored");
			return false;
		}

		assert (gateway != null);

		InetAddress originator = clMsg.getClusterLeaderOriginator();
		InetAddress clusterLeader = clMsg.getClusterLeaderClusterLeader();

		/* retrieve the node that sent the cluster leader update */
		Node originatorNode = nodes.getNode(originator);
		if (originatorNode == null) {
			/* new node */
			originatorNode = new Node(originator, gateway);
			nodes.saveNode(originatorNode);
		}

		/* link the node to the gateway from which it was received */
		originatorNode.setGateway(gateway);

		/* retrieve the cluster leader node of the node that sent the cluster leader update */
		Node clusterLeaderNode = nodes.getNode(clusterLeader);
		if (clusterLeaderNode == null) {
			/* new node */
			clusterLeaderNode = new Node(clusterLeader, null);
			nodes.saveNode(clusterLeaderNode);
		}

		/* get the cluster leader update of the node */
		ClusterLeaderMsg storedClusterLeader = originatorNode.getClusterLeaderMsg();
		if (storedClusterLeader == null) {
			/* new cluster leader update */
			storedClusterLeader = new ClusterLeaderMsg(originatorNode, clusterLeaderNode);
			clusterLeaderMsgs.saveClusterLeaderMsg(storedClusterLeader);
		}

		/* fill in the cluster leader update */
		storedClusterLeader.setReceptionTime(utcTimestamp);
		storedClusterLeader.setValidityTime(clMsg.getClusterLeaderValidityTime() * 1000);

		/* link the cluster leader update to the node */
		originatorNode.setClusterLeaderMsg(storedClusterLeader);

		/* link the cluster leader update to the cluster leader's clusterNodes */
		storedClusterLeader.setClusterLeaderNode(clusterLeaderNode);

		/* save the nodes and cluster leader update. explicitly saving the nodes is not needed since these are cascaded */
		clusterLeaderMsgs.saveClusterLeaderMsg(storedClusterLeader);

		return true;
	}
}
