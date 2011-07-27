package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import java.net.InetAddress;
import java.util.Date;

public class PudMessage {
	/*
	 * Static part
	 */

	static {
		try {
			System.loadLibrary("olsr_pud");
		} catch (Throwable e) {
			throw new ExceptionInInitializerError(e);
		}
		UplinkMessageType_POSITION = getUplinkMessageTypePosition();
	}

	static public final byte UplinkMessageType_POSITION;

	public static native byte getUplinkMessageTypePosition();

	/*
	 * Dynamic Part
	 */

	private byte[] data = null;

	public PudMessage(byte[] data) {
		super();
		this.data = data;
	}

	/*
	 * Getters
	 */

	/* OLSR header */
	public native InetAddress getOriginator();

	/* PudOlsrWireFormat */
	public native byte getVersion();

	public native long getValidityTime();

	public native byte getSMask();

	/* GpsInfo */
	public native Date getTime();

	public native double getLatitude();

	public native double getLongitude();

	public native long getAltitude();

	public native long getSpeed();

	public native long getTrack();

	public native double getHdop();

	/* NodeInfo */
	public native byte getNodeIdType();

	public native String getNodeId();
}
