package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.IllegalFormatException;
import java.util.List;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger.DatabaseLogger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.olsr.plugin.pud.PositionUpdate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DatabaseLoggerImpl implements DatabaseLogger {
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	private String databaseLogFile = null;

	/**
	 * @param databaseLogFile
	 *          the databaseLogFile to set
	 */
	@Required
	public void setDatabaseLogFile(String databaseLogFile) {
		this.databaseLogFile = databaseLogFile;
	}

	private String dotSimpleFile = null;

	/**
	 * @param dotSimpleFile
	 *          the dotFullFile to set
	 */
	@Required
	public void setDotSimpleFile(String dotSimpleFile) {
		this.dotSimpleFile = dotSimpleFile;
	}

	private String dotFullFile = null;

	/**
	 * @param dotFullFile
	 *          the dotFullFile to set
	 */
	@Required
	public void setDotFullFile(String dotFullFile) {
		this.dotFullFile = dotFullFile;
	}

	private boolean generateSVG = false;

	/**
	 * @param generateSVG
	 *          the generateSVG to set
	 */
	@Required
	public void setGenerateSVG(boolean generateSVG) {
		this.generateSVG = generateSVG;
	}

	private String svgSimpleFile = null;

	/**
	 * @param svgSimpleFile
	 *          the svgSimpleFile to set
	 */
	@Required
	public void setSvgSimpleFile(String svgSimpleFile) {
		this.svgSimpleFile = svgSimpleFile;
	}

	private String svgFullFile = null;

	/**
	 * @param svgFullFile
	 *          the svgFullFile to set
	 */
	@Required
	public void setSvgFullFile(String svgFullFile) {
		this.svgFullFile = svgFullFile;
	}

	/** the Node handler */
	private Nodes nodes;

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	@Required
	public void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the PositionUpdateMsgs handler */
	private PositionUpdateMsgs positionUpdateMsgs;

	/**
	 * @param positionUpdateMsgs
	 *          the positionUpdateMsgs to set
	 */
	@Required
	public void setPositions(PositionUpdateMsgs positionUpdateMsgs) {
		this.positionUpdateMsgs = positionUpdateMsgs;
	}

	/** the ClusterLeaderMsgs handler */
	private ClusterLeaderMsgs clusterLeaderMsgs;

	/**
	 * @param clusterLeaderMsgs
	 *          the clusterLeaderMsgs to set
	 */
	@Required
	public void setClusterLeaderMsgs(ClusterLeaderMsgs clusterLeaderMsgs) {
		this.clusterLeaderMsgs = clusterLeaderMsgs;
	}

	private RelayServers relayServers;

	/**
	 * @param relayServers
	 *          the relayServers to set
	 */
	@Required
	public void setRelayServers(RelayServers relayServers) {
		this.relayServers = relayServers;
	}

	private Senders senders;

	/**
	 * @param senders
	 *          the senders to set
	 */
	@Required
	public void setSenders(Senders senders) {
		this.senders = senders;
	}

	/*
	 * Main
	 */

	private static final String dotNodeTemplateSimple = "\n%s\"%s\" [shape=%s,style=filled,fillcolor=%s]\n";

	private static final String dotNodeTemplateFullIp = "\n%s%s [shape=box, margin=0, label=<\n"
			+ "    <table border=\"0\" cellborder=\"1\" cellspacing=\"2\" cellpadding=\"4\">\n"
			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>\n" + "      <tr><td bgcolor=\"%s\">%s</td></tr>\n"
			+ "    </table>>];\n";

	private static final String dotNodeTemplateFull = "\n%s%s [shape=box, margin=0, label=<\n"
			+ "    <table border=\"0\" cellborder=\"1\" cellspacing=\"2\" cellpadding=\"4\">\n"
			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>\n" + "      <tr><td bgcolor=\"%s\">%s</td></tr>\n"
			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>\n" + "    </table>>];\n";

	private static final String shapeNormal = "ellipse";
	private static final String shapeClusterLeader = "box";
	private static final String colorSimpleOk = "white";
	private static final String colorSimpleNotOk = "red";
	private static final String colorFullOk = colorSimpleOk;
	private static final String colorFullNotOk = colorSimpleNotOk;

	private static boolean useIPNodeNameInDot(PositionUpdate nodePUMsg) {
		// FIXME no magic numbers, add them to WireFormatConstants
		return ((nodePUMsg == null) || (nodePUMsg.getPositionUpdateNodeIdType() == 4) || (nodePUMsg
				.getPositionUpdateNodeIdType() == 6));
	}

	protected static String getNodeNameForDot(Node node) {
		PositionUpdateMsg nodePU = node.getPositionUpdateMsg();
		PositionUpdate nodePUMsg = (nodePU == null) ? null : nodePU.getPositionUpdateMsg();

		if (useIPNodeNameInDot(nodePUMsg)) {
			/* use IP variant */
			return node.getMainIp().getHostAddress().toString();
		}

		/* use named variant */
		assert (nodePUMsg != null);
		return nodePUMsg.getPositionUpdateNodeId();
	}

	private static void writeDotNode(OutputStream gvoss, OutputStream gvos, Node node, String indent)
			throws IllegalFormatException, FormatterClosedException, IOException {
		StringBuilder sbSimple = new StringBuilder();
		StringBuilder sbFull = new StringBuilder();
		Formatter formatterSimple = new Formatter(sbSimple);
		Formatter formatterFull = new Formatter(sbFull);

		Sender sender = node.getSender();
		String senderIP = (sender == null) ? "" : "" + sender.getIp().getHostAddress() + ":" + sender.getPort();
		String senderColor = (sender == null) ? colorFullNotOk : colorFullOk;

		Long nodeId = node.getId();
		String nodeIP = node.getMainIp().getHostAddress().toString();

		PositionUpdateMsg nodePU = node.getPositionUpdateMsg();
		PositionUpdate nodePUMsg = (nodePU == null) ? null : nodePU.getPositionUpdateMsg();

		String nodeSimpleColor = (nodePUMsg == null) ? colorSimpleNotOk : colorSimpleOk;
		String nodeColor = (nodePUMsg == null) ? colorFullNotOk : colorFullOk;
		String nodeShape = node.getClusterNodes().isEmpty() ? shapeNormal : shapeClusterLeader;

		String nodeName = getNodeNameForDot(node);

		formatterSimple.format(dotNodeTemplateSimple, indent, nodeName, nodeShape, nodeSimpleColor);
		if (useIPNodeNameInDot(nodePUMsg)) {
			/* use IP variant */
			formatterFull.format(dotNodeTemplateFullIp, indent, nodeId, nodeColor, nodeName, senderColor, senderIP);
		} else {
			/* use named variant */
			formatterFull.format(dotNodeTemplateFull, indent, nodeId, nodeColor, nodeName, colorFullOk, nodeIP, senderColor,
					senderIP);
		}

		/* now write graph */
		ClusterLeaderMsg nodeCL = node.getClusterLeaderMsg();
		if (nodeCL != null) {
			formatterSimple.format("%s\"%s\" -> \"%s\"\n", indent, nodeName, getNodeNameForDot(nodeCL.getClusterLeaderNode()));
			formatterFull.format("%s%s -> %s\n", indent, nodeId, nodeCL.getClusterLeaderNode().getId());
		}

		gvoss.write(sbSimple.toString().getBytes());
		gvos.write(sbFull.toString().getBytes());
	}

	protected class NodeNameComparatorForDot implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			return getNodeNameForDot(o1).compareTo(getNodeNameForDot(o2));
		}
	}

	private void generateDotAndSVG() throws IOException {
		List<Node> allNodes = this.nodes.getAllNodes();

		if (allNodes == null) {
			return;
		}

		this.logger.debug("Writing dot file");

		Collections.sort(allNodes, new NodeNameComparatorForDot());

		this.dotSimpleFileOSChannel.position(0);
		this.dotFullFileOSChannel.position(0);
		this.dotSimpleFileOS.write("digraph G {\n".getBytes());
		this.dotFullFileOS.write("digraph G {\n".getBytes());
		try {
			for (Node node : allNodes) {
				writeDotNode(this.dotSimpleFileOS, this.dotFullFileOS, node, "  ");
			}
		} catch (Exception e) {
			this.logger.error("Error while generating the dot file", e);
		} finally {
			this.dotSimpleFileOS.write("}\n".getBytes());
			this.dotFullFileOS.write("}\n".getBytes());
		}

		this.dotSimpleFileOS.flush();
		this.dotFullFileOS.flush();
		this.dotSimpleFileOSChannel.truncate(this.dotSimpleFileOSChannel.position());
		this.dotFullFileOSChannel.truncate(this.dotFullFileOSChannel.position());

		this.logger.debug("Generating SVG file");
		Runtime.getRuntime().exec("fdp -Tsvg " + this.dotSimpleFile + " -o " + this.svgSimpleFile);
		Runtime.getRuntime().exec("fdp -Tsvg " + this.dotFullFile + " -o " + this.svgFullFile);
	}

	@Override
	@Transactional(readOnly = true)
	public void logit() throws IOException {
		this.databaseLogFileOSChannel.position(0);

		this.logger.debug("Writing database logfile");

		this.relayServers.print(this.databaseLogFileOS);
		this.databaseLogFileOS.write(eol);
		this.senders.print(this.databaseLogFileOS);
		this.databaseLogFileOS.write(eol);
		this.nodes.print(this.databaseLogFileOS);
		this.databaseLogFileOS.write(eol);
		this.positionUpdateMsgs.print(this.databaseLogFileOS);
		this.databaseLogFileOS.write(eol);
		this.clusterLeaderMsgs.print(this.databaseLogFileOS);

		this.databaseLogFileOSChannel.truncate(this.databaseLogFileOSChannel.position());
		this.databaseLogFileOS.flush();

		if (this.generateSVG) {
			generateDotAndSVG();
		}
	}

	private FileOutputStream databaseLogFileOS = null;
	private FileChannel databaseLogFileOSChannel = null;
	private FileOutputStream dotSimpleFileOS = null;
	private FileChannel dotSimpleFileOSChannel = null;
	private FileOutputStream dotFullFileOS = null;
	private FileChannel dotFullFileOSChannel = null;
	private static final byte[] eol = "\n".getBytes();

	@Override
	public void init() throws FileNotFoundException {
		this.databaseLogFileOS = new FileOutputStream(this.databaseLogFile, false);
		this.databaseLogFileOSChannel = this.databaseLogFileOS.getChannel();

		if (this.generateSVG) {
			this.dotSimpleFileOS = new FileOutputStream(this.dotSimpleFile, false);
			this.dotSimpleFileOSChannel = this.dotSimpleFileOS.getChannel();
			this.dotFullFileOS = new FileOutputStream(this.dotFullFile, false);
			this.dotFullFileOSChannel = this.dotFullFileOS.getChannel();
		}
	}

	public void uninit() {
		if (this.databaseLogFileOSChannel != null) {
			try {
				this.databaseLogFileOSChannel.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.databaseLogFileOSChannel = null;
		}
		if (this.dotSimpleFileOS != null) {
			try {
				this.dotSimpleFileOS.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.dotSimpleFileOS = null;
		}
		if (this.dotFullFileOS != null) {
			try {
				this.dotFullFileOS.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.dotFullFileOS = null;
		}
		if (this.databaseLogFileOS != null) {
			try {
				this.databaseLogFileOS.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.databaseLogFileOS = null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void log(Logger log, Level level) {
		try {
			this.relayServers.log(log, level);
			this.senders.log(log, level);
			this.nodes.log(log, level);
			this.positionUpdateMsgs.log(log, level);
			this.clusterLeaderMsgs.log(log, level);
		} catch (Throwable t) {
			this.logger.error("Error while logging database", t);
		}
	}
}
