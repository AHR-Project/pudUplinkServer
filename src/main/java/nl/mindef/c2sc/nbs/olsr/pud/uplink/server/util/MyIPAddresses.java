package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

public class MyIPAddresses {
	private static final Set<InetAddress> myIpAddresses = new TreeSet<InetAddress>();

	static {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				if (networkInterface.isUp()) {
					Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
					while (ipAddresses.hasMoreElements()) {
						InetAddress ipAddress = ipAddresses.nextElement();
						myIpAddresses.add(ipAddress);
					}
				}
			}
		} catch (SocketException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public boolean isMe(InetAddress ip) {
		return myIpAddresses.contains(ip);
	}
}
