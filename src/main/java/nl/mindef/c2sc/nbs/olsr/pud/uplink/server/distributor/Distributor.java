package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

public interface Distributor {
	public RelayServer getMe();

	public void signalUpdate();
}
