package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.relaycluster;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.RelayServer;

public interface RelayCluster {
	public RelayServer getMe();

	public void signalUpdate();
}
