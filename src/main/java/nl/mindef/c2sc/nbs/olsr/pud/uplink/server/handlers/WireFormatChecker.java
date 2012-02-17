package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;

import org.olsr.plugin.pud.UplinkMessage;

public interface WireFormatChecker {
	/**
	 * Checks whether whether the uplink message can be processed (whether the wire format version of the uplink message
	 * is one that is compatible with the RelayServer)
	 * 
	 * @param sender
	 *          the sender of the uplink message
	 * @param msg
	 *          the uplink message
	 * @return true when the uplink message can be processed (wireformat matches expected version)
	 */
	public boolean checkUplinkMessageWireFormat(Sender sender, UplinkMessage msg);
}
