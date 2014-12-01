/*
 *  Copyright (C) 2012 Royal Dutch Army
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *  MA  02110-1301, USA.
 */
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

/**
 * Represents an OLSRd node that sends PositionUpdate and ClusterLeader messages to a sender node
 */
@Entity
public class Node implements Serializable {
	private static final long serialVersionUID = 3025709519453023866L;

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
	 *          the main IP address of the OLSR stack of the OLSRd node
	 * @param sender
	 *          the sender to which the node belongs
	 */
	public Node(InetAddress mainIp, Sender sender) {
		super();
		this.mainIp = mainIp;
		this.sender = sender;
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

	/** the main IP address of the OLSR stack of the OLSRd node */
	@NotNull
	private InetAddress mainIp = null;

	/**
	 * @return the mainIp
	 */
	public final InetAddress getMainIp() {
		return this.mainIp;
	}

	/**
	 * @param mainIp
	 *          the mainIp to set
	 */
	public final void setMainIp(InetAddress mainIp) {
		this.mainIp = mainIp;
	}

	/**
	 * the sender to which the node belongs; can be null when an OLSRd node sends a ClusterLeader message that points at a
	 * cluster leader node that has not been seen yet by the RelayServer
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, optional = true)
	private Sender sender = null;

	/**
	 * @return the sender
	 */
	public final Sender getSender() {
		return this.sender;
	}

	/**
	 * @param sender
	 *          the sender to set
	 */
	public final void setSender(Sender sender) {
		this.sender = sender;
	}

	/** the associated position update message */
	@OneToOne(cascade = CascadeType.ALL, optional = true)
	private PositionUpdateMsg positionUpdateMsg = null;

	/**
	 * @return the positionUpdateMsg
	 */
	public final PositionUpdateMsg getPositionUpdateMsg() {
		return this.positionUpdateMsg;
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
		return this.clusterLeaderMsg;
	}

	/**
	 * @param clusterLeaderMsg
	 *          the clusterLeaderMsg to set
	 */
	public final void setClusterLeaderMsg(ClusterLeaderMsg clusterLeaderMsg) {
		this.clusterLeaderMsg = clusterLeaderMsg;
	}

	/** the associated cluster nodes */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, mappedBy = "clusterLeaderNode")
	private Set<ClusterLeaderMsg> clusterNodes = new HashSet<ClusterLeaderMsg>();

	/**
	 * @return the clusterNodes
	 */
	public final Set<ClusterLeaderMsg> getClusterNodes() {
		return this.clusterNodes;
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
		builder.append(this.id);
		builder.append(", mainIp=");
		builder.append(this.mainIp.getHostAddress());
		builder.append(", sender=");
		builder.append((this.sender == null) ? "" : this.sender.getId());
		builder.append(", clusterNodes=[");
		boolean comma = false;
		Set<Long> ids = new TreeSet<Long>();
		for (ClusterLeaderMsg clusterNode : this.clusterNodes) {
			ids.add(clusterNode.getId());
		}
		for (Long idIterator : ids) {
			if (comma) {
				builder.append(", ");
			}
			builder.append(idIterator);
			comma = true;
		}
		builder.append("]");
		builder.append(", positionUpdateMsg=");
		builder.append((this.positionUpdateMsg == null) ? "" : this.positionUpdateMsg.getId());
		builder.append(", clusterLeaderMsg=");
		builder.append((this.clusterLeaderMsg == null) ? "" : this.clusterLeaderMsg.getId());
		builder.append("]");
		return builder.toString();
	}
}
