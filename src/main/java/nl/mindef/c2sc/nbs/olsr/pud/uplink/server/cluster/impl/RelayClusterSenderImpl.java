package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.cluster.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.cluster.RelayClusterSender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class RelayClusterSenderImpl extends Thread implements RelayClusterSender, StopHandlerConsumer {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private int relayClusterUdpPort;

	/**
	 * @param relayClusterUdpPort
	 *          the relayClusterUdpPort to set
	 */
	@Required
	public final void setRelayClusterUdpPort(int relayClusterUdpPort) {
		this.relayClusterUdpPort = relayClusterUdpPort;
	}

	private RelayServers relayServers;

	/**
	 * @param relayServers
	 *          the relayServers to set
	 */
	@Required
	public final void setRelayServers(RelayServers relayServers) {
		this.relayServers = relayServers;
	}

	/*
	 * Queue
	 */

	private LinkedBlockingQueue<RelayClusterMessage> packetsToSend = new LinkedBlockingQueue<RelayClusterMessage>();

	@Override
	public boolean addPacket(RelayServer relayServer, DatagramPacket packet) {
		RelayClusterMessage rcm = new RelayClusterMessage(relayServer, packet);
		int tries = 0;
		while (tries < 10) {
			tries++;
			if (this.packetsToSend.offer(rcm)) {
				return true;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				/* swallow */
			}
		}
		return false;
	}

	/*
	 * Distribution
	 */

	private void distribute(List<RelayClusterMessage> relayClusterMessages) {
		List<RelayServer> otherRelayServers = this.relayServers.getOtherRelayServers();
		if ((otherRelayServers == null) || (otherRelayServers.size() == 0)) {
			return;
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("*** distribute " + relayClusterMessages.size() + " packet(s) to " + otherRelayServers.size()
					+ " other relay servers");
		}

		RelayServer me = this.relayServers.getMe();

		InetAddress otherRelayServerIp = null;
		int otherRelayServerPort = -1;
		DatagramPacket p2s = null;
		for (RelayClusterMessage relayClusterMessage : relayClusterMessages) {
			p2s = relayClusterMessage.toWireFormat();
			if (p2s == null) {
				this.logger.error("Could not convert RelayClusterMessage to wire format");
				continue;
			}

			RelayServer relayServerOriginator = relayClusterMessage.getRelayServer();
			for (RelayServer otherRelayServer : otherRelayServers) {
				otherRelayServerIp = otherRelayServer.getIp();
				otherRelayServerPort = otherRelayServer.getPort().intValue();

				/* do not send to relay server from which the message came and also not to myself */
				if (!(otherRelayServerIp.equals(relayServerOriginator.getIp()) && (otherRelayServerPort == relayServerOriginator
						.getPort().intValue()))
						&& !(otherRelayServerIp.equals(me.getIp()) && (otherRelayServerPort == this.relayClusterUdpPort))) {
					p2s.setAddress(otherRelayServerIp);
					p2s.setPort(otherRelayServerPort);
					try {
						if (this.logger.isDebugEnabled()) {
							this.logger.debug("sending packet to RelayServer " + otherRelayServerIp.getHostAddress() + ":"
									+ otherRelayServerPort);
						}
						this.sock.send(p2s);
					} catch (IOException e) {
						this.logger.error("Could not send to relay server " + otherRelayServerIp.getHostAddress() + ":"
								+ otherRelayServerPort, e);
					}
				} else {
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("skip sending packet to RelayServer " + relayServerOriginator.getIp().getHostAddress()
								+ ":" + relayServerOriginator.getPort() + " since the packet came from there, or it's me");
					}
				}
			}
		}
	}

	/*
	 * Main
	 */

	private DatagramSocket sock;
	private AtomicBoolean run = new AtomicBoolean(true);

	public void init() throws SocketException {
		this.setName(this.getClass().getSimpleName());
		this.sock = new DatagramSocket(null);
		this.sock.setReuseAddress(true);
		this.sock.bind(new InetSocketAddress(this.relayClusterUdpPort));
		this.start();
	}

	public void uninit() {
		this.packetsToSend.clear();
		this.run.set(false);
		this.interrupt();
	}

	@Override
	public void run() {
		while (this.run.get()) {
			RelayClusterMessage relayClusterMessage = null;
			try {
				relayClusterMessage = this.packetsToSend.take();
			} catch (InterruptedException e) {
				/* swallow */
			}
			if (relayClusterMessage != null) {
				List<RelayClusterMessage> relayClusterMessages = new LinkedList<RelayClusterMessage>();
				relayClusterMessages.add(relayClusterMessage);
				this.packetsToSend.drainTo(relayClusterMessages);
				distribute(relayClusterMessages);
			}
		}
		this.sock.close();
		this.sock = null;
	}

	@Override
	public void signalStop() {
		uninit();
	}
}
