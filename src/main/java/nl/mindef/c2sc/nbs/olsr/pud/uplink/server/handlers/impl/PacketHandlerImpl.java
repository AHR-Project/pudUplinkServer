package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Gateways;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.ClusterLeaderHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PacketHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PositionUpdateHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl.debug.Faker;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util.DumpUtil;

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

	private PositionUpdateHandler positionUpdateHandler;

	/**
	 * @param positionUpdateHandler
	 *          the positionUpdateHandler to set
	 */
	@Required
	public final void setPositionUpdateHandler(PositionUpdateHandler positionUpdateHandler) {
		this.positionUpdateHandler = positionUpdateHandler;
	}

	private ClusterLeaderHandler clusterLeaderHandler;

	/**
	 * @param clusterLeaderHandler
	 *          the clusterLeaderHandler to set
	 */
	@Required
	public final void setClusterLeaderHandler(ClusterLeaderHandler clusterLeaderHandler) {
		this.clusterLeaderHandler = clusterLeaderHandler;
	}

	private Gateways gateways = null;

	/**
	 * @param gateways
	 *          the gateways to set
	 */
	@Required
	public final void setGateways(Gateways gateways) {
		this.gateways = gateways;
	}

	private RelayServers relayServers = null;

	/**
	 * @param relayServers
	 *          the relayServers to set
	 */
	@Required
	public final void setRelayServers(RelayServers relayServers) {
		this.relayServers = relayServers;
	}

	/*
	 * Fake data
	 */

	private boolean useFaker = false;

	/**
	 * @param useFaker
	 *          the useFaker to set
	 */
	public final void setUseFaker(boolean useFaker) {
		this.useFaker = useFaker;
	}

	private Faker faker = null;

	/**
	 * @param faker
	 *          the faker to set
	 */
	@Required
	public final void setFaker(Faker faker) {
		this.faker = faker;
	}

	/*
	 * end fake
	 */

	private ReentrantLock dataLock;

	/**
	 * @param dataLock
	 *          the dataLock to set
	 */
	@Required
	public final void setDataLock(ReentrantLock dataLock) {
		this.dataLock = dataLock;
	}

	@Override
	@Transactional
	public boolean processPacket(DatagramPacket packet) {
		long utcTimestamp = System.currentTimeMillis();
		boolean updated = false;

		if (logger.isDebugEnabled()) {
			logger.debug("[" + utcTimestamp + "] Received " + packet.getLength() + " bytes from "
					+ packet.getAddress().getHostAddress() + ":" + packet.getPort());
		}

		dataLock.lock();
		try {
			InetAddress srcIp = packet.getAddress();
			int srcPort = packet.getPort();

			RelayServer me = relayServers.getMe();

			/* get gateway node or create */
			Gateway gateway = gateways.getGateway(srcIp, srcPort);
			if (gateway == null) {
				gateway = new Gateway(srcIp, srcPort, me);
				gateways.saveGateway(gateway);
			}

			/* make sure the gateway is linked to this relayServer */
			gateway.setRelayServer(me);

			/* get packet data */
			byte[] packetData = packet.getData();
			int packetLength = packet.getLength();

			/* parse the uplink packet for messages */
			int messageOffset = 0;
			while (messageOffset < packetLength) {
				int messageType = UplinkMessage.getUplinkMessageType(packetData, messageOffset);
				int messageLength = UplinkMessage.getUplinkMessageHeaderLength()
						+ UplinkMessage.getUplinkMessageLength(packetData, messageOffset);

				byte[] messageData = Arrays.copyOfRange(packetData, messageOffset, messageOffset + messageLength);
				DumpUtil.dumpUplinkMessage(logger, Level.DEBUG, messageData, srcIp, srcPort, messageType, utcTimestamp, "  ");
				if (messageType == UplinkMessage.getUplinkMessageTypePosition()) {
					PositionUpdate pu = new PositionUpdate(messageData, messageLength);
					updated = positionUpdateHandler.handlePositionMessage(gateway, utcTimestamp, pu) || updated;
					if (useFaker) {
						faker.fakeit(gateway, utcTimestamp, Faker.MSGTYPE.PU, pu);
					}
				} else if (messageType == PositionUpdate.getUplinkMessageTypeClusterLeader()) {
					ClusterLeader cl = new ClusterLeader(messageData, messageLength);
					updated = clusterLeaderHandler.handleClusterLeaderMessage(gateway, utcTimestamp, cl) || updated;
					if (useFaker) {
						faker.fakeit(gateway, utcTimestamp, Faker.MSGTYPE.CL, cl);
					}
				} else {
					logger.warn("Uplink message type " + messageType + " not supported: ignored");
				}

				messageOffset += messageLength;
			}

			return updated;
		} finally {
			dataLock.unlock();
			if (useFaker) {
				faker.setFirstFake(false);
			}
		}
	}
}
