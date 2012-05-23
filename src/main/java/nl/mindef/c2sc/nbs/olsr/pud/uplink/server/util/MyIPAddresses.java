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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores the IP addresses of all network interfaces of the machine upon classload
 */
public class MyIPAddresses {
	private static final Set<InetAddress> myIpAddresses = new HashSet<InetAddress>();

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

	@SuppressWarnings("static-method")
	public boolean isMe(InetAddress ip) {
		return myIpAddresses.contains(ip);
	}
}
