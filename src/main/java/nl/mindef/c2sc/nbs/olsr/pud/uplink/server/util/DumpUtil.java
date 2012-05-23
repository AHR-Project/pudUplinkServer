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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.olsr.plugin.pud.ClusterLeader;
import org.olsr.plugin.pud.PositionUpdate;
import org.olsr.plugin.pud.UplinkMessage;

public class DumpUtil {
	public static void dumpUplinkMessage(Logger logger, Level level, byte[] data, InetAddress ip, int port, int type,
			long utcTimestamp, String indent) {
		if (!logger.isEnabledFor(level)) {
			return;
		}

		UplinkMessage ul;
		if (type == UplinkMessage.getUplinkMessageTypePosition()) {
			ul = new PositionUpdate(data, data.length);
		} else if (type == UplinkMessage.getUplinkMessageTypeClusterLeader()) {
			ul = new ClusterLeader(data, data.length);
		} else {
			logger.log(level, "Unknown uplink message type: " + type);
			return;
		}

		StringBuilder s = new StringBuilder();

		s.append("\n");
		s.append(indent + "*** UplinkMessage ***\n");
		s.append(indent + "data   =");
		for (int index = 0; index < data.length; index++) {
			if ((index % 8) == 0) {
				if ((index % 16) == 0) {
					if (index != 0) {
						s.append("\n" + indent + "        ");
					}
				} else {
					s.append("  ");
				}
			}
			s.append(String.format(" %02x", Byte.valueOf(data[index])));
		}
		s.append("\n");
		s.append(String.format(indent + "sender = %s:%d\n", ip.getHostAddress(), Integer.valueOf(port)));
		s.append(String.format(indent + "size   = %d bytes\n", Integer.valueOf(data.length)));

		s.append(indent + "  *** UplinkHeader ***\n");
		s.append(String.format(indent + "  type   = %d\n", Integer.valueOf(type)));
		s.append(String.format(indent + "  length = %d bytes\n", Integer.valueOf(ul.getUplinkMessageLength())));
		s.append(String.format(indent + "  ipv6   = %b\n", Boolean.valueOf(ul.isUplinkMessageIPv6())));

		if (type == UplinkMessage.getUplinkMessageTypePosition()) {
			PositionUpdate pu = (PositionUpdate) ul;

			s.append(indent + "    *** OLSR header ***\n");

			s.append(String.format(indent + "    originator   = %s\n", pu.getOlsrMessageOriginator().getHostAddress()));

			s.append(indent + "    *** PudOlsrPositionUpdate ***\n");
			s.append(String.format(indent + "    version      = %d\n", Integer.valueOf(pu.getPositionUpdateVersion())));
			s.append(String.format(indent + "    validity     = %d sec\n", Long.valueOf(pu.getPositionUpdateValidityTime())));
			s.append(String.format(indent + "    smask        = 0x%02x\n", Integer.valueOf(pu.getPositionUpdateSMask())));
			s.append(String.format(indent + "    flags        = 0x%02x\n", Integer.valueOf(pu.getPositionUpdateFlags())));

			s.append(indent + "      *** GpsInfo ***\n");
			s.append(String.format(indent + "      time       = %d\n",
					Long.valueOf(pu.getPositionUpdateTime(utcTimestamp, TimeZoneUtil.getTimezoneOffset()))));
			s.append(String.format(indent + "      lat        = %f\n",
					Double.valueOf(NmeaUtil.nmeaDeg2Ndeg(pu.getPositionUpdateLatitude()))));
			s.append(String.format(indent + "      lon        = %f\n",
					Double.valueOf(NmeaUtil.nmeaDeg2Ndeg(pu.getPositionUpdateLongitude()))));
			s.append(String.format(indent + "      alt        = %d m\n", Long.valueOf(pu.getPositionUpdateAltitude())));
			s.append(String.format(indent + "      speed      = %d kph\n", Long.valueOf(pu.getPositionUpdateSpeed())));
			s.append(String.format(indent + "      track      = %d deg\n", Long.valueOf(pu.getPositionUpdateTrack())));
			s.append(String.format(indent + "      hdop       = %f\n", Double.valueOf(pu.getPositionUpdateHdop())));

			s.append(indent + "      *** NodeInfo ***\n");
			s.append(String.format(indent + "      nodeIdType = %d\n", Integer.valueOf(pu.getPositionUpdateNodeIdType())));
			s.append(String.format(indent + "      nodeId     = %s\n", pu.getPositionUpdateNodeId()));
		} else if (type == UplinkMessage.getUplinkMessageTypeClusterLeader()) {
			ClusterLeader cl = (ClusterLeader) ul;

			s.append(indent + "    *** UplinkClusterLeader ***\n");
			s.append(String.format(indent + "    version       = %d\n", Integer.valueOf(cl.getClusterLeaderVersion())));
			s.append(String.format(indent + "    validity      = %d sec\n", Long.valueOf(cl.getClusterLeaderValidityTime())));
			s.append(String.format(indent + "    originator    = %s\n", cl.getClusterLeaderOriginator().getHostAddress()));
			s.append(String.format(indent + "    clusterLeader = %s\n", cl.getClusterLeaderClusterLeader().getHostAddress()));
		}

		logger.log(level, s.toString());
	}
}
