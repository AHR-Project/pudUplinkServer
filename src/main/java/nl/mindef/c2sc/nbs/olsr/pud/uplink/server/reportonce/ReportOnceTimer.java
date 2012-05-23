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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class ReportOnceTimer {
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

	/** the timer from which the expiry task runs */
	private Timer timer;

	/**
	 * @param timer
	 *          the timer to set
	 */
	@Required
	public final void setTimer(Timer timer) {
		this.timer = timer;
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
				this.reportOnce.flush(this.reportSubject, null);
			} catch (Throwable e) {
				this.logger.error("error report-once flushing of subject " + this.reportSubject, e);
			}
		}
	}

	/*
	 * Main
	 */

	public void init() {
		if ((this.intervalWireFormat <= 0) && (this.intervalDuplicateNames <= 0)) {
			return;
		}

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
		this.reportOnce.flush(null, null);
	}
}
