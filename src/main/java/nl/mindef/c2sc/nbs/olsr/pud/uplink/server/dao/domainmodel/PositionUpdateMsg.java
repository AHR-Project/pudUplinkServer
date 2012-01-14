package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.olsr.plugin.pud.PositionUpdate;

/**
 * Represents a PositionUpdate message as sent by the OLSRd PUD plugin
 */
@Entity
public class PositionUpdateMsg implements Serializable {
	private static final long serialVersionUID = -7725058655152809466L;

	/**
	 * Default constructor
	 */
	public PositionUpdateMsg() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param node
	 *          the node to which the PositionUpdate message belongs
	 * @param positionUpdateMsg
	 *          the PositionUpdate message
	 */
	public PositionUpdateMsg(Node node, PositionUpdate positionUpdateMsg) {
		super();
		this.node = node;
		this.positionUpdateMsg = positionUpdateMsg;
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

	/** the node to which the PositionUpdate message belongs */
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, mappedBy = "positionUpdateMsg", optional = false)
	@NotNull
	private Node node = null;

	/**
	 * @return the node
	 */
	public final Node getNode() {
		return this.node;
	}

	/**
	 * @param node
	 *          the node to set
	 */
	public final void setNode(Node node) {
		this.node = node;
	}

	/** the PositionUpdate message */
	@NotNull
	private PositionUpdate positionUpdateMsg = null;

	/**
	 * @return the positionUpdateMsg
	 */
	public final PositionUpdate getPositionUpdateMsg() {
		return this.positionUpdateMsg;
	}

	/**
	 * @param positionUpdateMsg
	 *          the positionUpdateMsg to set
	 */
	public final void setPositionUpdateMsg(PositionUpdate positionUpdateMsg) {
		this.positionUpdateMsg = positionUpdateMsg;
	}

	/** the reception date (UTC, milliseconds since Epoch) */
	private long receptionTime = 0;

	/**
	 * @return the receptionTime
	 */
	public final long getReceptionTime() {
		return this.receptionTime;
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
		return this.validityTime;
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
		builder.append(this.id);
		builder.append(", node=");
		builder.append(this.node.getId());
		builder.append(", receptionTime=");
		builder.append(this.receptionTime);
		builder.append(", validityTime=");
		builder.append(this.validityTime);
		builder.append("]");
		return builder.toString();
	}
}
