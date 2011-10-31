package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public interface RelayServers {
	public List<RelayServer> getRelayServers();

	public List<RelayServer> getOtherRelayServers();

	public void addRelayServer(RelayServer relayServer);

	public void log(Logger logger, Level level);
}
