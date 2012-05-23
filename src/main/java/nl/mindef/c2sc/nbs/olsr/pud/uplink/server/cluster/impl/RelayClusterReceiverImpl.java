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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.cluster.impl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.cluster.RelayClusterReceiver;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.cluster.RelayClusterSender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PacketHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportOnce;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.uplink.UplinkReceiver;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class RelayClusterReceiverImpl extends Thread implements RelayClusterReceiver, StopHandlerConsumer {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private boolean relayClusterForwarding;

	/**
	 * @param relayClusterForwarding
	 *          the relayClusterForwarding to set
	 */
	@Required
	public final void setRelayClusterForwarding(boolean relayClusterForwarding) {
		this.relayClusterForwarding = relayClusterForwarding;
	}

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

	private PacketHandler packetHandler;

	/**
	 * @param packetHandler
	 *          the packetHandler to set
	 */
	@Required
	public final void setPacketHandler(PacketHandler packetHandler) {
		this.packetHandler = packetHandler;
	}

	private RelayClusterSender relayClusterSender;

	/**
	 * @param relayClusterSender
	 *          the relayClusterSender to set
	 */
	@Required
	public final void setRelayClusterSender(RelayClusterSender relayClusterSender) {
		this.relayClusterSender = relayClusterSender;
	}

	private ReportOnce reportOnce;

	/**
	 * @param reportOnce
	 *          the reportOnce to set
	 */
	@Required
	public final void setReportOnce(ReportOnce reportOnce) {
		this.reportOnce = reportOnce;
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
		this.run.set(false);
		if (this.sock != null) {
			/* this is crude but effective */
			this.sock.close();
			this.sock = null;
		}
	}

	@Override
	public void run() {
		byte[] receiveBuffer = new byte[UplinkReceiver.BUFFERSIZE];
		DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

		while (this.run.get()) {
			try {
				this.sock.receive(packet);

				InetAddress ip = packet.getAddress();
				int port = packet.getPort();
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("received packet from RelayServer " + ip.getHostAddress() + ":" + port);
				}

				RelayServer me = this.relayServers.getMe();
				if (!(ip.equals(me.getIp()) && (port != this.relayClusterUdpPort))) {
					DatagramPacket msg = RelayClusterMessage.fromWireFormat(packet, this.reportOnce);
					if (msg != null) {
						RelayServer rs = this.relayServers.getOrAdd(ip, port);
						boolean packetCausedUpdate = this.packetHandler.processPacket(rs, msg);
						if (this.relayClusterForwarding && packetCausedUpdate) {
							if (this.logger.isDebugEnabled()) {
								this.logger.debug("forwarding packet from RelayServer " + ip.getHostAddress() + ":" + port);
							}
							this.relayClusterSender.addPacket(rs, msg);
						}
					} else {
						this.logger.error("Could not convert RelayClusterMessage from wire format");
					}
				} else {
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("skipping packet from RelayServer " + ip.getHostAddress() + ":" + port
								+ " since the packet came from me");
					}
				}
			} catch (Exception e) {
				if (!SocketException.class.equals(e.getClass())) {
					e.printStackTrace();
				}
			}
		}

		if (this.sock != null) {
			this.sock.close();
			this.sock = null;
		}
	}

	@Override
	public void signalStop() {
		uninit();
	}
}
