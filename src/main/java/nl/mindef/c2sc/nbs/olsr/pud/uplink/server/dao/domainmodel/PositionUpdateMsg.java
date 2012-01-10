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

@Entity
public class PositionUpdateMsg implements Serializable {
	private static final long serialVersionUID = -1387490955979060516L;

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
	 * @param positionUpdateMsg
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
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, mappedBy = "positionUpdateMsg", optional=false)
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

	/** the position update uplink message */
	@NotNull
	private PositionUpdate positionUpdateMsg = null;

	/**
	 * @return the positionUpdateMsg
	 */
	public final PositionUpdate getPositionUpdateMsg() {
		return positionUpdateMsg;
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
		builder.append(", receptionTime=");
		builder.append(receptionTime);
		builder.append(", validityTime=");
		builder.append(validityTime);
		builder.append("]");
		return builder.toString();
	}
}
