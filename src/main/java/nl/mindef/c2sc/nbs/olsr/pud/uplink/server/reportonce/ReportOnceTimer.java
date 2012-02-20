package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce;

import java.util.Timer;
import java.util.TimerTask;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class ReportOnceTimer implements StopHandlerConsumer {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private ReportOnce reportOnce;

	/**
	 * @param reportOnce
	 *          the reportOnce to set
	 */
	@Required
	public final void setReportOnce(ReportOnce reportOnce) {
		this.reportOnce = reportOnce;
	}

	/** the expiry interval for wireformat (millseconds) */
	private long intervalWireFormat;

	/**
	 * @param intervalWireFormat
	 *          the intervalWireFormat to set
	 */
	@Required
	public void setIntervalWireFormat(long intervalWireFormat) {
		this.intervalWireFormat = intervalWireFormat;
	}

	/** the expiry interval for duplicate names (millseconds) */
	private long intervalDuplicateNames;

	/**
	 * @param intervalDuplicateNames
	 *          the intervalDuplicateNames to set
	 */
	@Required
	public void setIntervalDuplicateNames(long intervalDuplicateNames) {
		this.intervalDuplicateNames = intervalDuplicateNames;
	}

	/**
	 * Timer task that does the actual expiry of out-of-date and empty objects
	 */
	private class ExpiryTimerTask extends TimerTask {
		@SuppressWarnings("hiding")
		private Logger logger;

		@SuppressWarnings("hiding")
		private ReportOnce reportOnce;

		private ReportSubject reportSubject;

		/**
		 * @param logger
		 *          the logger
		 * @param reportOnce
		 *          the database logger
		 * @param reportSubject
		 *          the report subject
		 */
		public ExpiryTimerTask(Logger logger, ReportOnce reportOnce, ReportSubject reportSubject) {
			super();
			this.logger = logger;
			this.reportOnce = reportOnce;
			this.reportSubject = reportSubject;
		}

		@Override
		public void run() {
			try {
				this.reportOnce.flush(this.reportSubject);
			} catch (Throwable e) {
				this.logger.error("error report-once flushing of subject " + this.reportSubject, e);
			}
		}
	}

	/*
	 * Main
	 */

	/** the timer from which the expiry task runs */
	private Timer timer = null;

	public void init() {
		if ((this.intervalWireFormat <= 0) && (this.intervalDuplicateNames <= 0)) {
			return;
		}

		this.timer = new Timer(this.getClass().getSimpleName());

		if (this.intervalWireFormat > 0) {
			this.timer.scheduleAtFixedRate(
					new ExpiryTimerTask(this.logger, this.reportOnce, ReportSubject.SENDER_WIRE_FORMAT), this.intervalWireFormat,
					this.intervalWireFormat);
		}
		if (this.intervalDuplicateNames > 0) {
			this.timer.scheduleAtFixedRate(new ExpiryTimerTask(this.logger, this.reportOnce,
					ReportSubject.DUPLICATE_NODE_NAME), this.intervalDuplicateNames, this.intervalDuplicateNames);
		}
	}

	public void uninit() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
		this.reportOnce.flush(null);
	}

	@Override
	public void signalStop() {
		uninit();
	}
}
