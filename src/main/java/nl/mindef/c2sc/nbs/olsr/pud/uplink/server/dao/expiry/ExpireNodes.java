package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Positions;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class ExpireNodes {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private ReentrantLock dataLock;

	/**
	 * @param dataLock
	 *            the dataLock to set
	 */
	@Required
	public final void setDataLock(ReentrantLock dataLock) {
		this.dataLock = dataLock;
	}

	protected class ExpiryTimerTask extends TimerTask {
		@Override
		public void run() {
			dataLock.lock();

			if (logger.isDebugEnabled()) {
				logger.debug("************************** expiry");
			}

			try {
				try {
					nodes.removeExpiredNodes(validityTimeMultiplier);
				} catch (Throwable e) {
					/* swallow */
					e.printStackTrace();
				}

				try {
					positions.removeExpiredNodePosition(validityTimeMultiplier);
				} catch (Throwable e) {
					/* swallow */
					e.printStackTrace();
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
	public final long getInterval() {
		return interval;
	}

	/**
	 * @param interval
	 *            the interval to set
	 */
	@Required
	public final void setInterval(long interval) {
		this.interval = interval;
	}

	/** the default multiplier for the validity time */
	public static final double validityTimeMultiplier_default = 3.0;

	/** the multiplier for the validity time */
	private double validityTimeMultiplier = validityTimeMultiplier_default;

	/**
	 * @param validityTimeMultiplier
	 *            the validityTimeMultiplier to set
	 */
	public final void setValidityTimeMultiplier(double validityTimeMultiplier) {
		this.validityTimeMultiplier = validityTimeMultiplier;
	}

	private Nodes nodes;

	/**
	 * @return the nodes
	 */
	public final Nodes getNodes() {
		return nodes;
	}

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	@Required
	public final void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the Positions handler */
	private Positions positions;

	/**
	 * @param positions
	 *            the positions to set
	 */
	@Required
	public final void setPositions(Positions positions) {
		this.positions = positions;
	}

	private Timer timer;

	public void init() {
		if (interval <= 0) {
			return;
		}

		timer = new Timer(this.getClass().getSimpleName());
		timer.scheduleAtFixedRate(new ExpiryTimerTask(), interval, interval);
	}

	public void destroy() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
