package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.reportonce;

public interface ReportOnce {
	static final String SUBJECT_SENDER = "senders";

	/**
	 * Add a report to a subject
	 * 
	 * @param reportSubject
	 *          the report subject
	 * @param report
	 *          the report
	 * @return true when the report was added (not yet reported)
	 */
	public boolean add(ReportSubject reportSubject, String report);

	/**
	 * Remove a report from a subject
	 * 
	 * @param reportSubject
	 *          the report subject
	 * @param report
	 *          the report
	 * @return true when the report was removed (previously reported)
	 */
	public boolean remove(ReportSubject reportSubject, String report);

	/**
	 * Flush the subject (remove all reports)
	 * 
	 * @param reportSubject
	 *          the subject
	 */
	public void flush(ReportSubject reportSubject);
}
