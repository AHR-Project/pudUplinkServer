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

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The ClusterLeaderMsg DAO
 */
public interface ClusterLeaderMsgs {
	/**
	 * Save a ClusterLeaderMsg into the database
	 * 
	 * @param clusterLeaderMsg
	 *          the ClusterLeaderMsg
	 */
	public void saveClusterLeaderMsg(ClusterLeaderMsg clusterLeaderMsg);

	/**
	 * Remove expired/out-of-date ClusterLeaderMsg objects from the database
	 * 
	 * @param utcTimestamp
	 *          the timestamp for which expiry must be evaluated
	 * @param validityTimeMultiplier
	 *          the multiplier that must be applied to the validity time of a ClusterLeaderMsg before evaluating expiry
	 * @return true when 1 or more ClusterLeaderMsg objects were removed from the database
	 */
	public boolean removeExpiredClusterLeaderMsg(long utcTimestamp, double validityTimeMultiplier);

	/**
	 * Log the printout of the ClusterLeaderMsg objects
	 * 
	 * @param logger
	 *          the logger to which the printout is sent
	 * @param level
	 *          the level at which the printout must be logged. no logging is performed when the logger is not enabled for
	 *          the specified level.
	 */
	public void log(Logger logger, Level level);

	/**
	 * Print the ClusterLeaderMsg objects to an output stream
	 * 
	 * @param out
	 *          the output stream to which the printout is sent
	 * @throws IOException
	 *           in case of an error
	 */
	public void print(OutputStream out) throws IOException;
}
