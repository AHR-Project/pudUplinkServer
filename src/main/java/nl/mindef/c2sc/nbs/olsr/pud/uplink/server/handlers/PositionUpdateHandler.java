package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.RelayServer;

import org.olsr.plugin.pud.PositionUpdate;

public interface PositionUpdateHandler {
	public boolean handlePositionMessage(long utcTimestamp,
			PositionUpdate posUpMsg, RelayServer relayServer);
}
