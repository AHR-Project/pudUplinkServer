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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Sender;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.WireFormatChecker;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportOnce;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportSubject;

import org.apache.log4j.Logger;
import org.olsr.plugin.pud.ClusterLeader;
import org.olsr.plugin.pud.PositionUpdate;
import org.olsr.plugin.pud.UplinkMessage;
import org.olsr.plugin.pud.WireFormatConstants;
import org.springframework.beans.factory.annotation.Required;

public class WireFormatCheckerImpl implements WireFormatChecker {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private ReportOnce reportOnce;

	/**
	 * @param reportOnce
	 *          the reportOnce to set
	 */
	@Required
	public final void setReportOnce(ReportOnce reportOnce) {
		this.reportOnce = reportOnce;
	}

	@Override
	public boolean checkUplinkMessageWireFormat(Sender sender, UplinkMessage msg) {
		assert (sender != null);
		assert (msg != null);

		int wireFormatVersion = -1;
		if (msg instanceof ClusterLeader) {
			wireFormatVersion = ((ClusterLeader) msg).getClusterLeaderVersion();
		} else /* if (msg instanceof PositionUpdate) */{
			wireFormatVersion = ((PositionUpdate) msg).getPositionUpdateVersion();
		}

		String senderReport = sender.getIp().getHostAddress() + ":" + sender.getPort().toString();

		if (wireFormatVersion == WireFormatConstants.VERSION) {
			if (this.reportOnce.remove(ReportSubject.SENDER_WIRE_FORMAT, senderReport, senderReport)) {
				this.logger.error("Received correct version of uplink message from " + senderReport
						+ ", node will no longer be ignored");
			}
			return true;
		}

		if (this.reportOnce.add(ReportSubject.SENDER_WIRE_FORMAT, senderReport, senderReport)) {
			this.logger.warn("Received uplink message version " + wireFormatVersion + " (expected version "
					+ WireFormatConstants.VERSION + ") from " + senderReport + ", node will be ignored");
		}
		return false;
	}
}
