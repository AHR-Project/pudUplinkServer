package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.ClusterLeaderMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Senders;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DatabaseLogger {
	Logger logger = Logger.getLogger(this.getClass().getName());

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

	/** the Node handler */
	Nodes nodes;

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	@Required
	public void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	/** the PositionUpdateMsgs handler */
	PositionUpdateMsgs positionUpdateMsgs;

	/**
	 * @param positionUpdateMsgs
	 *          the positionUpdateMsgs to set
	 */
	@Required
	public void setPositions(PositionUpdateMsgs positionUpdateMsgs) {
		this.positionUpdateMsgs = positionUpdateMsgs;
	}

	/** the ClusterLeaderMsgs handler */
	ClusterLeaderMsgs clusterLeaderMsgs;

	/**
	 * @param clusterLeaderMsgs
	 *          the clusterLeaderMsgs to set
	 */
	@Required
	public void setClusterLeaderMsgs(ClusterLeaderMsgs clusterLeaderMsgs) {
		this.clusterLeaderMsgs = clusterLeaderMsgs;
	}

	RelayServers relayServers;

	/**
	 * @param relayServers
	 *          the relayServers to set
	 */
	@Required
	public void setRelayServers(RelayServers relayServers) {
		this.relayServers = relayServers;
	}

	Senders senders;

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

	@Transactional(readOnly = true)
	public void logit() throws IOException {
		FileChannel channel = this.fos.getChannel();
		channel.position(0);

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

		channel.truncate(channel.position());
		this.fos.flush();
	}

	private Timer timer = null;
	private TimerTask task = null;
	FileOutputStream fos = null;
	static final byte[] eol = "\n".getBytes();

	public void init() throws FileNotFoundException {
		if (this.updateIntervalMs <= 0) {
			return;
		}

		this.fos = new FileOutputStream(this.databaseLogFile, false);

		this.timer = new Timer(this.getClass().getSimpleName() + "-Timer");
		this.task = new TimerTask() {
			@Override
			public void run() {
				try {
					logit();
				} catch (Throwable t) {
					DatabaseLogger.this.logger.error("Error while logging database", t);
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
		this.timer.cancel();

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
		this.relayServers.log(log, level);
		this.senders.log(log, level);
		this.nodes.log(log, level);
		this.positionUpdateMsgs.log(log, level);
		this.clusterLeaderMsgs.log(log, level);
	}
}
