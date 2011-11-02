package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.impl;

import java.net.InetAddress;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Nodes;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.Positions;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.NodePosition;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
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

	/** the Positions handler */
	private Positions positions;

	/**
	 * @param positions
	 *            the positions to set
	 */
	@Required
	public final void setPositions(Positions positions) {
		this.positions = positions;
	}

	/** the Node handler */
	private Nodes nodes;

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	@Required
	public final void setNodes(Nodes nodes) {
		this.nodes = nodes;
	}

	@Override
	@Transactional
	public boolean handlePositionMessage(InetAddress srcIp, long utcTimestamp,
			PositionUpdate posUpMsg, RelayServer relayServer) {
		assert (relayServer != null);

		if (posUpMsg.getPositionUpdateVersion() != WireFormatConstants.VERSION) {
			logger.warn("Received wrong version of position update"
					+ " message, expected version "
					+ WireFormatConstants.VERSION + ", received" + " version "
					+ posUpMsg.getPositionUpdateVersion() + ": ignored");
			return false;
		}

		InetAddress originator = posUpMsg.getOlsrMessageOriginator();

		/* retrieve the node that sent the position update */
		Node originatorNode = nodes.getNode(originator);
		if (originatorNode == null) {
			/* new node */
			originatorNode = new Node();
			originatorNode.setIp(srcIp);
			originatorNode.setMainIp(originator);
			originatorNode.setReceptionTime(utcTimestamp);
			originatorNode.setValidityTime(posUpMsg
					.getPositionUpdateValidityTime() * 1000);
			originatorNode.setRelayServer(relayServer);
			nodes.saveNode(originatorNode, true);
		}

		/* get the position that we stored */
		NodePosition storedPosition = originatorNode.getPosition();
		boolean storedPositionIsNew = false;
		if (storedPosition == null) {
			/* new position */
			storedPosition = new NodePosition();
			storedPositionIsNew = true;
		}

		/* get the stored timestamp */
		long storedTimestamp = 0;
		if (storedPosition.getPositionUpdate() != null) {
			storedTimestamp = storedPosition.getPositionUpdate()
					.getPositionUpdateTime(utcTimestamp,
							TimeZoneUtil.getTimezoneOffset());
		}

		/* get the received timestamp */
		long receivedTimeStamp = posUpMsg.getPositionUpdateTime(utcTimestamp,
				TimeZoneUtil.getTimezoneOffset());

		/* check that received timestamp is later than stored timestamp */
		if (receivedTimeStamp <= storedTimestamp) {
			/*
			 * we have stored a position with a more recent timestamp already,
			 * so skip this one
			 */
			return false;
		}

		/* fill in the stored position with the received position */
		storedPosition.setMainIp(originator);
		storedPosition.setReceptionTime(utcTimestamp);
		storedPosition
				.setValidityTime(posUpMsg.getPositionUpdateValidityTime() * 1000);
		storedPosition.setPositionUpdate(posUpMsg);

		/* use the owning side of the relation */
		originatorNode.setPosition(storedPosition);

		/*
		 * save the node and position. explicitly saving the originatorNode is
		 * not needed since that is cascaded
		 */
		positions.saveNodePosition(storedPosition, storedPositionIsNew);

		return true;
	}
}
