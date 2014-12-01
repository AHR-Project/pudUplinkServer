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
import javax.validation.constraints.NotNull;

/**
 * Represents a sender node that sends PositionUpdate and ClusterLeader messages (from an OLSRd node) to the RelayServer
 */
@Entity
public class Sender implements Serializable {
	private static final long serialVersionUID = -8727383276093376224L;

	/**
	 * Default constructor
	 */
	public Sender() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param ip
	 *          the IP address of the sender
	 * @param port
	 *          the port of the sender
	 * @param relayServer
	 *          the relay server that belongs to the sender
	 */
	public Sender(InetAddress ip, Integer port, RelayServer relayServer) {
		super();
		this.ip = ip;
		this.port = port;
		this.relayServer = relayServer;
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

	/** the IP address of the sender */
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

	/** the port of the sender */
	@NotNull
	private Integer port = null;

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

	/** the associated nodes */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "sender")
	private Set<Node> nodes = new HashSet<Node>();

	/**
	 * @return the nodes
	 */
	public final Set<Node> getNodes() {
		return this.nodes;
	}

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	public final void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	/** the relay server that belongs to the sender */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, optional = false)
	@NotNull
	private RelayServer relayServer = null;

	/**
	 * @return the relayServer
	 */
	public final RelayServer getRelayServer() {
		return this.relayServer;
	}

	/**
	 * @param relayServer
	 *          the relayServer to set
	 */
	public final void setRelayServer(RelayServer relayServer) {
		this.relayServer = relayServer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + " [id=");
		builder.append(this.id);
		builder.append(", ip=");
		builder.append(this.ip.getHostAddress() + ":" + this.port.intValue());
		builder.append(", relayServer=");
		builder.append((this.relayServer != null) ? this.relayServer.getId() : "");
		builder.append(", nodes=[");
		boolean comma = false;
		Set<Long> ids = new TreeSet<Long>();
		for (Node node : this.nodes) {
			ids.add(node.getId());
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
