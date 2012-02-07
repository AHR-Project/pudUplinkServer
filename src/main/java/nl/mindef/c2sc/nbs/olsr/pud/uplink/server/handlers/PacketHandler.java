package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import java.net.DatagramPacket;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

public interface PacketHandler {
	/**
	 * Process a packet that is received on the uplink
	 * 
	 * @param relayServer
	 *          the RelayServer from which the packet was received
	 * @param packet
	 *          the packet to process
	 * @return true when the data in the packet resulted in a database update
	 */
	public boolean processPacket(RelayServer relayServer, DatagramPacket packet);
}
