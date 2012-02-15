package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public interface DatabaseLogger {
	public void init() throws FileNotFoundException;
	public void log(Logger log, Level level);
	public void logit() throws IOException;
}
