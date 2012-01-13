package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The Gateway DAO
 */
public interface Gateways {
	/**
	 * Get a list of gateways.<br/>
	 * <br/>
	 * Only the gateways themselves are retrieved, no linked objects (non-eager fetching)<br/>
	 * <br/>
	 * 
	 * @param ip
	 *          the IP address of the gateways to fetch
	 * @return a set of gateways on the given IP address, or null when none found
	 */
	public List<Gateway> getGateways(InetAddress ip);

	/**
	 * Get a gateway.<br/>
	 * <br/>
	 * Only the gateway itself is retrieved, no linked objects (non-eager fetching)<br/>
	 * <br/>
	 * 
	 * @param ip
	 *          the IP address of the gateway to fetch
	 * @param port
	 *          the port of the gateway to fetch
	 * @return the gateway, or null when not found
	 */
	public Gateway getGateway(InetAddress ip, int port);

	/**
	 * Save a gateway into the database
	 * 
	 * @param gateway
	 *          the gateway to save
	 */
	public void saveGateway(Gateway gateway);

	/**
	 * Remove expired/out-of-date Gateway objects from the database
	 * 
	 * @return true when 1 or more Gateway objects were removed from the database
	 */
	public boolean removeExpiredGateways();

	/**
	 * Log the printout of the Gateway objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the Gateway objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}
