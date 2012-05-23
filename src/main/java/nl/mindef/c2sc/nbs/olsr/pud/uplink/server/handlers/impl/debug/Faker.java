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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl.debug;

import java.util.Random;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PositionUpdateHandler;

import org.olsr.plugin.pud.ClusterLeader;
import org.olsr.plugin.pud.PositionUpdate;
import org.springframework.beans.factory.annotation.Required;

public class Faker {
	private ClusterLeaderHandler clusterLeaderHandler;

	/**
	 * @param clusterLeaderHandler
	 *          the clusterLeaderHandler to set
	 */
	@Required
	public final void setClusterLeaderHandler(ClusterLeaderHandler clusterLeaderHandler) {
		this.clusterLeaderHandler = clusterLeaderHandler;
	}

	private PositionUpdateHandler positionUpdateHandler;

	/**
	 * @param positionUpdateHandler
	 *          the positionUpdateHandler to set
	 */
	@Required
	public final void setPositionUpdateHandler(PositionUpdateHandler positionUpdateHandler) {
		this.positionUpdateHandler = positionUpdateHandler;
	}

	private boolean firstFake = true;

	/**
	 * @return the firstFake
	 */
	public final boolean isFirstFake() {
		return this.firstFake;
	}

	/**
	 * @param firstFake
	 *          the firstFake to set
	 */
	public final void setFirstFake(boolean firstFake) {
		this.firstFake = firstFake;
	}

	/* locations in byte array */
	static private final int UplinkMessage_v4_olsrMessage_v4_originator_network = 10;
	static private final int UplinkMessage_v4_olsrMessage_v4_originator_node = 11;
	static private final int UplinkMessage_v4_clusterLeader_originator_network = 8;
	static private final int UplinkMessage_v4_clusterLeader_originator_node = 9;
	static private final int UplinkMessage_v4_clusterLeader_clusterLeader_network = 12;
	static private final int UplinkMessage_v4_clusterLeader_clusterLeader_node = 13;

	public void fakeit(Sender sender, long utcTimestamp, Object msg) {
		assert ((msg instanceof PositionUpdate) || (msg instanceof ClusterLeader));

		if (!this.firstFake) {
			return;
		}

		Random random = new Random();
		int randomRange = 100;
		byte[] clmsg = null;
		byte[] pumsg = null;
		int initialNetwork = 0;
		byte initialNode = 1;
		if (msg instanceof PositionUpdate) {
			pumsg = ((PositionUpdate) msg).getData();
			initialNetwork = pumsg[UplinkMessage_v4_olsrMessage_v4_originator_network];
			initialNode = pumsg[UplinkMessage_v4_olsrMessage_v4_originator_node];
		} else /* if (msg instanceof ClusterLeader) */{
			clmsg = ((ClusterLeader) msg).getData();
			initialNetwork = clmsg[UplinkMessage_v4_clusterLeader_originator_network];
			initialNode = clmsg[UplinkMessage_v4_clusterLeader_originator_node];
		}

		boolean firstNode = true;
		int network = initialNetwork;
		int networkMax = network + 2;
		byte node = initialNode;
		int nodeCount = 0;
		int nodeCountMax = 6;
		byte clusterLeaderNode = node;
		while (network <= networkMax) {
			node = initialNode;
			clusterLeaderNode = node;
			nodeCount = 0;
			while (nodeCount < nodeCountMax) {
				if (!firstNode) {
					boolean skipNode = ((network == (initialNetwork + 1)) && (node == initialNode));
					if (!skipNode) {
						/*
						 * Position Update Message
						 */
						if (msg instanceof PositionUpdate) {
							assert (pumsg != null);
							byte[] pumsgClone = pumsg.clone();
							/* olsr originator */
							pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_network] = (byte) network;
							pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_node] = node;

							PositionUpdate pu = new PositionUpdate(pumsgClone, pumsgClone.length);
							this.positionUpdateHandler.handlePositionMessage(sender, utcTimestamp + random.nextInt(randomRange), pu);
						}

						/*
						 * Cluster Leader Message
						 */
						else /* if (msg instanceof ClusterLeader) */{
							assert (clmsg != null);
							byte[] clmsgClone = clmsg.clone();
							/* originator */
							clmsgClone[UplinkMessage_v4_clusterLeader_originator_network] = (byte) network;
							clmsgClone[UplinkMessage_v4_clusterLeader_originator_node] = node;

							/* clusterLeader */
							clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_network] = (byte) network;
							clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_node] = clusterLeaderNode;

							ClusterLeader cl = new ClusterLeader(clmsgClone, clmsgClone.length);
							this.clusterLeaderHandler.handleClusterLeaderMessage(sender, utcTimestamp + random.nextInt(randomRange),
									cl);
						}
					}
				} else {
					firstNode = false;
				}

				node++;
				if (node == 0) {
					node++;
				}
				nodeCount++;
			}
			network++;
		}

		/* add an extra standalone node */

		node = initialNode;

		/*
		 * Position Update Message
		 */
		if (msg instanceof PositionUpdate) {
			assert (pumsg != null);
			byte[] pumsgClone = pumsg.clone();
			// olsr originator
			pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_network] = (byte) network;
			pumsgClone[UplinkMessage_v4_olsrMessage_v4_originator_node] = node;

			PositionUpdate pu = new PositionUpdate(pumsgClone, pumsgClone.length);
			this.positionUpdateHandler.handlePositionMessage(sender, utcTimestamp + random.nextInt(randomRange), pu);
		}

		/*
		 * Cluster Leader Message
		 */
		else /* if (msg instanceof ClusterLeader) */{
			assert (clmsg != null);
			byte[] clmsgClone = clmsg.clone();
			// originator
			clmsgClone[UplinkMessage_v4_clusterLeader_originator_network] = (byte) network;
			clmsgClone[UplinkMessage_v4_clusterLeader_originator_node] = node;

			// clusterLeader
			clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_network] = (byte) (network - 1);
			clmsgClone[UplinkMessage_v4_clusterLeader_clusterLeader_node] = (byte) (clusterLeaderNode + nodeCountMax - 1);

			ClusterLeader cl = new ClusterLeader(clmsgClone, clmsgClone.length);
			this.clusterLeaderHandler.handleClusterLeaderMessage(sender, utcTimestamp + random.nextInt(randomRange), cl);
		}
	}
}
