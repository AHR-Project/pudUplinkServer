package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

/**
 * Represents a RelayServer that receives and distributes PositionUpdate and ClusterLeader messages
 */
@Entity
public class RelayServer implements Serializable {
	private static final long serialVersionUID = 7450468040325248095L;

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
	 *          the IP address of the relay server
	 * @param port
	 *          the port of the relay server
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
		return this.id;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public final void setId(Long id) {
		this.id = id;
	}

	/** the IP address of the relay server */
	@NotNull
	private InetAddress ip = null;

	/**
	 * @return the ip
	 */
	public final InetAddress getIp() {
		return this.ip;
	}

	/**
	 * @param ip
	 *          the ip to set
	 */
	public final void setIp(InetAddress ip) {
		this.ip = ip;
	}

	/** the port of the relay server */
	private Integer port = Integer.valueOf(2243);

	/**
	 * @return the port
	 */
	public final Integer getPort() {
		return this.port;
	}

	/**
	 * @param port
	 *          the port to set
	 */
	public final void setPort(Integer port) {
		this.port = port;
	}

	/** the senders associated with the relay server */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "relayServer")
	private Set<Sender> senders = new HashSet<Sender>();

	/**
	 * @return the senders
	 */
	public final Set<Sender> getSenders() {
		return this.senders;
	}

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	public final void setSenders(Set<Sender> nodes) {
		this.senders = nodes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(this.id);
		builder.append(", ip=");
		builder.append(this.ip.getHostAddress() + ":" + this.port);
		builder.append(", senders=[");
		boolean comma = false;
		Set<Long> ids = new TreeSet<Long>();
		for (Sender sender : this.senders) {
			ids.add(sender.getId());
		}
		for (Long idIterator : ids) {
			if (comma) {
				builder.append(", ");
			}
			builder.append(idIterator);
			comma = true;
		}
		builder.append("]]");
		return builder.toString();
	}
}
