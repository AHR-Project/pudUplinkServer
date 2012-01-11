package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The RelayServer DAO
 */
public interface RelayServers {
	/**
	 * Retrieve all RelayServer objects from the database
	 * 
	 * @return a list of all RelayServer objects, or null when none found
	 */
	public List<RelayServer> getRelayServers();

	/**
	 * Retrieve a list of other (not me) RelayServer objects from the database
	 * 
	 * @return a list of all other RelayServer objects, or null when none found
	 */
	public List<RelayServer> getOtherRelayServers();

	/**
	 * Save a RelayServer into the database
	 * 
	 * @param relayServer
	 *          the RelayServer
	 * @param newObject
	 *          true when the object is newly created, false when it already exists in the database
	 */
	public void addRelayServer(RelayServer relayServer, boolean newObject);

	/**
	 * Retrieve the RelayServer object that represent this RelayServer from the database (it will be created when not
	 * found)
	 * 
	 * @return the RelayServer object
	 */
	public RelayServer getMe();

	/**
	 * Log the printout of the RelayServer objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the RelayServer objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}
