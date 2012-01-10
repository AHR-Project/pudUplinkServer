package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public interface PositionUpdateMsgs {
	public PositionUpdateMsg getPosition(InetAddress mainIp);

	public List<PositionUpdateMsg> getPositionsForDistribution(long startTime, long endTime, Node clusterLeader);

	public void saveNodePosition(PositionUpdateMsg position, boolean newObject);

	public boolean removeExpiredNodePosition(long utcTimestamp, double validityTimeMultiplier);

	public void log(Logger logger, Level level);

	public void print(OutputStream out) throws IOException;
}
