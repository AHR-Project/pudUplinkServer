package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The PositionUpdateMsg DAO
 */
public interface PositionUpdateMsgs {
	/**
	 * Retrieve the PositionUpdateMsg as sent by an OLSRd node
	 * 
	 * @param mainIp
	 *          the main IP address of the OLSR stack of an OLSRd node
	 * @return the PositionUpdateMsg, or null when the OLSRd node is not found
	 */
	public PositionUpdateMsg getPositionUpdateMsg(InetAddress mainIp);

	/**
	 * Retrieve the PositionUpdateMsg objects that must be distributed to the given cluster leader. A PositionUpdateMsg
	 * must be distributed if its reception time is later than startTime and at latest endTime.
	 * 
	 * @param startTime
	 *          the startTime of the reception time window
	 * @param endTime
	 *          the endTime of the reception time window
	 * @param clusterLeader
	 *          the cluster leader for which the objects must be retrieved. When null, then just retrieve all objects that
	 *          must be distributed
	 * @return the list of objects that must be distributed, or null when none found
	 */
	public List<PositionUpdateMsg> getPositionUpdateMsgForDistribution(long startTime, long endTime, Node clusterLeader);

	/**
	 * Save a PositionUpdateMsg into the database
	 * 
	 * @param positionUpdateMsg
	 *          the PositionUpdateMsg
	 */
	public void savePositionUpdateMsg(PositionUpdateMsg positionUpdateMsg);

	/**
	 * Remove expired/out-of-date PositionUpdateMsg objects from the database
	 * 
	 * @param utcTimestamp
	 *          the timestamp for which expiry must be evaluated
	 * @param validityTimeMultiplier
	 *          the multiplier that must be applied to the validity time of a PositionUpdateMsg before evaluating expiry
	 * @return true when 1 or more PositionUpdateMsg objects were removed from the database
	 */
	public boolean removeExpiredPositionUpdateMsg(long utcTimestamp, double validityTimeMultiplier);

	/**
	 * Log the printout of the PositionUpdateMsg objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the PositionUpdateMsg objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}