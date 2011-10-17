package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.impl;

import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.relaycluster.RelayCluster;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

public class RelayServersImpl implements RelayServers {
	private RelayCluster relayCluster;

	/**
	 * @param relayCluster
	 *            the relayCluster to set
	 */
	@Required
	public final void setRelayCluster(RelayCluster relayCluster) {
		this.relayCluster = relayCluster;
	}

	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *            the sessionFactory to set
	 */
	@Required
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional
	public List<RelayServer> getRelayServers() {
		@SuppressWarnings("unchecked")
		List<RelayServer> result = sessionFactory.getCurrentSession()
				.createQuery("select rs from RelayServer rs").list();

		return result;
	}

	@Transactional
	private RelayServer getRelayServer(RelayServer relayServer) {
		if (relayServer == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		List<RelayServer> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select rs from RelayServer rs where rs.ip = :par1"
								+ " and rs.port = :par2")
				.setParameter("par1", relayServer.getIp())
				.setParameter("par2", relayServer.getPort()).list();

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
		List<RelayServer> result = sessionFactory
				.getCurrentSession()
				.createQuery(
						"select rs from RelayServer rs" + " where rs.id != "
								+ relayCluster.getMe().getId()).list();

		return result;
	}

	@Transactional
	private void saveRelayServer(RelayServer relayServer, boolean newObject) {
		if (newObject) {
			sessionFactory.getCurrentSession().saveOrUpdate(relayServer);
		} else {
			sessionFactory.getCurrentSession().merge(relayServer);
		}
	}

	@Override
	@Transactional
	public void addRelayServer(RelayServer relayServer) {
		assert (getRelayServer(relayServer) == null);
		saveRelayServer(relayServer, true);
	}

	@Override
	@Transactional
	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			@SuppressWarnings("unchecked")
			List<RelayServer> result = sessionFactory.getCurrentSession()
					.createQuery("from RelayServer rs").list();

			if (result.size() == 0) {
				return;
			}

			StringBuilder s = new StringBuilder();
			s.append("*** RelayServers ***\n");
			for (RelayServer rs : result) {
				s.append(rs.toString());
			}
			logger.log(level, s.toString());
		}
	}
}