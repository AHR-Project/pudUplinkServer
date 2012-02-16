package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.cluster.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportOnce;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportSubject;

import org.apache.log4j.Logger;

public class RelayClusterMessage {
	private static Logger logger = Logger.getLogger(RelayClusterMessage.class.getName());

	public RelayClusterMessage(RelayServer relayServer, DatagramPacket packet) {
		super();
		this.relayServer = relayServer;
		this.ip = packet.getAddress();
		this.port = packet.getPort();
		this.data = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getOffset() + packet.getLength());
		assert (this.relayServer != null);
		assert (this.ip != null);
		assert (this.port >= 0 && this.port <= 65535);
		assert (this.data != null);
		assert (this.data.length <= 0xfff);

		if (this.data.length > 0xffff) {
			throw new IllegalStateException("Packet too large (" + this.data.length + " bytes)");
		}
	}

	private RelayServer relayServer;

	/**
	 * @return the relayServer
	 */
	public final RelayServer getRelayServer() {
		return this.relayServer;
	}

	private InetAddress ip = null;

	/**
	 * @return the ip
	 */
	public final InetAddress getIp() {
		return this.ip;
	}

	private int port = -1;

	/**
	 * @return the port
	 */
	public final int getPort() {
		return this.port;
	}

	private byte[] data = null;

	/**
	 * @return the data
	 */
	public final byte[] getData() {
		return this.data;
	}

	private static final int VERSION = 0x00;
	private static final int MAGIC = 0xb6;
	private static final int MAGIC4 = MAGIC & 0xFE;
	private static final int MAGIC6 = MAGIC | 0x01;

	public DatagramPacket toWireFormat() {
		int headerLength = 1 /* version */+ 1 /* ipType */+ ((this.ip instanceof Inet4Address) ? 4 : 16) /* IP address */
				+ 2 /* port */+ 2 /* length */;

		ByteArrayOutputStream baos = new ByteArrayOutputStream(headerLength + this.data.length);
		try {
			/* version / 1 byte */
			baos.write(VERSION);

			/* IP address type / 1 byte */
			if (this.ip instanceof Inet4Address) {
				baos.write(MAGIC4);
			} else /* if (this.ip instanceof Inet6Address) */{
				baos.write(MAGIC6);
			}

			/* IP address / 4 or 16 bytes */
			baos.write(this.ip.getAddress());

			/* port / 2 bytes */
			baos.write((this.port & 0xff00) >> 8);
			baos.write(this.port & 0x00ff);

			/* length / 2 bytes */
			int length = this.data.length;
			baos.write((length & 0xff00) >> 8);
			baos.write(length & 0x00ff);

			/* data / variable number of bytes */
			baos.write(this.data);

			/* flush out data */
			baos.flush();

			/* get bytes */
			byte[] ba = baos.toByteArray();

			return new DatagramPacket(ba, ba.length);
		} catch (IOException e) {
			logger.error(e);
			return null;
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				/* ignore */
			}
		}
	}

	public static DatagramPacket fromWireFormat(DatagramPacket packet, ReportOnce reportOnce) {
		assert (packet != null);

		ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
		try {
			/* we need at least version and ipType */
			if (bais.available() < 2) {
				return null;
			}

			String senderReport = packet.getAddress().getHostAddress().toString() + ":" + packet.getPort();

			/* read version */
			int version = bais.read();

			if (version != VERSION) {
				if (reportOnce.add(ReportSubject.CLUSTER_WIRE_FORMAT, senderReport, "version")) {
					logger.warn("Received cluster message version " + version + " (expected version " + VERSION + ") from "
							+ senderReport + ", relay server will be ignored");
				}
				return null;
			}

			if (reportOnce.remove(ReportSubject.CLUSTER_WIRE_FORMAT, senderReport, "version")) {
				logger.error("Received correct cluster message version from " + senderReport
						+ ", relay server will no longer be ignored");
			}

			/* read ipType */
			int ipType = bais.read();

			/* determine IP address size */
			int ipBytesLength = 0;
			if (ipType == MAGIC4) {
				ipBytesLength = 4;
			} else if (ipType == MAGIC6) {
				ipBytesLength = 16;
			} else {
				if (reportOnce.add(ReportSubject.CLUSTER_MESSAGE_TYPE, senderReport, "type")) {
					logger.warn("Received wrong cluster message type " + ipType + " from " + senderReport
							+ ", relay server will be ignored");
				}
				return null;
			}

			if (reportOnce.remove(ReportSubject.CLUSTER_MESSAGE_TYPE, senderReport, "type")) {
				logger.error("Received correct cluster message type from " + senderReport
						+ ", relay server will no longer be ignored");
			}

			/* we need at least IP, port, and length */
			if (bais.available() < (ipBytesLength + 2 + 2)) {
				return null;
			}

			/* read IP address */
			byte[] ipBytes = new byte[ipBytesLength];
			bais.read(ipBytes);
			InetAddress ip = InetAddress.getByAddress(ipBytes);

			/* read port */
			int port = (bais.read() << 8) + bais.read();

			/* read length */
			int length = (bais.read() << 8) + bais.read();

			/* we need at least length bytes */
			if (bais.available() < length) {
				return null;
			}

			/* data */
			byte[] data = new byte[length];
			bais.read(data);

			return new DatagramPacket(data, data.length, ip, port);
		} catch (IOException e) {
			logger.error(e);
			return null;
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
				/* ignore */
			}
		}
	}
}
