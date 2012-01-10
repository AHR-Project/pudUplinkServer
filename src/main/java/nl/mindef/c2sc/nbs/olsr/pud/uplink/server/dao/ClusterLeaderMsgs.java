package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public interface ClusterLeaderMsgs {
	public ClusterLeaderMsg getClusterLeader(InetAddress mainIp);

	public void saveClusterLeaderMsg(ClusterLeaderMsg position, boolean newObject);

	public boolean removeExpiredClusterLeaderMsg(long utcTimestamp, double validityTimeMultiplier);

	public void log(Logger logger, Level level);

	public void print(OutputStream out) throws IOException;
}
