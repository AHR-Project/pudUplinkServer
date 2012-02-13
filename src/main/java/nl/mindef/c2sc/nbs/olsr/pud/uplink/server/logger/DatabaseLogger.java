package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.olsr.plugin.pud.PositionUpdate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DatabaseLogger {
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	private int updateIntervalMs = 0;

	/**
	 * @param updateIntervalMs
	 *          the updateIntervalMs to set
	 */
	@Required
	public void setUpdateIntervalMs(int updateIntervalMs) {
		this.updateIntervalMs = updateIntervalMs;
	}

	private String databaseLogFile = null;

	/**
	 * @param databaseLogFile
	 *          the databaseLogFile to set
	 */
	@Required
	public void setDatabaseLogFile(String databaseLogFile) {
		this.databaseLogFile = databaseLogFile;
	}

	private boolean generateGraphviz = false;

	/**
	 * @param generateGraphviz
	 *          the generateGraphviz to set
	 */
	@Required
	public void setGenerateGraphviz(boolean generateGraphviz) {
		this.generateGraphviz = generateGraphviz;
	}

	private String graphvizSimpleFile = null;

	/**
	 * @param graphvizSimpleFile
	 *          the graphvizFile to set
	 */
	@Required
	public void setGraphvizSimpleFile(String graphvizSimpleFile) {
		this.graphvizSimpleFile = graphvizSimpleFile;
	}

	private String graphvizFile = null;

	/**
	 * @param graphvizFile
	 *          the graphvizFile to set
	 */
	@Required
	public void setGraphvizFile(String graphvizFile) {
		this.graphvizFile = graphvizFile;
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

	private String svgFile = null;

	/**
	 * @param svgFile
	 *          the svgFile to set
	 */
	@Required
	public void setSvgFile(String svgFile) {
		this.svgFile = svgFile;
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

	private static final String gvNodeTemplateSimple = "  \"%s\" [color=%s]\n";

	private static final String gvNodeTemplateIp = "  %s [shape=box, margin=0, label=<\n"
			+ "    <table border=\"0\" cellborder=\"1\" cellspacing=\"2\" cellpadding=\"4\">\n"
			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>\n" + "      <tr><td bgcolor=\"%s\">%s</td></tr>\n"
			+ "    </table>>];\n";

	private static final String gvNodeTemplate = "  %s [shape=box, margin=0, label=<\n"
			+ "    <table border=\"0\" cellborder=\"1\" cellspacing=\"2\" cellpadding=\"4\">\n"
			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>\n" + "      <tr><td bgcolor=\"%s\">%s</td></tr>\n"
			+ "      <tr><td bgcolor=\"%s\">%s</td></tr>\n" + "    </table>>];\n";

	private static final String colorOkSimple = "black";
	private static final String colorNotOkSimple = "red";
	private static final String colorOk = "white";
	private static final String colorNotOk = "red";

	private static boolean useIPNodeNameInDot(PositionUpdate nodePUMsg) {
		// FIXME no magic numbers, add them to WireFormatConstants
		return ((nodePUMsg == null) || (nodePUMsg.getPositionUpdateNodeIdType() == 4) || (nodePUMsg
				.getPositionUpdateNodeIdType() == 6));
	}

	private static String getNodeNameForDot(Node node) {
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

	private static void writeGraphvizNode(OutputStream gvoss, OutputStream gvos, Node node)
			throws IllegalFormatException, FormatterClosedException, IOException {
		StringBuilder sbs = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		Formatter formatters = new Formatter(sbs);
		Formatter formatter = new Formatter(sb);

		Sender sender = node.getSender();
		String senderIP = (sender == null) ? "" : "" + sender.getIp().getHostAddress() + ":" + sender.getPort();
		String senderColor = (sender == null) ? colorNotOk : colorOk;

		Long nodeId = node.getId();
		String nodeIP = node.getMainIp().getHostAddress().toString();

		PositionUpdateMsg nodePU = node.getPositionUpdateMsg();
		PositionUpdate nodePUMsg = (nodePU == null) ? null : nodePU.getPositionUpdateMsg();

		String nodeSimpleColor = (nodePUMsg == null) ? colorNotOkSimple : colorOkSimple;
		String nodeColor = (nodePUMsg == null) ? colorNotOk : colorOk;

		String nodeName = getNodeNameForDot(node);

		formatters.format(gvNodeTemplateSimple, nodeName, nodeSimpleColor);
		if (useIPNodeNameInDot(nodePUMsg)) {
			/* use IP variant */
			formatter.format(gvNodeTemplateIp, nodeId, nodeColor, nodeName, senderColor, senderIP);
		} else {
			/* use named variant */
			formatter.format(gvNodeTemplate, nodeId, nodeColor, nodeName, colorOk, nodeIP, senderColor, senderIP);
		}

		/* now write graph */
		ClusterLeaderMsg nodeCL = node.getClusterLeaderMsg();
		if (nodeCL != null) {
			formatters.format("\"%s\" -> \"%s\"\n\n", nodeName, getNodeNameForDot(nodeCL.getClusterLeaderNode()));
			formatter.format("%s -> %s\n\n", nodeId, nodeCL.getClusterLeaderNode().getId());
		}

		gvoss.write(sbs.toString().getBytes());
		gvos.write(sb.toString().getBytes());
	}

	private void generateGraphviz() throws IOException {
		List<Node> allNodes = this.nodes.getAllNodes();

		if (allNodes == null) {
			return;
		}

		this.logger.debug("Writing graphviz file");

		this.gvschannel.position(0);
		this.gvchannel.position(0);
		this.gvoss.write("digraph G {\n".getBytes());
		this.gvos.write("digraph G {\n".getBytes());
		try {
			for (Node node : allNodes) {
				writeGraphvizNode(this.gvoss, this.gvos, node);
			}
		} catch (Exception e) {
			this.logger.error("Error while generating the graphviz file", e);
		} finally {
			this.gvos.write("}\n".getBytes());
		}

		this.gvoss.flush();
		this.gvos.flush();
		this.gvschannel.truncate(this.gvschannel.position());
		this.gvchannel.truncate(this.gvchannel.position());

		if (this.generateSVG) {
			this.logger.debug("Generating SVG file");
			Runtime.getRuntime().exec("fdp -Tsvg " + this.graphvizSimpleFile + " -o " + this.svgSimpleFile);
			Runtime.getRuntime().exec("fdp -Tsvg " + this.graphvizFile + " -o " + this.svgFile);
		}
	}

	@Transactional(readOnly = true)
	public void logit() throws IOException {
		this.channel.position(0);

		this.logger.debug("Writing database logfile");

		this.relayServers.print(this.fos);
		this.fos.write(eol);
		this.senders.print(this.fos);
		this.fos.write(eol);
		this.nodes.print(this.fos);
		this.fos.write(eol);
		this.positionUpdateMsgs.print(this.fos);
		this.fos.write(eol);
		this.clusterLeaderMsgs.print(this.fos);

		this.channel.truncate(this.channel.position());
		this.fos.flush();

		if (this.generateGraphviz) {
			generateGraphviz();
		}
	}

	private Timer timer = null;
	private TimerTask task = null;
	private FileOutputStream fos = null;
	private FileChannel channel = null;
	private FileOutputStream gvoss = null;
	private FileChannel gvschannel = null;
	private FileOutputStream gvos = null;
	private FileChannel gvchannel = null;
	private static final byte[] eol = "\n".getBytes();

	public void init() throws FileNotFoundException {
		if (this.updateIntervalMs <= 0) {
			return;
		}

		this.fos = new FileOutputStream(this.databaseLogFile, false);
		this.channel = this.fos.getChannel();

		if (this.generateGraphviz) {
			this.gvoss = new FileOutputStream(this.graphvizSimpleFile, false);
			this.gvschannel = this.gvoss.getChannel();
			this.gvos = new FileOutputStream(this.graphvizFile, false);
			this.gvchannel = this.gvos.getChannel();
		}

		this.timer = new Timer(this.getClass().getSimpleName() + "-Timer");
		this.task = new TimerTask() {
			@Override
			public void run() {
				try {
					logit();
				} catch (Throwable e) {
					DatabaseLogger.this.logger.error("error during database logging", e);
				}
			}
		};

		this.timer.scheduleAtFixedRate(this.task, 0, this.updateIntervalMs);
	}

	public void destroy() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}

		if (this.channel != null) {
			try {
				this.channel.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.channel = null;
		}
		if (this.gvoss != null) {
			try {
				this.gvoss.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.gvoss = null;
		}
		if (this.gvos != null) {
			try {
				this.gvos.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.gvos = null;
		}
		if (this.fos != null) {
			try {
				this.fos.close();
			} catch (IOException e) {
				/* ignore */
			}
			this.fos = null;
		}
	}

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
