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
	private Map<ReportSubject, Map<String, Set<String>>> subject2Reports = new TreeMap<ReportSubject, Map<String, Set<String>>>();

	@Override
	public boolean add(ReportSubject reportSubject, String key, String report) {
		assert (key != null);
		assert (report != null);

		this.lock.lock();
		try {
			Map<String, Set<String>> subjectReports = this.subject2Reports.get(reportSubject);
			if (subjectReports == null) {
				subjectReports = new TreeMap<String, Set<String>>();
				this.subject2Reports.put(reportSubject, subjectReports);
			}

			Set<String> keyReports = subjectReports.get(key);
			if (keyReports == null) {
				keyReports = new TreeSet<String>();
				subjectReports.put(key, keyReports);
			}

			return keyReports.add(report);
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean remove(ReportSubject reportSubject, String key, String report) {
		assert (key != null);
		assert (report != null);

		boolean wasRemoved = false;
		this.lock.lock();
		try {
			Map<String, Set<String>> subjectReports = this.subject2Reports.get(reportSubject);
			if (subjectReports == null) {
				return false;
			}

			Set<String> keyReports = subjectReports.get(key);
			if (keyReports == null) {
				return false;
			}

			wasRemoved = keyReports.remove(report);

			if (keyReports.size() == 0) {
				subjectReports.remove(key);
			}

			if (subjectReports.size() == 0) {
				this.subject2Reports.remove(reportSubject);
			}
		} finally {
			this.lock.unlock();
		}

		return wasRemoved;
	}

	@Override
	public void flush(ReportSubject reportSubject, String key) {
		this.lock.lock();
		try {
			if (reportSubject == null) {
				this.subject2Reports.clear();
				return;
			}

			if (key == null) {
				this.subject2Reports.remove(reportSubject);
				return;
			}

			Map<String, Set<String>> subjectReports = this.subject2Reports.get(reportSubject);
			if (subjectReports == null) {
				return;
			}

			subjectReports.remove(key);

			if (subjectReports.size() == 0) {
				this.subject2Reports.remove(reportSubject);
			}
		} finally {
			this.lock.unlock();
		}
	}
}
