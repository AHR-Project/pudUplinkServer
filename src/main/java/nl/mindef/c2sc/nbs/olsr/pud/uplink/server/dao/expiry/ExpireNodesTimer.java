package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry;

import java.util.Timer;
import java.util.TimerTask;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class ExpireNodesTimer implements StopHandlerConsumer {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private ExpireNodes expireNodes;

	/**
	 * @param expireNodes
	 *          the expireNodes to set
	 */
	@Required
	public final void setExpireNodes(ExpireNodes expireNodes) {
		this.expireNodes = expireNodes;
	}

	/** the expiry interval (millseconds) */
	private long interval;

	/**
	 * @param interval
	 *          the interval to set
	 */
	@Required
	public void setInterval(long interval) {
		this.interval = interval;
	}

	/**
	 * Timer task that does the actual expiry of out-of-date and empty objects
	 */
	private class ExpiryTimerTask extends TimerTask {
		@SuppressWarnings("hiding")
		private Logger logger;

		@SuppressWarnings("hiding")
		private ExpireNodes expireNodes;

		/**
		 * @param logger
		 *          the logger
		 * @param expireNodes
		 *          the expireNodes
		 */
		public ExpiryTimerTask(Logger logger, ExpireNodes expireNodes) {
			super();
			this.logger = logger;
			this.expireNodes = expireNodes;
		}

		@Override
		public void run() {
			try {
				this.expireNodes.expire();
			} catch (Throwable e) {
				this.logger.error("error during expiry", e);
			}
		}
	}

	/*
	 * Main
	 */

	/** the timer from which the expiry task runs */
	private Timer timer = null;

	public void init() {
		if (this.interval <= 0) {
			return;
		}

		this.timer = new Timer(this.getClass().getSimpleName());
		this.timer.scheduleAtFixedRate(new ExpiryTimerTask(this.logger, this.expireNodes), 0, this.interval);
	}

	public void uninit() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
	}

	@Override
	public void signalStop() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
	}
}
