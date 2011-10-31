package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao;

import java.net.InetAddress;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.NodePosition;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public interface Positions {
	public NodePosition getPosition(InetAddress mainIp);

	public List<NodePosition> getPositionsForDistribution(long startTime,
			long endTime, Node clusterLeader);

	public void saveNodePosition(NodePosition position, boolean newObject);

	public void removeExpiredNodePosition(double validityTimeMultiplier);

	public void log(Logger logger, Level level);
}
