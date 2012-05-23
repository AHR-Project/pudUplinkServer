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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util.TxChecker;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ClusterLeaderMsgsImpl implements ClusterLeaderMsgs {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *          the sessionFactory to set
	 */
	@Required
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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
	public void saveClusterLeaderMsg(ClusterLeaderMsg clusterLeaderMsg) {
		try {
			this.txChecker.checkInTx("ClusterLeaderMsgs::saveClusterLeaderMsg");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		this.sessionFactory.getCurrentSession().saveOrUpdate(clusterLeaderMsg);
	}

	@Override
	@Transactional
	public boolean removeExpiredClusterLeaderMsg(long utcTimestamp, double validityTimeMultiplier) {
		try {
			this.txChecker.checkInTx("ClusterLeaderMsgs::removeExpiredClusterLeaderMsg");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		List<ClusterLeaderMsg> result = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"select cl from ClusterLeaderMsg cl where"
								+ " (receptionTime + (validityTime * :validityTimeMultiplier)) < :utcTimestamp")
				.setDouble("validityTimeMultiplier", validityTimeMultiplier).setLong("utcTimestamp", utcTimestamp).list();

		if (result.size() == 0) {
			return false;
		}

		for (ClusterLeaderMsg cl : result) {
			cl.getNode().setClusterLeaderMsg(null);
			cl.setClusterLeaderNode(null);
			this.sessionFactory.getCurrentSession().delete(cl);
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("removed " + result.size() + " ClusterLeaderMsg objects");
		}

		return true;
	}

	private String getClusterLeaderMsgsDump() {
		@SuppressWarnings("unchecked")
		List<ClusterLeaderMsg> result = this.sessionFactory.getCurrentSession()
				.createQuery("from ClusterLeaderMsg clusterLeaderMsg").list();

		StringBuilder s = new StringBuilder();
		s.append("[ClusterLeaderMsgs]\n");
		for (ClusterLeaderMsg clusterLeaderMsg : result) {
			s.append(clusterLeaderMsg.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional(readOnly = true)
	public void log(Logger log, Level level) {
		try {
			this.txChecker.checkInTx("ClusterLeaderMsgs::log");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (log.isEnabledFor(level)) {
			log.log(level, getClusterLeaderMsgsDump());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void print(OutputStream out) throws IOException {
		try {
			this.txChecker.checkInTx("ClusterLeaderMsgs::print");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		String s = getClusterLeaderMsgsDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
