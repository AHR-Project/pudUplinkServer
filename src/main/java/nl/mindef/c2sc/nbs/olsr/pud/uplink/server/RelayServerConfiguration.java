package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class RelayServerConfiguration {
	static public final int UPLINK_UDP_PORT_DEFAULT = 2241;

	/** the UDP port to listen on for uplink messages */
	private int uplinkUdpPort = UPLINK_UDP_PORT_DEFAULT;

	/**
	 * @return the uplinkUdpPort
	 */
	public final int getUplinkUdpPort() {
		return uplinkUdpPort;
	}

	/**
	 * @param uplinkUdpPort
	 *            the uplinkUdpPort to set
	 */
	public final void setUplinkUdpPort(int uplinkUdpPort) {
		this.uplinkUdpPort = uplinkUdpPort;
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
