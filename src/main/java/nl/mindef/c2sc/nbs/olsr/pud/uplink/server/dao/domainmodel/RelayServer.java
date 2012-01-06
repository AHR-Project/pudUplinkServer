package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
public class RelayServer implements Serializable {
	private static final long serialVersionUID = 3573019703164508653L;

	/**
	 * Default constructor
	 */
	public RelayServer() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param ip
	 * @param port
	 */
	public RelayServer(InetAddress ip, Integer port) {
		super();
		this.ip = ip;
		this.port = port;
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

	/** the IP address of the server */
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

	/** the port of the server */
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

	/** the gateways associated with the relay server */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "relayServer")
	private Set<Gateway> gateways = new HashSet<Gateway>();

	/**
	 * @return the gateways
	 */
	public final Set<Gateway> getGateways() {
		return gateways;
	}

	/**
	 * @param gateways
	 *          the gateways to set
	 */
	public final void setGateways(Set<Gateway> nodes) {
		this.gateways = nodes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(id);
		builder.append(", ip=");
		builder.append(ip.getHostAddress() + ":" + port.intValue());
		builder.append(", gateways=[");
		boolean comma = false;
		for (Gateway gateway : gateways) {
			if (comma) {
				builder.append(", ");
			}
			builder.append(gateway.getId());
			comma = true;
		}
		builder.append("]]");
		return builder.toString();
	}
}
