/**
 * 
 */
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Gateways;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class GatewaysImpl implements Gateways {
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

	@Override
	@Transactional
	public List<Gateway> getGateways(InetAddress ip) {
		if (ip == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Gateway> result = sessionFactory.getCurrentSession()
				.createQuery("select gw from Gateway gw where gw.ip = :ip").setParameter("ip", ip).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result;
	}

	@Override
	@Transactional
	public Gateway getGateway(InetAddress ip, int port) {
		if (ip == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Gateway> result = sessionFactory.getCurrentSession()
				.createQuery("select gw from Gateway gw where gw.ip = :ip and gw.port = " + port).setParameter("ip", ip).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	// TODO this is duplicated, move it to a helper
	@Override
	@Transactional
	public void saveGateway(Gateway gateway, boolean newObject) {
		if (newObject) {
			sessionFactory.getCurrentSession().saveOrUpdate(gateway);
		} else {
			sessionFactory.getCurrentSession().merge(gateway);
		}
	}

	@Override
	@Transactional
	public boolean removeExpiredGateways() {
		@SuppressWarnings("unchecked")
		List<Gateway> result = sessionFactory.getCurrentSession()
				.createQuery("select gw from Gateway gw where size(nodes) = 0").list();

		if (result.size() == 0) {
			return false;
		}

		for (Gateway gw : result) {
			gw.setRelayServer(null);
			sessionFactory.getCurrentSession().delete(gw);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("removed " + result.size() + " Gateway objects");
		}

		return true;
	}

	private String getGatewaysDump() {
		@SuppressWarnings("unchecked")
		List<Gateway> result = sessionFactory.getCurrentSession().createQuery("from Gateway gw").list();

		StringBuilder s = new StringBuilder();
		s.append("[Gateways]\n");
		for (Gateway gw : result) {
			s.append(gw.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getGatewaysDump());
		}
	}

	@Override
	@Transactional
	public void print(OutputStream out) throws IOException {
		String s = getGatewaysDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
