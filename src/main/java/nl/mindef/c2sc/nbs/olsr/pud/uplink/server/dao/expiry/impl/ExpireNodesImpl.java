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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry.impl;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry.ExpireNodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util.TxChecker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expire out-of-date PositionUpdate and ClusterLeader messages, and then remove empty Nodes and Senders. Do NOT remove
 * empty RelayServers since these are statically configured
 */
@Repository
public class ExpireNodesImpl implements ExpireNodes {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/** the multiplier for the validity time */
	private double validityTimeMultiplier = 3.0;

	/**
	 * @param validityTimeMultiplier
	 *          the validityTimeMultiplier to set
	 */
	public void setValidityTimeMultiplier(double validityTimeMultiplier) {
		this.validityTimeMultiplier = validityTimeMultiplier;
	}

	/** the Node DAO */
	private Nodes nodes;

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	@Required
	public void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the PositionUpdateMsg DAO */
	private PositionUpdateMsgs positionUpdateMsgs;

	/**
	 * @param positionUpdateMsgs
	 *          the positionUpdateMsgs to set
	 */
	@Required
	public void setPositions(PositionUpdateMsgs positionUpdateMsgs) {
		this.positionUpdateMsgs = positionUpdateMsgs;
	}

	/** the ClusterLeaderMsg DAO */
	private ClusterLeaderMsgs clusterLeaderMsgs;

	/**
	 * @param clusterLeaderMsgs
	 *          the clusterLeaderMsgs to set
	 */
	@Required
	public void setClusterLeaderMsgs(ClusterLeaderMsgs clusterLeaderMsgs) {
		this.clusterLeaderMsgs = clusterLeaderMsgs;
	}

	/** the Sender DAO */
	private Senders senders;

	/**
	 * @param senders
	 *          the senders to set
	 */
	@Required
	public void setSenders(Senders senders) {
		this.senders = senders;
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
	public void expire() {
		try {
			this.txChecker.checkInTx("ExpireNodes::expire");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("************************** expiry");
		}

		long utcTimestamp = System.currentTimeMillis();

		try {
			this.clusterLeaderMsgs.removeExpiredClusterLeaderMsg(utcTimestamp, this.validityTimeMultiplier);
		} catch (Throwable e) {
			this.logger.error("Removal of expired cluster leader messages failed", e);
		}

		try {
			this.positionUpdateMsgs.removeExpiredPositionUpdateMsg(utcTimestamp, this.validityTimeMultiplier);
		} catch (Throwable e) {
			this.logger.error("Removal of expired position update messages failed", e);
		}

		try {
			this.nodes.removeExpiredNodes();
		} catch (Throwable e) {
			this.logger.error("Removal of empty nodes failed", e);
		}

		try {
			this.senders.removeExpiredSenders();
		} catch (Throwable e) {
			this.logger.error("Removal of empty senders failed", e);
		}
	}
}
