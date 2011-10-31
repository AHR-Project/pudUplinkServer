package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	private static Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					"classpath:META-INF/spring/application-context.xml");

			context.start();

			logger.info("RelayServer started");

			RelayServer relayServer = (RelayServer) context
					.getBean("RelayServer");

			relayServer.join();

			logger.info("RelayServer stopped");

			context.stop();
			context.close();
			context.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
