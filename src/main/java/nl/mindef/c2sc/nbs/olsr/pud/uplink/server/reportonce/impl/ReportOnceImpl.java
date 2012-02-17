package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.impl;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportOnce;
import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce.ReportSubject;

public class ReportOnceImpl implements ReportOnce {
	private ReentrantLock lock = new ReentrantLock();
	private Map<ReportSubject, Set<String>> subject2Reports = new TreeMap<ReportSubject, Set<String>>();

	@Override
	public boolean add(ReportSubject reportSubject, String report) {
		this.lock.lock();
		try {
			Set<String> reports = this.subject2Reports.get(reportSubject);
			if (reports == null) {
				reports = new TreeSet<String>();
				this.subject2Reports.put(reportSubject, reports);
			}

			return reports.add(report);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean remove(ReportSubject reportSubject, String report) {
		boolean wasRemoved = false;
		this.lock.lock();
		try {
			Set<String> reports = this.subject2Reports.get(reportSubject);
			if (reports == null) {
				return false;
			}

			wasRemoved = reports.remove(report);

			if (reports.size() == 0) {
				this.subject2Reports.remove(reportSubject);
			}
		} finally {
			this.lock.unlock();
		}

		return wasRemoved;
	}

	@Override
	public void flush(ReportSubject reportSubject) {
		this.lock.lock();
		try {
			this.subject2Reports.remove(reportSubject);
		} finally {
			this.lock.unlock();
		}
	}
}
