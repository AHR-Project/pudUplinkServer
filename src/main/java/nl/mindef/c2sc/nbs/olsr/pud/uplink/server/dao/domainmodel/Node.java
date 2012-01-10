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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class Node implements Serializable {
	private static final long serialVersionUID = 8997670429374427930L;

	/**
	 * Default constructor
	 */
	public Node() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param mainIp
	 * @param gateway
	 */
	public Node(InetAddress mainIp, Gateway gateway) {
		super();
		this.mainIp = mainIp;
		this.gateway = gateway;
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
	 *          the mainIp to set
	 */
	public final void setMainIp(InetAddress mainIp) {
		this.mainIp = mainIp;
	}

	/** the associated gateway */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
	private Gateway gateway = null;

	/**
	 * @return the gateway
	 */
	public final Gateway getGateway() {
		return gateway;
	}

	/**
	 * @param gateway
	 *          the gateway to set
	 */
	public final void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	/** the associated position update message */
	@OneToOne(cascade = CascadeType.ALL, optional = true)
	private PositionUpdateMsg positionUpdateMsg = null;

	/**
	 * @return the positionUpdateMsg
	 */
	public final PositionUpdateMsg getPositionUpdateMsg() {
		return positionUpdateMsg;
	}

	/**
	 * @param positionUpdateMsg
	 *          the positionUpdateMsg to set
	 */
	public final void setPositionUpdateMsg(PositionUpdateMsg positionUpdateMsg) {
		this.positionUpdateMsg = positionUpdateMsg;
	}

	/** the associated cluster leader message */
	@OneToOne(cascade = CascadeType.ALL, optional = true)
	private ClusterLeaderMsg clusterLeaderMsg = null;

	/**
	 * @return the clusterLeaderMsg
	 */
	public final ClusterLeaderMsg getClusterLeaderMsg() {
		return clusterLeaderMsg;
	}

	/**
	 * @param clusterLeaderMsg
	 *          the clusterLeaderMsg to set
	 */
	public final void setClusterLeaderMsg(ClusterLeaderMsg clusterLeaderMsg) {
		this.clusterLeaderMsg = clusterLeaderMsg;
	}

	/** the associated cluster nodes */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, mappedBy = "clusterLeader")
	private Set<ClusterLeaderMsg> clusterNodes = new HashSet<ClusterLeaderMsg>();

	/**
	 * @return the clusterNodes
	 */
	public final Set<ClusterLeaderMsg> getClusterNodes() {
		return clusterNodes;
	}

	/**
	 * @param clusterNodes
	 *          the clusterNodes to set
	 */
	public final void setClusterNodes(Set<ClusterLeaderMsg> clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(id);
		builder.append(", mainIp=");
		builder.append(mainIp.getHostAddress());
		builder.append(", gateway=");
		builder.append(gateway.getId());
		builder.append(", clusterNodes=[");
		boolean comma = false;
		for (ClusterLeaderMsg clusterNode : clusterNodes) {
			if (comma) {
				builder.append(", ");
			}
			builder.append(clusterNode.getNode().getId());
			comma = true;
		}
		builder.append("]");
		builder.append(", positionUpdateMsg=");
		builder.append((positionUpdateMsg == null) ? "" : positionUpdateMsg.getId());
		builder.append(", clusterLeaderMsg=");
		builder.append((clusterLeaderMsg == null) ? "" : clusterLeaderMsg.getId());
		builder.append("]");
		return builder.toString();
	}
}
