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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DatabaseLoggerTimer {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private DatabaseLogger databaseLogger;

	/**
	 * @param databaseLogger
	 *          the databaseLogger to set
	 */
	@Required
	public final void setDatabaseLogger(DatabaseLogger databaseLogger) {
		this.databaseLogger = databaseLogger;
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
	private static class ExpiryTimerTask extends TimerTask {
		private Logger logger;

		private DatabaseLogger databaseLogger;

		/**
		 * @param logger
		 *          the logger
		 * @param databaseLogger
		 *          the database logger
		 */
		public ExpiryTimerTask(Logger logger, DatabaseLogger databaseLogger) {
			super();
			this.logger = logger;
			this.databaseLogger = databaseLogger;
		}

		@Override
		public void run() {
			try {
				this.databaseLogger.logit();
			} catch (Throwable e) {
				this.logger.error("error during database logging", e);
			}
		}
	}

	/*
	 * Main
	 */

	public void init() {
		if (this.interval <= 0) {
			return;
		}

		this.timer.scheduleAtFixedRate(new ExpiryTimerTask(this.logger, this.databaseLogger), 0, this.interval);
	}
}
