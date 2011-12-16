package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Positions;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DatabaseLogger {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public static final int UPDATE_INTERVAL_MS_DEFAULT = 15000;

	private boolean enabled = false;

	/**
	 * @param enabled
	 *          the enabled to set
	 */
	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private int updateIntervalMs = UPDATE_INTERVAL_MS_DEFAULT;

	/**
	 * @param updateIntervalMs
	 *          the updateIntervalMs to set
	 */
	public final void setUpdateIntervalMs(int updateIntervalMs) {
		this.updateIntervalMs = updateIntervalMs;
	}

	public static final String DATABASELOGFILE_DEFAULT = "database.txt";

	private String databaseLogFile = DATABASELOGFILE_DEFAULT;

	/**
	 * @param databaseLogFile
	 *          the databaseLogFile to set
	 */
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

	/** the Positions handler */
	private Positions positions;

	/**
	 * @param positions
	 *          the positions to set
	 */
	@Required
	public final void setPositions(Positions positions) {
		this.positions = positions;
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

	/*
	 * Main
	 */

	private Timer timer = new Timer(this.getClass().getName());

	private FileOutputStream fos;
	private TimerTask task = null;
	private static final byte[] eol = "\n".getBytes();

	public void init() throws FileNotFoundException {
		if (!enabled) {
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

					nodes.print(fos);
					fos.write(eol);
					positions.print(fos);
					fos.write(eol);
					relayServers.print(fos);

					channel.truncate(channel.position());
					fos.flush();
				} catch (Throwable t) {
					/* ignore */
					t.printStackTrace();
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

		try {
			fos.close();
		} catch (IOException e) {
			/* ignore */
		}
		fos = null;
	}

	public void log(Logger logger, Level level) {
		if (logger.isEnabledFor(level)) {
			nodes.log(logger, Level.DEBUG);
			positions.log(logger, Level.DEBUG);
			relayServers.log(logger, Level.DEBUG);
		}
	}
}
