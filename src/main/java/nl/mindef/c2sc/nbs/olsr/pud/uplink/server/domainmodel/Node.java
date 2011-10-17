package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class Node implements Serializable {
	private static final long serialVersionUID = -5972046138211714143L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * @return the id
	 */
	public final Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public final void setId(Long id) {
		this.id = id;
	}

	/** the main IP of the node */
	@NotNull
	private InetAddress mainIp = null;

	/**
	 * @return the mainIp
	 */
	public final InetAddress getMainIp() {
		return mainIp;
	}

	/**
	 * @param mainIp
	 *            the mainIp to set
	 */
	public final void setMainIp(InetAddress mainIp) {
		this.mainIp = mainIp;
	}

	public static final int DOWNLINK_PORT_INVALID = -1;

	/** the downlink UDP port for the node */
	private int downlinkPort = DOWNLINK_PORT_INVALID;

	/**
	 * @return the downlinkPort
	 */
	public final int getDownlinkPort() {
		return downlinkPort;
	}

	/**
	 * @param downlinkPort
	 *            the downlinkPort to set
	 */
	public final void setDownlinkPort(int downlinkPort) {
		this.downlinkPort = downlinkPort;
	}

	/** the cluster leader for the node */
	@ManyToOne(cascade = CascadeType.ALL, optional = true)
	private Node clusterLeader = null;

	/**
	 * @return the clusterLeader
	 */
	public final Node getClusterLeader() {
		return clusterLeader;
	}

	/**
	 * @param clusterLeader
	 *            the clusterLeader to set
	 */
	public final void setClusterLeader(Node clusterLeader) {
		this.clusterLeader = clusterLeader;
	}

	/** the cluster leader for the node */
	@OneToMany(mappedBy = "clusterLeader", cascade = CascadeType.ALL)
	private Set<Node> clusterNodes = new TreeSet<Node>();

	/**
	 * @return the clusterNodes
	 */
	public final Set<Node> getClusterNodes() {
		return clusterNodes;
	}

	/**
	 * @param clusterNodes
	 *            the clusterNodes to set
	 */
	public final void setClusterNodes(Set<Node> clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	/** the reception date (UTC, milliseconds since Epoch) */
	private long receptionTime = 0;

	/**
	 * @return the receptionTime
	 */
	public final long getReceptionTime() {
		return receptionTime;
	}

	/**
	 * @param receptionTime
	 *            the receptionTime to set
	 */
	public final void setReceptionTime(long receptionTime) {
		this.receptionTime = receptionTime;
	}

	/** the validity time in milliseconds */
	private long validityTime = 0;

	/**
	 * @return the validityTime
	 */
	public final long getValidityTime() {
		return validityTime;
	}

	/**
	 * @param validityTime
	 *            the validityTime to set
	 */
	public final void setValidityTime(long validityTime) {
		this.validityTime = validityTime;
	}

	/** the associated position */
	@OneToOne(cascade = CascadeType.ALL, optional = true)
	private NodePosition position = null;

	/**
	 * @return the position
	 */
	public final NodePosition getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public final void setPosition(NodePosition position) {
		this.position = position;
	}

	@ManyToOne(cascade = CascadeType.ALL, optional = true)
	private RelayServer relayServer = null;

	/**
	 * @return the relayServer
	 */
	public final RelayServer getRelayServer() {
		return relayServer;
	}

	/**
	 * @param relayServer
	 *            the relayServer to set
	 */
	public final void setRelayServer(RelayServer relayServer) {
		this.relayServer = relayServer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(id);
		builder.append(", mainIp=");
		builder.append((mainIp != null) ? mainIp.getHostAddress() : "null");
		builder.append(", downlinkPort=");
		builder.append(downlinkPort);
		builder.append(", position=");
		builder.append((position != null) ? position.getId() : "null");
		builder.append(", clusterLeader=");
		builder.append((clusterLeader == null) ? "-" : clusterLeader.getId());
		builder.append(", relayServer=");
		builder.append((relayServer != null) ? relayServer.getId() : "null");
		builder.append(", receptionTime=");
		builder.append(receptionTime);
		builder.append(", validityTime=");
		builder.append(validityTime);
		builder.append(", clusterNodes=");
		boolean comma = false;
		for (Node node : clusterNodes) {
			if (comma) {
				builder.append(", ");
			}
			builder.append(node.getId());
			comma = true;
		}
		builder.append("]");
		builder.append("]");
		return builder.toString();
	}
}
