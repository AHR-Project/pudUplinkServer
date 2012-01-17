package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;

import org.olsr.plugin.pud.ClusterLeader;

public interface ClusterLeaderHandler {
	/**
	 * Process a ClusterLeader message and save it into the database
	 * 
	 * @param sender
	 *          the sender from which the message was received
	 * @param utcTimestamp
	 *          the timestamp on which the message was received
	 * @param clMsg
	 *          the ClusterLeader message
	 * @return true when the data in the message resulted in a database update
	 */
	public boolean handleClusterLeaderMessage(Sender sender, long utcTimestamp, ClusterLeader clMsg);
}
