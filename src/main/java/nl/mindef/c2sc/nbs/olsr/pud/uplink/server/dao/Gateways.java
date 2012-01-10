package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public interface Gateways {
	/**
	 * Get a list of gateways.<br/>
	 * <br/>
	 * Only the gateways themselves are retrieved, no linked objects (non-eager fetching) <br/>
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
	 * Only the gateway itself is retrieved, no linked objects (non-eager fetching) <br/>
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
	 * Save a gateway.
	 * 
	 * @param gateway
	 *          the gateway to save
	 * @param newObject
	 *          true when the gateway is new, false when already exists
	 */
	public void saveGateway(Gateway gateway, boolean newObject);

	/**
	 * Remove all empty gateways.<br/>
	 */
	public boolean removeExpiredGateways();

	public void log(Logger logger, Level level);

	public void print(OutputStream out) throws IOException;
}
