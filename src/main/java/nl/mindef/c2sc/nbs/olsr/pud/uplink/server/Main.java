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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.uplink.UplinkReceiver;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	private static Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		logger.info("RelayServer starting");
		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					"classpath:META-INF/spring/application-context.xml");

			logger.info("RelayServer started");

			context.start();

			UplinkReceiver uplinkReceiver = (UplinkReceiver) context.getBean("UplinkReceiver");
			uplinkReceiver.join();

			logger.info("RelayServer stopped");

			context.stop();
			context.close();
			context.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
