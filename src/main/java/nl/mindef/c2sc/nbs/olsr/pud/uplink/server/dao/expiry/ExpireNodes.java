package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry;

import java.util.Timer;
import java.util.TimerTask;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expire out-of-date PositionUpdate and ClusterLeader messages, and then remove empty Nodes and Senders. Do NOT remove
 * empty RelayServers since these are statically configured
 */
@Repository
public class ExpireNodes {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Timer task that does the actual expiry of out-of-date and empty objects
	 */
	protected class ExpiryTimerTask extends TimerTask {
		@Override
		public void run() {
			expire();
		}
	}

	@Transactional
	public void expire() {
		try {
			if (ExpireNodes.this.logger.isDebugEnabled()) {
				ExpireNodes.this.logger.debug("************************** expiry");
			}

			long utcTimestamp = System.currentTimeMillis();

			try {
				ExpireNodes.this.clusterLeaderMsgs.removeExpiredClusterLeaderMsg(utcTimestamp,
						ExpireNodes.this.validityTimeMultiplier);
			} catch (Throwable e) {
				ExpireNodes.this.logger.error("Removal of expired cluster leader messages failed", e);
			}

			try {
				ExpireNodes.this.positionUpdateMsgs.removeExpiredPositionUpdateMsg(utcTimestamp,
						ExpireNodes.this.validityTimeMultiplier);
			} catch (Throwable e) {
				ExpireNodes.this.logger.error("Removal of expired position update messages failed", e);
			}

			try {
				ExpireNodes.this.nodes.removeExpiredNodes();
			} catch (Throwable e) {
				ExpireNodes.this.logger.error("Removal of empty nodes failed", e);
			}

			try {
				ExpireNodes.this.senders.removeExpiredSenders();
			} catch (Throwable e) {
				ExpireNodes.this.logger.error("Removal of empty senders failed", e);
			}
		} catch (Throwable e) {
			ExpireNodes.this.logger.error("error during expiry", e);
		}
	}

	/** the expiry interval (millseconds) */
	private long interval;

	/**
	 * @return the interval
	 */
	public long getInterval() {
		return this.interval;
	}

	/**
	 * @param interval
	 *          the interval to set
	 */
	@Required
	public void setInterval(long interval) {
		this.interval = interval;
	}

	/** the multiplier for the validity time */
	double validityTimeMultiplier = 3.0;

	/**
	 * @param validityTimeMultiplier
	 *          the validityTimeMultiplier to set
	 */
	public void setValidityTimeMultiplier(double validityTimeMultiplier) {
		this.validityTimeMultiplier = validityTimeMultiplier;
	}

	/** the Node DAO */
	Nodes nodes;

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	@Required
	public void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the PositionUpdateMsg DAO */
	PositionUpdateMsgs positionUpdateMsgs;

	/**
	 * @param positionUpdateMsgs
	 *          the positionUpdateMsgs to set
	 */
	@Required
	public void setPositions(PositionUpdateMsgs positionUpdateMsgs) {
		this.positionUpdateMsgs = positionUpdateMsgs;
	}

	/** the ClusterLeaderMsg DAO */
	ClusterLeaderMsgs clusterLeaderMsgs;

	/**
	 * @param clusterLeaderMsgs
	 *          the clusterLeaderMsgs to set
	 */
	@Required
	public void setClusterLeaderMsgs(ClusterLeaderMsgs clusterLeaderMsgs) {
		this.clusterLeaderMsgs = clusterLeaderMsgs;
	}

	/** the Sender DAO */
	Senders senders;

	/**
	 * @param senders
	 *          the senders to set
	 */
	@Required
	public void setSenders(Senders senders) {
		this.senders = senders;
	}

	/** the timer from which the expiry task runs */
	private Timer timer = null;

	public void init() {
		if (this.interval <= 0) {
			return;
		}

		this.timer = new Timer(this.getClass().getSimpleName() + "-Timer");
		this.timer.scheduleAtFixedRate(new ExpiryTimerTask(), this.interval, this.interval);
	}

	public void destroy() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
	}
}
