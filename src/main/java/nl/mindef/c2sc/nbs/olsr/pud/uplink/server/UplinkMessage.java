package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import java.net.InetAddress;

public class UplinkMessage {
	static public final byte UPLINKMESSAGE_TYPE_PU = PudMessage.UplinkMessageType_POSITION;

	private InetAddress sender = null;
	private byte[] data = null;
	private int dataSize = 0;

	/**
	 * @return the sender
	 */
	public final InetAddress getSender() {
		return sender;
	}

	/**
	 * @param sender
	 *            the sender to set
	 */
	public final void setSender(InetAddress sender) {
		this.sender = sender;
	}

	/**
	 * @return the data
	 */
	public final byte[] getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public final void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * @return the dataSize
	 */
	public final int getDataSize() {
		return dataSize;
	}

	/**
	 * @param dataSize
	 *            the dataSize to set
	 */
	public final void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	/**
	 * @return the uplink message type
	 * @throws IllegalStateException
	 *             when data is uninitialised
	 */
	public byte getType() throws IllegalStateException {
		if (data == null) {
			throw new IllegalStateException("Data is uninitialised");
		}

		return data[0];
	}

	/**
	 * @return the position update message
	 * @throws IllegalStateException
	 *             when not a position update message or when data is
	 *             uninitialised
	 */
	public PudMessage getPudMessage() throws IllegalStateException {
		byte type = getType();
		if (type != UPLINKMESSAGE_TYPE_PU) {
			throw new IllegalStateException(
					"Not a position update message (type = " + type + ")");
		}

		return new PudMessage(data);
	}
}
