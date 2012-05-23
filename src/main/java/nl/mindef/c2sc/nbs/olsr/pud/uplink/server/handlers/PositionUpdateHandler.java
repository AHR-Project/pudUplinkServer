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

import org.olsr.plugin.pud.PositionUpdate;

public interface PositionUpdateHandler {
	/**
	 * Process a PositionUpdate message and save it into the database
	 * 
	 * @param sender
	 *          the sender from which the message was received
	 * @param utcTimestamp
	 *          the timestamp on which the message was received
	 * @param puMsg
	 *          the PositionUpdate message
	 * @return true when the data in the message resulted in a database update
	 */
	public boolean handlePositionMessage(Sender sender, long utcTimestamp, PositionUpdate puMsg);
}
