/**
 * 
 */
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util.TxChecker;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SendersImpl implements Senders {
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
	@Transactional(readOnly = true)
	public Sender getSender(InetAddress ip, int port) {
		try {
			this.txChecker.checkInTx("Senders::getSender");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (ip == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Sender> result = this.sessionFactory.getCurrentSession()
				.createQuery("select gw from Sender gw where gw.ip = :ip and gw.port = :port").setParameter("ip", ip)
				.setInteger("port", port).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional
	public void saveSender(Sender sender) {
		try {
			this.txChecker.checkInTx("Senders::saveSender");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		this.sessionFactory.getCurrentSession().saveOrUpdate(sender);
	}

	@Override
	@Transactional
	public boolean removeExpiredSenders() {
		try {
			this.txChecker.checkInTx("Senders::removeExpiredSenders");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		List<Sender> result = this.sessionFactory.getCurrentSession()
				.createQuery("select sender from Sender sender where size(nodes) = 0").list();

		if (result.size() == 0) {
			return false;
		}

		for (Sender gw : result) {
			gw.setRelayServer(null);
			this.sessionFactory.getCurrentSession().delete(gw);
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("removed " + result.size() + " Sender objects");
		}

		return true;
	}

	private String getSendersDump() {
		@SuppressWarnings("unchecked")
		List<Sender> result = this.sessionFactory.getCurrentSession().createQuery("from Sender gw").list();

		StringBuilder s = new StringBuilder();
		s.append("[Senders]\n");
		for (Sender gw : result) {
			s.append(gw.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional(readOnly = true)
	public void log(Logger log, Level level) {
		try {
			this.txChecker.checkInTx("Senders::log");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (log.isEnabledFor(level)) {
			log.log(level, getSendersDump());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void print(OutputStream out) throws IOException {
		try {
			this.txChecker.checkInTx("Senders::print");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		String s = getSendersDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
