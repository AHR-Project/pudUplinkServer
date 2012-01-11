package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.PositionUpdateMsgs;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.PositionUpdateMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PositionUpdateHandler;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util.TimeZoneUtil;

import org.apache.log4j.Logger;
import org.olsr.plugin.pud.PositionUpdate;
import org.olsr.plugin.pud.WireFormatConstants;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PositionUpdateHandlerImpl implements PositionUpdateHandler {
	private Logger logger = Logger.getLogger(this.getClass().getName());

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

	@Override
	@Transactional
	public boolean handlePositionMessage(Gateway gateway, long utcTimestamp, PositionUpdate puMsg) {
		assert (puMsg != null);

		if (puMsg.getPositionUpdateVersion() != WireFormatConstants.VERSION) {
			logger.error("Received wrong version of position update message from " + gateway.getIp().getHostAddress() + ":"
					+ gateway.getPort() + ", expected version " + WireFormatConstants.VERSION + ", received version "
					+ puMsg.getPositionUpdateVersion() + ": ignored");
			return false;
		}

		assert (gateway != null);

		InetAddress originator = puMsg.getOlsrMessageOriginator();

		/* retrieve the node that sent the position update */
		Node originatorNode = nodes.getNode(originator);
		if (originatorNode == null) {
			/* new node */
			originatorNode = new Node(originator, gateway);
			nodes.saveNode(originatorNode, true);
		}

		/* link the node to the gateway from which it was received */
		originatorNode.setGateway(gateway);

		/* get the position update of the node */
		PositionUpdateMsg storedPositionUpdate = originatorNode.getPositionUpdateMsg();
		if (storedPositionUpdate == null) {
			/* new position update */
			storedPositionUpdate = new PositionUpdateMsg(originatorNode, puMsg);
			positionUpdateMsgs.savePositionUpdateMsg(storedPositionUpdate, true);
		} else {
			/* check that received timestamp is later than the stored timestamp */
			if (storedPositionUpdate.getPositionUpdateMsg() != null) {
				long storedTimestamp = storedPositionUpdate.getPositionUpdateMsg().getPositionUpdateTime(utcTimestamp,
						TimeZoneUtil.getTimezoneOffset());

				/* get the received timestamp */
				long receivedTimeStamp = puMsg.getPositionUpdateTime(utcTimestamp, TimeZoneUtil.getTimezoneOffset());

				if (receivedTimeStamp <= storedTimestamp) {
					/* we have stored a position with a more recent timestamp already, so skip this one */
					return false;
				}
			}
		}

		/* fill in the position update */
		storedPositionUpdate.setPositionUpdateMsg(puMsg);
		storedPositionUpdate.setReceptionTime(utcTimestamp);
		storedPositionUpdate.setValidityTime(puMsg.getPositionUpdateValidityTime() * 1000);

		/* link the position update to the node */
		originatorNode.setPositionUpdateMsg(storedPositionUpdate);

		/* save the node and position. explicitly saving the originatorNode is not needed since that is cascaded */
		positionUpdateMsgs.savePositionUpdateMsg(storedPositionUpdate, false);

		return true;
	}
}
