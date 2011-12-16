package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.impl;

import java.util.Set;
import java.util.TreeSet;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;

import org.springframework.beans.factory.annotation.Required;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class StopHandlerImpl implements SignalHandler {
	private Signal signal = null;

	/**
	 * @param signal
	 *          the signal to set
	 */
	@Required
	public final void setSignal(String signal) {
		this.signal = new Signal(signal);
	}

	private Set<StopHandlerConsumer> handlers = new TreeSet<StopHandlerConsumer>();

	/**
	 * @param handlers
	 *          the handlers to set
	 */
	@Required
	public final void setHandlers(Set<StopHandlerConsumer> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void handle(Signal signal) {
		if (signal.equals(this.signal)) {
			for (StopHandlerConsumer handler : handlers) {
				try {
					handler.signalStop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/* Chain back to previous handler, if one exists */
		if ((oldHandler != null) && (oldHandler != SIG_DFL) && (oldHandler != SIG_IGN)) {
			try {
				oldHandler.handle(signal);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private SignalHandler oldHandler = null;

	public void init() {
		oldHandler = Signal.handle(signal, this);
	}

	public void destroy() {
		Signal.handle(signal, oldHandler);
	}
}
