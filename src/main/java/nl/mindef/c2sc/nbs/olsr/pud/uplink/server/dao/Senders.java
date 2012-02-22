package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The Sender DAO
 */
public interface Senders {
	/**
	 * Get a sender.<br/>
	 * <br/>
	 * Only the sender itself is retrieved, no linked objects (non-eager fetching)<br/>
	 * <br/>
	 * 
	 * @param ip
	 *          the IP address of the sender to fetch
	 * @param port
	 *          the port of the sender to fetch
	 * @return the sender, or null when not found
	 */
	public Sender getSender(InetAddress ip, int port);

	/**
	 * Save a sender into the database
	 * 
	 * @param sender
	 *          the sender to save
	 */
	public void saveSender(Sender sender);

	/**
	 * Remove expired/out-of-date Sender objects from the database
	 * 
	 * @return true when 1 or more Sender objects were removed from the database
	 */
	public boolean removeExpiredSenders();

	/**
	 * Log the printout of the Sender objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the Sender objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}
