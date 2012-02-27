package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.util;

import java.util.List;
import java.util.Set;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.ClusterLeaderMsg;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Node;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;

public class TxChecker {
	private Logger logger = Logger.getLogger(TxChecker.class.getName());

	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *          the sessionFactory to set
	 */
	@Required
	public final void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void checkInTx(String s) throws Throwable {
		if (!this.logger.isDebugEnabled()) {
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			List<Node> allNodes = this.sessionFactory.getCurrentSession()
					.createQuery("select node from Node node order by size(node.clusterNodes) desc").list();
			if (allNodes.size() == 0) {
				return;
			}

			@SuppressWarnings("unused")
			Set<ClusterLeaderMsg> clusterNodes = allNodes.get(0).getClusterNodes();
		} catch (Throwable e) {
			this.logger.debug("*** " + s + " is NOT in a transaction ***", e);
			throw new IllegalStateException(e);
		}
	}
}
