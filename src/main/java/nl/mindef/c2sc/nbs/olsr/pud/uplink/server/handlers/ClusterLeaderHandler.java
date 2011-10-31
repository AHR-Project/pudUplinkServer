package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

import org.olsr.plugin.pud.ClusterLeader;

public interface ClusterLeaderHandler {
	public boolean handleClusterLeaderMessage(long timestamp,
			ClusterLeader clUpMsg, RelayServer relayServer);
}
