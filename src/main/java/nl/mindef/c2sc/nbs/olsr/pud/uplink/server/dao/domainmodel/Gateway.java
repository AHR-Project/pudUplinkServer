package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel;

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
import javax.validation.constraints.NotNull;

@Entity
public class Gateway implements Serializable {
	private static final long serialVersionUID = -7276838498590984721L;

	/**
	 * Default constructor
	 */
	public Gateway() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param ip
	 *          the IP address of the gateway
	 * @param port
	 *          the port of the gateway
	 * @param relayServer
	 *          the relay server that received from the gateway
	 */
	public Gateway(InetAddress ip, Integer port, RelayServer relayServer) {
		super();
		this.ip = ip;
		this.port = port;
		this.relayServer = relayServer;
	}

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
	 *          the id to set
	 */
	public final void setId(Long id) {
		this.id = id;
	}

	/** the destination IP of the node */
	@NotNull
	private InetAddress ip = null;

	/**
	 * @return the ip
	 */
	public final InetAddress getIp() {
		return ip;
	}

	/**
	 * @param ip
	 *          the ip to set
	 */
	public final void setIp(InetAddress ip) {
		this.ip = ip;
	}

	/** the UDP port for the node */
	@NotNull
	private Integer port = null;

	/**
	 * @return the port
	 */
	public final Integer getPort() {
		return port;
	}

	/**
	 * @param port
	 *          the port to set
	 */
	public final void setPort(Integer port) {
		this.port = port;
	}

	/** the associated nodes */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "gateway", orphanRemoval = true)
	private Set<Node> nodes = new TreeSet<Node>();

	/**
	 * @return the nodes
	 */
	public final Set<Node> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	public final void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	/** the associated relay server */
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	@NotNull
	private RelayServer relayServer = null;

	/**
	 * @return the relayServer
	 */
	public final RelayServer getRelayServer() {
		return relayServer;
	}

	/**
	 * @param relayServer
	 *          the relayServer to set
	 */
	public final void setRelayServer(RelayServer relayServer) {
		this.relayServer = relayServer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(id);
		builder.append(", ip=");
		builder.append(ip.getHostAddress());
		builder.append(":");
		builder.append(port.intValue());
		builder.append(", nodes=[");
		boolean comma = false;
		for (Node node : nodes) {
			if (comma) {
				builder.append(", ");
			}
			builder.append(node.getId());
			comma = true;
		}
		builder.append("]");
		builder.append(", relayServer=");
		builder.append(relayServer.getId());
		builder.append("]");
		return builder.toString();
	}
}
