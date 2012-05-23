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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The RelayServer DAO
 */
public interface RelayServers {
	/**
	 * Get the RelayServer with the specified IP address and UDP port, or create a new one and return that one.
	 * 
	 * @param ip
	 *          the IP address
	 * @param port
	 *          the UDP port
	 * @return the RelayServer with the specified IP address and UDP port
	 */
	public RelayServer getOrAdd(InetAddress ip, int port);

	/**
	 * Retrieve a list of other (not me) RelayServer objects from the database
	 * 
	 * @return a list of all other RelayServer objects, or null when none found
	 */
	public List<RelayServer> getOtherRelayServers();

	/**
	 * Save a RelayServer into the database
	 * 
	 * @param relayServer
	 *          the RelayServer
	 */
	public void addRelayServer(RelayServer relayServer);

	/**
	 * Retrieve the RelayServer object that represent this RelayServer from the database (it will be created when not
	 * found)
	 * 
	 * @return the RelayServer object
	 */
	public RelayServer getMe();

	/**
	 * Log the printout of the RelayServer objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the RelayServer objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}
