package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.WireFormatChecker;

import org.apache.log4j.Logger;
import org.olsr.plugin.pud.ClusterLeader;
import org.olsr.plugin.pud.PositionUpdate;
import org.olsr.plugin.pud.UplinkMessage;
import org.olsr.plugin.pud.WireFormatConstants;

public class WireFormatCheckerImpl implements WireFormatChecker {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Set<ReportedNode> reportedNodes = new HashSet<ReportedNode>();

	private class ReportedNode {
		private InetAddress ip;
		private Integer port;

		public ReportedNode(Sender sender) {
			super();
			this.ip = sender.getIp();
			this.port = sender.getPort();
			assert (this.ip != null);
			assert (this.port != null);
		}

		/**
		 * @return the ip
		 */
		public final InetAddress getIp() {
			return this.ip;
		}

		/**
		 * @return the port
		 */
		public final Integer getPort() {
			return this.port;
		}

		@Override
		public String toString() {
			return this.ip.getHostAddress().toString() + ":" + this.port.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ReportedNode) {
				return (this.ip.equals(((ReportedNode) obj).getIp()) && this.port.equals(((ReportedNode) obj).getPort()));
			}

			return false;
		}

		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}
	}

	@Override
	public boolean checkUplinkMessageWireFormat(Sender sender, UplinkMessage msg) {
		assert (sender != null);
		assert (msg != null);

		int wireFormatVersion = -1;
		if (msg instanceof ClusterLeader) {
			wireFormatVersion = ((ClusterLeader) msg).getClusterLeaderVersion();
		} else /* if (msg instanceof ClusterLeader) */{
			wireFormatVersion = ((PositionUpdate) msg).getPositionUpdateVersion();
		}

		ReportedNode reportedNode = new ReportedNode(sender);
		boolean senderAlreadyReported = this.reportedNodes.contains(reportedNode);

		if (wireFormatVersion == WireFormatConstants.VERSION) {
			if (senderAlreadyReported) {
				this.reportedNodes.remove(reportedNode);
				this.logger.error("Received correct version of uplink message from " + reportedNode.toString()
						+ ", node will no longer be ignored");
			}
			return true;
		}

		if (!senderAlreadyReported) {
			this.reportedNodes.add(reportedNode);
			this.logger.error("Received uplink message version " + wireFormatVersion + " (expected version "
					+ WireFormatConstants.VERSION + ") from " + reportedNode.toString() + ", node will be ignored");
		}
		return false;
	}
}
