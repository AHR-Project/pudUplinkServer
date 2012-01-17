package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The Node DAO
 */
public interface Nodes {
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
	 * Get a list of all cluster leaders.<br/>
	 * <br/>
	 * Only the cluster leader nodes themselves are retrieved along with their senders (eager), no _other_ linked objects
	 * (non-eager fetching) <br/>
	 * <br/>
	 * 
	 * A cluster leader is:
	 * <ul>
	 * <li>a node that points to itself as a cluster leader</li>
	 * <li>a node that points to another node that does not point to itself as a cluster leader</li>
	 * </ul>
	 * When clusterLeadersIncludesTransitionalNodes is true, then a cluster leader also is:
	 * <ul>
	 * <li>a node of which the cluster leader does not point to itself</li>
	 * </ul>
	 * 
	 * 
	 * @return a list of all cluster leader nodes, or null when not found
	 */
	public List<Node> getClusterLeaders();

	/**
	 * Get a substitute cluster leader for a given cluster leader.<br/>
	 * <br/>
	 * Only the substitute cluster leader node is retrieved along with its sender (eager), no _other_ linked objects
	 * (non-eager fetching)<br/>
	 * <br/>
	 * The substitute cluster leader is:
	 * <ul>
	 * <li>a node that is in the same cluster as the cluster leader, but is not the cluster leader itself</li>
	 * <li>a node that has a valid Sender</li>
	 * </ul>
	 * 
	 * @param clusterLeader
	 *          the cluster leader for which a substitute is sought
	 * @return the substitute cluster leader, or null when not found
	 */
	public Node getSubstituteClusterLeader(Node clusterLeader);

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
