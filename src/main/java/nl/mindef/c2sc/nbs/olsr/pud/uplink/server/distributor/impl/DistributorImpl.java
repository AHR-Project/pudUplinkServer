package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.impl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.Distributor;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.DistributorWorker;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DistributorImpl implements Distributor, StopHandlerConsumer {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private DistributorWorker distributorWorker;

	/**
	 * @param distributorWorker
	 *          the distributorWorker to set
	 */
	@Required
	public final void setDistributorWorker(DistributorWorker distributorWorker) {
		this.distributorWorker = distributorWorker;
	}

	private long distributionDelay = 1000;

	/**
	 * @param distributionDelay
	 *          the distributionDelay to set
	 */
	public final void setDistributionDelay(long distributionDelay) {
		this.distributionDelay = distributionDelay;
	}

	protected class DistributionTimerTask extends TimerTask {
		@SuppressWarnings("hiding")
		private Logger logger;

		@SuppressWarnings("hiding")
		private DistributorWorker distributorWorker;

		@SuppressWarnings("hiding")
		private AtomicInteger signaledUpdates;

		/**
		 * @param logger
		 *          the logger
		 * @param distributorWorker
		 *          the distributorWorker
		 * @param signaledUpdates
		 *          the updates counter
		 */
		public DistributionTimerTask(Logger logger, DistributorWorker distributorWorker, AtomicInteger signaledUpdates) {
			super();
			this.logger = logger;
			this.distributorWorker = distributorWorker;
			this.signaledUpdates = signaledUpdates;
		}

		@Override
		public void run() {
			try {
				this.distributorWorker.distribute(this.signaledUpdates);
			} catch (Throwable e) {
				this.logger.error("error during distribution", e);
			}
		}
	}

	public void init() {
		this.timer = new Timer(this.getClass().getSimpleName() + "-Timer");
	}

	public void uninit() {
		signalStop();
	}

	@Override
	public void signalStop() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
	}

	private Timer timer = null;
	private AtomicInteger signaledUpdates = new AtomicInteger(0);

	@Override
	public void signalUpdate() {
		int previousSignaledUpdates = this.signaledUpdates.getAndIncrement();
		if (previousSignaledUpdates <= 0) {
			this.timer.schedule(new DistributionTimerTask(this.logger, this.distributorWorker, this.signaledUpdates),
					this.distributionDelay);
		}
	}
}
