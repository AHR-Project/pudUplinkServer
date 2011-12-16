package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Positions;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.NodePosition;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.Distributor;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util.MyIPAddresses;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DistributorImpl extends Thread implements Distributor {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public static final int packetMaxSizeDefault = 1500;

	private int packetMaxSize = packetMaxSizeDefault;

	/**
	 * @param packetMaxSize
	 *            the packetMaxSize to set
	 */
	public final void setPacketMaxSize(int packetMaxSize) {
		this.packetMaxSize = packetMaxSize;
	}

	private ReentrantLock dataLock;

	/**
	 * @param dataLock
	 *            the dataLock to set
	 */
	@Required
	public final void setDataLock(ReentrantLock dataLock) {
		this.dataLock = dataLock;
	}

	private MyIPAddresses myIPAddresses;

	/**
	 * @param myIPAddresses
	 *            the myIPAddresses to set
	 */
	@Required
	public final void setMyIPAddresses(MyIPAddresses config) {
		this.myIPAddresses = config;
	}

	/** the UDP port to listen on for uplink messages */
	private int uplinkUdpPort = RelayServer.PORT_DEFAULT;

	/**
	 * @param uplinkUdpPort
	 *            the uplinkUdpPort to set
	 */
	public final void setUplinkUdpPort(int uplinkUdpPort) {
		this.uplinkUdpPort = uplinkUdpPort;
	}

	/** the Node handler */
	private Nodes nodes;

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	@Required
	public final void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the Positions handler */
	private Positions positions;

	/**
	 * @param positions
	 *            the positions to set
	 */
	@Required
	public final void setPositions(Positions positions) {
		this.positions = positions;
	}

	private RelayServers relayServers;

	/**
	 * @param relayServers
	 *            the relayServers to set
	 */
	@Required
	public final void setRelayServers(RelayServers relayServers) {
		this.relayServers = relayServers;
	}

	public static final long DISTRIBUTION_DELAY_DEFAULT = 1000;
	private long distributionDelay = DISTRIBUTION_DELAY_DEFAULT;

	/**
	 * @param distributionDelay
	 *            the distributionDelay to set
	 */
	public final void setDistributionDelay(long distributionDelay) {
		this.distributionDelay = distributionDelay;
	}

	private Set<RelayServer> configuredRelayServers = new HashSet<RelayServer>();

	private static final String ipMatcher = "(\\d{1,3}\\.){0,3}\\d{1,3}";
	private static final String portMatcher = "\\d{1,5}";
	private static final String entryMatcher = "\\s*" + ipMatcher + "(:"
			+ portMatcher + ")?\\s*";
	private static final String matcher = "^\\s*" + entryMatcher + "(,"
			+ entryMatcher + ")*\\s*$";

	/**
	 * @param relayServers
	 *            the relayServers to set
	 * @throws UnknownHostException
	 *             upon error converting an IP address or host name to an
	 *             INetAddress
	 */
	@Required
	public final void setConfiguredRelayServers(String relayServers)
			throws UnknownHostException {
		if ((relayServers == null) || relayServers.trim().isEmpty()) {
			configuredRelayServers.clear();
			return;
		}

		if (!relayServers.matches(matcher)) {
			throw new IllegalArgumentException(
					"Configured relayServers string does not comply to"
							+ " regular expression \"" + matcher + "\"");
		}

		String[] splits = relayServers.split("\\s*,\\s*");
		for (String split : splits) {
			String[] fields = split.split(":", 2);

			InetAddress ip = InetAddress.getByName(fields[0].trim());

			RelayServer relayServer = new RelayServer();
			relayServer.setIp(ip);

			if (fields.length == 2) {
				Integer port = Integer.valueOf(fields[1].trim());
				if ((port <= 0) || (port > 65535)) {
					throw new IllegalArgumentException("Configured port "
							+ port + " for IP address " + ip.getHostAddress()
							+ " is outside valid range of [1, 65535]");
				}
				relayServer.setPort(port.intValue());
			}

			this.configuredRelayServers.add(relayServer);
		}
	}

	public void init() throws SocketException, UnknownHostException {
		this.setName(this.getClass().getSimpleName());
		
		this.configuredRelayServers.add(relayServers.getMe());

		/* save into database */
		for (RelayServer relayServer : configuredRelayServers) {
			relayServers.addRelayServer(relayServer);
		}

		timer = new Timer(this.getClass().getSimpleName() + "-Timer");

		sock = new DatagramSocket();

		this.start();
	}

	public void destroy() {
		run.set(false);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		synchronized (run) {
			run.notifyAll();
		}
	}

	private AtomicBoolean run = new AtomicBoolean(true);
	private AtomicBoolean distribute = new AtomicBoolean(false);

	@Override
	public void run() {
		while (run.get()) {
			synchronized (run) {
				try {
					run.wait();
				} catch (InterruptedException e) {
					/* swallow */
				}
			}
			if (distribute.getAndSet(false)) {
				dataLock.lock();
				try {
					distribute();
				} finally {
					dataLock.unlock();
				}
			}
		}
	}

	/*
	 * Distribution
	 */

	private Timer timer = null;
	private AtomicInteger signaledUpdates = new AtomicInteger(0);

	private long lastDistributionTime = 0;

	public void signalUpdate() {
		int previousSignaledUpdates = signaledUpdates.getAndIncrement();
		if (previousSignaledUpdates == 0) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					distribute.set(true);
					synchronized (run) {
						run.notifyAll();
					}
				}
			}, distributionDelay);
		}
	}

	private DatagramSocket sock;

	private DatagramPacket toPacket(Set<NodePosition> packetPositions,
			int packetPositionsByteCount) {
		assert (packetPositions != null);
		assert (packetPositions.size() > 0);
		assert (packetPositionsByteCount > 0);
		assert (packetPositionsByteCount <= packetMaxSize);

		DatagramPacket packet = new DatagramPacket(
				new byte[packetPositionsByteCount], packetPositionsByteCount);
		byte[] data = packet.getData();
		int dataPos = 0;
		for (NodePosition packetPosition : packetPositions) {
			byte[] src = packetPosition.getPositionUpdate().getData();
			int srcLength = src.length;
			System.arraycopy(src, 0, data, dataPos, srcLength);
			dataPos += srcLength;
		}

		return packet;
	}

	private Set<DatagramPacket> positionsToPackets(
			List<NodePosition> positionsToDistribute) {
		if ((positionsToDistribute == null)
				|| (positionsToDistribute.size() == 0)) {
			return null;
		}

		Set<DatagramPacket> result = new HashSet<DatagramPacket>();

		Set<NodePosition> packetPositions = new HashSet<NodePosition>();
		int packetPositionsByteCount = 0;
		for (NodePosition positionToDistribute : positionsToDistribute) {
			int msgLength = positionToDistribute.getPositionUpdate().getData().length;
			if ((packetPositionsByteCount + msgLength) > packetMaxSize) {
				result.add(toPacket(packetPositions, packetPositionsByteCount));
				packetPositions.clear();
				packetPositionsByteCount = 0;
			}

			packetPositions.add(positionToDistribute);
			packetPositionsByteCount += msgLength;
		}
		if (packetPositions.size() != 0) {
			result.add(toPacket(packetPositions, packetPositionsByteCount));
		}

		return result;
	}

	private void distribute() {
		while (signaledUpdates.get() > 0) {
			signaledUpdates.decrementAndGet();

			long currentTime = System.currentTimeMillis();

			StringBuilder s = new StringBuilder();

			if (logger.isDebugEnabled()) {
				logger.debug("Have to distribute [" + lastDistributionTime
						+ ", " + currentTime + "]");
			}

			/*
			 * Relay Servers
			 */

			if (logger.isDebugEnabled()) {
				logger.debug("  *** relay servers");
			}

			List<RelayServer> otherRelayServers = relayServers
					.getOtherRelayServers();

			if ((otherRelayServers != null) && (otherRelayServers.size() > 0)) {
				List<NodePosition> p4ds = positions
						.getPositionsForDistribution(lastDistributionTime,
								currentTime, null);
				if (logger.isDebugEnabled()) {
					s.setLength(0);
					s.append("  p4ds(" + p4ds.size() + ")=");
					for (NodePosition p4d : p4ds) {
						s.append(" " + p4d.getId());
					}
					logger.debug(s.toString());
				}

				Set<DatagramPacket> packets = positionsToPackets(p4ds);
				if ((packets != null) && (packets.size() > 0)) {
					for (RelayServer otherRelayServer : otherRelayServers) {
						InetAddress otherRelayServerIp = otherRelayServer
								.getIp();
						int otherRelayServerPort = otherRelayServer.getPort();

						s.setLength(0);
						if (logger.isDebugEnabled()) {
							s.append("  tx " + packets.size()
									+ " packet(s) to "
									+ otherRelayServerIp.getHostAddress() + ":"
									+ otherRelayServerPort + ", sizes=");
						}
						for (DatagramPacket packet : packets) {
							if (logger.isDebugEnabled()) {
								s.append(" " + packet.getLength());
							}
							packet.setAddress(otherRelayServerIp);
							packet.setPort(otherRelayServerPort);
							try {
								sock.send(packet);
							} catch (IOException e) {
								if (logger.isDebugEnabled()) {
									s.append(" ERROR:"
											+ e.getLocalizedMessage());
									logger.debug(s.toString());
									s.setLength(0);
								}
								logger.error("Could not send to relay server "
										+ otherRelayServerIp + ":"
										+ otherRelayServerPort);
							}
						}
						if (logger.isDebugEnabled()) {
							logger.debug(s.toString());
						}
					}
				}
			}

			/*
			 * Cluster Leaders
			 */

			List<Node> clusterLeaders = nodes.getClusterLeaders();
			if ((clusterLeaders != null) && (clusterLeaders.size() > 0)) {
				for (Node clusterLeader : clusterLeaders) {
					InetAddress clusterLeaderMainIp = clusterLeader.getMainIp();
					InetAddress clusterLeaderIp = clusterLeader.getIp();
					int clusterLeaderDownlinkPort = clusterLeader
							.getDownlinkPort();

					if ((clusterLeaderIp == null)
							|| (clusterLeaderDownlinkPort == Node.DOWNLINK_PORT_INVALID)) {
						Node substituteClusterLeader = nodes
								.getSubstituteClusterLeader(clusterLeader);

						if (substituteClusterLeader == null) {
							if (logger.isDebugEnabled()) {
								logger.debug("  *** cluster leader "
										+ clusterLeaderMainIp.getHostAddress()
										+ " has an invalid IP address and/or port"
										+ " and no substitute cluster leader:"
										+ " skipped");
							}

							continue;
						}

						clusterLeaderIp = substituteClusterLeader.getIp();
						clusterLeaderDownlinkPort = substituteClusterLeader
								.getDownlinkPort();
						if (logger.isDebugEnabled()) {
							logger.debug("  *** cluster leader "
									+ clusterLeaderMainIp.getHostAddress()
									+ " has an invalid IP address and/or port:"
									+ " selected substitute cluster leader "
									+ substituteClusterLeader.getMainIp()
											.getHostAddress());
						}
					}

					if (logger.isDebugEnabled()) {
						logger.debug("  *** cluster leader "
								+ clusterLeaderMainIp.getHostAddress() + ":"
								+ clusterLeaderDownlinkPort + " (ip="
								+ clusterLeaderIp.getHostAddress() + ")");
					}

					if ((myIPAddresses.isMe(clusterLeaderIp) || myIPAddresses
							.isMe(clusterLeaderMainIp))
							&& (clusterLeaderDownlinkPort == uplinkUdpPort)) {
						/* do not relay to ourselves */
						if (logger.isDebugEnabled()) {
							logger.debug("  this is me: skipping");
						}
						continue;
					}

					List<NodePosition> p4ds = positions
							.getPositionsForDistribution(lastDistributionTime,
									currentTime, clusterLeader);
					if ((p4ds == null) || (p4ds.size() == 0)) {
						if (logger.isDebugEnabled()) {
							logger.debug("  p4ds EMPTY");
						}
						continue;
					}

					if (logger.isDebugEnabled()) {
						s.setLength(0);
						s.append("  p4ds(" + p4ds.size() + ")=");
						for (NodePosition p4d : p4ds) {
							s.append(" " + p4d.getId());
						}
						logger.debug(s.toString());
					}

					Set<DatagramPacket> packets = positionsToPackets(p4ds);
					if ((packets != null) && (packets.size() > 0)) {
						s.setLength(0);
						if (logger.isDebugEnabled()) {
							s.append("    tx " + packets.size()
									+ " packet(s) to "
									+ clusterLeaderMainIp.getHostAddress()
									+ ":" + clusterLeaderDownlinkPort + " (ip="
									+ clusterLeaderIp.getHostAddress()
									+ "), sizes=");
						}

						for (DatagramPacket packet : packets) {
							if (logger.isDebugEnabled()) {
								s.append(" " + packet.getLength());
							}
							packet.setAddress(clusterLeaderIp);
							packet.setPort(clusterLeaderDownlinkPort);
							try {
								sock.send(packet);
							} catch (IOException e) {
								if (logger.isDebugEnabled()) {
									s.append(" ERROR:"
											+ e.getLocalizedMessage());
									logger.debug(s.toString());
									s.setLength(0);
								}
								logger.error("Could not send to cluster leader "
										+ clusterLeaderMainIp
										+ ":"
										+ clusterLeaderDownlinkPort);
							}
						}
						if (logger.isDebugEnabled()) {
							logger.debug(s.toString());
						}
					}
				}
			}

			lastDistributionTime = currentTime;
		}
	}
}
