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
	public boolean handlePositionMessage(Gateway gateway, long utcTimestamp, PositionUpdate posUpMsg) {
		assert (posUpMsg != null);

		if (posUpMsg.getPositionUpdateVersion() != WireFormatConstants.VERSION) {
			logger.warn("Received wrong version of position update message, expected version " + WireFormatConstants.VERSION
					+ ", received version " + posUpMsg.getPositionUpdateVersion() + ": ignored");
			return false;
		}

		assert (gateway != null);

		InetAddress originator = posUpMsg.getOlsrMessageOriginator();

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
		PositionUpdateMsg storedPosition = originatorNode.getPositionUpdateMsg();
		boolean storedPositionIsNew = false;
		if (storedPosition == null) {
			/* new position update */
			storedPosition = new PositionUpdateMsg(originatorNode, posUpMsg);
			positionUpdateMsgs.savePositionUpdateMsg(storedPosition, true);
			storedPositionIsNew = true;
		}

		/* check that received timestamp is later than stored timestamp */
		if (!storedPositionIsNew) {
			/* get the stored timestamp */
			long storedTimestamp = 0;
			if (storedPosition.getPositionUpdateMsg() != null) {
				storedTimestamp = storedPosition.getPositionUpdateMsg().getPositionUpdateTime(utcTimestamp,
						TimeZoneUtil.getTimezoneOffset());
			}

			/* get the received timestamp */
			long receivedTimeStamp = posUpMsg.getPositionUpdateTime(utcTimestamp, TimeZoneUtil.getTimezoneOffset());

			if (receivedTimeStamp <= storedTimestamp) {
				/* we have stored a position with a more recent timestamp already, so skip this one */
				return false;
			}
		}

		/* fill in the position update */
		storedPosition.setPositionUpdateMsg(posUpMsg);
		storedPosition.setReceptionTime(utcTimestamp);
		storedPosition.setValidityTime(posUpMsg.getPositionUpdateValidityTime() * 1000);

		/* link the position update to the node */
		originatorNode.setPositionUpdateMsg(storedPosition);

		/* save the node and position. explicitly saving the originatorNode is not needed since that is cascaded */
		positionUpdateMsgs.savePositionUpdateMsg(storedPosition, false);

		return true;
	}
}
