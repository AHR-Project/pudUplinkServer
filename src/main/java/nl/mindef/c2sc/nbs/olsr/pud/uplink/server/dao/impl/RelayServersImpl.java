package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RelayServersImpl implements RelayServers {
	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *          the sessionFactory to set
	 */
	@Required
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/** the UDP port to listen on for uplink messages */
	private Integer uplinkUdpPort = null;

	/**
	 * @param uplinkUdpPort
	 *          the uplinkUdpPort to set
	 */
	@Required
	public final void setUplinkUdpPort(Integer uplinkUdpPort) {
		this.uplinkUdpPort = uplinkUdpPort;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RelayServer> getRelayServers() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = this.sessionFactory.getCurrentSession().createQuery("select rs from RelayServer rs")
				.list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RelayServer> getOtherRelayServers() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = this.sessionFactory.getCurrentSession()
				.createQuery("select rs from RelayServer rs where rs.id != " + getMe().getId()).list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional
	public void addRelayServer(RelayServer relayServer) {
		this.sessionFactory.getCurrentSession().saveOrUpdate(relayServer);
	}

	private String getRelayServersDump() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = this.sessionFactory.getCurrentSession().createQuery("from RelayServer rs").list();

		StringBuilder s = new StringBuilder();
		s.append("[RelayServers]\n");
		for (RelayServer rs : result) {
			s.append(rs.toString() + "\n");
		}

		return s.toString();
	}

	@Override
	@Transactional
	public RelayServer getMe() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = this.sessionFactory.getCurrentSession()
				.createQuery("select rs from RelayServer rs where rs.ip = :ip and rs.port = " + this.uplinkUdpPort)
				.setParameter("ip", myIp).list();

		if (result.size() == 0) {
			RelayServer me = new RelayServer(myIp, this.uplinkUdpPort);
			addRelayServer(me);
			return me;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	private static InetAddress myIp = null;

	static {
		try {
			myIp = InetAddress.getByName("localhost");
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getRelayServersDump());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void print(OutputStream out) throws IOException {
		String s = getRelayServersDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
