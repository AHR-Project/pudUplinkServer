package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The ClusterLeaderMsg DAO
 */
public interface ClusterLeaderMsgs {
	/**
	 * Retrieve the ClusterLeaderMsg as sent by an OLSRd node
	 * 
	 * @param mainIp
	 *          the main IP address of the OLSR stack of an OLSRd node
	 * @return the ClusterLeaderMsg, or null when the OLSRd node is not found
	 */
	public ClusterLeaderMsg getClusterLeader(InetAddress mainIp);

	/**
	 * Save a ClusterLeaderMsg into the database
	 * 
	 * @param clusterLeaderMsg
	 *          the ClusterLeaderMsg
	 * @param newObject
	 *          true when the object is newly created, false when it already exists in the database
	 */
	public void saveClusterLeaderMsg(ClusterLeaderMsg clusterLeaderMsg, boolean newObject);

	/**
	 * Remove expired/out-of-date ClusterLeaderMsg objects from the database
	 * 
	 * @param utcTimestamp
	 *          the timestamp for which expiry must be evaluated
	 * @param validityTimeMultiplier
	 *          the multiplier that must be applied to the validity time of a ClusterLeaderMsg before evaluating expiry
	 * @return true when 1 or more ClusterLeaderMsg objects were removed from the database
	 */
	public boolean removeExpiredClusterLeaderMsg(long utcTimestamp, double validityTimeMultiplier);

	/**
	 * Log the printout of the ClusterLeaderMsg objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the ClusterLeaderMsg objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}
