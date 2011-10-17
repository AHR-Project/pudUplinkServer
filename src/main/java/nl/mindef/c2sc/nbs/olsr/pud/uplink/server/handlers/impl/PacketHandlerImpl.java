package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.DatagramPacket;
import java.util.Arrays;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.RelayServerConfiguration;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PacketHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PositionUpdateHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util.Util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.olsr.plugin.pud.ClusterLeader;
import org.olsr.plugin.pud.PositionUpdate;
import org.olsr.plugin.pud.UplinkMessage;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PacketHandlerImpl implements PacketHandler {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private RelayServerConfiguration config;

	/**
	 * @param config
	 *            the config to set
	 */
	@Required
	public final void setConfig(RelayServerConfiguration config) {
		this.config = config;
	}

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

	/*
	 * Fake data
	 */

	private enum MSGTYPE {
		PU, CL
	};

	static private boolean firstFake = true;

	private void fakeit(MSGTYPE type, long utcTimestamp, Object msg,
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

	/*
	 * end fake
	 */

	@Override
	@Transactional
	public boolean processPacket(DatagramPacket packet, RelayServer relayServer) {
		boolean updated = false;
		long utcTimestamp = System.currentTimeMillis();

		config.getDataLock().lock();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Received " + packet.getLength()
						+ " bytes on timestamp " + utcTimestamp + " from "
						+ packet.getAddress().getHostAddress());
			}

			byte[] packetData = packet.getData();
			int packetLength = packet.getLength();

			int from = 0;
			while (from < packetLength) {
				int type = UplinkMessage.getUplinkMessageType(packetData, from);
				int length = UplinkMessage.getUplinkMessageHeaderLength()
						+ UplinkMessage
								.getUplinkMessageLength(packetData, from);

				byte[] data1 = Arrays.copyOfRange(packetData, from, from
						+ length);
				Util.dumpUplinkMessage(logger, Level.DEBUG, data1, packet,
						type, utcTimestamp);
				if (type == UplinkMessage.getUplinkMessageTypePosition()) {
					PositionUpdate pu = new PositionUpdate(data1, length);
					updated |= positionUpdateHandler.handlePositionMessage(
							utcTimestamp, pu, relayServer);
					fakeit(MSGTYPE.PU, utcTimestamp, pu, relayServer);
				} else if (type == PositionUpdate
						.getUplinkMessageTypeClusterLeader()) {
					ClusterLeader cl = new ClusterLeader(data1, length);
					updated |= clusterLeaderHandler.handleClusterLeaderMessage(
							utcTimestamp, cl, relayServer);
					fakeit(MSGTYPE.CL, utcTimestamp, cl, relayServer);
				} else {
					logger.warn("Uplink message type " + type
							+ " not supported");
				}

				from += length;
			}

			return updated;
		} finally {
			config.getDataLock().unlock();
			firstFake = false;
		}
	}
}
