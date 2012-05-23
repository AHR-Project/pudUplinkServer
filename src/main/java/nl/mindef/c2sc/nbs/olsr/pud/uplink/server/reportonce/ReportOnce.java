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
package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce;

public interface ReportOnce {
	/**
	 * Add a report to a subject
	 * 
	 * @param reportSubject
	 *          the report subject
	 * @param key
	 *          the key of the report
	 * @param report
	 *          the report
	 * @return true when the report was added (not yet reported)
	 */
	public boolean add(ReportSubject reportSubject, String key, String report);

	/**
	 * Remove a report from a subject
	 * 
	 * @param reportSubject
	 *          the report subject
	 * @param key
	 *          the key of the report
	 * @param report
	 *          the report
	 * @return true when the report was removed (previously reported)
	 */
	public boolean remove(ReportSubject reportSubject, String key, String report);

	/**
	 * Flush the subject (remove all reports)
	 * 
	 * @param reportSubject
	 *          the subject. when null will flush every report for all subjects.
	 * @param key
	 *          the key of the report. when null will flush every report for the indicated subject.
	 */
	public void flush(ReportSubject reportSubject, String key);
}
