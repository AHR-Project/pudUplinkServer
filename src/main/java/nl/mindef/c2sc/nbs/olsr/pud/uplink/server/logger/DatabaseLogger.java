package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Gateways;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DatabaseLogger {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private int updateIntervalMs = 0;

	/**
	 * @param updateIntervalMs
	 *          the updateIntervalMs to set
	 */
	@Required
	public final void setUpdateIntervalMs(int updateIntervalMs) {
		this.updateIntervalMs = updateIntervalMs;
	}

	private String databaseLogFile = null;

	/**
	 * @param databaseLogFile
	 *          the databaseLogFile to set
	 */
	@Required
	public final void setDatabaseLogFile(String databaseLogFile) {
		this.databaseLogFile = databaseLogFile;
	}

	/** the Node handler */
	private Nodes nodes;

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	@Required
	public final void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the PositionUpdateMsgs handler */
	private PositionUpdateMsgs positionUpdateMsgs;

	/**
	 * @param positionUpdateMsgs
	 *          the positionUpdateMsgs to set
	 */
	@Required
	public final void setPositions(PositionUpdateMsgs positionUpdateMsgs) {
		this.positionUpdateMsgs = positionUpdateMsgs;
	}

	/** the ClusterLeaderMsgs handler */
	private ClusterLeaderMsgs clusterLeaderMsgs;

	/**
	 * @param clusterLeaderMsgs
	 *          the clusterLeaderMsgs to set
	 */
	@Required
	public final void setClusterLeaderMsgs(ClusterLeaderMsgs clusterLeaderMsgs) {
		this.clusterLeaderMsgs = clusterLeaderMsgs;
	}

	private RelayServers relayServers;

	/**
	 * @param relayServers
	 *          the relayServers to set
	 */
	@Required
	public final void setRelayServers(RelayServers relayServers) {
		this.relayServers = relayServers;
	}

	private Gateways gateways;

	/**
	 * @param gateways
	 *          the gateways to set
	 */
	@Required
	public final void setGateways(Gateways gateways) {
		this.gateways = gateways;
	}

	/*
	 * Main
	 */

	private Timer timer = new Timer(this.getClass().getName() + "-Timer");
	private TimerTask task = null;
	private FileOutputStream fos = null;
	private static final byte[] eol = "\n".getBytes();

	public void init() throws FileNotFoundException {
		if (updateIntervalMs <= 0) {
			return;
		}

		fos = new FileOutputStream(databaseLogFile, false);

		task = new TimerTask() {
			@Override
			public void run() {
				try {
					FileChannel channel = fos.getChannel();
					channel.position(0);

					logger.debug("Writing database logfile");

					relayServers.print(fos);
					fos.write(eol);
					gateways.print(fos);
					fos.write(eol);
					nodes.print(fos);
					fos.write(eol);
					positionUpdateMsgs.print(fos);
					fos.write(eol);
					clusterLeaderMsgs.print(fos);

					channel.truncate(channel.position());
					fos.flush();
				} catch (Throwable t) {
					logger.error("Error while logging database", t);
				}
			}
		};

		timer.scheduleAtFixedRate(task, 0, updateIntervalMs);
	}

	public void destroy() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		timer.cancel();

		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				/* ignore */
			}
			fos = null;
		}
	}

	public void log(Logger logger, Level level) {
		relayServers.log(logger, level);
		gateways.log(logger, level);
		nodes.log(logger, level);
		positionUpdateMsgs.log(logger, level);
		clusterLeaderMsgs.log(logger, level);
	}
}
