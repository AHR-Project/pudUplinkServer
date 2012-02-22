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
