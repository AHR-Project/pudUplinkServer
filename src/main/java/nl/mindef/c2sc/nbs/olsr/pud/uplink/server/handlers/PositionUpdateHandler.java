package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;

import org.olsr.plugin.pud.PositionUpdate;

public interface PositionUpdateHandler {
	public boolean handlePositionMessage(Gateway gateway, long utcTimestamp, PositionUpdate posUpMsg);
}
