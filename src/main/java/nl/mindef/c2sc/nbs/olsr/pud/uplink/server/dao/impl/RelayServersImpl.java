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
	public final void setUplinkUdpPort(int uplinkUdpPort) {
		this.uplinkUdpPort = uplinkUdpPort;
	}

	@Override
	@Transactional
	public List<RelayServer> getRelayServers() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = sessionFactory.getCurrentSession().createQuery("select rs from RelayServer rs").list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	/**
	 * Retrieve a RelayServer object from the database from an unsaved RelayServer object
	 * 
	 * @param relayServer
	 *          the unsaved RelayServer object
	 * @return the RelayServer object from the database, or null when not found
	 */
	@Transactional
	private RelayServer getRelayServer(RelayServer relayServer) {
		if (relayServer == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<RelayServer> result = sessionFactory.getCurrentSession()
				.createQuery("select rs from RelayServer rs where rs.ip = :ip and rs.port = :port")
				.setParameter("ip", relayServer.getIp()).setParameter("port", relayServer.getPort()).list();

		if (result.size() == 0) {
			return null;
		}

		assert (result.size() == 1);

		return result.get(0);
	}

	@Override
	@Transactional
	public List<RelayServer> getOtherRelayServers() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = sessionFactory.getCurrentSession()
				.createQuery("select rs from RelayServer rs" + " where rs.id != :meId").setParameter("meId", getMe().getId())
				.list();

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	@Transactional
	public void addRelayServer(RelayServer relayServer, boolean newObject) {
		assert (getRelayServer(relayServer) == null);
		if (newObject) {
			sessionFactory.getCurrentSession().saveOrUpdate(relayServer);
		} else {
			sessionFactory.getCurrentSession().merge(relayServer);
		}
		// FIXME remove all session flushes
	}

	private String getRelayServersDump() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = sessionFactory.getCurrentSession().createQuery("from RelayServer rs").list();

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
		List<RelayServer> result = sessionFactory.getCurrentSession()
				.createQuery("select rs from RelayServer rs where rs.ip = :ip and rs.port = :port").setParameter("ip", myIp)
				.setParameter("port", uplinkUdpPort).list();

		if (result.size() == 0) {
			RelayServer me = new RelayServer(myIp, uplinkUdpPort);
			addRelayServer(me, true);
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
	@Transactional
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			logger.log(level, getRelayServersDump());
		}
	}

	@Override
	@Transactional
	public void print(OutputStream out) throws IOException {
		String s = getRelayServersDump();
		out.write(s.getBytes(), 0, s.length());
	}
}
