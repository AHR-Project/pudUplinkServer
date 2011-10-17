package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl.debug;

import org.olsr.plugin.pud.ClusterLeader;
import org.olsr.plugin.pud.PositionUpdate;
import org.springframework.beans.factory.annotation.Required;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PositionUpdateHandler;

public class Faker {
	private ClusterLeaderHandler clusterLeaderHandler;

	/**
	 * @param clusterLeaderHandler
	 *            the clusterLeaderHandler to set
	 */
	@Required
	public final void setClusterLeaderHandler(
			ClusterLeaderHandler clusterLeaderHandler) {
		this.clusterLeaderHandler = clusterLeaderHandler;
	}

	private PositionUpdateHandler positionUpdateHandler;

	/**
	 * @param positionUpdateHandler
	 *            the positionUpdateHandler to set
	 */
	@Required
	public final void setPositionUpdateHandler(
			PositionUpdateHandler positionUpdateHandler) {
		this.positionUpdateHandler = positionUpdateHandler;
	}

	public enum MSGTYPE {
		PU, CL
	};

	private boolean firstFake = true;

	/**
	 * @return the firstFake
	 */
	public final boolean isFirstFake() {
		return firstFake;
	}

	/**
	 * @param firstFake the firstFake to set
	 */
	public final void setFirstFake(boolean firstFake) {
		this.firstFake = firstFake;
	}

	public void fakeit(MSGTYPE type, long utcTimestamp, Object msg,
			RelayServer relayServer) {
		if (!firstFake) {
			return;
		}

		byte[] clmsg = null;
		byte[] pumsg = null;
		int initialNetwork = 0;
		byte initialNode = 1;
		if (type == MSGTYPE.PU) {
			pumsg = ((PositionUpdate) msg).getData();
			initialNetwork = pumsg[10];
			initialNode = pumsg[11];
		} else if (type == MSGTYPE.CL) {
			clmsg = ((ClusterLeader) msg).getData();
			initialNetwork = clmsg[10];
			initialNode = clmsg[11];
		} else {
			throw new IllegalArgumentException("Illegal msg type");
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
			while ((node < 255) && (nodeCount < nodeCountMax)) {
				if (!firstNode) {
					/*
					 * Position Update Message
					 */
					if (type == MSGTYPE.PU) {
						byte[] pumsgClone = pumsg.clone();
						// olsr originator
						pumsgClone[10] = (byte) network;
						pumsgClone[11] = (byte) node;

						positionUpdateHandler.handlePositionMessage(
								utcTimestamp, new PositionUpdate(pumsgClone,
										pumsgClone.length), relayServer);
					}

					/*
					 * Cluster Leader Message
					 */
					if (type == MSGTYPE.CL) {
						byte[] clmsgClone = clmsg.clone();
						// originator
						clmsgClone[10] = (byte) network;
						clmsgClone[11] = (byte) node;

						// clusterLeader
						clmsgClone[14] = (byte) network;
						clmsgClone[15] = (byte) clusterLeaderNode;

						clusterLeaderHandler.handleClusterLeaderMessage(
								utcTimestamp, new ClusterLeader(clmsgClone,
										clmsgClone.length), relayServer);
					}
				} else {
					firstNode = false;
				}

				node++;
				nodeCount++;
			}
			network++;
		}

		/* add an extra standalone node */

		node = initialNode;

		/*
		 * Position Update Message
		 */
		if (type == MSGTYPE.PU) {
			byte[] pumsgClone = pumsg.clone();
			// olsr originator
			pumsgClone[10] = (byte) network;
			pumsgClone[11] = (byte) node;

			positionUpdateHandler.handlePositionMessage(utcTimestamp,
					new PositionUpdate(pumsgClone, pumsgClone.length),
					relayServer);
		}

		/*
		 * Cluster Leader Message
		 */
		if (type == MSGTYPE.CL) {
			byte[] clmsgClone = clmsg.clone();
			// originator
			clmsgClone[10] = (byte) network;
			clmsgClone[11] = (byte) node;

			// clusterLeader
			clmsgClone[14] = (byte) (network - 1);
			clmsgClone[15] = (byte) (clusterLeaderNode + nodeCountMax - 1);

			clusterLeaderHandler.handleClusterLeaderMessage(utcTimestamp,
					new ClusterLeader(clmsgClone, clmsgClone.length),
					relayServer);
		}
	}
}
