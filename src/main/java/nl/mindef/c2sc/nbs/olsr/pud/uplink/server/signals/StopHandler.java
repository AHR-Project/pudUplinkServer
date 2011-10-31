package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class StopHandler implements SignalHandler {
	private Signal signal = null;

	/**
	 * @param signal
	 *            the signal to set
	 */
	@Required
	public final void setSignal(String signal) {
		this.signal = new Signal(signal);
	}

	private Set<SignalHandler> handlers = new HashSet<SignalHandler>();

	/**
	 * @param handlers
	 *            the handlers to set
	 */
	@Required
	public final void setHandlers(Set<SignalHandler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void handle(Signal signal) {
		for (SignalHandler handler : handlers) {
			try {
				handler.handle(signal);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* Chain back to previous handler, if one exists */
		if ((oldHandler != null) && (oldHandler != SIG_DFL)
				&& (oldHandler != SIG_IGN)) {
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
