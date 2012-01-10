package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;

import org.olsr.plugin.pud.ClusterLeader;

public interface ClusterLeaderHandler {
	public boolean handleClusterLeaderMessage(Gateway gateway, long timestamp, ClusterLeader clUpMsg);
}
