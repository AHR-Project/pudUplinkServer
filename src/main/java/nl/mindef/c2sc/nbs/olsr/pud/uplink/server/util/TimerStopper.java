package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util;

import java.util.Timer;

import org.springframework.beans.factory.annotation.Required;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;

public class TimerStopper implements StopHandlerConsumer {

	private Timer timer;
	
	/**
	 * @param timer the timer to set
	 */
	@Required
	public final void setTimer(Timer timer) {
		this.timer = timer;
	}

	@Override
	public void signalStop() {
		this.timer.cancel();
	}

}
