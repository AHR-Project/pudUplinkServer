package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.uplink;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.Distributor;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PacketHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger.DatabaseLogger;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class UplinkReceiver extends Thread implements StopHandlerConsumer {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	static private int BUFFERSIZE = 16 * 1024; /* 16KB */

	/** the UDP port to listen on for uplink messages */
	private Integer uplinkUdpPort = null;

	/**
	 * @param uplinkUdpPort
	 *          the uplinkUdpPort to set
	 */
	@Required
	public final void setUplinkUdpPort(int uplinkUdpPort) {
		this.uplinkUdpPort = uplinkUdpPort;
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

	private Distributor distributor;

	/**
	 * @param distributor
	 *          the distributor to set
	 */
	@Required
	public final void setDistributor(Distributor distributor) {
		this.distributor = distributor;
	}

	private DatabaseLogger databaseLogger;

	/**
	 * @param databaseLogger
	 *          the databaseLogger to set
	 */
	@Required
	public final void setDatabaseLogger(DatabaseLogger databaseLogger) {
		this.databaseLogger = databaseLogger;
	}

	/*
	 * Main
	 */

	private DatagramSocket sock = null;
	private AtomicBoolean run = new AtomicBoolean(true);

	public void init() throws SocketException {
		this.setName(this.getClass().getSimpleName());
		sock = new DatagramSocket(uplinkUdpPort);
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
	 *           when the socket could not be created
	 */
	@Override
	public void run() {
		byte[] receiveBuffer = new byte[BUFFERSIZE];
		DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

		while (run.get()) {
			try {
				sock.receive(packet);
				try {
					if (packetHandler.processPacket(packet)) {
						databaseLogger.log(logger, Level.DEBUG);
						distributor.signalUpdate();
					}
				} catch (Throwable e) {
					logger.error(e);
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
	public void signalStop() {
		run.set(false);
		if (sock != null) {
			/* this is crude but effective */
			sock.close();
		}
	}
}
