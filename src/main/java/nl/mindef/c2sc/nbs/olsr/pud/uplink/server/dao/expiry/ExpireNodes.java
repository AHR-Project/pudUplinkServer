package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Gateways;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Expire out-of-date PositionUpdate and ClusterLeader messages, and then remove empty Nodes and Gateways. Do NOT remove
 * empty RelayServers since these are statically configured
 */
public class ExpireNodes {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	// FIXME remove the data lock
	/** the data lock used to serialize access to the database */
	private ReentrantLock dataLock;

	/**
	 * @param dataLock
	 *          the dataLock to set
	 */
	@Required
	public void setDataLock(ReentrantLock dataLock) {
		this.dataLock = dataLock;
	}

	/**
	 * Timer task that does the actual expiry of out-of-date and empty objects
	 */
	protected class ExpiryTimerTask extends TimerTask {
		@Override
		public void run() {
			dataLock.lock();
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("************************** expiry");
				}

				long utcTimestamp = System.currentTimeMillis();

				try {
					clusterLeaderMsgs.removeExpiredClusterLeaderMsg(utcTimestamp, validityTimeMultiplier);
				} catch (Throwable e) {
					logger.error("Removal of expired cluster leader messages failed", e);
				}

				try {
					positionUpdateMsgs.removeExpiredPositionUpdateMsg(utcTimestamp, validityTimeMultiplier);
				} catch (Throwable e) {
					logger.error("Removal of expired position update messages failed", e);
				}

				try {
					nodes.removeExpiredNodes();
				} catch (Throwable e) {
					logger.error("Removal of empty nodes failed", e);
				}

				try {
					gateways.removeExpiredGateways();
				} catch (Throwable e) {
					logger.error("Removal of empty gateways failed", e);
				}
			} finally {
				dataLock.unlock();
			}
		}
	}

	/** the expiry interval (millseconds) */
	private long interval;

	/**
	 * @return the interval
	 */
	public long getInterval() {
		return interval;
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

	/** the Gateway DAO */
	private Gateways gateways;

	/**
	 * @param gateways
	 *          the gateways to set
	 */
	@Required
	public void setGateways(Gateways gateways) {
		this.gateways = gateways;
	}

	/** the timer from which the expiry task runs */
	private Timer timer;

	public void init() {
		if (interval <= 0) {
			return;
		}

		timer = new Timer(this.getClass().getSimpleName() + "-Timer");
		timer.scheduleAtFixedRate(new ExpiryTimerTask(), interval, interval);
	}

	public void destroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
