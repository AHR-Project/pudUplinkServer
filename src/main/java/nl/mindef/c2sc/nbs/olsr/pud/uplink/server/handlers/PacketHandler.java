package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import java.net.DatagramPacket;

public interface PacketHandler {
	/**
	 * Process a packet that is received on the uplink
	 * 
	 * @param packet
	 *          the packet to process
	 * @return true when the data in the packet resulted in a database update
	 */
	public boolean processPacket(DatagramPacket packet);
}
