package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import java.net.DatagramPacket;

public interface PacketHandler {
	public boolean processPacket(DatagramPacket packet);
}
