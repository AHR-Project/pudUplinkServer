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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class ExpireNodesTimer {
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

	public void init() {
		if (this.interval <= 0) {
			return;
		}

		this.timer.scheduleAtFixedRate(new ExpiryTimerTask(this.logger, this.expireNodes), 0, this.interval);
	}
}
