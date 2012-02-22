package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The Node DAO
 */
public interface Nodes {
	/**
	 * Get all clusters
	 * 
	 * @param relayServer
	 *          if not null, then limit the clusters returned to those nodes that are seen by the specified RelayServer.
	 * 
	 * @return a list of clusters. Each cluster is a sorted list of nodes: sorted first on the number of cluster nodes
	 *         (cluster leaders first), sorted second on the reception time of the cluster leader message (recently seen
	 *         nodes first), sorted third on the mainIP of the node (final discriminator). When there are no clusters,
	 *         then null is returned.
	 */
	public List<List<Node>> getClusters(RelayServer relayServer);

	/**
	 * Get a node.<br/>
	 * <br/>
	 * Only the node itself is retrieved, no linked objects (non-eager fetching) <br/>
	 * <br/>
	 * 
	 * @param mainIp
	 *          the main IP address of the OLSR stack of the OLSRd node
	 * @return a list of all nodes, or null when not found
	 */
	public Node getNode(InetAddress mainIp);

	/**
	 * Save a node into the database
	 * 
	 * @param node
	 *          the node to save
	 */
	public void saveNode(Node node);

	/**
	 * Remove expired/out-of-date Node objects from the database
	 * 
	 * @return true when 1 or more Node objects were removed from the database
	 */
	public boolean removeExpiredNodes();

	/**
	 * Log the printout of the Node objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the Node objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}
