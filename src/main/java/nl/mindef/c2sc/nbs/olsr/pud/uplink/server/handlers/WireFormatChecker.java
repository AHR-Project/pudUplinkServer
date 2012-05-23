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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;

import org.olsr.plugin.pud.UplinkMessage;

public interface WireFormatChecker {
	/**
	 * Checks whether whether the uplink message can be processed (whether the wire format version of the uplink message
	 * is one that is compatible with the RelayServer)
	 * 
	 * @param sender
	 *          the sender of the uplink message
	 * @param msg
	 *          the uplink message
	 * @return true when the uplink message can be processed (wireformat matches expected version)
	 */
	public boolean checkUplinkMessageWireFormat(Sender sender, UplinkMessage msg);
}
