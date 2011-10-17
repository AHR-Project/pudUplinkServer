package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.DatagramPacket;
import java.util.Arrays;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.RelayServerConfiguration;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PacketHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PositionUpdateHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl.debug.Faker;
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

	private boolean useFaker = false;

	/**
	 * @param useFaker
	 *            the useFaker to set
	 */
	public final void setUseFaker(boolean useFaker) {
		this.useFaker = useFaker;
	}

	private Faker faker = null;

	/**
	 * @param faker
	 *            the faker to set
	 */
	@Required
	public final void setFaker(Faker faker) {
		this.faker = faker;
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
					if (useFaker) {
						faker.fakeit(Faker.MSGTYPE.PU, utcTimestamp, pu,
								relayServer);
					}
				} else if (type == PositionUpdate
						.getUplinkMessageTypeClusterLeader()) {
					ClusterLeader cl = new ClusterLeader(data1, length);
					updated |= clusterLeaderHandler.handleClusterLeaderMessage(
							utcTimestamp, cl, relayServer);
					if (useFaker) {
						faker.fakeit(Faker.MSGTYPE.CL, utcTimestamp, cl,
								relayServer);
					}
				} else {
					logger.warn("Uplink message type " + type
							+ " not supported");
				}

				from += length;
			}

			return updated;
		} finally {
			config.getDataLock().unlock();
			if (useFaker) {
				faker.setFirstFake(false);
			}
		}
	}
}
