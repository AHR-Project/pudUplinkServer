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
		return this.id;
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
		return this.node;
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
		return this.clusterLeaderNode;
	}

	/**
	 * @param clusterLeaderNode
	 *          the clusterLeaderNode to set
	 */
	public final void setClusterLeaderNode(Node clusterLeaderNode) {
		this.clusterLeaderNode = clusterLeaderNode;
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
		builder.append(", clusterLeaderNode=");
		builder.append(this.clusterLeaderNode.getId());
		builder.append(", receptionTime=");
		builder.append(this.receptionTime);
		builder.append(", validityTime=");
		builder.append(this.validityTime);
		builder.append("]");
		return builder.toString();
	}
}
