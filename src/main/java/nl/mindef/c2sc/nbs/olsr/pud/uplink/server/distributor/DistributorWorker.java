package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor;

import java.util.concurrent.atomic.AtomicInteger;

public interface DistributorWorker {
	public void distribute(AtomicInteger signaledUpdates);
}
