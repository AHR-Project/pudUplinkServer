/*
 *  Copyright (C) 2012 Royal Dutch Army
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *  MA  02110-1301, USA.
 */
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
	public void handle(Signal signl) {
		if (signl.equals(this.signal)) {
			for (StopHandlerConsumer handler : this.handlers) {
				try {
					handler.signalStop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/* Chain back to previous handler, if one exists */
		if ((this.oldHandler != null) && (this.oldHandler != SIG_DFL) && (this.oldHandler != SIG_IGN)) {
			try {
				this.oldHandler.handle(signl);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private SignalHandler oldHandler = null;

	public void init() {
		this.oldHandler = Signal.handle(this.signal, this);
	}

	public void destroy() {
		Signal.handle(this.signal, this.oldHandler);
	}
}
