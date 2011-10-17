package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Positions;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PacketHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.relaycluster.RelayCluster;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class RelayServer extends Thread implements SignalHandler {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	static private int BUFFERSIZE = 4000;

	private RelayServerConfiguration config;

	/**
	 * @param config
	 *            the config to set
	 */
	@Required
	public final void setConfig(RelayServerConfiguration config) {
		this.config = config;
	}

	/** the Node handler */
	private Nodes nodes;

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	@Required
	public final void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the Positions handler */
	private Positions positions;

	/**
	 * @param positions
	 *            the positions to set
	 */
	@Required
	public final void setPositions(Positions positions) {
		this.positions = positions;
	}

	private RelayServers relayServers;

	/**
	 * @param relayServers
	 *            the relayServers to set
	 */
	@Required
	public final void setRelayServers(RelayServers relayServers) {
		this.relayServers = relayServers;
	}

	private PacketHandler packetHandler;

	/**
	 * @param packetHandler
	 *            the packetHandler to set
	 */
	@Required
	public final void setPacketHandler(PacketHandler packetHandler) {
		this.packetHandler = packetHandler;
	}

	private RelayCluster relayCluster;

	/**
	 * @param relayCluster
	 *            the relayCluster to set
	 */
	@Required
	public final void setRelayCluster(RelayCluster relayCluster) {
		this.relayCluster = relayCluster;
	}

	/*
	 * Main
	 */

	private DatagramSocket sock = null;

	private AtomicBoolean run = new AtomicBoolean(true);

	public void init() {
		this.start();
	}

	public void destroy() {
		run.set(false);
		synchronized (run) {
			run.notifyAll();
		}
	}

	/**
	 * Run the relay server.
	 * 
	 * @throws SocketException
	 *             when the socket could not be created
	 */
	@Override
	public void run() {
		this.setName(this.getClass().getSimpleName());
		try {
			sock = new DatagramSocket(config.getUdpPort());
		} catch (SocketException e1) {
			System.err.println("Can't bind to UDP port " + config.getUdpPort()
					+ ": " + e1.getMessage());
			return;
		}

		byte[] receiveBuffer = new byte[BUFFERSIZE];
		DatagramPacket packet = new DatagramPacket(receiveBuffer,
				receiveBuffer.length);

		while (run.get()) {
			try {
				sock.receive(packet);
				if (packetHandler.processPacket(packet, relayCluster.getMe())) {
					if (logger.isDebugEnabled()) {
						nodes.log(logger,Level.DEBUG);
						positions.log(logger,Level.DEBUG);
						relayServers.log(logger,Level.DEBUG);
					}

					relayCluster.signalUpdate();
				}
			} catch (Exception e) {
				if (!SocketException.class.equals(e.getClass())) {
					e.printStackTrace();
				}
			}
		}

		sock.close();
	}

	/*
	 * Signal Handling
	 */

	@Override
	public void handle(Signal signal) {
		run.set(false);
		if (sock != null) {
			/* this is crude but effective */
			sock.close();
		}
	}
}
