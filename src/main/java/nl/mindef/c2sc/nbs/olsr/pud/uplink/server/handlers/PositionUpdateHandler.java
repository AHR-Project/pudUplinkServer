package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;

import org.olsr.plugin.pud.PositionUpdate;

public interface PositionUpdateHandler {
	/**
	 * Process a PositionUpdate message and save it into the database
	 * 
	 * @param gateway
	 *          the gateway from which the message was received
	 * @param utcTimestamp
	 *          the timestamp on which the message was received
	 * @param puMsg
	 *          the PositionUpdate message
	 * @return true when the data in the message resulted in a database update
	 */
	public boolean handlePositionMessage(Gateway gateway, long utcTimestamp, PositionUpdate puMsg);
}
