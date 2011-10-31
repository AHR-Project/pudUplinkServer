package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import java.net.DatagramPacket;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

public interface PacketHandler {
	public boolean processPacket(DatagramPacket packet, RelayServer relayServer);
}
