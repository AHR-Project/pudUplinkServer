package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.cluster;

import java.net.DatagramPacket;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

public interface RelayClusterSender {
	/**
	 * Schedule a packet for distribution to other relay servers
	 * 
	 * @param relayServer
	 *          the relay server from which the packet was received
	 * @param packet
	 *          the packet
	 * @return true when the packet was scheduled for distribution, false otherwise
	 */
	public boolean addPacket(RelayServer relayServer, DatagramPacket packet);
}
