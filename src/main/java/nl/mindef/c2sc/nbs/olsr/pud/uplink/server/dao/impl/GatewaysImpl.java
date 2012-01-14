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
	@Transactional(readOnly = true)
	public List<Gateway> getGateways(InetAddress ip) {
		if (ip == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Gateway> result = this.sessionFactory.getCurrentSession()
				.createQuery("select gw from Gateway gw where gw.ip = :ip").setParameter("ip", ip).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public Gateway getGateway(InetAddress ip, int port) {
		if (ip == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<Gateway> result = this.sessionFactory.getCurrentSession()
				.createQuery("select gw from Gateway gw where gw.ip = :ip and gw.port = " + port).setParameter("ip", ip).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional
	public void saveGateway(Gateway gateway) {
		this.sessionFactory.getCurrentSession().saveOrUpdate(gateway);
	}

	@Override
	@Transactional
	public boolean removeExpiredGateways() {
		@SuppressWarnings("unchecked")
		List<Gateway> result = this.sessionFactory.getCurrentSession()
				.createQuery("select gw from Gateway gw where size(nodes) = 0").list();

		if (result.size() == 0) {
			return false;
		}

		for (Gateway gw : result) {
			gw.setRelayServer(null);
			this.sessionFactory.getCurrentSession().delete(gw);
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("removed " + result.size() + " Gateway objects");
		}

		return true;
	}

	private String getGatewaysDump() {
		@SuppressWarnings("unchecked")
		List<Gateway> result = this.sessionFactory.getCurrentSession().createQuery("from Gateway gw").list();

		StringBuilder s = new StringBuilder();
		s.append("[Gateways]\n");
		for (Gateway gw : result) {
			s.append(gw.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional(readOnly = true)
	public void log(Logger log, Level level) {
		if (log.isEnabledFor(level)) {
			log.log(level, getGatewaysDump());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void print(OutputStream out) throws IOException {
		String s = getGatewaysDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
