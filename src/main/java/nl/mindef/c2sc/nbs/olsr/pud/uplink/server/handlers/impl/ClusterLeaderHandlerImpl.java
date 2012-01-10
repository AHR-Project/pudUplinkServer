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
	public boolean handleClusterLeaderMessage(Gateway gateway, long utcTimestamp, ClusterLeader clUpMsg) {
		assert (clUpMsg != null);

		if (clUpMsg.getClusterLeaderVersion() != WireFormatConstants.VERSION) {
			logger.warn("Received wrong version of cluster leader" + " message, expected version "
					+ WireFormatConstants.VERSION + ", received version " + clUpMsg.getClusterLeaderVersion() + ": ignored");
			return false;
		}

		assert (gateway != null);

		InetAddress originator = clUpMsg.getClusterLeaderOriginator();
		InetAddress clusterLeader = clUpMsg.getClusterLeaderClusterLeader();

		/* retrieve the node that sent the cluster leader update */
		Node originatorNode = nodes.getNode(originator);
		if (originatorNode == null) {
			/* new node */
			originatorNode = new Node(originator, gateway);
			nodes.saveNode(originatorNode, true);
		}

		/* link the node to the gateway from which it was received */
		originatorNode.setGateway(gateway);

		/* retrieve the cluster leader node of the node that sent the cluster leader update */
		Node clusterLeaderNode = nodes.getNode(clusterLeader);
		if (clusterLeaderNode == null) {
			/* new node */
			clusterLeaderNode = new Node(clusterLeader, null);
			nodes.saveNode(clusterLeaderNode, true);
		}

		/* get the cluster leader update of the node */
		ClusterLeaderMsg storedClusterLeader = originatorNode.getClusterLeaderMsg();
		if (storedClusterLeader == null) {
			/* new cluster leader update */
			storedClusterLeader = new ClusterLeaderMsg(originatorNode, clusterLeaderNode);
			clusterLeaderMsgs.saveClusterLeaderMsg(storedClusterLeader, true);
		}

		/* fill in the cluster leader update */
		storedClusterLeader.setReceptionTime(utcTimestamp);
		storedClusterLeader.setValidityTime(clUpMsg.getClusterLeaderValidityTime() * 1000);

		/* link the cluster leader update to the node */
		originatorNode.setClusterLeaderMsg(storedClusterLeader);

		/* link the clust leader update to the cluster leader's clusterNodes */
		storedClusterLeader.setClusterLeader(clusterLeaderNode);

		/* save the nodes and cluster leader update. explicitly saving the nodes is not needed since these are cascaded */
		clusterLeaderMsgs.saveClusterLeaderMsg(storedClusterLeader, false);

		return true;
	}
}
