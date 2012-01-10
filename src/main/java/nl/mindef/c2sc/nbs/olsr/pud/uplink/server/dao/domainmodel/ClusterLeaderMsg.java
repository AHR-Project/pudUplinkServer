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

@Entity
public class ClusterLeaderMsg implements Serializable {
	private static final long serialVersionUID = -2705472710068034493L;

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
	 * @param clusterLeader
	 */
	public ClusterLeaderMsg(Node node, Node clusterLeader) {
		super();
		this.node = node;
		this.clusterLeader = clusterLeader;
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

	/** the associated node */
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

	/** the associated cluster leader node */
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	@NotNull
	private Node clusterLeader = null;

	/**
	 * @return the clusterLeader
	 */
	public final Node getClusterLeader() {
		return clusterLeader;
	}

	/**
	 * @param clusterLeader
	 *          the clusterLeader to set
	 */
	public final void setClusterLeader(Node clusterLeader) {
		this.clusterLeader = clusterLeader;
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
		builder.append(", clusterLeader=");
		builder.append(clusterLeader.getId());
		builder.append(", receptionTime=");
		builder.append(receptionTime);
		builder.append(", validityTime=");
		builder.append(validityTime);
		builder.append("]");
		return builder.toString();
	}
}
