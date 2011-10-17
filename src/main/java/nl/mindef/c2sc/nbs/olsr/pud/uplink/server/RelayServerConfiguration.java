package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class RelayServerConfiguration {
	static public final int UDP_PORT_DEFAULT = 2241;

	/** the UDP port to listen on */
	private int udpPort = UDP_PORT_DEFAULT;

	/**
	 * @return the udpPort
	 */
	public final int getUdpPort() {
		return udpPort;
	}

	/**
	 * @param udpPort
	 *            the udpPort to set
	 */
	public final void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}

	private ReentrantLock dataLock = new ReentrantLock();

	/**
	 * @return the dataLock
	 */
	public final ReentrantLock getDataLock() {
		return dataLock;
	}

	private List<InetAddress> addrList = new ArrayList<InetAddress>();

	public boolean isMe(InetAddress ip) {
		return addrList.contains(ip);
	}

	void init() throws SocketException {
		Enumeration<NetworkInterface> ifs = NetworkInterface
				.getNetworkInterfaces();
		while (ifs.hasMoreElements()) {
			NetworkInterface ifc = ifs.nextElement();
			if (ifc.isUp()) {
				Enumeration<InetAddress> ips = ifc.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress addr = ips.nextElement();
					addrList.add(addr);
				}
			}
		}
	}
}
