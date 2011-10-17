package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	public static void main(String[] args) {
		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					"classpath:META-INF/spring/application-context.xml");
			context.start();

			RelayServer relayServer = (RelayServer) context
					.getBean("RelayServer");

			relayServer.join();

			context.stop();
			context.close();
			context.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
