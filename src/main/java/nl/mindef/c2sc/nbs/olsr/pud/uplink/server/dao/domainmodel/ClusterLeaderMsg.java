package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

/**
 * Represents a ClusterLeader message as sent by the OLSRd PUD plugin
 */
@Entity
public class ClusterLeaderMsg implements Serializable {
	private static final long serialVersionUID = 6855352041095506686L;

	/**
	 * Default constructor
	 */
	public ClusterLeaderMsg() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param node
	 *          the node to which the ClusterLeader message belongs
	 * @param clusterLeaderNode
	 *          the cluster leader of the node
	 */
	public ClusterLeaderMsg(Node node, Node clusterLeaderNode) {
		super();
		this.node = node;
		this.clusterLeaderNode = clusterLeaderNode;
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

	/** the node to which the ClusterLeader message belongs */
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, mappedBy = "clusterLeaderMsg", optional = false)
	@NotNull
	private Node node = null;

	/**
	 * @return the node
	 */
	public final Node getNode() {
		return node;
	}

	/**
	 * @param node
	 *          the node to set
	 */
	public final void setNode(Node node) {
		this.node = node;
	}

	/** the cluster leader of the node */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, optional = false)
	@NotNull
	private Node clusterLeaderNode = null;

	/**
	 * @return the clusterLeaderNode
	 */
	public final Node getClusterLeaderNode() {
		return clusterLeaderNode;
	}

	/**
	 * @param clusterLeaderNode
	 *          the clusterLeaderNode to set
	 */
	public final void setClusterLeaderNode(Node clusterLeader) {
		this.clusterLeaderNode = clusterLeader;
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
	 *          the receptionTime to set
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
	 *          the validityTime to set
	 */
	public final void setValidityTime(long validityTime) {
		this.validityTime = validityTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(id);
		builder.append(", node=");
		builder.append(node.getId());
		builder.append(", clusterLeaderNode=");
		builder.append(clusterLeaderNode.getId());
		builder.append(", receptionTime=");
		builder.append(receptionTime);
		builder.append(", validityTime=");
		builder.append(validityTime);
		builder.append("]");
		return builder.toString();
	}
}
