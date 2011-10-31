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
import javax.persistence.OneToMany;

@Entity
public class RelayServer implements Serializable {
	private static final long serialVersionUID = 5783285610419347767L;

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

	private InetAddress ip = null;

	/**
	 * @return the ip
	 */
	public final InetAddress getIp() {
		return ip;
	}

	/**
	 * @param ip
	 *            the ip to set
	 */
	public final void setIp(InetAddress ip) {
		this.ip = ip;
	}

	static public final int PORT_DEFAULT = 2242;
	private int port = PORT_DEFAULT;

	/**
	 * @return the port
	 */
	public final int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public final void setPort(int port) {
		this.port = port;
	}

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "relayServer")
	private Set<Node> nodes = new TreeSet<Node>();

	/**
	 * @return the nodes
	 */
	public final Set<Node> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	public final void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(id);
		builder.append(", ip=");
		builder.append((ip == null) ? "null" : ip.getHostAddress());
		builder.append(", port=");
		builder.append(port);
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
		builder.append("]");
		return builder.toString();
	}
}
