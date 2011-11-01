package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

import org.olsr.plugin.pud.PositionUpdate;

public interface PositionUpdateHandler {
	public boolean handlePositionMessage(InetAddress srcIp, long utcTimestamp,
			PositionUpdate posUpMsg, RelayServer relayServer);
}
