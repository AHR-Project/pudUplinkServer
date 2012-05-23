/*
 *  Copyright (C) 2012 Royal Dutch Army
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *  MA  02110-1301, USA.
 */
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util.TxChecker;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.WireFormatChecker;

import org.olsr.plugin.pud.ClusterLeader;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ClusterLeaderHandlerImpl implements ClusterLeaderHandler {
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

	private Senders senders = null;

	/**
	 * @param senders
	 *          the senders to set
	 */
	@Required
	public final void setSenders(Senders senders) {
		this.senders = senders;
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

	/** the wire format checker */
	private WireFormatChecker wireFormatChecker;

	/**
	 * @param wireFormatChecker
	 *          the wireFormatChecker to set
	 */
	@Required
	public final void setWireFormatChecker(WireFormatChecker wireFormatChecker) {
		this.wireFormatChecker = wireFormatChecker;
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
	@Transactional
	public boolean handleClusterLeaderMessage(Sender sender, long utcTimestamp, ClusterLeader clMsg) {
		try {
			this.txChecker.checkInTx("ClusterLeaderHandler::handleClusterLeaderMessage");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		assert (clMsg != null);
		assert (sender != null);

		if (!this.wireFormatChecker.checkUplinkMessageWireFormat(sender, clMsg)) {
			return false;
		}

		if (sender.getId() == null) {
			this.senders.saveSender(sender);
		}

		InetAddress originator = clMsg.getClusterLeaderOriginator();
		InetAddress clusterLeader = clMsg.getClusterLeaderClusterLeader();

		/* retrieve the node that sent the cluster leader update */
		Node originatorNode = this.nodes.getNode(originator);
		if (originatorNode == null) {
			/* new node */
			originatorNode = new Node(originator, sender);
			this.nodes.saveNode(originatorNode);
		}

		/* link the node to the sender from which it was received */
		originatorNode.setSender(sender);

		/* retrieve the cluster leader node of the node that sent the cluster leader update */
		Node clusterLeaderNode = this.nodes.getNode(clusterLeader);
		if (clusterLeaderNode == null) {
			/* new node */
			clusterLeaderNode = new Node(clusterLeader, null);
			this.nodes.saveNode(clusterLeaderNode);
		}

		/* get the cluster leader update of the node */
		ClusterLeaderMsg storedClusterLeader = originatorNode.getClusterLeaderMsg();
		if (storedClusterLeader == null) {
			/* new cluster leader update */
			storedClusterLeader = new ClusterLeaderMsg(originatorNode, clusterLeaderNode);
			this.clusterLeaderMsgs.saveClusterLeaderMsg(storedClusterLeader);
		}

		/* fill in the cluster leader update */
		storedClusterLeader.setReceptionTime(utcTimestamp);
		storedClusterLeader.setValidityTime(clMsg.getClusterLeaderValidityTime() * 1000);

		/* link the cluster leader update to the node */
		originatorNode.setClusterLeaderMsg(storedClusterLeader);

		/* link the cluster leader update to the cluster leader's clusterNodes */
		storedClusterLeader.setClusterLeaderNode(clusterLeaderNode);

		/* save the nodes and cluster leader update. explicitly saving the nodes is not needed since these are cascaded */
		this.clusterLeaderMsgs.saveClusterLeaderMsg(storedClusterLeader);

		return true;
	}
}
