package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Hello world!
 * 
 */
public class Main {


	static final int BUFFERSIZE = 256;
	static final int UDP_PORT = 2241;

	private static String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private static void dumpPacket(boolean processed, UplinkMessage msg) {
		byte[] data = msg.getData();
		int length = msg.getDataSize();
		InetAddress addr = msg.getSender();
		String time = getCurrentTime();

		if (!processed) {
			System.out.printf("[%s] Ignored %s bytes of data from %s\n", time,
					length, addr.toString());
			return;
		}

		System.out.printf("[%s] Received %s bytes of data from %s:\n", time,
				length, addr.toString());
		for (int index = 0; index < length; index++) {
			System.out.printf(" %02x", data[index]);
		}
		System.out.println();
	}

	private static UplinkMessage processPacket(DatagramPacket pack) {
		byte[] data = pack.getData();

		if (data[0] != UplinkMessage.UPLINKMESSAGE_TYPE_PU) {
			return null;
		}

		UplinkMessage msg = new UplinkMessage();
		msg.setSender(pack.getAddress());
		msg.setData(data);
		msg.setDataSize(pack.getLength());

		return msg;
	}

	public static void main(String[] args) {
		DatagramPacket pack = new DatagramPacket(new byte[BUFFERSIZE],
				BUFFERSIZE);
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket(UDP_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}

		while (true) {
			try {
				sock.receive(pack);
				UplinkMessage msg = processPacket(pack);
				dumpPacket(msg != null, msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
